package com.Tabletop;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.link.R;


public class GameBaseActivity extends Activity {
	/* variables related to game state and game logic */
	private GameState g;
	private GameState offG;
	private int playernum;

	/* enumerations for pictures */
	private int p0pic = R.drawable.megusta;;
	private int p1pic = R.drawable.p1button;
	private int p2pic = R.drawable.p2button;

	private String myNetID;
	private String otherNetID;

	/* Password to access database */
	private static final String AUTHCODE = "cos333";

	/* URLs to PHP scripts that access the database */
	private final String sendstateurl = "http://webscript.princeton.edu/~asarwate/sendstate.php";
	private final String getgamesurl = "http://webscript.princeton.edu/~asarwate/getgames.php";
	private final String sendchallengeurl = "http://webscript.princeton.edu/~asarwate/challenge2.php";
	private final String deleteurl = "http://webscript.princeton.edu/~asarwate/delete.php";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tabletophomelayout);
		myNetID = getIntent().getStringExtra("netid");
		g = new GameState();
	}

	/**handles what to do when the DONE! button (id: button) is clicked in the mainlayout:
	 * finalizes move and sends game state to server
	 */
	public void onButtonClicked(View v) {


		if(g.board.isGameOver()) {
			//does it handle if you make the other guy win?
			Toast.makeText(GameBaseActivity.this, g.board.winner() + " is the winner" ,Toast.LENGTH_SHORT).show();
			return;
		}

		//if its not your turn, read from socket to see if a state has been sent from other player
		if(playernum != g.playerturn) {
			Toast.makeText(GameBaseActivity.this, "its not your turn" ,Toast.LENGTH_SHORT).show();	
			return;
		}

		int choiceA = -1;
		int choiceB = -1;
		int validMove = 0;
		//checks to see what spots have been chosen 
		if(g.choice1 != -1) choiceA = g.choice1;
		else if(g.choice2 != -1) choiceA = g.choice2;
		else {
			Toast.makeText(GameBaseActivity.this, "pick something" ,Toast.LENGTH_SHORT).show();
			return;
		}

		if(g.choice1 != -1 && g.choice2 != -1) choiceB = g.choice2;

		//add
		if(g.nUnused == 1) {
			g.board.add(choiceA, g.playerturn);
			g.used[choiceA/4][choiceA%4] = true;
			validMove = 1;
		}

		//capture
		if(g.nUsed == 1 && g.numKicks[g.playerturn] < 3
				&& g.playerturn != g.board.tables[choiceA/4].table[choiceA%4]) {
			g.board.capture(choiceA);
			validMove = 1;
			g.numKicks[g.playerturn]++;
		}

		//swap
		if(g.nUsed == 2 && (g.board.tables[choiceA/4].table[choiceA%4] 
				!= g.board.tables[choiceB/4].table[choiceB%4])
				&& !samePair(g, choiceA, choiceB)) {
			g.board.swap(choiceA, choiceB);
			validMove = 1;
			g.lastPair[0] = choiceA;
			g.lastPair[1] = choiceB;
		}

		if(validMove == 0) {
			Toast.makeText(GameBaseActivity.this, "not a valid move " + choiceA + " " + choiceB ,Toast.LENGTH_SHORT).show();
			return;
		}
		else {
			//Toast.makeText(GameBaseActivity.this, (g.numTurns+1) + " Turn Comleted by " + g.playerturn, Toast.LENGTH_SHORT).show();
			if(g.playerturn == 1) g.playerturn = 2;
			else if(g.playerturn == 2) g.playerturn = 1;
			g.choice1 = -1;
			g.choice2 = -1;
			g.kickmove = false;
			g.nUnused = 0;
			g.nUsed = 0;
			g.numTurns++;
			if(choiceA != -1) g.selected[choiceA] = false;
			if(choiceB != -1) g.selected[choiceB] = false;
		}

		SendStateViaPHP task = new SendStateViaPHP();
		if(g.board.isGameOver()) {
			task.execute(new String[] { sendstateurl, myNetID, otherNetID, g.string()+myNetID });
		}
		else task.execute(new String[] { sendstateurl, myNetID, otherNetID, g.string() });

		if(g.board.isGameOver()) Toast.makeText(GameBaseActivity.this, g.board.winner() + " is the winner" ,Toast.LENGTH_SHORT).show();

	}

	public boolean samePair(GameState game, int a, int b) {
		return ((a == g.lastPair[0] || a == g.lastPair[1]) && (b == g.lastPair[0] || b == g.lastPair[1]));
	}

	/**handles what occurs when the DONE! button (id: offlinebutton) is pressed in the offline layout
	 * finalizes move and switches turn to other player
	 */
	public void offlineDone(View v) {

		if(offG.board.isGameOver()) {
			//does it handle if you make the other guy win?
			Toast.makeText(GameBaseActivity.this, offG.board.winner() + " is the winner" ,Toast.LENGTH_SHORT).show();
			return;
		}


		int choiceA = -1;
		int choiceB = -1;
		int validMove = 0;
		//checks to see what spots have been chosen 
		if(offG.choice1 != -1) choiceA = offG.choice1;
		else if(offG.choice2 != -1) choiceA = offG.choice2;
		else {
			Toast.makeText(GameBaseActivity.this, "pick something" ,Toast.LENGTH_SHORT).show();
			return;
		}

		if(offG.choice1 != -1 && offG.choice2 != -1) choiceB = offG.choice2;

		//add
		if(offG.nUnused == 1) {
			offG.board.add(choiceA, offG.playerturn);
			offG.used[choiceA/4][choiceA%4] = true;
			validMove = 1;
		}

		//capture
		if(offG.nUsed == 1 && offG.numKicks[offG.playerturn] < 3
				&& offG.playerturn != offG.board.tables[choiceA/4].table[choiceA%4]) {
			offG.board.capture(choiceA);
			validMove = 1;
			offG.numKicks[offG.playerturn]++;
		}

		//swap
		if(offG.nUsed == 2 && (offG.board.tables[choiceA/4].table[choiceA%4] 
				!= offG.board.tables[choiceB/4].table[choiceB%4])
				&& !samePair(offG, choiceA, choiceB)) {
			offG.board.swap(choiceA, choiceB);
			validMove = 1;
			offG.lastPair[0] = choiceA;
			offG.lastPair[1] = choiceB;
		}

		if(validMove == 0) {
			Toast.makeText(GameBaseActivity.this, "not a valid move " + choiceA + " " + choiceB ,Toast.LENGTH_SHORT).show();
			return;
		}
		else {
			if(offG.playerturn == 1) offG.playerturn = 2;
			else if(offG.playerturn == 2) offG.playerturn = 1;
			offG.choice1 = -1;
			offG.choice2 = -1;
			offG.kickmove = false;
			offG.nUnused = 0;
			offG.nUsed = 0;
			offG.numTurns++;
			if(choiceA != -1) offG.selected[choiceA] = false;
			if(choiceB != -1) offG.selected[choiceB] = false;
		}

		if(offG.board.isGameOver()) Toast.makeText(GameBaseActivity.this, offG.board.winner() + " is the winner" ,Toast.LENGTH_SHORT).show();


	}


	/** handles what occurs when the Help button (id: instructbutton) is pressed in the homelayout
	 * inflates rules screen
	 */
	public void instruct(View v) {
		setContentView(R.layout.tabletopinstructlayout);
	}

	/**handles what occurs when the Challenge! button (id: sendchallenge) is pressed in the startlayout:
	 * sanitizes input and calls private class to handle challenge
	 */
	public void sendChallenge(View v) {

		final EditText edittext = (EditText) findViewById(R.id.challengee);
		otherNetID = edittext.getText().toString();
		g = new GameState();

		PasswordChecker pwc = new PasswordChecker();

		if(!pwc.check(otherNetID)) {
			Toast.makeText(GameBaseActivity.this, "\"" + otherNetID + "\" is not a valid netID", Toast.LENGTH_SHORT).show();
			return;
		}

		SendChallengeViaPHP task = new SendChallengeViaPHP();
		task.execute(new String[] { sendchallengeurl, myNetID, otherNetID, g.string() });

	}

	/** handles what occurs when the Go! button (id: startbutton) is pressed in the homelayout
	 * inflates game screen with chosen game
	 */
	public void start(View v) {
		setContentView(R.layout.tabletopstartlayout);

		GetGamesViaPHP getlobby = new GetGamesViaPHP();
		getlobby.execute(new String[] { getgamesurl });


	}

	/** handles what occurs when the Back button is pressed in the 
	 * mainlayout and offlinelayout (ids: back, offlineback):
	 * sets view to main menu and calls private class to retrieve in progress games
	 */
	public void onBack(View v) {
		setContentView(R.layout.tabletopstartlayout);

		GetGamesViaPHP getlobby = new GetGamesViaPHP();
		getlobby.execute(new String[] { getgamesurl });
	}

	/** handles what occurs when the Back button (id: losebutton) is pressed in the loselayout
	 * calls private class to update database
	 * calls private class to retrieve games in main menu
	 */
	public void lost(View v) {

		EndGameViaPHP end = new EndGameViaPHP();
		if(playernum == 1) end.execute(new String[] { deleteurl, myNetID, otherNetID });
		if(playernum == 2) end.execute(new String[] { deleteurl, otherNetID, myNetID });

		setContentView(R.layout.tabletopstartlayout);

		GetGamesViaPHP getlobby = new GetGamesViaPHP();
		getlobby.execute(new String[] { getgamesurl });
	}

	/** handles what occurs when the Play Offline (id: playoffline) button is pressed in the startlayout
	 * inflates game board to be played by 2 players
	 * loads an inprogress "offline" game if there is one
	 */
	public void startOffline(View v) {

		GridView gridview;

		if(offG == null)  {
			offG = new GameState();
			setContentView(R.layout.tabletopofflinelayout);

			gridview = (GridView) findViewById(R.id.offlinegridview);
			gridview.setAdapter(new TileAdapter(this));
		}
		else {
			setContentView(R.layout.tabletopofflinelayout);

			Integer[] pics = new Integer[16];

			for(int i = 0; i < 16; i++) {
				if(offG.board.tables[i/4].table[i%4] == 1) pics[i] = p1pic;
				if(offG.board.tables[i/4].table[i%4] == 2) pics[i] = p2pic;
				if(offG.board.tables[i/4].table[i%4] == 0) pics[i] = p0pic;	
			}

			gridview = (GridView) findViewById(R.id.offlinegridview);
			gridview.setAdapter(new TileAdapter(this, pics));
		}

		gridview.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {

				GameTile gt = (GameTile) v;
				gt.setState(offG);
				gt.setIndex(position);
				offG = gt.clickLogic();
			}
		});	
	}

	/**handles what occurs when the Quit button (id: quit) is pressed in the offlinelayout
	 * erases in progress "offline" game
	 * inflates main menu and calls private class to get inprogres games
	 */
	public void quit(View v) {

		offG = null;

		setContentView(R.layout.tabletopstartlayout);

		GetGamesViaPHP getlobby = new GetGamesViaPHP();
		getlobby.execute(new String[] { getgamesurl });	
	}

	/**class that handles sending the game data to the server after a turn
	 * sends a string representation of the GameState class to a PHP script
	 * via HTTP post. 
	 */
	private class SendStateViaPHP extends AsyncTask<String, String, String[]> {
		@Override
		protected String[] doInBackground(String... params) {
			String[] result = new String[2];

			// set up login/password to be posted to PHP
			ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("player1", myNetID));
			nameValuePairs.add(new BasicNameValuePair("player2", otherNetID));
			nameValuePairs.add(new BasicNameValuePair("state", g.string()));
			nameValuePairs.add(new BasicNameValuePair("auth", AUTHCODE));

			InputStream content;

			// try getting http response
			try {
				HttpClient httpclient = new DefaultHttpClient();
				HttpPost httppost = new HttpPost(sendstateurl);	        
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
			result[0] = output;
			return result;
		}
		@Override
		//method useful for debugging
		protected void onPostExecute(String results[]) {
			final String sendscorefailure = "error";
			String sendscoreresult = results[0];
			if(sendscoreresult.equals(sendscorefailure)) {
				Toast.makeText(GameBaseActivity.this, "Error sending move", Toast.LENGTH_SHORT).show();
				return;
			}
		}
	}

	/**class that handles retrieving what games are in progress for the current user
	 * sends query to database for games in progress with PHP script as an intermediary
	 * HTTP post used to communicate with PHP script
	 */
	private class GetGamesViaPHP extends AsyncTask<String, String, String[]> {
		protected String[] doInBackground(String... params) {
			String getgamesurl;

			String[] result = new String[] { "error", "" }; // to be returned

			try {
				getgamesurl = params[0];

			} catch (Exception e) {
				e.printStackTrace();
				result[0] = "error";
				return result;
			}

			ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("auth", AUTHCODE));
			nameValuePairs.add(new BasicNameValuePair("netid", myNetID));

			InputStream content;

			// try getting http response
			try {
				HttpClient httpclient = new DefaultHttpClient();
				HttpPost httppost = new HttpPost(getgamesurl);	        
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
			//final String lobbysuccess = "yes";
			final String lobbyfailure = "error";
			String lobbyresult = results[0];
			String lobbytext = results[1];

			//return if problem getting lobby
			if(lobbyresult.equals(lobbyfailure) || lobbytext.length() < 1) { //this means internet was down: toast and return 
				Toast.makeText(GameBaseActivity.this, "Error Getting Games", Toast.LENGTH_SHORT).show();
				setContentView(R.layout.tabletopstartlayout);	
				return;
			}

			//string formatting for printing
			String[] rows = lobbytext.split("\n");
			final String[][] splitrows = new String[rows.length][4];
			for(int i = 0; i < rows.length; i++) {
				splitrows[i] = rows[i].split(";");
			}
			GameState temp;
			ArrayList<String> displayS = new ArrayList<String>();

			String[] display = new String[rows.length];
			int turn;
			int opturn;
			String opp;
			for(int i = 0; i < rows.length; i++) {
				if(splitrows[i][3].contains(myNetID)) {
					if(splitrows[i][1].equals(myNetID)) display[i] = "waiting for " + splitrows[i][2] + " to confirm your victory";
					else display[i] = "waiting for " + splitrows[i][1] + " to confirm your victory";
					displayS.add(display[i]); 
					continue; //prevents displaying games you've won
				}
				temp = new GameState(splitrows[i][3]);     //sometimes index out of bounds (when mysql fail?)
				if(splitrows[i][1].equals(myNetID)){
					turn = 1;
					opp = splitrows[i][2];
					opturn = 2;
				}
				else{ 
					turn = 2;
					opp = splitrows[i][1];
					opturn = 1;
				}
				display[i] = "("+turn+")" + myNetID + " vs " + "("+opturn+")" + opp + ": ";
				if(temp.playerturn != turn) display[i] += opp + "'s turn";
				else display[i] += "Your turn";
				displayS.add(display[i]); 
			}


			//inflate ListView for games in progress
			setContentView(R.layout.tabletopstartlayout);
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.tabletoplist_item, displayS);
			ListView lv = (ListView) findViewById(R.id.listview);
			lv.setAdapter(adapter);
			int numCh = lv.getChildCount();
			for(int i = 0; i < numCh; i++) {
				TextView tv = (TextView) lv.getChildAt(i);
				tv.setTextColor(Color.BLACK);
			}
			lv.setOnItemClickListener(new OnItemClickListener() {

				public void onItemClick(AdapterView<?> parent, View view,
						final int position, long id) {
					g = new GameState(splitrows[position][3]);
					if(splitrows[position][1].equals(myNetID)) {
						playernum = 1;
						otherNetID = splitrows[position][2];
					}
					if(splitrows[position][2].equals(myNetID)) {
						playernum = 2;
						otherNetID = splitrows[position][1];
					}

					if(splitrows[position][3].contains(otherNetID)) { //if you have lost the game
						setContentView(R.layout.tabletoploselayout);
						Integer[] pics = new Integer[16];

						for(int i = 0; i < 16; i++) {
							if(g.board.tables[i/4].table[i%4] == 1) pics[i] = p1pic;
							if(g.board.tables[i/4].table[i%4] == 2) pics[i] = p2pic;
							if(g.board.tables[i/4].table[i%4] == 0) pics[i] = p0pic;

						}

						GridView gridview = (GridView) findViewById(R.id.losegridview);
						gridview.setAdapter(new TileAdapter(GameBaseActivity.this, pics));
						return;
					}

					setContentView(R.layout.tabletopmainlayout);

					Integer[] pics = new Integer[16];

					for(int i = 0; i < 16; i++) {
						if(g.board.tables[i/4].table[i%4] == 1) pics[i] = p1pic;
						if(g.board.tables[i/4].table[i%4] == 2) pics[i] = p2pic;
						if(g.board.tables[i/4].table[i%4] == 0) pics[i] = p0pic;

					}

					setContentView(R.layout.tabletopmainlayout);
					GridView gridview = (GridView) findViewById(R.id.gridview);
					gridview.setAdapter(new TileAdapter(GameBaseActivity.this, pics));

					gridview.setOnItemClickListener(new OnItemClickListener() {
						public void onItemClick(AdapterView<?> parent, View v, int position, long id) {

							GameTile gt = (GameTile) v;
							gt.setState(g);
							gt.setIndex(position);
							g = gt.clickLogic();
						}
					}); 	
				}
			});
		}
	}

	/** class that handles sending a challenge to another user
	 * inserts row into databse via PHP and HTTP post,
	 * takes challenger to game screen to play first move
	 */
	private class SendChallengeViaPHP extends AsyncTask<String, String, String[]> {
		@Override
		protected String[] doInBackground(String... params) {
			String sendchallengeurl;
			String challenger;
			String challengee;
			String state;

			String[] result = new String[2];
			// get url/login/password from params
			try {
				sendchallengeurl = params[0];
				challenger = params[1];
				challengee = params[2];
				state = params[3];
				result[1] = challengee;  
			} catch (Exception e) {
				e.printStackTrace();
				result[0] = "error";
				return result;
			}

			// set up login/password to be posted to PHP
			ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("challenger", challenger));
			nameValuePairs.add(new BasicNameValuePair("challengee", challengee));
			nameValuePairs.add(new BasicNameValuePair("state", state));
			nameValuePairs.add(new BasicNameValuePair("auth", AUTHCODE));

			InputStream content;

			// try getting http response
			try {
				HttpClient httpclient = new DefaultHttpClient();
				HttpPost httppost = new HttpPost(sendchallengeurl);	        
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

			result[0] = output;
			return result;
		}
		@Override
		//inflates game screen after database update
		protected void onPostExecute(String results[]) {
			final String sendscorefailure = "error";
			String sendscoreresult = results[0];
			System.out.println("challenge output: " + sendscoreresult);

			if(sendscoreresult.equals("not real")) {
				Toast.makeText(GameBaseActivity.this, "Not a User", Toast.LENGTH_SHORT).show();
				return;
			}

			if(sendscoreresult.equals("existing")) {
				Toast.makeText(GameBaseActivity.this, "Already playing this user", Toast.LENGTH_SHORT).show();
				return;
			}

			if(!sendscoreresult.equals(sendscorefailure)) {
				playernum = 1;

				setContentView(R.layout.tabletopmainlayout);

				GridView gridview = (GridView) findViewById(R.id.gridview);
				gridview.setAdapter(new TileAdapter(GameBaseActivity.this));        

				gridview.setOnItemClickListener(new OnItemClickListener() {
					public void onItemClick(AdapterView<?> parent, View v, int position, long id) {

						GameTile gt = (GameTile) v;
						gt.setState(g);
						gt.setIndex(position);
						g = gt.clickLogic();
					}
				});
			}
			else Toast.makeText(GameBaseActivity.this, "Error sending challenge", Toast.LENGTH_SHORT).show();	
		}
	} 

	/** class that handles a completed game for the database 
	 * updates database rows via PHP and HTTP post
	 */
	private class EndGameViaPHP extends AsyncTask<String, String, String[]> {
		@Override
		protected String[] doInBackground(String... params) {
			String delurl;
			String player1;
			String player2;

			String[] result = new String[2];
			// get url/login/password from params
			try {
				delurl = params[0];
				player1 = params[1];
				player2 = params[2];
				result[1] = player1 + player2;  //make this table ID for debugging?
			} catch (Exception e) {
				e.printStackTrace();
				result[0] = "error";
				return result;
			}

			// set up login/password to be posted to PHP
			ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("player1", myNetID));
			nameValuePairs.add(new BasicNameValuePair("player2", otherNetID));
			nameValuePairs.add(new BasicNameValuePair("auth", AUTHCODE));

			InputStream content;

			// try getting http response
			try {
				HttpClient httpclient = new DefaultHttpClient();
				HttpPost httppost = new HttpPost(delurl);	        
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

			result[0] = output;
			return result;
		}
		@Override
		//method for debugging
		protected void onPostExecute(String results[]) {
			return;
		}
	}

	/** handles what occurs when the Back button (id: back) is pressed in the homelayout
	 * inflates rules screen
	 */
	public void back(View view)
	{
		setContentView(R.layout.tabletophomelayout);
	}

}