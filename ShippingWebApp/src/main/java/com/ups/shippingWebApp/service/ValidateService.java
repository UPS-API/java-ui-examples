package com.ups.shippingWebApp.service;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

import org.openapitools.oauth.client.ApiClient;
import org.openapitools.oauth.client.api.OAuthApi;
import org.openapitools.oauth.client.model.GenerateTokenSuccessResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.ups.shippingWebApp.app.AppConfig;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@AllArgsConstructor
public class ValidateService {
	
	RestTemplate restTemplate;

	AppConfig appConfig;

	private static final String CLIENT_CREDENTIALS = "client_credentials";
	private static final String BASIC_AUTH = "Basic ";
	private static final AtomicLong EXPIRY = new AtomicLong(0);


	public boolean validateUser(String clientId,String secret) {	
		boolean authenticated=false;
		String accessToken=null;
		if(null!=clientId&&null!=secret) {
					OAuthApi oauthApi = new OAuthApi(new ApiClient(this.restTemplate));
					final String encodedClientIdAndSecret = Base64.getEncoder().encodeToString(
																					(clientId + ':' + secret).
																					getBytes(StandardCharsets.UTF_8));
					oauthApi.getApiClient().setBasePath(this.appConfig.getOauthBaseUrl());
					oauthApi.getApiClient().addDefaultHeader(HttpHeaders.AUTHORIZATION, BASIC_AUTH + encodedClientIdAndSecret);
					log.info("ecnoded clientId and secret: [{}]", encodedClientIdAndSecret);
					
					try {
						GenerateTokenSuccessResponse generateAccessTokenResponse = oauthApi.generateToken(CLIENT_CREDENTIALS, null);
						accessToken = generateAccessTokenResponse.getAccessToken();				
						EXPIRY.set(new Date().getTime()/1000 + Long.parseLong(generateAccessTokenResponse.getExpiresIn()) - 2);
					} catch (Exception ex) {
						log.error("Unhandled Exception in Validate Service {}",ex.getMessage());
					}
		
		}
		if(null!=accessToken){
			authenticated=true;
			this.appConfig.getAccessTokenStore().put(clientId, accessToken);
		}	
		return authenticated;
	}

}
