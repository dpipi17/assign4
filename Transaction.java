
public class Transaction {
	
	// private instance variables
	private int fromId;
	private int toId;
	private int transferMoney;
	
	// Constructor
	public Transaction(int fromId, int toId, int transferMoney) {
		this.fromId = fromId;
		this.toId = toId;
		this.transferMoney = transferMoney;
	}
	
	// getter method for To id
	public int getToId() {
		return toId;
	}
	
	// getter method for from id
	public int getFromId() {
		return fromId;
	}
	
	// getter method for Transfer Money
	public int getTransferMoney() {
		return transferMoney;
	}
	
}
