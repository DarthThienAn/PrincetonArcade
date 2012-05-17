/*
 * Game Thread
 * Runs all game logic.
 * Initiated by and can only be directly accessed from the Game Display.
 */

package puArcade.princetonTD.main;

import java.util.Enumeration;

import puArcade.princetonTD.creatures.Creature;
import puArcade.princetonTD.exceptions.BlockedPathException;
import puArcade.princetonTD.exceptions.InaccessibleZoneException;
import puArcade.princetonTD.exceptions.NoMoneyException;
import puArcade.princetonTD.game.Game;
import puArcade.princetonTD.game.GameSolo;
import puArcade.princetonTD.maps.Campus;
import puArcade.princetonTD.maps.Map;
import puArcade.princetonTD.players.Player;
import puArcade.princetonTD.players.Team;
import puArcade.princetonTD.towers.Tower;
import puArcade.princetonTD.towers.TowerType;
import puArcade.princetonTD.util.StringResources;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.widget.Scroller;
import android.widget.Toast;

public class GameThread extends Thread {

	/** Indicate whether the surface has been created & is ready to draw */
	private boolean runThread = false;

	private Context context;
	private SurfaceHolder surfaceHolder;

	private Scroller mScroller;
	private GestureDetector mGD;

	private Bitmap mapBM;
	private Bitmap[] towerBM;
	private Bitmap activeCreatureBM;

	private static final int SELECT_RANGE = 2500;	// squared

	private final int WIDTH;
	private final int HEIGHT;

	private int xOrigin;
	private int yOrigin;

	private Game game;
	private Map map;
	private Team team;
	private Player player;

	private Tower activeTower;
	private Tower selectTower;

	// Constructor
	public GameThread(SurfaceHolder surfaceHolder, final int w, final int h, Context context) {

		this.context = context;
		this.surfaceHolder = surfaceHolder;

		// initialize game
		this.game = new GameSolo();
		// initialize map and map resource
		map = new Campus(game);
		mapBM = StringResources.toBitmap(context, map.getBgImage());

		map.initialize();
		WIDTH = map.getWidth();
		HEIGHT = map.getHeight();
		xOrigin = 0;
		yOrigin = 0;

		// set map to game
		game.setMap(map);
		// add teams and players
		team = game.getTeams().get(0);
		player = new Player("Player");
		team.addPlayer(player);

		// set player to game
		game.setMainPlayer(player);
		game.initialize();

		//-------------------------------------
		// Scrolling for screen repositioning
		//-------------------------------------

		mScroller = new Scroller(context);
		mGD = new GestureDetector(context,
				new GestureDetector.SimpleOnGestureListener() {

			@Override
			public boolean onScroll(MotionEvent e1, MotionEvent e2,
					float distanceX, float distanceY) {
				xOrigin -= distanceX;
				if (WIDTH < w)
					xOrigin = Math.max(Math.min(xOrigin,w-WIDTH), 0);
				else
					xOrigin = Math.max(Math.min(xOrigin,0), w-WIDTH);
				yOrigin -= distanceY;
				if (HEIGHT < h)
					yOrigin = Math.max(Math.min(yOrigin,h-HEIGHT), 0);
				else
					yOrigin = Math.max(Math.min(yOrigin,0), h-HEIGHT);
				return true;
			}

			@Override
			public boolean onFling(MotionEvent e1, MotionEvent e2, float vX, float vY) {
				mScroller.fling(xOrigin, yOrigin,
						-(int)vX, -(int)vY, 0, (int)WIDTH, 0, (int)HEIGHT);
				return true;
			}

			@Override
			public boolean onDown(MotionEvent e) {
				if(!mScroller.isFinished() ) { // is flinging
					mScroller.forceFinished(true); // to stop flinging on touch
				}
				return true; // else won't work
			}
		});

		//---------------------------------------------------------
		// Preload tower bitmaps - to improve runtime performance
		//---------------------------------------------------------

		towerBM = new Bitmap[TowerType.getN()];
		for (int i = 0; i < towerBM.length; i++)
		{
			towerBM[i] = StringResources.toBitmap(context, TowerType.getTower(i+1).getImage());
		}
	}

	// set active tower after button press on UI
	public void setActiveTower(Tower tower)
	{
		activeTower = tower;
		selectTower = null;
	}

	// launch wave when a key is pressed
	public boolean doOnKeyDown(int keyCode, KeyEvent event) {
		if (!game.isLaunching())
			game.launchNewWave(player, team);
		else
		{
			CharSequence text = "Current wave still launching!";
			Toast toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
			toast.show();
		}
		return true;
	}

	// when screen is touched
	public void onTouch(MotionEvent event) {

		// reposition screen
		mGD.onTouchEvent(event);

		// add tower if active and possible
		if (activeTower != null && canPlaceTowers())
		{
			int xTouch = (int)event.getX() - activeTower.width()/2;
			int yTouch = (int)event.getY() - activeTower.height()/2;
			if ((xTouch-xOrigin>=0 && xTouch-xOrigin<=WIDTH) && (yTouch-yOrigin>=0 && yTouch-yOrigin<=HEIGHT))
			{
				try
				{
					activeTower.setX(xTouch-xOrigin);
					activeTower.setY(yTouch-yOrigin);
					activeTower.setOwner(player);
					game.addTower(activeTower);
					selectTower = activeTower;
					activeTower = null;
				}
				catch (NoMoneyException e)
				{
					CharSequence text = "Not enough gold!";
					Toast toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
					toast.show();
				}
				catch (BlockedPathException e)
				{
					CharSequence text = "Cannot block path!";
					Toast toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
					toast.show();
				}
				catch (InaccessibleZoneException e)
				{
					CharSequence text = "Cannot place tower there!";
					Toast toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
					toast.show();
				}
			}
		}
		else
		{
			// select closest tower in range
			selectTower = null;
			for(Tower tower : game.getTowers())
			{
				int xTouch = (int)event.getX() - tower.width()/2;
				int yTouch = (int)event.getY() - tower.height()/2;
				int newDist = dist2(xTouch-xOrigin,yTouch-yOrigin,tower.x(),tower.y());
				if (selectTower == null)
				{
					if (newDist < SELECT_RANGE)
						selectTower = tower;
				}
				else
				{
					int oldDist = dist2(xTouch-xOrigin,yTouch-yOrigin,selectTower.x(),selectTower.y());
					if (newDist < oldDist)
					{
						selectTower = tower;
					}
				}
			}
		}
	}
	// return distance squared
	private int dist2(int x1, int y1, int x2, int y2) {
		return (x1-x2)*(x1-x2)+(y1-y2)*(y1-y2);
	}

	// run game?
	public void setRunning(boolean b) {
		this.runThread = b;
	}

	@Override
	public void run() {
		game.start();
		while (runThread) {

			// initiate new canvas to draw to
			Canvas canvas = null;

			try {
				canvas = surfaceHolder.lockCanvas(null);
				synchronized (surfaceHolder) {
					// draw onto canvas
					onDraw(canvas);
				}
			} finally {
				if (canvas != null) {
					// swap and post new canvas to display
					surfaceHolder.unlockCanvasAndPost(canvas);
				}
				try {
					Thread.sleep(25);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	// draw the game (on each refresh)
	private void onDraw(Canvas canvas) {

		// background
		canvas.drawColor(Color.BLACK);
		canvas.drawBitmap(mapBM,xOrigin,yOrigin,null);


		// draw tower range
		if (selectTower != null)
		{
			Paint paint = new Paint();
			paint.setColor(Color.RED);
			paint.setAlpha(100);
			canvas.drawCircle(xOrigin+selectTower.x()+selectTower.width()/2, 
					yOrigin+selectTower.y()+selectTower.height()/2, 
					(float) selectTower.getRange(), paint);
		}


		// animations - not implemented


		// draw towers

		for(Tower tower : game.getTowers())
			drawTower(tower,canvas);


		// draw creatures
		Creature creature;
		Enumeration<Creature> eCreatures = game.getCreatures().elements();
		while(eCreatures.hasMoreElements())
		{
			creature = eCreatures.nextElement();
			drawHealthBar(creature, canvas);
			drawCreature(creature,canvas);
		}

	}

	// draw tower
	private void drawTower(Tower tower, Canvas canvas) {
		canvas.drawBitmap(towerBM[TowerType.getTowerType(tower)-1],xOrigin+tower.x(),yOrigin+tower.y(),null);
	}
	// draw health bar
	private void drawHealthBar(Creature creature, Canvas canvas) {

		int bar = (int) (creature.width() * 1.5);
		int xBar = (int) (creature.x() - (bar - creature.width()) / 2);
		int yBar = (int)(creature.y() + creature.height());

		int left = xOrigin + xBar;
		int top = yOrigin + yBar;
		int right = xOrigin + xBar + bar;
		int bottom = yOrigin + yBar+4;

		Paint paint = new Paint();
		paint.setColor(creature.getOwner().getTeam().getColor());
		canvas.drawRect(left, top, right, bottom, paint);
		paint.setColor(Color.GREEN);
		canvas.drawRect(left+1, top+1, left+1+(int)(creature.getHealth()*(bar - 2)/creature.getHealthMax()), bottom-1, paint);
	}
	// draw creature
	private void drawCreature(Creature creature, Canvas canvas) {

		activeCreatureBM = StringResources.toBitmap(context, creature.getImage());
		canvas.drawBitmap(activeCreatureBM,xOrigin+creature.x(),yOrigin+creature.y(),null);

		if (creature.getCoeffSlow() > 0)
		{
			Bitmap iceBM = StringResources.toBitmap(context, "drawable/ice");
			int left = xOrigin+creature.x();
			int top = yOrigin+creature.y();
			int right = left + creature.width();
			int bottom = top + creature.height();
			canvas.drawBitmap(iceBM,null,(new Rect(left,top,right,bottom)),null);
		}
	}


	//-------------------------------------
	// return game data to game display
	//-------------------------------------

	public int getScore()
	{
		return player.getScore();
	}
	public String getWave()
	{
		return game.getWave().toString();
	}
	public int getLives()
	{
		return player.getTeam().getLives();
	}
	public int getGold()
	{
		return (int) player.getGold();
	}
	public boolean canPlaceTowers()
	{
		return game.getCreatures().isEmpty();
	}

}