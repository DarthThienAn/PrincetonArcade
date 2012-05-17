/* Handles the game logic for Connect Four */

package com.connectfour;

import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;
import com.link.*;

public class ConnectFourView extends ConnectFourTileView {
	/**
	 * Current mode of application: READY to run, RUNNING, or you have already
	 * lost. static final ints are used instead of an enum for performance
	 * reasons.
	 */
	public static final int PAUSE = 0;
	public static final int READY = 1;
	public static final int RUNNING = 2;
	public static final int LOSE = 3;
	public static final int WIN = 4;

	/**
	 * Labels for the drawables that will be loaded into the TileView class
	 */
	public final static int BACK = 1;
	public final static int RED = 2;
	public final static int GREEN = 3;

	private static final long mDelay = 50;

	/**
	 * mLastMove: tracks the absolute time when the Tetris last moved, and is
	 * used to determine if a move should be made based on mMoveDelay.
	 */
	private long mLastMove;

	/**
	 * mStatusText: text shows to the user in some run states
	 */
	private TextView mStatusText;

	private int[][] board;

	private static boolean gameover = false;
	//	private static int mode;
	private static boolean turn;
	private static int lastX = 0;
	private static int lastY = 0;
	private static int winner = 0;




	/**
	 * Create a simple handler that we can use to cause animation to happen. We
	 * set ourselves as a target and we can use the sleep() function to cause an
	 * update/invalidate to occur at a later date.
	 */
	private RefreshHandler mRefreshHandler = new RefreshHandler();

	class RefreshHandler extends Handler {

		@Override
		public void handleMessage(Message msg) {
			ConnectFourView.this.update();
			ConnectFourView.this.invalidate();
		}

		public void sleep(long delayMillis) {
			this.removeMessages(0);
			sendMessageDelayed(obtainMessage(0), delayMillis);
		}
	};

	/**
	 * Constructs a ConnectFourView based on inflation from XML
	 * 
	 * @param context
	 * @param attrs
	 */
	public ConnectFourView(Context context)
	{
		super(context);
	}
	public ConnectFourView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ConnectFourView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initView();
	}


	/**
	 * load drawables
	 */
	private void initView() {
		setFocusable(true);

		Resources r = this.getContext().getResources();

		resetTiles(4);
		loadTile(BACK, r.getDrawable(R.drawable.connectback));
		loadTile(RED, r.getDrawable(R.drawable.acorn_bomb));
		loadTile(GREEN, r.getDrawable(R.drawable.acorn_cut));

		board = new int[xTileNum][yTileNum];

		update();
		clearTiles();
		draw();
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

	public void setTextVisibility(int visibility) {
		mStatusText.setVisibility(visibility);
	}
	
	public void setText(String str) {
		mStatusText.setText(str);
	}	

	public void setTurn(boolean newTurn) {
		turn = newTurn;
	}	

	public boolean getTurn() {
		return turn;
	}	

	/**
	 * Draw stuff
	 * 
	 */
	private void draw() {
		for (int x = 0; x < xTileNum; x++) {
			for (int y = 0; y < yTileNum; y++) {
				if (board[x][y] == 0)
					setTile(BACK, x, y);
				else if (board[x][y] == 1)
					setTile(RED, x, y);
				else if (board[x][y] == 2)
					setTile(GREEN, x, y);
			}
		}
	}

	/**
	 * Handles the basic update loop, checking to see if we are in the running
	 * state, determining if a move should be made, updating the Tetris's
	 * location.
	 */
	public void update() {
		if (!gameover)
		{
			long now = System.currentTimeMillis();

			if (now - mLastMove > mDelay) {
				clearTiles();
				draw();
				mLastMove = now;
			}
		}
		mRefreshHandler.sleep(50);
	}

	public boolean touch(double x, double y)
	{
		if (turn && !gameover)
		{
			int yBlock = yTileNum - 1;
			int xTile = (int) (((x - xOffset) / mTileSize));
			int yTile = (int) (((y - yOffset - barOffset) / mTileSize));

			//			Log.d("XY", xTile + " " + yTile);

			while (checkTile(xTile, yBlock))
				yBlock--;

			yTile = yBlock;

			if (xTile >= xTileNum || xTile < 0)
			{
				Log.d("X", "ERROR");
				return false;
			}
			if (yTile >= yTileNum || yTile < 0)
			{
				Log.d("Y", "ERROR");
				return false;
			}

			lastX = xTile;
			lastY = yTile;
			if(!checkTile(xTile, yTile))
			{
				setTile(RED, xTile, yTile);
				board[xTile][yTile] = (RED - 1);

				turn = false;
				winner = checkGameOver();
				update();

				setText("Waiting for opponent's move.");
				
				return true;
			}
			else
				return false;
		}
		else 
			return false;

	}

	private int checkGameOver()
	{
		for (int color = RED; color < 4; color++)
		{
			//columns
			for (int x = 0; x < xTileNum; x++)
			{
				for (int y = 0; y < (yTileNum - 3); y++)
				{
					if (board[x][y] == (color - 1) && 
							board[x][y+1] == (color - 1) && 
							board[x][y+2] == (color - 1) && 
							board[x][y+3] == (color - 1))
					{
						gameover = true;
						return color;
					}
				}
			}

			//rows
			for (int y = 0; y < yTileNum; y++)
			{
				for (int x = 0; x < (xTileNum - 3); x++)
				{
					if (board[x][y] == (color - 1) && 
							board[x+1][y] == (color - 1) && 
							board[x+2][y] == (color - 1) && 
							board[x+3][y] == (color - 1))
					{
						gameover = true;
						return color;
					}
				}
			}

			//diagonals like a \
			for (int x = 0; x < (xTileNum - 3); x++)
			{
				for (int y = 0; y < (yTileNum - 3); y++)
				{
					if (board[x][y] == (color - 1) && 
							board[x+1][y+1] == (color - 1) && 
							board[x+2][y+2] == (color - 1) && 
							board[x+3][y+3] == (color - 1))
					{
						gameover = true;
						return color;
					}

				}
			}

			//diagonals like a /
			for (int x = xTileNum - 1; x > 2; x--)
			{
				for (int y = 0; y < (yTileNum - 3); y++)
				{
					if (board[x][y] == (color - 1) && 
							board[x-1][y+1] == (color - 1) && 
							board[x-2][y+2] == (color - 1) && 
							board[x-3][y+3] == (color - 1))
					{
						gameover = true;
						return color;
					}

				}
			}

		}


		//no victory
		return 0;
	}

	public boolean getGameOver()
	{
		return gameover;
	}

	public int getWinner()
	{
		return winner;
	}

	public boolean checkTile(int x, int y)
	{
		if (x < 0 || x >= xTileNum)
			return true;
		if (y < 0 || y >= yTileNum)
			return true;

		if(board[x][y] > 0)
			return true;

		return false;
	}

	public void setTileMarked(int color, int x, int y)
	{
		setTile(color, x, y);
		board[x][y] = (GREEN - 1);
		winner = checkGameOver();
	}

	public int getLastX()
	{
		return lastX;
	}

	public int getLastY()
	{
		return lastY;
	}

}