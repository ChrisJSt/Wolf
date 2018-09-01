package main.actions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.brunocvcunha.instagram4j.Instagram4j;
import org.brunocvcunha.instagram4j.requests.InstagramTagFeedRequest;
import org.brunocvcunha.instagram4j.requests.payload.InstagramFeedItem;
import org.brunocvcunha.instagram4j.requests.payload.InstagramFeedResult;

import main.client.Client;
import main.constants.Constants;

public class SearchHashtags {

	public static void searchAndLike(Client client) throws InterruptedException {
		Instagram4j insta = client.getInstagram();
		if (insta == null) {
			System.out.println("Instagram null for user " + client.getUsername());
		} else {
			InstagramFeedResult tagFeed = null;
			Set<InstagramFeedItem> filteredItems = new HashSet<InstagramFeedItem>();
			for (String[] hashtagCombos : client.hashtags) {
				int ind = 0;
				int numTags = hashtagCombos.length;
				for (String hashtag : hashtagCombos) {
					hashtag = hashtag.trim();
					try {
						Thread.sleep(3000);
						synchronized (insta) {
							tagFeed = insta.sendRequest(new InstagramTagFeedRequest(hashtag));
						}
						if (numTags >= 2) {
							filteredItems.addAll(multiHashtagFilter(tagFeed, hashtagCombos[(ind + 1) % numTags]));
						} else {
							filteredItems.addAll(tagFeed.getItems());
						}
					} catch (IOException e) {
						System.out.println(
								"Exception caught for hashtag: " + hashtag + " for user " + client.getUsername());

					} 
					ind = ind + 1;
					if (ind == numTags) {
						break;
					}

				}
				if (Thread.currentThread().isInterrupted()){
					throw (new InterruptedException());
				}
			}
			try {
				List<InstagramFeedItem> shuffledFilterItemList = new ArrayList<InstagramFeedItem>(filteredItems);
				Collections.shuffle(shuffledFilterItemList);
				Likes.likeFeed(client, shuffledFilterItemList);
			} catch (InterruptedException e) {
				System.out.println("Thread interrupted while liking feed in SearchHashtags");
				throw (new InterruptedException());

			}
			System.out.println("Like Count since start of session for user " + client.getUsername() + " : "
					+ client.numLikesSinceStartofSession);
			System.out.println("Follow Count since start of session for user " + client.getUsername() + " : "
					+ client.numFollowsSinceStartofSession);
		}
	}

	private static List<InstagramFeedItem> multiHashtagFilter(InstagramFeedResult tagFeed, String hashtag) {
		List<InstagramFeedItem> filteredItems = new ArrayList<InstagramFeedItem>();
		hashtag = hashtag.trim();
		for (InstagramFeedItem item : tagFeed.getItems()) {
			if (item.getCaption() != null) {
				String caption = (String) item.getCaption().get(Constants.captionText);
				if (caption != null && caption.contains(hashtag)) {
					filteredItems.add(item);
				}
			}
		}
		return filteredItems;
	}
}
