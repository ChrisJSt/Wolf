package main.server.authorization;

import org.apache.commons.lang3.StringUtils;

import main.util.SetupClient;

public class ValidateLogin {
	
	public static void main(String[] args) {
		if (StringUtils.isNotBlank(args[0]) && StringUtils.isNotBlank(args[1])){
			try {
				SetupClient.validateLogin(args);
			}
			catch (Exception e) {		
			}
			SetupClient.store(); 
		}
	}
}
