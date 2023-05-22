/*
* EE422C Final Project submission by
* Replace <...> with your actual data.
* Keshav Narasimhan
* kn9558
* 17805
* Fall 2021
*/

public class UserDetails {
	private int clientNo;
	private String username;
	private String password;
	
	public UserDetails(int c, String u, String p) {
		this.clientNo = c;
		this.username = u;
		this.password = p;
	}

	public int getClientNo() {
		return clientNo;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}
	
}
