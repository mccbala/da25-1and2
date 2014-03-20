package da25.process;

import java.rmi.RemoteException;

import da25.base.Message;
import da25.base.NetworkInterface;
import da25.base.ProcessInterface;
import da25.base.VectorClock;

/**
 * Common prototype for a concrete Process, it has to be subclassed for each
 * assignment.
 * <p>
 * Derived classes must have a nullary constructor.
 * 
 * @author Stefano Tribioli
 * @author Casper Folkers
 * 
 */
public abstract class Process implements ProcessInterface {
	public int id;
	public VectorClock clock = new VectorClock();
	public NetworkInterface network;

	@Override
	public void start() {
		return;
	}

	@Override
	public void exit() throws RemoteException {
		System.exit(0);
	}

	/**
	 * A message is delivered from the local buffer for actual elaboration.
	 * Since we are only showcasing control algorithms, the process simply
	 * prints the event to the standard output.
	 * 
	 */
	protected void deliverMessage(Message message) {
		System.out.println("Delivered message: " + message);
	}
}
