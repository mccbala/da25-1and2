package da25.base;

import java.io.Serializable;

/**
 * A basic class representing a message.
 * 
 * @author Stefano Tribioli
 * @author Casper Folkers
 * 
 */
public class Message implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 * When recipient is set to BROADCAST, the network takes care of
	 * distributing it to all nodes (except the sender).
	 */
	public static final int BROADCAST = -1;
	
	/**
	 * When recipient is set to NETWORK, the message is intended to be processed
	 * by the network for control purposes and will not be forwarded.
	 */
	public static final int NETWORK = -2;

	public int sender;
	public int recipient;
	public VectorClock clock;
	public String body;

	public Message(int sender, int recipient, VectorClock clock, String body) {
		this.sender = sender;
		this.recipient = recipient;
		this.clock = clock;
		this.body = body;
	}

	@Override
	public String toString() {
		return "[Message from " + sender + " to " + recipient + ", VC" + clock
				+ ", saying '" + body + "']";
	}
}
