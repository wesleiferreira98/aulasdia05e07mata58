/**
 * ThreadSync3s.java
 * This version uses Mailbox3s.java and Producers and Consumers
 * as before but with suitable variable renaming.
 * The Mailbox3g class has synchronized methods and uses
 * wait() and notify(). Sample output in ThreadSync3s.txt.
 */

public class ThreadSync3s {

	public static void main(String [] args) {
		Mailbox3s mailbox = new Mailbox3s();
		Consumer3s cons1 = new Consumer3s(mailbox);
		Producer3s prod1 = new Producer3s( mailbox, "Dave", "Hello, world.");
		Producer3s prod2 = new Producer3s(mailbox, "Bill", "Hot dog!");
		cons1.start();
		prod1.start();
		prod2.start();
	}
}

