package da25.base;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interface defining a single process, running on a separate JVM and passing messages
 * using RMI.
 * 
 * @author Stefano Tribioli
 * @author Casper Folkers
 *
 */
public interface ProcessInterface extends Remote {
	public void processMessage(Message message) throws RemoteException;
}
