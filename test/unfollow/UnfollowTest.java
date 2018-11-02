package unfollow;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import main.actions.InstagramUnfollowRequest;
import main.client.Client;
import main.util.Login;

public class UnfollowTest {
	
	
	public static void main(String[] args) {
		Client client = new Client("Jutta_Lemcke","dummy");
		Login.login(client, Boolean.TRUE);
		Instagram4j insta = client.getInstagram();
		Pattern pattern = Pattern.compile("(.)+:");
		Matcher matcher;
		FileReader fr = null;
		BufferedReader buff = null;
		try {
			fr = new FileReader("file.txt");
			buff = new BufferedReader(fr);
		} catch (FileNotFoundException e) {

		}
		
		String line;
		try {
			while ((line = buff.readLine()) != null) {
				if (!line.startsWith("!!!!!")){
					matcher = pattern.matcher(line);
					if (matcher.find()){
						String username = matcher.group(1);
						String id = matcher.group(2);
						System.out.println("About to unfollow " + username + " with ID " + id);
						Thread.sleep(30000);
						StatusResult result = null;
						synchronized (insta) {
							result = insta.sendRequest(new InstagramUnfollowRequest(id));
						}
						if (!checkIsSpam(result)) {
							System.out.println(client.getUsername() + " successfully unfollowed " + username + " with id " + id);
						}
						
					}
					
				}
			}
		} catch (IOException e) {

		}
	}

}
