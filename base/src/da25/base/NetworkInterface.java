package da25.base;

import java.rmi.Remote;
import java.rmi.RemoteException;

import da25.base.exceptions.DuplicateIDException;
import da25.base.exceptions.LockedException;

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
	 * @throws LockedException
	 *             The network is already locked, no new process can register.
	 */
	public int register(ProcessInterface process) throws RemoteException,
			LockedException;

	/**
	 * Called by a process (passing a reference of itself) when entering the
	 * network. The network will store the reference for future message
	 * delivery.
	 * <p>
	 * This signature allows for a manually specified ID, as per Assignment 2
	 * requirements.
	 * 
	 * @param process
	 *            A reference to a remote Process object.
	 * @param id
	 *            The ID to be assigned to the new process.
	 * @return The passed ID.
	 * @throws RemoteException
	 * @throws LockedException
	 *             The network is already locked, no new process can register.
	 * @throws DuplicateIDException
	 *             The passed ID is already in use by another process.
	 */
	public int register(ProcessInterface process, int id)
			throws RemoteException, LockedException, DuplicateIDException;

	/**
	 * Get a count of registered processes.
	 * 
	 * @throws RemoteException
	 */
	public int getCount() throws RemoteException;

	/**
	 * This function is called by processes willing to send a message. The
	 * message itself can be unicast or broadcast, depending on the value of its
	 * recipient field.
	 * 
	 * @param message
	 *            The message to be sent.
	 * @throws RemoteException
	 */
	public void sendMessage(Message message) throws RemoteException;
}
