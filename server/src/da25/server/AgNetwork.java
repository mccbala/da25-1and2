package da25.server;

import java.util.Scanner;

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
		switch (command) {
		case "test1":
			testCase1();
			return true;
		default:
			return super.performCommand(scanner, command);
		}
	}
	
	/**
	 * Test case 1: TODO
	 */
	private void testCase1() {
		
	}
}
