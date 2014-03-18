package da25.server;

import java.rmi.RemoteException;
import java.util.Random;
import java.util.Scanner;

import da25.base.Message;
import da25.base.exceptions.DuplicateIDException;
import da25.base.exceptions.LockedException;

/**
 * An instance of a network performing asynchronous message delivery. Messages
 * can be delivered automatically or waiting for user's directives.
 * 
 * @author Stefano Tribioli
 * @author Casper Folkers
 * 
 */
public class AsyncNetwork extends Network {
	/**
	 * A fixed amount of time to wait before dispatching the next message.
	 */
	public static final long DISPATCH_DELAY = 1000;

	/**
	 * A worker thread who regularly checks the message queue.
	 */
	private Thread worker;

	public AsyncNetwork() {
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
	protected boolean performCommand(Scanner scanner, String command) {
		switch (command) {
		case "next":
			forwardSingleSequentially();
			return true;
		case "flush":
			forwardAllSequentially();
			return true;
		case "rnd":
			forwardSingleRandomly();
			return true;
		case "rndflush":
			forwardAllRandomly();
			return true;
		case "auto":
			worker.start();
			return true;

		case "test1":
			testCase1();
			return true;
		case "test2":
			testCase2();
			return true;
		case "test3":
			testCase3();
			return true;
		case "test4":
			testCase4();
			return true;

		default:
			return super.performCommand(scanner, command);
		}
	}

	protected void forwardSingleRandomly() {
		synchronized (queue) {
			if (!queue.isEmpty()) {
				Random rnd = new Random();
				forwardMessage(rnd.nextInt(queue.size()));
			}
		}
	}

	protected void forwardAllRandomly() {
		synchronized (queue) {
			Random rnd = new Random();
			while (!queue.isEmpty()) {
				forwardMessage(rnd.nextInt(queue.size()));
			}
		}
	}

	/**
	 * Test case 1: This simple test case is equal to the one presented in the
	 * Lecture Slides (slide 6).
	 */
	private void testCase1() {
		try {
			for (int i = 0; i < 3; i++) {
				spawnProcess(AUTO_INCREMENT);	
			}
			lock();
		} catch (LockedException e) {
			System.out
					.println("Unable to spawn new process: network is locked.");
			return;
		} catch (DuplicateIDException e) {
			System.out
					.println("Unable to spawn new process: ID already in use.");
			return;
		}

		try {
			processes.get(1).sendMessage(Message.BROADCAST, "First broadcast");
			forwardMessage(0);
			processes.get(2).sendMessage(Message.BROADCAST, "Second broadcast");
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
	 * The network is composed of 5 processes (IDs 1 to 5), each of the first
	 * four processes sends a broadcast, but after receiving the one sent by the
	 * previous one. So, the broadcast from ID 4 is at the end of a chain
	 * comprising all the previous ones. In the meantime, messages are also
	 * received by process with ID 5 (who doesn't send a broadcast), with the
	 * single exception of the very first message. This way, since subsequent
	 * messages are dependent on the first one, the message with ID 5 has to
	 * keep them in the buffer. At last, also the first message is forwarded and
	 * it triggers the delivery of all the stored messages at process 5.
	 */
	private void testCase2() {
		try {
			for (int i = 0; i < 5; i++) {
				spawnProcess(AUTO_INCREMENT);	
			}
			lock();
		} catch (LockedException e) {
			System.out
					.println("Unable to spawn new process: network is locked.");
			return;
		} catch (DuplicateIDException e) {
			System.out
					.println("Unable to spawn new process: ID already in use.");
			return;
		}

		try {
			processes.get(1).sendMessage(Message.BROADCAST, "First broadcast");
			synchronized (queue) {
				for (int i = queue.size() - 1; i >= 0; i--) {
					if (queue.get(i).recipient != 5) {
						forwardMessage(i);
					}
				}
			}
			processes.get(2).sendMessage(Message.BROADCAST, "Second broadcast");
			synchronized (queue) {
				for (int i = queue.size() - 1; i > 0; i--) {
					forwardMessage(i);
				}
			}
			processes.get(3).sendMessage(Message.BROADCAST, "Third broadcast");
			synchronized (queue) {
				for (int i = queue.size() - 1; i > 0; i--) {
					forwardMessage(i);
				}
			}
			processes.get(4).sendMessage(Message.BROADCAST, "Fourth broadcast");
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
		try {
			for (int i = 0; i < 2; i++) {
				spawnProcess(AUTO_INCREMENT);	
			}
			lock();
		} catch (LockedException e) {
			System.out
					.println("Unable to spawn new process: network is locked.");
			return;
		} catch (DuplicateIDException e) {
			System.out
					.println("Unable to spawn new process: ID already in use.");
			return;
		}

		try {
			processes.get(1).sendMessage(Message.BROADCAST, "First broadcast");
			processes.get(1).sendMessage(Message.BROADCAST, "Second broadcast");
			processes.get(1).sendMessage(Message.BROADCAST, "Third broadcast");
			processes.get(1).sendMessage(Message.BROADCAST, "Fourth broadcast");

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
		try {
			for (int i = 0; i < 4; i++) {
				spawnProcess(AUTO_INCREMENT);	
			}
			lock();
		} catch (LockedException e) {
			System.out
					.println("Unable to spawn new process: network is locked.");
			return;
		} catch (DuplicateIDException e) {
			System.out
					.println("Unable to spawn new process: ID already in use.");
			return;
		}

		try {
			processes.get(1).sendMessage(Message.BROADCAST, "First broadcast");
			processes.get(2).sendMessage(Message.BROADCAST, "Second broadcast");
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
			processes.get(3).sendMessage(Message.BROADCAST, "Third broadcast");
			processes.get(4).sendMessage(Message.BROADCAST, "Fourth broadcast");

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
