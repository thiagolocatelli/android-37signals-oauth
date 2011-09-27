package br.com.dina.oauth.signals;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import br.com.dina.oauth.signals.OAuth37SignalsDialog.OAuthDialogListener;


/**
 * 
 * @author Thiago Locatelli <thiago.locatelli@gmail.com>
 * @author Lorensius W. L T <lorenz@londatiga.net>
 *
 */
public class OAuth37SignalsApp {
	private OAuth37SignalsSession mSession;
	private OAuth37SignalsDialog mDialog;
	private OAuthAuthenticationListener mListener;
	private ProgressDialog mProgress;
	private String mAuthUrl;
	private String mTokenUrl;
	private String mRefreshTokenUrl;
	private String mAccessToken;
	private String mExpireToken;
	private String mExpiresAt;
	
	/**
	 * Callback url, as set in 'Manage OAuth Costumers' page (https://developer.37signals.com/)
	 */
	public static String CALLBACK_URL = "";
	private static final String AUTH_URL = "https://launchpad.37signals.com/authorization/new?type=web_server";
	private static final String TOKEN_URL = "https://launchpad.37signals.com/authorization/token?type=web_server";	
	private static final String REFRESH_TOKEN_URL = "https://launchpad.37signals.com/authorization/token?type=refresh";	
	private static final String API_URL = "https://launchpad.37signals.com";
	
	private static final String TAG = "37SignalsApi";
	
	public OAuth37SignalsApp(Context context, String clientId, String clientSecret, String callbackUrl) {
		mSession = new OAuth37SignalsSession(context);		
		mAccessToken = mSession.getAccessToken();
		mExpireToken = mSession.getExpireToken();
		CALLBACK_URL = callbackUrl;
		mTokenUrl = TOKEN_URL + "&client_id=" + clientId + "&client_secret=" + clientSecret + "&redirect_uri=" + CALLBACK_URL;
		mRefreshTokenUrl = REFRESH_TOKEN_URL + "&client_id=" + clientId + "&client_secret=" + clientSecret + "&redirect_uri=" + CALLBACK_URL + "&refresh_token=" + mExpireToken;
		mAuthUrl = AUTH_URL + "&client_id=" + clientId + "&redirect_uri=" + CALLBACK_URL;
		
		OAuthDialogListener listener = new OAuthDialogListener() {
			@Override
			public void onComplete(String code) {
				getAccessToken(code);
			}
			
			@Override
			public void onError(String error) {
				mListener.onFail("Authorization failed");
			}
		};
		
		mDialog = new OAuth37SignalsDialog(context, mAuthUrl, listener);
		mProgress = new ProgressDialog(context);		
		mProgress.setCancelable(false);
	}
	
	private void getAccessToken(final String code) {
		mProgress.setMessage("Getting access token ...");
		mProgress.show();
		
		new Thread() {
			@Override
			public void run() {
				Log.i(TAG, "Getting access token");				
				int what = 0;
				
				try {
					URL url = new URL(mTokenUrl + "&code=" + code);					
					Log.i(TAG, "Opening URL " + url.toString());
					
					HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();					
					urlConnection.setRequestMethod("POST");
					urlConnection.setDoInput(true);
					urlConnection.setDoOutput(true);					
					urlConnection.connect();
					
					JSONObject jsonObj = (JSONObject) new JSONTokener(streamToString(urlConnection.getInputStream())).nextValue();
		        	mAccessToken = jsonObj.getString("access_token");
		        	mExpireToken = jsonObj.getString("refresh_token");
		        	mExpiresAt = jsonObj.getString("expires_in");
		        	
		        	Log.i(TAG, "Got access token: " + mAccessToken);
		        	Log.i(TAG, "Got expire token: " + mExpireToken);
		        	Log.i(TAG, "Got token expires in: " + mExpiresAt);
		        	
				} catch (Exception ex) {
					what = 1;
					
					ex.printStackTrace();
				}
				
				mHandler.sendMessage(mHandler.obtainMessage(what, 1, 0));
			}
		}.start();
	}
	
	private void fetchUserName() {
		mProgress.setMessage("Finalizing ...");
		
		new Thread() {
			@Override
			public void run() {
				Log.i(TAG, "Fetching user info");
				int what = 0;
		
				try {
					URL url = new URL(API_URL + "/authorization.json");
					
					Log.d(TAG, "Opening URL " + url.toString());					
					
					HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();					
					urlConnection.setRequestMethod("GET");
					urlConnection.addRequestProperty("Authorization", "Basic " + String.format("Token token=\"%s\"", mAccessToken));
					urlConnection.setDoInput(true);
					urlConnection.setDoOutput(true);					
					urlConnection.connect();					
					String response	= streamToString(urlConnection.getInputStream());
					
					/*
				
					DefaultHttpClient httpclient = new DefaultHttpClient();
					HttpRequestBase httpReq = new HttpGet("https://launchpad.37signals.com/authorization.json");
					BasicHeader authHeader = new BasicHeader("Authorization", String.format("Token token=\"%s\"", mAccessToken));
					Log.i(TAG, "auth header: " + authHeader.toString());	
					httpReq.setHeader(authHeader);
					HttpResponse resp1 = httpclient.execute(httpReq);
					InputStream instream = resp1.getEntity().getContent();
					String response		= streamToString(instream);
					*/
					System.out.println(response);					
					JSONObject jsonObj 	= (JSONObject) new JSONTokener(response).nextValue();
					
					mExpiresAt = jsonObj.getString("expires_at");
					
					JSONObject identity = jsonObj.getJSONObject("identity");
					String lastName = identity.getString("last_name");
					String firstName = identity.getString("first_name");	        
		        	Log.i(TAG, "Got user name: " + firstName + " " + lastName);		        	
		        	mSession.storeAccessToken(mAccessToken, mExpireToken, mExpiresAt, firstName + " " + lastName);
				} catch (Exception ex) {
					what = 1;					
					ex.printStackTrace();
				}
				
				mHandler.sendMessage(mHandler.obtainMessage(what, 2, 0));
			}
		}.start();
	}
	
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.arg1 == 1) {
				if (msg.what == 0) {
					fetchUserName();
				} else {
					mProgress.dismiss();					
					mListener.onFail("Failed to get access token");
				}
			} else {
				mProgress.dismiss();				
				mListener.onSuccess();
			}
		}
	};
	
	public boolean hasAccessToken() {
		return (mAccessToken == null) ? false : true;
	}
	
	public void setListener(OAuthAuthenticationListener listener) {
		mListener = listener;
	}
	
	public String getUserName() {
		return mSession.getUsername();
	}
	
	public String getTokenExpirationDate() {
		return mSession.getExpiresAt();
	}
	
	public void authorize() {
		mDialog.show();
	}
	
	
	public void refreshToken() throws Exception {
		
		try {
			Log.d(TAG, "Refresh token URL " + mRefreshTokenUrl);
			URL url = new URL(mRefreshTokenUrl);
			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();			
			urlConnection.setRequestMethod("POST");
			urlConnection.addRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			urlConnection.setDoInput(true);
			urlConnection.setDoOutput(true);		
			urlConnection.connect();			
			String response = streamToString(urlConnection.getInputStream());
			Log.d(TAG, "Refresh token response " + response);
			JSONObject jsonObj = (JSONObject) new JSONTokener(response).nextValue();
			
        	mAccessToken = jsonObj.getString("access_token");
        	mSession.storeAccessToken(mAccessToken);
			
		} catch (Exception ex) {
			throw ex;
		}
	}
	
	public List<Account> getAccountList()  throws Exception {
		List<Account> accountList = null;
		try {
			URL url = new URL(API_URL + "/authorization.json");
			
			Log.d(TAG, "Account List URL " + url.toString());
			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();					
			urlConnection.setRequestMethod("GET");
			urlConnection.addRequestProperty("Authorization", "Basic " + String.format("Token token=\"%s\"", mAccessToken));
			urlConnection.setDoInput(true);
			urlConnection.setDoOutput(true);					
			urlConnection.connect();					
			String response	= streamToString(urlConnection.getInputStream());	
			Log.d(TAG, "Account list response " + response);
			JSONObject jsonObj = (JSONObject) new JSONTokener(response).nextValue();
			JSONArray accounts = jsonObj.getJSONArray("accounts");
			if(accounts.length() > 0) {
				accountList = new ArrayList<Account>();
				for(int i = 0; i < accounts.length(); i++) {
					JSONObject accObj = accounts.getJSONObject(i);
					Account acc = new Account(accObj.getString("href"),
							accObj.getString("id"),
							accObj.getString("name"),
							accObj.getString("product"));
					accountList.add(acc);
				}
			}			
			
		} catch (Exception ex) {
			throw ex;
		}	
		return accountList;
	}	
	
	private String streamToString(InputStream is) throws IOException {
		String str  = "";
		
		if (is != null) {
			StringBuilder sb = new StringBuilder();
			String line;
			
			try {
				BufferedReader reader 	= new BufferedReader(new InputStreamReader(is));
				
				while ((line = reader.readLine()) != null) {
					sb.append(line);
				}
				
				reader.close();
			} finally {
				is.close();
			}
			
			str = sb.toString();
		}
		
		return str;
	}
	
	public void resetAccessToken() {
		if (mAccessToken != null) {
			mSession.resetAccessToken();		
			mAccessToken = null;
		}
	}
	
	public boolean isAccessTokenExpired() {
		if (mAccessToken != null) {
			return mSession.isAccessTokenExpired();
		}
		return false;
	}
	
	public interface OAuthAuthenticationListener {
		public abstract void onSuccess();
		public abstract void onFail(String error);
	}
}