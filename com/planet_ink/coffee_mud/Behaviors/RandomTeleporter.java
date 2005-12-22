package com.planet_ink.coffee_mud.Behaviors;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;


import java.util.*;

/* 
   Copyright 2000-2006 Bo Zimmerman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
public class RandomTeleporter extends ActiveTicker
{
	public String ID(){return "RandomTeleporter";}
	protected int canImproveCode(){return Behavior.CAN_MOBS;}
	public long flags(){return Behavior.FLAG_MOBILITY;}
	protected Vector restrictedLocales=null;
	protected boolean nowander=false;

	public RandomTeleporter()
	{
		super();
		minTicks=1; maxTicks=5; chance=100;
		restrictedLocales=null;
		tickReset();
	}


	public boolean okRoomForMe(Room currentRoom, Room newRoom)
	{
		if(currentRoom==null) return false;
		if(newRoom==null) return false;
		if((nowander)&&((currentRoom.getArea()!=newRoom.getArea())))
			return false;
		if(restrictedLocales==null) return true;
		return !restrictedLocales.contains(new Integer(newRoom.domainType()));
	}

	public void setParms(String newParms)
	{
		super.setParms(newParms);
		nowander=false;
		restrictedLocales=null;
		Vector V=CMParms.parse(newParms);
		for(int v=0;v<V.size();v++)
		{
			String s=(String)V.elementAt(v);
			if(s.toUpperCase().startsWith("NOWANDER"))
				nowander=true;
			else
			if((s.startsWith("+")||(s.startsWith("-")))&&(s.length()>1))
			{
				if(restrictedLocales==null)
					restrictedLocales=new Vector();
				if(s.equalsIgnoreCase("+ALL"))
					restrictedLocales.clear();
				else
				if(s.equalsIgnoreCase("-ALL"))
				{
					restrictedLocales.clear();
					for(int i=0;i<Room.indoorDomainDescs.length;i++)
						restrictedLocales.addElement(new Integer(Room.INDOORS+i));
					for(int i=0;i<Room.outdoorDomainDescs.length;i++)
						restrictedLocales.addElement(new Integer(i));
				}
				else
				{
					char c=s.charAt(0);
					s=s.substring(1).toUpperCase().trim();
					int code=-1;
					for(int i=0;i<Room.indoorDomainDescs.length;i++)
						if(Room.indoorDomainDescs[i].startsWith(s))
							code=Room.INDOORS+i;
					if(code>=0)
					{
						if((c=='+')&&(restrictedLocales.contains(new Integer(code))))
							restrictedLocales.removeElement(new Integer(code));
						else
						if((c=='-')&&(!restrictedLocales.contains(new Integer(code))))
							restrictedLocales.addElement(new Integer(code));
					}
					code=-1;
					for(int i=0;i<Room.outdoorDomainDescs.length;i++)
						if(Room.outdoorDomainDescs[i].startsWith(s))
							code=i;
					if(code>=0)
					{
						if((c=='+')&&(restrictedLocales.contains(new Integer(code))))
							restrictedLocales.removeElement(new Integer(code));
						else
						if((c=='-')&&(!restrictedLocales.contains(new Integer(code))))
							restrictedLocales.addElement(new Integer(code));
					}

				}
			}
		}
		if((restrictedLocales!=null)&&(restrictedLocales.size()==0))
			restrictedLocales=null;
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		super.tick(ticking,tickID);
		if((canAct(ticking,tickID))&&(ticking instanceof MOB))
		{
			MOB mob=(MOB)ticking;
			int tries=0;
			Room R=null;
			while(((++tries)<250)&&(R==null))
			{
				R=CMLib.map().getRandomRoom();
				if((!CMLib.flags().isInFlight(mob))
				&&((R.domainType()==Room.DOMAIN_INDOORS_AIR)
				||(R.domainType()==Room.DOMAIN_OUTDOORS_AIR)))
					R=null;
				else
				if((!CMLib.flags().isSwimming(mob))
				&&((R.domainType()==Room.DOMAIN_INDOORS_UNDERWATER)
				||(R.domainType()==Room.DOMAIN_OUTDOORS_UNDERWATER)))
					R=null;
				else
				if(!okRoomForMe(mob.location(),R))
					R=null;
			}
			Room oldRoom=mob.location();
			CMLib.tracking().wanderAway(mob,true,false);
			R.bringMobHere(mob,true);
			if(mob.location()==oldRoom)
				tickDown=0;
		}
		return true;
	}
}
