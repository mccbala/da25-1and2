package da25.base;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interface defining an asynchronous network.
 * 
 * @author Stefano Tribioli
 * @author Casper Folkers
 * 
 */
public interface NetworkInterface extends Remote {
	/**
	 * Called by a process (passing a reference of itself) when entering the
	 * network. The network will store the reference for future message
	 * delivery.
	 * 
	 * @param process
	 *            A reference to a remote Process object.
	 * @return The ID assigned to the new process.
	 * @throws RemoteException
	 */
	public int register(ProcessInterface process) throws RemoteException;

	/**
	 * Get a count of registered processes.
	 * 
	 * @throws RemoteException
	 */
	public int getCount() throws RemoteException;

	/**
	 * Send a unicast message.
	 * 
	 * @param message
	 *            The message to be sent.
	 * @param The
	 *            ID of recipient process.
	 * @throws RemoteException
	 */
	public void sendMessage(Message message, int recipient)
			throws RemoteException;

	/**
	 * Send a broadcast message.
	 * 
	 * @param message
	 *            The message to be sent.
	 * @throws RemoteException
	 */
	public void sendMessage(Message message) throws RemoteException;
	
	/**
	 * Send an acknowledgment
	 * 
	 * @throws RemoteException
	 */
	public void sendAck() throws RemoteException;
}
