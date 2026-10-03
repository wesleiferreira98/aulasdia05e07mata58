/**
 * Producer3b.java
 * Produces short string messages at random intervals.
 * See also Mailbox3b.java, ThreadSync3b.java, and Consumer3b.java
 */

public class Producer3b extends Thread {

	private final int DELAY = 131;		
																	// if 100 messages mixed up.
	private Mailbox3b myMailbox;
	private String msg;
	private String name;

	public Producer3b (Mailbox3b box, String name, String msg) {
		this.msg = msg;
		this.name = name;
		myMailbox = box;
	}

	public void run() {
		while(true) {
		try {	
				myMailbox.storeMessage("My name is, " + name + ". " +
										"I say, " + msg);
				sleep((int) (DELAY * Math.random())); 
			} catch (InterruptedException e) {}
		}
	}
}
		