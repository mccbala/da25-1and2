package da25.server;

import java.util.Scanner;

import da25.base.Message;
import da25.process.Process;

/**
 * An instance of a network performing synchronous message delivery.
 * 
 * @author Stefano Tribioli
 * @author Casper Folkers
 * 
 */
public class SyncNetwork extends Network {
	/**
	 * Message body to signal readiness.
	 * <p>
	 * When a process has sent all its messages for a round and is ready to
	 * receive the ones pertaining to the next one, it sends a control message
	 * with recipient Message.NETWORK and body READ_ROUND.
	 */
	public static final String READY_ROUND = "READY_ROUND";

	/**
	 * Keeps track of the number of processes who have already signalled to be
	 * ready.
	 */
	private int readyCount = 0;

	/**
	 * If true, the network will proceed to the next round as soon as all
	 * processes have signalled to be ready, otherwise it will wait for a user's
	 * "round" command.
	 */
	private boolean autoMode = false;

	public SyncNetwork(Class<? extends Process> processClass) {
		super(processClass);
	}

	@Override
	protected boolean performCommand(Scanner scanner, String command) {
		switch (command) {
		case "round":
			if (readyCount == processes.size()) {
				nextRound();
			} else {
				System.out.println("Waiting for all the processes to be ready.");
			}
			return true;
		case "auto":
			autoMode = true;
			if (readyCount == processes.size()) {
				nextRound();
			}
			return true;
		default:
			return super.performCommand(scanner, command);
		}
	}

	@Override
	protected void processControlMessage(Message message) {
		switch (message.body) {
		case READY_ROUND:
			readyCount++;
			System.out.println("Process "+message.sender+" is ready, readyCount is "+readyCount);
			if (autoMode && readyCount == processes.size()) {
				nextRound();
			}
			return;
		default:
			super.processControlMessage(message);
			return;
		}
	}

	private void nextRound() {
		System.out.println("Starting a new round.");
		forwardAllSequentially();
	}
}
