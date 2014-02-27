package da25.base;

import java.io.Serializable;

/**
 * A basic class representing a message. The message recipients are not
 * specified, so it can be a unicast, multicast or broadcast message.
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
	public int[] clock;
	public String body;
	
	public Message(int sender, int recipient, int[] clock, String body) {
		this.sender = sender;
		this.recipient = recipient;
		this.clock = clock;
		this.body = body;
	}
	
	@Override
	public String toString() {
		StringBuilder bld = new StringBuilder();
		bld.append("Message from "+this.sender+" saying: " + body +  " (");
		for (int i = 0; i < clock.length; i++) {
			bld.append(clock[i]);
			bld.append(",");
		}
		bld.deleteCharAt(bld.length()-1);
		bld.append(")");
		return bld.toString();
	}
}
