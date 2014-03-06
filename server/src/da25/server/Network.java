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
					if (locked) {
						break;
					} else {
						locked = true;
					}

					for (ProcessInterface process : processes) {
						try {
							process.start();
						} catch (RemoteException e) {
							throw new RuntimeException(e);
						}
					}
					break;
				case "test1":
					testCase1();
					break;
				case "test2":
					testCase2();
					break;
				case "test3":
					testCase3();
					break;
				case "test4":
					testCase4();
					break;
				case "next":
					forwardSingleSequentially();
					break;
				case "flush":
					forwardAllSequentially();
					break;
				case "rnd":
					forwardSingleRandomly();
					break;
				case "rndflush":
					forwardAllRandomly();
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
						System.out.println(messageCopy
								+ " put in queue. Queue size is "
								+ queue.size());
					}
				}
			} else {
				synchronized (queue) {
					queue.add(message);
					System.out.println(message
							+ " put in queue. Queue size is " + queue.size());
				}
			}
		}
	}

	private void forwardSingleRandomly() {
		synchronized (queue) {
			if (!queue.isEmpty()) {
				Random rnd = new Random();
				forwardMessage(rnd.nextInt(queue.size()));
			}
		}
	}

	private void forwardSingleSequentially() {
		forwardMessage(0);
	}

	private void forwardAllSequentially() {
		synchronized (queue) {
			while (!queue.isEmpty()) {
				forwardMessage(0);
			}
		}
	}

	private void forwardAllRandomly() {
		synchronized (queue) {
			Random rnd = new Random();
			while (!queue.isEmpty()) {
				forwardMessage(rnd.nextInt(queue.size()));
			}
		}
	}

	/**
	 * Dispatches a message from the queue to the recipient.
	 */
	private void forwardMessage(int index) {
		Message message;

		synchronized (queue) {
			message = queue.remove(index);
		}

		try {
			System.out.println("Forwarding " + message);
			processes.get(message.recipient).recieveMessage(message);
		} catch (RemoteException e) {
			System.out
					.println("Unable to send message " + message.toString()
							+ " sent by " + message.sender + " to "
							+ message.recipient);
		}
	}

	/**
	 * Test case 1: This simple test case is equal to the one presented in the
	 * Lecture Slides (slide 6).
	 */
	private void testCase1() {
		if (!locked || processes.size() != 3) {
			System.out.println("Test case 1 requires exactly three clients.");
			return;
		}

		try {
			processes.get(0).sendMessage(Message.BROADCAST, "First broadcast");
			forwardMessage(0);
			processes.get(1).sendMessage(Message.BROADCAST, "Second broadcast");
			forwardMessage(2);
			forwardMessage(0);
			forwardMessage(0);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Test case 2: this one shows how a single arrival can trigger the delivery
	 * of multiple waiting messages.
	 * <p>
	 * The network is composed of 5 processes (IDs 0 to 4), each of the first
	 * four processes sends a broadcast, but after receiving the one sent by the
	 * previous one. So, the broadcast from ID 3 is at the end of a chain
	 * comprising all the previous ones. In the meantime, messages are also
	 * received by process with ID 4 (who doesn't send a broadcast), with the
	 * single exception of the very first message. This way, since subsequent
	 * messages are dependent on the first one, the message with ID 4 has to
	 * keep them in the buffer. At last, also the first message is forwarded and
	 * it triggers the delivery of all the stored messages at process 4.
	 */
	private void testCase2() {
		if (!locked || processes.size() != 5) {
			System.out.println("Test case 2 requires exactly five clients.");
			return;
		}

		try {
			processes.get(0).sendMessage(Message.BROADCAST, "First broadcast");
			synchronized (queue) {
				for (int i = queue.size() - 1; i >= 0; i--) {
					if (queue.get(i).recipient != 4) {
						forwardMessage(i);
					}
				}
			}
			processes.get(1).sendMessage(Message.BROADCAST, "Second broadcast");
			synchronized (queue) {
				for (int i = queue.size() - 1; i > 0; i--) {
					forwardMessage(i);
				}
			}
			processes.get(2).sendMessage(Message.BROADCAST, "Third broadcast");
			synchronized (queue) {
				for (int i = queue.size() - 1; i > 0; i--) {
					forwardMessage(i);
				}
			}
			processes.get(3).sendMessage(Message.BROADCAST, "Fourth broadcast");
			synchronized (queue) {
				for (int i = queue.size() - 1; i > 0; i--) {
					forwardMessage(i);
				}
			}

			forwardMessage(0);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Test case 3: again showing how a late message will trigger multiple
	 * deliveries, but this time the chain of dependencies is inside the same
	 * process.
	 * <p>
	 * A buffer can also be full of messages from the same source depending from
	 * a single first message from that source. This is to show that FIFO links
	 * are not needed for this algorithm to work correctly.
	 */
	private void testCase3() {
		if (!locked || processes.size() != 2) {
			System.out.println("Test case 3 requires exactly two clients.");
			return;
		}

		try {
			processes.get(0).sendMessage(Message.BROADCAST, "First broadcast");
			processes.get(0).sendMessage(Message.BROADCAST, "Second broadcast");
			processes.get(0).sendMessage(Message.BROADCAST, "Third broadcast");
			processes.get(0).sendMessage(Message.BROADCAST, "Fourth broadcast");

			synchronized (queue) {
				for (int i = queue.size() - 1; i >= 0; i--) {
					forwardMessage(i);
				}
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Test case 4: showing when the algorithm is not supposed to buffer
	 * messages.
	 * <p>
	 * Causal message ordering doesn't mean that messages have to be delivered
	 * exactly in the same order that they were generated, of course. The only
	 * goal is to rearrange messages that could depend from each other.
	 * <p>
	 * Here, four processes send broadcasts sequentially. We even introduced a
	 * one second delay to make it more clear. Then, these messages are
	 * broadcasted in a reverse order, but this is perfectly fine, since they
	 * are concurrent from the point of view of the processes. So they are
	 * delivered without buffering.
	 */
	private void testCase4() {
		if (!locked || processes.size() != 4) {
			System.out.println("Test case 4 requires exactly four clients.");
			return;
		}

		try {
			processes.get(0).sendMessage(Message.BROADCAST, "First broadcast");
			processes.get(1).sendMessage(Message.BROADCAST, "Second broadcast");
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
			processes.get(2).sendMessage(Message.BROADCAST, "Third broadcast");
			processes.get(3).sendMessage(Message.BROADCAST, "Fourth broadcast");

			synchronized (queue) {
				for (int i = queue.size() - 1; i >= 0; i--) {
					forwardMessage(i);
				}
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
}
