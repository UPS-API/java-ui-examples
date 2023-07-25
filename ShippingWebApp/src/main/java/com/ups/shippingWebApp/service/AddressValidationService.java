package com.ups.shippingWebApp.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ups.shippingWebApp.app.AppConfig;
import com.ups.shippingWebApp.app.tool.AddressValidationApi;
import com.ups.shippingWebApp.app.tool.Util;
import com.ups.shippingWebApp.model.AddressResponse;
import com.ups.shippingWebApp.model.Response;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openapitools.addressValidation.client.ApiClient;
import org.openapitools.addressValidation.client.model.XAVRequestWrapper;
import org.openapitools.addressValidation.client.model.XAVResponse;
import org.openapitools.addressValidation.client.model.XAVResponseWrapper;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;

@Service
@Slf4j
@AllArgsConstructor
public class AddressValidationService {

    private static final ThreadLocal<AddressValidationApi> addressApi = new ThreadLocal<>();
    private static final String BEARER = "Bearer ";
    RestTemplate restTemplate;
    AppConfig appConfig;

    /**
     * @param scenarioName
     * @param response
     */
    public static void processResult(final String scenarioName, final Object response) {
        ObjectMapper mapper = new ObjectMapper();

        try {
            // convert user object to json string and return it
            log.info("Scenario name: {}, response[{}]", scenarioName, mapper.writeValueAsString(response));
        } catch (Exception e) {
            log.warn("Unable to process result {}", e.getMessage());
        }
    }

    public Response validateAddress(String addressrequest, HttpServletRequest request) {
        Response response = new Response();
        try {
            //addressrequest="{\"XAVRequest\":{\"AddressKeyFormat\":{\"ConsigneeName\":\"Tester\",\"AddressLine\":\"@##$#$\",\"PoliticalDivision2\":\"23424542\",\"PoliticalDivision1\":\"CA\",\"PostcodePrimaryLow\":\"95113\",\"CountryCode\":\"US\"}}}";
            XAVRequestWrapper xavRequestWrapper = Util.buildAddresssRequest(addressrequest);
            // Get Address Validation result
            XAVResponseWrapper xavResponseWrapper = (XAVResponseWrapper) this.sendRequest(xavRequestWrapper, request);
            XAVResponse xavResponse = xavResponseWrapper.getXaVResponse();

            AddressResponse addressResponse = new AddressResponse();
            if (xavResponse != null) {
                if (xavResponse.getNoCandidatesIndicator() != null) {
                    addressResponse.setInvalidAddress(true);
                    request.getSession().setAttribute("validAddress", "Address is InValid");
                } else if (xavResponse.getValidAddressIndicator() != null) {
                    addressResponse.setValidAddress(true);
                    request.getSession().setAttribute("validAddress", "Address is Valid");
                } else if (xavResponse.getAmbiguousAddressIndicator() != null) {
                    addressResponse.setAmbiguousAddress(true);
                    request.getSession().setAttribute("validAddress", "Address is Ambigous");
                }
            }
            addressResponse.setValidated(true);
            response.setResponse(addressResponse);
            response.setStatusCode(Util.SUCCESS_CODE);
            response.setStatusMsg(Util.SUCCESS_MSG);
            processResult("AddressValidation resp", xavResponse);

        } catch (HttpClientErrorException he) {
            log.error("HttpClientErrorException in AddressValidation Service {}",he.getMessage());
            Util.constructHttpException(response, he);

        }catch (HttpServerErrorException e) {
            log.error("HttpServerErrorException in AddressValidation Service {}",e.getMessage());
            Util.constructHttpServerException(response, e);

        }  catch (Exception ex) {
            log.error("Unhandled Exception in AddressValidation Service {}",ex.getMessage());
            Util.constructExceptionResp(response);

        } finally {
            this.cleanup();
        }
        return response;
    }

    public XAVResponseWrapper sendRequest(final XAVRequestWrapper xavRequestWrapper, HttpServletRequest request) throws JsonProcessingException {
        final String accessToken = Util.getAccessToken(this.appConfig, this.restTemplate, request);
        AddressValidationApi addressValidationApi = addressApi.get();
        if (null == addressValidationApi) {
            addressValidationApi = new AddressValidationApi(new ApiClient(this.restTemplate));
            addressValidationApi.getApiClient().setBasePath(this.appConfig.getBaseUrl());
            addressApi.set(addressValidationApi);
        }

        addressValidationApi.getApiClient().addDefaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
        return Util.jsonResultPreprocess(addressValidationApi
                        .addressValidation(this.appConfig.getAddressValidationReqOption(), this.appConfig.getVersion(),
                                xavRequestWrapper, null, null),
                Util.getJsonToObjectConversionMap(), XAVResponseWrapper.class);
    }

    private void cleanup() {
        AddressValidationApi addressValidationApi = addressApi.get();
        if (null != addressValidationApi) {
            addressApi.remove();
        }
    }
}
