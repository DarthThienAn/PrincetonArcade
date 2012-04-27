package puArcade.princetonTD.creatures;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.NoSuchElementException;
import java.util.Vector;

import puArcade.princetonTD.exceptions.PathNotFoundException;
import puArcade.princetonTD.game.Game;
import puArcade.princetonTD.players.Player;
import puArcade.princetonTD.players.Team;



import android.graphics.Point;
import android.graphics.Rect;

public class CreatureManager implements Runnable {

	private static final long WAIT_TIME = 50; // ms
	private static final int MARGIN = 5; // pixel

	private Vector<Creature> creatures = new Vector<Creature>();
	private boolean inManagement;
	private boolean paused = false;
	private Object pause = new Object();
	private Game game;

	// Constructor
	public CreatureManager(Game game)
	{
		this.game = game;
	}

	// start
	public void start()
	{
		Thread thread = new Thread(this);
		thread.start();
	}

	// add creature
	public void addCreature(Creature creature)
	{
		if (creature == null)
			throw new IllegalArgumentException("Invalid creature");

		creatures.add(creature);
	}

	// remove creature
	public void removeCreature(Creature creature)
	{
		if (creature != null)
			creatures.remove(creature);  
	}

	public void run()
	{
		inManagement = true;

		ArrayList<Creature> deleteCreatures = new ArrayList<Creature>();
		Creature creature;

		while(inManagement)
		{ 
			try
			{

				Enumeration<Creature> eCreatures = creatures.elements();
				while(eCreatures.hasMoreElements())
				{
					creature = eCreatures.nextElement();

					creature.erase();	// multi-player

					if(creature.death())
						deleteCreatures.add(creature);
					else
						creature.action((long)(WAIT_TIME*game.getCoeffSpeed()));
				}
			}
			catch(NoSuchElementException nse)
			{
				System.err.println("Creature not found");
			}

			// delete creatures
			for(Creature deleteCreature : deleteCreatures)
				creatures.remove(deleteCreature);
			deleteCreatures.clear();

			// pause
			try
			{
				synchronized (pause)
				{
					if(paused)
						pause.wait();
				} 

				Thread.sleep(WAIT_TIME);
			} 
			catch (InterruptedException e){
				e.printStackTrace();
			}
		}
	}

	// stop manager
	public void stopCreatures()
	{
		inManagement = false;
	}

	// return managed creatures
	@SuppressWarnings("unchecked")
	public Vector<Creature> getCreatures()
	{
		return (Vector<Creature>) creatures.clone();
	}

	// pause
	public void pause()
	{
		paused = true;
	}

	// unpause
	public void unpause()
	{ 
		synchronized (pause)
		{
			paused = false;
			pause.notifyAll();
		}
	}

	// return creatures in rectangle
	public Vector<Creature> getCreaturesInRectangle(Rect rectangle)
	{
		Vector<Creature> creaturesIn = new Vector<Creature>();

		try
		{
			Creature creature;
			Enumeration<Creature> eCreatures = creatures.elements();
			while(eCreatures.hasMoreElements())
			{
				creature = eCreatures.nextElement();

				if(creature.intersects(rectangle))
					creaturesIn.add(creature);
			}
		}
		catch(NoSuchElementException nse)
		{
			System.err.println("Creature not found");
		}

		return creaturesIn;
	}


	// return creatures in circle
	public Vector<Creature> getCreaturesInCircle(int x, int y, double radius)
	{
		Vector<Creature> creaturesIn = new Vector<Creature>();

		try
		{
			Creature creature;
			Enumeration<Creature> eCreatures = creatures.elements();
			while(eCreatures.hasMoreElements())
			{
				creature = eCreatures.nextElement();

				Point pCreature = new Point((int)creature.centerX(), 
						(int)creature.centerY());
				Point pCircle = new Point(x,y);

				if(Math.sqrt((pCreature.x-pCircle.x)*(pCreature.x-pCircle.x)+
						(pCreature.y-pCircle.y)*(pCreature.y-pCircle.y)) < radius + creature.width() / 2)
					creaturesIn.add(creature);
			}
		}
		catch(NoSuchElementException nse)
		{
			System.err.println("Creature not found");
		}

		return creaturesIn;
	}

	// return creature
	public Creature getCreature(int id)
	{
		try
		{
			Creature creature;
			Enumeration<Creature> eCreatures = creatures.elements();
			while(eCreatures.hasMoreElements())
			{
				creature = eCreatures.nextElement();

				if(creature.getId() == id)
					return creature;
			}
		}
		catch(NoSuchElementException nse)
		{
			System.err.println("Creature not found");
		}

		return null;
	}

	public void destroy()
	{
		stopCreatures();

		creatures.clear();
	}


	// launch wave
	public void launchWave(final CreatureWave wave,
			final Player launcher,
			final Team targetTeam, 
			final WaveState ws,
			final CreatureState cs)
	{

		new Thread(
				new Runnable()
				{
					public void run()
					{
						
						// recovery areas
						// FIXME 

						final Rect START_ZONE = targetTeam.getStartZone(randomStart(0, targetTeam.getNStartZones()-1));
						final Rect END_ZONE = targetTeam.getEndZone();

						int xStart = (int) START_ZONE.centerX();
						int yStart = (int) START_ZONE.centerY();
						
						if (game.getMode() == Game.MODE_RANDOM)
						{

							xStart = randomStart(0, 
									(int) START_ZONE.width()-MARGIN*2) + START_ZONE.left+MARGIN;
							yStart = randomStart(0, 
									(int) START_ZONE.height()-MARGIN*2) + START_ZONE.top+MARGIN;
						}
						
						ArrayList<Point> newPath;
						Creature c = wave.getCopy();
						try
						{
							newPath = game.getMap().getShortestPath(xStart,
									yStart, (int) END_ZONE.centerX(),
									(int) END_ZONE.centerY(), c.getType());
						}
						catch (PathNotFoundException e1) 
						{
							newPath = null;
						}

						
						for (int i = 0; i < wave.getN() && game.isStarted(); i++)
						{

							// pause
							try
							{
								synchronized (pause)
								{
									if(paused)
										pause.wait();
								} 
							} 
							catch (InterruptedException e1)
							{
								e1.printStackTrace();
							}

							if(game.isStarted())
							{
								Creature creature = wave.getCopy();
								creature.setX(xStart-creature.width()/2);
								creature.setY(yStart-creature.height()/2);
								creature.setOwner(launcher);
								creature.setTargetTeam(targetTeam);
								creature.addCreatureState(cs);
								
								creature.setPath(newPath);

								addCreature(creature);
								game.addCreature(creature);

								try
								{
									Thread.sleep((long)(CreatureWave.getInterval(creature.getSpeed()) / game.getCoeffSpeed()));
								} catch (InterruptedException e)
								{
									e.printStackTrace();
								}
							}
						}

						if (ws != null)
							ws.launchWave(wave); 
					}
				}).start();


	}

	protected int randomStart(int min, int max) {
		return min + (int) Math.round(Math.random() * (max - min)); 
	}

}
