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
	private static final long serialVersionUID = 1L;
	
	public int recipient;
	
	public MessageCopy(Message message, int recipient) {
		super(message.sender, message.clock, message.body);
		
		this.recipient = recipient;
	}
	
	public Message getOriginal() {
		return new Message(sender, clock, body);
	}
}
