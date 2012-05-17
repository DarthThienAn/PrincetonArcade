package com.squirrel;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.link.R;

public class SquirrelMain extends Activity implements OnGestureListener {

	private GestureDetector gestureScanner;
	private SquirrelView mySquirrelView;
	/** Instruction screen **/
	private ImageButton instructions;
	/** Buffer for the end of game - allow user to touch twice before restarting. **/
	private boolean touched = false;
	private Button easyMode;
	private Button mediumMode;
	private Button hardMode;
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		gestureScanner = new GestureDetector(this);

		// remove title bar
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.squirrelgame);
		mySquirrelView = (SquirrelView) findViewById(R.id.game);
		mySquirrelView.setTextView((TextView) findViewById(R.id.gametext));
		instructions = (ImageButton) findViewById(R.id.squirrel_instructs);
		instructions.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				easyMode.setVisibility(View.VISIBLE);
				mediumMode.setVisibility(View.VISIBLE);
				hardMode.setVisibility(View.VISIBLE);
				instructions.setVisibility(View.INVISIBLE);
				mySquirrelView.setVis(View.INVISIBLE);
			}
		});
		
		easyMode = (Button) findViewById(R.id.squirrel_easy);
		easyMode.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				easyMode.setVisibility(View.INVISIBLE);
				mediumMode.setVisibility(View.INVISIBLE);
				hardMode.setVisibility(View.INVISIBLE);
				mySquirrelView.setVis(View.VISIBLE);
				mySquirrelView.setDifficulty(SquirrelView.EASY);
				mySquirrelView.reset();
				mySquirrelView.setText("Score: " + mySquirrelView.getScore());
			}
		});
		
		mediumMode = (Button) findViewById(R.id.squirrel_medium);
		mediumMode.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				easyMode.setVisibility(View.INVISIBLE);
				mediumMode.setVisibility(View.INVISIBLE);
				hardMode.setVisibility(View.INVISIBLE);
				mySquirrelView.setVis(View.VISIBLE);
				mySquirrelView.setDifficulty(SquirrelView.MEDIUM);
				mySquirrelView.reset();
				mySquirrelView.setText("Score: " + mySquirrelView.getScore());
			}
		});
		
		hardMode = (Button) findViewById(R.id.squirrel_hard);
		hardMode.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				easyMode.setVisibility(View.INVISIBLE);
				mediumMode.setVisibility(View.INVISIBLE);
				hardMode.setVisibility(View.INVISIBLE);
				mySquirrelView.setVis(View.VISIBLE);
				mySquirrelView.setDifficulty(SquirrelView.HARD);
				mySquirrelView.reset();
				mySquirrelView.setText("Score: " + mySquirrelView.getScore());
			}
		});
		
	}

	/* The following functions implement touch functionality */
	@Override
	public boolean onTouchEvent(MotionEvent me) {
		return gestureScanner.onTouchEvent(me);
	}

	@Override
	public boolean onDown(MotionEvent e) {
		return true;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		return true;
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		return true;
	}

	@Override
	public void onLongPress(MotionEvent e) {
		return;
	}

	@Override
	public void onShowPress(MotionEvent e) {
		return;
	}

	/** Handle user touch input **/
	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		if (mySquirrelView != null)
		{
			if (mySquirrelView.getGameOver())
			{
				mySquirrelView.setText("Game Over! Score: " + mySquirrelView.getScore());

				if (touched) //second touch
				{
					touched = false;
					instructions.setVisibility(View.VISIBLE);
				}
				else //first touch
					touched = true;
			}
			else
			{				
				mySquirrelView.update();
				mySquirrelView.touch(e.getX(), e.getY());
			}
		}
		return true;
	}

	/** Returns the score to the previous activity when a user presses back **/
	public boolean onKeyDown(int keyCode, KeyEvent msg) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			mySquirrelView.setGameOver();
			Intent resultIntent = new Intent();
			resultIntent.putExtra("score", mySquirrelView.getMaxScore());
			System.out.println("sending score of: " + mySquirrelView.getMaxScore());
			setResult(Activity.RESULT_OK, resultIntent);
			finish();
		}
		return true;
	}
}	