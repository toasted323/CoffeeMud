package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
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
   Copyright 2000-2013 Bo Zimmerman

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
@SuppressWarnings("rawtypes")
public class Aft extends Go
{
	public Aft(){}

	private final String[] access={"AFT"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		int direction=Directions.SOUTH;
		if((commands!=null)&&(commands.size()>1))
		{
			int nextDir=Directions.getDirectionCode((String)commands.get(2));
			if(nextDir == Directions.EAST)
				direction=Directions.SOUTHEAST;
			else
			if(nextDir == Directions.WEST)
				direction=Directions.SOUTHWEST;
		}
		standIfNecessary(mob,metaFlags);
		if((CMLib.flags().isSitting(mob))||(CMLib.flags().isSleeping(mob)))
		{
			mob.tell("You need to stand up first.");
			return false;
		}
		if(CMath.bset(mob.getBitmap(),MOB.ATT_AUTORUN))
			CMLib.tracking().run(mob, direction, false,false,false);
		else
			CMLib.tracking().walk(mob, direction, false,false,false);
		return false;
	}
	public boolean canBeOrdered(){return true;}

	public boolean securityCheck(MOB mob)
	{
		return (mob==null) || (mob.isMonster()) || (mob.location()==null) 
			|| (mob.location() instanceof SpaceShip) || (mob.location().getArea() instanceof SpaceShip);
	}
}
