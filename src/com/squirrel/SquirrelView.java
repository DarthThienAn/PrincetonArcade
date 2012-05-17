package com.squirrel;

import com.link.*;
import java.util.Random;

import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * SquirrelView: implementation of Squirrel Hunt
 * Handles all the game logic and stores variables.
 */
public class SquirrelView extends TileView {

	/**
	 * Labels for the drawables that will be loaded into the TileView class
	 */
	public static final int BROWNSQ = 1;
	public static final int REDSQ = 2;
	public static final int RACCOON = 3;
	public static final int TREE = 4;
	public static final int GRAYSQ = 5;

	/**
	 * Labels for difficulty levels
	 */
	public static final int EASY = 1;
	public static final int MEDIUM = 2;
	public static final int HARD = 3;

	private static double currX;
	private static double currY;
	private static int currType = 0;
	private static int range;
	private static boolean gameover = false;
	private static int score = 0;
	private static int maxScore = 0;
	private static int counter = 0;


	/**
	 * mStatusText: text shows to the user in some run states
	 */
	private TextView mStatusText;

	/**
	 * mLastMove: tracks the absolute time when the last Squirrel
	 * spawned, and tells whether or not a new one should spawn.
	 */
	private long mLastMove;

	/** Maximum delay between each Squirrel **/
	private int mDelay = 600;
	/** Keeps track of when the game starts **/
	private long mFirst;
	/** Game length: 30 seconds **/
	private int mTotalTime = 30 * 1000;
	/** score multiplier based on difficulty **/
	private int scoreMultiplier = 10;

	/**
	 * Create a simple handler that we can use to cause animation to happen. We
	 * set ourselves as a target and we can use the sleep() function to cause an
	 * update/invalidate to occur at a later date.
	 */
	private RefreshHandler mRedrawHandler = new RefreshHandler();

	class RefreshHandler extends Handler {

		@Override
		public void handleMessage(Message msg) {
			SquirrelView.this.update();
			SquirrelView.this.invalidate();
		}

		public void sleep(long delayMillis) {
			this.removeMessages(0);
			sendMessageDelayed(obtainMessage(0), delayMillis);
		}
	};

	/**
	 * Constructs a SquirrelView based on inflation from XML
	 * 
	 * @param context
	 * @param attrs
	 */
	public SquirrelView(Context context)
	{
		super(context);
	}
	public SquirrelView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public SquirrelView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initView();
	}

	public void setVis(int visibility)
	{
		this.setVisibility(visibility);
		mStatusText.setVisibility(visibility);
	}

	private void initView() {
		setFocusable(true);

		Resources r = this.getContext().getResources();

		resetTiles(6);
		loadTile(BROWNSQ, r.getDrawable(R.drawable.brown_squirrel));
		loadTile(REDSQ, r.getDrawable(R.drawable.red_squirrel));
		loadTile(RACCOON, r.getDrawable(R.drawable.raccoon));
		loadTile(TREE, r.getDrawable(R.drawable.tree_icon));
		loadTile(GRAYSQ, r.getDrawable(R.drawable.gray_squirrel));


		score = 0;
		counter = 0;
		mFirst = System.currentTimeMillis();
		range = mTileSize;
		mLastMove = System.currentTimeMillis();
		updateWalls();
		drawNew();
	}

	public void reset()
	{
		updateWalls();
		drawNew();

		gameover = false;
		score = 0;
		counter = 0;
		mFirst = System.currentTimeMillis();
		mLastMove = System.currentTimeMillis();

		update();
	}

	/**
	 * Sets the TextView that will be used to give information (such as "Game
	 * Over" to the user.
	 * 
	 * @param newView
	 */
	public void setTextView(TextView newView) {
		mStatusText = newView;
	}

	/**
	 * Updates the text view to the given parameter
	 * @param str: new text to be shown
	 */
	public void setText(String str) {
		mStatusText.setText(str);
	}	

	/**
	 * Calculate score given coordinates of where user presses.
	 * Updates streaks as well.
	 * 
	 * @param x: x value of where user touched screen
	 * @param y: y value of where user touched screen
	 * @return true if user hits, false if miss
	 */
	public boolean touch(double x, double y)
	{
		//hit
		//if the point touched is within (2 * range), return true and create a new point
		if ((Math.abs(x - currX) < (range * 2.0)) && (Math.abs(y - currY) < (range * 2.0)))
		{
			//10 points for a brown squirrel, increment counter
			if (currType == BROWNSQ)
			{
				score += 1 * scoreMultiplier;
				counter++;
			}
			//50 points for a red squirrel, increment counter
			else if (currType == REDSQ)
			{
				score += 5 * scoreMultiplier;
				counter++;
			}
			//100 points for a red squirrel, increment counter
			else if (currType == GRAYSQ)
			{
				score += 10 * scoreMultiplier;
				counter++;
			}
			//-100 points for a skunk, reset counter
			else if (currType == RACCOON)
			{
				score -= 10 * scoreMultiplier;
				if (score < 0)
					score = 0;
				counter = 0;
			}

			//calculate bonus points for streak
			if (counter > 5)
				score += 1 * scoreMultiplier * (counter - 5);

			updateNew();
			return true;
		}
		//miss
		else
		{
			counter = 0;
			return false;
		}
	}

	/**
	 * Handles the basic update loop, 
	 * drawing screen and updating text
	 * updates every mDelay seconds
	 */
	public void update() {
		if (!gameover)
		{
			long now = System.currentTimeMillis();

			if (now - mLastMove > mDelay) {
				if (currType != RACCOON)
					counter = 0;
				clearTiles();
				updateWalls();
				drawNew();
				mLastMove = now;
				setText("Score: " + score);
			}

			//end game
			if (now > mFirst + mTotalTime)
			{
				gameover = true;

				if (score > maxScore)
					maxScore = score;
			}

			if (counter > 5)
				setText("Score: " + score + " - " + counter + " in a row!");

			mRedrawHandler.sleep(0);
		}
		else
		{
			clearTiles();
			updateWalls();
			mStatusText.setText("Game Over! Score: " + score);
		}

	}

	/**
	 * Handles the basic update loop, 
	 * drawing screen and updating text
	 * updates whenever a new squirrel is generated
	 * due to a hit
	 */
	public void updateNew() {
		if (!gameover)
		{
			clearTiles();
			updateWalls();
			drawNew();
			mLastMove = System.currentTimeMillis();
			setText("Score: " + score);

			if (counter > 5)
				setText("Score: " + score + " - " + counter + " in a row!");
		}
		else
		{
			clearTiles();
			updateWalls();
			mStatusText.setText("Game Over! Score: " + score);
		}
	}

	/**
	 * Draw the boundary and border.
	 */
	private void updateWalls() {
		for (int x = 0; x < xTileNum; x++) {
			setTile(TREE, x, 0);
			setTile(TREE, x, yTileNum - 1);
		}
		for (int y = 1; y < xTileNum - 1; y++) {
			setTile(TREE, 0, y);
			setTile(TREE, yTileNum - 1, y);
		}
	}

	/**
	 * Generate and draw a new squirrel.
	 */
	public void drawNew() {
		/* Generate new squirrel randomly */
		int type = 0;
		double rand = Math.random();
		Random randomGen = new Random();

		/* for checking that new squirrel is not in the same spot */
		double oldX = currX;
		double oldY = currY;


		if (rand < 0.05)
			type = GRAYSQ;
		else if (rand < 0.15)
			type = RACCOON;
		else if (rand < 0.3)
			type = REDSQ;
		else
			type = BROWNSQ;

		currType = type;


		/* generate a random integer for the x and y position in the grid */
		int xRandom = randomGen.nextInt(xTileNum - 2) + 1;
		int yRandom = randomGen.nextInt(yTileNum - 2) + 1;

		/* calculate the current x and y position as the center of the tile */
		currX = xOffset + (xRandom * range) + (0.5 * range);
		currY = barOffset + yOffset + (yRandom * range) + (0.5 * range);

		/* make sure the new squirrel isn't the same as the old one */
		while ((oldX == currX) && (oldY == currY))
		{
			xRandom = randomGen.nextInt(xTileNum - 2) + 1;
			yRandom = randomGen.nextInt(yTileNum - 2) + 1;
			currX = xOffset + (xRandom * range) + (0.5 * range);			
			currY = barOffset + yOffset + (yRandom * range) + (0.5 * range);
		}

		/* draw the squirrel */
		setTile(type, xRandom, yRandom);
	}

	/**
	 * Get the current type of squirrel on the field.
	 * @return an int corresponding to Squirrel Types enumerations.
	 */
	public int getType()
	{
		return currType;
	}

	/**
	 * Return the current score of this game.
	 * @return int equal to value of current score of this game.
	 */
	public int getScore()
	{
		return score;
	}

	/**
	 * Return the maximum score from all sessions in this Activity.
	 * @return int equal to value of maximum score during this session.
	 */
	public int getMaxScore() {
		return Math.max(score, maxScore);
	}

	/**
	 * Return the current number of consecutive squirrels caught
	 * @return int equal to value of the current number of consecutive squirrels caught
	 */
	public int getCounter()
	{
		return counter;
	}

	/**
	 * Return whether or not the current game has ended or not
	 * @return true if game has ended, false if still going on.
	 */
	public boolean getGameOver()
	{
		return gameover;
	}

	/**
	 * Externally end the game, set it to game over state.
	 */
	public void setGameOver() {
		gameover = true;
	}

	/**
	 * Externally set the difficulty of the game; 
	 * default is medium difficulty
	 */
	public void setDifficulty (int diff)
	{
		switch (diff)
		{
		case EASY:
			mDelay = 800;
			scoreMultiplier = 5;
			break;
		case MEDIUM:
			mDelay = 600;
			scoreMultiplier = 10;
			break;
		case HARD:
			mDelay = 400;
			scoreMultiplier = 20;
			break;
		default:
			mDelay = 600;
			scoreMultiplier = 10;
			break;
		}
	}
}