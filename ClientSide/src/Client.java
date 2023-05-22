/*
* EE422C Final Project submission by
* Replace <...> with your actual data.
* Keshav Narasimhan
* kn9558
* 17805
* Fall 2021
*/

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Observable;
import java.util.Observer;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Client extends Application {

	/*
	 * instance variables
	 */
	private static final String IP = "10.1.76.254";// "10.148.89.13";//"192.168.1.233";
	private BufferedReader readServer;
	private PrintWriter writeServer;
	private ObjectInputStream inputMap;
	private static HashMap<String, AuctionItems> items = new HashMap<>();
	private static ArrayList<Object> controls = new ArrayList<>();
	private HashMap<String, Object> currBidDescriptions = new HashMap<>();
	private Stage stage;
	private GridPane pane;
	private GridPane historyPane;
	private String userName = "";
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		// initialize client socket
		Socket clientSocket = new Socket(IP, 1717);
		System.out.println("Got a connection");
		readServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		writeServer = new PrintWriter(clientSocket.getOutputStream());
		inputMap = new ObjectInputStream(clientSocket.getInputStream());

		// obtain auction items from server to make GUI
		items = (HashMap<String, AuctionItems>)inputMap.readObject();
		for (String s : items.keySet()) {
			System.out.println(s);
		}
		
		// gridpane initialization + create a scene for it
		this.pane = new GridPane();
		stage = primaryStage;
		Scene scene = new Scene(pane, 800, 650);
		stage.setScene(scene);
		stage.show();
		
		// background image of a gavel for the client GUI
		pane.setStyle("-fx-background-image: "
				//+ "url('https://ak.picdn.net/shutterstock/videos/20439940/thumb/1.jpg'); "
				+ "url('https://upload.wikimedia.org/wikipedia/commons/c/cb/3D_Judges_Gavel.jpg');"
				+ "-fx-background-repeat: no-repeat; "
				+ "-fx-background-size: 800 800; "
				+ "-fx-background-position: center center;");
		
		pane.setVgap(15);
		pane.setHgap(15);
		
		// to be used later
		Button auctionHistory = new Button("History");
		
		// initialize historyPane
		historyPane = new GridPane();
		ScrollPane scroll = new ScrollPane(historyPane);
		historyPane.getChildren().add(new Text());
		historyPane.setVisible(false);
		Stage secondaryStage = new Stage();
		secondaryStage.setTitle("Auction History");
		Scene secondaryScene = new Scene(scroll, 300, 400);
		secondaryStage.setScene(secondaryScene);
		secondaryStage.show();
		
		// initialize all control components for GUI as well as their functionality
		int row = 1;
		for (String item : items.keySet()) {
			// create components for each Auction Item
			Button button = new Button("Get " + item);
			button.setStyle("-fx-background-color: #ffff00; -fx-border-color: #000000");
			Text description = new Text(items.get(item).getItemDescription());
			description.setFont(Font.font("Verdana", FontWeight.BOLD, 12));
			description.setFill(Color.BLACK);
			TextField bid = new TextField();
			bid.setPromptText("Enter bid");
			Text buynow = new Text("Buy Now Price! >> \t$" + String.valueOf(items.get(item).getBuyNowPrice()) + "\nStarting Bid! \t >> \t$" + String.valueOf(items.get(item).getStartBid()) + "\nTime: " + String.valueOf(items.get(item).getCounter()) + " seconds");
			Text currBidDescription = new Text("Current Bid >> ");
			TextField currBid = new TextField();
			currBid.setText("$" + String.valueOf(items.get(item).getCurrBid()));
			currBid.setEditable(false);
			Text customerBid = new Text(items.get(item).getLatestBid());
			if (customerBid.getText().contains(";")) {
				String [] received = items.get(item).getLatestBid().split(";");
				customerBid.setText(received[0] + "\n" + received[1]);
			}
			customerBid.setFill(Color.CYAN);
			if (items.get(item).isBought()) {
				bid.setEditable(false);
				button.setDisable(true);
				String s = customerBid.getText();
				int ind = s.indexOf("'");
				String out =  "This item has been bought by \n" + s.substring(0, ind);
				customerBid.setText(out);
				customerBid.setFill(Color.RED);
			}
			
			// references to customerBid field for each item
			currBidDescriptions.put(item, customerBid);
			
			// send bids to the server
			button.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent event) {
					String bidText = bid.getText();
					double val = 0.0;
					try {
						val = Double.valueOf(bidText);
						String send;
						if (items.get(item).isBought()) {
							send = "Code1;" + item + ";" + val;
							customerBid.setText("This item is already bought!");
							customerBid.setFill(Color.CYAN);
						} else if (val <= items.get(item).getCurrBid() || val < items.get(item).getStartBid()) {
							send = "Code2;" + item + ";" + val;
							customerBid.setText("This bid is too low!");
							customerBid.setFill(Color.CYAN);
						} else {
							send = item + ";" + val;
						}
						writeServer.println(send);
						writeServer.flush();
					} catch(Exception e) {
						customerBid.setText("INVALID BID! Enter a valid money amount");
						customerBid.setFill(Color.CYAN);
					}
				}
			});
			
			// add control items to a log of all controls on the GUI
			controls.add(button);
			controls.add(description);
			controls.add(bid);
			controls.add(buynow);
			controls.add(currBidDescription);
			controls.add(currBid);
			controls.add(customerBid);
			
			// initially make these invisible for the user/pswd login
			button.setVisible(false);
			description.setVisible(false);
			bid.setVisible(false);
			buynow.setVisible(false);
			currBidDescription.setVisible(false);
			currBid.setVisible(false);
			customerBid.setVisible(false);
			
			// add components to the pane
			pane.add(button, 1, row);
			pane.add(description, 2, row);
			pane.add(bid, 3, row);
			pane.add(buynow, 4, row);
			pane.add(currBidDescription, 2, row + 1);
			pane.add(currBid, 3, row + 1);
			pane.add(customerBid, 4, row + 1);
			
			// move to the next row
			row += 2;
		}
		
		// Logout button
		Button logout = new Button("Logout");
		logout.setVisible(false);
		pane.add(logout, 1, row + 1);
		logout.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				writeServer.println("Logout;" + userName);
				writeServer.flush();
				System.exit(0);
			}
		});
		
		// add the history button to the pane
		auctionHistory.setVisible(false);
		pane.add(auctionHistory, 1, row);
		auctionHistory.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				writeServer.println("History");
				writeServer.flush();
			}
		});
		
		// nonvolatile history button
		Button nonvolatileHistory = new Button("Nonvolatile History");
		nonvolatileHistory.setVisible(false);
		pane.add(nonvolatileHistory, 2, row);
		nonvolatileHistory.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				writeServer.println("Nonvolatile");
				writeServer.flush();
			}
		});
		
		// username + password login buttons/controls
		TextField username = new TextField();
		username.setPromptText("Enter Username");
		PasswordField password = new PasswordField();
		password.setPromptText("Enter password");
		TextField actualPswd = new TextField();
		actualPswd.setVisible(false);
		pane.add(username, 2, 1);
		pane.add(password, 2, 2);
		pane.add(actualPswd, 2, 2);
		CheckBox showPswd = new CheckBox("Show Password");
		Text pswdText = new Text("Password must contain\natleast 6 characters with\nat least one lowercase, uppercase, and digit");
		Button moveOn = new Button("Enter");
		pane.add(moveOn, 3, 1);
		pane.add(pswdText, 3, 2);
		pane.add(showPswd, 4, 2);
		Button guestLogin = new Button("Continue as guest");
		pane.add(guestLogin, 4, 1);
		
		// show encrypted password on checked box
		showPswd.setOnAction(new EventHandler<ActionEvent> () {
			@Override
			public void handle(ActionEvent event) {
				if (showPswd.isSelected()) {
					actualPswd.setText(password.getText());
					actualPswd.setVisible(true);
					password.setVisible(false);
				} else {
					actualPswd.setVisible(false);
					password.setText(actualPswd.getText());
					password.setVisible(true);
				}
			}
		});
		
		// sign in via username + password
		moveOn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				try {
					String user = username.getText();
					if (user.length() == 0) { throw new Exception(); }
					String pswd = password.getText();
					if (!checkPswd(pswd)/*pswd.length() < 6*/) { throw new Exception(); }
					
					moveOn.setVisible(false);
					username.setVisible(false);
					password.setVisible(false);
					pswdText.setVisible(false);
					guestLogin.setVisible(false);
					actualPswd.setVisible(false);
					showPswd.setVisible(false);
					
					for (int i = 0; i < controls.size(); i+=7) {
						Button b = (Button)controls.get(i);
						b.setVisible(true);
						
						Text t1 = (Text)controls.get(i+1);
						t1.setVisible(true);
						
						TextField tf1 = (TextField)controls.get(i+2);
						tf1.setVisible(true);
						
						Text t2 = (Text)controls.get(i+3);
						t2.setVisible(true);
						
						Text t3 = (Text)controls.get(i+4);
						t3.setVisible(true);
						
						TextField tf2 = (TextField)controls.get(i+5);
						tf2.setVisible(true);
						
						Text t4 = (Text)controls.get(i+6);
						t4.setVisible(true);
					}
					
					auctionHistory.setVisible(true);
					nonvolatileHistory.setVisible(true);
					logout.setVisible(true);
					historyPane.setVisible(true);	
					userName = user;
					stage.setTitle(userName);
					
					String message = "Login;" + user + ";" + pswd;
					writeServer.println(message);
					writeServer.flush();
				} catch(Exception e) {
					username.setPromptText("Enter Valid Username!");
					password.setPromptText("Enter Valid Password!");
					pswdText.setText("Enter Valid Password!");
				}
			}
		});

		// guest login
		guestLogin.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				moveOn.setVisible(false);
				username.setVisible(false);
				password.setVisible(false);
				pswdText.setVisible(false);
				guestLogin.setVisible(false);
				actualPswd.setVisible(false);
				showPswd.setVisible(false);
				
				for (int i = 0; i < controls.size(); i+=7) {
					Button b = (Button)controls.get(i);
					b.setVisible(true);
					
					Text t1 = (Text)controls.get(i+1);
					t1.setVisible(true);
					
					TextField tf1 = (TextField)controls.get(i+2);
					tf1.setVisible(true);
					
					Text t2 = (Text)controls.get(i+3);
					t2.setVisible(true);
					
					Text t3 = (Text)controls.get(i+4);
					t3.setVisible(true);
					
					TextField tf2 = (TextField)controls.get(i+5);
					tf2.setVisible(true);
					
					Text t4 = (Text)controls.get(i+6);
					t4.setVisible(true);
				}
				
				auctionHistory.setVisible(true);
				nonvolatileHistory.setVisible(true);
				logout.setVisible(true);
				historyPane.setVisible(true);
				stage.setTitle("Guest");

				writeServer.println("Guest");
				writeServer.flush();
			}
		});
		
		// thread to read data from the server to make actions
		Thread readerThread = new Thread(new Runnable() {
			@Override
			public void run() {
				String messageFromServer;
				try {
					while ((messageFromServer = readServer.readLine()) != null) {
						// incorrect login
						if (messageFromServer.contains("Login")) {
							for (int i = 0; i < controls.size(); i+=7) {
								Button b = (Button)controls.get(i);
								b.setVisible(false);
								
								Text t1 = (Text)controls.get(i+1);
								t1.setVisible(false);
								
								TextField tf1 = (TextField)controls.get(i+2);
								tf1.setVisible(false);
								
								Text t2 = (Text)controls.get(i+3);
								t2.setVisible(false);
								
								Text t3 = (Text)controls.get(i+4);
								t3.setVisible(false);
								
								TextField tf2 = (TextField)controls.get(i+5);
								tf2.setVisible(false);
								
								Text t4 = (Text)controls.get(i+6);
								t4.setVisible(false);
							}
							
							auctionHistory.setVisible(false);
							nonvolatileHistory.setVisible(false);
							logout.setVisible(false);
							historyPane.setVisible(false);
							
							moveOn.setVisible(true);
							username.setVisible(true);
							password.setVisible(true);
							guestLogin.setVisible(true);
							actualPswd.setVisible(false);
							showPswd.setVisible(true);
							pswdText.setVisible(true);
							pswdText.setText("Incorrect Password!");
							
						}
						// guest login
						else if (messageFromServer.contains("Code G")) {
							String [] received = messageFromServer.split(";");
							userName = received[1];
						}
						// update count down clocks
						else if (messageFromServer.contains("Clock")) {
							String [] received = messageFromServer.split(";");
							for (int i = 1; i < received.length; i++) {
								AuctionItems item = items.get(received[i]);
								item.decrementCounter();
								Text t = (Text)currBidDescriptions.get(received[i]);
								int index = pane.getChildren().indexOf(t);
								Text change = (Text)pane.getChildren().get(index - 3);
								change.setText("Buy Now Price! >> \t$" + String.valueOf(items.get(received[i]).getBuyNowPrice()) + "\nStarting Bid! \t >> \t$" + String.valueOf(items.get(received[i]).getStartBid()) + "\nTime: " + String.valueOf(items.get(received[i]).getCounter()) + " seconds");
								
								if (item.getCounter() == 0) {
									if (t.getText().equals("This item is already bought!")) {
										t.setText(item.getLatestBid());
									}
									if (t.getText().equals("This bid is too low!")) {
										t.setText(item.getLatestBid());
									}
									if (t.getText().equals("INVALID BID! Enter a valid money amount")) {
										t.setText(item.getLatestBid());
									}
									
									if (!item.isBought() && t.getText().length() != 0) {
										String currText = t.getText();
										int ind = currText.indexOf("'");
										String out =  "This item has been bought by \n" + currText.substring(0, ind);
										t.setText(out);
										item.setBought(true);
									}
									t.setFill(Color.RED);
									
									TextField noMoreBids = (TextField)pane.getChildren().get(index - 4);
									noMoreBids.setEditable(false);
									
									Button disableButton = (Button)pane.getChildren().get(index - 6);
									disableButton.setDisable(true);
								} else {
									if (item.isBought()) {
										item.setCounter();
									}
								}
							}
						}
						// print history of auction
						else if (messageFromServer.contains("History")) {
							String [] received = messageFromServer.split(";");
							String out = "Auction History:\n";
							for (int i = 1; i < received.length; i++) {
								out += received[i];
								out += "\n";
							}
							Text hist = (Text)historyPane.getChildren().get(0);
							hist.setText(out);
						}
						// print non-volatile history
						else if (messageFromServer.contains("Nonvolatile")) {
							String [] received = messageFromServer.split(";");
							String out = "All Auction History:\n";
							for (int i = 1; i < received.length; i++) {
								out += received[i];
								out += "\n";
							}
							Text hist = (Text)historyPane.getChildren().get(0);
							hist.setText(out);
						}
						// invalid bid from the client
						else if(messageFromServer.contains("Error")) {
							String [] received = messageFromServer.split(";");
							Text text = (Text)currBidDescriptions.get(received[1]);
							text.setText(received[2]);
							text.setFill(Color.CYAN);
						} 
						// valid bid from the client
						else {
							String [] received = messageFromServer.split(";");
							Text text = (Text)currBidDescriptions.get(received[0]);
							text.setText(received[1] + "\n" + received[2]);
							text.setFill(Color.CYAN);
							items.get(received[0]).updateBid(received[1] + "\n" + received[2]);
							
							int index = pane.getChildren().indexOf(text);
							TextField textfield = (TextField)pane.getChildren().get(index - 1);
							double bid = Double.valueOf(received[3]);
							items.get(received[0]).setCurrBid(bid);
							textfield.setText("$" + String.valueOf(bid));
							
							boolean hasBeenBought = Boolean.valueOf(received[4]);
							items.get(received[0]).setBought(hasBeenBought);
							if(hasBeenBought) {
								text.setText("This item has been bought by \n" + received[5]);
								text.setFill(Color.RED);
								
								TextField noMoreBids = (TextField)pane.getChildren().get(index - 4);
								noMoreBids.setEditable(false);
								
								Button disableButton = (Button)pane.getChildren().get(index - 6);
								disableButton.setDisable(true);
							}
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		
		// start the thread
		readerThread.start();
		
	}
	
	public static void main(String [] args) {
		launch(args);
	}
	
	// check validity of entered password
	private boolean checkPswd(String pswd) {
		if (pswd.length() < 6) {
			return false;
		}
		
		boolean containsDigit = false;
		boolean containsLowerCase = false;
		boolean containsUpperCase = false;
		
		for (int i = 0; i < pswd.length(); i++) {
			char c = pswd.charAt(i);
			if (Character.isLetter(c)) {
				if (Character.isLowerCase(c)) {
					containsLowerCase = true;
				}
				if (Character.isUpperCase(c)) {
					containsUpperCase = true;
				}
			}
			if (Character.isDigit(c)) {
				containsDigit = true;
			}
			
			if (containsDigit && containsLowerCase && containsUpperCase) {
				return true;
			}
		}
		
		return false;
	}
	
}
