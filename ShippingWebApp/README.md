# Shipping Application Demo
## Introduction
This shipping application is a sample demo to showcase creating a U.S. domestic package shipment, and all the steps to make that shipment. This demo utilizes 4 APIs: OAuth, Address Validation, Rate (with TNT option), and Shipping.

## Getting Started
### Prerequisites
- You will need to have Maven and the Java Development Kit installed.

### Download
- You can either download a local copy or clone the repository:

```sh
git clone https://github.com/UPS-API/java-api-examples.git
```


### Build and run

- Build the project using Maven
```sh
cd <project home>
mvn clean package
```

- Run the project that is generated in the <project home>/target directory.
```sh
java -jar shippingWebApp-x.x.x-SNAPSHOT.jar
```
- In your browser, go to localhost:8080
- The port can be changed the `application.properties` file with `server.port`


## Code Walk Through
This project is built using Spring Boot and Thymeleaf as the template engine.

The main file for handling requests is the `WebController.java` file. In here we handle multiple endpoints (`addressValidationAjax`, `rateAjax`, and `shipAjax`) which make an API request with the corresponding service classes (`AddressValidationService.java`, `RateService.java`, `ShippingService.java`). These service and util classes are taken from the java examples and modified slightly to give back the responses in a json. The json response is then sent back in the endpoint. The frontend uses `fetch` to make the HTTP request call, and on response, will parse and display the proper data / errors.

The `/` and `/application` endpoints are used to display certain html templates found in the `resources/templates` file where `index.html` is the initial login screen, and `application.html` handles the shipment process.

The following is a table of what endpoint the frontend calls, and the corresponding API url the backend calls from that endpoint

|  Browser `fetch` url   | Backend API URL |
| ---------------------- | --------------- |
| `/addressValidationAjax` | `<base_url>/addressvalidation/v1/1` |
| `/rateAjax` | `<base_url>/rating/v1/shop?additionalinfo=timeintransit` |
| `/shipAjax` | `<base_url>/shipments/v1/ship` |

`<base_url>` can be either `https://wwwcie.ups.com` or `https://onlinetools.ups.com`

The first call at line 298 in `application.js` is where we make our first API call, which is to Address Validation.
```javascript
fetch('/addressValidationAjax', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json'
  },
  body: ...
})
  .then(response => response.json())
  .then(...)
```
corresponds to line 84 in `WebController.java` where we see
```java
@PostMapping("/addressValidationAjax")
public ResponseEntity<Response> addressValidationAjax(@RequestBody String addressJson, HttpServletRequest request) {
  ...
}

```
Here we see that The `addressValidationService` calls the API, and then gets the response and sends it back to the frontend. We follow this pattern for each service to avoid form submits and page resets for each API call we make.

After we validate the addresses and get all the package information, we send a request to the Rate API to get available services.

Rate API at line 509:
```javascript
fetch('/rateAjax', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json'
  },
  body: ...
})
  .then(response => response.json())
  .then(...)
```
and line 97
```java
@PostMapping("/rateAjax")
public ResponseEntity<Response> rateAjax(@RequestBody String rateJson, HttpServletRequest request) {
  ...
}
```
After a service is selected and they review (browser side only) the information, they can send a shipment request.

Ship API at line 923:
```javascript
fetch('/shipAjax', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json'
  },
  body: ...
})
  .then(res => res.json())
  .then(...)
```
and line 97
```java
@PostMapping("/shipAjax")
public ResponseEntity<Response> rateAjax(@RequestBody String shippingJson, HttpServletRequest request) {
  ...
}
```

### Data Schema
The JSON Request schemas can be found here:
- [OAuth Schemas](https://developer.ups.com/en-us/catalog/authorization-oauth/view-oauth-client-credentials-spec/securityv1oauthtoken)
- [Address Validation Schemas](https://developer.ups.com/en-us/catalog/address-validate/view-address-validation-spec/addressvalidationversionrequestoption)
- [Rate Schemas](https://developer.ups.com/index.php/en-us/catalog/rating/view-rate-spec/ratingversionrequestoption)
- [Ship Schemas](https://developer.ups.com/en-us/catalog/shipping/view-shipping-spec/shipmentsversionship)


### Extra Notes
- For the Rate API call, we utilize the Time in Transite (TNT) option, so our Rate response has both the regular rate response as well as TNT information for each available service.
- We use the Negotiated Rates Indicator in the Shipment object for both Rate and Shipping calls. If our Shipper Number has negotiated rates set up, you will see total charges in a different place

(Negotiated Rates Indicator) Request
This is where the `NegotiatedRatesIndicator` should go.
```json
"RateRequest": {
  "Shipment": {
    "ShipmentRatingOptions": {
      "NegotiatedRatesIndicator": ""
    }
  }
}
```
(Negotiated Rate Location vs Total Charges Location) Response
In the response, you will see 2 different places for Total Charges. The `ShipmentCharges > TotalCharges` is for non-negotiated rates, and `NegotiatedRateCharges > TotalCharge` is for negotiated rates.
```json
"ShipmentResults": {
  "ShipmentCharges": {
      "TotalCharges": {
          "MonetaryValue": "22.94"
      },
      "TotalChargesWithTaxes": null
  },
  "NegotiatedRateCharges": {
      "TotalCharge": {
          "MonetaryValue": "22.71"
      },
  }
}
```

- This demo is an example of a basic flow of creating a domestic US package shipment. Please refer to the [developer documentation](https://developer.ups.com) for all the request options available for you use. It is important to input as much accurate information as possible to avoid too much difference rate estimated costs, and actual shipping cost.
- In this demo we collect "Shipper" information, but it can be the case that you are the shipper, and you already have that information, so you do not need to have a page or fields for this. Please note that the "Shipper" and "ShipFrom" objects in the request can be different as well.
- Please make sure that the OAuth, Address Validate, Rate, and Shipping APIs are enabled for the client ID / secret pair you use to login, so that all API calls function properly.