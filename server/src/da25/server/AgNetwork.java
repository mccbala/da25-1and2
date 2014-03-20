package da25.server;

import java.rmi.RemoteException;
import java.util.Random;
import java.util.Scanner;

import da25.base.exceptions.DuplicateIDException;
import da25.base.exceptions.LockedException;
import da25.process.AgProcess;

/**
 * The actual network for assignment 2, complete with a test suite.
 * 
 * @author Stefano Tribioli
 * @author Casper Folkers
 * 
 */
public class AgNetwork extends SyncNetwork {
	public AgNetwork() {
		super(AgProcess.class);
	}

	@Override
	protected boolean performCommand(Scanner scanner, String command) {
		try {
			switch (command) {
			case "start":
				System.out.println("Enter ID of process:");
				int candidateId = Integer.parseInt(scanner.nextLine());

				lock();

				try {
					((AgProcess) processes.get(candidateId)).startCandidate();
				} catch (NullPointerException e) {
					System.out
							.println("Entered ID is not present in the network.");
				}
				return true;
			case "test1":
				testCase1();
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
	 * Test case 1.
	 */
	private void testCase1() throws LockedException, DuplicateIDException,
			RemoteException {
		int clientsCount = 200;
		populateNetwork(clientsCount);

		((AgProcess) processes.get(new Random().nextInt(clientsCount) + 1))
				.startCandidate();

		((AgProcess) processes.get(new Random().nextInt(clientsCount) + 1))
				.startCandidate();

		((AgProcess) processes.get(new Random().nextInt(clientsCount) + 1))
				.startCandidate();

		performCommand(null, "round");

		((AgProcess) processes.get(new Random().nextInt(clientsCount) + 1))
				.startCandidate();

		((AgProcess) processes.get(new Random().nextInt(clientsCount) + 1))
				.startCandidate();

		performCommand(null, "auto");
	}
}
