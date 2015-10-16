import edu.rit.numeric.ListSeries;
import edu.rit.sim.Simulation;

/**
 * This is the Request class. Each request has an id, cache object from where
 * the request originated, a request name associated with it.
 * 
 * @author shashank
 * 
 */
public class Requests {

	// Integer to store the request ID.
	private int id;
	// Cache object.
	private Caches c;
	// String to store the request name.
	private String requestName;
	// Simulation object.
	private Simulation sim;
	// ListSeries to store the response times of the requests.
	private ListSeries respTimeSeries;

	// Double to store the start time of the request. The start time is the time
	// when the request is added to the respective cache queue.
	private double startTime;
	// Double to store the finish time of the request.
	private double finishTime;
	// Boolean to indicate if the request has been served by any cache.
	public boolean requestServed;
	// Double to indicate the overall lookup time of the request.
	public double overallLookupTime;

	/**
	 * Constructor to initialize the variable. The overall lookup time for a
	 * request initially is 0.0 and also the requestServed value is false
	 * intially.
	 * 
	 * @param id
	 * @param c
	 * @param requestName
	 * @param sim
	 * @param respTimeSeries
	 */
	public Requests(int id, Caches c, String requestName, Simulation sim,
			ListSeries respTimeSeries) {
		this.id = id;
		this.c = c;
		this.requestName = requestName;
		this.sim = sim;
		this.startTime = sim.time();
		this.respTimeSeries = respTimeSeries;
		this.overallLookupTime = 0.0;
		this.requestServed = false;
	}

	/**
	 * This function returns the name of the request.
	 * 
	 * @return String
	 */
	public String getRequestName() {
		return requestName;
	}

	/**
	 * This function notes the finish time and stores the response time in the
	 * list series.
	 */
	public void finish() {
		this.finishTime = sim.time();
		if (respTimeSeries != null) {
			respTimeSeries.add(responseTime());
		}
	}

	/**
	 * This function calculates the response time of the request.
	 * 
	 * @return double
	 */
	public double responseTime() {
		return finishTime - startTime;
	}
}