/* Implements the Challenger's side of the ConnectFour game */

package com.connectfour;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.link.R;

public class ConnectFourChallenger extends Activity implements OnGestureListener {

	Connect connect = new Connect();
	Connect connect2 = new Connect();

	ConnectFourView myConnectFour;

	TextView text;
	ImageButton instructions;
	
	private Button clientButton;
	private Button cancelButton;
	private boolean connected = false;

	private String opponentip = "";

	private boolean gameStart = false;
	private boolean ready = false;
	private boolean ready2 = false;

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

			if(connect2 != null)
				connect2.sendMsg(" ");
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
			//			Log.d("turn", "" + myConnectFour.getTurn());

			if (myConnectFour.getTurn())
			{
				myConnectFour.update();
				if(myConnectFour.touch(e.getX(), e.getY()))
				{
					connect2.sendMsg("" + myConnectFour.getLastX());
					connect2.sendMsg("" + myConnectFour.getLastY());

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
					yString = connect2.getMsg();

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

		setContentView(R.layout.connectfourchallenger);
		text = (TextView) findViewById(R.id.client_text);
		clientButton = (Button) findViewById(R.id.client_challenge);
		clientButton.setOnClickListener(challenge);
		cancelButton = (Button) findViewById(R.id.client_cancel);
		cancelButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (connect != null)
					connect.close();
				Intent resultIntent = new Intent();
				setResult(Activity.RESULT_OK, resultIntent);
				finish();
			}
		});
		// get server ip to connect to
		Bundle extras = getIntent().getExtras();
		if(extras != null) {
			opponentip = extras.getString("opip");
		}

		if (!connected) {
			if (!opponentip.equals(""))
			{
				if(connect.initClient(opponentip))
				{
					connected = true;

					//send challenge
					connect.sendMsg("" + com.link.Linker.CONNECT_ID);

					//text.setText("Waiting for response to invitation");


					// TODO: set onclick listener
				}
				else
					text.setText("Could not connect to that ip address.");
			}
			else
				text.setText("Client Initialization failed");
		}

	}




	private OnClickListener challenge = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (!ready)
			{
				ready = true;

				connect.sendMsg("" + com.link.Linker.CONNECT_ID);
			}

			if (!ready2)
			{
				String input = "";

				if (connect != null)
					input = connect.getMsg();


				if (input == null)
					return;
				else if (input.equals("ready"))
					ready2 = true;
			}

			if (ready && ready2 && !gameStart)
			{
				gameStart = true;

				setContentView(R.layout.connectfourgame);
				instructions = (ImageButton) (findViewById(R.id.connect_instructs));
				instructions.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						instructions.setVisibility(View.INVISIBLE);
						myConnectFour = (ConnectFourView) (findViewById(R.id.game));
						myConnectFour.setTextView((TextView) findViewById(R.id.status));
						myConnectFour.setVisibility(View.VISIBLE);
						myConnectFour.setTextVisibility(View.VISIBLE);
						
						
						myConnectFour.setTurn(true);
						myConnectFour.update();
						myConnectFour.setTurn(false);
						myConnectFour.setText("Game Start. Waiting for opponent's move.");
						myConnectFour.update();

						connect2.initClient(opponentip);
						checkInput();
					}
				});
			}
		}
	};


	private void checkInput()
	{
		if (gameStart && !myConnectFour.getTurn() && !gameover) {
			String input = "";

			if (connect2 != null)
			{
				input = connect2.getMsg();
				if((input != null) && !input.equals(" "))
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

	@Override
	protected void onStop() {
		super.onStop();

		if (connect != null)
			connect.close();
		if (connect2 != null)
			connect2.close();
		//System.exit(0);
	}

	@Override
	protected void onPause() {
		super.onPause();

		if (connect != null)
			connect.close();
		if (connect2 != null)
			connect2.close();
		//System.exit(0);
	}

}
