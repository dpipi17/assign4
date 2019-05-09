import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.Semaphore;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JProgressBar;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;


public class WebFrame extends JFrame {

	// private instance variables
	private DefaultTableModel model;
	private JTable table;
	private JPanel panel;
	
	// buttons
	private JButton singleFetch;
	private JButton concurFetch;
	private JButton stop;
	
	// labels
	private JLabel running;
	private JLabel completed;
	private JLabel elapsed;
	
	// other instance variables
	private JTextField field;
	private JProgressBar bar;
	private Vector<Vector<String>> tableData;
	private int urlNum;
	private int runningNum;
	private int completedNum;
	private Launcher launcher;
	private long startTime;
	private long endTime;
	
	
	private static final String FILE_NAME = "links.txt"; 
	
	// Constructor of WebFrame
	public WebFrame() {
		super("WebLoader");
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		tableData = new Vector<Vector<String>>();
		
		model = new DefaultTableModel(new String[] { "url", "status"}, 0);   
		table = new JTable(model);  
		table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS); 
		  
		JScrollPane scrollpane = new JScrollPane(table);   
		scrollpane.setPreferredSize(new Dimension(600,300));   
		panel.add(scrollpane); 
		
		runningNum = 0;
		completedNum = 0;
		readFile();
		urlNum = tableData.size();
		
		
		createComponents();
		addComponents();
		addListeners();
		add(panel);
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		pack();
		setVisible(true);	 
	}
	
	// read information from file and insert it to model
	private void readFile() {
		try {
			// reading information from file
			BufferedReader br = new BufferedReader(new FileReader(FILE_NAME));
			String currLine;
			
			while ((currLine = br.readLine()) != null) {
				Vector<String> v = new Vector<String>();
				v.add(currLine);
				model.addRow(v);
				tableData.add(v);
			}
			br.close();
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	// creates buttons, textfield, Labels and ProgressBar
	private void createComponents() {
		singleFetch = new JButton("Single Thread Fetch");
		concurFetch = new JButton("Concurrent Fetch");
		stop = new JButton("Stop");
		stop.setEnabled(false);	
		
		field = new JTextField();
		field.setMaximumSize(new Dimension(50, 0));
		
		running = new JLabel("Running:0");
		completed = new JLabel("Completed:0");
		elapsed = new JLabel("Elapsed:");
		
		bar = new JProgressBar();
		bar.setMinimum(0);
	}
	
	// add every component in panel
	private void addComponents() {
		panel.add(singleFetch);
		panel.add(concurFetch);
		panel.add(field);
		panel.add(running);
		panel.add(completed);
		panel.add(elapsed);
		panel.add(bar);
		panel.add(stop);
	}

	// addListeners on every button
	private void addListeners() {
		listenSingleFetch();
		listenconcurFetch();
		listenStop();
	}

	// listens to single Fetch button
	private void listenSingleFetch() {
		singleFetch.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				runningState();
				launcher = new Launcher(1);
				launcher.start();
			}
		});
		
	}
	
	// listens to concurrent Fetch button
	private void listenconcurFetch() {
		concurFetch.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				int limit = Integer.parseInt(field.getText());
				runningState();
				launcher = new Launcher(limit);
				launcher.start();
			}
		});
		
	}
	
	// listens to stop button
	private void listenStop() {
		stop.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(launcher != null) {
					launcher.interrupt();
					launcher = null;
				}
			}
		});
	}
	
	// makes everything in running state
	private void runningState() {
		startTime = System.currentTimeMillis();
		
		stop.setEnabled(true);
		singleFetch.setEnabled(false);
		concurFetch.setEnabled(false);
		
		field.setText("");
		bar.setMaximum(urlNum);

		completedNum = 0;
		completed.setText("Completed:0");
		elapsed.setText("Elapsed:");
		
		// clears right column information for every row
		for(int i = 0; i < urlNum; i++) {
			model.setValueAt("", i, 1);
		}
	}
	
	// makes everything in ready state
	private void readyState() {
		stop.setEnabled(false);
		singleFetch.setEnabled(true);
		concurFetch.setEnabled(true);
		endTime = System.currentTimeMillis();
		
		// determines elapsed time
		long elapsTime = endTime - startTime; 
		elapsed.setText("Elapsed:" + elapsTime);
		
		bar.setValue(0);
	}
	
	// increases running threads counter by one and updates running label
	// its atomic
	public synchronized void inreaseRunning() {
		runningNum++;
		int newVal = runningNum;
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				running.setText("Running:" + newVal);
			}
		});
	}
	
	// decreases running threads counter by one and updates running label
	// its atomic
	public synchronized void decreaseRunning() {
		runningNum--;
		int newVal = runningNum;
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				running.setText("Running:" + newVal);
			}
		});
	}
	
	// increases completed threads counter by one and updates completed label
	// its atomic
	public synchronized void inreaseCompleted() {
		completedNum++;
		int newVal = completedNum;
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				completed.setText("Completed:" + newVal);
				bar.setValue(completedNum);
			}
		});
	}
	
	// makes changes into JTable(second column values) and releases launcher semaphore
	public void makeChanges(String result, Semaphore sem, int row) {
		inreaseCompleted();
		sem.release();
		model.setValueAt(result, row, 1);
	}
	
	
	// inner launcher class
	private class Launcher extends Thread {
		
		// private instance variables
		private int limit;
		private List<WebWorker> workers;
		
		// Constructor
		public Launcher(int limit) {
			this.limit = limit;
			workers = new ArrayList<WebWorker>();
		}
		
		@Override
	    public void run() {
			Semaphore sem = new Semaphore(limit);
			doWork(sem);
	    }

		// inits threads, starts workers and waits to it
		private void doWork(Semaphore sem) {
			// increase number of running thread
			inreaseRunning();
			
			try {
				// init and starts workers and adds it to list
				for(int i = 0; i < urlNum; i++) {
					sem.acquire();
					WebWorker worker = new WebWorker(tableData.get(i).get(0), i, WebFrame.this, sem);
					workers.add(worker);
					worker.start();
				}
				
				// waits workers to done work
				for(WebWorker worker : workers) {
					worker.join();
				}
				
			} catch (InterruptedException e) {
				// interrupts every worker
				for(WebWorker worker : workers) {
					worker.interrupt();
				}
			}	
			// decrease number of running thread
			decreaseRunning();
			
			// all work done, swing has to change world to ready State
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					readyState();
				}
			});	
		}
	}


	
	// main method
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
            public void run() {
            	WebFrame frame = new WebFrame();
            }
        });
	}
}
