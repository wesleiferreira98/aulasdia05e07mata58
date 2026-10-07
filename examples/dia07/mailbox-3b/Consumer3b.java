/**
 * Consumer3b.java
 * A consumer class which gets messages from a mailbox and prints
 * them to stdout.	Looks for messages every CHECKTIME ms.
 * Used with Mailbox3b.java. Producer3b.java and ThreadSync3b.java.
 * (The classes ...3b, ...3g and ...3s for the the Consumers,
 * Producers and ThreadSyncs are all the same except for variable
 * name changes.)
 */

public class Consumer3b extends Thread {
	private Mailbox3b myMailbox;
	private final int CHECKTIME = 20;

	public Consumer3b(Mailbox3b box) {
		myMailbox = box;
	}

	public void run () {
		while(true) {
			System.out.println(myMailbox.retrieveMessage());
			try {
				Thread.sleep(CHECKTIME);
			}
			catch (InterruptedException e) {}
			
		}
	}
}