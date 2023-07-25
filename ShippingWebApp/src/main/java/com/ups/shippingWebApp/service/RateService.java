package com.ups.shippingWebApp.service;
import com.ups.shippingWebApp.app.tool.RateApi;
import com.ups.shippingWebApp.model.Response;
import org.openapitools.rate.client.ApiClient;
import org.openapitools.rate.client.model.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ups.shippingWebApp.app.AppConfig;
import com.ups.shippingWebApp.app.tool.Util;
import javax.servlet.http.HttpServletRequest;
import java.util.UUID;

@Service
@Slf4j
@AllArgsConstructor
public class RateService {

    RestTemplate restTemplate;

	AppConfig appConfig;

	private static final ThreadLocal<RateApi> api = new ThreadLocal<>();
	
	private static final String BEARER = "Bearer ";

    public Response validateRate(String rateRequest, HttpServletRequest request) {
        Response response = new Response();
        try {
            RATERequestWrapper rateRequestWrapper = Util.buildRateRequest(rateRequest);
            final String transId = UUID.randomUUID().toString().replaceAll("-", "");
            final String requestOption = "shop";
            final String additionalInfo = "timeintransit";
            // Get Rate Validation result
            RATEResponseWrapper rateResponseWrapper = (RATEResponseWrapper)this.sendRequest(rateRequestWrapper,request,transId,requestOption,additionalInfo);
            RateResponse rateResponse=rateResponseWrapper.getRateResponse();
            response.setResponse(rateResponse);
            response.setStatusCode(Util.SUCCESS_CODE);
            response.setStatusMsg(Util.SUCCESS_MSG);
            processResult("RateValidation resp", rateResponse);
            ObjectMapper objectMapper=new ObjectMapper();
            request.getSession().setAttribute("rateResponse", objectMapper.writeValueAsString(rateResponse));
        } catch (HttpClientErrorException he) {
            log.error("HttpClientErrorException in Rate Service {}",he.getMessage());
            Util.constructHttpException(response, he);

        }catch (HttpServerErrorException e) {
            log.error("HttpServerErrorException in Rate Service {}",e.getMessage());
            Util.constructHttpServerException(response, e);

        }  catch (Exception ex) {
            log.error("Unhandled Exception in Rate Service {}",ex.getMessage());
            Util.constructExceptionResp(response);

        }finally {
            this.cleanup();
        }
        return response;
    }

public RATEResponseWrapper sendRequest(final RATERequestWrapper rateRequestWrapper, HttpServletRequest request,                                        
     final String transId,                                       
     final String requestOption,                                       
     final String additionalInfo) throws 
JsonProcessingException {   
    log.info("transId: {}, requestOption: [{}], additionalInfo: [{}]", 
    transId, requestOption, additionalInfo);   
    final String accessToken = Util.getAccessToken(this.appConfig, this.restTemplate,request);       
    RateApi rateApi = api.get();       
    if(null == rateApi) {           
        rateApi = new RateApi(new ApiClient(restTemplate));          
        rateApi.getApiClient().setBasePath(appConfig.getBaseUrl());           
        api.set(rateApi);       
    }       
        rateApi.getApiClient().addDefaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);

        return Util.jsonResultPreprocess(rateApi.rate(appConfig.getVersion(),
                        requestOption,
                        rateRequestWrapper,
                        transId,
                        appConfig.getTransactionSrc(),
                        additionalInfo),
                             Util.getJsonToObjectConversionMap(),RATEResponseWrapper.class);
                        }

    private void cleanup() {
        RateApi rateApi = api.get();
        if(null != rateApi) {
            api.remove();
        }
    }

    public static void processResult(final String scenarioName, final Object response) {
        ObjectMapper mapper = new ObjectMapper();

        try {
            // convert user object to json string and return it
            log.info("Scenario name: {}, response[{}]", scenarioName, mapper.writeValueAsString(response));
        } catch (Exception e) {
            log.warn("Unable to process result {}", e.getMessage());
        }
    }

}
