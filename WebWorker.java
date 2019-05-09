import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Semaphore;

import javax.swing.*;

public class WebWorker extends Thread {
	
	// private instance variables
	private String urlString;
	private int row;
	private WebFrame frame;
	private Semaphore sem;
	private String result;
	
	// Constructor
	public WebWorker(String urlString, int row, WebFrame frame, Semaphore sem) {
		this.urlString = urlString;
		this.row = row;
		this.frame = frame;
		this.sem = sem;
		result = "";
	}
	
	@Override
	public void run() {
		frame.inreaseRunning();
		download();
		frame.decreaseRunning();
		frame.makeChanges(result, sem, row);
	}
	
	// downLoad method
	private void download() {
		InputStream input = null;
		StringBuilder contents = null;
		try {
			URL url = new URL(urlString);
			URLConnection connection = url.openConnection();
		
			// Set connect() to throw an IOException
			// if connection does not succeed in this many msecs.
			connection.setConnectTimeout(5000);
			
			connection.connect();
			input = connection.getInputStream();

			BufferedReader reader  = new BufferedReader(new InputStreamReader(input));
		
			char[] array = new char[1000];
			int len;
			long start = System.currentTimeMillis();
			int length = 0;
			
			contents = new StringBuilder(1000);
			while ((len = reader.read(array, 0, array.length)) > 0) {
				length += len;
				contents.append(array, 0, len);
				Thread.sleep(100);
			}
			
			// determines elapsed time
			long end = System.currentTimeMillis();
			long elpsTime = end - start;
			
			// Successful download if we get here
			// build res string
			result += new SimpleDateFormat("HH:mm:ss").format(new Date(start));
			result += "   " + elpsTime + "ms    ";
			result += length + "bytes";
		}
		// Otherwise control jumps to a catch...
		catch(MalformedURLException ignored) {
			result = "err";
		}
		catch(InterruptedException exception) {
			// YOUR CODE HERE
			// deal with interruption
			result = "interrupted";
		}
		catch(IOException ignored) {
			result = "err";
		}
		// "finally" clause, to close the input stream
		// in any case
		finally {
			try{
				if (input != null) input.close();
			}
			catch(IOException ignored) {}
		}
	}
	
}
