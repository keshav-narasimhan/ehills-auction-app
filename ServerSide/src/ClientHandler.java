/*
* EE422C Final Project submission by
* Replace <...> with your actual data.
* Keshav Narasimhan
* kn9558
* 17805
* Fall 2021
*/

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;

import javafx.scene.control.TextField;
import javafx.scene.text.Text;

public class ClientHandler implements Runnable, Observer {

	/*
	 * instance variables
	 */
	private Server server;
	private Socket client;
	private /*DataInputStream*/BufferedReader readClient;
	private /*DataOutputStream*/PrintWriter writeClient;
	private ObjectOutputStream outputMap;
	private int clientNo;
	private String username = "";
	
	public ClientHandler(Server server, Socket client, HashMap<String, AuctionItems> items, int clientNo) {
		// initialize components used to communicate to/from clients 
		this.server = server;
		this.client = client;
		this.clientNo = clientNo;
		try {
			this.readClient = /*new DataInputStream(client.getInputStream());*/ new BufferedReader(new InputStreamReader(client.getInputStream()));
			this.writeClient = new PrintWriter(client.getOutputStream());
			this.outputMap = new ObjectOutputStream(client.getOutputStream());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("Something when wrong initializing writer/reader to clients");
		}
		
		// send all auction items + data to client in setup first
		try {
			outputMap.writeObject(items);
			outputMap.flush();
		} catch (IOException e) {
			System.out.println("Try writing a map");
		}
	}
	
	@Override
	public void run() {
		try {
			String input;
			while ((input = readClient.readLine())!= null) {
				// regular login
				if (input.contains("Login")) {
					String [] received = input.split(";");
					String username = received[1];
					String password = received[2];
					int c = server.checkUser(this.clientNo, username, password);
					if (c < 0) {
						writeClient.println("Login");
						writeClient.flush();
					} else {
						this.server.addHistory(username + " logged in!");
						this.clientNo = c;
					}
					this.username = username;
				} 
				// guest login
				else if (input.equals("Guest")) {
					String username = "Guest " + server.getGuestNo();
					this.username = username;
					this.server.addHistory(username + " logged in!");
					writeClient.println("Code G;" + username);
					writeClient.flush();
				} 
				// logout
				else if (input.contains("Logout")) {
					String [] received = input.split(";");
					String username = received[1];
					this.server.addHistory(username + " logged out!");
				} 
				// print auction history
				else if (input.contains("History")) {
					String message = this.server.printAuctionHistory();
					writeClient.println(message);
					writeClient.flush();
				} 
				// print nonvolatile history
				else if (input.contains("Nonvolatile")) {
					String message = this.server.getNonVolatileHistory();
					writeClient.println(message);
					writeClient.flush();
				} 
				// checking errors
				else if (input.contains("Code1")) {
					// Error Code 1 : the item in question has been bought already
					String [] received = input.split(";");
					this.server.addHistory(/*"Customer " + clientNo*/username + " attempted to bid for the already-bought " + received[1]);
				} else if (input.contains("Code2")) {
					// Error Code 2 : the bid for the item is too low
					String [] received = input.split(";");
					this.server.addHistory(/*"Customer " + clientNo*/username + "'s bid of $" + received[2] + " for " + received[1] + " was too low");
				} 
				// process bids
				else {
					String [] received = input.split(";");
					String itemBidOn = received[0];
					double valueOfBid = Double.valueOf(received[1]);
					this.server.processBid(itemBidOn, valueOfBid, clientNo, username);
				}
			}
		} catch (IOException e) {
			System.out.println("Couldn't read from the client socket");
		}
	}

	@Override
	public void update(Observable o, Object arg) {
		// send to all observers (clients) connected to the observable object (server)
		String message = (String)arg;
		writeClient.println(message);
		writeClient.flush();
	}

}
