package da25.server;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Scanner;

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
	/**
	 * 
	 */
	protected boolean locked = false;

	public Network() {
		worker = new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					synchronized (queue) {
						if (!queue.isEmpty()) {
							Random rnd = new Random();
							forwardMessage(rnd.nextInt(queue.size()));
						}
					}

					try {
						Thread.sleep(DISPATCH_DELAY);
					} catch (InterruptedException e) {
					}
				}
			}
		});
	}

	@Override
	public int register(ProcessInterface process) throws RemoteException {
		if (locked) {
			throw new RemoteException("Network has been already locked.");
		}

		synchronized (processes) {
			processes.add(process);
			System.out.println("Added new process with id "
					+ (processes.size() - 1));
			return processes.size() - 1;
		}
	}

	@Override
	public int getCount() throws RemoteException {
		return processes.size();
	}

	/**
	 * RMI operations are concluded, starts actual elaboration.
	 */
	public void start() {
		System.out.println("Network is running, waiting for clients.");

		Thread parser = new Thread(new Runnable() {
			@Override
			public void run() {
				parseIn();
			}
		});
		parser.start();
	}

	/**
	 * Blocks waiting for user input.
	 */
	private void parseIn() {
		Scanner scanner = new Scanner(System.in);
		while (true) {
			try {
				String command = scanner.nextLine();
				switch (command) {
				case "lock":
					for (ProcessInterface process : processes) {
						try {
							process.start();
						} catch (RemoteException e) {
							throw new RuntimeException();
						}
					}
					locked = true;
					break;
				case "flush":
					synchronized (queue) {
						while (!queue.isEmpty()) {
							forwardMessage(0);
						}
					}
					break;
				case "rnd":
					synchronized (queue) {
						if (!queue.isEmpty()) {
							Random rnd = new Random();
							forwardMessage(rnd.nextInt(queue.size()));
						}
					}
					break;
				case "rndflush":
					synchronized (queue) {
						Random rnd = new Random();
						while (!queue.isEmpty()) {
							forwardMessage(rnd.nextInt(queue.size()));
						}
					}
					break;
				case "auto":
					worker.start();
					break;
				case "exit":
					scanner.close();
					for (ProcessInterface process : processes) {
						try {
							process.exit();
						} catch (RemoteException e) {
							// The call will always throw a SocketException
							// since the client terminates before sending return
							// value, but this is fine for us.
						}
					}
					System.exit(0);
				default:
					System.out.println("Unknown command");
					break;
				}
			} catch (NoSuchElementException e) {
			}
		}
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
						Message messageCopy = new Message(message.sender, i,
								message.clock, message.body);
						queue.add(messageCopy);
						System.out.println("[" + messageCopy.toString()
								+ "] put in queue. Queue size is "
								+ queue.size());
					}
				}
			} else {
				synchronized (queue) {
					queue.add(message);
					System.out.println("[" + message.toString()
							+ "] put in queue. Queue size is " + queue.size());
				}
			}
		}
	}

	/**
	 * Selects a random message from the queue and dispatches it to the
	 * recipient.
	 */
	private void forwardMessage(int index) {
		Message message;

		synchronized (queue) {
			message = queue.remove(index);
		}

		try {
			System.out.println("Dispatching [" + message + "]");
			processes.get(message.recipient).recieveMessage(message);
		} catch (RemoteException e) {
			System.out.println("Unable to send message [" + message.toString()
					+ "] sent by " + message.sender + " to "
					+ message.recipient);
		}
	}
}
