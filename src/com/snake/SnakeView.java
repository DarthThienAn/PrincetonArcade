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

import java.util.ArrayList;
import java.util.Random;

import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.link.R;

/**
 * SnakeView: implementation of a simple game of Snake
 * 
 * 
 */
public class SnakeView extends TileView implements OnGestureListener {

	private static final String TAG = "SnakeView";
	private GestureDetector gestureScanner;
	private final int sensitivity = 20;

	/**
	 * Current mode of application: READY to run, RUNNING, or you have already
	 * lost. static final ints are used instead of an enum for performance
	 * reasons.
	 */
	public static final int PAUSE = 0;
	public static final int READY = 1;
	public static final int RUNNING = 2;
	public static final int LOSE = 3;
	public static final int INSTRUCTIONS = 4;
	private int mMode = INSTRUCTIONS;

	/**
	 * Current direction the snake is headed.
	 */
	private int mDirection = NORTH;
	private int mNextDirection = NORTH;
	private static final int NORTH = 1;
	private static final int SOUTH = 2;
	private static final int EAST = 3;
	private static final int WEST = 4;

	/**
	 * Labels for the drawables that will be loaded into the TileView class
	 */
	private static final int SNAKE_TYPE = 0;
	private static final int WALL_TILE = 1;

	// acorn types
	private static final int MAX_TYPES = 11; // update this number as acorns are added
	private static final int REG_ACORN = 2;
	private static final int BOMB_ACORN = 3;
	private static final int SLOW_ACORN = 4;
	private static final int CUT_ACORN = 10;
	private static final int INVIS_ACORN = 11;

	private static final int ACORNS_TO_CUT = 3; // no. of tail acorns removed

	private static final int TAIL_ACORN = 5, INVIS_TAIL = 0;

	private static final int SQUIRREL_LEFT = 6;
	private static final int SQUIRREL_RIGHT = 7;
	private static final int SQUIRREL_UP = 8;
	private static final int SQUIRREL_DOWN = 9;

	/**
	 * mScore: used to track the number of apples captured mMoveDelay: number of
	 * milliseconds between snake movements. This will decrease as apples are
	 * captured.
	 */
	private int mScore = 0;
	private int maxScore = 0; // max score over all games in session
	private int invisCounter = 0; // number of moves before tail becomes visible again
	private long mMoveDelay = 400;
	/**
	 * mLastMove: tracks the absolute time when the snake last moved, and is used
	 * to determine if a move should be made based on mMoveDelay.
	 */
	private long mLastMove;

	/**
	 * mStatusText: text shows to the user in some run states
	 */
	private TextView mStatusText;
	private TextView scoreText;

	/**
	 * mSnakeTrail: a list of Coordinates that make up the snake's body
	 * mAcornList: the secret location of the juicy apples the snake craves.
	 */
	private ArrayList<Coordinate> mSnakeTrail = new ArrayList<Coordinate>();
	private ArrayList<Coordinate> mAcornList = new ArrayList<Coordinate>();

	/**
	 * Everyone needs a little randomness in their life
	 */
	private static final Random RNG = new Random();

	/**
	 * Create a simple handler that we can use to cause animation to happen.  We
	 * set ourselves as a target and we can use the sleep()
	 * function to cause an update/invalidate to occur at a later date.
	 */
	private RefreshHandler mRedrawHandler = new RefreshHandler();

	class RefreshHandler extends Handler {

		@Override
		public void handleMessage(Message msg) {
			SnakeView.this.update();
			SnakeView.this.invalidate();
		}

		public void sleep(long delayMillis) {
			this.removeMessages(0);
			sendMessageDelayed(obtainMessage(0), delayMillis);
		}
	};


	/**
	 * Constructs a SnakeView based on inflation from XML
	 * 
	 * @param context
	 * @param attrs
	 */
	public SnakeView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initSnakeView();
	}

	public SnakeView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initSnakeView();
	}

	private void initSnakeView() {
		setFocusable(true);

		Resources r = this.getContext().getResources();
		// define tiles
		resetTiles(MAX_TYPES + 1);

		loadTile(WALL_TILE, r.getDrawable(R.drawable.tree));

		loadTile(REG_ACORN, r.getDrawable(R.drawable.acorn_reg));
		loadTile(BOMB_ACORN, r.getDrawable(R.drawable.acorn_bomb));
		loadTile(SLOW_ACORN, r.getDrawable(R.drawable.acorn_slow));
		loadTile(CUT_ACORN, r.getDrawable(R.drawable.acorn_cut));
		loadTile(TAIL_ACORN, r.getDrawable(R.drawable.acorn_tail));
		loadTile(INVIS_ACORN, r.getDrawable(R.drawable.acorn_invis));

		loadTile(SQUIRREL_LEFT, r.getDrawable(R.drawable.squirrel_left));
		loadTile(SQUIRREL_RIGHT, r.getDrawable(R.drawable.squirrel_right));
		loadTile(SQUIRREL_UP, r.getDrawable(R.drawable.squirrel_up));
		loadTile(SQUIRREL_DOWN, r.getDrawable(R.drawable.squirrel_down));

		gestureScanner = new GestureDetector(this);
	}
	private boolean checkstate() {
		return mAcornList.size() >= 4;
	}
	private void initNewGame(int DIRECTION) {

		mSnakeTrail.clear();
		mAcornList.clear();


		mSnakeTrail.add(new Coordinate(mXTileCount/2, mYTileCount/2, SNAKE_TYPE));
		mNextDirection = DIRECTION;

		// start with acorns
		addRandomAcorn(rndAcornType());
		addRandomAcorn(rndAcornType());
		addRandomAcorn(rndAcornType());
		addRandomAcorn(rndAcornType());

		mMoveDelay = 400;
		mScore = 0;
	}

//	private int[] coordArrayListToArray(ArrayList<Coordinate> cvec) {
//		int count = cvec.size();
//		int[] rawArray = new int[count * 3];
//		for (int index = 0; index < count; index++) {
//			Coordinate c = cvec.get(index);
//			rawArray[3 * index] = c.x;
//			rawArray[3 * index + 1] = c.y;
//			rawArray[3 * index + 2] = c.type;
//		}
//		return rawArray;
//	}

//	public Bundle saveState() {
//		Bundle map = new Bundle();
//
//		map.putIntArray("mAcornList", coordArrayListToArray(mAcornList));
//		map.putInt("mDirection", Integer.valueOf(mDirection));
//		map.putInt("mNextDirection", Integer.valueOf(mNextDirection));
//		map.putLong("mMoveDelay", Long.valueOf(mMoveDelay));
//		map.putLong("mScore", Long.valueOf(mScore));
//		map.putIntArray("mSnakeTrail", coordArrayListToArray(mSnakeTrail));
//
//		return map;
//	}

//	private ArrayList<Coordinate> coordArrayToArrayList(int[] rawArray) {
//		ArrayList<Coordinate> coordArrayList = new ArrayList<Coordinate>();
//
//		int coordCount = rawArray.length;
//		for (int index = 0; index < coordCount; index += 3) {
//			Coordinate c = new Coordinate(rawArray[index], rawArray[index + 1], rawArray[index + 2]);
//			coordArrayList.add(c);
//		}
//		return coordArrayList;
//	}

//	public void restoreState(Bundle icicle) {
//		setMode(PAUSE);
//
//		mAcornList = coordArrayToArrayList(icicle.getIntArray("mAcornList"));
//		mDirection = icicle.getInt("mDirection");
//		mNextDirection = icicle.getInt("mNextDirection");
//		mMoveDelay = icicle.getLong("mMoveDelay");
//		mScore = icicle.getInt("mScore");
//		mSnakeTrail = coordArrayToArrayList(icicle.getIntArray("mSnakeTrail"));
//	}
	
	public int getScore() {
		return Math.max(mScore, maxScore);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent msg) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			System.out.println("registered back");
			return super.onKeyDown(keyCode, msg);
		}

		if (mMode == INSTRUCTIONS) {
			return super.onKeyDown(keyCode, msg);
		}
		if (mMode == LOSE || mMode == READY) { // start game with any direction key
			System.out.println("trying to start game!");
			switch(keyCode) {
			case KeyEvent.KEYCODE_DPAD_UP:
				initNewGame(NORTH);
				break;
			case KeyEvent.KEYCODE_DPAD_DOWN:
				initNewGame(SOUTH);
				break;
			case KeyEvent.KEYCODE_DPAD_LEFT:
				initNewGame(WEST);
				break;
			case KeyEvent.KEYCODE_DPAD_RIGHT:
				initNewGame(EAST);
				break;
			default:
			}
			setMode(RUNNING);
			update();
			return true;

		}
		if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
			if (mMode == PAUSE) {
				setMode(RUNNING);
				update();
			}
			if (mDirection != SOUTH) {
				mNextDirection = NORTH;
			}
			return true;
		}
		if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
			if (mMode == PAUSE) {
				setMode(RUNNING);
				update();
			}
			if (mDirection != NORTH) {
				mNextDirection = SOUTH;
			}
			return true;
		}

		if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
			if (mMode == PAUSE) {
				setMode(RUNNING);
				update();
			}
			if (mDirection != EAST) {
				mNextDirection = WEST;
			}
			return true;
		}

		if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
			if (mMode == PAUSE) {
				setMode(RUNNING);
				update();
			}
			if (mDirection != WEST) {
				mNextDirection = EAST;
			}
			return true;
		}

		return super.onKeyDown(keyCode, msg);
	}

	public void setTextView(TextView newView) {
		mStatusText = newView;
	}
	public void setScoreTextView(TextView newView) {
		scoreText = newView;
	}
	public void setTextViewVisibility(int visibility) {
		scoreText.setVisibility(visibility);
	}
	public void setMode(int newMode) {
		int oldMode = mMode;
		mMode = newMode;

		if (newMode == RUNNING & oldMode != RUNNING) {
			mStatusText.setVisibility(View.INVISIBLE);
			scoreText.setText("Score: " + mScore);
			update();
			return;
		}

		Resources res = getContext().getResources();
		CharSequence str = "";
		if (newMode == LOSE) {
			str = res.getString(R.string.mode_lose_prefix) + mScore
					+ res.getString(R.string.mode_lose_suffix);
			scoreText.setText("--Game Over!-- Score: " + mScore);
			maxScore = Math.max(maxScore, mScore); // update high score
		} else if (newMode == PAUSE) {
			str = res.getText(R.string.mode_pause);
			scoreText.setText("--Paused-- Score: " + mScore);
		} else if (newMode == READY) {
			str = res.getText(R.string.mode_ready);
			scoreText.setText("Score: " + mScore);
		}

		mStatusText.setText(str);
		mStatusText.setVisibility(View.VISIBLE);
	}

	private int rndAcornType() {
		// generate type randomly
		final double P_BOMB = 0.15;
		final double P_SLOW = 0.20;
		final double P_CUT = 0.15;
		final double P_INVIS = 0.1;
		if (RNG.nextDouble() < P_BOMB) {
			addRandomAcorn(REG_ACORN); // add bomb and regular acorn
			return BOMB_ACORN;
		} else if (RNG.nextDouble() < P_CUT) {
			//addRandomAcorn(REG_ACORN);
			return CUT_ACORN;
		} else if (RNG.nextDouble() < P_SLOW) {
			return SLOW_ACORN;
		} else if (RNG.nextDouble() < P_INVIS) {
			return INVIS_ACORN;
		} else {
			return REG_ACORN;
		}
	}
	private void addRandomAcorn(int newType) {
		Coordinate newAcorn = null;
		boolean found = false;
		while (!found) {
			// Choose a new location for our apple
			int newX = 1 + RNG.nextInt(mXTileCount - 2);
			int newY = 1 + RNG.nextInt(mYTileCount - 2);
			newAcorn = new Coordinate(newX, newY, newType);

			// Make sure it's not already under the snake
			boolean collision = false;
			int snakelength = mSnakeTrail.size();
			for (int index = 0; index < snakelength; index++) {
				if (mSnakeTrail.get(index).equals(newAcorn)) {
					collision = true;
				}
			}
			// check for existing acorn
			for (int i = 0; i < mAcornList.size(); i++) {
				if (mAcornList.get(i).equals(newAcorn)) {
					collision = true;
				}
			}
			found = !collision;
		}
		if (newAcorn == null) {
			Log.e(TAG, "Somehow ended up with a null newCoord!");
		}
		mAcornList.add(newAcorn);
	}


	/**
	 * Handles the basic update loop, checking to see if we are in the running
	 * state, determining if a move should be made, updating the snake's location.
	 */
	public void update() {
		if (mMode == RUNNING) {
			long now = System.currentTimeMillis();

			if (now - mLastMove > mMoveDelay) {
				clearTiles();
				updateWalls();
				updateSnake();
				updateAcorns();
				//checks that the game state is valid, otherwise restart the game
				if (!checkstate())
					initNewGame(mDirection);
				mLastMove = now;
				scoreText.setText("Score: " + mScore);
			}
			mRedrawHandler.sleep(10);
		}
	}

	/**
	 * Draws some walls.
	 * 
	 */
	private void updateWalls() {
		for (int x = 0; x < mXTileCount; x++) {
			setTile(WALL_TILE, x, 0);
			setTile(WALL_TILE, x, mYTileCount - 1);
		}
		for (int y = 1; y < mYTileCount - 1; y++) {
			setTile(WALL_TILE, 0, y);
			setTile(WALL_TILE, mXTileCount - 1, y);
		}
	}

	/**
	 * Draws some acorns.
	 * 
	 */
	private void updateAcorns() {
		for (Coordinate c : mAcornList) {
			setTile(c.type, c.x, c.y);
		}
	}

	private void updateSnake() {

		if (mSnakeTrail.isEmpty())
			mSnakeTrail.add(new Coordinate(mXTileCount/2, mYTileCount/2, SNAKE_TYPE));

		if (invisCounter > 0) { // decrease counter with each move
			invisCounter--;
		}
		boolean growSnake = false;

		// grab the snake by the head
		Coordinate head = mSnakeTrail.get(0);
		Coordinate newHead;

		mDirection = mNextDirection;

		switch (mDirection) {
		case EAST: {
			newHead = new Coordinate(head.x + 1, head.y);
			break;
		}
		case WEST: {
			newHead = new Coordinate(head.x - 1, head.y);
			break;
		}
		case NORTH: {
			newHead = new Coordinate(head.x, head.y - 1);
			break;
		}
		case SOUTH: {
			newHead = new Coordinate(head.x, head.y + 1);
			break;
		}
		default:
			newHead = new Coordinate(1,1); // should never happen
		}

		// Collision detection
		// For now we have a 1-square wall around the entire arena
		if ((newHead.x < 1) || (newHead.y < 1) || (newHead.x > mXTileCount - 2)
				|| (newHead.y > mYTileCount - 2)) {
			setMode(LOSE);
			return;

		}

		// Look for collisions with itself
		int snakelength = mSnakeTrail.size();
		for (int snakeindex = 0; snakeindex < snakelength; snakeindex++) {
			Coordinate c = mSnakeTrail.get(snakeindex);
			if (c.equals(newHead)) {
				setMode(LOSE);
				return;
			}
		}

		// Look for apples

		boolean cutAcorn = false;
				for (int appleindex = 0; appleindex < mAcornList.size(); appleindex++) {
					Coordinate c = mAcornList.get(appleindex);
					if (c.equals(newHead)) {
						mAcornList.remove(c);
						switch (c.type) {
						case BOMB_ACORN:
							setMode(LOSE);
							return;
						case SLOW_ACORN:
							mMoveDelay *= 1.05;
							mScore+=5;
							addRandomAcorn(rndAcornType());
							break;
						case INVIS_ACORN:
							mScore+=10;
							invisCounter += 10;
							addRandomAcorn(rndAcornType());
							break;
						case CUT_ACORN:
							mScore+=10;
							cutAcorn = true;
							addRandomAcorn(rndAcornType());
							break;
						default: // regular acorn
						mMoveDelay *= 0.95;
						mScore++;
						addRandomAcorn(rndAcornType());
						break;

						}
						growSnake = true; // found an acorn
					}
				}

				// push a new head onto the ArrayList and pull off the tail
				mSnakeTrail.add(0, newHead);
				// except if we want the snake to grow
				if (!growSnake) {
					mSnakeTrail.remove(mSnakeTrail.size() - 1);
				}
				if (cutAcorn) {
					for (int i = 0; i < Math.min(mSnakeTrail.size(), ACORNS_TO_CUT); i++) {
						mSnakeTrail.remove(mSnakeTrail.size() - 1);
					}
				}
				int index = 0;
				for (Coordinate c : mSnakeTrail) {
					if (index == 0) { // head
						switch (mDirection) {
						case EAST: {
							setTile(SQUIRREL_RIGHT, c.x, c.y);
							break;
						}
						case WEST: {
							setTile(SQUIRREL_LEFT, c.x, c.y);
							break;
						}
						case NORTH: {
							setTile(SQUIRREL_UP, c.x, c.y);
							break;
						}
						case SOUTH: {
							setTile(SQUIRREL_DOWN, c.x, c.y);
							break;
						}
						}

					} else { // tail
						if (invisCounter == 0) {
							setTile(TAIL_ACORN, c.x, c.y);
						} else {
							setTile(INVIS_TAIL, c.x, c.y);
						}
					}
					index++;
				}

	}

	/**
	 * Simple class containing two integer values and a comparison function.
	 * There's probably something I should use instead, but this was quick and
	 * easy to build.
	 * 
	 */
	private class Coordinate {
		public int x;
		public int y;
		public int type = 0;

		public Coordinate(int newX, int newY, int newType) {
			x = newX;
			y = newY;
			type = newType;
		}
		public Coordinate(int newX, int newY) {
			x = newX;
			y = newY;
		}
//		public void setType(int newType) {
//			type = newType;
//		}
		public boolean equals(Coordinate other) {
			if (x == other.x && y == other.y) {
				return true;
			}
			return false;
		}

		@Override
		public String toString() {
			return "Coordinate: [" + x + "," + y + "]";
		}
	}


	/** implementing touch functionality **/
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

		boolean up = distanceY > sensitivity;
		boolean down =  distanceY < (-1 *sensitivity);
		boolean left = distanceX > sensitivity;
		boolean right = distanceX < (-1 * sensitivity);

		if (mMode == INSTRUCTIONS) {
			return true;
		}

		if (mMode == LOSE || mMode == READY) { // start game with any direction key
			if (up)
				initNewGame(NORTH);
			else if (down)
				initNewGame(SOUTH);
			else if (left)
				initNewGame(WEST);
			else if (right)
				initNewGame(EAST);

			setMode(RUNNING);
			update();
			return true;

		}

		//up
		if (up) {
			if (mMode == PAUSE) {
				setMode(RUNNING);
				update();
			}
			if (mDirection != SOUTH) {
				mNextDirection = NORTH;
			}
			return true;
		}

		//down
		else if (down) {
			if (mMode == PAUSE) {
				setMode(RUNNING);
				update();
			}
			if (mDirection != NORTH) {
				mNextDirection = SOUTH;
			}
			return true;
		}

		//left
		else if (left) {
			if (mMode == PAUSE) {
				setMode(RUNNING);
				update();
			}
			if (mDirection != EAST) {
				mNextDirection = WEST;
			}
			return true;
		}

		//right
		else if (right) {
			if (mMode == PAUSE) {
				setMode(RUNNING);
				update();
			}
			if (mDirection != WEST) {
				mNextDirection = EAST;
			}
			return true;
		}

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

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		return true;
	}

}
