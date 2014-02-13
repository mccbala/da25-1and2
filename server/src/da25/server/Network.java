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
	private ArrayList<ProcessInterface> processes = new ArrayList<>();
	/**
	 * List holding the messages waiting to be dispatched.
	 */
	private ArrayList<MessageCopy> queue = new ArrayList<>();
	/**
	 * Flag indicating whether a dispatch thread is present in the system.
	 */
	private Boolean dispatchPending = false;

	@Override
	public int register(ProcessInterface process) throws RemoteException {
		synchronized (processes) {
			processes.add(process);
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
		MessageCopy copy = new MessageCopy(message, recipient);
		synchronized (queue) {
			queue.add(copy);
			scheduleDispatch();
		}
	}

	@Override
	public void sendMessage(Message message) throws RemoteException {
		synchronized (processes) {
			for (int i = 0; i < processes.size(); i++) {
				if (i != message.sender) {
					sendMessage(message, i);
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
		MessageCopy message;

		synchronized (queue) {
			int index = rnd.nextInt(queue.size());
			message = queue.remove(index);
		}

		try {
			processes.get(message.recipient).processMessage(message.getOriginal());
		} catch (RemoteException e) {
			System.out
					.println("Unable to send message [" + message.toString()
							+ "] sent by " + message.sender + " to "
							+ message.recipient);
		}

		synchronized (queue) {
			dispatchPending = false;
			if (queue.size() != 0) {
				scheduleDispatch();
			}
		}
	}

	/**
	 * Schedules a dispatch with a fixed delay.
	 */
	private void scheduleDispatch() {
		synchronized (dispatchPending) {
			if (dispatchPending) {
				return;
			} else {
				dispatchPending = true;
			}
		}

		Thread dispatcher = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}

				Network.this.dispatchMessage();
			}
		});
		dispatcher.run();
	}
}
