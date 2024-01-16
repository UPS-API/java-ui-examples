package com.ups.oauthdemo;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Base64;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class AuthService {
	//Loaded from application.properties
	@Value("${redirect.uri}")
	private String redirectUri;
	
	@Autowired
	private AuthModel authModel;
	
	private String BASE_URL = "https://wwwcie.ups.com/security/v1/oauth";
	
	/**
	 * Opens a browser window and navigates to login URL if possible.
	 * 
	 * @throws IOException if system does not allow browser window to be opened
	 * @throws URISyntaxException if client ID or redirect URI cause the login URI string to violate RFC 2396
	 */
	public void openLoginPage() throws IOException, URISyntaxException {
		//String clientId = this.clientId;
		String redirectUri = this.redirectUri;
		String clientId = authModel.getClientID();
		
		String uri = String.format("%s/authorize?client_id=%s&redirect_uri=%s&response_type=code", BASE_URL, clientId, redirectUri);
		
		if (!Desktop.isDesktopSupported() || !Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {				
			throw new IOException(String.format("Unable to launch browser. Please navigate to the following URL in a browser.\n\n%s", uri));
		}
		
		try {
			Desktop.getDesktop().browse(new URI(uri));
		} catch (URISyntaxException e) {
			throw new URISyntaxException(uri, "Invalid URI. Client ID or redirect URI may be invalid.");
		}
	}
	
	/**
	 * Sends a POST request using an authorization code to retrieve an access token.
	 * Updates the auth model with the new access token and refresh token if successful.
	 * 
	 * @return the HTTP response body as a JSONObject
	 * @throws RestClientException if request is unsuccessful
	 * @throws JSONException if the response body is not in JSON format
	 */
	public JSONObject generateToken() throws RestClientException, JSONException {
		String clientID = authModel.getClientID();
		String clientSecret = authModel.getClientSecret();
		String code = this.authModel.getCode();
		String redirectUri = this.redirectUri;
		String grant_type = "authorization_code";

		String uri = String.format("%s/token", BASE_URL);
		RestTemplate restTemplate = new RestTemplate();
		
		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-Type", MediaType.APPLICATION_FORM_URLENCODED.toString());
		headers.add("Authorization", "Basic " + Base64.getEncoder().encodeToString((String.format("%s:%s", clientID, clientSecret)).getBytes()));
		
		MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<String, String>();
		requestBody.add("grant_type", grant_type);
		requestBody.add("code", code);
		requestBody.add("redirect_uri", redirectUri);
		
		HttpEntity<MultiValueMap<String,String>> formEntity = new HttpEntity<MultiValueMap<String, String>>(requestBody, headers);
		
		ResponseEntity<String> response = restTemplate.postForEntity(uri, formEntity, String.class);
		JSONObject resp = new JSONObject(response.getBody());
		
		authModel.setToken(resp.getString("access_token"));
		authModel.setRefreshToken(resp.getString("refresh_token"));
		
		return resp;
	}
	

	/**
	 * Sends a POST request using the refresh token to generate a new access token.
	 * Updates the auth model with the new access token and refresh token if successful.
	 * 
	 * @return the HTTP response body as a JSONObject
	 * @throws RestClientException if request is unsuccessful
	 * @throws JSONException if the response body is not in JSON format
	 */
	public JSONObject refreshToken() throws RestClientException, JSONException {
		String clientID = authModel.getClientID();
		String clientSecret = authModel.getClientSecret();
		String grant_type = "refresh_token";
		String refresh_token = authModel.getRefreshToken();
		
		String uri = String.format("%s/refresh", BASE_URL);
		RestTemplate restTemplate = new RestTemplate();
		
		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-Type", MediaType.APPLICATION_FORM_URLENCODED.toString());
		headers.add("Authorization", "Basic " + Base64.getEncoder().encodeToString((String.format("%s:%s", clientID, clientSecret)).getBytes()));
		
		MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<String, String>();
		requestBody.add("grant_type", grant_type);
		requestBody.add("refresh_token", refresh_token);
		
		HttpEntity<MultiValueMap<String,String>> formEntity = new HttpEntity<MultiValueMap<String, String>>(requestBody, headers);
			
		ResponseEntity<String> response = restTemplate.postForEntity(uri, formEntity, String.class);
		JSONObject resp = new JSONObject(response.getBody());
		authModel.setToken(resp.getString("access_token"));
		authModel.setRefreshToken(resp.getString("refresh_token"));

		return resp;		
	}
}
