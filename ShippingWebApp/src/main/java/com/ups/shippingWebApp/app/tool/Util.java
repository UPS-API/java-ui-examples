package com.ups.shippingWebApp.app.tool;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.AbstractMap.SimpleEntry;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import javax.servlet.http.HttpServletRequest;

import com.ups.shippingWebApp.model.Error;
import com.ups.shippingWebApp.model.ErrorRespobj;
import com.ups.shippingWebApp.model.ErrorResponse;
import com.ups.shippingWebApp.model.Response;
import org.openapitools.addressValidation.client.model.XAVRequestWrapper;
import org.openapitools.oauth.client.ApiClient;
import org.openapitools.oauth.client.api.OAuthApi;
import org.openapitools.oauth.client.model.GenerateTokenSuccessResponse;
import org.openapitools.rate.client.model.RATERequestWrapper;
import org.openapitools.shipping.client.model.SHIPRequestWrapper;
import org.openapitools.shipping.client.model.SHIPResponseWrapper;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ups.shippingWebApp.app.AppConfig;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Util {
	private static final Map<String, API_TYPE> JSON_OBJECT_TO_TARGET_TYPE = new HashMap<>();
	static {
		JSON_OBJECT_TO_TARGET_TYPE.put("\"ShipmentRequest\".\"Shipment\".\"Package\"", API_TYPE.ARRAY);
		JSON_OBJECT_TO_TARGET_TYPE.put("\"ShipmentResponse\".\"ShipmentResults\".\"PackageResults\"", API_TYPE.ARRAY);
		JSON_OBJECT_TO_TARGET_TYPE.put("\"ShipmentResponse\".\"ShipmentResults\".\"PackageResults\".\"ItemizedCharges\"", API_TYPE.ARRAY);
		JSON_OBJECT_TO_TARGET_TYPE.put("\"ShipmentResponse\".\"ShipmentResults\".\"ShipmentCharges\".\"ItemizedCharges\"", API_TYPE.ARRAY);
		JSON_OBJECT_TO_TARGET_TYPE.put("\"ShipmentResponse\".\"Response\".\"Alert\"", API_TYPE.ARRAY);
		JSON_OBJECT_TO_TARGET_TYPE.put("\"ShipmentResponse\".\"ShipmentResults\".\"PackageResults\".\"NegotiatedCharges\".\"ItemizedCharges\"", API_TYPE.ARRAY);

		JSON_OBJECT_TO_TARGET_TYPE.put("\"RateResponse\".\"RatedShipment\"", API_TYPE.ARRAY);
		JSON_OBJECT_TO_TARGET_TYPE.put("\"RateResponse\".\"RatedShipment\".\"ItemizedCharges\"", API_TYPE.ARRAY);
		JSON_OBJECT_TO_TARGET_TYPE.put("\"RateResponse\".\"RatedShipment\".\"RatedPackage\"", API_TYPE.ARRAY);
		JSON_OBJECT_TO_TARGET_TYPE.put("\"RateResponse\".\"RatedShipment\".\"RatedShipmentAlert\"", API_TYPE.ARRAY);
		JSON_OBJECT_TO_TARGET_TYPE.put("\"RateResponse\".\"RatedShipment\".\"RatedPackage\".\"NegotiatedCharges\".\"ItemizedCharges\"", API_TYPE.ARRAY);
		JSON_OBJECT_TO_TARGET_TYPE.put("\"RateResponse\".\"RatedShipment\".\"NegotiatedRateCharges\".\"ItemizedCharges\"", API_TYPE.ARRAY);

		JSON_OBJECT_TO_TARGET_TYPE.put("\"XAVResponse\".\"Candidate\"", API_TYPE.ARRAY);

	}

	private enum API_TYPE {
		ARRAY
	}

	public static Map<String, API_TYPE> getJsonToObjectConversionMap() {
		return Collections.unmodifiableMap(JSON_OBJECT_TO_TARGET_TYPE);
	}

	public static final String SUCCESS_MSG = "Success";
	public static final int SUCCESS_CODE = 200;

	public static final int UNHANDLED_EXCEPTION_CODE = 500;

	public static final String UNHANDLED_EXCEPTION = "Unhandled Exception";
	public static final String UNHANDLED_EXCEPTION_MSG = "Something Wrong!Pls check the logs!";
	private static final String CLIENT_CREDENTIALS = "client_credentials";
	private static final String BASIC_AUTH = "Basic ";
	private static final AtomicLong EXPIRY = new AtomicLong(0);
	private static final AtomicLong TOKEN_EXPIRY_TOLERANCE_IN_SEC = new AtomicLong(5);
	private static boolean readExpiryToleranceFromConfig = false;

	private static boolean isTokenExpired() {
		return ((EXPIRY.get() - new Date().getTime() / 1000) - 1 < TOKEN_EXPIRY_TOLERANCE_IN_SEC.get());
	}

	public static String getAccessToken(final AppConfig appConfig, final RestTemplate restTemplate,
			HttpServletRequest request) {
		String accessToken = null;
		if (null != request.getSession().getAttribute("clientId") &&
				null != request.getSession().getAttribute("secret")) {
			if (!readExpiryToleranceFromConfig) {
				TOKEN_EXPIRY_TOLERANCE_IN_SEC.set(appConfig.getTokenExipryToleranceInSec());
				readExpiryToleranceFromConfig = true;
			}
			String clientId = (String) request.getSession().getAttribute("clientId");
			String secret = (String) request.getSession().getAttribute("secret");
			accessToken = appConfig.getAccessTokenStore().get(clientId);
			if (null == accessToken || isTokenExpired()) {
				synchronized (Util.class) {
					OAuthApi oauthApi = new OAuthApi(new ApiClient(restTemplate));
					final String encodedClientIdAndSecret = Base64.getEncoder().encodeToString(
							(clientId + ':' + secret).getBytes(StandardCharsets.UTF_8));
					oauthApi.getApiClient().setBasePath(appConfig.getOauthBaseUrl());
					oauthApi.getApiClient().addDefaultHeader(HttpHeaders.AUTHORIZATION,
							BASIC_AUTH + encodedClientIdAndSecret);
					log.info("ecnoded clientId and secret: [{}]", encodedClientIdAndSecret);

					try {
						GenerateTokenSuccessResponse generateAccessTokenResponse = oauthApi
								.generateToken(CLIENT_CREDENTIALS, null);
						accessToken = generateAccessTokenResponse.getAccessToken();
						EXPIRY.set(new Date().getTime() / 1000
								+ Long.parseLong(generateAccessTokenResponse.getExpiresIn()) - 2);
					} catch (Exception ex) {
						throw new IllegalStateException(ex);
					}
				}
			}
			log.info("access token [{}], expiry [{}]", accessToken, EXPIRY.get());
			appConfig.getAccessTokenStore().put(clientId, accessToken);
		}
		return accessToken;
	}


	public static String getAccessTokenForShipping(final AppConfig appConfig, final RestTemplate restTemplate) {
		if(!readExpiryToleranceFromConfig) {
			TOKEN_EXPIRY_TOLERANCE_IN_SEC.set(appConfig.getTokenExipryToleranceInSec());
			readExpiryToleranceFromConfig = true;
		}
		log.info("[getAccessTokenForShipping]" + appConfig.getClientID());
		String accessToken = appConfig.getAccessTokenStore().get(appConfig.getClientID());
		log.info("[getAccessTokenForShipping]" + accessToken);
		if(null == accessToken || isTokenExpired()) {
			synchronized(Util.class) {
				if(null == accessToken || isTokenExpired()) {
					OAuthApi oauthApi = new OAuthApi(new ApiClient(restTemplate));
					final String encodedClientIdAndSecret = Base64.getEncoder().encodeToString(
							(appConfig.getClientID() + ':' + appConfig.getSecret()).
									getBytes(StandardCharsets.UTF_8));
					oauthApi.getApiClient().setBasePath(appConfig.getOauthBaseUrl());
					oauthApi.getApiClient().addDefaultHeader(HttpHeaders.AUTHORIZATION, BASIC_AUTH + encodedClientIdAndSecret);
					log.info("ecnoded clientId and secret: [{}]", encodedClientIdAndSecret);

					try {
						GenerateTokenSuccessResponse generateAccessTokenResponse = oauthApi.generateToken(CLIENT_CREDENTIALS, null);
						accessToken = generateAccessTokenResponse.getAccessToken();
						EXPIRY.set(new Date().getTime()/1000 + Long.parseLong(generateAccessTokenResponse.getExpiresIn()) - 2);
					} catch (Exception ex) {
						throw new IllegalStateException(ex);
					}
				}
			}
		}
		log.info("access token [{}], expiry [{}]", accessToken, EXPIRY.get());
		appConfig.getAccessTokenStore().put(appConfig.getClientID(), accessToken);
		return accessToken;
	}

	public static SHIPRequestWrapper buildShippingRequestNew(String req) throws JsonProcessingException {
			return Util.jsonResultPreprocess(req, Util.getJsonToObjectConversionMap(), SHIPRequestWrapper.class);
	}

	public static XAVRequestWrapper buildAddresssRequest(String req) throws JsonProcessingException {

			ObjectMapper mapper = new ObjectMapper();
			return mapper.readValue(req,
			XAVRequestWrapper.class);

	}

	public static ErrorResponse constructErrorResponse(String errorMsg) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			return mapper.readValue(errorMsg,
					ErrorResponse.class);
		} catch (Exception ex) {
		}
		return null;
	}
	public static void constructHttpException(Response response, HttpClientErrorException he) {
		ErrorResponse errorResponse=constructErrorResponse(he.getResponseBodyAsString());
		response.setStatusCode(he.getStatusCode().value());
		response.setStatusMsg(he.getStatusCode().getReasonPhrase());
		response.setErrorResponse(errorResponse);
	}

	public static void constructHttpServerException(Response response, HttpServerErrorException he) {
		ErrorResponse errorResponse=constructErrorResponse(he.getResponseBodyAsString());
		response.setStatusCode(he.getStatusCode().value());
		response.setStatusMsg(he.getStatusCode().getReasonPhrase());
		response.setErrorResponse(errorResponse);
	}
	public static void constructExceptionResp(Response response) {
		ErrorResponse errorResponse=new ErrorResponse();
		response.setStatusCode(UNHANDLED_EXCEPTION_CODE);
		response.setStatusMsg(UNHANDLED_EXCEPTION);
		ErrorRespobj errorRespobj=new ErrorRespobj();
		Error error=new Error();
		error.setCode(UNHANDLED_EXCEPTION_CODE);
		error.setMessage(UNHANDLED_EXCEPTION_MSG);
		List<Error> errorList=new ArrayList<>();
		errorList.add(error);
		errorRespobj.setErrors(errorList);
	}

	public static RATERequestWrapper buildRateRequest(String req) throws JsonProcessingException {

			ObjectMapper mapper = new ObjectMapper();
			return mapper.readValue(req,
					RATERequestWrapper.class);

	}

	/**
	 * json result preprocessing
	 * 
	 * @param resultResponse
	 * @param jsonObject2TargetType
	 * @param targetClassType
	 * @return
	 * @param <T>
	 * @throws JsonProcessingException
	 */
	public static <T> T jsonResultPreprocess(final  String resultResponse,
			final Map<String, API_TYPE> jsonObject2TargetType, Class<T> targetClassType)
			throws JsonProcessingException {
				ObjectMapper objectMapper=new ObjectMapper();
		AtomicReference<String> response = new AtomicReference<>(resultResponse);

		Consumer<Map.Entry<String, API_TYPE>> convertObjectToArray = entry -> {
			final String elementString = entry.getKey();

			String updatedResponse = response.get();

			// find the end position of last element.
			SimpleEntry<String, Integer> pointer = indexOf(elementString, updatedResponse);
			if (pointer.getValue() != -1) {
				updatedResponse = updateJsonResponse(updatedResponse, pointer);
			}
			// store the updated response for next element processing in the
			// jsonObject2TargetType.
			response.set(updatedResponse);
		};

		// Currently converting object to array of object.
		jsonObject2TargetType.entrySet().stream().filter(entry -> entry.getValue() == API_TYPE.ARRAY)
				.forEach(convertObjectToArray::accept);
		return objectMapper.readValue(response.get(), targetClassType);
	}

	/**
	 * updating json response
	 * 
	 * @param response
	 * @param pointer
	 * @return
	 */
	private static String updateJsonResponse(final String response, final SimpleEntry<String, Integer> pointer) {
		int position = pointer.getValue();
		String lastElement = pointer.getKey();

		String updatedResponse = response;
		while (-1 != position) {
			position = updatedResponse.indexOf(":", position);

			// Is last element already an array in resultResponse?
			boolean arrayType = false;
			boolean done = false;
			for (int i = position + 1; i < updatedResponse.length(); i++) {
				if (updatedResponse.charAt(i) == '{') {
					// non-array
					position = i;
					done = true;
				} else if (updatedResponse.charAt(i) == '[') {
					arrayType = true;
					done = true;
				}

				if (done) {
					break;
				}
			}

			if (!arrayType) {
				StringBuilder builder = new StringBuilder(updatedResponse.substring(0, position));
				builder.append('[').append(updatedResponse.substring(position, updatedResponse.length()));
				updatedResponse = addClosingArray(builder.toString(), position);
			}
			position = updatedResponse.indexOf(lastElement, position);
		}
		return updatedResponse;
	}

	/**
	 * mapping json object to responce
	 * 
	 * @param elementString
	 * @param response
	 * @return
	 */
	private static SimpleEntry<String, Integer> indexOf(final String elementString, final String response) {
		int position = 0;
		final String[] elements = elementString.split("\\.");
		String lastElement = null;
		for (String element : elements) {
			position = response.indexOf(element, position);
			if (-1 == position) {
				return new SimpleEntry<>(lastElement, position);
			}
			lastElement = element;
			position += lastElement.length();
		}

		if (-1 == position || null == lastElement) {
			throw new NoSuchElementException(elementString + " does not exist in response");
		}

		return new SimpleEntry<>(lastElement, position);
	}

	private static String addClosingArray(final String response, int position) {
		position = response.indexOf('{', position);
		if (-1 == position) {
			throw new NoSuchElementException("internal error - cannot find beginning of element.");
		}

		int outstandingParathesis = 0;

		for (int i = position + 1; i < response.length(); i++) {
			if (response.charAt(i) == '}') {
				if (outstandingParathesis == 0) {
					// found the element of element.
					StringBuilder builder = new StringBuilder(response.substring(0, i + 1));
					builder.append(']').append(response.substring(i + 1, response.length()));
					return builder.toString();
				} else {
					outstandingParathesis--;
				}
			}
			if (response.charAt(i) == '{') {
				outstandingParathesis++;
			}
		}
		throw new NoSuchElementException("incomplete response - missing ending element parathesis [" + response + ']');
	}

	private Util() {
	}
}
