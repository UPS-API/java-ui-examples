package com.ups.shippingWebApp.model;

import lombok.Data;


@Data
public class AddressResponse {
	private boolean validAddress;

	private boolean ambiguousAddress;

	private boolean invalidAddress;

	private boolean validated;

}
