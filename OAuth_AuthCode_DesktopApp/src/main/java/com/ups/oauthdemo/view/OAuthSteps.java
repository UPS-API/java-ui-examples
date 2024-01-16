package com.ups.oauthdemo.view;

/**
 * Encapsulates descriptive information of the steps in the OAuth flow
 */
public class OAuthSteps {
	private static final String OAUTH_STEP_ONE = "Opening browser to send customer to login URL";
	private static final String OAUTH_STEP_TWO = "Capturing auth-code from callback from UPS login";
	private static final String OAUTH_STEP_THREE = "Sending auth-code in request to obtain access token";
	private static final String OAUTH_STEP_FOUR = "Sending refresh token in request to obtain new access token";
	private static final String[] steps = {OAUTH_STEP_ONE, OAUTH_STEP_TWO, OAUTH_STEP_THREE, OAUTH_STEP_FOUR};

	public static final String codeTooltip = "GET &#x3C;BASE_URL&#x3E;/security/v1/oauth/authorize?client_id=&#x3C;CLIENT_ID&#x3E;&redirect_uri=&#x3C;REDIRECT_URI&#x3E;&response_type=code";
	public static final String tokenTooltip = "POST &#x3C;BASE_URL&#x3E;/security/v1/oauth/token\nBody:\ngrant_type=authorization_code\ncode=&#x3C;CODE&#x3E;\nredirect_uri=&#x3C;REDIRECT_URI&#x3E;";
	public static final String refreshTooltip = "POST &#x3C;BASE_URL&#x3E;/security/v1/oauth/refresh\nBody:\ngrant_type=refresh_token\nrefresh_token=&#x3C;REFRESH_TOKEN&#x3E;";
	
	/**
	 * Generates a formatted string representation of the steps in the OAuth flow up to a given step
	 * 
	 * @param step the last step to include in the string
	 * @return formatted string representation of steps
	 */
	public static String asList(int step) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < step-1; i++) {
			String newline = i+1 == step-1 ? "" : "\n"; //Don't need a newline for last line
			String line = String.format("%d. %s%s", i+1, OAuthSteps.steps[i], newline);
			builder.append(line);
		}
		return builder.toString();
	}
	
	/**
	 *  Wraps given string in necessary html tags to display as code. Does not check for valid html code.
	 *  
	 * @param str given string
	 * @return formatted string of html code
	 */
	public static String asHtmlCode(String str) {
		return String.format("<html><pre><code>%s</code></pre></html>", str);
	}
}
