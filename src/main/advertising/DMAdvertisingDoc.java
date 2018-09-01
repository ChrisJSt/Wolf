package main.advertising;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.*;
import com.google.api.services.sheets.v4.Sheets;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DMAdvertisingDoc {
	
	private static String message = "";
	
	private static Long userID= null;
	
	private static String scriptCombo = "";
	
	private static Sheets service;
	
	private static int cellToUpdate = 2;
	
    /** Application name. */
    private static final String APPLICATION_NAME =
        "Google Sheets API Java Quickstart";

    /** Directory to store user credentials for this application. */
    private static final java.io.File DATA_STORE_DIR = new java.io.File(
        System.getProperty("user.home"), ".credentials/sheets.googleapis.com-java-quickstart");

    /** Global instance of the {@link FileDataStoreFactory}. */
    private static FileDataStoreFactory DATA_STORE_FACTORY;

    /** Global instance of the JSON factory. */
    private static final JsonFactory JSON_FACTORY =
        JacksonFactory.getDefaultInstance();

    /** Global instance of the HTTP transport. */
    private static HttpTransport HTTP_TRANSPORT;

    /** Global instance of the scopes required by this quickstart.
     *
     * If modifying these scopes, delete your previously saved credentials
     * at ~/.credentials/sheets.googleapis.com-java-quickstart
     */
    private static final List<String> SCOPES =
        Arrays.asList(SheetsScopes.SPREADSHEETS);

    static {
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            DATA_STORE_FACTORY = new FileDataStoreFactory(DATA_STORE_DIR);
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Creates an authorized Credential object.
     * @return an authorized Credential object.
     * @throws Exception 
     */
    public static Credential authorize() throws Exception {
        // Load client secrets.
        InputStream in =
        		DMAdvertisingDoc.class.getResourceAsStream("/client_secret.json");
        GoogleClientSecrets clientSecrets =
            GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow =
                new GoogleAuthorizationCodeFlow.Builder(
                        HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(DATA_STORE_FACTORY)
                .setAccessType("offline")
                .build();
        Credential credential = new AuthorizationCodeInstalledApp(
            flow, new LocalServerReceiver()).authorize("user");
        System.out.println(
                "Credentials saved to " + DATA_STORE_DIR.getAbsolutePath());
        return credential;
    }

    /**
     * Build and return an authorized Sheets API client service.
     * @return an authorized Sheets API client service
     * @throws Exception 
     */
    public static Sheets getSheetsService() throws Exception {
        Credential credential = authorize();
        return new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    public static List<List<?>> getUnmessagedProspectsFromGoogleSheet() throws Exception {
        // Build a new authorized API client service.
        service = getSheetsService();

        String spreadsheetId = "1-qsEgVTdMgC9wLYEGJSvdGd8YQ0J8qcrCjy8rSORveY";
        String range = "AutomatedDMList!A2:E";
        ValueRange response = service.spreadsheets().values()
            .get(spreadsheetId, range)
            .execute();
        List<List<Object>> values = response.getValues();
        List<List<?>> prospects = null;
        if (values == null || values.size() == 0) {
            System.out.println("No data found.");
        } else {
          prospects = new ArrayList<List<?>>();
          List<String> alreadyMsged = new ArrayList<String>();
          System.out.println("Username, Name, Followers, Sent, Script Combo");
          int rowcounter = 1;
          for (List<?> row : values) {
        	  rowcounter += 1;
        	  if (row.get(0) == null || (row.get(3) != null && !row.get(3).toString().trim().equalsIgnoreCase("NO"))) {
        		  if (row.get(0) != null) {
                	  String user = (String) row.get(0);
                	  user = user.trim();
                	  alreadyMsged.add(user);
        		  }
        		  continue;
        	  }
        	  String user = (String) row.get(0);
        	  user = user.trim();
        	  if (alreadyMsged.contains(user)) {
        		  continue;
        	  }
        	  prospects.add(row);
            System.out.printf("%s, %s, %s, %s\n", row.get(0), row.get(1)
            		,row.get(2),row.get(3));
            System.out.println();
            cellToUpdate = rowcounter;
            break;
          }
        }
        return prospects;
    }

	public static void markSent(boolean wasSent) throws Exception {
		String note = "YES";
		if (!wasSent){
			note = "ERROR...skipping";
		}
		String id = "1-qsEgVTdMgC9wLYEGJSvdGd8YQ0J8qcrCjy8rSORveY";
		List<List<Object>> values = Arrays.asList(
		        Arrays.asList(
		        		note,
		        		message,
		        		userID
		        )
		        // Additional rows ...
		);
		ValueRange body = new ValueRange()
		        .setValues(values);
		UpdateValuesResponse result =
		        service.spreadsheets().values().update(id, "AutomatedDMList!D".
		        							concat(String.valueOf(cellToUpdate)), body)
		                .setValueInputOption("USER_ENTERED")
		                .execute();
		System.out.printf("%d cells updated.\n", result.getUpdatedCells());
		
	}

	public static void setMessage(String msg) {
		if (msg != null){
			message = msg;
		}
	}

	public static void setCombo(String combo) {
		if (combo != null) {
			scriptCombo = combo;
		}	
	}
	
	public static void setUserIDofMostRecentRecipient(Long id) {
		if (id != null){
			userID = id;
		}
	}


}
