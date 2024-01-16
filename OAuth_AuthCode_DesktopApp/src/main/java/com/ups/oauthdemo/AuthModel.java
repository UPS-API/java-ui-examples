package com.ups.oauthdemo;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class AuthModel {
	private String clientID; //This is set as user input in the GUI class
	private String clientSecret; //This is set as user input in the GUI class
	private String code;
	private String token;
	private String refreshToken;
	
	public AuthModel() {
		clientID = "";
		clientSecret = "";
		code = "";
		token = "";
		refreshToken = "";
	}
	
	public void deleteCode() {
		code = "";
	}
	
	public void deleteToken() {
		token = "";
	}
	
	public void deleteRefreshToken() {
		refreshToken = "";
	}
	
	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getRefreshToken() {
		return refreshToken;
	}

	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getClientID() {
		return clientID;
	}

	public void setClientID(String clientID) {
		this.clientID = clientID;
	}

	public String getClientSecret() {
		return clientSecret;
	}

	public void setClientSecret(String clientSecret) {
		this.clientSecret = clientSecret;
	}
	
	public boolean hasCode() {
		return StringUtils.hasText(code);
	}
	
	public boolean hasToken() {
		return StringUtils.hasText(token);
	}
	
	public boolean hasRefreshToken() {
		return StringUtils.hasText(refreshToken);
	}
	
	public boolean hasClientID() {
		return StringUtils.hasText(clientID);
	}
	
	public boolean hasClientSecret() {
		return StringUtils.hasText(clientSecret);
	}
	
}
