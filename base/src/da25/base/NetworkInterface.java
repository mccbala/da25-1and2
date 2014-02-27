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
	 * Send a broadcast message.
	 * 
	 * @param message
	 *            The message to be sent.
	 * @throws RemoteException
	 */
	public void sendMessage(int level, int id, int recipient) throws RemoteException;
	
	/**
	 * Send an acknowledgment
	 * 
	 * @param id
	 *            The id of the reciever
	 * @throws RemoteException
	 */
	public void sendAck(int id) throws RemoteException;
	
	/**
	 * informs server that client with given id is done with this round
	 * 
	 * @param id
	 *            The id of the sender
	 * @throws RemoteException
	 */
	public void done(int id) throws RemoteException;
}
