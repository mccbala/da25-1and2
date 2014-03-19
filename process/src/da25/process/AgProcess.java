package da25.process;

import java.rmi.RemoteException;

import da25.base.Message;
import da25.base.VectorClock;

public class AgProcess extends Process {
	@Override
	public void recieveMessage(Message message) throws RemoteException {
		// TODO
	}

	@Override
	public void sendMessage(int recipient, String body) throws RemoteException {
		synchronized (clock) {
			clock.increase(id);
			Message message = new Message(id, recipient,
					new VectorClock(clock), body);
			try {
				network.sendMessage(message);
			} catch (RemoteException e) {
				System.out.println("Unable to send message " + message
						+ ", because of: " + e.getMessage());
			}
		}
	}
}
