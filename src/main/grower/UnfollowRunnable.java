package main.grower;

import main.actions.Unfollow;
import main.client.Client;

public class UnfollowRunnable implements Runnable {

	private Client client;

	public UnfollowRunnable(Client user) {
		this.client = user;
	}

	@Override
	public void run() {
		try {
			if (client.continueEngaging()){
				System.out.println("Sleeping for a moment before unfollowing...");
				Thread.sleep(120000);
			}
			Unfollow.unFollowUsersWhoHaventFollowedBackInXDays(client);
		} catch (InterruptedException e) {

		}

	}
}
