package com.ups.oauthdemo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ups.oauthdemo.view.GUI;

@RestController
public class CodeController {
	
	@Autowired
	AuthModel authModel;
	
	@Autowired
	GUI gui;
	
	/**
	 * Listen on the localhost port specified by your redirect URI.
	 * Extract authorization code from the request parameters and updates AuthModel
	 */
	@GetMapping("/")
	public String getCode(@RequestParam("code") String code) {
		gui.updateStatus(3);
		authModel.setCode(code);
		gui.updateView(authModel);

		return "Return to OAuth Desktop App";
	}

}