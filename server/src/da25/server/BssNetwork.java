package da25.server;

import java.rmi.RemoteException;
import java.util.Random;
import java.util.Scanner;

import da25.base.Constants;
import da25.base.ProcessInterface;
import da25.base.exceptions.DuplicateIDException;
import da25.base.exceptions.LockedException;
import da25.process.BssProcess;

/**
 * The actual network for assignment 1, complete with a test suite.
 * 
 * @author Stefano Tribioli
 * @author Casper Folkers
 * 
 */
public class BssNetwork extends AsyncNetwork {
	public BssNetwork() {
		super(BssProcess.class);
	}

	@Override
	protected boolean performCommand(Scanner scanner, String command) {
		try {
			switch (command) {
			case "test1":
				testCase1();
				return true;
			case "test2":
				testCase2(true);
				return true;
			case "test3":
				testCase3();
				return true;
			case "test4":
				testCase4();
				return true;
			case "test5":
				testCase5();
				return true;
			case "test6":
				testCase6();
				return true;
			default:
				return super.performCommand(scanner, command);
			}
		} catch (LockedException e) {
			System.out.println("Unable to populate a non-empty network.");
			return true;
		} catch (Exception e) {
			e.printStackTrace(System.out);
			return true;
		}
	}

	/**
	 * Test case 1: This simple test case is equal to the one presented in the
	 * Lecture Slides (slide 6).
	 */
	private void testCase1() throws LockedException, DuplicateIDException {
		populateNetwork(3);

		try {
			processes.get(1)
					.sendMessage(Constants.BROADCAST, "First broadcast");
			forwardMessage(0);
			processes.get(2).sendMessage(Constants.BROADCAST,
					"Second broadcast");
			forwardMessage(2);
			forwardMessage(0);
			forwardMessage(0);
		} catch (RemoteException e) {
			e.printStackTrace(System.out);
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
	 * 
	 * @param autoSpawn
	 *            If true, it automatically spawns five processes, otherwise it
	 *            expects to find them in the network.
	 */
	private void testCase2(boolean autoSpawn) throws LockedException,
			DuplicateIDException {
		if (autoSpawn) {
			populateNetwork(5);
		} else {
			if (!locked || processes.size() != 5) {
				System.out
						.println("Test case 2 with autoSpawn turned off expects to find a locked network with five processes.");
				throw new RuntimeException();
			}
		}

		try {
			processes.get(1)
					.sendMessage(Constants.BROADCAST, "First broadcast");
			synchronized (queue) {
				for (int i = queue.size() - 1; i >= 0; i--) {
					if (queue.get(i).recipient != 5) {
						forwardMessage(i);
					}
				}
			}
			processes.get(2).sendMessage(Constants.BROADCAST,
					"Second broadcast");
			synchronized (queue) {
				for (int i = queue.size() - 1; i > 0; i--) {
					forwardMessage(i);
				}
			}
			processes.get(3)
					.sendMessage(Constants.BROADCAST, "Third broadcast");
			synchronized (queue) {
				for (int i = queue.size() - 1; i > 0; i--) {
					forwardMessage(i);
				}
			}
			processes.get(4).sendMessage(Constants.BROADCAST,
					"Fourth broadcast");
			synchronized (queue) {
				for (int i = queue.size() - 1; i > 0; i--) {
					forwardMessage(i);
				}
			}

			forwardMessage(0);
		} catch (RemoteException e) {
			e.printStackTrace(System.out);
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
	private void testCase3() throws LockedException, DuplicateIDException {
		populateNetwork(2);

		try {
			processes.get(1)
					.sendMessage(Constants.BROADCAST, "First broadcast");
			processes.get(1).sendMessage(Constants.BROADCAST,
					"Second broadcast");
			processes.get(1)
					.sendMessage(Constants.BROADCAST, "Third broadcast");
			processes.get(1).sendMessage(Constants.BROADCAST,
					"Fourth broadcast");

			synchronized (queue) {
				for (int i = queue.size() - 1; i >= 0; i--) {
					forwardMessage(i);
				}
			}
		} catch (RemoteException e) {
			e.printStackTrace(System.out);
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
	private void testCase4() throws LockedException, DuplicateIDException {
		populateNetwork(4);

		try {
			processes.get(1)
					.sendMessage(Constants.BROADCAST, "First broadcast");
			processes.get(2).sendMessage(Constants.BROADCAST,
					"Second broadcast");
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
			processes.get(3)
					.sendMessage(Constants.BROADCAST, "Third broadcast");
			processes.get(4).sendMessage(Constants.BROADCAST,
					"Fourth broadcast");

			synchronized (queue) {
				for (int i = queue.size() - 1; i >= 0; i--) {
					forwardMessage(i);
				}
			}
		} catch (RemoteException e) {
			e.printStackTrace(System.out);
		}
	}

	/**
	 * Test case 5: this is a copy of test case 2, but without automatic process
	 * spawning, in order to showcase the ability of our solution to work also
	 * with RMI calls. Five clients must be created on external VMs and network
	 * must be locked before starting the test.
	 */
	private void testCase5() throws LockedException, DuplicateIDException {
		testCase2(false);
	}

	/**
	 * Test case 6: this is a completely random test. It picks five random
	 * processes in an already locked network with arbitrary size, then everyone
	 * sends a broadcast with a fixed delay. In the meantime, the worker thread
	 * is running, forwarding messages in random order.
	 */
	private void testCase6() throws LockedException, DuplicateIDException {
		if (!locked) {
			System.out.println("Test case 6 expects to find a locked network");
			throw new RuntimeException();
		}

		worker.start();

		try {
			for (int i = 1; i <= 5; i++) {
				int randomProcessID = new Random().nextInt(processes.keySet()
						.size() - 1);
				ProcessInterface randomProcess = processes
						.get(randomProcessID + 1);
				randomProcess.sendMessage(Constants.BROADCAST, "Broadcast " + i);
				Thread.sleep(DISPATCH_DELAY * 2);
			}
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		} catch (InterruptedException e) {
		}
	}
}
