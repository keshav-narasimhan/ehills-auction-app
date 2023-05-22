/*
* EE422C Final Project submission by
* Replace <...> with your actual data.
* Keshav Narasimhan
* kn9558
* 17805
* Fall 2021
*/

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Observable;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

public class Server extends Observable {
	
	/*
	 * instance variables
	 */
	private static File auctionFile;
	private static Scanner scanAuctionFile;
	private static File nonvolatileHistory;
	private static FileWriter writeHistory;
	private static HashMap<String, AuctionItems> items = new HashMap<>();
	private ArrayList<String> logOfAuction = new ArrayList<>();
	private HashMap<String, UserDetails> listOfUsers = new HashMap<>();
	private int clientNo = 1;
	private int guestNo = 1;
	private Timer timer = new Timer();
	private TimerTask task;
	
	public static void main(String[] args) {
		// files for history + nonvolatile history
		auctionFile = new File(/*"C:/Users/knkes/git/ee-422c-f21-final-project-keshav-narasimhan/ServerSide/items.txt"*/"items.txt");
		nonvolatileHistory = new File(/*"C:/Users/knkes/git/ee-422c-f21-final-project-keshav-narasimhan/ServerSide/AuctionHistory.txt"*/"AuctionHistory.txt");
		try {
			scanAuctionFile = new Scanner(auctionFile);
			while (scanAuctionFile.hasNextLine()) {
				AuctionItems ai = new AuctionItems(scanAuctionFile.nextLine());
				items.put(ai.getItemName(), ai);
			}
		} catch (FileNotFoundException e) {
			System.out.println("Error with file");
		} catch (IOException e) {
			System.out.println("Error with non-volatile history file");
		}
		
		// start the server
		new Server().runServer();
	}

	private void runServer() {
		try {
			// write to non volatile history file
			writeHistory = new FileWriter(/*"C:/Users/knkes/git/ee-422c-f21-final-project-keshav-narasimhan/ServerSide/AuctionHistory.txt"*/"AuctionHistory.txt"/*nonvolatileHistory*/, true);
			String header = "\n\n****************************************\nAuction started at " + Instant.now() + "\n****************************************\n";
			writeHistory.write(header);
			writeHistory.flush();
			
			// initialize count down clock task
			task = new TimerTask() {
				@Override
				public void run() {
					String message = "Clock;";
					for (String s : items.keySet()) {
						items.get(s).decrementCounter();
						message += s + ";";
					}
					setChanged();
					notifyObservers(message);
				}
			};
			
			// listen for client sockets
			setUpNetworking();
		} catch (Exception e) {
		    e.printStackTrace();
		    return;
		}
	}

	private void setUpNetworking() throws IOException {
		// listen for clients + start the count-down clocks
		ServerSocket serverSock = new ServerSocket(1717);
		timer.scheduleAtFixedRate(task, 1000, 1000);
	    while (true) {
	    	// kick off a client-handler thread for each client for data communication
	    	Socket clientSocket = serverSock.accept();
	    	ClientHandler handler = new ClientHandler(this, clientSocket, items, clientNo);
	    	clientNo++;
	    	this.addObserver(handler);
	    	Thread t = new Thread(handler);
	    	t.start();
	    }
		
	}
	
	// add bid/user action to history + nonvolatile history
	public synchronized void addHistory(String message) {
		try {
			this.writeHistory.write(message + "\n");
			this.writeHistory.flush();
		} catch (IOException e) {
			System.out.println("Errorn writing to file");
		}
		this.logOfAuction.add(message);
	}
	
	// check to see if user exists or not
	public synchronized int checkUser(int clientNo, String username, String pswd) {
		if (!listOfUsers.containsKey(username)) {
			UserDetails details = new UserDetails(clientNo, username, pswd);
			listOfUsers.put(username, details);
			return clientNo;
		} else {
			UserDetails details = listOfUsers.get(username);
			if (details.getPassword().equals(pswd)) {
				return details.getClientNo();
			} else {
				return -1;
			}
		}
	}
	
	// process a bid from a client
	public synchronized void processBid(String item, double bid, int clientNo, String user) {
		// initialize variables to check if bid is valid, what item is being bidded on, etc.
		AuctionItems currItem = items.get(item);
		boolean isValidBid = currItem.isValidBid(bid);
		String notifyResults;
		
		// if the bid is valid proceed, otherwise send an "Error!" message in which case each Observer won't do anything
		if(isValidBid) {
			currItem.placeBid(bid);
			notifyResults = item + ";"/*Customer " + clientNo*/ + user + "'s bid of $" + bid + ";is the current highest bid" + ";" + bid;
			items.get(item).updateBid(user + "'s bid of $" + bid + ";is the current highest bid");
			boolean isBought = currItem.isBought();
			if (isBought) {
				notifyResults += ";true";
				addHistory(/*"Customer " + clientNo*/user + " has bought " + item + " with a bid of $" + bid);
				items.get(item).setCounter();
			} else {
				notifyResults += ";false";
				addHistory(/*"Customer " + clientNo*/user + " bid for " + item + " with a bid of $" + bid);
			}
			notifyResults += ";" + user;//clientNo;
			this.setChanged();
			this.notifyObservers(notifyResults);
		} else {
			notifyResults = "Error;" + item + ";"/*Customer " + clientNo*/+ user + "'s bid of $" + bid + " was invalid";
			this.setChanged();
			this.notifyObservers(notifyResults);
		}
	}
	
	// print auction history
	public synchronized String printAuctionHistory() {
		String message = "History;";
		for (int index = 0; index < this.logOfAuction.size(); index++) {
			message += this.logOfAuction.get(index) + ";";
		}
		return message;
	}
	
	// print nonvolatile auction(s) history
	public synchronized String getNonVolatileHistory() {
		String message = "Nonvolatile;";
		try {
			Scanner scanner = new Scanner(nonvolatileHistory);
			while (scanner.hasNextLine()) {
				message += scanner.nextLine() + ";";
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return message;
	}
	
	// generate guest numbers for guest logins to differentiate btwn multiple guests
	public synchronized int getGuestNo() {
		this.guestNo++;
		return this.guestNo - 1;
	}
	
}
