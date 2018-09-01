package main.actions;

import java.io.IOException;
import java.util.List;
import java.util.Random;

import org.apache.http.client.ClientProtocolException;
import org.brunocvcunha.instagram4j.Instagram4j;
import org.brunocvcunha.instagram4j.requests.InstagramFollowRequest;
import org.brunocvcunha.instagram4j.requests.InstagramLikeRequest;
import org.brunocvcunha.instagram4j.requests.InstagramUserFeedRequest;
import org.brunocvcunha.instagram4j.requests.payload.InstagramFeedItem;
import org.brunocvcunha.instagram4j.requests.payload.InstagramFeedResult;
import org.brunocvcunha.instagram4j.requests.payload.InstagramLikeResult;
import org.brunocvcunha.instagram4j.requests.payload.StatusResult;

import main.client.Client;
import main.constants.Constants;

public class Likes {

	private static Random rn = new Random();

	public static void likeFeed(Client client, List<InstagramFeedItem> filteredItems) throws InterruptedException{
		Instagram4j instagram = client.getInstagram();
		for (InstagramFeedItem item : filteredItems) {
			if (Thread.currentThread().isInterrupted()){
				System.out.println("Thread interrupted while liking feed");
				throw new InterruptedException();
			}
			if (client.followsUser(item.getUser().getPk())) {
				continue;
			}
			double randomValue = 24 + (6 * rn.nextDouble());
	        Thread.sleep((long) (randomValue * 1000));
	        
			try {
				 if (client.continueLiking()){
						InstagramLikeResult result = null;
						synchronized (instagram) {
							result = instagram.sendRequest(new InstagramLikeRequest(item.getPk()));
						}
						if (checkIsSpam(result)){
							System.out.println("User " + instagram.getUsername() + " spammed while liking"
									+ " item " + item.id);
							if (client.incrementSpamCount()){
								System.out.println("Thread interrupted due to like spam");
								throw new InterruptedException();
							}
							
						} 
						else{
							System.out.println("User " + instagram.getUsername() + " liked " + item.id);
							client.incrementLikeCount();
						}
						if (!client.continueFollowing()) {
							Thread.sleep((long) (randomValue * 1000));
						}
						
				 }
				 
				 if (client.continueFollowing()) {
					 
						if (item.like_count < client.maxLikesAcceptableToFollow) {
							Thread.sleep((long) (randomValue * 1000));
							StatusResult followresult = null;
							synchronized (instagram) {
								followresult = instagram.sendRequest(new InstagramFollowRequest(item.user.getPk()));
							}
							
							if (checkIsSpam(followresult)){
								System.out.println("User " + instagram.getUsername() + " spammed while following "
										+  item.user.username);
								if (client.incrementSpamCount()){
									System.out.println("Thread interrupted due to follow spam");
									throw new InterruptedException();
								}
								
							}
							else{
								System.out.println("User " + instagram.getUsername() + " followed " + item.user.username
										+ " w/ ID " + item.user.getPk());
								client.addFollowingIDSinceStartofSession(item.getUser().getPk());
								client.incrementFollowCount();
							}
						    if (client.continueLiking()){
						    	Thread.sleep(1400L);
							    client.numLikesSinceStartofSession += likeTwoRandomPhotosOnUserTimeLine(client, item);
						    }
						    else {
								Thread.sleep((long) (randomValue*(2.5) * 1000));
						    }
					   }
				 }
					 			 

			} catch (ClientProtocolException e) {

			} catch (IOException e) {

			}
		}
	}

	/**
	 * Like 2 random pictures from this user's timeline to abide by
	 * Follow+Like+Like+Like method
	 * 
	 * @param instagram
	 * @param item
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws InterruptedException
	 */
	private static int likeTwoRandomPhotosOnUserTimeLine(Client client, InstagramFeedItem item) throws InterruptedException{
		InstagramFeedResult userFeed = null;
		Instagram4j instagram = client.getInstagram();
		try {
			synchronized (instagram) {
				userFeed = instagram.sendRequest(new InstagramUserFeedRequest(item.getUser().getPk()));
			}
		} catch (IOException e) {
		}
		int previous = 0, randomIndex = 0, numItemsLiked = 0;
		if (userFeed == null) {
			return 0;
		}
		int numItems = userFeed.getNum_results();
		while (numItems >= 3 && numItemsLiked < 2) {
			while (randomIndex == previous) {
				randomIndex = 1 + (rn.nextInt(numItems - 1));
			}
			previous = randomIndex;
			try {
				Thread.sleep((long) ((16 + (6 * rn.nextDouble())) * 1000));
				InstagramLikeResult result = null;
				synchronized (instagram) {
					result = instagram.sendRequest(new InstagramLikeRequest(userFeed.getItems().get(randomIndex).getPk()));
				}
				if (checkIsSpam(result)){
					System.out.println("User " + instagram.getUsername() + " spammed while liking random"
							+ " item " + item.id);
					if (client.incrementSpamCount()){
						System.out.println("Thread interrupted due to like spam");
						throw new InterruptedException();
					}
					
				}
				else{
					System.out.println(
							"User " + instagram.getUsername() + " liked " + userFeed.getItems().get(randomIndex).id);
				}
				
			} catch (IOException e) {
				
			} 
			numItemsLiked = numItemsLiked + 1;

		}
		return numItemsLiked;

	}
	

	static boolean checkIsSpam(StatusResult result) {
		return (result == null || result.isSpam()
				|| !result.getStatus().equalsIgnoreCase(Constants.OK));
	}

}
