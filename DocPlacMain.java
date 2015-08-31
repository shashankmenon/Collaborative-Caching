import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Scanner;
import edu.rit.numeric.AggregateXYSeries;
import edu.rit.numeric.ListSeries;
import edu.rit.numeric.Series;
import edu.rit.numeric.plot.Plot;
import edu.rit.sim.Simulation;
import edu.rit.util.Random;

public class DocPlacMain {

	// Double inter arrival rate i.e. rate at which requests come in.
	private static double interArrivalRate;
	// Integer for total no. of requests.
	private static int noOfRequests;
	// Double for mean cache processing time.
	private static double cacheProcTime;
	// Double for mean server cache processing time.
	private static double serverCacheProcTime;
	// Double for mean server disk processing time.
	private static double serverDiskProcTime;
	// File which contains the list of strings i.e. data items.
	private static String filename;
	// Integer for total no. of peers in the system.
	private static int noOfPeers;
	// Integer for total no. of caches in the system.
	private static int noOfCaches;
	// Seed value for random assignment of peers to their caches.
	private static Long seed;
	// Another seed value for randomness of requests.
	private static Long seedReq;
	// Integer for server data size.
	private static int serverDataSize = 0;

	// Cache objects.
	private static Caches cache1;
	private static Caches cache2;
	private static Caches cache3;
	private static Caches cache4;
	private static Caches cache5;
	private static Caches cache6;
	private static Caches cache7;
	private static Caches cache8;
	// Class generator object.
	private static Generator gen;
	// Class Server object.
	private static Server setupServer;

	// Simulation object.
	private static Simulation sim;

	// Random prng for random generation.
	private static Random prng;
	// String to store the scheme evaluation
	private static String whichScheme;
	// To store the contents of the server i.e. all the data items.
	private static ArrayList<String> listofdata;
	// ArrayList to store the usage efficiencies of all the three schemes.
	private static ArrayList<String> usageEfficiency;
	// Integer to store the size of the entire cache group.
	private static int sizeOfCacheGroup = 0;
	// Array to store the various cache group sizes on which the experiments are
	// carried.
	private static int[] cacheGroupSize = new int[6];
	// List series to store the average latency in Ad hoc Scheme.
	private static ListSeries latenciesAdHoc;
	private static ListSeries latenciesAdHocLocalCache;
	private static ListSeries latenciesAdHocRemoteCache;
	private static ListSeries latenciesAdHocServerCache;
	private static ListSeries latenciesAdHocServerDisk;
	// List series to store the average latency in Expiration Scheme.
	private static ListSeries latenciesExpiration;
	// List series to store the average latency in Modified Expiration Age
	// Scheme.
	private static ListSeries latenciesModExpiration;
	// // List series to store the Document Hit Ratio in Ad hoc Scheme.
	private static ListSeries docHitRatioAdHoc;
	private static ListSeries docHitRatioAdHocLocalCache;
	private static ListSeries docHitRatioAdHocRemoteCache;
	private static ListSeries docHitRatioAdHocServerCache;
	private static ListSeries docHitRatioAdHocServerDisk;
	// List series to store the Document Hit Ratio in Expiration Scheme.
	private static ListSeries docHitRatioExpiration;
	// List series to store the Document Hit Ratio in Modified Expiration Age
	// Scheme.
	private static ListSeries docHitRatioModExpiration;
	// List series to store the disk usage efficiency in Ad hoc scheme.
	private static ListSeries diskEffAdHoc;
	private static ListSeries diskEffAdHocLocalCache;
	private static ListSeries diskEffAdHocRemoteCache;
	private static ListSeries diskEffAdHocServerCache;
	private static ListSeries diskEffAdHocServerDisk;
	// List series to store the disk usage efficiency in Expiration scheme.
	private static ListSeries diskEffExpiration;
	// List series to store the disk usage efficiency in Modified Expiration
	// scheme.
	private static ListSeries diskEffModExpiration;
	// List series to store the various cache group sizes.
	private static ListSeries totalCacheSizes;
	private static ListSeries totalCacheSizesLocalCache;
	private static ListSeries totalCacheSizesRemoteCache;
	private static ListSeries totalCacheSizesServerCache;
	private static ListSeries totalCacheSizesServerDisk;

	// Random for peer to cache assignment.
	private static Random peerToCache;
	// Seed value for peer to cache assignment.
	private static int seedForPeerToCache = 51351;
	// String to indicate which application scenario is being used.
	private static String whichScenario;
	// String array to store the basecases.
	private static String[] baseCases = new String[4];
	// Boolean for base cases graph plotting.
	public static boolean flagForBaseGraphs = false;

	/**
	 * Takes the parameters from the Input File and calls the perform function.
	 * 
	 * @param args
	 * @throws FileNotFoundException
	 */
	public static void main(String args[]) throws FileNotFoundException {
		String InputFile = null;
		if (args.length == 1) {
			InputFile = args[0];
		}
		/**
		 * Reads from the Input file and stores the values of the parameters.
		 * Each iteration is seperated by a blank line in the input file.
		 */
		Scanner scanner = new Scanner(new File(InputFile));
		while (scanner.hasNextLine()) {
			String contents = scanner.nextLine();
			System.out.println(contents);
			if (contents.isEmpty()) {
				perform();
				continue;
			}
			String temp = contents.split("=")[0];
			String value = contents.split("=")[1];

			if (temp.equals("interArrivalRate")) {
				interArrivalRate = Double.parseDouble(value);
			} else if (temp.equals("noOfRequests")) {
				noOfRequests = Integer.parseInt(value);
			} else if (temp.equals("cacheProcTime")) {
				cacheProcTime = Double.parseDouble(value);
			} else if (temp.equals("serverCacheProcTime")) {
				serverCacheProcTime = Double.parseDouble(value);
			} else if (temp.equals("serverDiskProcTime")) {
				serverDiskProcTime = Double.parseDouble(value);
			} else if (temp.equals("filename")) {
				filename = value;
			} else if (temp.equals("noOfPeers")) {
				noOfPeers = Integer.parseInt(value);
			} else if (temp.equals("noOfCaches")) {
				noOfCaches = Integer.parseInt(value);
			} else if (temp.equals("seed")) {
				seed = Long.parseLong(value);
			} else if (temp.equals("seedReq")) {
				seedReq = Long.parseLong(value);
			} else if (temp.equals("serverDataSize")) {
				serverDataSize = Integer.parseInt(value);
			} else if (temp.equals("whichScenario")) {
				whichScenario = value;
			}
		}
	}

	/**
	 * This function sets up the server, assigns peers to caches, calls the
	 * generator class which is responsible for generating the requests
	 * randomly. Plots the graphs for average latency v/s cache group size and
	 * document hit ratio v/s cache group size for all the three schemes. Also,
	 * it starts independent execution for Expiration and Modified Expiration
	 * Scheme for the calculation of expiration ages of the caches periodically.
	 */
	public static void perform() {

		// Initializing the different cache group sizes.
		cacheGroupSize[0] = 1000;
		cacheGroupSize[1] = 1200;
		cacheGroupSize[2] = 1500;
		cacheGroupSize[3] = 1800;
		cacheGroupSize[4] = 2000;
		cacheGroupSize[5] = 2500;
		// cacheGroupSize[6] = 50000;

		baseCases[0] = "localCache";
		baseCases[1] = "remoteCache";
		baseCases[2] = "serverCache";
		baseCases[3] = "serverDisk";

		// Initializing the various list series.
		totalCacheSizes = new ListSeries();
		totalCacheSizesLocalCache = new ListSeries();
		totalCacheSizesRemoteCache = new ListSeries();
		totalCacheSizesServerCache = new ListSeries();
		totalCacheSizesServerDisk = new ListSeries();

		latenciesAdHoc = new ListSeries();
		latenciesAdHocLocalCache = new ListSeries();
		latenciesAdHocRemoteCache = new ListSeries();
		latenciesAdHocServerCache = new ListSeries();
		latenciesAdHocServerDisk = new ListSeries();
		latenciesExpiration = new ListSeries();
		latenciesModExpiration = new ListSeries();

		docHitRatioAdHoc = new ListSeries();
		docHitRatioAdHocLocalCache = new ListSeries();
		docHitRatioAdHocRemoteCache = new ListSeries();
		docHitRatioAdHocServerCache = new ListSeries();
		docHitRatioAdHocServerDisk = new ListSeries();
		docHitRatioExpiration = new ListSeries();
		docHitRatioModExpiration = new ListSeries();

		diskEffAdHoc = new ListSeries();
		diskEffAdHocLocalCache = new ListSeries();
		diskEffAdHocRemoteCache = new ListSeries();
		diskEffAdHocServerCache = new ListSeries();
		diskEffAdHocServerDisk = new ListSeries();
		diskEffExpiration = new ListSeries();
		diskEffModExpiration = new ListSeries();

		if (whichScenario.equals("baseCases")) {
			flagForBaseGraphs = true;
			for (int i = 0; i < baseCases.length; i++) {
				whichScenario = baseCases[i];
				System.out.println("Which scenario? = " + whichScenario);
				computation();
			}
			plotBaseGraphs();
		} else {
			computation();
			plotGraphs();
		}
	}

	public static void computation() {

		// Outer for loop iterates for all the cache group sizes.
		for (int groupSize = 0; groupSize < 6; groupSize++) {

			// Adding each cache group size to the totalCacheSizes ListSeries.
			if (whichScenario.equals("localCache") == true) {
				totalCacheSizesLocalCache.add(cacheGroupSize[groupSize]);
			} else if (whichScenario.equals("remoteCache") == true) {
				totalCacheSizesRemoteCache.add(cacheGroupSize[groupSize]);
			} else if (whichScenario.equals("serverCache") == true) {
				totalCacheSizesServerCache.add(cacheGroupSize[groupSize]);
			} else if (whichScenario.equals("serverDisk") == true) {
				totalCacheSizesServerDisk.add(cacheGroupSize[groupSize]);
			} else {
				totalCacheSizes.add(cacheGroupSize[groupSize]);
			}
			// Size of individual cache.
			int sizeOfCache = cacheGroupSize[groupSize] / noOfCaches;
			System.out.println();
			System.out.println("--------------- FOR " + noOfCaches
					+ " CACHES IN THE CACHE GROUP & "
					+ cacheGroupSize[groupSize]
					+ " in cache group ---------------");
			System.out
					.println("\t\tLocalHits\tRemoteHits\tSrvCacheHits\tSrvDiskHits\tDocHitRatio\tDiskEff   \tAverageLatency");

			// Inner for loop for all the three schemes.
			for (int schemeNo = 0; schemeNo < 3; schemeNo++) {
				peerToCache = Random.getInstance(seedForPeerToCache);
				if (schemeNo == 0) {
					whichScheme = "adhoc";
				} else if (schemeNo == 1) {
					whichScheme = "expiration";
				} else if (schemeNo == 2) {
					whichScheme = "modexpiration";
				}
				// Cache object to pass total number of caches and the size of
				// each cache.
				Caches c = new Caches(noOfCaches, sizeOfCache, whichScenario);
				// To clear all the counters in Class Caches.
				c.clearEverything();
				sizeOfCacheGroup = 0;
				usageEfficiency = new ArrayList<String>();

				prng = Random.getInstance(seed);

				// Server object to pass the server cache processing time,
				// server disk processing time and the filename where all the
				// data items are present.
				try {
					setupServer = new Server(filename, serverCacheProcTime,
							serverDiskProcTime, prng, whichScenario,
							(cacheGroupSize[groupSize] / noOfCaches));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				listofdata = setupServer.getData();

				// Creating the cache objects depending on the number of caches
				// in the experiment.
				if (noOfCaches == 2) {
					cache1 = new Caches("c1", cacheProcTime, setupServer, prng,
							whichScheme);

					cache2 = new Caches("c2", cacheProcTime, setupServer, prng,
							whichScheme);
					assignPeersToCaches_2();
				}

				else if (noOfCaches == 4) {
					cache1 = new Caches("c1", cacheProcTime, setupServer, prng,
							whichScheme);
					cache2 = new Caches("c2", cacheProcTime, setupServer, prng,
							whichScheme);
					cache3 = new Caches("c3", cacheProcTime, setupServer, prng,
							whichScheme);
					cache4 = new Caches("c4", cacheProcTime, setupServer, prng,
							whichScheme);
					assignPeersToCaches_4();
				}

				else if (noOfCaches == 8) {
					cache1 = new Caches("c1", cacheProcTime, setupServer, prng,
							whichScheme);
					cache2 = new Caches("c2", cacheProcTime, setupServer, prng,
							whichScheme);
					cache3 = new Caches("c3", cacheProcTime, setupServer, prng,
							whichScheme);
					cache4 = new Caches("c4", cacheProcTime, setupServer, prng,
							whichScheme);
					cache5 = new Caches("c5", cacheProcTime, setupServer, prng,
							whichScheme);
					cache6 = new Caches("c6", cacheProcTime, setupServer, prng,
							whichScheme);
					cache7 = new Caches("c7", cacheProcTime, setupServer, prng,
							whichScheme);
					cache8 = new Caches("c8", cacheProcTime, setupServer, prng,
							whichScheme);
					assignPeersToCaches_8();
				}

				else {
					System.out
							.println("Experimenting with Cache group of 2, 4 or 8 caches only!!");
					System.exit(1);
				}

				// Start of simulation.
				sim = new Simulation();

				// If the scheme being experimented with is Expiration Age
				// scheme or modified Expiration age scheme, then each cache
				// starts computing the expiration age in periodic intervals.
				if (whichScheme == "expiration"
						|| whichScheme == "modexpiration") {
					if (noOfCaches == 2) {
						cache2.computeExpirationAge();
						cache1.computeExpirationAge();
					} else if (noOfCaches == 4) {
						cache1.computeExpirationAge();
						cache2.computeExpirationAge();
						cache3.computeExpirationAge();
						cache4.computeExpirationAge();
					} else if (noOfCaches == 8) {
						cache1.computeExpirationAge();
						cache2.computeExpirationAge();
						cache3.computeExpirationAge();
						cache4.computeExpirationAge();
						cache5.computeExpirationAge();
						cache6.computeExpirationAge();
						cache7.computeExpirationAge();
						cache8.computeExpirationAge();
					}
				}

				// Generator object to generate the requests randomly. Requests
				// are originated from a random peer and the cache with which
				// the peer is associated with, is checked first since that is
				// the local cache.
				gen = new Generator(interArrivalRate, sim, noOfRequests,
						noOfPeers, listofdata, prng, seedReq, serverDataSize,
						whichScenario);

				// Run the simulation.
				sim.run();

				String str = String.format("%2.02f",
						(((float) c.getDocHitRatioCounter()) * 100)
								/ noOfRequests);
				double docHitRatioPerc = Double.parseDouble(str);

				// Series statistics to generate the mean response time.
				Series.Stats stats = gen.responseTimeStats();

				// For usage efficieny
				ArrayList<Caches> allCaches = c.getCaches();
				for (int i = 0; i < allCaches.size(); i++) {
					ArrayList<String> tempCacheDataList = allCaches.get(i)
							.getCacheDataList();

					sizeOfCacheGroup += tempCacheDataList.size();
					for (int j = 0; j < tempCacheDataList.size(); j++) {
						if (usageEfficiency.contains(tempCacheDataList.get(j))) {
							continue;
						} else {
							usageEfficiency.add(tempCacheDataList.get(j));
						}
					}
				}
				String temp = String.format("%2.02f",
						(((float) usageEfficiency.size()) * 100)
								/ sizeOfCacheGroup);
				double usageEff = Double.parseDouble(temp);

				if (whichScheme.equals("adhoc") == true) {
					System.out.print(whichScheme + "\t\t" + c.getLocalHits()
							+ "\t\t" + c.getRemoteHits() + "\t\t"
							+ setupServer.getServerCacheCounter() + "\t\t"
							+ setupServer.getServerDiskCounter() + "\t\t"
							+ docHitRatioPerc + "%\t\t" + usageEff + "%\t\t"
							+ stats.mean);
				} else {
					System.out.print(whichScheme + "\t" + c.getLocalHits()
							+ "\t\t" + c.getRemoteHits() + "\t\t"
							+ setupServer.getServerCacheCounter() + "\t\t"
							+ setupServer.getServerDiskCounter() + "\t\t"
							+ docHitRatioPerc + "%\t\t" + usageEff + "%\t\t"
							+ stats.mean);
				}
				System.out.println();

				if (whichScheme == "adhoc" && whichScenario == "localCache") {
					latenciesAdHocLocalCache.add(stats.mean);
					docHitRatioAdHocLocalCache.add(docHitRatioPerc);
					diskEffAdHocLocalCache.add(usageEff);
				} else if (whichScheme == "adhoc"
						&& whichScenario == "remoteCache") {
					latenciesAdHocRemoteCache.add(stats.mean);
					docHitRatioAdHocRemoteCache.add(docHitRatioPerc);
					diskEffAdHocRemoteCache.add(usageEff);
				} else if (whichScheme == "adhoc"
						&& whichScenario == "serverCache") {
					latenciesAdHocServerCache.add(stats.mean);
					docHitRatioAdHocServerCache.add(docHitRatioPerc);
					diskEffAdHocServerCache.add(usageEff);
				} else if (whichScheme == "adhoc"
						&& whichScenario == "serverDisk") {
					latenciesAdHocServerDisk.add(stats.mean);
					docHitRatioAdHocServerDisk.add(docHitRatioPerc);
					diskEffAdHocServerDisk.add(usageEff);
				} else if (whichScheme == "adhoc") {
					latenciesAdHoc.add(stats.mean);
					docHitRatioAdHoc.add(docHitRatioPerc);
					diskEffAdHoc.add(usageEff);
				} else if (whichScheme == "expiration") {
					latenciesExpiration.add(stats.mean);
					docHitRatioExpiration.add(docHitRatioPerc);
					diskEffExpiration.add(usageEff);
				} else if (whichScheme == "modexpiration") {
					latenciesModExpiration.add(stats.mean);
					docHitRatioModExpiration.add(docHitRatioPerc);
					diskEffModExpiration.add(usageEff);
				}

				// For expiration and modified expiration age scheme, to cancel
				// the expiration handler once it is done.
				if (whichScheme == "expiration"
						|| whichScheme == "modexpiration") {
					if (noOfCaches == 2) {
						cache2.cancelExpirationHandler();
						cache1.cancelExpirationHandler();
					} else if (noOfCaches == 4) {
						cache1.cancelExpirationHandler();
						cache2.cancelExpirationHandler();
						cache3.cancelExpirationHandler();
						cache4.cancelExpirationHandler();
					} else if (noOfCaches == 8) {
						cache1.cancelExpirationHandler();
						cache2.cancelExpirationHandler();
						cache3.cancelExpirationHandler();
						cache4.cancelExpirationHandler();
						cache5.cancelExpirationHandler();
						cache6.cancelExpirationHandler();
						cache7.cancelExpirationHandler();
						cache8.cancelExpirationHandler();
					}
				}

			}
		}

	}

	/*
	 * Graph for average latency vs cache group sizes.
	 */
	public static void plotGraphs() {
		Plot plot = new Plot();
		plot.plotTitle("Graph of Average Latency and Cache group sizes ")
				.xAxisTitle("Cache Group sizes").yAxisTitle("Average Latency");

		plot.leftMargin(100)
				.rightMargin(100)
				.bottomMargin(50)
				.xAxisTickFormat(new DecimalFormat("0.0"))
				.yAxisTickFormat(new DecimalFormat("0.0"))
				.xAxisStart(cacheGroupSize[0])
				.xAxisEnd(cacheGroupSize[5])
				.xAxisMajorDivisions(5)
				.seriesDots(null)
				.seriesColor(Color.BLUE)
				.xySeries(
						new AggregateXYSeries(totalCacheSizes, latenciesAdHoc))
				.seriesColor(Color.RED)
				.xySeries(
						new AggregateXYSeries(totalCacheSizes,
								latenciesExpiration))
				.seriesColor(Color.BLACK)
				.xySeries(
						new AggregateXYSeries(totalCacheSizes,
								latenciesModExpiration))
				.labelPosition(Plot.RIGHT)
				.labelOffset(6)
				.labelColor(Color.BLUE)
				.label("Ad Hoc Scheme",
						totalCacheSizes.x(totalCacheSizes.length() - 1),
						latenciesAdHoc.x(latenciesAdHoc.length() - 1))
				.labelColor(Color.RED)
				.label("Expiration Age Scheme",
						totalCacheSizes.x(totalCacheSizes.length() - 1),
						latenciesExpiration.x(latenciesExpiration.length() - 1))
				.labelColor(Color.BLACK)
				.label("Modified Expiration Age Scheme",
						totalCacheSizes.x(totalCacheSizes.length() - 1),
						latenciesModExpiration.x(latenciesModExpiration
								.length() - 1))

				.getFrame().setVisible(true);

		/**
		 * Graph for document hit ratio vs cache group sizes.
		 */
		if (whichScenario.equals("localCache") == true
				|| whichScenario.equals("remoteCache") == true
				|| whichScenario.equals("repetitive") == true
				|| whichScenario.equals("sequential") == true
				|| whichScenario.equals("popular") == true) {
			System.out.println("HERE?");
			Plot plot1 = new Plot();
			plot1.plotTitle(
					"Graph of document hit ratio and Cache group sizes ")
					.xAxisTitle("Cache Group sizes")
					.yAxisTitle("Document hit ratio");

			plot1.leftMargin(100)
					.rightMargin(100)
					.bottomMargin(50)
					.xAxisTickFormat(new DecimalFormat("0.0"))
					.yAxisTickFormat(new DecimalFormat("0.0"))
					.xAxisStart(cacheGroupSize[0])
					.xAxisEnd(cacheGroupSize[5])
					.xAxisMajorDivisions(5)
					.seriesDots(null)
					.seriesColor(Color.BLUE)
					.xySeries(
							new AggregateXYSeries(totalCacheSizes,
									docHitRatioAdHoc))
					.seriesColor(Color.RED)
					.xySeries(
							new AggregateXYSeries(totalCacheSizes,
									docHitRatioExpiration))
					.seriesColor(Color.BLACK)
					.xySeries(
							new AggregateXYSeries(totalCacheSizes,
									docHitRatioModExpiration))
					.labelPosition(Plot.RIGHT)
					.labelOffset(6)
					.labelColor(Color.BLUE)
					.label("Ad Hoc Scheme",
							totalCacheSizes.x(totalCacheSizes.length() - 1),
							docHitRatioAdHoc.x(docHitRatioAdHoc.length() - 1))
					.labelColor(Color.RED)
					.label("Expiration Age Scheme",
							totalCacheSizes.x(totalCacheSizes.length() - 1),
							docHitRatioExpiration.x(docHitRatioExpiration
									.length() - 1))
					.labelColor(Color.BLACK)
					.label("Modified Expiration Age Scheme",
							totalCacheSizes.x(totalCacheSizes.length() - 1),
							docHitRatioModExpiration.x(docHitRatioModExpiration
									.length() - 1))

					.getFrame().setVisible(true);
		}
		/**
		 * Graph for Disk Usage Efficiency v/s cache group sizes.
		 */
		if (whichScenario.equals("localCache") == true
				|| whichScenario.equals("remoteCache") == true
				|| whichScenario.equals("repetitive") == true
				|| whichScenario.equals("sequential") == true
				|| whichScenario.equals("popular") == true) {
			Plot plot2 = new Plot();
			plot2.plotTitle(
					"Graph of Disk Usage Efficiency and Cache group sizes ")
					.xAxisTitle("Cache Group sizes")
					.yAxisTitle("Disk Usage Efficiency");

			plot2.leftMargin(100)
					.rightMargin(100)
					.bottomMargin(50)
					.xAxisTickFormat(new DecimalFormat("0.0"))
					.yAxisTickFormat(new DecimalFormat("0.0"))
					.xAxisStart(cacheGroupSize[0])
					.xAxisEnd(cacheGroupSize[5])
					.xAxisMajorDivisions(5)
					.seriesDots(null)
					.seriesColor(Color.BLUE)
					.xySeries(
							new AggregateXYSeries(totalCacheSizes, diskEffAdHoc))
					.seriesColor(Color.RED)
					.xySeries(
							new AggregateXYSeries(totalCacheSizes,
									diskEffExpiration))
					.seriesColor(Color.BLACK)
					.xySeries(
							new AggregateXYSeries(totalCacheSizes,
									diskEffModExpiration))
					.labelPosition(Plot.RIGHT)
					.labelOffset(6)
					.labelColor(Color.BLUE)
					.label("Ad Hoc Scheme",
							totalCacheSizes.x(totalCacheSizes.length() - 1),
							diskEffAdHoc.x(diskEffAdHoc.length() - 1))
					.labelColor(Color.RED)
					.label("Expiration Age Scheme",
							totalCacheSizes.x(totalCacheSizes.length() - 1),
							diskEffExpiration.x(diskEffExpiration.length() - 1))
					.labelColor(Color.BLACK)
					.label("Modified Expiration Age Scheme",
							totalCacheSizes.x(totalCacheSizes.length() - 1),
							diskEffModExpiration.x(diskEffModExpiration
									.length() - 1))

					.getFrame().setVisible(true);
		}

	}

	public static void plotBaseGraphs() {
		Plot plot = new Plot();
		plot.plotTitle("Graph of Average Latency and Cache group sizes")
				.xAxisTitle("Cache Group sizes").yAxisTitle("Average Latency");

		plot.leftMargin(100)
				.rightMargin(100)
				.bottomMargin(50)
				.xAxisTickFormat(new DecimalFormat("0.0"))
				.yAxisTickFormat(new DecimalFormat("0.0"))
				.xAxisStart(cacheGroupSize[0])
				.xAxisEnd(cacheGroupSize[5])
				.xAxisMajorDivisions(5)
				.seriesDots(null)
				.seriesColor(Color.BLUE)
				.xySeries(
						new AggregateXYSeries(totalCacheSizesLocalCache,
								latenciesAdHocLocalCache))
				.seriesColor(Color.RED)
				.xySeries(
						new AggregateXYSeries(totalCacheSizesRemoteCache,
								latenciesAdHocRemoteCache))
				.seriesColor(Color.BLACK)
				.xySeries(
						new AggregateXYSeries(totalCacheSizesServerCache,
								latenciesAdHocServerCache))
				.seriesColor(Color.DARK_GRAY)
				.xySeries(
						new AggregateXYSeries(totalCacheSizesServerDisk,
								latenciesAdHocServerDisk))
				.labelPosition(Plot.RIGHT)
				// .labelOffset(6)
				.labelColor(Color.BLUE)
				.label("Local Cache",
						totalCacheSizesLocalCache.x(totalCacheSizesLocalCache
								.length() - 1),
						latenciesAdHocLocalCache.x(latenciesAdHocLocalCache
								.length() - 1))
				.labelColor(Color.RED)
				.label("Remote Cache",
						totalCacheSizesRemoteCache.x(totalCacheSizesRemoteCache
								.length() - 1),
						latenciesAdHocRemoteCache.x(latenciesAdHocRemoteCache
								.length() - 1))
				.labelColor(Color.BLACK)
				.label("Server Cache",
						totalCacheSizesServerCache.x(totalCacheSizesServerCache
								.length() - 1),
						latenciesAdHocServerCache.x(latenciesAdHocServerCache
								.length() - 1))
				.labelColor(Color.DARK_GRAY)
				.label("Server Disk",
						totalCacheSizesServerDisk.x(totalCacheSizesServerDisk
								.length() - 1),
						latenciesAdHocServerDisk.x(latenciesAdHocServerDisk
								.length() - 1))

				.getFrame().setVisible(true);
	}

	// Assign peers to caches when total number of caches in the system is 2.
	public static void assignPeersToCaches_2() {
		for (int j = 0; j < noOfPeers; j++) {
			int whichCache = peerToCache.nextInt(2);
			if (whichCache == 0) {
				cache1.setNode(j, new Peers(j, cache1));
			}
			if (whichCache == 1) {
				cache2.setNode(j, new Peers(j, cache2));
			}
		}
	}

	// Assign peers to caches when total number of caches in the system is 4.
	public static void assignPeersToCaches_4() {
		for (int j = 0; j < noOfPeers; j++) {
			int whichCache = peerToCache.nextInt(4);
			if (whichCache == 0) {
				cache1.setNode(j, new Peers(j, cache1));
			}
			if (whichCache == 1) {
				cache2.setNode(j, new Peers(j, cache2));
			}
			if (whichCache == 2) {
				cache3.setNode(j, new Peers(j, cache3));
			}
			if (whichCache == 3) {
				cache4.setNode(j, new Peers(j, cache4));
			}
		}
	}

	// Assign peers to caches when total number of caches in the system is 8.
	public static void assignPeersToCaches_8() {
		for (int j = 0; j < noOfPeers; j++) {
			int whichCache = peerToCache.nextInt(8);
			if (whichCache == 0) {
				cache1.setNode(j, new Peers(j, cache1));
			}
			if (whichCache == 1) {
				cache2.setNode(j, new Peers(j, cache2));
			}
			if (whichCache == 2) {
				cache3.setNode(j, new Peers(j, cache3));
			}
			if (whichCache == 3) {
				cache4.setNode(j, new Peers(j, cache4));
			}
			if (whichCache == 4) {
				cache5.setNode(j, new Peers(j, cache5));
			}
			if (whichCache == 5) {
				cache6.setNode(j, new Peers(j, cache6));
			}
			if (whichCache == 6) {
				cache7.setNode(j, new Peers(j, cache7));
			}
			if (whichCache == 7) {
				cache8.setNode(j, new Peers(j, cache8));
			}
		}
	}
}