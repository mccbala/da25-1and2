package da25.server;

import da25.base.Message;

/**
 * A single copy of a message, bearing also the recipient.
 * <p>
 * This class is only used internally by the network, processes only see Message
 * instances.
 * 
 * @author Stefano Tribioli
 * @author Casper Folkers
 * 
 */
public class MessageCopy extends Message {
	public int recipient;
	
	public MessageCopy(Message message, int recipient) {
		this.sender = message.sender;
		this.clock = message.clock;
		this.body = message.body;
		
		this.recipient = recipient;
	}
}
