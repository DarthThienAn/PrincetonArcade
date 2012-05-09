package puArcade.princetonTD.towers;

import puArcade.princetonTD.animations.Arrow;
import puArcade.princetonTD.creatures.Creature;
import android.graphics.Color;

public class TowerArcher extends Tower {

	public static final int COLOR;
	public static final String IMAGE;
	public static final String ICON;
	public static final int MAX_LEVEL = 5;
	public static final int PRICE = 10;
	private static final String DESCRIPTION = "";

	static
	{
		COLOR 	= Color.RED;
		IMAGE   = "drawable/towerarcher";
		ICON    = "drawable/icontowerarcher";
	}

	public TowerArcher()
	{
		super(	0,
				0,
				20,
				20,
				COLOR,
				"Archer Tower",
				PRICE,
				5,
				50,
				2,
				Tower.TYPE_LAND_AND_AIR,
				IMAGE,
				ICON);

		description = DESCRIPTION;
	}

	public void upgrade()
	{
		if(canUpgrade())
		{
			priceTotal += price;

			price *= 2;

			damage = nextDamage();

			range = nextRange();

			setRate(nextRate());

			level++;
		}
	}

	public void attack(Creature creature)
	{
		game.addAnimation(new Arrow(game,this,creature,damage));
	}

	public Tower copy()
	{
		return new TowerArcher();
	}

	public boolean canUpgrade()
	{
		return level < MAX_LEVEL;
	}

	@Override
	public long nextDamage()
	{
		return (long) (damage * 1.5);
	}

	@Override
	public double nextRange()
	{
		return range + 10;
	}
	
	@Override
	public double nextRate()
	{
		return getRate() * 1.2;
	}

}