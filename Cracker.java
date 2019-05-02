import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.CountDownLatch;


public class Cracker {
	// Array of chars used to produce strings
	public static final char[] CHARS = "abcdefghijklmnopqrstuvwxyz0123456789.,-!".toCharArray();	
	
	// private instance variables
	private String passwordHash;
	private int maxLength;
	private CountDownLatch allFinished;
	private String result;
	private boolean found;
	
	/*
	 Given a byte[] array, produces a hex String,
	 such as "234a6f". with 2 chars for each byte in the array.
	 (provided code)
	*/
	public static String hexToString(byte[] bytes) {
		StringBuffer buff = new StringBuffer();
		for (int i=0; i<bytes.length; i++) {
			int val = bytes[i];
			val = val & 0xff;  // remove higher bits, sign
			if (val<16) buff.append('0'); // leading 0
			buff.append(Integer.toString(val, 16));
		}
		return buff.toString();
	}
	
	/*
	 Given a string of hex byte values such as "24a26f", creates
	 a byte[] array of those values, one byte value -128..127
	 for each 2 chars.
	 (provided code)
	*/
	public static byte[] hexToArray(String hex) {
		byte[] result = new byte[hex.length()/2];
		for (int i=0; i<hex.length(); i+=2) {
			result[i/2] = (byte) Integer.parseInt(hex.substring(i, i+2), 16);
		}
		return result;
	}
	
	// stores information into instance variables and calls startWorkers
	public void findPassword(String passwordHash, int maxLength, int numberOfWorkers) {
		this.passwordHash = passwordHash;
		this.maxLength = maxLength;
		allFinished = new CountDownLatch(numberOfWorkers);
		result = "No result Found";
		found = false;
		
		startWorkers(numberOfWorkers);
	}
	
	
	// starts new workers 
	private void startWorkers(int numberOfWorkers) {
		// using this workers have roughly the same number of starting chars
		int[] charsNum = new int[numberOfWorkers];
		for(int i = 0; i < numberOfWorkers; i++) {
			charsNum[i] = CHARS.length / numberOfWorkers;
		}
		
		for(int i = 0; i < CHARS.length % numberOfWorkers; i++) {
			charsNum[i]++;
		}
			
		// starts new workers and finds start and end index for it
		int currInd = 0;
		for(int i = 0; i < numberOfWorkers; i++) {
			int startInd = currInd;
			int endInd = currInd + charsNum[i] - 1;
			
			Worker newWorker = new Worker(startInd, endInd);
			newWorker.start();
			currInd = endInd + 1;
		}
	}
	
	// returns hash of the given string 
	public static String getHash(String str) {
		try {
			MessageDigest messageDigest = MessageDigest.getInstance("SHA");
			messageDigest.update(str.getBytes());
			return hexToString(messageDigest.digest());
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return "";
	}
	
	// main thread waits on this CountDownLatch
	public void waitOnAllFinished() {
		try {
			allFinished.await();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}		
	}
	
	// after all prints result 
	public void printResult() {
		System.out.println(result);
		System.out.println("all done");
	}
	


	// inner Worker class
	private class Worker extends Thread {
		
		// private instance variables of Worker
		private int startInd;
		private int endInd;
		
		// Worker Constructor
		public Worker(int startInd, int endInd) {
			this.startInd = startInd;
			this.endInd = endInd;
		}
		
		@Override
	    public void run() {
			doWork();
	    }

		// generates start index character using its start and end indexes
		private void doWork() {
			for(int ind = startInd; ind <= endInd; ind++) {
				String startStr = "" + CHARS[ind];
				recFinding(startStr);
			}
			// post for main thread
			allFinished.countDown();
		}
		
		// recursion string generator
		// searches password using dfs algorithm
		private void recFinding(String currStr) {
			// if we already found the password 
			// or this string is larger than max Size, worker has to stop working
			if(currStr.length() > maxLength || found) return;
			
			if(match(currStr)) {
				result = currStr;
				found = true;
				return;
			}
			
			// adds new characters
			for(int i = 0; i < CHARS.length; i++) {
				recFinding(currStr + CHARS[i]);
			}
		}
		
		// determines if we found password
		private boolean match(String str) {
			return passwordHash.equals(getHash(str));
		}
	}
	
	
	// main method
	public static void main(String[] args) {		
		
		if(args.length == 1) {
			// returns hash of the password
			System.out.println(Cracker.getHash(args[0]));
		} else if(args.length == 3){
			// finds password using input 
			String passwordHash = args[0];
			int maxLength = Integer.parseInt(args[1]);
			int numberOfWorkers = Integer.parseInt(args[2]);
			
			Cracker crack = new Cracker();
			crack.findPassword(passwordHash, maxLength, numberOfWorkers);
			crack.waitOnAllFinished();
			crack.printResult();
		} else {
			System.out.println("Invalid Arguments");
		}
			
	}

	
	// possible test values:
	// a 86f7e437faa5a7fce15d1ddcb9eaeaea377667b8
	// fm adeb6f2a18fe33af368d91b09587b68e3abcb9a7
	// a! 34800e15707fae815d7c90d49de44aca97e2d759
	// xyz 66b27417d37e024c46526c2f6d358a754fc552f3

}
