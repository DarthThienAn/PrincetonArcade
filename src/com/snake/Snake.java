/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.snake;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;

import com.link.R;

/**
 * Snake: a simple game that everyone can enjoy.
 * 
 * This is an implementation of the classic Game "Snake", in which you control a
 * serpent roaming around the garden looking for apples. Be careful, though,
 * because when you catch one, not only will you become longer, but you'll move
 * faster. Running into yourself or the walls will end the game.
 * 
 */
public class Snake extends Activity {

	private SnakeView mSnakeView;
	private ImageButton snakeInst;
	private boolean started = false;

	//    private static String ICICLE_KEY = "snake-view";

	/**
	 * Called when Activity is first created. Turns off the title bar, sets up
	 * the content views, and fires up the SnakeView.
	 * 
	 */
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.snakegame);
		snakeInst = (ImageButton) findViewById(R.id.snake_instructs);
		snakeInst.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				snakeInst.setVisibility(View.INVISIBLE);
				startgame();
			}
		});

	}
	private void startgame() {
		System.out.println("game initialized");
		started = true;
		mSnakeView = (SnakeView) findViewById(R.id.snake);
		mSnakeView.setTextView((TextView) findViewById(R.id.text));
		mSnakeView.setScoreTextView((TextView) findViewById(R.id.snakescoreTV));
		mSnakeView.setTextViewVisibility(View.VISIBLE);
		mSnakeView.setMode(SnakeView.READY);
		//        if (savedInstanceState == null) {
		//            // We were just launched -- set up a new game
		//        } else {
		//            // We are being restored
		//            Bundle map = savedInstanceState.getBundle(ICICLE_KEY);
		//            if (map != null) {
		//                mSnakeView.restoreState(map);
		//            } else {
		//                mSnakeView.setMode(SnakeView.PAUSE);
		//            }
		//        }        
	}

	//    @Override
	//    public void onSaveInstanceState(Bundle outState) {
	//        //Store the game state
	//        outState.putBundle(ICICLE_KEY, mSnakeView.saveState());
	//    }
	public boolean onKeyDown(int keyCode, KeyEvent msg) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			System.out.println("pressed back!");
			Intent resultIntent = new Intent();
			if (started == false) {
				resultIntent.putExtra("score", 0);
			} else {
				resultIntent.putExtra("score", mSnakeView.getScore());
			}
			setResult(Activity.RESULT_OK, resultIntent);
			finish();
		}
		return true;
	}
}
