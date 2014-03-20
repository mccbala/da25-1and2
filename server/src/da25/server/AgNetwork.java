package da25.server;

import java.rmi.RemoteException;
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
		populateNetwork(200);

		((AgProcess) processes.get(4)).randomize = true;
		((AgProcess) processes.get(4)).startCandidate();

		((AgProcess) processes.get(10)).randomize = true;
		((AgProcess) processes.get(10)).startCandidate();

		performCommand(null, "round");

		((AgProcess) processes.get(100)).randomize = true;
		((AgProcess) processes.get(100)).startCandidate();

		performCommand(null, "auto");
	}
}
