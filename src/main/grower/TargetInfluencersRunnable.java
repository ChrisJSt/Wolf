package main.grower;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.http.client.ClientProtocolException;
import org.brunocvcunha.instagram4j.Instagram4j;
import org.brunocvcunha.instagram4j.requests.InstagramFollowRequest;
import org.brunocvcunha.instagram4j.requests.InstagramGetUserFollowersRequest;
import org.brunocvcunha.instagram4j.requests.InstagramGetUserInfoRequest;
import org.brunocvcunha.instagram4j.requests.InstagramSearchUsernameRequest;
import org.brunocvcunha.instagram4j.requests.payload.InstagramGetUserFollowersResult;
import org.brunocvcunha.instagram4j.requests.payload.InstagramSearchUsernameResult;
import org.brunocvcunha.instagram4j.requests.payload.InstagramUser;
import org.brunocvcunha.instagram4j.requests.payload.InstagramUserSummary;

import main.client.Client;

public class TargetInfluencersRunnable implements Runnable {

	private Client client;

	private static Random rn = new Random();

	public TargetInfluencersRunnable(Client client) {
		this.client = client;
	}

	@Override
	public void run() {
		if (client.targetInfluencers != null || !client.targetInfluencers.isEmpty()) {
			Instagram4j instagram = client.getInstagram();
			for (String influencerName : client.targetInfluencers) {

				InstagramSearchUsernameResult userResult = null;
				try {
					synchronized (instagram) {
						userResult = instagram.sendRequest(new InstagramSearchUsernameRequest(influencerName));
					}
				} catch (IOException e) {
					System.out.println("IOException caught when searching username " + influencerName);
				}
				InstagramUser influencer = null;
				if (userResult == null || (influencer = userResult.getUser()) == null) {
					System.out.println("User " + influencerName + " was null");
					continue;
				}

				List<InstagramUserSummary> influencerFollowers = new ArrayList<InstagramUserSummary>();
				String nextMaxID = null;
				while (true) {
					InstagramGetUserFollowersResult fr = null;
					try {
						synchronized (instagram) {
							fr = instagram
									.sendRequest(new InstagramGetUserFollowersRequest(influencer.getPk(), nextMaxID));
						}
					} catch (Exception e) {
						System.out.println("Exception caught when getting followers for user " + client.getUsername());
					}
					influencerFollowers.addAll(fr.getUsers());
					nextMaxID = fr.getNext_max_id();

					if (nextMaxID == null || influencerFollowers.size() > 600) {
						break;
					}
				}

				for (InstagramUserSummary follower : influencerFollowers) {
					try {
						InstagramSearchUsernameResult userInfo = null;
						synchronized (instagram) {
							userInfo = instagram.sendRequest(new InstagramGetUserInfoRequest(follower.getPk()));
						}
						InstagramUser user = null;
						if (userInfo != null && (user = userInfo.getUser()) != null){
							if (user.getBiography().contains("travel")){
								synchronized (instagram){
									instagram.sendRequest(new InstagramFollowRequest(follower.getPk()));
									System.out.println("User " + client.getUsername() + " followed " + follower.getUsername()
										+ " through influencer " + influencerName);
								}
							}
						}
						else{
							continue;
						}
						
						instagram.sendRequest(new InstagramFollowRequest(follower.getPk()));
					} catch (ClientProtocolException e) {
						System.out.println("ClientProtocolException caught when analyzing followers of influencer "
								+ influencerName);
					} catch (IOException e) {
						System.out.println("IOException caught when analyzing followers of influencer "
								+ influencerName);
					}

					double randomValue = 57 + (6 * rn.nextDouble());
					try {
						Thread.sleep((long) (randomValue * 1000));
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
			}
		}
	}
}
