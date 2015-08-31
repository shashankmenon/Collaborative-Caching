import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Scanner;
import edu.rit.numeric.ExponentialPrng;
import edu.rit.sim.Event;
import edu.rit.sim.Simulation;
import edu.rit.util.Random;

/**
 * This is the server class. It has a mean server processing time associated
 * with it and this is exponentially distributed. Also, the server class takes
 * the data from a file which consists of random strings. In the simulation,
 * this is the all the data for which nodes request for.
 * 
 * Here the server is not an actual server but it is a gateway to the internet.
 * And it is assumed that whatever data the client request is found in the
 * internet.
 * 
 * @author shashank
 * 
 */
public class Server {

	// Array list to store the data from the file.
	private static ArrayList<String> data;

	// Double to store the mean server processing time.
	private double serverProcTime;

	// Simulation sim.
	private Simulation sim;

	// Exponential prng for exponentially distributing the mean server
	// cache processing time.
	private ExponentialPrng servCacheProc;

	// Exponential prng for exponentially distributing the mean server disk
	// processing time.
	private ExponentialPrng servDiskProc;

	// Linked list to store the requests as they arrive in the server cache.
	private LinkedList<Requests> serverCacheQueue;
	// Array list to represent the server cache itself.
	private ArrayList<String> serverCacheDataList;
	// Counter for the server cache hits.
	private static int serverCacheCounter;
	// Counter for the server disk hits.
	private static int serverDiskCounter;
	// Double to introduce network delay.
	private double networkDelay;
	// Double to indicate the server cache processing time.
	private double serverCacheProcTime;
	// Double to indicate the server disk processing time.
	private double serverDiskProcTime;
	// String to indicate the application scenario being used.
	private String whichScenario;
	// Integer to indicate the size of the cache.
	private int sizeOfCache;

	/**
	 * Default constructor.
	 */
	public Server() {
	}

	/**
	 * This constructor takes the filename and copies all the data to the
	 * arraylist.
	 * 
	 * @param filename
	 * @param serverCacheProcTime
	 * @param serverDiskProcTime
	 * @param prng
	 * @throws IOException
	 */
	public Server(String filename, double serverCacheProcTime,
			double serverDiskProcTime, Random prng, String whichScenario,
			int sizeOfCache) throws IOException {
		this.serverCacheProcTime = serverCacheProcTime;
		this.serverDiskProcTime = serverDiskProcTime;
		this.servCacheProc = new ExponentialPrng(prng,
				1.0 / serverCacheProcTime);
		this.servDiskProc = new ExponentialPrng(prng, 1.0 / serverDiskProcTime);
		this.whichScenario = whichScenario;
		this.sizeOfCache = sizeOfCache;
		data = new ArrayList<String>();
		serverCacheQueue = new LinkedList<Requests>();
		serverCacheDataList = new ArrayList<String>();
		serverCacheCounter = 0;
		serverDiskCounter = 0;
		Scanner scanner = null;
		try {
			scanner = new Scanner(new File(filename));
			while (scanner.hasNextLine()) {
				String contents = scanner.nextLine();
				data.add(contents);
			}
		} finally {
			if (scanner != null)
				scanner.close();
		}
	}

	/**
	 * Returns the data array list.
	 * 
	 * @return Arraylist
	 */
	public ArrayList<String> getData() {
		return data;
	}

	/**
	 * This function adds the request to the server cache queue and if the
	 * server cache queue size is one, it starts processes the request.
	 * 
	 * @param request
	 * @param sim
	 */
	public void add(final Requests request, Simulation sim, double networkDelay) {
		this.sim = sim;
		this.networkDelay = networkDelay;
		serverCacheQueue.add(request);
		if (this.serverCacheQueue.size() == 1) {
			this.searchServerCache();
		}
	}

	/**
	 * This function searches the server cache. If it is found in the server
	 * cache, then it calls the serverCacheFinishProcessing() method which
	 * finishes the request processing. Otherwise, the data item is assumed to
	 * be found in the server disk and thus it finishes processing in either
	 * case. However, the processing time of the former is less than the
	 * processing time of the latter since server cache is faster than the
	 * server disk.
	 */
	public void searchServerCache() {
		final Requests request = serverCacheQueue.getFirst();
		if (!serverCacheDataList.isEmpty()
				&& serverCacheDataList.contains(request.getRequestName())) {

			serverCacheCounter++;
			serverCacheDataList.remove(request.getRequestName());
			serverCacheDataList.add(request.getRequestName());
			sim.doAfter((serverCacheProcTime + networkDelay), new Event() {
				public void perform() {
					serverFinishProcessing();
				}
			});
		} else {
			serverDiskCounter++;
			if (whichScenario.equals("repetitive") == true
					|| whichScenario.equals("sequential") == true
					|| whichScenario.equals("pattern") == true
					|| whichScenario.equals("serverCache") == true
					|| whichScenario.equals("popular") == true) {
				serverCacheDataList.add(request.getRequestName());
				if (serverCacheDataList.size() > (sizeOfCache * 2)) {
					serverCacheDataList.remove(0);
				}
			}
			double abcd = (serverDiskProcTime + networkDelay);
			sim.doAfter(abcd, new Event() {
				public void perform() {
					serverFinishProcessing();
				}
			});
		}
	}

	/**
	 * Calls the finish method which adds the response times of the requests to
	 * the list series.
	 */
	private void serverFinishProcessing() {
		Requests request = serverCacheQueue.removeFirst();

		request.finish();
		if (!serverCacheQueue.isEmpty())
			searchServerCache();
	}

	/**
	 * Returns the name of the request.
	 * 
	 * @param request
	 * @return String
	 */
	public String getReqData(final Requests request) {
		return request.getRequestName();
	}

	/**
	 * Returns the server disk hits.
	 * 
	 * @return int
	 */
	public int getServerDiskCounter() {
		return serverDiskCounter;
	}

	/**
	 * Returns the server cache hits.
	 * 
	 * @return int
	 */
	public int getServerCacheCounter() {
		return serverCacheCounter;
	}

}