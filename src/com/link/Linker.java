package com.link;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class Linker extends Activity {
	private static final String AUTHCODE = "cos333";

	private final String sendscoreurl = "http://webscript.princeton.edu/~pcao/cos333/sendscore.php";
	private final String updateactivityurl = "http://webscript.princeton.edu/~pcao/cos333/updateactivity.php";
	// game ids
	public static final int SNAKE_ID = 0;
	public static final int SQUIRRELHUNT_ID = 1;
	public static final int TD_ID = 2;
	public static final int TABLES_ID = 3;
	public static final int CONNECT_ID = 4;

	private final int NUM_GAMES = 5;

	private Button button_squirrel;
	private Button button_snake;
	private Button button_lobby;
	private Button button_scores;
	private Button button_td;
	private Button button_tabletop;

	private String netid = "";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.menu);

		button_squirrel = (Button) findViewById(R.id.start_squirrel);
		button_squirrel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setActivity("Playing Squirrel Hunt");

				Intent myIntent = new Intent(Linker.this, com.squirrel.SquirrelMain.class);
				Linker.this.startActivityForResult(myIntent, SQUIRRELHUNT_ID);
			}
		});
		button_snake = (Button) findViewById(R.id.start_snake);
		button_snake.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setActivity("Playing Snake");
				Intent myIntent = new Intent(Linker.this, com.snake.Snake.class);
				Linker.this.startActivityForResult(myIntent, SNAKE_ID);
			}
		});

		button_td = (Button) findViewById(R.id.start_princetontd);
		button_td.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setActivity("Playing Tower Defense");
				Intent myIntent = new Intent(Linker.this, puArcade.princetonTD.main.PrincetonTD.class);
				Linker.this.startActivityForResult(myIntent, TD_ID);
			}
		});
		button_tabletop = (Button) findViewById(R.id.start_tabletop);
		button_tabletop.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//setActivity("Playing Tower Defense");
				Intent myIntent = new Intent(Linker.this, com.Tabletop.GameBaseActivity.class);
				myIntent.putExtra("netid", netid);
				Linker.this.startActivity(myIntent);
			}
		});

		button_scores = (Button) findViewById(R.id.view_highscores);
		button_scores.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) { // get high scores
				//setActivity("");
				Intent myIntent = new Intent(Linker.this, com.link.HighScores.class);
				Linker.this.startActivity(myIntent);
			}
		});

		button_lobby = (Button) findViewById(R.id.returntolobby);
		button_lobby.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setActivity("In Lobby");
				Intent resultIntent = new Intent();
				setResult(Activity.RESULT_OK, resultIntent);
				finish();
			}
		});

		Bundle extras = getIntent().getExtras();
		if(extras !=null)
		{
			netid = extras.getString("netid");
		}
		setActivity("In Game Select");
	}

	public class UpdateActivityViaPHP extends AsyncTask<String, String, String[]> {
		protected String[] doInBackground(String... params) {
			String updateactivityurl;
			String netidIn;
			String activityIn;
			String phoneipIn;

			String[] result = new String[2];


			// get url/login/password from params
			try {
				updateactivityurl = params[0];
				netidIn = params[1];
				activityIn = params[2];
				phoneipIn = params[3];
				result[1] = activityIn;
			} catch (Exception e) {
				e.printStackTrace();
				result[0] = "error";
				return result;
			}
			System.out.println("attempting to update activity with: " + netidIn + ", " + activityIn + ", " + phoneipIn);

			// set up login/password to be posted to PHP
			ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("netid", netidIn));
			nameValuePairs.add(new BasicNameValuePair("activity", activityIn));
			nameValuePairs.add(new BasicNameValuePair("phoneip", phoneipIn));
			nameValuePairs.add(new BasicNameValuePair("auth", AUTHCODE));

			InputStream content;

			// try getting http response
			try {
				HttpClient httpclient = new DefaultHttpClient();
				HttpPost httppost = new HttpPost(updateactivityurl);	        
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
				HttpResponse response = httpclient.execute(httppost);
				HttpEntity entity = response.getEntity();
				content = entity.getContent();
			} catch(Exception e){
				Log.e("log_tag","Error in internet connection " + e.toString());
				result[0] = "error";
				return result;
			}
			System.out.println("post successful");

			// try reading http response
			String output = "";
			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(content,"iso-8859-1"), 8);
				String line;
				while((line = reader.readLine()) != null){
					output += line;
				}
				content.close();
			} catch(Exception e){
				Log.e("log_tag", "Error converting result " + e.toString());
				result[0] = "error";
				return result;
			}
			Log.e("log_tag", "output: " + output);
			result[0] = output;
			return result;
		}
		protected void onPostExecute(String results[]) {
			final String updatesuccess = "yes";
			//final String updatefailure = "error";
			String updateresult = results[0];
			String activity = results[1];
			if (updateresult.equals(updatesuccess)) {
				// success
				System.out.println("updated activity: " + activity);
			} else {
				// failure
				System.out.println("failed to update activity: " + activity);
			}
		}
	}

	private void setActivity(String newActivity) {
		UpdateActivityViaPHP task = new UpdateActivityViaPHP();
		task.execute(new String[] { updateactivityurl, netid, newActivity, getLocalIpAddress() }); // set activity to in lobby
	}

	public String getLocalIpAddress() {
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress()) {
						return inetAddress.getHostAddress().toString();
					}
				}
			}
		} catch (SocketException ex) {
			Log.e("get ip error", ex.toString());
		}
		return null;
	}

	// send score when each game activity ends
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		String gameid;
		setActivity("In Game Select"); // game has ended
		if (requestCode > NUM_GAMES) { return; }
		if (resultCode == RESULT_OK) {
			gameid = String.valueOf(requestCode); // check game id
			Bundle result = data.getExtras();
			if (result != null) {
				String score = String.valueOf(result.getInt("score"));
				// send the score
				SendScoreViaPHP task = new SendScoreViaPHP();
				task.execute(new String[] { sendscoreurl, netid, gameid, score });
			}
		}

	}

	private class SendScoreViaPHP extends AsyncTask<String, String, String[]> {
		@Override
		protected String[] doInBackground(String... params) {
			String sendscoreurl;
			String netidIn;
			String gameidIn;
			String scoreIn;

			String[] result = new String[2];
			// get url/login/password from params
			try {
				sendscoreurl = params[0];
				netidIn = params[1];
				gameidIn = params[2];
				scoreIn = params[3];
				result[1] = gameidIn; 
			} catch (Exception e) {
				e.printStackTrace();
				result[0] = "error";
				return result;
			}
			System.out.println("attempting to send score: " + netidIn + ", " + gameidIn + ", " + scoreIn);

			// set up login/password to be posted to PHP
			ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("netid", netidIn));
			nameValuePairs.add(new BasicNameValuePair("gameid", gameidIn));
			nameValuePairs.add(new BasicNameValuePair("score", scoreIn));
			nameValuePairs.add(new BasicNameValuePair("auth", AUTHCODE));

			InputStream content;

			// try getting http response
			try {
				HttpClient httpclient = new DefaultHttpClient();
				HttpPost httppost = new HttpPost(sendscoreurl);	        
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
				HttpResponse response = httpclient.execute(httppost);
				HttpEntity entity = response.getEntity();
				content = entity.getContent();
			} catch(Exception e){
				Log.e("log_tag","Error in internet connection " + e.toString());
				result[0] = "error";
				return result;
			}
			System.out.println("post successful");

			// try reading http response
			String output = "";
			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(content,"iso-8859-1"), 8);
				String line;
				while((line = reader.readLine()) != null){
					output += line;
				}
				content.close();
			} catch(Exception e){
				Log.e("log_tag", "Error converting result " + e.toString());
				result[0] = "error";
				return result;
			}
			System.out.println(output);
			result[0] = output;
			return result;
		}
		@Override
		// process result of login query (true or false)
		protected void onPostExecute(String results[]) {
			final String sendscoresuccess = "yes"; // output from PHP to match
			//final String sendscorefailure = "error";
			String sendscoreresult = results[0];
			String gameid = results[1];
			if (sendscoreresult.equals(sendscoresuccess)) {
				// send score success!
				System.out.println("score for gameid: " + gameid);
			} else {
				// send score failed!
				System.out.println("send score failed: " + gameid);
			}
		}
	}

	public void setNetid(String netid) {
		this.netid = netid;
	}
}
