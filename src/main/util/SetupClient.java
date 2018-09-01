package main.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;

import main.client.Client;
import main.constants.Constants;

public class SetupClient {
	
	private static Properties proxyProperties;
	private static FileOutputStream out;

	static {
		proxyProperties = new Properties();
		File file = new File("C:/Users/Administrator/Projects/Wolfstagram/proxies.properties");
		try {
			proxyProperties.load(new FileInputStream(file));
			out = new FileOutputStream(file);
		} catch (IOException e) {
			proxyProperties = null;
		}
	}
	
	/**
	 * date, fname, email, phone-num, 
	 * username, password, 
	 * influencer-1, influencer-2, influencer-3, 
	 * hashtag-1, hashtag-2, hashtag-3														
	 * @throws IOException 
	 */
	
	public static void createClient(List<String> row) throws IOException{
		
		String travel = "travel";
		String destination = "destination";
		/*
		String date = row.get(0);
		String name = row.get(1);
		String email = row.get(2);
		String phone = row.get(3);
		*/
		String username = row.get(4).trim();
		String password = row.get(5).trim();	
		int i;
		for (i=6; i<12; i++){
			String replacement = row.get(i);
			replacement = replacement.replace("@", "");
			replacement = replacement.replace("#", "");
			replacement = replacement.trim();
			row.set(i, replacement);		
		}
		String influencer1 = row.get(6).trim();
		String influencer2 = row.get(7).trim();
		String influencer3 = row.get(8).trim();
		String hashtag1 = row.get(9).toLowerCase().trim();
		String hashtag2 = row.get(10).toLowerCase().trim();
		String hashtag3 = row.get(11).toLowerCase().trim();
		List<String> hashtagsLowerCase = new ArrayList<String>() {{
			add(hashtag1);
			add(hashtag2);
			add(hashtag3); }};
		
		Map<String,String> clientInfoMap = new HashMap<String,String>();
		clientInfoMap.put(Constants.username, username);
		clientInfoMap.put(Constants.password, password);
		String commaSpace = ", ";
		String spaceNewline = " \n ";
		StringBuilder hashtags = new StringBuilder();
		hashtags.append(hashtag1).append(commaSpace).append(hashtag2).append(spaceNewline)
		.append(hashtag2).append(commaSpace).append(hashtag3).append(spaceNewline)
		.append(hashtag1).append(commaSpace).append(hashtag3);
		
		if (!hashtagsLowerCase.contains(travel) || !hashtagsLowerCase.contains(destination)){
			hashtags.append(spaceNewline)
			.append(travel).append(commaSpace).append(destination);
		}

		clientInfoMap.put(Constants.hashtags, hashtags.toString());
		clientInfoMap.put(Constants.exclude, "");
		clientInfoMap.put(Constants.numOfDaysBeforeUnfollowing, "");
		clientInfoMap.put(Constants.numOfHoursBeforeUnfollowing, "12");
		clientInfoMap.put(Constants.continueEngaging, "");
		clientInfoMap.put(Constants.proxyPort, "65233");
		
		/**
		 * PROXY ALLOCATION
		 */
		String allocatedProxy;
		allocatedProxy = allocateProxy(username);
		
		if (StringUtils.isNotBlank(allocatedProxy)){
			clientInfoMap.put(Constants.proxyIP, allocatedProxy);
		}
		else {
			return;
		}
		
		createClientDirectory(username, clientInfoMap);	
		
		Client client = new Client(username, password);
		client.setProxyIP(allocatedProxy);
		client.setProxyPort(65233);
		try{
			Login.login(client, false);
		}catch (Exception n){
			
		}
	
	}

	private static void createClientDirectory(String username, Map<String, String> clientInfoMap) throws IOException {
		File clientDir = new File(Constants.clientDir + "/" + username);
		if (!clientDir.exists()) {
			boolean created = clientDir.mkdir();
			if (!created)
				return;
			createClientInfoPropertyFile(clientDir, clientInfoMap);
			createEngagementGroupFile(clientDir);
				
		}
		
	}

	private static void createClientInfoPropertyFile(File clientDir, Map<String, String> clientInfoMap) throws IOException {
		File clientInfoProperty = new File(clientDir.getAbsolutePath() +  "/clientInfo.property");
		if (!clientInfoProperty.exists()){
			clientInfoProperty.createNewFile();
		}
		Properties properties = new Properties();
		properties.putAll(clientInfoMap);
		try {
			properties.store(new FileOutputStream(clientInfoProperty), null);
		} catch (FileNotFoundException e) {

		} catch (IOException e) {

		}
		
	}

	private static boolean createEngagementGroupFile(File clientDir) {
		File engagementGroup = new File(clientDir.getAbsolutePath() +  "/engagementGroup");
		boolean created = false;
		try {
			created = engagementGroup.createNewFile();
		} catch (IOException e) {
			
		}
		return created;
		
	}
	
	private static String allocateProxy(String username) throws IOException {
		
		String proxyToUse = "";
		if (proxyProperties != null && !proxyProperties.containsValue(username)){
			for (Entry<Object, Object> proxy: proxyProperties.entrySet()) {
				if (StringUtils.isBlank((String)(proxy.getValue()))){
					proxyToUse = (String)proxy.getKey();
					proxyProperties.put(proxyToUse, username);
					break;
				}
			}
		}

		return proxyToUse;
	}
	
	public static void store() {
		try {
			proxyProperties.store(out, null);
		} catch (IOException e) {
		}
	}
	
	public static boolean validateLogin(String[] loginInfo) {
		if (loginInfo.length != 2) {
			return false;
		}
		String username = loginInfo[0];
		String password = loginInfo[1];
		Properties properties = new Properties();
		
		File clientPropFile = new File(Constants.clientDir + "/" + username +  "/clientInfo.property");
		String proxyIP = null;
		if (clientPropFile.exists()){
			try {
				properties.load(new FileInputStream(clientPropFile));
			} catch (Exception e) {
				return false;
			}
			proxyIP = properties.getProperty(Constants.proxyIP);
			properties.setProperty(Constants.username, username);
			properties.setProperty(Constants.password, password);
			try {
				properties.store(new FileOutputStream(clientPropFile), null);
			} catch (IOException e) {

			}
		}
		else {	
			
			Map<String,String> clientInfoMap = new HashMap<String,String>();
			clientInfoMap.put(Constants.username, username);
			clientInfoMap.put(Constants.password, password);
			clientInfoMap.put(Constants.hashtags, "travel, destination");
			clientInfoMap.put(Constants.exclude, "");
			clientInfoMap.put(Constants.continueFollowing, "");
			clientInfoMap.put(Constants.continueLiking, "");
			clientInfoMap.put(Constants.numOfDaysBeforeUnfollowing, "");
			clientInfoMap.put(Constants.numOfHoursBeforeUnfollowing, "12");
			clientInfoMap.put(Constants.continueEngaging, "");
			clientInfoMap.put(Constants.proxyPort, "65233");
			
			try {
				/**
				 * PROXY ALLOCATION
				 */
				proxyIP = allocateProxy(username);
				if (StringUtils.isNotBlank(proxyIP)){
					clientInfoMap.put(Constants.proxyIP, proxyIP);
				}
				else {
					return false;
				}
				createClientDirectory(username, clientInfoMap);
			}
			catch (IOException e) {
				return false;
			}			
		}
		Client client = new Client(username, password);
		client.setProxyIP(proxyIP);
		client.setProxyPort(65233);
		try{
			Login.login(client, false);
		}catch (Exception n){
			
		}
		return true;
		
	}
}
