package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Spell_DetectWater extends Spell
{
	public String ID() { return "Spell_DetectWater"; }
	public String name(){return "Detect Water";}
	public String displayText(){return "(Detecting Water)";}
	public int quality(){ return OK_SELF;}
	protected int canAffectCode(){return CAN_MOBS;}
	Room lastRoom=null;
	public Environmental newInstance(){	return new Spell_DetectWater();	}
	public int classificationCode(){return Ability.SPELL|Ability.DOMAIN_DIVINATION;	}

	public void unInvoke()
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		if(canBeUninvoked())
			lastRoom=null;
		super.unInvoke();
		if(canBeUninvoked())
			mob.tell(mob,null,"Your senses are no longer sensitive to liquids.");
	}
	public String waterCheck(MOB mob, Item I, Item container, StringBuffer msg)
	{
		if(I==null) return "";
		if(I.container()==container)
		{
			if(((I instanceof Drink))
			&&(((Drink)I).containsDrink())
			&&(Sense.canBeSeenBy(I,mob)))
				msg.append(I.name()+" contains some sort of liquid.\n\r");
		}
		else
		if((I.container()!=null)&&(I.container().container()==container))
			if(msg.toString().indexOf(I.container().name()+" contains some sort of liquid.")<0)
				msg.append(I.container().name()+" contains some sort of liquid.\n\r");
		return msg.toString();
	}
	public String waterHere(MOB mob, Environmental E, Item container)
	{
		StringBuffer msg=new StringBuffer("");
		if(E==null) return msg.toString();
		if((E instanceof Room)&&(Sense.canBeSeenBy(E,mob)))
		{
			Room room=(Room)E;
			if((room.domainType()==Room.DOMAIN_OUTDOORS_UNDERWATER)
			||(room.domainType()==Room.DOMAIN_OUTDOORS_WATERSURFACE)
			||(room.domainType()==Room.DOMAIN_INDOORS_UNDERWATER)
			||(room.domainType()==Room.DOMAIN_INDOORS_WATERSURFACE))
				msg.append("Your liquid senses are saturated.  This is a very wet place.\n\r");
			else
			if(room.domainConditions()==Room.CONDITION_WET)
				msg.append("Your liquid senses are saturated.  This is a damp place.\n\r");
			else
			if((room.getArea().weatherType(room)==Area.WEATHER_RAIN)
			||(room.getArea().weatherType(room)==Area.WEATHER_THUNDERSTORM))
				msg.append("It is raining here! Your liquid senses are saturated!\n\r");
			else
			if(room.getArea().weatherType(room)==Area.WEATHER_HAIL)
				msg.append("It is hailing here! Your liquid senses are saturated!\n\r");
			else
			if(room.getArea().weatherType(room)==Area.WEATHER_SNOW)
				msg.append("It is snowing here! Your liquid senses are saturated!\n\r");
			else
			{
				for(int i=0;i<room.numItems();i++)
				{
					Item I=room.fetchItem(i);
					waterCheck(mob,I,container,msg);
				}
				for(int m=0;m<room.numInhabitants();m++)
				{
					MOB M=room.fetchInhabitant(m);
					if((M!=null)&&(M!=mob))
						msg.append(waterHere(mob,M,null));
				}
			}
		}
		else
		if((E instanceof Item)&&(Sense.canBeSeenBy(E,mob)))
		{
			waterCheck(mob,(Item)E,container,msg);
			msg.append(waterHere(mob,((Item)E).owner(),(Item)E));
		}
		else
		if((E instanceof MOB)&&(Sense.canBeSeenBy(E,mob)))
		{
			for(int i=0;i<((MOB)E).inventorySize();i++)
			{
				Item I=((MOB)E).fetchInventory(i);
				StringBuffer msg2=new StringBuffer("");
				waterCheck(mob,I,container,msg2);
				if(msg2.length()>0)
					return E.name()+" is carrying some liquids.";
			}
			if(E instanceof ShopKeeper)
			{
				StringBuffer msg2=new StringBuffer("");
				Vector V=((ShopKeeper)E).getUniqueStoreInventory();
				for(int v=0;v<V.size();v++)
				{
					Environmental E2=(Environmental)V.elementAt(v);
					if(E2 instanceof Item)
						waterCheck(mob,(Item)E2,container,msg2);
					if(msg2.length()>0)
						return E.name()+" has some liquids in stock.";
				}
			}
		}
		return msg.toString();
	}
	public void messageTo(MOB mob)
	{
		String last="";
		String dirs="";
		for(int d=0;d<=Directions.NUM_DIRECTIONS;d++)
		{
			Room R=null;
			Exit E=null;
			if(d<Directions.NUM_DIRECTIONS)
			{
				R=mob.location().getRoomInDir(d);
				E=mob.location().getExitInDir(d);
			}
			else
			{
				R=mob.location();
				E=CMClass.getExit("StdExit");
			}
			if((R!=null)&&(E!=null))
			{
				boolean metalFound=false;
				if(waterHere(mob,R,null).length()>0)
					metalFound=true;
				else
				for(int m=0;m<R.numInhabitants();m++)
				{
					MOB M=R.fetchInhabitant(m);
					if((M!=null)&&(M!=mob)&&(waterHere(mob,M,null).length()>0))
					{ metalFound=true; break;}
				}

				if(metalFound)
				{
					if(last.length()>0)
						dirs+=", "+last;
					if(d>=Directions.NUM_DIRECTIONS)
						last="here";
					else
						last=Directions.getFromDirectionName(d);
				}
			}
		}

		if((dirs.length()!=0)||(last.length()!=0))
		{
			if(dirs.length()==0)
				mob.tell("Water smells are coming from "+last+".");
			else
				mob.tell("Water smells are coming from "+dirs.substring(2)+", and "+last+".");
		}
	}
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if((tickID==Host.MOB_TICK)
		   &&(affected!=null)
		   &&(affected instanceof MOB)
		   &&(((MOB)affected).location()!=null)
		   &&((lastRoom==null)||(((MOB)affected).location()!=lastRoom)))
		{
			lastRoom=((MOB)affected).location();
			messageTo((MOB)affected);
		}
		return true;
	}


	public void affect(Affect affect)
	{
		super.affect(affect);
		if((affected!=null)
		   &&(affected instanceof MOB)
		   &&(affect.target()!=null)
		   &&(affect.amISource((MOB)affected))
		   &&(affect.sourceMinor()==Affect.TYP_EXAMINESOMETHING))
		{
			if((affect.tool()!=null)&&(affect.tool().ID().equals(ID())))
			{
				String msg=waterHere((MOB)affected,affect.target(),null);
				if(msg.length()>0)
					((MOB)affected).tell(msg);
			}
			else
			{
				FullMsg msg=new FullMsg(affect.source(),affect.target(),this,affect.MSG_EXAMINESOMETHING,affect.NO_EFFECT,affect.NO_EFFECT,null);
				affect.addTrailerMsg(msg);
			}
		}
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		if(mob.fetchAffect(this.ID())!=null)
		{
			mob.tell("You are already detecting liquid things.");
			return false;
		}

		MOB target=mob;
		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof MOB)) 
			target=(MOB)givenTarget;
		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"<T-NAME> gain(s) liquid sensitivities!":"^S<S-NAME> incant(s) softly, and gain(s) liquid sensitivities!^?");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,0);
			}
		}
		else
			beneficialVisualFizzle(mob,null,"<S-NAME> incant(s) and open(s) <S-HIS-HER> liquified eyes, but the spell fizzles.");

		return success;
	}
}