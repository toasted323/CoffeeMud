package com.planet_ink.coffee_mud.Items.MiscMagic;
import com.planet_ink.coffee_mud.Items.Basic.StdItem;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import java.util.*;

/*
   Copyright 2003-2023 Bo Zimmerman

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
public class CubicGate extends StdItem implements MiscMagic
{
	@Override
	public String ID()
	{
		return "CubicGate";
	}

	public CubicGate()
	{
		super();

		setName("a cube of stange metal");
		setDisplayText("a strange, metallic cube sits here.");
		resetCube();
		basePhyStats().setLevel(1);
		setMaterial(RawMaterial.RESOURCE_ADAMANTITE);
		basePhyStats().setWeight(2);
		baseGoldValue=7500;
		basePhyStats().setDisposition(basePhyStats().disposition()|PhyStats.IS_BONUS);
		recoverPhyStats();
	}
	
	protected final static String[] ACT_WORDS = new String[] { "zero", "one", "two", "three", "four", "five", "six" };
	protected final List<String> planes = new ArrayList<String>(6);
	
	protected void setCubeDescription()
	{
		setDescription(L("This three-inch cube is of some unearthly metal. each face is engraved it alien-looking sigils, as well as a @x1 pips (dots).",""+planes.size()));
		secretIdentity=L("A cubic gate.  Hold, and say 'one' to '@x1' to activate.", ACT_WORDS[planes.size()]);
	}
	
	protected void resetCube()
	{
		planes.clear();
		planes.add(L("Prime Material"));
		final PlanarAbility planeA =(PlanarAbility)CMClass.getAbility("StdPlanarAbility");
		if(planeA != null)
		{
			int numPlanes = CMLib.dice().roll(1, 5, 1);
			int tries = 1000;
			while((planes.size() < numPlanes) && (--tries>0))
			{
				final String newName = planeA.getAllPlaneKeys().get(CMLib.dice().roll(1, planeA.getAllPlaneKeys().size(), -1));
				if(!planes.contains(newName))
					planes.add(newName);
			}
			super.miscText = CMParms.combineWith(planes,',');
		}
		setCubeDescription();
	}
	
	@Override
	public void setMiscText(final String newMiscText)
	{
		super.setMiscText(newMiscText);
		if((newMiscText != null) && (newMiscText.trim().length()>0))
		{
			
			planes.clear();
			planes.addAll(CMParms.parseAny(newMiscText, ',', true));
			if(planes.contains(L("Prime Material")))
				setCubeDescription();
			else
				resetCube();
		}
		else
			resetCube();
	}
	
	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		switch(msg.targetMinor())
		{
		case CMMsg.TYP_WAND_USE:
			if(msg.amITarget(this)
			&&((msg.tool()==null)||(msg.tool() instanceof Physical))
			&&(msg.targetMessage()!=null))
			{
				String plane = CMLib.flags().getPlaneOfExistence(msg.source());
				if(plane == null)
					plane = L("prime material");
				if(plane.equalsIgnoreCase(msg.targetMessage()))
					msg.source().tell(L("Nothing happens."));
				else
				{
					final PlanarAbility spellA = (PlanarAbility)CMClass.getAbility("Spell_Planeshift");
					final List<String> cmds = new XVector<String>(msg.targetMessage());
					if(spellA != null)
						spellA.invoke(msg.source(), cmds, msg.source(), true, msg.source().phyStats().level());
					else
						msg.source().tell(L("Nothing happens."));
				}
			}
			break;
		case CMMsg.TYP_SPEAK:
			if((msg.sourceMinor()==CMMsg.TYP_SPEAK)
			&&(msg.target()==this)
			&&(!amWearingAt(Wearable.IN_INVENTORY)))
			{
				boolean alreadyWanding=false;
				final List<CMMsg> trailers=msg.trailerMsgs();
				if(trailers!=null)
				{
					for(final CMMsg msg2 : trailers)
					{
						if(msg2.targetMinor()==CMMsg.TYP_WAND_USE)
							alreadyWanding=true;
					}
				}
				final String said=CMStrings.getSayFromMessage(msg.sourceMessage());
				if((!alreadyWanding)
				&&(said!=null)
				&&(said.indexOf(' ')<0)
				&&(this.rawWornCode() != Wearable.IN_INVENTORY))
				{
					int x = CMParms.indexOf(ACT_WORDS, said.toLowerCase().trim());
					if((x>=1)&&(x<=planes.size()))
					{
						final String plane = planes.get(x-1);
						msg.addTrailerMsg(CMClass.getMsg(msg.source(),this,msg.target(),CMMsg.NO_EFFECT,null,CMMsg.MASK_ALWAYS|CMMsg.TYP_WAND_USE,plane,CMMsg.NO_EFFECT,null));
					}
				}
			}
			break;
		default:
			break;
		}
		super.executeMsg(myHost,msg);
	}

}