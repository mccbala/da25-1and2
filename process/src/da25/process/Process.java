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
	private int processCount;
	
	public Process(){
		buffer = new ArrayList<Message>();
		clock = new VectorClock(10);
	}
	
	/**
	 * RMI operations are concluded, starts actual process commands.
	 */
	@Override
	public void start() {
		Scanner scanner = new Scanner(System.in);
		while(true){
			
			System.out.println("enter message");
			String messageBody = scanner.nextLine();
			if(messageBody.equals("exit")){
				continue;
			}
			System.out.println("enter recipient client");
			int recipient = scanner.nextInt();
			scanner.nextLine();
			
			Message message = new Message(id, recipient, clock, messageBody);
			
			try {
				clock.increase(id);
				network.sendMessage(message);
			} catch (RemoteException e) {
				clock.decrease(id);
				System.out
				.println("Unable to send message [" + message.toString()
						+ "], because of: " + e.getMessage());
			}
		}
		
	}

	@Override
	public void newProcess(int id) throws RemoteException {
		processCount++;
		clock.resize(processCount);	
	}
	
	@Override
	public void recieveMessage(Message message) throws RemoteException {
		clock.increase(message.sender);
		if(clock.GreaterEqual(message.clock)){
			dispatchMessage(message);
			boolean newUpdate = true;
			while(newUpdate){
				newUpdate = false;
				for(int i = 0; i < buffer.size(); i++){
					Message nextMessage = buffer.get(i);
					clock.increase(nextMessage.sender);
					if(clock.GreaterEqual(nextMessage.clock)){
						newUpdate = true;
						dispatchMessage(nextMessage);
						buffer.remove(i);
						i--;
					}
					else{
						clock.decrease(nextMessage.sender);
					}
				}
			}
		}
		else{
			clock.decrease(message.sender);
			System.out.println("Missing messages, message put in buffer");
			buffer.add(message);	
		}
	}
	
	private void dispatchMessage(Message message){
		System.out.println("Incoming message: [" + message + "]");
	}
}
