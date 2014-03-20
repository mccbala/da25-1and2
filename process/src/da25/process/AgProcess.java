package da25.process;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Random;

import da25.base.Constants;
import da25.base.Message;

public class AgProcess extends Process {
	private final static String CANDIDATE = "MSG_CAN";
	private final static String ACK = "MSG_ACK";

	public boolean randomize = true;

	private boolean isCandidate = false;
	private int candidateLevel = -1;
	private ArrayList<Integer> links;
	private int candidatesTarget = 0;
	private int candidatesCount = 0;

	private int ordinaryLevel = -1;
	private int ordinaryId = -1;
	private int winningLink = -1;
	private ArrayList<Message> candidates = new ArrayList<>();

	private boolean isElected = false;

	@Override
	synchronized public void recieveMessage(Message message)
			throws RemoteException {
		clock.increase(id);

		switch (message.sender) {
		case Constants.NETWORK:
			if (message.body == Constants.PULSE_ROUND) {
				pulse();
			}
			break;
		default:
			switch (message.body.substring(0, 7)) {
			case CANDIDATE:
				candidates.add(message);
				break;
			case ACK:
				candidatesCount++;
				break;
			default:
				System.out.println(message + " discarded");
				break;
			}
			break;
		}
	}

	/**
	 * The network signalled that all message for this round were delivered.
	 * <p>
	 * The candidate process checks the number of ACKs received, the ordinary
	 * process sorts the candidate messages received and answers if necessary.
	 */
	private void pulse() {
		if (isCandidate) {
			performCandidateRound();
		}

		performOrdinaryRound();

		finalizeRound();
	}

	private void performCandidateRound() {
		candidateLevel++;

		if (candidateLevel % 2 == 0) {
			if (candidatesCount < candidatesTarget) {
				System.out.println("Process " + id + " no longer a candidate.");
				isCandidate = false;
			} else {

				if (links.isEmpty()) {
					isElected = true;
					System.out.println("Elected process " + id + "!");
				} else {
					candidatesTarget = (int) Math.min(
							Math.pow(2D, (double) candidateLevel / 2D),
							links.size());

					if (randomize) {
						Collections.shuffle(links, new Random(id));
					}

					Iterator<Integer> iter = links.iterator();
					for (int i = 0; i < candidatesTarget; i++) {
						int recipient = iter.next();
						try {
							network.sendMessage(new Message(id, recipient,
									null, CANDIDATE + "|" + candidateLevel
											+ "|" + id));
							iter.remove();
						} catch (RemoteException e) {
							throw new RuntimeException(e);
						}
					}
				}
			}
		}
	}

	private void performOrdinaryRound() {
		if (ordinaryId == -1) {
			ordinaryId = id;
		}

		if (!candidates.isEmpty()) {
			int largestLevel = -1;
			int largestId = -1;

			for (Message message : candidates) {
				message.body = message.body.substring(8);
				int otherLevel = Integer.parseInt(message.body.substring(0,
						message.body.indexOf('|')));
				int otherId = Integer.parseInt(message.body
						.substring(message.body.indexOf('|') + 1));
				if (pairIsLarger(largestLevel, largestId, otherLevel, otherId)) {
					largestLevel = otherLevel;
					largestId = otherId;
				}
			}

			if (pairIsLarger(ordinaryLevel, ordinaryId, largestLevel, largestId)) {
				ordinaryLevel = largestLevel;
				ordinaryId = largestId;
				winningLink = largestId;
			} else {
				winningLink = -1;
			}

			if (winningLink > -1) {
				try {
					network.sendMessage(new Message(id, winningLink, null, ACK));
				} catch (RemoteException e) {
					throw new RuntimeException(e);
				}
			}

			ordinaryLevel++;
		}
	}

	private void finalizeRound() {
		candidatesCount = 0;
		candidates.clear();

		try {
			if (!isElected) {
				network.sendMessage(new Message(id, Constants.NETWORK, null,
						Constants.READY_ROUND));
			}
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void sendMessage(int recipient, String body) throws RemoteException {
		synchronized (clock) {
			clock.increase(id);
			Message message = new Message(id, recipient, null, body);
			try {
				network.sendMessage(message);
			} catch (RemoteException e) {
				System.out.println("Unable to send message " + message
						+ ", because of: " + e.getMessage());
			}
		}
	}

	public void startCandidate() {
		if (isCandidate) {
			System.out.println("Process is already a candidate.");
			return;
		}
		
		if (candidateLevel > -1) {
			System.out.println("Process already tried to be a candidate.");
			return;
		}
		
		try {
			links = new ArrayList<>(network.getIds());
			links.remove(Integer.valueOf(id));
			isCandidate = true;
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	public static boolean pairIsLarger(int referenceLevel, int referenceId,
			int otherLevel, int otherId) {
		return otherLevel > referenceLevel
				|| (otherLevel == referenceLevel && otherId > referenceId);
	}
}
