package da25.server;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Random;

import da25.base.Message;
import da25.base.NetworkInterface;
import da25.base.ProcessInterface;

/**
 * Singleton simulating an asynchronous message delivery and keeping track of
 * all running processes.
 * 
 * @author Stefano Tribioli
 * @author Casper Folkers
 * 
 */
public class Network implements NetworkInterface {
	/**
	 * A fixed amount of time to wait before dispatching the next message.
	 */
	public static final long DISPATCH_DELAY = 1000;

	/**
	 * List holding references for all processes.
	 */
	protected ArrayList<ProcessInterface> processes = new ArrayList<>();
	/**
	 * List holding the messages waiting to be dispatched.
	 */
	protected ArrayList<Message> queue = new ArrayList<>();
	/**
	 * A worker thread who regularly checks the message queue.
	 */
	private Thread worker;

	private int[] busyIDs;
	private int[] busyAckIDs;
	
	public Network() {
		
	}
	
	@Override
	public int register(ProcessInterface process) throws RemoteException {
		synchronized (processes) {
			processes.add(process);
			return processes.size() - 1;
		}
	}

	@Override
	public int getCount() throws RemoteException {
		return processes.size();
	}

	@Override
	public void sendMessage(int level, int id, int recipient)
			throws RemoteException {
		processes.get(recipient).recieveMessage(level, id);
	}

	@Override
	public void sendAck(int id) throws RemoteException {
		processes.get(id).recieveAck();
	}

	@Override
	public void ackDone(int id) throws RemoteException {
		System.out.println("client " + id + "reports done");
		synchronized (busyAckIDs) {
			if(busyAckIDs[id] > 0){
				throw new RemoteException("Client already send ack done signal");
			}
			busyAckIDs[id] = 1;
		}
	}
	
	@Override
	public void done(int id) throws RemoteException {
		System.out.println("client " + id + "reports done");
		synchronized (busyIDs) {
			if(busyIDs[id] > 0){
				throw new RemoteException("Client already send done signal");
			}
			busyIDs[id] = 1;
		}
		
	}
	
	public void start() {
		int round = 1;
		int[] clientIDs = new int[processes.size()];
		for(int i = 0; i < processes.size(); i++){
			try {
				clientIDs [i] = processes.get(i).getID();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		for(int i = 0; i < processes.size(); i++){
			try {
				processes.get(i).UpdateClientList(clientIDs);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		
		while(round < 10){
			System.out.println("starting round " + round);
			busyIDs = new int[processes.size()];
			busyAckIDs = new int[processes.size()];
			if(round == 3){
				try {
					processes.get(0).startElection();
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			communicateNewRound(round);
			boolean stillAcking = true;
			while(stillAcking){
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				stillAcking = false;
				synchronized (busyAckIDs) {
					for(int i = 0; i < busyAckIDs.length; i++){
						if(busyAckIDs[i] == 0){
							stillAcking = true;
						}
					}
				}
			}
			communicateDoneAck();
			boolean stillBusy = true;
			while(stillBusy){
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				stillBusy = false;
				synchronized (busyIDs) {
					for(int i = 0; i < busyIDs.length; i++){
						if(busyIDs[i] == 0){
							stillBusy = true;
						}
					}
				}
			}		
			round++;
		}
		
	}
	private void communicateDoneAck() {
		for(int i = 0; i < processes.size(); i++){
			try {
				processes.get(i).doneAck();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}

	private void communicateNewRound(int round){
		for(int i = 0; i < processes.size(); i++){
			try {
				processes.get(i).nextRound(round);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}
}
