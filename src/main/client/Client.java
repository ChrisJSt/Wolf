package main.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.brunocvcunha.instagram4j.Instagram4j;

import main.constants.Constants;

/*
 * Client class used to refer to a client of the InstaGrower application
 */
public class Client {

	/**
	 * PRIVATE fields
	 */
	private final String username;
	private final String password;
	private Instagram4j instagram;
	private Set<Long> followingIdsSinceStartofSession = new HashSet<Long>();
	private Map<Long, Set<Long>> followingIdsFromLastWeek = new HashMap<Long, Set<Long>>();
	private Long id;
	private List<Long> followerIDS = new ArrayList<Long>();
	private String followingFileSer;
	private String cookieLoginSer;
	private String uuIDFileName;
	private String uuID;
	private boolean isLoggedIn = false;
	private boolean continueEngaging = true;
	private boolean continueLiking = true;
	private boolean continueFollowing = true;
	private boolean isCookieLogin = false;
	private int numSpamRequests = 0;
/*	private int spamCounter = 0;
	private int numLikeSpamRequests = 0;
	private int numFollowSpamRequests = 0;
	private int numUnfollowSpamRequests = 0;*/

	/*
	 * time to wait before unfollowing followed users, either in hours or days
	 */
	private int timeToWaitBeforeUnfollowing = 5;

	/*
	 * time unit for above value, either TRUE for days or FALSE for hours
	 */
	public boolean isTimeUnitDays = Boolean.TRUE;

	private String proxyIP;
	private int proxyPORT;
	private boolean proxy = true;

	/**
	 * PUBLIC fields
	 */
	public List<String[]> hashtags = new ArrayList<String[]>();
	public List<String> targetInfluencers = new ArrayList<String>();
	public int numLikesSinceStartofSession = 0;
	public int numFollowsSinceStartofSession = 0;
	public int numberFollowers = 1000;

	/*
	 * Max number of likes a pic can have to follow owner of pic
	 */
	public int maxLikesAcceptableToFollow = 50;
	
	public static class Influencer {
		public String name;
		public String numFollowers;
		public String[] followerBioKeyWords;	
	}

	public Client(String username, String password) {

		this.username = username;
		this.password = password;
		if (username.equalsIgnoreCase(Constants.Chris) || username.equalsIgnoreCase("unnamed_days")
				|| username.equalsIgnoreCase("belma_dub")) {
			this.proxy = false;
		}
		if (username.equalsIgnoreCase(Constants.testUserName)) {
			this.cookieLoginSer = Constants.cookieLoginAdminFileName;
			this.uuIDFileName = Constants.uuIDAdminFileName;
		}
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public Instagram4j getInstagram() {
		return instagram;
	}

	public void setInstagram(Instagram4j instagram) {
		this.instagram = instagram;
		if (instagram != null) {
			this.id = instagram.getUserId();
			this.isLoggedIn = instagram.isLoggedIn();
		}
	}

	public int getLikeCount() {
		return this.numLikesSinceStartofSession;
	}

	public int getFollowCount() {
		return this.numFollowsSinceStartofSession;
	}

	public Long getUserID() {
		return this.id;
	}

	public List<Long> getFollowerIDs() {
		return this.followerIDS;
	}

	public void setFollowerIDs(List<Long> followers) {
		this.followerIDS = followers;
	}

	public void incrementFollowCount() {
		this.numFollowsSinceStartofSession += 1;
	}

	public void incrementLikeCount() {
		this.numLikesSinceStartofSession += 1;
	}

	public void addFollowingIDSinceStartofSession(Long id) {
		followingIdsSinceStartofSession.add(id);
	}

	public Set<Long> getFollowingIDSinceStartofSession() {
		return followingIdsSinceStartofSession;
	}

	public Map<Long, Set<Long>> getFollowingFromLastWeek() {
		return followingIdsFromLastWeek;
	}

	public boolean followsUser(Long id) {
		return followingIdsSinceStartofSession.contains(id);
	}

	public void setFollowingFromLastWeek(HashMap<Long, Set<Long>> map) {
		if (map != null && !map.isEmpty()) {
			this.followingIdsFromLastWeek = map;
		}
	}

	public void setFollowingHashMapFileName() {
		String filename = Constants.clientDir + "/" + this.username + "/" + Constants.followingFileSer;
		this.followingFileSer = filename;
	}

	public String getFollowingHashMapFileName() {
		return this.followingFileSer;
	}

	public void setCookieLoginFileName() {
		String filename = Constants.clientDir + "/" + this.username + "/" + Constants.cookieFileSer;
		this.cookieLoginSer = filename;
	}

	public String getCookieLoginFileName() {
		return this.cookieLoginSer;
	}

	public void setUUIDFileName() {
		String filename = Constants.clientDir + "/" + this.username + "/" + Constants.uuIDFileSer;
		this.uuIDFileName = filename;
	}

	public String getUUIDFileName() {
		return this.uuIDFileName;
	}

	public boolean isLoggedIn() {
		return this.isLoggedIn;
	}

	public int getTimeToWaitBeforeUnfollowing() {
		return this.timeToWaitBeforeUnfollowing;
	}

	public void setTimeToWaitBeforeUnfollowing(String time) {
		time = time.trim();
		if (!StringUtils.isBlank(time)) {
			try {
				int timeInt = Integer.valueOf(time);
				this.timeToWaitBeforeUnfollowing = timeInt;
			} catch (NumberFormatException e) {
			}
		}
	}

	public boolean isProxy() {
		return this.proxy;
	}

	public String getProxyIP() {
		return this.proxyIP;
	}

	public int getProxyPort() {
		return this.proxyPORT;
	}

	public void setProxyIP(String IP) {
		this.proxyIP = IP;
	}

	public void setProxyPort(int port) {
		this.proxyPORT = port;
	}

	public boolean continueEngaging() {
		return this.continueEngaging;
	}

	public void setContinueEngaging(boolean continueEngaging) {
		this.continueEngaging = continueEngaging;
	}
	
	public boolean continueLiking() {
		return this.continueLiking;
	}

	public void setContinueLiking(boolean continueLiking) {
		this.continueLiking = continueLiking;
	}
	
	public boolean continueFollowing() {
		return this.continueFollowing;
	}

	public void setContinueFollowing(boolean continueFollowing) {
		this.continueFollowing = continueFollowing;
	}

	public boolean isCookieLogin() {
		return this.isCookieLogin;
	}

	public void setCookieLogin(boolean isCookieLogin) {
		this.isCookieLogin = isCookieLogin;
	}

	public void setUUID(String uuID) {
		this.uuID = uuID;
	}

	public String getUUID() {
		return this.uuID;
	}
	
	public boolean incrementSpamCount(){
		if (++this.numSpamRequests > 3){
			return true;
		}
		return false;
		
	}
	
	public void setNumberOfFollowers(int numberFollowers) {
		this.numberFollowers = numberFollowers;
	}

}
