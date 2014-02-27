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
			if(candidate){
				performCandidateRound(newRound);
			}
			performOrdinaryRound(newRound);
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


	private void performCandidateRound(int round) {
		// TODO Auto-generated method stub
		
	}

	private void performOrdinaryRound(int round) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void recieveMessage(int level, int id) throws RemoteException {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void recieveAck() throws RemoteException {
		// TODO Auto-generated method stub
		
	}
}
