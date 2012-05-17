package com.link;


import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TabHost;


public class HighScores extends TabActivity {
	private Button backtolobby;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.highscores);
		
		
		Resources res = getResources(); // Resource object to get Drawables
	    TabHost tabHost = getTabHost();  // The activity TabHost
	    TabHost.TabSpec spec;  // Resusable TabSpec for each tab
	    Intent intent;  // Reusable Intent for each tab

	    // Create an Intent to launch an Activity for the tab (to be reused)
	    intent = new Intent().setClass(this, GetScores0Activity.class);

	    // Initialize a TabSpec for each tab and add it to the TabHost
	    spec = tabHost.newTabSpec("snake").setIndicator("Snake",
	                      res.getDrawable(R.drawable.game_icons0))
	                  .setContent(intent);
	    tabHost.addTab(spec);

	    // Do the same for the other tabs
	    intent = new Intent().setClass(this, GetScores1Activity.class);
	    spec = tabHost.newTabSpec("squirrel").setIndicator("Squirrel Hunt",
	                      res.getDrawable(R.drawable.game_icons1))
	                  .setContent(intent);
	    tabHost.addTab(spec);

	    intent = new Intent().setClass(this, GetScores2Activity.class);
	    spec = tabHost.newTabSpec("td").setIndicator("Tower Defense",
	                      res.getDrawable(R.drawable.game_icons2))
	                  .setContent(intent);
	    tabHost.addTab(spec);

	    tabHost.setCurrentTab(0);


		backtolobby = (Button) findViewById(R.id.backtogames);
		backtolobby.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}

	public boolean onKeyDown(int keyCode, KeyEvent msg) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			System.out.println("pressed back!");
			finish();
		}
		return true;
	}
	
}