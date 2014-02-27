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
	public static final int BROADCAST = -1;
	
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
		StringBuilder bld = new StringBuilder();
		bld.append("Message from "+sender+" saying: " + body +  " (");
		
		bld.append(clock.toString());
		bld.append(")");
		return bld.toString();
	}
}
