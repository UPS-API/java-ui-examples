package com.ups.shippingWebApp.model;

import lombok.Data;


@Data
public class Response {
	private int statusCode;

	private String statusMsg;

	private Object response;

	private ErrorResponse errorResponse;

}
