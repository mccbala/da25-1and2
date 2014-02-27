package da25.server;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Random;

import da25.base.Message;
import da25.base.NetworkInterface;
import da25.base.ProcessInterface;

/**
 * Singleton simulating an asynchronous message delivery and keeping track of
 * all running processes.
 * 
 * @author Stefano Tribioli
 * @author Casper Folkers
 * 
 */
public class Network implements NetworkInterface {
	/**
	 * A fixed amount of time to wait before dispatching the next message.
	 */
	public static final long DISPATCH_DELAY = 1000;

	/**
	 * List holding references for all processes.
	 */
	protected ArrayList<ProcessInterface> processes = new ArrayList<>();
	/**
	 * List holding the messages waiting to be dispatched.
	 */
	protected ArrayList<Message> queue = new ArrayList<>();
	/**
	 * A worker thread who regularly checks the message queue.
	 */
	private Thread worker;

	public Network() {
		worker = new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					synchronized (queue) {
						if (!queue.isEmpty()) {
							dispatchMessage();
						}
					}
					
					try {
						Thread.sleep(10000);
					} catch (InterruptedException e) {}
				}
			}
		});
		worker.start();
	}
	
	@Override
	public int register(ProcessInterface process) throws RemoteException {
		synchronized (processes) {
			processes.add(process);
//			for (int i = 0; i < processes.size(); i++) {
//				processes.get(i).newProcess(processes.size() - 1);
//			}
			return processes.size() - 1;
		}
	}

	@Override
	public int getCount() throws RemoteException {
		return processes.size();
	}

	@Override
	public void sendMessage(Message message, int recipient)
			throws RemoteException {
		synchronized (queue) {
			queue.add(message);
		}
	}

	@Override
	public void sendMessage(Message message) throws RemoteException {
		synchronized (processes) {
			if (message.recipient == Message.BROADCAST) {
				for (int i = 0; i < processes.size(); i++) {
					if (i != message.sender) {
						queue.add(new Message(message.sender, i, message.clock, message.body));
					}
				}
			} else {
				synchronized (queue) {
					queue.add(message);
				}
			}
		}
	}

	/**
	 * Selects a random message from the queue and dispatches it to the
	 * recipient.
	 */
	private void dispatchMessage() {
		Random rnd = new Random();
		Message message;

		synchronized (queue) {
			int index = rnd.nextInt(queue.size());
			message = queue.remove(index);
		}

		try {
			processes.get(message.recipient).recieveMessage(message);
		} catch (RemoteException e) {
			System.out
					.println("Unable to send message [" + message.toString()
							+ "] sent by " + message.sender + " to "
							+ message.recipient);
		}
	}
}
