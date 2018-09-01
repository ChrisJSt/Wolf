package main.actions;

import java.io.IOException;

import org.brunocvcunha.instagram4j.Instagram4j;
import org.brunocvcunha.instagram4j.requests.InstagramAutoCompleteUserListRequest;
import org.brunocvcunha.instagram4j.requests.payload.StatusResult;

import main.grower.Wolfstagram;

public class Friendships {

	public static boolean thatUserFollowsthisUser(String thisUser, String thatUser) {
		return thatUserFollowsthisUser(Wolfstagram.getAdminLogin(), thisUser, thatUser);
	}

	public static boolean thatUserFollowsthisUser(Instagram4j instagram, String thisUser, String thatUser) {
		try {
			StatusResult result = instagram.sendRequest(new InstagramAutoCompleteUserListRequest());
			result.getFeedback_message();
		} catch (IOException e) {

		}

		return true;
	}

	// InstagramAutoCompleteUserListRequest

}
