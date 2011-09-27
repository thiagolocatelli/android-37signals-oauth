package br.com.dina.oauth.signals.example;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import br.com.dina.oauth.signals.Account;
import br.com.dina.oauth.signals.OAuth37SignalsApp;
import br.com.dina.oauth.signals.OAuth37SignalsApp.OAuthAuthenticationListener;

public class MainActivity extends Activity {

	private OAuth37SignalsApp mApp;
	
	private Button btnConnect;
	private Button btnRefresh;
	private Button btnListAccounts;
	private TextView tvSummary;
	private TextView tvExpires;
	private TextView tvExpires2;
	

	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        mApp = new OAuth37SignalsApp(this, ApplicationData.CLIENT_ID, ApplicationData.CLIENT_SECRET, ApplicationData.CALLBACK_URL);
        mApp.setListener(listener);
        
        tvSummary = (TextView) findViewById(R.id.tvSummary);
        tvExpires = (TextView) findViewById(R.id.tvExpire);
        tvExpires2 = (TextView) findViewById(R.id.tvExpire2);
        btnRefresh = (Button) findViewById(R.id.btnRefresh);
        
        btnListAccounts = (Button) findViewById(R.id.btnListAccounts);
        btnConnect = (Button) findViewById(R.id.btnConnect);
        btnConnect.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View view) {
				if(mApp.hasAccessToken()) {
					final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
					builder.setMessage("Disconnect from 37Signals?")
							.setCancelable(false)
							.setPositiveButton("Yes",
									new DialogInterface.OnClickListener() {
										public void onClick(DialogInterface dialog, int id) {
											mApp.resetAccessToken();
											btnConnect.setText("Connect");
											tvSummary.setText("Not connected");
											tvExpires.setVisibility(View.GONE);
											tvExpires2.setVisibility(View.GONE);
											btnRefresh.setVisibility(View.GONE);
								    		btnListAccounts.setVisibility(View.GONE);
										}
									})
							.setNegativeButton("No",
									new DialogInterface.OnClickListener() {
										public void onClick(DialogInterface dialog, int id) {
											dialog.cancel();
										}
									});
					final AlertDialog alert = builder.create();
					alert.show();					
				}
				else {
					mApp.authorize();
				}
			}
		});
        
        btnRefresh.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View view) {
				
				try {
					mApp.refreshToken();
					Toast.makeText(MainActivity.this, "Token successfully refreshed", Toast.LENGTH_SHORT).show();
				}
				catch (Exception e) {
					e.printStackTrace();
					Toast.makeText(MainActivity.this, "Error while refreshing token", Toast.LENGTH_SHORT).show();
				}
				
			}
		});
        
        
        btnListAccounts.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent i = new Intent(MainActivity.this, AccountsActivity.class);
				startActivity(i);
			}
		});
        
        if(mApp.hasAccessToken()) {
        	tvSummary.setText("Connected as " + mApp.getUserName());
    		tvExpires.setText("Token expires at a " + mApp.getTokenExpirationDate());
    		tvExpires.setVisibility(View.VISIBLE);
    		tvExpires2.setText("Token expired: " + (mApp.isAccessTokenExpired() ? "yes" : "no"));
    		tvExpires2.setVisibility(View.VISIBLE);
    		btnConnect.setText("Disconnect");
    		btnRefresh.setVisibility(View.VISIBLE);
    		btnListAccounts.setVisibility(View.VISIBLE);
        }
        
        
    }
    
    OAuthAuthenticationListener listener = new OAuthAuthenticationListener() {
    	
    	@Override
    	public void onSuccess() {
    		tvSummary.setText("Connected as " + mApp.getUserName());
    		tvExpires.setText("Token expires at a " + mApp.getTokenExpirationDate());
    		tvExpires.setVisibility(View.VISIBLE);
    		tvExpires.setVisibility(View.VISIBLE);
    		tvExpires2.setText("Token expired: " + (mApp.isAccessTokenExpired() ? "yes" : "no"));
    		btnConnect.setText("Disconnect");
    		btnRefresh.setVisibility(View.VISIBLE);
    		btnListAccounts.setVisibility(View.VISIBLE);
    	}
    	
    	@Override
    	public void onFail(String error) {
    		Toast.makeText(MainActivity.this, error, Toast.LENGTH_SHORT).show();
    	}
    };
}