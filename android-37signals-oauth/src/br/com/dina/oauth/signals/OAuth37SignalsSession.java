package br.com.dina.oauth.signals;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.Context;

/**
 * Manage access token and user name. Uses shared preferences to store access token
 * and user name.
 * 
 * @author Thiago Locatelli <thiago.locatelli@gmail.com>
 * @author Lorensius W. L T <lorenz@londatiga.net>
 *
 */
public class OAuth37SignalsSession {
	
	private SharedPreferences sharedPref;
	private Editor editor;
	
	private DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
	
	private static final String SHARED = "37Signals_Preferences";
	private static final String API_USERNAME = "username";
	private static final String API_ACCESS_TOKEN = "access_token";
	private static final String API_EXPIRE_TOKEN = "expire_token";
	private static final String API_EXPIRES_AT = "expires_at";
	
	public OAuth37SignalsSession(Context context) {
		sharedPref 	  = context.getSharedPreferences(SHARED, Context.MODE_PRIVATE);		
		editor 		  = sharedPref.edit();
	}
	

	/**
	 * 
	 * @param accessToken
	 * @param expireToken
	 * @param expiresIn
	 * @param username
	 */
	public void storeAccessToken(String accessToken, String expireToken, String expiresAt, String username) {
		editor.putString(API_ACCESS_TOKEN, accessToken);
		editor.putString(API_EXPIRE_TOKEN, expireToken);
		editor.putString(API_EXPIRES_AT, expiresAt);
		editor.putString(API_USERNAME, username);		
		editor.commit();
	}
	
	public void storeAccessToken(String accessToken, String expiresAt) {
		editor.putString(API_ACCESS_TOKEN, accessToken);
		editor.putString(API_EXPIRES_AT, expiresAt);	
		editor.commit();		
	}
	
	public void storeAccessToken(String accessToken) {
		editor.putString(API_ACCESS_TOKEN, accessToken);	
		editor.commit();		
	}
	
	/**
	 * Reset access token and user name
	 */
	public void resetAccessToken() {
		editor.putString(API_ACCESS_TOKEN, null);
		editor.putString(API_EXPIRE_TOKEN, null);
		editor.putString(API_EXPIRES_AT, null);
		editor.putString(API_USERNAME, null);		
		editor.commit();
	}
	
	/**
	 * Get user name
	 * @return User name
	 */
	public String getUsername() {
		return sharedPref.getString(API_USERNAME, null);
	}
	
	/**
	 * Get access token
	 * @return Access token
	 */
	public String getAccessToken() {
		return sharedPref.getString(API_ACCESS_TOKEN, null);
	}
	
	/**
	 * 
	 * @return
	 */
	public String getExpireToken() {
		return sharedPref.getString(API_EXPIRE_TOKEN, null);
	}
	
	/**
	 * 
	 * @return
	 */
	public String getExpiresAt() {
		return sharedPref.getString(API_EXPIRES_AT, null);
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean isAccessTokenExpired() {
		try {			
			Date expDate = formatter.parse(this.getExpiresAt());
			return expDate.before(new Date());
		}
		catch(ParseException e) {
			e.printStackTrace();
		}
		return false;
	}
}