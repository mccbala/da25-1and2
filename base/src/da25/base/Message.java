package da25.base;

/**
 * A basic class representing a message. The message recipients are not
 * specified, so it can be a unicast, multicast or broadcast message.
 * 
 * @author Stefano Tribioli
 * @author Casper Folkers
 * 
 */
public class Message {
	public int sender;
	public int[] clock;
	public String body;
	
	@Override
	public String toString() {
		StringBuilder bld = new StringBuilder();
		bld.append("Message (");
		for (int i = 0; i < clock.length; i++) {
			bld.append(clock[i]);
			bld.append(",");
		}
		bld.deleteCharAt(bld.length());
		bld.append(")");
		return bld.toString();
	}
}
