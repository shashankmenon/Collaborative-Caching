import java.util.ArrayList;
import edu.rit.numeric.ExponentialPrng;
import edu.rit.numeric.ListSeries;
import edu.rit.numeric.Series;
import edu.rit.sim.Event;
import edu.rit.sim.Simulation;
import edu.rit.util.Random;

/**
 * This is the generator class. The main task of this class is to generate
 * requests and add them to respective caches queue.
 * 
 * @author shashank
 * 
 */
public class Generator {

	// Integer to represent the number of requests.
	private int noOfRequests;
	// Simulation object.
	private Simulation sim;
	// Exponential prng for exponential distribution of mean inter arrival rate.
	private ExponentialPrng treqPrng;
	// Integer to represent the number of peers.
	private int noOfPeers;
	// ArrayList to store the contents of the server i.e. all data items.
	private ArrayList<String> listofdata;
	// Random to get random request.
	private Random randForRequests;
	// Random to get random Peer.
	private Random randForPeers;
	// Listseries to store the response times of requests.
	private ListSeries respTimeSeries;
	private Long seed_req;
	// Static integer to keep track of the number of requests generated.
	private static int n;
	// Static peer counter.
	private static int counterForPeer;
	// Static Request counter.
	private static int counterForReq;
	// Integer to represent the total size of the server.
	private int serverDataSize;
	// String to indicate which scenario is being used.
	private String whichScenario;
	private static int randomRemotePeer = 1;
	private static Long newSeedReq;
	// Integer to select a random request.
	private static int whichReq;
	// Integer to select a random peer.
	private static int whichPeer;

	/**
	 * This constructor initializes various parameters and variables.
	 * 
	 * @param interArrivalRate
	 * @param sim
	 * @param noOfRequests
	 * @param noOfPeers
	 * @param listofdata
	 * @param prng
	 * @param seed_req
	 * @param servDataSize
	 */
	public Generator(double interArrivalRate, Simulation sim, int noOfRequests,
			int noOfPeers, ArrayList<String> listofdata, Random prng,
			Long seed_req, int serverDataSize, String whichScenario) {
		this.noOfRequests = noOfRequests;
		this.sim = sim;
		this.treqPrng = new ExponentialPrng(prng, 1.0 / interArrivalRate);
		this.noOfPeers = noOfPeers;
		this.listofdata = listofdata;
		this.seed_req = seed_req;
		randForRequests = Random.getInstance(seed_req);
		this.newSeedReq = ++seed_req;
		randForPeers = Random.getInstance(newSeedReq);
		respTimeSeries = new ListSeries();
		this.serverDataSize = serverDataSize;
		this.whichScenario = whichScenario;
		if (whichScenario.equals("localCache") == true
				|| whichScenario.equals("serverCache") == true) {
			n = 0;
			generateRequestsLocal(noOfRequests, noOfPeers, treqPrng, listofdata);
		} else if (whichScenario.equals("remoteCache")) {
			n = 0;
			Peers peer = new Peers();
			Peers temp = peer.getPeer(5);
			Peers temp1 = peer.getPeer(randomRemotePeer);
			while (temp.getCache().toString()
					.equals(temp1.getCache().toString())) {
				temp1 = peer.getPeer(++randomRemotePeer);
			}
			int whichReq = 14133;
			Requests r = new Requests(temp.getID(), temp.getCache(),
					listofdata.get(whichReq), sim, respTimeSeries);
			temp.getCache().add(r, sim);
			++n;

			whichReq = 1311;
			Requests r1 = new Requests(temp.getID(), temp.getCache(),
					listofdata.get(whichReq), sim, respTimeSeries);
			temp1.getCache().add(r1, sim);
			++n;

			generateRequestsRemote(noOfRequests, noOfPeers, treqPrng,
					listofdata);
		} else if (whichScenario.equals("serverDisk") == true) {
			n = 0;
			generateRequests(noOfRequests, noOfPeers, treqPrng, listofdata);
		} else if (whichScenario.equals("repetitive") == true) {
			n = 0;
			counterForPeer = 0;
			counterForReq = 0;
			generateRequestsRepetitive(noOfRequests, noOfPeers, treqPrng,
					listofdata);
		} else if (whichScenario.equals("sequential") == true) {
			n = 0;
			counterForPeer = 0;
			counterForReq = 0;
			generateRequestsSequential(noOfRequests, noOfPeers, treqPrng,
					listofdata);
		} else if (whichScenario.equals("popular") == true) {
			n = 0;
			generateRequests(noOfRequests, noOfPeers, treqPrng, listofdata);
		}
	}

	/**
	 * This function is for generating requests when all the data items are
	 * found in the remote caches.
	 * 
	 * @param noOfRequests
	 * @param noOfPeers
	 * @param treqPrng
	 * @param listofdata
	 */
	public void generateRequestsRemote(final int noOfRequests,
			final int noOfPeers, final ExponentialPrng treqPrng,
			final ArrayList<String> listofdata) {
		Peers peer = new Peers();
		this.treqPrng = treqPrng;
		Peers temp = null;
		int whichReq = 0;
		if (n <= noOfRequests / 2) {
			temp = peer.getPeer(5);
			whichReq = 1311;
		} else if (n > noOfRequests / 2) {
			temp = peer.getPeer(randomRemotePeer);
			whichReq = 14133;
		}
		Requests r = new Requests(temp.getID(), temp.getCache(),
				listofdata.get(whichReq), sim, respTimeSeries);
		temp.getCache().add(r, sim);
		++n;
		if (n < noOfRequests) {
			sim.doAfter(this.treqPrng.next(), new Event() {
				public void perform() {
					generateRequestsRemote(noOfRequests, noOfPeers, treqPrng,
							listofdata);
				}
			});
		}
	}

	/**
	 * This function is for generating requests when all data items are found in
	 * the local cache.
	 * 
	 * @param noOfRequests
	 * @param noOfPeers
	 * @param treqPrng
	 * @param listofdata
	 */
	public void generateRequestsLocal(final int noOfRequests,
			final int noOfPeers, final ExponentialPrng treqPrng,
			final ArrayList<String> listofdata) {
		Peers peer = new Peers();
		this.treqPrng = treqPrng;
		Peers temp = null;
		int whichReq = 0;
		if (n <= noOfRequests / 2) {
			temp = peer.getPeer(5);
			whichReq = 1413;
		} else if (n > noOfRequests / 2) {
			temp = peer.getPeer(53);
			whichReq = 8741;
		}
		Requests r = new Requests(temp.getID(), temp.getCache(),
				listofdata.get(whichReq), sim, respTimeSeries);
		temp.getCache().add(r, sim);
		++n;
		if (n < noOfRequests) {
			sim.doAfter(this.treqPrng.next(), new Event() {
				public void perform() {
					generateRequestsLocal(noOfRequests, noOfPeers, treqPrng,
							listofdata);
				}
			});
		}
	}

	/**
	 * This function is for generating requests when the application scenario is
	 * "repetitive".
	 * 
	 * @param noOfRequests
	 * @param noOfPeers
	 * @param treqPrng
	 * @param listofdata
	 */
	public void generateRequestsRepetitive(final int noOfRequests,
			final int noOfPeers, final ExponentialPrng treqPrng,
			final ArrayList<String> listofdata) {
		this.treqPrng = treqPrng;
		if (counterForReq % 3 == 0) {
			if (n < noOfRequests / 2) {
				whichReq = getRandomData();
			} else {
				whichReq = getRandomData1();
			}
		}
		Peers peer = new Peers();
		int whichPeer = getRandomPeer(noOfPeers);
		Peers temp = peer.getPeer(whichPeer);
		Requests r = new Requests(temp.getID(), temp.getCache(),
				listofdata.get(whichReq), sim, respTimeSeries);
		temp.getCache().add(r, sim);
		++n;
		++counterForPeer;
		++counterForReq;

		if (n < noOfRequests) {
			sim.doAfter(this.treqPrng.next(), new Event() {
				public void perform() {
					generateRequestsRepetitive(noOfRequests, noOfPeers,
							treqPrng, listofdata);
				}
			});
		}
	}

	/**
	 * This function is for generation requests when application scenario is
	 * "sequential".
	 * 
	 * @param noOfRequests
	 * @param noOfPeers
	 * @param treqPrng
	 * @param listofdata
	 */
	public void generateRequestsSequential(final int noOfRequests,
			final int noOfPeers, final ExponentialPrng treqPrng,
			final ArrayList<String> listofdata) {
		Peers peer = new Peers();
		this.treqPrng = treqPrng;
		if (counterForPeer % 10 == 0) {
			whichPeer = getRandomPeer(noOfPeers);
		}
		Peers temp = peer.getPeer(whichPeer);
		if (counterForReq % 10 == 0) {
			if (n <= noOfRequests / 2) {
				whichReq = getRandomSequential();
			} else {
				whichReq = getRandomSequential1();
			}
		} else {
			whichReq = whichReq + 1;
		}
		Requests r = new Requests(temp.getID(), temp.getCache(),
				listofdata.get(whichReq), sim, respTimeSeries);
		temp.getCache().add(r, sim);
		++n;
		++counterForReq;
		++counterForPeer;
		if (n < noOfRequests) {
			sim.doAfter(this.treqPrng.next(), new Event() {
				public void perform() {
					generateRequestsSequential(noOfRequests, noOfPeers,
							treqPrng, listofdata);
				}
			});
		}
	}

	/**
	 * This function generates the requests originating from any random peer.
	 * 
	 * @param noOfRequests
	 * @param noOfPeers
	 * @param treqPrng
	 * @param listofdata
	 */
	public void generateRequests(final int noOfRequests, final int noOfPeers,
			final ExponentialPrng treqPrng, final ArrayList<String> listofdata) {
		Peers peer = new Peers();
		this.treqPrng = treqPrng;
		int whichPeer = getRandomPeer(noOfPeers);
		Peers temp = peer.getPeer(whichPeer);
		int whichReq;
		if (n % 2 == 0) {
			whichReq = getRandomData();
		} else {
			whichReq = getRandomData1();
		}
		Requests r = new Requests(temp.getID(), temp.getCache(),
				listofdata.get(whichReq), sim, respTimeSeries);
		temp.getCache().add(r, sim);
		++n;
		if (n < noOfRequests) {
			sim.doAfter(this.treqPrng.next(), new Event() {
				public void perform() {
					generateRequests(noOfRequests, noOfPeers, treqPrng,
							listofdata);
				}
			});
		}
	}

	/**
	 * This function returns a random peer between 0 and (noOfPeers - 1).
	 * 
	 * @param noOfPeers
	 * @return int
	 */
	public int getRandomPeer(int noOfPeers) {
		return randForPeers.nextInt(noOfPeers);
	}

	/**
	 * This function returns any random request. This is for the popular items.
	 * Half of the requests are for popular items. Used for "Repetitive"
	 * application scenario.
	 * 
	 * @return int
	 */

	public int getRandomData() {
		return randForRequests.nextInt(500);
	}

	/**
	 * This function returns any random request and these are the non popular
	 * items. Half of the requests are for non popular items. Used for
	 * "Repetitive" application scenario.
	 * 
	 * @return int
	 */
	public int getRandomData1() {
		if (serverDataSize != 0) {
			return randForRequests.nextInt(serverDataSize - 500 + 1) + 500;
		} else {
			return randForRequests.nextInt(2000 - 500 + 1) + 500;
		}
	}

	/**
	 * This function returns any random request and these are the popular
	 * items. Half of the requests are for popular items. Used for
	 * "Sequential" application scenario.
	 * 
	 * @return int
	 */
	public int getRandomSequential() {
		int randNumber = randForRequests.nextInt(500);
		return randNumber;
	}
	
	/**
	 * This function returns any random request and these are the non popular
	 * items. Half of the requests are for non popular items. Used for
	 * "Sequential" application scenario.
	 * 
	 * @return int
	 */
	public int getRandomSequential1() {
		int randNumber = randForRequests.nextInt(2000 - 500 + 1) + 500;
		return randNumber;
	}

	/**
	 * This function returns the statistics of the response times list series.
	 * 
	 * @return Series.Stats
	 */
	public Series.Stats responseTimeStats() {
		return respTimeSeries.stats();
	}

	/**
	 * This function returns the list series of the response times.
	 * 
	 * @return Series
	 */
	public Series responseTimeSeries() {
		return respTimeSeries;
	}
}