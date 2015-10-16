import java.util.HashMap;

/**
 * This is the Peers class. Whenever a new peer is created, it has an associated
 * unique peer ID with it and also the cache with which it is associated.
 * 
 * @author shashank
 * 
 */
public class Peers {

	// Integer for unique peer ID.
	private int peerID;
	// Cache object with which the peer is associated with.
	private Caches c;
	// HashMap to store the respective peer with its peer ID.
	private static HashMap<Integer, Peers> idToPeer = new HashMap<Integer, Peers>();

	/**
	 * Default constructor.
	 */
	public Peers() {
	}

	/**
	 * Constructor which initializes the peer ID and the respective cache.
	 * 
	 * @param peerId
	 * @param c
	 */
	public Peers(int peerId, Caches c) {
		this.peerID = peerId;
		this.c = c;
		idToPeer.put(this.peerID, this);
	}

	/**
	 * Returns the peer object for the given peer ID.
	 * 
	 * @param id
	 * @return Peers
	 */
	public Peers getPeer(int id) {
		return idToPeer.get(id);
	}

	/**
	 * Returns the peer ID for a given peer object.
	 * 
	 * @return int
	 */
	public int getID() {
		return peerID;
	}

	/**
	 * Returns the cache with which the peer is associated.
	 * 
	 * @return Caches
	 */
	public Caches getCache() {
		return c;
	}
}