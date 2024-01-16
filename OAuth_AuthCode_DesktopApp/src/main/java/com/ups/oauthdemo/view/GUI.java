package com.ups.oauthdemo.view;
import javax.swing.JButton;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URISyntaxException;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;

import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

import com.ups.oauthdemo.AuthModel;
import com.ups.oauthdemo.AuthService;
import com.ups.oauthdemo.view.components.StyledButton;
import com.ups.oauthdemo.view.components.StyledTextArea;

@Component
public class GUI extends JFrame {
	private static final long serialVersionUID = 1L;
	private JTextArea codeText;
	private JTextArea tokenText;
	private JTextArea refreshText;
	private JTextArea statusText;
	private JTextArea idText;
	private JTextArea secretText;
	
	private JButton codeButton;
	private JButton tokenButton;
	private JButton refreshButton;

	@Autowired
	private AuthService authService;
	
	@Autowired
	private AuthModel authModel;
	
	public GUI() {
		setTitle("OAuth Desktop App");

        /* Components */
        //Status text field
        statusText = new StyledTextArea();
		JScrollPane statusScrollPane = new JScrollPane(statusText);
		statusScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        statusText.setRows(4);
        
        //Code text field
        JLabel codeLabel = new JLabel("Code: ");
        codeLabel.setFont(new Font(Font.DIALOG, Font.BOLD, 20));
        codeText = new StyledTextArea();
		codeText.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
		JScrollPane codeScrollPane = new JScrollPane(codeText);
		codeScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        
        //Token text field
        JLabel tokenLabel = new JLabel("Token: ");
        tokenLabel.setFont(new Font(Font.DIALOG, Font.BOLD, 20));
        tokenText = new StyledTextArea();
		tokenText.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
		JScrollPane tokenScrollPane = new JScrollPane(tokenText);
		tokenScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		
        //refresh token text field
        JLabel refreshLabel = new JLabel("Refresh Token: ");
        refreshLabel.setFont(new Font(Font.DIALOG, Font.BOLD, 20));
        refreshText = new StyledTextArea();
		refreshText.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
		JScrollPane refreshScrollPane = new JScrollPane(refreshText);
		refreshScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		
		//Client ID text field
		JLabel idLabel = new JLabel("Client ID");
		idLabel.setFont(new Font(Font.DIALOG, Font.BOLD, 20));
		idText = new StyledTextArea();
		JScrollPane idScrollPane = new JScrollPane(idText);
		idScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		
		//Client Secret text field
		JLabel secretLabel = new JLabel("Client Secret");
		secretLabel.setFont(new Font(Font.DIALOG, Font.BOLD, 20));
		secretText = new StyledTextArea();
		JScrollPane secretScrollPane = new JScrollPane(secretText);
		secretScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		
		//Code button
		codeButton = new StyledButton("Fetch Code");
		codeButton.setEnabled(false);
		codeButton.setToolTipText(OAuthSteps.asHtmlCode(OAuthSteps.codeTooltip));
		codeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateStatus(2);
				authModel.deleteCode();
				authModel.deleteToken();
				authModel.deleteRefreshToken();
				try {
					authService.openLoginPage();
				} catch (IOException err) {
					showError(err.getMessage());
					err.printStackTrace();
				} catch (URISyntaxException err) {
					showError(err.getMessage());
					err.printStackTrace();
				} finally {
					updateView(authModel);
				}
			}
		});
		
		//Token button
		tokenButton = new StyledButton("Fetch Token");
		tokenButton.setEnabled(false);
		tokenButton.setToolTipText(OAuthSteps.asHtmlCode(OAuthSteps.tokenTooltip));
		tokenButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateStatus(4);
				authModel.deleteToken();
				authModel.deleteRefreshToken();
				updateView(authModel);
				try {
					authService.generateToken(); // handles api call and updates model
				} catch (RestClientException err) {
					showError(err.getMessage());
					err.printStackTrace();
				} catch (JSONException err) {
					showError(err.getMessage());
					err.printStackTrace();
				} finally {
					updateView(authModel);
				}
			}
		});

		//Refresh button
		refreshButton = new StyledButton("Refresh Token");
		refreshButton.setEnabled(false);
		refreshButton.setToolTipText(OAuthSteps.asHtmlCode(OAuthSteps.refreshTooltip));
		refreshButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				tokenText.setText("");
				refreshText.setText("");
				updateStatus(5);
				try {
					authService.refreshToken();
					updateView(authModel);
				} catch (JSONException err) {
					updateStatus(4);
					showError(err.getMessage());
					err.printStackTrace();
				} catch (RestClientException err) {
					updateStatus(4);
					showError(err.getMessage());
					err.printStackTrace();
				}
			}
		});
		
		//Edit buttons
		JButton editIdButton = new StyledButton("Edit");
		editIdButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				editClientId();
			}
		});
		
		JButton editSecretButton = new StyledButton("Edit");
		editSecretButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				editClientSecret();
			}
		});
		
		/* Layout */
		JPanel basePanel = new JPanel();
        basePanel.setLayout(new BorderLayout());
        basePanel.setBorder(new EmptyBorder(10,10,10,10));
        
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.X_AXIS));
        headerPanel.setBorder(new EmptyBorder(10,10,10,10));
        headerPanel.setOpaque(false);

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new GridLayout(0,3));
        centerPanel.setOpaque(false);
        
        JPanel footerPanel = new JPanel();
        footerPanel.setLayout(new GridLayout(2,0));
        footerPanel.setOpaque(false);
        
        JPanel codePanel = new JPanel();
        codePanel.setLayout(new BorderLayout());
        codePanel.setOpaque(false);
        codePanel.setBorder(new EmptyBorder(10,10,10,10));
        
        JPanel tokenPanel = new JPanel();
        tokenPanel.setLayout(new BorderLayout());
        tokenPanel.setOpaque(false);
        tokenPanel.setBorder(new EmptyBorder(10,10,10,10));
        
        JPanel refreshPanel = new JPanel();
        refreshPanel.setLayout(new BorderLayout());
        refreshPanel.setOpaque(false);
        refreshPanel.setBorder(new EmptyBorder(10,10,10,10));
        
        JPanel idPanel = new JPanel();
        idPanel.setLayout(new BorderLayout());
        idPanel.setOpaque(false);
        idPanel.setBorder(new EmptyBorder(10,10,10,10));
        
        JPanel secretPanel = new JPanel();
		secretPanel.setLayout(new BorderLayout());
		secretPanel.setOpaque(false);
		secretPanel.setBorder(new EmptyBorder(10,10,10,10));
		
		/* Connecting parts */
		headerPanel.add(statusScrollPane);
		
		codePanel.add(codeLabel, BorderLayout.PAGE_START);
		codePanel.add(codeScrollPane, BorderLayout.CENTER);
		codePanel.add(codeButton, BorderLayout.PAGE_END);
		
		tokenPanel.add(tokenLabel, BorderLayout.PAGE_START);
		tokenPanel.add(tokenScrollPane, BorderLayout.CENTER);
		tokenPanel.add(tokenButton, BorderLayout.PAGE_END);
		
		refreshPanel.add(refreshLabel, BorderLayout.PAGE_START);
		refreshPanel.add(refreshScrollPane, BorderLayout.CENTER);
		refreshPanel.add(refreshButton, BorderLayout.PAGE_END);
		
		centerPanel.add(codePanel);
		centerPanel.add(tokenPanel);
		centerPanel.add(refreshPanel);
		
		idPanel.add(idLabel, BorderLayout.PAGE_START);
		idPanel.add(idScrollPane, BorderLayout.CENTER);
		idPanel.add(editIdButton, BorderLayout.LINE_END);
		
		secretPanel.add(secretLabel, BorderLayout.PAGE_START);
		secretPanel.add(secretScrollPane, BorderLayout.CENTER);
		secretPanel.add(editSecretButton, BorderLayout.LINE_END);
		
		footerPanel.add(idPanel);
		footerPanel.add(secretPanel);
		
		basePanel.add(headerPanel, BorderLayout.PAGE_START);
		basePanel.add(centerPanel, BorderLayout.CENTER);
		basePanel.add(footerPanel, BorderLayout.PAGE_END);
		
		basePanel.setBackground(new Color(223, 219, 215));

		add(basePanel);
		setSize(900,600);
		setVisible(true);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
	}
	
	public void updateView(AuthModel model) {
		if (model == null) {
			return;
		}
		
		codeText.setText(model.getCode());
		tokenText.setText(model.getToken());
		refreshText.setText(model.getRefreshToken());
		idText.setText(model.getClientID());
		secretText.setText(model.getClientSecret());
		
		//Show buttons only if necessary data exists
		codeButton.setEnabled(model.hasClientID() && model.hasClientSecret());
		tokenButton.setEnabled(model.hasCode());
		refreshButton.setEnabled(model.hasToken() && model.hasRefreshToken());
	}
	
	public void showError(String message) {
		if (message == null) {
			return;
		}
		
		statusText.setText("Error:\n" + message);
		statusText.setVisible(true);
		bringToFront();
	}
	
	/**
	 * Workaround to brings GUI window to the front of the screen. Minimizes and immediately reopens the window
	 */
	public void bringToFront() {
		if (!isActive()) {
			setState(JFrame.ICONIFIED);
			setState(JFrame.NORMAL);
		}
	}
	
	public void updateStatus(int status) {
		statusText.setText(OAuthSteps.asList(status));
		bringToFront();
	}
	
	public void inputClientCredentials() {
		String clientID = JOptionPane.showInputDialog(this, "Please enter your Client ID");
		String clientSecret = JOptionPane.showInputDialog(this, "Please enter your Client Secret");
		
		if (clientID != null) authModel.setClientID(clientID.trim());
		if (clientSecret != null) authModel.setClientSecret(clientSecret.trim());

		updateView(authModel);
	}
	
	private void editClientId() {
		String newID = JOptionPane.showInputDialog(this, "Please enter your Client ID");
		if (newID != null) authModel.setClientID(newID.trim());
		updateView(authModel);
	}
	
	private void editClientSecret() {
		String newSecret = JOptionPane.showInputDialog(this, "Please enter your Client Secret");
		if (newSecret != null) authModel.setClientSecret(newSecret.trim());
		updateView(authModel);
	}
	
}
