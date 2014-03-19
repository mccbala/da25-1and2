package da25.base;

public class Constants {
	/**
	 * When a message has the recipient field set to BROADCAST, the network
	 * takes care of distributing it to all nodes (except the sender).
	 */
	public static final int BROADCAST = -1;

	/**
	 * When a message has the recipient field set to NETWORK, the message is
	 * intended to be processed by the network for control purposes and will not
	 * be forwarded.
	 */
	public static final int NETWORK = -2;

	/**
	 * Message body to signal readiness in synchronous networks.
	 * <p>
	 * When a process has sent all its messages for a round and is ready to
	 * receive the ones pertaining to the next one, it sends a control message
	 * with recipient Message.NETWORK and body READ_ROUND.
	 */
	public static final String READY_ROUND = "READY_ROUND";

	/**
	 * Message body to signal end of messages in synchronous networks.
	 * <p>
	 * When the network has sent all the messages of a round to a process, it
	 * sends a control message with sender Message.NETWORK and body PULSE_ROUND.
	 */
	public static final String PULSE_ROUND = "PULSE_ROUND";
}
