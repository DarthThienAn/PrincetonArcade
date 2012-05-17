/*
  Copyright (C) 2010 Aurelien Da Campo

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 3 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

/*
  Unless stated otherwise, all code below is from said above open 
  source project. Code variables have been translated from French to
  English to facilitate development. Everything else has been left intact
  from the original source.

  Modified portions are further commented detailing changes made.
 */

package puArcade.princetonTD.game;

import java.util.ArrayList;
import java.util.Vector;

import puArcade.princetonTD.animations.Animation;
import puArcade.princetonTD.animations.AnimationManager;
import puArcade.princetonTD.creatures.Creature;
import puArcade.princetonTD.creatures.CreatureManager;
import puArcade.princetonTD.creatures.CreatureWave;
import puArcade.princetonTD.exceptions.BlockedPathException;
import puArcade.princetonTD.exceptions.InaccessibleZoneException;
import puArcade.princetonTD.exceptions.MaxLevelException;
import puArcade.princetonTD.exceptions.NoMoneyException;
import puArcade.princetonTD.maps.Map;
import puArcade.princetonTD.players.Player;
import puArcade.princetonTD.players.PlayerLocation;
import puArcade.princetonTD.players.Team;
import puArcade.princetonTD.towers.Tower;
import puArcade.princetonTD.towers.TowerManager;

import android.graphics.Rect;

public abstract class Game
{
	// Center Position
	public static final int MODE_CENTER = 0;

	// Random Position
	public static final int MODE_RANDOM = 1;

	// Position Mode
	private static final int MODE = MODE_RANDOM;

	// Game speed coefficient
	private static final double STEP_COEFF_SPEED = 0.5;

	// Max speed coefficient
	private static final double MAX_COEFF_SPEED = 5.0;

	// Min speed coefficient
	private static final double MIN_COEFF_SPEED = 0.1;

	// Playing map
	protected Map map;

	// Set of teams
	protected ArrayList<Team> teams = new ArrayList<Team>();

	// Tower manager
	protected TowerManager towerManager;

	// Creature manager
	protected CreatureManager creatureManager;

	// Animation manager
	protected AnimationManager animationManager;

	// Pause
	protected boolean pause;

	// Current wave index
	protected int indexCurrentWave = 1;

	// Wave of creatures
	protected CreatureWave currentWave;
	
	// Is a wave launching?
	protected boolean isLaunching = false;

	// Initialized?
	protected boolean isInitialized;

	// Started?
	private boolean isStarted;

	// Finished?
	protected boolean isFinished;

	// Destroyed?
	protected boolean isDestroyed;

	// Player
	protected Player player;

	// Speed coefficient
	private double coeffSpeed;

	// CONSTRUCTOR
	public Game()
	{
		towerManager		= new TowerManager(this);
		creatureManager		= new CreatureManager(this);
		animationManager	= new AnimationManager(this);
	}

	// Initializer
	synchronized public void initialize()
	{
		if(map == null)
			throw new IllegalStateException("No map");

		if(teams.size() == 0)
			throw new IllegalStateException("No team");

		// attributes
		indexCurrentWave	= 1;
		pause				= false;
		isStarted           = false;
		isFinished	        = false;
		isDestroyed         = false;
		currentWave         = map.getCreatureWave(indexCurrentWave);
		coeffSpeed          = 1.0;

		// initialize default values
		for(Team team : teams)
		{
			// initialize lives
			team.setLives(map.getLives());

			// initialize players and gold
			for(Player p : team.getPlayers())
			{
				p.setScore(0);
				p.setGold(map.getGold());
			}
		}  

		isInitialized = true;
	}

	//-----------------------------------------
	// Management
	//-----------------------------------------

	public void reset()
	{
		map.reset();

		isInitialized = false;
		isStarted = false;

		initialize();

		// stop managers
		towerManager.stopTowers();
		creatureManager.stopCreatures();
		animationManager.stopAnimations();

		towerManager		= new TowerManager(this);
		creatureManager		= new CreatureManager(this);
		animationManager	= new AnimationManager(this);

		// add players
		for(Team t : teams)
		{
			t.setLives(map.getLives());

			for(Player p : t.getPlayers())    
			{
				p.setGold(map.getGold());
				p.setScore(0);
			}
		}
	}

	// Start
	public void start()
	{
		if(map == null)
			throw new IllegalStateException("No map");

		if(!isInitialized)
			throw new IllegalStateException("Game not initialized");

		if(isStarted)
			throw new IllegalStateException("Game already started");

		// start managers
		towerManager.start();
		creatureManager.start();
		animationManager.start();

		isStarted = true;
	}


	// end
	public void end()
	{
		if(!isFinished)
		{
			isFinished = true;

			stopAll();
		}
	}

	// Stop all
	protected void stopAll()
	{
		towerManager.stopTowers();
		creatureManager.stopCreatures();
		animationManager.stopAnimations();
	}

	// Pause
	public boolean togglePause()
	{
		if(pause)
		{
			towerManager.unpause();
			creatureManager.unpause();
			animationManager.unpause();
		}
		else
		{
			towerManager.pause();
			creatureManager.pause();
			animationManager.pause();
		}

		return pause = !pause;  
	}

	// destroy
	public void destroy()
	{
		isDestroyed = true;

		towerManager.destroy();
		creatureManager.destroy();
		animationManager.destroy();
	}

	// is initialized?
	public boolean isInitialized()
	{
		return isInitialized;
	}

	// is started?
	public boolean isStarted()
	{
		return isStarted;
	}

	// is finished?
	public boolean isFinished()
	{
		return isFinished;
	}

	// is paused?
	public boolean isPaused()
	{
		return pause;
	}

	// is destroyed?
	public boolean isDestroyed()
	{
		return isDestroyed;
	}

	// return speed coefficient
	public double getCoeffSpeed()
	{
		return coeffSpeed;
	}

	// increment speed coefficient
	public synchronized double increaseSpeed()
	{
		if(coeffSpeed + STEP_COEFF_SPEED <= MAX_COEFF_SPEED)
		{    
			coeffSpeed += STEP_COEFF_SPEED;
		}

		return coeffSpeed;
	}

	// decrement speed coefficient
	synchronized public double decreaseSpeed()
	{
		if(coeffSpeed - STEP_COEFF_SPEED >= MIN_COEFF_SPEED)
		{ 
			coeffSpeed -= STEP_COEFF_SPEED;
		}    
		return coeffSpeed;
	}

	// set speed coefficient
	public void setCoeffSpeed(double value)
	{
		if(coeffSpeed - STEP_COEFF_SPEED < MIN_COEFF_SPEED
				&& coeffSpeed + STEP_COEFF_SPEED > MAX_COEFF_SPEED)
			throw new IllegalArgumentException(
					"Cannot modify speed");

		coeffSpeed = value;
	}

	// return positioning mode
	public int getMode()
	{
		return MODE;
	}

	//-----------------------------------------
	// Towers
	//-----------------------------------------

	// get Tower
	public Tower getTower(int idTower)
	{
		for (Tower tower : getTowers())
			if (tower.getId() == idTower)
				return tower;

		return null;
	}

	// return towers
	public Vector<Tower> getTowers()
	{
		return towerManager.getTowers();
	}

	// Place tower
	public void addTower(Tower tower) throws NoMoneyException, InaccessibleZoneException, BlockedPathException
	{
		// valid tower?
		if (tower == null)
			throw new IllegalArgumentException("Invalid tower!");

		// enough money?
		if(!canBuyTower(tower))    
			throw new NoMoneyException("Not enough money!");

		// valid area?
		if (!canPlaceTower(tower))
			throw new InaccessibleZoneException("Inaccessible area!");

		// is path blocked
		if (map.blockedPath(tower))
			throw new BlockedPathException("Path blocked!");

		// deactivate tower zone
		map.deactivateZone(new Rect(tower.x(),tower.y(),tower.x()+tower.width(),tower.y()+tower.height()), true);

		// add tower
		towerManager.addTower(tower);

		// update game
		tower.setGame(this);

		// activate tower
		tower.start();

		// gold transaction
		tower.getOwner().setGold(
				tower.getOwner().getGold() - tower.getPrice());
	}


	// Sell tower
	public void sellTower(Tower tower)
	{
		towerManager.removeTower(tower);

		tower.getOwner().setGold(
				tower.getOwner().getGold() + tower.getSellPrice());
	}


	// Upgrade tower
	public void upgradeTower(Tower tower) throws MaxLevelException, NoMoneyException
	{
		if(!tower.canUpgrade())
			throw new MaxLevelException("Max level reached!");

		if(tower.getOwner().getGold() < tower.getPrice())
			throw new NoMoneyException("Not enough money!");

		tower.getOwner().setGold(tower.getOwner().getGold() - tower.getPrice());

		tower.upgrade();
	}

	// Can place tower
	public boolean canPlaceTower(Tower tower)
	{
		return towerManager.canPlaceTower(tower);
	}

	// Can buy tower
	public boolean canBuyTower(Tower tower)
	{  
		return towerManager.canBuyTower(tower);
	}

	//-----------------------------------------
	// Creatures
	//-----------------------------------------

	// get Creature
	public Creature getCreature(int idCreature)
	{
		return creatureManager.getCreature(idCreature);
	}

	// get all creatures
	public Vector<Creature> getCreatures()
	{
		return creatureManager.getCreatures();
	}

	// return creatures in circle
	public Vector<Creature> getCreaturesInCircle(int centerX, int centreY, int radius)
	{
		return creatureManager.getCreaturesInCircle(centerX, centreY, radius);
	}

	//-----------------------------------------
	// Waves
	//-----------------------------------------

	// get current wave
	public CreatureWave getWave()
	{
		return currentWave;
	}

	// return current wave number
	public int getCurrentWave()
	{
		return indexCurrentWave;
	}

	// move onto next wave
	public void nextWave()
	{
		indexCurrentWave++;
	}

	// Start new wave of creatures
	public void launchNewWave(Player player, Team team)
	{
		currentWave = map.getCreatureWave(indexCurrentWave);
		
		isLaunching = true;
		nextWave();

		creatureManager.launchWave(currentWave, player, team);

		currentWave = map.getCreatureWave(indexCurrentWave);
	}

	// wave finished launching
	public void waveFinishedLaunch(CreatureWave wave) {
		isLaunching = false;
	}
	
	// is a wave being launched?
	public boolean isLaunching()
	{
		return isLaunching;
	}

	synchronized public void killedCreature(Creature creature, Player player)
	{
		player.setGold(player.getGold() + creature.getReward());

		player.setScore(player.getScore() + creature.getReward());
	}

	synchronized public void reachedCreature(Creature creature)
	{
		Team team = creature.getTargetTeam();

		if(!team.hasLost())
		{
			team.loseLife();

			if(team.hasLost())
			{
				int remaining = 0;
				for(Team tmp : teams)
					if(!tmp.hasLost())
						remaining++;

				if(remaining <= 1)
					end();
			}
		} 
	}

	//-----------------------------------------
	// Map
	//-----------------------------------------

	// Set map
	public void setMap(Map map) throws IllegalArgumentException
	{
		teams = map.getTeams();

		this.map  = map;  
	}

	// Return map
	public Map getMap()
	{
		if(map == null)
			throw new NullPointerException("No map!");

		return map;
	}

	//-----------------------------------------
	// Player
	//-----------------------------------------

	// get player
	public Player getPlayer(int idPlayer)
	{
		ArrayList<Player> players = getPlayers();

		for(Player player : players)
			if(player.getId() == idPlayer)
				return player;

		return null;
	}

	// return players
	public ArrayList<Player> getPlayers()
	{
		ArrayList<Player> players = new ArrayList<Player>();

		for(Team t : teams)
			for(Player p : t.getPlayers())
				players.add(p);

		return players;
	}

	// Add player
	public void addPlayer(Player player)
	{
		for(int i=0;i<teams.size();i++)
		{   
			teams.get(i).addPlayer(player);
		}
	}

	// return main player
	public Player getMainPlayer()
	{ 
		return player;
	}

	// set main player
	public void setMainPlayer(Player player)
	{
		this.player = player;
	}

	// get Player Loaction
	public PlayerLocation getPlayerLocation(int idLocation)
	{
		for(Team team : teams)
			for(PlayerLocation pl : team.getPlayerLocations())
				if(pl.getId() == idLocation)
					return pl;

		return null;
	}

	//-----------------------------------------
	// Team
	//-----------------------------------------

	// get team
	public Team getTeam(int idTeam)
	{
		for(Team team : teams)
			if(team.getId() == idTeam)
				return team;

		return null;
	}

	// return teams
	public ArrayList<Team> getTeams()
	{
		return teams;
	}

	// next non-empty team
	public Team getNextTeam(Team team)
	{
		int i = (teams.indexOf(team)+1) % teams.size();

		while(teams.get(i).getPlayers().size() == 0 || teams.get(i).hasLost())
			i = ++i % teams.size();

		return teams.get(i);
	}

	// add team
	public void addTeam(Team team)
	{
		teams.add(team);
	}

	// remove team
	public void removeTeam(Team team)
	{
		teams.remove(team);
	}

	//-----------------------------------------
	// Animation
	//-----------------------------------------

	// add animation
	public void addAnimation(Animation animation)
	{
		animationManager.addAnimation(animation);
	}

}
