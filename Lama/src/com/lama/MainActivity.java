package com.lama;

import java.io.IOException;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;


public class MainActivity extends ActionBarActivity {

	Button btnSend;
	
	TextView txtvId;
	public static final String REGISTRATION = "registration";
	private static final String GCM_PROJECT_ID = "846240793675";
	private static final String TAG ="GCM TEST";
	String regId;
	private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		btnSend=(Button)findViewById(R.id.btnGcm);        
		txtvId=(TextView)findViewById(R.id.txtvGcm);    
		regId=getRegistrationId(MainActivity.this);
		if(checkPlayServices())
			System.out.println("Good");
		{


			if(regId.isEmpty())
			{
				if(isConnectingToInternet())
				{
					registerGcm();
				}else
				{
					Toast.makeText(MainActivity.this, "Check network connectivity", 2).show();
				}
			}
			else
			{
				txtvId.setText("RegId is \n"+regId);
				btnSend.setVisibility(View.VISIBLE);
			}
		}

		btnSend.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ShowEmailDialog();

			}
		});


	}

	private void ShowEmailDialog()
	{		
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setTitle("Enter Email");		

		// Set an EditText view to get user input 
		final EditText input = new EditText(this);
		alert.setView(input);

		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				Editable value = input.getText();
				// Do something with value!
				if(!value.toString().isEmpty()){


					Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
							"mailto",value.toString(), null));
					emailIntent.putExtra(Intent.EXTRA_SUBJECT, "GCM KEY ");
					emailIntent.putExtra(Intent.EXTRA_TEXT, getRegistrationId(MainActivity.this));
					startActivity(Intent.createChooser(emailIntent, "Send email..."));
				}
				else
				{
					Toast.makeText(MainActivity.this, "Enter Email id", 2).show();
				}
			}
		});

		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				// Canceled.
			}
		});

		alert.show();



	}


	/**
	 * Check the device to make sure it has the Google Play Services APK. If
	 * it doesn't, display a dialog that allows users to download the APK from
	 * the Google Play Store or enable it in the device's system settings.
	 */
	private boolean checkPlayServices() {
		int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
		if (resultCode != ConnectionResult.SUCCESS) {
			if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
				GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICES_RESOLUTION_REQUEST).show();
			} else {
				Log.i(TAG, "This device is not supported.");
				finish();
			}
			return false;
		}
		return true;
	}

	public boolean isConnectingToInternet(){
		ConnectivityManager connectivity = (ConnectivityManager) MainActivity.this.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivity != null)
		{
			NetworkInfo[] info = connectivity.getAllNetworkInfo();
			if (info != null)
				for (int i = 0; i < info.length; i++)
					if (info[i].getState() == NetworkInfo.State.CONNECTED)
					{
						return true;
					}

		}
		return false;
	}

	private String getRegistrationId(Context context) {
		final SharedPreferences prefs = getGCMPreferences(context);
		String registrationId = prefs.getString(REGISTRATION, "");
		if (registrationId.isEmpty()) {
			Log.i(TAG, "Registration not found.");
			return "";
		}

		return registrationId;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public void registerGcm() {

		AsyncTask<Void, Void, String> registerTask = new AsyncTask<Void, Void, String>() {

			@Override
			protected String doInBackground( Void... params ) {

				GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance( MainActivity.this );
				String regId = null;
				try {
					regId = gcm.register( GCM_PROJECT_ID );
					Log.i(TAG, "Registration id is"+regId);

				}
				catch (IOException e) {
					Log.e( TAG, "Error Registering GCM", e );
				}

				if ( !TextUtils.isEmpty( regId ) ) {
					storeRegistrationId(MainActivity.this,regId);
				}
				return regId;
			}

			@Override
			protected void onPostExecute( String result ) {

				super.onPostExecute( result );
				Log.i( TAG, "Scheduling next interval..." );
				if(result!=null)
				{
					txtvId.setText("RegId is \n"+result);
					btnSend.setVisibility(View.VISIBLE);
				}

			}
		};
		registerTask.execute();
	}

	/**
	 * Stores the registration ID and app versionCode in the application's
	 * {@code SharedPreferences}.
	 *
	 * @param context application's context.
	 * @param regId registration ID
	 */
	private void storeRegistrationId(Context context, String regId) {
		final SharedPreferences prefs = getGCMPreferences(context);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(REGISTRATION, regId);
		editor.commit();
	}


	/**
	 * @return Application's {@code SharedPreferences}.
	 */
	private SharedPreferences getGCMPreferences(Context context) {
		// This sample app persists the registration ID in shared preferences, but
		// how you store the regID in your app is up to you.
		return getSharedPreferences(MainActivity.class.getSimpleName(),
				Context.MODE_PRIVATE);
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
