package da25.server;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.Set;

import da25.base.Constants;
import da25.base.Message;
import da25.base.NetworkInterface;
import da25.base.ProcessInterface;
import da25.base.exceptions.DuplicateIDException;
import da25.base.exceptions.LockedException;
import da25.process.Process;

/**
 * Singleton simulating a generic network and keeping track of all running
 * processes.
 * 
 * @author Stefano Tribioli
 * @author Casper Folkers
 * 
 */
public abstract class Network implements NetworkInterface {
	/**
	 * When spawning a new process, any non-positive ID will result in an
	 * auto-increment being performed. This constant can (and indeed should) be
	 * used to clarify this fact.
	 */
	public static final int AUTO_INCREMENT = 0;

	/**
	 * List holding references for all processes.
	 */
	protected HashMap<Integer, ProcessInterface> processes = new HashMap<>();

	/**
	 * Keeps track of the largest ID in the network for auto-increment purposes.
	 */
	protected int largestID = 0;

	/**
	 * List holding the messages waiting to be dispatched.
	 */
	protected ArrayList<Message> queue = new ArrayList<>();

	/**
	 * If true, no new client can register to the network.
	 */
	protected boolean locked = false;

	/**
	 * When spawning processes, they will be object of this class.
	 */
	protected Class<? extends Process> processClass;

	/**
	 * Creates a new network instance.
	 * 
	 * @param processClass
	 *            When spawning processes, they will be object of this class.
	 */
	public Network(Class<? extends Process> processClass) {
		this.processClass = processClass;
	}

	@Override
	public int register(ProcessInterface process) throws RemoteException,
			LockedException {
		if (locked) {
			throw new LockedException();
		}

		synchronized (processes) {
			processes.put(++largestID, process);
			System.out.println("Added new process with id " + largestID);
			return largestID;
		}
	}

	@Override
	public int register(ProcessInterface process, int id)
			throws RemoteException, LockedException, DuplicateIDException {
		if (locked) {
			throw new LockedException();
		}

		synchronized (processes) {
			if (processes.containsKey(id)) {
				throw new DuplicateIDException();
			}

			if (id > largestID) {
				largestID = id;
			}

			processes.put(id, process);
			System.out.println("Added new process with id " + id);
			return id;
		}
	}

	@Override
	public Set<Integer> getIds() throws RemoteException {
		return processes.keySet();
	}

	/**
	 * RMI operations are concluded. A parser thread is started and blocks,
	 * waiting for user input.
	 * <p>
	 * Derived classes must call through to the super class's implementation of
	 * this method.
	 */
	public void start() {
		System.out.println("Network is running, waiting for clients.");

		Thread parser = new Thread(new Runnable() {
			@Override
			public void run() {
				parseCommand();
			}
		});
		parser.start();
	}

	/**
	 * Convenience function to parse lines of user input. The Scanner create
	 * herein must be passed around to functions subsequentely dealing with user
	 * input, in order to avoid the notorius "Clashing Scanners" problem.
	 */
	protected void parseCommand() {
		Scanner scanner = new Scanner(System.in);
		while (true) {
			try {
				String command = scanner.nextLine();
				if (!performCommand(scanner, command)) {
					System.out.println("Unknown command: '" + command + "'");
				}
			} catch (NoSuchElementException e) {
				break;
			}
		}
	}

	/**
	 * Actually perform the actions requested by the user. This function can be
	 * extended by derived classes, but they really should call through to the
	 * current class's implementation if the command is not consumed.
	 * 
	 * @param scanner
	 *            The one Scanner operating on standard input.
	 *            <p>
	 *            Only one Scanner is allowed to insist on a stream, in order to
	 *            avoid weird clashes, so a reference to this single Scanner is
	 *            passed around to perform additional parsing if needed.
	 * @param command
	 *            The last command (i.e., line) typed by the user.
	 * @return Returns true if the command was consumed, false otherwise.
	 */
	protected boolean performCommand(Scanner scanner, String command) {
		switch (command) {
		case "new":
			/*
			 * A "new" command spawns a new process and registers it in the
			 * network. The user is asked for an ID, but any non-positive number
			 * will result in the standard auto-increment being employed.
			 */
			System.out
					.println("Enter new process ID (or 0 for auto-increment):");
			
			int newId;
			try {
				newId = Integer.parseInt(scanner.nextLine());
			} catch (NumberFormatException e) {
				newId = AUTO_INCREMENT;
			}
			
			try {
				spawnProcess(newId);
			} catch (LockedException e) {
				System.out
						.println("Unable to spawn new process: network is locked.");
			} catch (DuplicateIDException e) {
				System.out
						.println("Unable to spawn new process: ID already in use.");
			}
			return true;
		case "populate":
			/*
			 * A "populate" command will quicky spawn a specified number of
			 * processes with incrementing IDs.
			 */
			System.out
					.println("Enter size of the network:");
			int newSize = Integer.parseInt(scanner.nextLine());
			
			try {
				populateNetwork(newSize);
			} catch (LockedException e) {
				System.out
						.println("Unable to spawn new process: network is locked.");
			} catch (DuplicateIDException e) {
				System.out
						.println("Unable to spawn new process: ID already in use.");
			}
			return true;
		case "lock":
			lock();
			return true;
		case "exit":
			/*
			 * An "exit" command will terminate all VMs (all the clients and
			 * then the server). This is only a useful shortcut for debugging
			 * purposes and should not be relied upon, since various conditions
			 * may result in lingering clients VMs.
			 */
			for (ProcessInterface process : processes.values()) {
				try {
					process.exit();
				} catch (RemoteException e) {
					/*
					 * The call will always throw a SocketException since the
					 * client terminates before sending return value, but this
					 * is fine for us.
					 */
				}
			}
			System.exit(0);
			return true;
		default:
			return false;
		}
	}

	/**
	 * Spawn a new process locally (i.e., in the same VM of the server).
	 * 
	 * @param id
	 *            The ID of the new process. If non-positive, the ID will be
	 *            generated by auto-increment.
	 * @throws LockedException
	 *             The network is already locked, no new process can register.
	 * @throws DuplicateIDException
	 *             The passed ID is already in use by another process.
	 */
	public void spawnProcess(int id) throws LockedException,
			DuplicateIDException {
		Process process;
		try {
			process = processClass.newInstance();
			process.network = this;
			try {
				if (id > 0) {
					process.id = register(process, id);
				} else {
					process.id = register(process);
				}
			} catch (RemoteException re) {
				/*
				 * This exception is never thrown, since we are creating the
				 * object locally.
				 */
			}
		} catch (InstantiationException | IllegalAccessException e) {
			e.printStackTrace(System.out);
		}
	}

	/**
	 * Lock the network, preventing other processes to register.
	 */
	public void lock() {
		if (!locked) {
			locked = true;

			for (ProcessInterface process : processes.values()) {
				try {
					process.start();
				} catch (RemoteException e) {
					throw new RuntimeException(e);
				}
			}
		}
	}

	@Override
	public void sendMessage(Message message) throws RemoteException {
		synchronized (processes) {
			switch (message.recipient) {
			case Constants.BROADCAST:
				for (Entry<Integer, ProcessInterface> pair : processes
						.entrySet()) {
					if (!pair.getKey().equals(message.sender)) {
						Message messageCopy = new Message(message.sender,
								pair.getKey(), message.clock, message.body);
						queue.add(messageCopy);
						System.out.println(messageCopy.toString(processes
								.size()) + " put in queue.");
					}
				}
				break;
			case Constants.NETWORK:
				processControlMessage(message);
				break;
			default:
				synchronized (queue) {
					queue.add(message);
					System.out.println(message + " put in queue.");
				}
				break;
			}
		}
	}

	/**
	 * Process a message with Message.NETWORK recipient.
	 * <p>
	 * This implementation does nothing, but derived classes may override this
	 * function. For maximum clarity and forward compatibility, they should call
	 * back to the superclass method if the message is not understood.
	 * 
	 * @param message
	 *            The message to be processed.
	 */
	protected void processControlMessage(Message message) {
		System.out.println("Control message " + message + " discarded");
	}

	/**
	 * Dispatches a message from the queue to the recipient.
	 */
	protected void forwardMessage(int index) {
		Message message;

		synchronized (queue) {
			message = queue.remove(index);
		}

		try {
			if (this instanceof AsyncNetwork) {
				/*
				 * The forwarding operation is interesting in an async
				 * environment, much less in a sync one, since there it happends
				 * predictably at the end of the round.
				 */
				System.out.println("Forwarding "
						+ message.toString(processes.size()));
			}
			processes.get(message.recipient).recieveMessage(message);
		} catch (RemoteException e) {
			System.out.println("Unable to send message " + message
					+ " RemoteException");
		} catch (NullPointerException e) {
			System.out.println("Unable to send message " + message
					+ " Missing recipient");
		}
	}

	protected void forwardSingleSequentially() {
		forwardMessage(0);
	}

	protected void forwardAllSequentially() {
		synchronized (queue) {
			while (!queue.isEmpty()) {
				forwardMessage(0);
			}
		}
	}

	/**
	 * Convenience function to quickly populate a network. Processes will have
	 * auto-incremented IDs, starting from 1.
	 * 
	 * @param size
	 *            The number of processes.
	 * @return True in case of success, false otherwise.
	 */
	protected void populateNetwork(int size) throws LockedException,
			DuplicateIDException {
		if (!processes.isEmpty()) {
			throw new LockedException();
		}

		for (int i = 0; i < size; i++) {
			spawnProcess(AUTO_INCREMENT);
		}
		
		lock();
	}
}
