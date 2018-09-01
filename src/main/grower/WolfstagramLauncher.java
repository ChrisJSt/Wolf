package main.grower;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import main.client.Client;
import main.constants.Constants;
import main.util.FollowingMapSerializerDeserializer;
import main.util.Login;

public class WolfstagramLauncher {
	
	private static Properties wolfProperties = new Properties();

	private static List<Client> currentClientList = new ArrayList<Client>();
	
	public static void main(String[] args) throws IOException, InterruptedException {
		
		Client adminClient = new Client(Constants.testUserName, Constants.testUserPass);
		adminClient.setProxyIP("216.21.9.81");
		adminClient.setProxyPort(65233);
		if (!Login.login(adminClient, Boolean.TRUE)) {
			System.out.println("Admin Login unsuccessful. Shutting down..");
			System.exit(1);
		}
		
		wolfProperties.clear();
		wolfProperties.load(new FileInputStream(new File(Constants.wolfProperties)));
				
		Login.setAdmin(adminClient.getInstagram());
		currentClientList.addAll(Login.loadClients());

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				System.out.println("SHUTDOWN HOOK ACTIVATED. END OF SESSION");
				for (Client client : currentClientList) {
					System.out.println("-------------------------------------------------");
					System.out.println("Like and Follow stats for " + client.getUsername());
					System.out.println("Media Likes: " + client.getLikeCount());
					System.out.println("Media Follows: " + client.getFollowCount());
					FollowingMapSerializerDeserializer.serialize(client,
							new File(client.getFollowingHashMapFileName()));

				}
			}
		});

		List<Thread> clientThreads = new ArrayList<Thread>();
		for (Client user : currentClientList) {
			WolfstagramRunnable wolf = new WolfstagramRunnable(user);
			Thread thread = new Thread(wolf);
			thread.start();
			clientThreads.add(thread);
		}
		
		
		System.out.println("About to sleep...");
		Thread.sleep(Long.valueOf(wolfProperties.getProperty("timeToRun")));
		System.out.println("Woke Up");
		for (Thread t: clientThreads){
			t.interrupt();
		}
		System.out.println("end of program");
			
	}
}
