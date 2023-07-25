package com.ups.shippingWebApp.app;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import lombok.Data;

@Configuration
@Data
public class AppConfig {
	
	public static final int SCENARIO_PROPERTIES_JSON_FILE_NAME = 0;
	public static final int SCENARIO_PROPERTIES_CLASS_NAME = 1;
	
	@Value("${api.oauth.partner.client.id:#{null}}")
	private String clientID;

	@Value("${api.oauth.partner.secret:#{null}}")
	private String secret;
	
	@Value("${api.oauth.base.url}")
	private String oauthBaseUrl;

	// store access token obtaining by client_credential grant_type.
	private Map<String,String> accessTokenStore = new ConcurrentHashMap<>();
	
	@Value("${api.base.url:#{null}}")
	private String baseUrl;
	
	@Value("${api.version:v1}")
	private String version;

	
	@Value("${api.transaction.source:testing}")
	private String transactionSrc;

	@Value("${api.addressValidation.reqOption}")
	private Integer addressValidationReqOption;

	@Value("${api.oauth.access.token.expiry.tolerance:5}")
	private long tokenExipryToleranceInSec;
	
	
	@Bean 
	public RestTemplate restTemplate(RestTemplateBuilder builder) {
		final SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
		return builder.requestFactory(() -> factory).build();
	}
}
