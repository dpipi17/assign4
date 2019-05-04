import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class JCount extends JPanel {
	
	// private constant variables
	private static final int SLEEP_TIME = 100;
	private static final int MOD = 10000;
	private static final String DEFAULT_MAX_VALUE = "100000000";
	private static final String DEFAULT_LABEL = "0";
	private static final int NUMBER_OF_JCOUNTS = 4;
	
	// private instance variables
	private JTextField textField;
	private JLabel label;
	private JButton start;
	private JButton stop;
	private Worker worker;
	
	// Constructor
	public JCount() {
		// call parent class constructor
		super();
		
		// initialize instance variables
		textField = new JTextField(DEFAULT_MAX_VALUE);
		label = new JLabel(DEFAULT_LABEL);
		start = new JButton("Start");
		stop = new JButton("Stop");
		worker = null;
		
		addComponents();
		addListeners();
		
	}
	
	// adds Listeners on the buttons
	private void addListeners() {
		// adds action Listeners on start button
		start.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {

				// if current worker is working, swing should interrupt it
				if(worker != null) {
					worker.interrupt();
					
				} 
				
				// create new worker, with new maxVal 
				worker = new Worker(Integer.parseInt(textField.getText()));
				worker.start();	
			}
		});
		
		
		// adds action Listeners on stop button
		stop.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {

				// if current worker is working, swing should interrupt it
				if(worker != null) {
					worker.interrupt();
					worker = null;
				}
			}
		});
	}

	// adds new Components on the JCount
	private void addComponents() {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		add(textField);
		add(label);
		add(start);
		add(stop);
		add(Box.createRigidArea(new Dimension(0,40))); 
	}
	
	// inner Worker class
	private class Worker extends Thread {
		
		// private instance variable of Worker
		private int currVal;
		private int maxVal;
		
		// Worker Constructor
		public Worker(int maxVal) {
			this.currVal = 0;
			this.maxVal = maxVal;
		}
		
		@Override
	    public void run() {
			doWork();
	    }

		// increases currVal and updates label
		private void doWork() {
			while(true) {
				//if thread is interrupted, it should stop working
				if(isInterrupted()) break;
				//if currVal reached maxVal, thread should stop working
				if(currVal >= maxVal) break;
				
				// situation when thread sleeps and then invokes swing to update label 
				if(currVal % MOD == 0) {
					try {
						sleep(SLEEP_TIME);
					} catch (InterruptedException e) {
						break;
					}
					
					int newVal = currVal;
					SwingUtilities.invokeLater(new Runnable() {
						
						@Override
						public void run() {
							label.setText(newVal + "");
						}
					});
					
				}			
				currVal++;
			}
		}
	}
	
	// creates frame and adds JCounts on it
	private static void createAndShowGUI() {  
		// create your GUI HERE 
		JFrame frame = new JFrame();
		frame.getContentPane().setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));
		
		for(int i = 0; i < NUMBER_OF_JCOUNTS; i++) {
			frame.add(new JCount());
		}
		
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
	} 
	

	// main method
	public static void main(String[] args) {   
		SwingUtilities.invokeLater(new Runnable() {   
			public void run() {     
				createAndShowGUI();   
			}   
		}); 
	} 
	
	
}
		 

