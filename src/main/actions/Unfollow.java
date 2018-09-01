package main.actions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.brunocvcunha.instagram4j.Instagram4j;
import org.brunocvcunha.instagram4j.requests.InstagramGetUserFollowersRequest;
import org.brunocvcunha.instagram4j.requests.InstagramUnfollowRequest;
import org.brunocvcunha.instagram4j.requests.payload.InstagramGetUserFollowersResult;
import org.brunocvcunha.instagram4j.requests.payload.InstagramUserSummary;
import org.brunocvcunha.instagram4j.requests.payload.StatusResult;

import main.client.Client;
import main.constants.Constants;
import main.util.Login;

public class Unfollow {

	public static Random rn = new Random();

	/**
	 * Return list of User Followers, as determined by the nextMaxID parameter.
	 * Accounts with less than 200 followers have their follower list sorted
	 * alphabetically. Accounts with greater than 200 followers have their
	 * follower list sorted by most recent follower.
	 * 
	 * @param client
	 */
	public static List<Long> getRecentFollowers(Client client) {

		if (client == null) {
			System.out.println("Input parameter Client client was null.");
			return null;
		}
		if (client.getFollowerIDs() != null && !client.getFollowerIDs().isEmpty()) {
			return client.getFollowerIDs();
		}
		Instagram4j instagram = client.getInstagram();
		if (instagram == null) {
			System.out.println("Instagram for admin was null for client " + client.getUsername());
			return null;
		}
		List<InstagramUserSummary> users = new ArrayList<InstagramUserSummary>();
		List<Long> followerIDs = new ArrayList<Long>();
		String nextMaxID = null;
		while (true) {
			InstagramGetUserFollowersResult fr = null;
			try {
				Thread.sleep(4000);
				synchronized (instagram) {
					fr = instagram.sendRequest(new InstagramGetUserFollowersRequest(client.getUserID(), nextMaxID));
				}
			} catch (Exception e) {
				System.out.println("Exception caught when getting followers for user " + client.getUsername());
			    continue;
			}
			if (fr != null){
				users.addAll(fr.getUsers());
				nextMaxID = fr.getNext_max_id();
			}

			if (nextMaxID == null || users.size() > 1000) {
				break;
			}
		}

		for (InstagramUserSummary user : users) {
			followerIDs.add(user.getPk());
		}
		client.setFollowerIDs(followerIDs);
		return followerIDs;

	}

	public static void unFollowUsersWhoHaventFollowedBackInXDays(Client client) throws InterruptedException {

		Map<Long, Set<Long>> followingFromLastWeek = client.getFollowingFromLastWeek();
		int timeToWait = client.getTimeToWaitBeforeUnfollowing();
		Long TimeInMilliseconds;
		if (client.isTimeUnitDays) {
			TimeInMilliseconds = TimeUnit.DAYS.toMillis(timeToWait);
		} else {
			TimeInMilliseconds = TimeUnit.HOURS.toMillis(timeToWait);
		}

		Long currentTimeInMilli = System.currentTimeMillis();
		Iterator<Long> iter = followingFromLastWeek.keySet().iterator();
		while (iter.hasNext()) {
			Long timeOfFollowing = iter.next();
			if ((Math.abs(Math.subtractExact(currentTimeInMilli, timeOfFollowing))) >= TimeInMilliseconds) {
				if (unFollowUsers(client, followingFromLastWeek.get(timeOfFollowing))) {
					iter.remove();
					System.out.println(
							"Unfollowed all users from " + timeOfFollowing + " for user " + client.getUsername());
				}
			}
		}
	}

	private static boolean unFollowUsers(Client client, Set<Long> followingFromLastWeek) throws InterruptedException {
		Instagram4j insta = client.getInstagram();
		List<Long> recentFollowers = getRecentFollowers(client);
		System.out.println("Recent Follower count for user " + client.getUsername() + ": " + recentFollowers.size());
		Iterator<Long> iter = followingFromLastWeek.iterator();
		while (iter.hasNext()) {
			if (Thread.currentThread().isInterrupted()){
				throw new InterruptedException();
			}
			Long following = iter.next();
			if (following != null && !recentFollowers.contains(following)) {
				try {
					StatusResult result = null;
					synchronized (client.getInstagram()) {
						result = insta.sendRequest(new InstagramUnfollowRequest(following));
					}
					if (checkIsSpam(result)){
						System.out.println("User " + client.getUsername() + " spammed while unfollowing "
								+ following);
						if (client.incrementSpamCount()){
							System.out.println("Thread interrupted due to unfollow spam");
							throw new InterruptedException();
						}
					}
					else{
						System.out.println("User " + client.getUsername() + " unfollowed " + following);
						iter.remove();
					}
					
					double randomValue = 60 + (6 * rn.nextDouble());
					Thread.sleep((long) (randomValue * 1000));
				} catch (IOException e) {

				}
			}

		}
		if (followingFromLastWeek.isEmpty()) {
			return true;
		}
		return false;
	}
	
	private static boolean checkIsSpam(StatusResult result) {
		return ((result == null || result.isSpam()
				|| !result.getStatus().equalsIgnoreCase(Constants.OK))
				&& (!StringUtils.isBlank(result.getMessage()) && !result.getMessage().contains("SC_NOT_FOUND")));
	}

}
