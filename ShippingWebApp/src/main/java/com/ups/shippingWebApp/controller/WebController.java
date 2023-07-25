package com.ups.shippingWebApp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ups.shippingWebApp.model.Response;
import com.ups.shippingWebApp.service.AddressValidationService;
import com.ups.shippingWebApp.service.RateService;
import com.ups.shippingWebApp.service.ShippingService;
import com.ups.shippingWebApp.service.ValidateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Controller
public class WebController {

    @Autowired
    AddressValidationService addressValidationService;


    @Autowired
    ValidateService validateService;

    @Autowired
    ShippingService shippingService;

    @Autowired
    RateService rateService;

    @GetMapping("/application")
    public String application() {
        return "application";
    }

    @GetMapping("/")
    public String index(HttpSession session) {
        if (session != null && session.getAttribute("keepSignIn") != null && session.getAttribute("keepSignIn").equals(true)) {
            return "application";
        }
        return "index";
    }

    @PostMapping("/logout")
    public String logout(HttpSession session) {
        if (session != null && session.getAttribute("keepSignIn") != null) {
            session.removeAttribute("keepSignIn");
        }
        return "index";
    }


    @PostMapping("/validateUser")
    public String validateUser(@RequestParam("clientId") String clientId, @RequestParam("secret") String secret, @RequestParam("keepSignIn") String keepSignIn, HttpServletRequest request) {


        if (this.validateService.validateUser(clientId, secret)) {
            request.getSession().setAttribute("clientId", clientId);
            request.getSession().setAttribute("secret", secret);
            if (null != keepSignIn && !keepSignIn.equalsIgnoreCase("notselected")) {
                request.getSession().setAttribute("keepSignIn", true);
            }

            return "redirect:/application";
        }
        return "unAuthorized";
    }


    @PostMapping("/shipAjax")
    public ResponseEntity<Response> shipAjax(@RequestBody String shippingJson, HttpServletRequest request) {
        Response response = this.shippingService.getShippingResponse(shippingJson, request);
        String shippingReponse = "empty";
        if (null != request.getSession().getAttribute("ShipmentResponse")) {
            shippingReponse = (String) request.getSession().getAttribute("ShipmentResponse");
        }

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/addressValidationAjax")
    public ResponseEntity<Response> addressValidationAjax(@RequestBody String addressJson, HttpServletRequest request) {

        Response response = this.addressValidationService.validateAddress(addressJson, request);
        ObjectMapper mapper = new ObjectMapper();
        String validAddress = "NotValidated";

        if (null != request.getSession().getAttribute("validAddress")) {
            validAddress = (String) request.getSession().getAttribute("validAddress");
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/rateAjax")
    public ResponseEntity<Response> rateAjax(@RequestBody String rateJson, HttpServletRequest request) {

        Response response = this.rateService.validateRate(rateJson, request);
        String rateResponse = "empty";

        if (null != request.getSession().getAttribute("rateResponse")) {
            rateResponse = (String) request.getSession().getAttribute("rateResponse");
        }

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
