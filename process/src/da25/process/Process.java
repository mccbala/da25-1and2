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
	public NetworkInterface network;
	private int processCount;
	private boolean candidate;
	private int newRound = 0;
	private int[] clients;
	
	//candidate variables
	private int[] clientsLeft;
	private int[] nextTargetClients;
	private int Candidatelevel = -1;
	private boolean elected = false;
	private int AckCount = 0;
	private int targetCount = -1;
	private boolean ackDone = false;
	
	//ordinary variables
	private int Ordinarylevel = -1;
	private int link = -1;
	private int ordinaryID = -1;
	
	
	public void start(){
		int finishedRound = 0;
		while(true){
			while(newRound <= finishedRound){
				try {
					Thread.sleep(20);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			performOrdinaryRound(newRound);
			if(candidate){
				performCandidateRound(newRound);
			}
			finishedRound++;
			try {
				network.done(id);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}
	

	@Override
	public void nextRound(int round) {
		newRound = round;
	}

	@Override
	public void doneAck() {
		ackDone = true;
	}


	private void performCandidateRound(int round) {
		if(Candidatelevel == -1){
			clientsLeft = clients;
		}
		Candidatelevel++;
		if(Candidatelevel%2 == 0){
			if(clientsLeft.length == 0){
				elected = true;
				System.out.println("elected");
			}
			else{
				targetCount = (int) Math.min(Math.round(Math.pow(2, Candidatelevel/2)), clientsLeft.length);
				nextTargetClients = new int[targetCount];
				for(int i =0; i< targetCount;i++){
					try {
						network.sendMessage(Candidatelevel, id, clientsLeft[i]);
					} catch (RemoteException e) {
						e.printStackTrace();
					}
				}
				int[] newClientsLeft = new int[clientsLeft.length - targetCount];
				for(int i = 0; i < (clientsLeft.length - targetCount); i++){
					newClientsLeft[i] = clientsLeft[i + targetCount];
				}
				clientsLeft = newClientsLeft;
			}
		}
		else{
			while(!ackDone){
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			ackDone = false;
			if(AckCount < targetCount){
				System.out.println("rejected");
				candidate = false;
				nextTargetClients = null;
				clientsLeft = null;
				Candidatelevel = -1;
				AckCount = 0;
				targetCount = -1;
			}
			else{
				AckCount = -1;
			}
		}		
	}

	private void performOrdinaryRound(int round) {
		if(Ordinarylevel >= 0){
			if(link >= 0){
				try {
					System.out.println("process " + link + " acknowledged");
					network.sendAck(link);
					
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				link = -1;
				
			}
			Ordinarylevel++;
		}
		try {
			network.ackDone(id);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void UpdateClientList(int[] clients) {
		this.clients = clients;
	}
	
	@Override
	public int getID() {
		return id;
	}
	
	@Override
	public void recieveMessage(int level, int id) throws RemoteException {
		synchronized (this) {
			System.out.println("recieved from " + id);
			if(level > Ordinarylevel || (level == Ordinarylevel && id > ordinaryID) ){
				System.out.println("should acknowledge");
				Ordinarylevel = level;
				ordinaryID = id;
				link = id;
			}
		}
	}

	@Override
	public void startElection() throws RemoteException {
		System.out.println("started as candidate");
		candidate = true;		
	}

	@Override
	public void recieveAck() throws RemoteException {
		System.out.println("acknowledged");
		AckCount++;
		
	}
}
