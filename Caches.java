import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import static java.util.concurrent.TimeUnit.*;

import edu.rit.numeric.ExponentialPrng;
import edu.rit.numeric.ListSeries;
import edu.rit.sim.Event;
import edu.rit.sim.Simulation;
import edu.rit.util.Random;

/**
 * This is the Cache Class. It has a name, cache queue and a cache data list
 * which is the cache itself. It also has a cache processing time associated
 * with it. Each cache also has an expiration age associated with itself which
 * is used when for expiration and modified expiration age schemes.
 * 
 * @author Shashank
 * 
 */
public class Caches {

	// String to store the cache name.
	private String cacheName;
	// Double for mean cache processing time.
	private double cacheProcTime;
	// Server object.
	private Server serv;
	// Array list to store the cache objects.
	private static ArrayList<Caches> cacheList = new ArrayList<Caches>();
	// For exponential distribution over the mean cache processing time.
	private ExponentialPrng tprocPrng1;
	// Queue to store the requests coming in the cache.
	private LinkedList<Requests> cacheQueue;
	// Array list to store the data items of the cache. It represents the
	// actually cache itself.
	private ArrayList<String> cacheDataList;
	// Double value to store the expiration age i.e. the approximate time for
	// which a data item is expected to stay in the cache.
	private double expirationAge;

	// Various counters for statistics.
	private static int docHitRatioCounter = 0;
	private static int docMissRatioCounter = 0;
	private static int localHits = 0;
	private static int remoteHits = 0;

	// Integer for number of caches in the system.
	private static int noOfCaches;
	// Integer for the size of each cache.
	private static int sizeOfCache;

	// Scheduled executor service used in the computation of expiration age.
	private final ScheduledExecutorService scheduler = Executors
			.newScheduledThreadPool(noOfCaches);

	// HashMap to store the request to time mapping i.e. the time at which the
	// request is added in the cache is stored. It is later used in the
	// computation of expiration age.
	private HashMap<String, Double> reqToTimeMapping;

	// List series to store the lifetimes of each data item evicted from the
	// cache.
	private ListSeries storeLifetimes;

	// String to indicate which scheme is being processed.
	private String whichScheme;
	// Hashmap to store the cache id to peer mapping.
	private HashMap<Integer, Peers> idToPeerMapping = new HashMap<Integer, Peers>();

	// HashMap to store the frequency of a particular request on a cache. This
	// is used during modified expiration age scheme.
	private HashMap<String, Integer> freqOfRequests;
	// Random to get random delay between any two caches.
	private Random randDelayCache;

	// Simulation object.
	private Simulation sim;
	// ScheduledFuture object for expiration age computation.
	private ScheduledFuture expirationHandler;
	// String to indicate which application scenario is being used.
	private static String whichScenario;

	/**
	 * Caches class constructor to initialize the total number of caches and the
	 * size of each cache.
	 * 
	 * @param noOfCaches
	 * @param sizeOfCache
	 */
	public Caches(int noOfCaches, int sizeOfCache, String whichScenario) {
		this.noOfCaches = noOfCaches;
		this.sizeOfCache = sizeOfCache;
		this.whichScenario = whichScenario;
	}

	/**
	 * Returns the expiration age of the cache.
	 * 
	 * @return double
	 */
	public double getExpirationAge() {
		return expirationAge;
	}

	/**
	 * Method for LRU cache replacement policy when the cache is full. Also, the
	 * eviction time is noted to compute the lifetime of each data item which
	 * has been evicted.
	 */
	public void cacheReplacement() {
		if (this.cacheDataList.size() > sizeOfCache) {
			double initialTime = this.reqToTimeMapping.get(cacheDataList
					.remove(0));
			double evictionTime = sim.time() - initialTime;
			this.storeLifetimes.add(evictionTime);
		}
	}

	/**
	 * This function calls the computer() function periodically which computes
	 * the expiration age of the cache.
	 */
	public void computeExpirationAge() {
		final Runnable expAge = new Runnable() {
			public void run() {
				compute();
			}
		};
		this.expirationHandler = scheduler.scheduleAtFixedRate(expAge, 0, 10,
				MILLISECONDS);
	}

	/**
	 * This function cancel the expiration handler of the cache.
	 */
	public void cancelExpirationHandler() {
		this.expirationHandler.cancel(true);
	}

	/**
	 * This function computes the mean of the lifetimes of the data items i.e.
	 * the expiration age.
	 */
	public void compute() {
		if (this.storeLifetimes.length() != 0) {
			this.expirationAge = this.storeLifetimes.stats().mean;
			this.storeLifetimes.clear();
		}
	}

	/**
	 * Caches constructor which initializes various cache parameters.
	 * 
	 * @param cacheName
	 * @param cacheProcTime
	 * @param serv
	 * @param prng
	 * @param whichScheme
	 */
	public Caches(String cacheName, double cacheProcTime, Server serv,
			Random prng, String whichScheme) {
		this.cacheName = cacheName;
		this.cacheProcTime = cacheProcTime;
		this.tprocPrng1 = new ExponentialPrng(prng, 1.0 / cacheProcTime);
		cacheQueue = new LinkedList<Requests>();
		cacheDataList = new ArrayList<String>();
		this.serv = serv;
		cacheList.add(this);
		this.whichScheme = whichScheme;
		reqToTimeMapping = new HashMap<String, Double>();
		storeLifetimes = new ListSeries();
		expirationAge = Double.POSITIVE_INFINITY;
		freqOfRequests = new HashMap<String, Integer>();
		randDelayCache = Random.getInstance(8855);
	}

	/**
	 * This toString() Method which returns the name of the cache.
	 */
	public String toString() {
		return cacheName;
	}

	/**
	 * This function clears the various counters. This is required since the
	 * counters have to be reset for different schemes.
	 */
	public void clearEverything() {
		docHitRatioCounter = 0;
		docMissRatioCounter = 0;
		localHits = 0;
		remoteHits = 0;
		cacheList.clear();
	}

	/**
	 * This function returns the cache data list i.e. array list of the
	 * respective cache.
	 * 
	 * @return ArrayList<String>
	 */
	public ArrayList<String> getCacheDataList() {
		return cacheDataList;
	}

	/**
	 * This function associates peers to a cache.
	 * 
	 * @param i
	 * @param n
	 */
	public void setNode(int i, Peers n) {
		idToPeerMapping.put(i, n);
	}

	/**
	 * Returns the number of associated with this cache.
	 * 
	 * @return int
	 */
	public int getNoOfPeersPerCache() {
		return idToPeerMapping.size();
	}

	/**
	 * This function adds a request to the cache queue. It calls the
	 * searchCache() method which processes the request and checks it first in
	 * the local cache and then in the remote caches.
	 * 
	 * @param request
	 * @param sim
	 */
	public void add(final Requests request, Simulation sim) {
		this.sim = sim;
		this.cacheQueue.add(request);
		if (this.cacheQueue.size() == 1) {
			this.searchCache();
		}
	}

	/**
	 * This function searches the caches for all the three schemes. For modified
	 * expiration age, it maintains the frequency of each request on a cache. If
	 * the request cannot be served by the local cache or remote caches, the
	 * request is forwarded to the server.
	 */
	public void searchCache() {
		final Requests request = cacheQueue.getFirst();

		// Calculating frequency of requests for MODIFIED EXPIRATION AGE SCHEME.
		if (whichScheme == "modexpiration") {
			if (this.freqOfRequests.containsKey(request.getRequestName())) {
				int temp = this.freqOfRequests.get(request.getRequestName());
				temp = temp + 1;
				this.freqOfRequests.put(request.getRequestName(), temp);
			} else {
				this.freqOfRequests.put(request.getRequestName(), 1);
			}
		}

		// Checking on the caches starting from the local cache.
		for (int i = cacheList.indexOf(this); i < cacheList.size(); i++) {
			ArrayList<String> tempCacheDataList = cacheList.get(i).cacheDataList;
			if (cacheList.get(i) == this) {
				request.overallLookupTime = this.cacheProcTime;
			} else {
				request.overallLookupTime += cacheList.get(i).cacheProcTime
						+ (getDelay() * getNumberOfRoutersBetwCaches());
			}
			// Searching the cache.
			if (!tempCacheDataList.isEmpty()
					&& tempCacheDataList.contains(request.getRequestName())) {
				if (cacheList.get(i) == this) {
					localHits++;
				} else {
					/**
					 * AD HOC SCHEME
					 */
					if (whichScheme.equals("adhoc")) {
						if (whichScenario.equals("remoteCache") == false) {
							cacheDataList.add(request.getRequestName());
							if (cacheDataList.size() > sizeOfCache) {
								cacheDataList.remove(0);
							}
						}
						tempCacheDataList.remove(request.getRequestName());
						tempCacheDataList.add(request.getRequestName());
					}

					/**
					 * EXPIRATION AGE
					 */
					if (whichScheme.equals("expiration")) {
						if (this.getExpirationAge() > cacheList.get(i)
								.getExpirationAge()) {

							this.cacheDataList.add(request.getRequestName());
							this.reqToTimeMapping.put(request.getRequestName(),
									sim.time());

							// check for cache replacement
							this.cacheReplacement();
						} else {
							cacheList.get(i).cacheDataList.remove(request
									.getRequestName());
							cacheList.get(i).cacheDataList.add(request
									.getRequestName());
							cacheList.get(i).reqToTimeMapping.put(
									request.getRequestName(), sim.time());
						}
					}

					/**
					 * MODIFIED EXPIRATION AGE
					 */
					if (whichScheme == "modexpiration") {
						if (this.getExpirationAge() > cacheList.get(i)
								.getExpirationAge()
								|| this.freqOfRequests.get(request
										.getRequestName()) > 5) {
							// Copying it in requestor cache.
							if (whichScenario.equals("remoteCache") == false) {
								this.cacheDataList
										.add(request.getRequestName());
							}
							this.reqToTimeMapping.put(request.getRequestName(),
									sim.time());
							// Checking for cache replacement.
							this.cacheReplacement();
						} else {
							tempCacheDataList.remove(request.getRequestName());
							tempCacheDataList.add(request.getRequestName());
							cacheList.get(i).reqToTimeMapping.put(
									request.getRequestName(), sim.time());
						}
					}

					remoteHits++;
				}

				request.requestServed = true;
				docHitRatioCounter++;
				sim.doAfter(request.overallLookupTime, new Event() {
					public void perform() {
						endProcessing();
					}
				});
				break;
			}
		}

		// If request is still not served, then check other caches. Caches are
		// searched in a round robin fashion.
		if (request.requestServed == false) {
			for (int i = 0; i < cacheList.indexOf(this); i++) {
				ArrayList<String> tempCache = cacheList.get(i).cacheDataList;
				request.overallLookupTime += cacheList.get(i).cacheProcTime
						+ (getDelay() * getNumberOfRoutersBetwCaches());
				if (!tempCache.isEmpty()
						&& tempCache.contains(request.getRequestName())) {
					if (cacheList.get(i) == this) {
						localHits++;
					} else {

						/**
						 * AD HOC SCHEME
						 */
						if (whichScheme.equals("adhoc")) {
							if (whichScenario.equals("remoteCache") == false) {
								this.cacheDataList
										.add(request.getRequestName());
								if (this.cacheDataList.size() > sizeOfCache) {
									this.cacheDataList.remove(0);
								}
							}
							tempCache.remove(request.getRequestName());
							tempCache.add(request.getRequestName());
						}

						/**
						 * EXPIRATION AGE SCHEME
						 */
						if (whichScheme.equals("expiration")) {
							if (this.getExpirationAge() > cacheList.get(i)
									.getExpirationAge()) {
								this.cacheDataList
										.add(request.getRequestName());
								this.reqToTimeMapping.put(
										request.getRequestName(), sim.time());
								// check for cache replacement
								this.cacheReplacement();

							} else {
								cacheList.get(i).cacheDataList.remove(request
										.getRequestName());
								cacheList.get(i).cacheDataList.add(request
										.getRequestName());
								cacheList.get(i).reqToTimeMapping.put(
										request.getRequestName(), sim.time());
							}
						}

						/**
						 * MODIFIED EXPIRATION AGE SCHEME
						 */
						if (whichScheme == "modexpiration") {
							if (this.getExpirationAge() > cacheList.get(i)
									.getExpirationAge()
									|| this.freqOfRequests.get(request
											.getRequestName()) > 5) {

								// Copying it in requestor cache.
								if (whichScenario.equals("remoteCache") == false) {
									this.cacheDataList.add(request
											.getRequestName());
								}
								this.reqToTimeMapping.put(
										request.getRequestName(), sim.time());
								this.cacheReplacement();
							} else {
								// Updating it in the responder cache.
								tempCache.remove(request.getRequestName());
								tempCache.add(request.getRequestName());
								cacheList.get(i).reqToTimeMapping.put(
										request.getRequestName(), sim.time());
							}
						}
						remoteHits++;
					}

					request.requestServed = true;
					docHitRatioCounter++;

					sim.doAfter(request.overallLookupTime, new Event() {
						public void perform() {
							endProcessing();
						}
					});
					break;
				}
			}
		}
		// If request was not served by any of the caches, the request is
		// forwarded to the server.
		if (request.requestServed == false) {
			cacheQueue.removeFirst();
			docMissRatioCounter++;
			double delay = 0;
			for (int routerCount = 0; routerCount < getNumberOfRoutersBetwServer(); routerCount++) {
				delay += getDelay();
			}
			// System.out.println(request.overallLookupTime);
			serv.add(request, sim, (delay + request.overallLookupTime));
			String newEntry = serv.getReqData(request);
			/**
			 * LRU policy.
			 */
			if (whichScenario.equals("localCache") == true
					|| whichScenario.equals("remoteCache") == true
					|| whichScenario.equals("repetitive") == true
					|| whichScenario.equals("sequential") == true
					|| whichScenario.equals("popular") == true) {
				this.cacheDataList.add(newEntry);
			}

			if (whichScheme == "expiration" || whichScheme == "modexpiration") {
				this.reqToTimeMapping.put(newEntry, sim.time());
				this.cacheReplacement();
			}

			else {
				if (this.cacheDataList.size() > sizeOfCache) {
					this.cacheDataList.remove(0);
				}
			}
			if (!cacheQueue.isEmpty()) {
				searchCache();
			}
		}

	}

	/**
	 * This function ends the processing of a request and calls the finish()
	 * method which records the end time of the request. This is used in
	 * calculating the average latency.
	 */
	public void endProcessing() {
		Requests request = cacheQueue.removeFirst();
		request.finish();
		if (!cacheQueue.isEmpty())
			searchCache();
	}

	/**
	 * This function returns the mean of the lifetimes of the data items evicted
	 * from the cache in a certain period of time. This is basically the
	 * expiration age of the cache.
	 * 
	 * @return double
	 */
	public double calculateExpirationAge() {
		return storeLifetimes.stats().mean;
	}

	/**
	 * This function returns a random delay between 0 and 1 to represent the
	 * delay in the network.
	 * 
	 * @return double
	 */
	public double getDelay() {
		return 0.5;
	}

	/**
	 * This function returns the number of routers between a cache and the
	 * server.
	 * 
	 * @return int
	 */
	public int getNumberOfRoutersBetwServer() {
		return 3;
	}

	/**
	 * This function returns the number of routers between two caches.
	 * 
	 * @return int
	 */
	public int getNumberOfRoutersBetwCaches() {
		return 1;
	}

	/**
	 * This function returns the document hit counter.
	 * 
	 * @return int
	 */
	public int getDocHitRatioCounter() {
		return docHitRatioCounter;
	}

	/**
	 * This function returns the document miss counter.
	 * 
	 * @return int
	 */
	public int getDocMissRatioCounter() {
		return docMissRatioCounter;
	}

	/**
	 * This function returns the list of all the cache objects.
	 * 
	 * @return Arraylist<Caches>
	 */
	public ArrayList<Caches> getCaches() {
		return cacheList;
	}

	/**
	 * This function returns the number of local hits encountered.
	 * 
	 * @return int
	 */
	public int getLocalHits() {
		return localHits;
	}

	/**
	 * This function returns the number of remote hits encountered.
	 * 
	 * @return int
	 */
	public int getRemoteHits() {
		return remoteHits;
	}
}