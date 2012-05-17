/* Implements the Challenged's side of the ConnectFour game */

package com.connectfour;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.View.OnClickListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.TextView;

import com.link.R;

public class ConnectFourChallenged extends Activity implements OnGestureListener {

	Connect connect = new Connect();

	ConnectFourView myConnectFour;

	TextView text;

	ImageButton instructions;
	
	private boolean gameStart = false;
	private boolean gameover = false;


	private GestureDetector gestureScanner;

	/**
	 * Create a simple handler that we can use to cause animation to happen. We
	 * set ourselves as a target and we can use the sleep() function to cause an
	 * update/invalidate to occur at a later date.
	 */
	private RefreshHandler mRefreshHandler = new RefreshHandler();

	class RefreshHandler extends Handler {

		@Override
		public void handleMessage(Message msg) {
			myConnectFour.update();
			if(connect != null)
			{
				connect.sendMsg(" ");
				//Log.d("Server", "Server sent space");
			}

			checkInput();

		}

		public void sleep(long delayMillis) {
			this.removeMessages(0);
			sendMessageDelayed(obtainMessage(0), delayMillis);
		}
	};

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

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		if(myConnectFour != null)
		{
			if (myConnectFour.getTurn())
			{
				myConnectFour.update();
				if(myConnectFour.touch(e.getX(), e.getY()))
				{
					connect.sendMsg("" + myConnectFour.getLastX());
					connect.sendMsg("" + myConnectFour.getLastY());

					if(gameover = myConnectFour.getGameOver())
					{
						if (myConnectFour.getWinner() == ConnectFourView.RED)
							myConnectFour.setText("Game Over: You win");
						else if (myConnectFour.getWinner() == ConnectFourView.GREEN)
							myConnectFour.setText("Game Over: You lose");
					}

					mRefreshHandler.sleep(50);
				}				
			}
		}

		return true;
	}

	public void test(String input) {
		if (gameStart)
		{
			if (input != null)
			{
				int x = Integer.parseInt(input);
				String yString = " ";
				while (yString.equals(" "))
					yString = connect.getMsg();

				int y = Integer.parseInt(yString);

				if (!myConnectFour.checkTile(x, y))
				{
					myConnectFour.setTileMarked(ConnectFourView.GREEN, x, y);
					myConnectFour.setText("Your move.");
				}
			}
		}
	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		gestureScanner = new GestureDetector(this);

		// remove title bar
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		gameStart = true;
		setContentView(R.layout.connectfourgame);

		connect = new Connect();
		connect.initServer();
		
		instructions = (ImageButton) (findViewById(R.id.connect_instructs));
		instructions.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				instructions.setVisibility(View.INVISIBLE);
				myConnectFour = (ConnectFourView) (findViewById(R.id.game));
				myConnectFour.setTextView((TextView) findViewById(R.id.status));
				myConnectFour.setVisibility(View.VISIBLE);
				myConnectFour.setTextVisibility(View.VISIBLE);
				myConnectFour.setTurn(true);
				myConnectFour.setText("Game Start. Your move.");

				mRefreshHandler.sleep(50);
			}
		});
	}

	private void checkInput()
	{
		if (gameStart && !myConnectFour.getTurn() && !gameover) {
			//			text.setText("Waiting for move...");

			String input = " ";

			if (connect != null)
			{
				if((input = connect.getMsg()) != null)
				{
					if(!input.equals(" "))
					{
						test(input);
						myConnectFour.setTurn(true);
						myConnectFour.setText("Your move.");
						if(gameover = myConnectFour.getGameOver())
						{
							if (myConnectFour.getWinner() == ConnectFourView.RED)
								myConnectFour.setText("Game Over: You win");
							else if (myConnectFour.getWinner() == ConnectFourView.GREEN)
								myConnectFour.setText("Game Over: You lose");
						}
					}
				}
			}
			mRefreshHandler.sleep(50);
		}
	}

	@Override
	protected void onStop() {
		super.onStop();

		if (connect != null)
			connect.close();
		//		System.exit(0);
	}

	@Override
	protected void onPause() {
		super.onPause();

		if (connect != null)
			connect.close();
		//		System.exit(0);
	}

}
