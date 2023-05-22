/*
* EE422C Final Project submission by
* Replace <...> with your actual data.
* Keshav Narasimhan
* kn9558
* 17805
* Fall 2021
*/

import java.io.Serializable;

public class AuctionItems implements Serializable {
	private String itemName;
	private String itemDescription;
	private double buyNowPrice;
	private double startBid;
	private String line;
	private boolean isBought;
	private double currBid;
	private int seconds;
	private String latestBid;
	
	public AuctionItems(String s) {
		String [] itemData = s.split(";");
		itemName = itemData[0];
		itemDescription = itemData[1];
		buyNowPrice = Double.parseDouble(itemData[2]);
		startBid = Double.parseDouble(itemData[3]);
		line = s;
		isBought = false;
		currBid = 0;
		seconds = Integer.parseInt(itemData[4]);
		latestBid = "";
	}

	public String getItemName() {
		return itemName;
	}

	public String getItemDescription() {
		return itemDescription;
	}

	public double getBuyNowPrice() {
		return buyNowPrice;
	}

	public double getStartBid() {
		return startBid;
	}
	
	public String toString() {
		return line;
	}
	
	public boolean isValidBid(double bid) {
		if (bid < startBid) {
			return false;
		} else if (bid < currBid) {
			return false;
		} else {
			return true;
		}
 	}
	
	public void placeBid(double bid) {
		currBid = bid;
		if (bid >= buyNowPrice) {
			isBought = true;
		}
	}
	
	public boolean isBought() {
		return isBought;
	}
	
	public double getCurrBid() {
		return this.currBid;
	}
	
	public void setBought(boolean b) {
		this.isBought = b;
	}
	
	public void setCurrBid(double b) {
		this.currBid = b;
	}
	
	public void decrementCounter() {
		if (this.seconds > 0) {
			this.seconds--;
		}
	}
	
	public int getCounter() {
		return this.seconds;
	}
	
	public void setCounter() {
		this.seconds = 0;
	}
	
	public void updateBid(String s) {
		this.latestBid = s;
	}
	
	public String getLatestBid() {
		return this.latestBid;
	}
	
}
