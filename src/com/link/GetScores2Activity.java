package com.link;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class GetScores2Activity extends Activity {

	private static final int RANK_LENGTH = 10;
	private static final int GAME_ID = 2;

	private ListView lv;

	private static final String AUTHCODE = "cos333";
	private final String scoresurl = "http://webscript.princeton.edu/~pcao/cos333/gethighscores.php";
	private static ArrayList<Score> scoreList;

	private class Score {
		private String netid;
		private int score;
		private int gameid;
		public Score(int gameid, String netid, int score) {
			this.netid = netid;
			this.score = score;
			this.gameid = gameid;
		}
		public String getNetid() {
			return netid;
		}
		public int getScore() {
			return score;
		}
		public int getGameid() {
			return gameid;
		}
	}

	private class GetHighScoresViaPHP extends AsyncTask<String, String, String[]> {
		protected String[] doInBackground(String... params) {
			String getscoresurl;
			//String gameid;

			String[] result = new String[] { "error", "" }; // to be returned

			try {
				getscoresurl = params[0];
				//gameid = params[1];
			} catch (Exception e) {
				e.printStackTrace();
				result[0] = "error";
				return result;
			}

			ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("auth", AUTHCODE));
			//nameValuePairs.add(new BasicNameValuePair("gameid", gameid));

			InputStream content;

			// try getting http response
			try {
				HttpClient httpclient = new DefaultHttpClient();
				HttpPost httppost = new HttpPost(getscoresurl);
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
				HttpResponse response = httpclient.execute(httppost);
				HttpEntity entity = response.getEntity();
				content = entity.getContent();
			} catch(Exception e){
				Log.e("log_tag","Error in internet connection " + e.toString());
				result[0] = "error";
				return result;
			}

			// try reading http response
			String output = "";
			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(content,"utf-8"), 8);
				String line;
				while((line = reader.readLine()) != null){
					output += line + "\n";
				}
				content.close();
			} catch(Exception e){
				Log.e("log_tag", "Error converting result " + e.toString());
				result[0] = "error";
				return result;
			}
			Log.e("log_tag", "output: " + output);
			result[0] = "yes";
			result[1] = output;
			return result;
		}
		protected void onPostExecute(String results[]) { // print lobby results
			String scoresresult = results[0]; // status of the post for debug
			String scorestext = results[1];
			System.out.println("got highscores: " + scorestext);

			String[] scoresdata = scorestext.split(";");
			int[] gameid = new int[scoresdata.length/3];
			String[] netid = new String[scoresdata.length/3];
			int[] score = new int[scoresdata.length/3];
			for (int i = 0; i < scoresdata.length/3; i++)
			{
				gameid[i] = Integer.parseInt(scoresdata[i*3]);
				netid[i] = scoresdata[i*3+1];
				score[i] = Integer.parseInt(scoresdata[i*3+2]);
			}
			scoreList = new ArrayList<Score>();

			for (int i = 0; i < netid.length; i++) {
				scoreList.add(new Score(gameid[i], netid[i], score[i]));
			}

			ArrayList<User> userList = new ArrayList<User>();
			for (int i = 0; i < scoreList.size() ; i++)
			{
				Score s = scoreList.get(i);
				if (s.getGameid() == GAME_ID)
					userList.add(new User(s.getNetid(), s.getScore()));
			}
			Collections.sort(userList);

			ArrayList<User> rankList = new ArrayList<User>();
			for (int i = 0; i < Math.min(RANK_LENGTH,userList.size()); i++)
			{
				rankList.add(userList.get(i));
			}

			lv.setAdapter(new UserAdapter(getApplicationContext(), R.layout.lobby_item, rankList));

			setContentView(lv);
		}
	}


	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		GetHighScoresViaPHP task = new GetHighScoresViaPHP();
		task.execute(scoresurl, "0");

		lv = new ListView(this);
	}

	//-------------------------------------------------------------------------------


	class User implements Comparable<User> {
		private String username;
		private int score;

		public String getName() {
			return username;
		}
		public int getScore() {
			return score;
		}

		public void setName(String name) {
			username = name;
		}
		public void setScore(int score) {
			this.score = score;
		}

		public User(String name, int score) {
			username = name;
			this.score = score;
		}

		@Override
		public int compareTo(User another) {
			return another.getScore() - this.score;
		}
	}

	public class UserAdapter extends ArrayAdapter<User> {
		private ArrayList<User> items;
		private UserViewHolder userHolder;

		private class UserViewHolder {
			TextView name;
			TextView score; 
		}

		public UserAdapter(Context context, int tvResId, ArrayList<User> items) {
			super(context, tvResId, items);
			this.items = items;
		}

		@Override
		public View getView(int pos, View convertView, ViewGroup parent) {
			View v = convertView;
			if (v == null) {
				LayoutInflater vi = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(R.layout.lobby_item, null);
				userHolder = new UserViewHolder();
				userHolder.name = (TextView)v.findViewById(R.id.lobby_name);
				userHolder.score = (TextView)v.findViewById(R.id.lobby_status);
				v.setTag(userHolder);
			} else userHolder = (UserViewHolder)v.getTag(); 

			User user = items.get(pos);

			if (user != null) {
				userHolder.name.setText((pos+1) + ". " + user.getName());
				userHolder.score.setText("" + user.getScore());
			}

			return v;
		}
	}
}
