package com.planet_ink.coffee_mud.Abilities.Paladin;
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

public class Paladin_ImprovedResists extends Paladin
{
	public String ID() { return "Paladin_ImprovedResists"; }
	public String name(){ return "Paladin`s Resistance";}

	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		if((affected!=null)&&(CMLib.flags().isGood(affected)))
		{
			int amount=(int)Math.round(CMath.mul(CMath.div(profficiency(),100.0),affected.envStats().level()));
			affectableStats.setStat(CharStats.SAVE_ACID,affectableStats.getStat(CharStats.SAVE_ACID)+amount);
			affectableStats.setStat(CharStats.SAVE_COLD,affectableStats.getStat(CharStats.SAVE_COLD)+amount);
			affectableStats.setStat(CharStats.SAVE_ELECTRIC,affectableStats.getStat(CharStats.SAVE_ELECTRIC)+amount);
			affectableStats.setStat(CharStats.SAVE_FIRE,affectableStats.getStat(CharStats.SAVE_FIRE)+amount);
			affectableStats.setStat(CharStats.SAVE_GAS,affectableStats.getStat(CharStats.SAVE_GAS)+amount);
			affectableStats.setStat(CharStats.SAVE_MIND,affectableStats.getStat(CharStats.SAVE_MIND)+amount);
			affectableStats.setStat(CharStats.SAVE_PARALYSIS,affectableStats.getStat(CharStats.SAVE_PARALYSIS)+amount);
			affectableStats.setStat(CharStats.SAVE_MAGIC,affectableStats.getStat(CharStats.SAVE_MAGIC)+amount);
			affectableStats.setStat(CharStats.SAVE_JUSTICE,affectableStats.getStat(CharStats.SAVE_JUSTICE)+amount);
			affectableStats.setStat(CharStats.SAVE_POISON,affectableStats.getStat(CharStats.SAVE_POISON)+amount);
			affectableStats.setStat(CharStats.SAVE_WATER,affectableStats.getStat(CharStats.SAVE_WATER)+amount);
			affectableStats.setStat(CharStats.SAVE_UNDEAD,affectableStats.getStat(CharStats.SAVE_UNDEAD)+amount);
			affectableStats.setStat(CharStats.SAVE_DISEASE,affectableStats.getStat(CharStats.SAVE_DISEASE)+amount);
		}
	}
}
