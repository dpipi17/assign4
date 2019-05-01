
public class Account {

	// private instance variables
	private int id;
	private int currBalance;
	private int numberOfTruns;
	
	// Constructor
	public Account(int id, int balance) {
		this.id = id;
		currBalance = balance;
		numberOfTruns = 0;
	}
	
	// Changes Balance and pluses transactions counter
	public synchronized void changeBalance(int diff) {
		currBalance += diff;
		numberOfTruns++;
	}
	
	// getter method for id
	public synchronized int getId() {
		return id;
	}
	
	// getter method for Balance
	public synchronized int getBalance() {
		return currBalance;
	}
	
	// getter method for NumberOfTruns
	public synchronized int getNumberOfTruns() {
		return numberOfTruns;
	}
	
	// To String method to print Account
	@Override
	public String toString() {
		StringBuilder buffer = new StringBuilder();
		buffer.append("acc: " + getId() + " ");
		buffer.append("bal:" + getBalance() + " ");
		buffer.append("trans:" + getNumberOfTruns());
		
		return (buffer.toString());
	}

}
