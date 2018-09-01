package main.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.CookieStore;
import org.brunocvcunha.instagram4j.Instagram4j;

import main.client.Client;

public class CookieLoginSerializerDeserializer {

	public static void serialize(Instagram4j instagram, File cookieLoginFile, File uuIDFile) {

		CookieStore cookieStore = instagram.getCookieStore();
		String uID = instagram.getUuid();
		try {
			FileOutputStream fos = new FileOutputStream(cookieLoginFile);
			FileOutputStream fos2 = new FileOutputStream(uuIDFile);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			ObjectOutputStream oos2 = new ObjectOutputStream(fos2);
			oos.writeObject(cookieStore);
			oos2.writeObject(uID);
			oos.close();
			oos2.close();
			fos.close();
			fos2.close();
			System.out.printf("Cookie Login saved in " + cookieLoginFile.getPath());
		} catch (FileNotFoundException fnf) {
		} catch (IOException ioe) {
		}
	}

	public static CookieStore deserialize(Client client, File cookieLoginFile, File uuIDFile) {
		CookieStore cookie = null;
		String uuID = StringUtils.EMPTY;
		try {
			FileInputStream fis = new FileInputStream(cookieLoginFile);
			ObjectInputStream ois = new ObjectInputStream(fis);
			FileInputStream fis2 = new FileInputStream(uuIDFile);
			ObjectInputStream ois2 = new ObjectInputStream(fis2);
			cookie = (CookieStore) ois.readObject();
			uuID = (String) ois2.readObject();
			ois.close();
			ois2.close();
			fis.close();
			fis2.close();
		} catch (IOException ioe) {
		} catch (ClassNotFoundException e) {
		}
		System.out.println("Deserialized Cookie Login..");

		if (cookie == null || StringUtils.isBlank(uuID)) {
			return null;
		}

		client.setUUID(uuID);
		return cookie;
	}

}
