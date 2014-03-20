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
		if (clock != null) {
			return "[Message from " + sender + " to " + recipient + ", VC"
					+ clock + ", saying '" + body + "']";
		} else {
			return "[Message from " + sender + " to " + recipient
					+ ", saying '" + body + "']";
		}
	}

	public String toString(int largestId) {
		if (clock != null) {
			return "[Message from " + sender + " to " + recipient + ", VC"
					+ clock + ", saying '" + body
					+ "']";
		} else {
			return toString();
		}
	}
}
