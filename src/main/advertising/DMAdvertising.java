package main.advertising;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.brunocvcunha.instagram4j.Instagram4j;
import org.brunocvcunha.instagram4j.requests.InstagramDirectShareRequest;
import org.brunocvcunha.instagram4j.requests.InstagramDirectShareRequest.ShareType;
import org.brunocvcunha.instagram4j.requests.InstagramFollowRequest;
import org.brunocvcunha.instagram4j.requests.InstagramLikeRequest;
import org.brunocvcunha.instagram4j.requests.InstagramSearchUsernameRequest;
import org.brunocvcunha.instagram4j.requests.InstagramUserFeedRequest;
import org.brunocvcunha.instagram4j.requests.payload.InstagramFeedItem;
import org.brunocvcunha.instagram4j.requests.payload.InstagramFeedResult;
import org.brunocvcunha.instagram4j.requests.payload.InstagramLikeResult;
import org.brunocvcunha.instagram4j.requests.payload.InstagramSearchUsernameResult;
import org.brunocvcunha.instagram4j.requests.payload.StatusResult;

import main.client.Client;
import main.constants.Constants;
import main.util.Login;

public class DMAdvertising {
	
	public static Random rn = new Random();

	public static void main(String[] args) throws Exception {

		List<List<?>> prospects = null;
		try {
			prospects = DMAdvertisingDoc.getUnmessagedProspectsFromGoogleSheet();
		} catch (Exception e1) {
			System.out.println("Exception Caught when getting prospect list from Google Sheet");
			System.exit(1);
		}
		if (prospects == null || prospects.isEmpty()) {
			return;
		}
        
		Client advertiser = null;
		
		if (args.length == 1 && args[0].equalsIgnoreCase("Eri")){
			advertiser = new Client(Constants.Eroslav, "Balkan94");
			advertiser.setProxyIP("191.96.50.42");
			advertiser.setProxyPort(65233);
		}
		else if (args.length == 1 && args[0].equalsIgnoreCase("Chris")) {
			advertiser = new Client(Constants.Chris, "dummy");
		}
		else {
			System.out.println("Usage: args = Eri|Chris");
			System.exit(0);
		}
		System.out.println("Setting up advertiser: " + advertiser.getUsername());
		advertiser.setCookieLoginFileName();
		advertiser.setUUIDFileName();
		Login.login(advertiser, Boolean.TRUE);
		Thread.sleep(10000);
		Instagram4j IG = advertiser.getInstagram();
		
		for (List<?> prospect: prospects) {
			String username = (String) prospect.get(0);
			String name = "";
			if (prospect.get(1) != null) {
				name = (String) prospect.get(1);
			}
/*			String followers = (String) prospect.get(2);
			String sent = (String) prospect.get(3);
			String combo = (String) prospect.get(4);*/
			
			InstagramSearchUsernameResult user = IG.sendRequest(new InstagramSearchUsernameRequest(username));
			Thread.sleep(12000);
			if (user == null || user.isSpam() || !user.getStatus().equalsIgnoreCase("OK") || user.getUser().is_private()) {
				DMAdvertisingDoc.markSent(false);
				System.exit(0);
			}
			Long userID = user.getUser().getPk();
			IG.sendRequest(new InstagramFollowRequest(userID));
			Thread.sleep(6000);
			InstagramFeedResult result = IG.sendRequest(new InstagramUserFeedRequest(userID));
			if (result == null || result.isSpam() || !result.getStatus().equalsIgnoreCase("OK")) {
				DMAdvertisingDoc.markSent(false);
				System.exit(0);
			}
			Thread.sleep(6000);
			InstagramFeedItem item = result.getItems().get(0);
			String mediaId = item.getId();
			Long id = item.getPk();
			InstagramLikeResult likeResult = IG.sendRequest(new InstagramLikeRequest(id));
			Thread.sleep(10000);
			List<String> recipients = new ArrayList<String>();
			recipients.add(String.valueOf(userID));
			String message = createMessage();
			message = message.replaceAll("<NAME>", name).trim();
			StatusResult r = null;
			if (message.contains("<EMBED>")){
				System.out.println("Embedding user photo within DM");
				message = message.replace("<EMBED>", "");
				r = IG.sendRequest(
						InstagramDirectShareRequest.builder(ShareType.MEDIA, recipients).mediaId(mediaId).message(message).build());
				
			}
			else {
				r = IG.sendRequest(
						InstagramDirectShareRequest.builder(ShareType.MESSAGE, recipients).message(message).build());
			}
			
			System.out.println(r.getStatus());
			DMAdvertisingDoc.setUserIDofMostRecentRecipient(userID);
			if (!r.getStatus().equalsIgnoreCase("OK")) {
				DMAdvertisingDoc.markSent(false);
				System.out.println("Error sending DM to user: " + user.getUser().getUsername());
			}else {
				DMAdvertisingDoc.markSent(true);
				System.out.println("Successfully sent DM to user: " + user.getUser().getUsername());
			}
			break;
			
		}

	}

	private static String createMessage() {
		File introDir= new File(Constants.DMAdvertising.INTRO);
		File valueDir = new File(Constants.DMAdvertising.VALUE);
		File qualifierDir = new File(Constants.DMAdvertising.QUALIFIER);
		File closeDir = new File(Constants.DMAdvertising.CLOSE);
		String[] filePaths = new String[4];
		
		int introScriptNo = rn.nextInt(introDir.list().length);
		filePaths[0] = introDir.listFiles()[introScriptNo].getAbsolutePath();
		introScriptNo += 1;
		String sIntro = String.valueOf(introScriptNo);
		
		int valueScriptNo = rn.nextInt(valueDir.list().length);
		filePaths[1] = valueDir.listFiles()[valueScriptNo].getAbsolutePath();
		valueScriptNo += 1;
		String sValue = String.valueOf(valueScriptNo);
		
		int qualifierScriptNo = rn.nextInt(qualifierDir.list().length);
		filePaths[2] = qualifierDir.listFiles()[qualifierScriptNo].getAbsolutePath();
		qualifierScriptNo += 1;
		String sQualifier = String.valueOf(qualifierScriptNo);
		
		int closeScriptNo = rn.nextInt(closeDir.list().length);
		filePaths[3] = closeDir.listFiles()[closeScriptNo].getAbsolutePath();
		closeScriptNo += 1;
		String sClose = String.valueOf(closeScriptNo);
			
		byte[] encoded;
		String msg = "";
		for (int i=0; i <4; i++) {
		try {
			encoded = Files.readAllBytes(Paths.get(filePaths[i]));
			msg = msg + (new String(encoded, StandardCharsets.UTF_8));
		} catch (IOException e) {
			
		}
		}
		DMAdvertisingDoc.setMessage(msg);
		DMAdvertisingDoc.setCombo(sIntro+"/"+sValue+"/"+sQualifier+"/"+sClose);
		return msg;
	}

}
