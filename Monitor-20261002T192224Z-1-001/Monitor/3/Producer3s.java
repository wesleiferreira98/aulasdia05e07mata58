/**
 * Producer3s.java
 * This is a source of 2 line messages to the mail box. There is a
 * delay between the two strings. The 100 ms is arbitrary.
 * If the delay is only 10 ms between the 2 sections fo the message
 * then the mail is not mixed up.
 * See also Consumer1.java and ThreadSync0.java
 */


public class Producer3s extends Thread {

	private final int DELAY = 131;		
	private Mailbox3s myMailbox;
	
	private String msg;
	private String name;

	public Producer3s (Mailbox3s box, String name, String msg) {
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
		