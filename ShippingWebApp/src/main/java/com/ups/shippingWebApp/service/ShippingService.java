package com.ups.shippingWebApp.service;

import com.ups.shippingWebApp.model.Response;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.openapitools.shipping.client.ApiClient;
import org.openapitools.shipping.client.model.SHIPRequestWrapper;
import org.openapitools.shipping.client.model.SHIPResponseWrapper;
import org.openapitools.shipping.client.model.ShipmentResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ups.shippingWebApp.app.AppConfig;
import com.ups.shippingWebApp.app.tool.ShipApi;
import com.ups.shippingWebApp.app.tool.Util;

@Service
@Slf4j
@AllArgsConstructor
public class ShippingService {

    private static final ThreadLocal<ShipApi> api = new ThreadLocal<>();
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

    public Response getShippingResponse(String shippingRequest, HttpServletRequest request) {

        Response response = new Response();
        try {
            // SHIPRequestWrapper shipmentRequest =Util.buildShippingRequest(shippingRequest);
            SHIPRequestWrapper shipmentRequest = Util.buildShippingRequestNew(shippingRequest);
            SHIPResponseWrapper shipResponseWrapper = this.sendRequest(shipmentRequest, request);
            ShipmentResponse shipmentResponse = shipResponseWrapper.getShipmentResponse();
            response.setResponse(shipmentResponse);
            response.setStatusCode(Util.SUCCESS_CODE);
            response.setStatusMsg(Util.SUCCESS_MSG);
            processResult("Shipping resp", shipmentResponse);
            ObjectMapper objectMapper = new ObjectMapper();

            request.getSession().setAttribute("ShipmentResponse", objectMapper.writeValueAsString(shipmentResponse));
        } catch (HttpClientErrorException he) {
            log.error("HttpClientErrorException in Shipping Service {}",he.getMessage());
            Util.constructHttpException(response, he);

        } catch (HttpServerErrorException e) {
            log.error("HttpServerErrorException in Shipping Service {}",e.getMessage());
            Util.constructHttpServerException(response, e);

        } catch (Exception ex) {
            log.error("Unhandled Exception in Shipping Service {}",ex.getMessage());
            Util.constructExceptionResp(response);

        } finally {
            this.cleanup();
        }
        return response;

    }

    public SHIPResponseWrapper sendRequest(final SHIPRequestWrapper shipRequestWrapper, HttpServletRequest request) throws JsonProcessingException, RestClientException {
        final String transId = UUID.randomUUID().toString().replaceAll("-", "");
        final String accessToken = Util.getAccessToken(this.appConfig, this.restTemplate, request);
        ShipApi shipApi = api.get();
        if (null == shipApi) {
            shipApi = new ShipApi(new ApiClient(this.restTemplate));
            shipApi.getApiClient().setBasePath(this.appConfig.getBaseUrl());
            api.set(shipApi);
        }

        shipApi.getApiClient().addDefaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
        return Util.jsonResultPreprocess(shipApi.shipment(this.appConfig.getVersion(), shipRequestWrapper, transId, this.appConfig.getTransactionSrc(), null), Util.getJsonToObjectConversionMap(), SHIPResponseWrapper.class);
    }

    private void cleanup() {
        ShipApi shipApi = api.get();
        if (null != shipApi) {
            api.remove();
        }
    }
}
