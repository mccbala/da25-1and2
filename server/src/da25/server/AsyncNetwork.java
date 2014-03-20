package da25.server;

import java.util.Random;
import java.util.Scanner;

import da25.process.Process;

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
	public static final long DISPATCH_DELAY = 100;

	/**
	 * A worker thread who regularly checks the message queue.
	 */
	protected Thread worker;

	public AsyncNetwork(Class<? extends Process> processClass) {
		super(processClass);

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
}
