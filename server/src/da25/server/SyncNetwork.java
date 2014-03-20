package da25.server;

import java.rmi.RemoteException;
import java.util.Map.Entry;
import java.util.Scanner;

import da25.base.Constants;
import da25.base.Message;
import da25.base.ProcessInterface;
import da25.base.VectorClock;
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
	 * Keeps track of the number of processes who have already signalled to be
	 * ready.
	 */
	private int readyCount = 0;

	/**
	 * If positive, the network will proceed to the next round as soon as all
	 * processes have signalled to be ready (autoMode on), if negative it will
	 * wait for a user's "round" command (autoMode off), if zero it will be in
	 * autoMode for the current round, but then it will switch back to autoMode
	 * off.
	 */
	private int autoMode = -1;

	public SyncNetwork(Class<? extends Process> processClass) {
		super(processClass);
	}

	@Override
	synchronized protected boolean performCommand(Scanner scanner,
			String command) {
		switch (command) {
		case "round":
			if (readyCount == processes.size()) {
				nextRound();
			} else {
				autoMode = 0;
			}
			return true;
		case "auto":
			autoMode = +1;
			if (readyCount == processes.size()) {
				nextRound();
			}
			return true;
		default:
			return super.performCommand(scanner, command);
		}
	}

	@Override
	public void lock() {
		super.lock();
		readyCount = processes.size();
	}

	@Override
	synchronized protected void processControlMessage(Message message) {
		switch (message.body) {
		case Constants.READY_ROUND:
			readyCount++;
			if (autoMode >= 0 && readyCount == processes.size()) {
				if (autoMode == 0) {
					autoMode = -1;
				}

				nextRound();
			}
			return;
		default:
			super.processControlMessage(message);
			return;
		}
	}

	synchronized protected void nextRound() {
		readyCount = 0;
		System.out.println("Starting a new round.");
		synchronized (queue) {
			forwardAllSequentially();
			for (Entry<Integer, ProcessInterface> pair : processes.entrySet()) {
				try {
					pair.getValue().recieveMessage(
							new Message(Constants.NETWORK, pair.getKey(),
									new VectorClock(), Constants.PULSE_ROUND));
				} catch (RemoteException e) {
					throw new RuntimeException(e);
				}
			}
		}
	}
}
