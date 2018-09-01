package main.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CookieStore;
import org.apache.http.conn.params.ConnRoutePNames;
import org.brunocvcunha.instagram4j.Instagram4j;
import org.brunocvcunha.instagram4j.requests.InstagramSearchUsernameRequest;
import org.brunocvcunha.instagram4j.requests.payload.InstagramLoginResult;
import org.brunocvcunha.instagram4j.requests.payload.InstagramUser;

import main.client.Client;
import main.constants.Constants;

@SuppressWarnings("deprecation")
public class Login {

	private static Instagram4j adminLogin;

	private static FilenameFilter propFilter = new FilenameFilter() {
		@Override
		public boolean accept(File dir, String name) {
			return name.endsWith(".property");
		}
	};

	private static FilenameFilter cookieFilter = new FilenameFilter() {
		@Override
		public boolean accept(File dir, String name) {
			return name.endsWith("cookie.ser");
		}
	};

	/**
	 * Set the administrator
	 */
	public static void setAdmin(Instagram4j login) {
		adminLogin = login;
	}

	public static Instagram4j getAdmin() {
		return adminLogin;
	}

	/**
	 * 
	 * @param client
	 * @return - boolean successfulLogin - true if login successful
	 */
	public static boolean login(Client client, boolean isCookieLogin) {

		Instagram4j instagram = null;
		if (isCookieLogin) {
			instagram = loadCookieLogin(client);
		} else {
			instagram = Instagram4j.builder().username(client.getUsername()).password(client.getPassword()).build();
		}

		instagram.setup();
		boolean successfulLogin = true;
		if (client.isProxy()) {
/*			if (client.getUsername().equalsIgnoreCase("madeinitalytraveler") ||
			   client.getUsername().equalsIgnoreCase("thetravexplorer")){
				setupProxyServer(instagram, client, "chris1994", "testpassword");
			}
			else {
				setupProxyServer(instagram, client);
			}*/
			setupProxyServer(instagram, client);
		}
		if (!isCookieLogin) {
			InstagramLoginResult result = null;
				try{
					result = instagram.login();
				} catch (Exception e){
					System.out.println("Exception caught when logging in " + e.getMessage());
				}
				
			if (result != null && result.getStatus().equalsIgnoreCase("ok")) {
				CookieLoginSerializerDeserializer.serialize(instagram, new File(client.getCookieLoginFileName()),
						new File(client.getUUIDFileName()));
			} else {
				return false;
			}
		}
		client.setInstagram(instagram);
		return successfulLogin;
	}

	/**
	 * Reuse the login session stored in the user's cookie file
	 * 
	 * @return
	 */
	private static Instagram4j loadCookieLogin(Client client) {
		File cookieFile = new File(client.getCookieLoginFileName());
		File UUIDFile = new File(client.getUUIDFileName());
		Long userID = null;

		ObjectInputStream cis = null;
		ObjectInputStream uis = null;
		CookieStore cookieStore = null;
		String uuID = null;

		try {
			cis = new ObjectInputStream(new FileInputStream(cookieFile));
			uis = new ObjectInputStream(new FileInputStream(UUIDFile));
			cookieStore = (CookieStore) cis.readObject();
			try {
				userID = Long.parseLong(cookieStore.getCookies().get(2).getValue());
			}
			catch (NumberFormatException e) {
				System.out.println("NumberFormatExceptionCaught for user " + client.getUsername());
				userID = Long.parseLong(cookieStore.getCookies().get(3).getValue());
			}
			uuID = (String) uis.readObject();
		} catch (FileNotFoundException e) {
			System.out.println("Caught FileNotFoundException " + e.getMessage());

		} catch (IOException e) {
			System.out.println("Caught IOexception " + e.getMessage());

		} catch (ClassNotFoundException e) {
			System.out.println("Caught ClassNotFoundException " + e.getMessage());

		} finally {
			try {
				if (cis != null)
					cis.close();
				if (uis != null)
					uis.close();
			} catch (IOException e) {

			}
		}
		if (cookieStore == null || uuID == null) {
			return null;
		}

		Instagram4j instagramCookieLogin = Instagram4j.builder().username(client.getUsername())
				.password(client.getPassword()).uuid(uuID).cookieStore(cookieStore).build();
		instagramCookieLogin.setUserId(userID);
		return instagramCookieLogin;

	}

	private static void setupProxyServer(Instagram4j insta, Client client) {
		if (insta == null) {
			System.out.println("Instagram for client " + client.getUsername() + " was null");
		} else {
			HttpHost proxy = new HttpHost(client.getProxyIP(), client.getProxyPort(), Constants.HTTP);
			insta.getClient().getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
			insta.getClient().getParams().setIntParameter("http.connection.timeout", 600000);
			insta.getClient().getCredentialsProvider()
					.setCredentials(new AuthScope(AuthScope.ANY_HOST,
							AuthScope.ANY_PORT),
					new UsernamePasswordCredentials(Constants.proxyUsernameCredential, Constants.proxyPassCredential));
		}
	}	
	

	private static void setupProxyServer(Instagram4j insta, Client client, String IPUsername, String IPPass) {
		if (insta == null) {
			System.out.println("Instagram for client " + client.getUsername() + " was null");
		} else {
			HttpHost proxy = new HttpHost(client.getProxyIP(), client.getProxyPort(), Constants.HTTP);
			insta.getClient().getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
			insta.getClient().getParams().setIntParameter("http.connection.timeout", 600000);
			insta.getClient().getCredentialsProvider()
					.setCredentials(new AuthScope(AuthScope.ANY_HOST,
							AuthScope.ANY_PORT),
					new UsernamePasswordCredentials(IPUsername, IPPass));
		}
	}

	/**
	 * Instatiate clients and their respective property files in dir
	 * ../clientDir/
	 * 
	 * @return
	 * @throws IOException
	 */
	public static List<Client> loadClients() throws IOException {

		List<Client> currentClientList = new ArrayList<Client>();
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
			if (newclient.isProxy()) {
				newclient.setProxyIP(prop.getProperty(Constants.proxyIP));
				newclient.setProxyPort(Integer.parseInt(prop.getProperty(Constants.proxyPort)));
			}

			File[] cookieFiles = dir.listFiles(cookieFilter);
			if (cookieFiles != null && cookieFiles.length >= 1) {
				File cookieFile = cookieFiles[0];
				if (cookieFile.exists() || cookieFile.length() != 0) {
					newclient.setCookieLogin(Boolean.TRUE);
				}
			}

			String timeToWait = prop.getProperty(Constants.numOfDaysBeforeUnfollowing);
			if (StringUtils.isBlank(timeToWait)) {
				timeToWait = prop.getProperty(Constants.numOfHoursBeforeUnfollowing);
				newclient.isTimeUnitDays = Boolean.FALSE;
			}
			String continueEngaging = prop.getProperty(Constants.continueEngaging);
			if (!StringUtils.isBlank(continueEngaging)) {
				newclient.setContinueEngaging(Boolean.parseBoolean(continueEngaging));
			}
			
			String continueLiking = prop.getProperty(Constants.continueLiking);
			if (!StringUtils.isBlank(continueLiking)) {
				newclient.setContinueLiking(Boolean.parseBoolean(continueLiking));
			}
			
			String continueFollowing = prop.getProperty(Constants.continueFollowing);
			if (!StringUtils.isBlank(continueFollowing)) {
				newclient.setContinueFollowing(Boolean.parseBoolean(continueFollowing));
			}
			
			try{
				int followers = Integer.parseInt(prop.getProperty(Constants.followers));
				newclient.setNumberOfFollowers(followers);
			}
			catch (NumberFormatException e) {
				
			}
			prop.getProperty(Constants.influencers);
			newclient.setTimeToWaitBeforeUnfollowing(timeToWait);
			newclient.setFollowingHashMapFileName();
			File followingFile = new File(newclient.getFollowingHashMapFileName());
			if (followingFile.exists() && followingFile.length() > 0) {
				if (FollowingMapSerializerDeserializer.deserialize(newclient, followingFile) == null) {
					System.out.println("Error deserializing non empty following file for client "
							+ newclient.getUsername() + " . Skipping client.");
					continue;
				}
				;
			}
			newclient.setCookieLoginFileName();
			newclient.setUUIDFileName();
			currentClientList.add(newclient);
		}
		return currentClientList;
	}
}
