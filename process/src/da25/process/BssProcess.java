package da25.process;

import java.rmi.RemoteException;
import java.util.ArrayList;

import da25.base.Message;
import da25.base.VectorClock;

/**
 * The concrete implementation of a process for Assignment 1:
 * Birman-Schiper-Stephenson algorithm for causal ordering of broadcast
 * messages.
 * 
 * @author Stefano Tribioli
 * @author Casper Folkers
 * 
 */
public class BssProcess extends Process {
	private ArrayList<Message> buffer = new ArrayList<Message>();

	@Override
	public void recieveMessage(Message message) throws RemoteException {
		synchronized (clock) {
			clock.increase(message.sender);
			if (clock.greaterEqual(message.clock)) {
				deliverMessage(message);

				boolean newUpdate = true;
				while (newUpdate) {
					newUpdate = false;
					for (int i = 0; i < buffer.size(); i++) {
						Message nextMessage = buffer.get(i);
						clock.increase(nextMessage.sender);
						if (clock.greaterEqual(nextMessage.clock)) {
							newUpdate = true;
							deliverMessage(nextMessage);
							buffer.remove(i);
							i--;
						} else {
							clock.decrease(nextMessage.sender);
						}
					}
				}
			} else {
				System.out.println(message + " put in buffer");
				clock.decrease(message.sender);
				buffer.add(message);
			}
		}
	}

	@Override
	public void sendMessage(int recipient, String body) {
		synchronized (clock) {
			clock.increase(id);
			Message message = new Message(id, recipient,
					new VectorClock(clock), body);
			try {
				network.sendMessage(message);
			} catch (RemoteException e) {
				clock.decrease(id);
				System.out.println("Unable to send message " + message
						+ ", because of: " + e.getMessage());
			}
		}
	}
}
