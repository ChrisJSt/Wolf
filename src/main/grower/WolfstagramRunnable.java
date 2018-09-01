package main.grower;

import java.util.Date;

import main.actions.SearchHashtags;
import main.client.Client;
import main.constants.Constants;
import main.util.Login;

public class WolfstagramRunnable implements Runnable {

	private Client client;

	public WolfstagramRunnable(Client user) {
		this.client = user;
	}

	public Client getClient() {
		return client;
	}

	@Override
	public void run() {
		
		// login with username and password if not already
		if (!client.isLoggedIn()) {
			boolean loginSuccess = Login.login(client, client.isCookieLogin());
			if (!loginSuccess) {
				System.out.println("Login Error for client " + client.getUsername());
				return;
			}
		}

		/**
		 * Start a thread to unfollow users who have not followed back after an
		 * X amount of days
		 */
		UnfollowRunnable unfollow = new UnfollowRunnable(client);
		Thread unFollowThread = new Thread(unfollow);
		unFollowThread.start();

		if (!client.continueEngaging()) {
			return;
		}
		long startTime = System.currentTimeMillis();
		long elapsedTime = 0L;
		
		while (elapsedTime < Constants.threeHours) {		
			try{
				SearchHashtags.searchAndLike(client);
			}
			catch (InterruptedException e){
				System.out.println("Caught Interrupted Exception."
						+ " About to interrupt unfollow thread.");
				unFollowThread.interrupt();
				break;
			}
			elapsedTime = (new Date()).getTime() - startTime;
			System.out.println("Elapsed Time was " + elapsedTime);
		}
		unFollowThread.interrupt();
		System.out.println("Time elapsed");

	}

}
