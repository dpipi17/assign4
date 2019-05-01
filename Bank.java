import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;

public class Bank {
	
	// private instance variables
	private static BlockingQueue<Transaction> blockQ;
	private static List<Account> accList;
	private static CountDownLatch allFinished;
	
	private static final int NUMBER_OF_ACCOUNTS = 20;
	private static final int START_CASH = 1000;
	private static final int BREAK_SYMBOL = -1;
	
	// Constructor
	public Bank(int numberOfWorkers) {
		blockQ = new ArrayBlockingQueue<>(numberOfWorkers);
		accList = new ArrayList<>();
		allFinished = new CountDownLatch(numberOfWorkers);
		
		createAccounts();
		startWorkers(numberOfWorkers);	
	}
	
	// creates new Accounts
	private void createAccounts() {
		for(int i = 1; i <= NUMBER_OF_ACCOUNTS; i++) {
			Account newAccount = new Account(i, START_CASH);
			accList.add(newAccount);
		}	
	}
	
	// starts new Workers
	private void startWorkers(int numberOfWorkers) {
		for(int i = 0; i < numberOfWorkers; i++) {
			Worker newWorker = new Worker();
			newWorker.start();
		}
	}

	// main thread waits on this CountDownLatch
	private static void waitOnAllFinished() {
		try {
			allFinished.await();
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}		
	}
	
	// add Special transactions in Blocking queue for workers.
	// using this symbol worker finishes work.
	private static void addNulTrans(int numberOfWorkers) {
		for(int i = 0; i < numberOfWorkers; i++) {
			Transaction nullTrans = new Transaction(BREAK_SYMBOL,0,0);
			try {
				blockQ.put(nullTrans);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	// inner Worker class
	private class Worker extends Thread {
		
		@Override
	    public void run() {
			doWork();
	    }

		// makes new transaction operations
		private void doWork() {
			while(true) {
				try {
					Transaction currTrans = blockQ.take();
					if(currTrans.getFromId() == BREAK_SYMBOL) break;
					
					accList.get(currTrans.getFromId()).changeBalance(-currTrans.getTransferMoney());
					accList.get(currTrans.getToId()).changeBalance(currTrans.getTransferMoney());
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
			// post for main thread
			allFinished.countDown();
		}
	}
	
	// after all prints accounts balances 
	private static void printResults() {
		for(int i = 0; i < accList.size(); i++) {
			System.out.println(accList.get(i));
		}
	}

	// main method
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String fileName = args[0];
		int numberOfWorkers = Integer.parseInt(args[1]);
		Bank bank = new Bank(numberOfWorkers); 
		
		try {
			// reading information from file
			BufferedReader br = new BufferedReader(new FileReader(fileName));
			String currLine;
			
			while ((currLine = br.readLine()) != null) {
				String[] transactionsDetails = currLine.split(" "); 
				
				int from = Integer.parseInt(transactionsDetails[0]);
				int to = Integer.parseInt(transactionsDetails[1]);
				int transferMoney = Integer.parseInt(transactionsDetails[2]);
				
				Transaction newTrans = new Transaction(from, to, transferMoney);
				blockQ.put(newTrans);
			}
			
			addNulTrans(numberOfWorkers);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// wait workers to finish work
		waitOnAllFinished();
		printResults();
	}

}
