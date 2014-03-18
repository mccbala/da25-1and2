package da25.server;

import java.rmi.RemoteException;

import da25.base.Message;

/**
 * An instance of a network performing synchronous message delivery.
 * <p>
 * The mechanism is very simple: message are forwarded as soon as they reach the
 * queue; the entire operation is in a synchronized block, so only one message
 * (or different copies of the same message, in case of a broadcast operation)
 * can reside in the queue at a given time.
 * 
 * @author Stefano Tribioli
 * @author Casper Folkers
 * 
 */
public class SyncNetwork extends Network {
	@Override
	public void sendMessage(Message message) throws RemoteException {
		synchronized (queue) {
			super.sendMessage(message);
			forwardAllSequentially();
		}
	}

	@Override
	public void sendMessage(Message message, int recipient)
			throws RemoteException {
		synchronized (queue) {
			super.sendMessage(message, recipient);
			forwardAllSequentially();
		}
	}
}
