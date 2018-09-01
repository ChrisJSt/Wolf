package main.grower;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.ClientProtocolException;
import org.brunocvcunha.instagram4j.Instagram4j;
import org.brunocvcunha.instagram4j.requests.InstagramSearchUsernameRequest;
import org.brunocvcunha.instagram4j.requests.payload.InstagramUser;

import main.actions.SearchHashtags;
import main.actions.Unfollow;
import main.client.Client;
import main.constants.Constants;
import main.util.FollowingMapSerializerDeserializer;

public class Wolfstagram {

	private static Instagram4j adminLogin;

	private static FilenameFilter propFilter = new FilenameFilter() {
		@Override
		public boolean accept(File dir, String name) {
			return name.endsWith(".property");
		}
	};

	private static List<Client> currentClientList = new ArrayList<Client>();

	public static Instagram4j getAdminLogin() {
		return adminLogin;
	}

	/**
	 * Login to instagram with given username and password
	 * 
	 * @param username
	 *            - The username to login with
	 * @param password
	 *            - The password to login with
	 * @return - instagram instance with logged in user
	 */
	public static Instagram4j login(String username, String password) {

		Instagram4j instagram = Instagram4j.builder().username(username).password(password).build();
		instagram.setup();
		try {
			instagram.login();
		} catch (ClientProtocolException e) {
			System.out.println(
					"ClientProtocolException caught for username " + instagram.getUsername() + " when logging in.");
		} catch (IOException e) {
			System.out.println("IOException caught for username " + instagram.getUsername() + " when logging in.");
		}
		return instagram;
	}

	/**
	 * Set property files of clients in directory clientDir"
	 * 
	 * @throws IOException
	 */
	private static void setClientPropertyFiles() throws IOException {
		File clientInfoListDir = new File(Constants.clientDir);
		if (!clientInfoListDir.isDirectory()) {
			System.out.println("Wolfstagram/clients directory not set");
			throw new FileNotFoundException(Constants.clientDir + " not set.");
		}
		Properties prop = new Properties();
		for (File dir : clientInfoListDir.listFiles()) {
			boolean exclude = false;
			for (File pfile : dir.listFiles(propFilter)) {
				prop.clear();
				prop.load(new FileInputStream(pfile));

				if (StringUtils.equalsIgnoreCase(prop.getProperty(Constants.exclude).trim(), Boolean.TRUE.toString())) {
					exclude = true;
					break;
				}

				if (!StringUtils.isBlank(prop.getProperty(Constants.followers))) {
					break;
				}

				InstagramUser user = adminLogin
						.sendRequest((new InstagramSearchUsernameRequest(prop.getProperty(Constants.username))))
						.getUser();
				System.out.println(user.username + " has " + user.follower_count);
				prop.setProperty(Constants.followers, String.valueOf(user.getFollower_count()));
				prop.setProperty(Constants.following, String.valueOf(user.getFollowing_count()));
				prop.store(new FileOutputStream(pfile), dir.getName());
			}
			if (exclude) {
				continue;
			}
			Client newclient = new Client(prop.getProperty(Constants.username), prop.getProperty(Constants.password));
			for (String hashtagCombos : prop.getProperty(Constants.hashtags).split(Constants.newLine)) {
				newclient.hashtags.add(hashtagCombos.split(Constants.comma));
			}
			newclient.setFollowingHashMapFileName();
			FollowingMapSerializerDeserializer.deserialize(newclient,
					new File(newclient.getFollowingHashMapFileName()));
			currentClientList.add(newclient);
		}

	}

	public static void main(String[] args) throws IOException {

		adminLogin = login(Constants.testUserName, Constants.testUserPass);
		setClientPropertyFiles();

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				System.out.println("SHUTDOWN HOOK ACTIVATED. END OF SESSION");
				for (Client client : currentClientList) {
					System.out.println("Like and Follow stats for " + client.getUsername());
					System.out.println("Media Likes: " + client.getLikeCount());
					System.out.println("Media Follows: " + client.getFollowCount());
					FollowingMapSerializerDeserializer.serialize(client,
							new File(client.getFollowingHashMapFileName()));

				}
			}
		});

		/**
		 * UnFollow Users who have not followed back after a week's time
		 */
		for (Client client : currentClientList) {
			// login with username and password
			Instagram4j instagram = login(client.getUsername(), client.getPassword());
			client.setInstagram(instagram);
			try {
				Unfollow.unFollowUsersWhoHaventFollowedBackInXDays(client);
			} catch (InterruptedException e) {
			}
		}

		long startTime = System.currentTimeMillis();
		long elapsedTime = 0L;

		while (elapsedTime < 360 * 60 * 1000) {

			for (Client client : currentClientList) {
				try {
					SearchHashtags.searchAndLike(client);
				} catch (InterruptedException e) {

				}
			}
			elapsedTime = (new Date()).getTime() - startTime;
			System.out.println("Elapsed Time was " + elapsedTime);
		}
		System.out.println("Time elapsed");

	}

}
