/**
 * Consumer3s.java
 * Works with Mailbox3g.java. Same as Consumer3b.java with renamed
 * variables
 */

public class Consumer3s extends Thread {
	private Mailbox3s myMailbox;
	private final int CHECKTIME = 20;

	public Consumer3s(Mailbox3s box) {
		myMailbox = box;
	}

	public void run () {
		while(true) {
			System.out.println("Looking for my mail ..");
			System.out.println(myMailbox.retrieveMessage());
			try {
				Thread.sleep(CHECKTIME);
			}
			catch (InterruptedException e) {}
			
		}
	}
}