package main.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import main.client.Client;

public class FollowingMapSerializerDeserializer {

	public static void serialize(Client client, File followingFile) {
		Set<Long> idsSinceStartOfSession = client.getFollowingIDSinceStartofSession();
		Map<Long, Set<Long>> idsFromLastWeek = client.getFollowingFromLastWeek();

		if (client.continueEngaging()) {
			if (idsSinceStartOfSession == null || idsSinceStartOfSession.isEmpty()) {
				return;
			}
			addIDsToAppropriateDay(idsSinceStartOfSession, idsFromLastWeek);
		}

		try {
			FileOutputStream fos = new FileOutputStream(followingFile);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(idsFromLastWeek);
			oos.flush();
			oos.close();
			fos.close();
			System.out.printf("Serialized HashSet data is saved in " + followingFile.getPath());
		} catch (FileNotFoundException fnf) {

		} catch (IOException ioe) {

		}
	}

	public static void addIDsToAppropriateDay(Set<Long> idsSinceStartOfSession, Map<Long, Set<Long>> idsFromLastWeek) {
		//Long millisecondsPerDay = TimeUnit.DAYS.toMillis(1);
		Long millisecondsPerThreeHours = TimeUnit.HOURS.toMillis(3);
		Long currentTimeInMilli = System.currentTimeMillis();
		boolean added = false;
		for (Long dateInMilli : idsFromLastWeek.keySet()) {
			if ((Math.abs(Math.subtractExact(currentTimeInMilli, dateInMilli))) < millisecondsPerThreeHours) {
				idsFromLastWeek.get(dateInMilli).addAll(idsSinceStartOfSession);
				added = true;
				break;
			}
		}
		if (!added) {
			idsFromLastWeek.put(currentTimeInMilli, idsSinceStartOfSession);
		}

	}

	public static HashMap<Long, Set<Long>> deserialize(Client client, File followingFile) {
		HashMap<Long, Set<Long>> map = null;
		try {
			FileInputStream fis = new FileInputStream(followingFile);
			ObjectInputStream ois = new ObjectInputStream(fis);
			map = (HashMap) ois.readObject();
			ois.close();
			fis.close();
		} catch (IOException ioe) {
		} catch (ClassNotFoundException e) {
		}
		System.out.println("Deserialized HashMap..");

		if (map == null || map.isEmpty()) {
			return null;
		}
		// Display content using Iterator
		Set set = map.entrySet();
		Iterator iterator = set.iterator();
		while (iterator.hasNext()) {
			Map.Entry mentry = (Map.Entry) iterator.next();
			System.out.print("key: " + mentry.getKey() + " & Value: ");
			System.out.println(mentry.getValue());
			System.out.println("Size of unfollow list is " + map.get(mentry.getKey()).size());
		}
		client.setFollowingFromLastWeek(map);
		return map;
	}
}
