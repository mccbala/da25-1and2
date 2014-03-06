package da25.process;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Scanner;

import da25.base.Message;
import da25.base.NetworkInterface;
import da25.base.ProcessInterface;
import da25.base.VectorClock;

/**
 * Concrete implementation of a Process
 * 
 * @author Stefano Tribioli
 * @author Casper Folkers
 * 
 */
public class Process implements ProcessInterface {
	public int id;
	public VectorClock clock;
	public NetworkInterface network;
	private ArrayList<Message> buffer;
	private boolean started = false;

	public Process() {
		buffer = new ArrayList<Message>();
	}

	@Override
	public void start() {
		if (started) {
			return;
		} else {
			started = true;
		}

		try {
			clock = new VectorClock(network.getCount());
		} catch (RemoteException e) {
			System.out.println("Unable to get client count.");
			throw new RuntimeException(e);
		}

		@SuppressWarnings("unused")
		Thread parser = new Thread(new Runnable() {
			@Override
			public void run() {
				parseIn();
			}
		});
		//parser.start();
	}

	@Override
	public void exit() throws RemoteException {
		System.exit(0);
	}

	/**
	 * Blocks waiting for user input.
	 */
	private void parseIn() {
		Scanner scanner = new Scanner(System.in);
		while (true) {
			System.out.println("Enter message or type 'exit':");
			String messageBody = scanner.nextLine();
			if (messageBody.equals("exit")) {
				scanner.close();
				return;
			}

			System.out.println("Enter recipient id:");
			int recipient = scanner.nextInt();
			scanner.nextLine();

			Message message;
			synchronized (clock) {
				message = new Message(id, recipient, clock, messageBody);
			}
			sendMessage(message);
		}
	}

	@Override
	public void recieveMessage(Message message) throws RemoteException {
		synchronized (clock) {
			clock.increase(message.sender);
			if (clock.greaterEqual(message.clock)) {
				dispatchMessage(message);

				boolean newUpdate = true;
				while (newUpdate) {
					newUpdate = false;
					for (int i = 0; i < buffer.size(); i++) {
						Message nextMessage = buffer.get(i);
						clock.increase(nextMessage.sender);
						if (clock.greaterEqual(nextMessage.clock)) {
							newUpdate = true;
							dispatchMessage(nextMessage);
							buffer.remove(i);
							i--;
						} else {
							clock.decrease(nextMessage.sender);
						}
					}
				}
			} else {
				System.out.println(message+" put in buffer");
				clock.decrease(message.sender);
				buffer.add(message);
			}
		}
	}

	/**
	 * A message is dispatched from the local buffer for actual elaboration.
	 * 
	 * @param message
	 */
	private void dispatchMessage(Message message) {
		System.out.println("Dispatched message: " + message);
	}

	@Override
	public void sendMessage(int recipient, String body) {
		synchronized (clock) {
			sendMessage(new Message(id, recipient, clock, body));
		}
	};
	
	/**
	 * Sends a new broadcast message.
	 */
	private void sendMessage(Message message) {
		synchronized (clock) {
			try {
				clock.increase(id);
				network.sendMessage(message);
			} catch (RemoteException e) {
				clock.decrease(id);
				System.out.println("Unable to send message " + message
						+ ", because of: " + e.getMessage());
			}
		}
	}
}
