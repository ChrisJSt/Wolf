package main.actions;

import java.io.IOException;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.brunocvcunha.instagram4j.Instagram4j;
import org.brunocvcunha.instagram4j.requests.InstagramGetUserFollowersRequest;
import org.brunocvcunha.instagram4j.requests.InstagramGetUserInfoRequest;
import org.brunocvcunha.instagram4j.requests.InstagramLikeRequest;
import org.brunocvcunha.instagram4j.requests.InstagramSearchUsernameRequest;
import org.brunocvcunha.instagram4j.requests.InstagramUserFeedRequest;
import org.brunocvcunha.instagram4j.requests.payload.InstagramFeedItem;
import org.brunocvcunha.instagram4j.requests.payload.InstagramFeedResult;
import org.brunocvcunha.instagram4j.requests.payload.InstagramGetUserFollowersResult;
import org.brunocvcunha.instagram4j.requests.payload.InstagramLikeResult;
import org.brunocvcunha.instagram4j.requests.payload.InstagramSearchUsernameResult;
import org.brunocvcunha.instagram4j.requests.payload.InstagramUser;
import org.brunocvcunha.instagram4j.requests.payload.InstagramUserSummary;

import main.client.Client;

public class TargetInfluencers {

	public static void targetInfluencerFollowing(Client client, String[] influencerNames) throws InterruptedException {

		Instagram4j instagram = client.getInstagram();
		InstagramSearchUsernameResult userResult = null;

		for (String influencerName : influencerNames) {
			try {
				synchronized (instagram) {
					userResult = instagram.sendRequest(new InstagramSearchUsernameRequest(influencerName));
				}
			} catch (IOException e) {

				System.out.println("IOException caught when searching username " + influencerName);
			}
			InstagramUser influencer = null;
			InstagramGetUserFollowersResult fr = null;
			if (userResult == null || (influencer = userResult.getUser()) == null) {
				System.out.println("Influencer " + influencerName + " was null");
				continue;
			}
			try {
				synchronized (instagram) {
					fr = instagram.sendRequest(new InstagramGetUserFollowersRequest(influencer.getPk()));
				}
			} catch (ClientProtocolException e) {

			} catch (IOException e) {

			}
			List<InstagramUserSummary> influencerFollowers = null;
			if (fr == null || (influencerFollowers = fr.getUsers()) == null) {
				System.out.println("Followers for Influencer " + influencerName + " was null");
				continue;
			}

			for (InstagramUserSummary user : influencerFollowers) {
				InstagramFeedResult userFeed = null;
				InstagramFeedItem feedItem = null;
				InstagramLikeResult likeResult = null;
				try {
					synchronized (instagram) {
						userFeed = instagram.sendRequest(new InstagramUserFeedRequest(user.getPk()));
					}
					if (userFeed != null && userFeed.getNum_results() >= 1){
						feedItem = userFeed.getItems().get(0);
						Thread.sleep(10000L);
						synchronized (instagram) {
							likeResult = instagram.sendRequest(new InstagramLikeRequest(feedItem.getPk()));
						}
						Likes.checkIsSpam(likeResult);
					}
					
					//instagram.sendRequest(new InstagramGetUserInfoRequest(user.getPk()));
				} catch (ClientProtocolException e) {
					
				} catch (IOException e) {
				}

			}
		}
	}
}
