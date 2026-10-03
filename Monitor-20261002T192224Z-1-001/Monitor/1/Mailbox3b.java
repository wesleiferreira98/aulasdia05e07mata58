/**
 *Mailbox3b.java
 * Objects of the Mailbox3b class are shared by Producer and
 * Consumer objects. Producers send messages to the mailbox at
 * random intervals. Consumers collect the mail at fixed intervals.
 *
 * This mailbox does not work because there is no coordination. 
 * To fix you need the Java synchronization facility.
 * See also ThreadSync3b.java, Producer3b.java and Consumer3b.java.
 * The synchronized version is in Mailbox3g.java and Mailbox3s.java.
 */

public class Mailbox3b {
	private boolean youHaveMail;	// no longer public. 
								// state variables should be private!
	private char [] message;
	private final int MAXMESSAGE = 72;
	private final int MAXPROCESSTIME = 7;
	
	
	public Mailbox3b() {
		youHaveMail = false;
		message = new char[MAXMESSAGE];
	}

	public  void storeMessage(String msg) {
		
		for(int i = 0; i < msg.length(); i++) {
			message[i] = msg.charAt(i);
			try {
				Thread.sleep((int) (MAXPROCESSTIME * Math.random()));
			}
			catch (InterruptedException e) {}
		}
		for(int i = msg.length(); i < MAXMESSAGE; i++)
			message[i] = ' ';
		youHaveMail = true;
	}


	public String retrieveMessage() {
	
		if(youHaveMail) {
			String msg =  new String(message,0, 72);
			youHaveMail = false;
			return (msg );
		}
		else {
			return "How sad, no mail ..";
		}
	}
}										 