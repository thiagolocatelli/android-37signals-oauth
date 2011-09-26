package br.com.dina.oauth.signals.example;

import java.util.ArrayList;
import java.util.List;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import br.com.dina.oauth.signals.Account;
import br.com.dina.oauth.signals.OAuth37SignalsApp;

public class AccountsActivity extends ListActivity {

	private List<Account> mAccounts;
	private OAuth37SignalsApp mApp;
	private EfficientAdapter mAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list_activity);
		mApp = new OAuth37SignalsApp(this, ApplicationData.CLIENT_ID, ApplicationData.CLIENT_SECRET, ApplicationData.CALLBACK_URL);
		mAccounts = new ArrayList<Account>();
		mAdapter = new EfficientAdapter(this);		
		setListAdapter(mAdapter);
		new doGetAccountList().execute();
	}
	
	
	
    private class doGetAccountList extends AsyncTask<Void, Void, List<Account>> {
    	
		private final ProgressDialog dialog = new ProgressDialog(AccountsActivity.this);
		
		@Override
		protected void onPreExecute() {
			this.dialog.setMessage("Getting Account list...");
			this.dialog.show();
		}
		
		@Override
		protected List<Account> doInBackground(Void... arg0) {
	    	List<Account> accounts = null;
	    	try {
	    		accounts = mApp.getAccountList();
	    	}catch (Exception e) {
				e.printStackTrace();
				Toast.makeText(AccountsActivity.this, "Error while acquiring account list", Toast.LENGTH_SHORT).show();
			}
	    	return accounts;
		}
		
		@Override
		 protected void onPostExecute(List<Account> accounts) {
			if (this.dialog.isShowing()) {
				this.dialog.dismiss();
			}

			if(accounts != null) {
				mAccounts.clear();
				mAccounts.addAll(accounts);
				mAdapter.notifyDataSetChanged();
			}

		}
    }
    
    private class EfficientAdapter extends BaseAdapter {
		
        private LayoutInflater mInflater;
        
        public EfficientAdapter(Context context) {
            mInflater = LayoutInflater.from(context);
        }
        
        public int getCount() {        	
        	return mAccounts.size();
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
        
            ViewHolder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.list_item, null);
                holder = new ViewHolder();
                holder.text = (TextView) convertView.findViewById(R.id.tvTitle);
                holder.description = (TextView) convertView.findViewById(R.id.tvDescription);
                holder.img = (ImageView) convertView.findViewById(R.id.icon);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }   

            Account acc = mAccounts.get(position);           
            holder.text.setText(acc.getName());
            Log.d("AccountsActivity", "product: " + acc.getName());
            holder.description.setText(acc.getUrl());
            holder.img.setImageResource(getLogoId(acc.getProduct()));
            return convertView;
           
        }
        
        private int getLogoId(String product) {
        	if("backpack".equals(product)) {
        		return R.drawable.logo_bp;
        	}
        	else {
        		return R.drawable.logo_hr;
        	}
        }
        
        private class ViewHolder {
            TextView text;
            TextView description;
            ImageView img;
        }
        
        @Override
        public boolean isEnabled(int position) {
        	return true;
        }
    }
    
	
}
