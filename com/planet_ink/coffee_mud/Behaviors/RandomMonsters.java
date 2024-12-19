package com.planet_ink.coffee_mud.Behaviors;
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
   Copyright 2003-2024 Bo Zimmerman

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

public class RandomMonsters extends ActiveTicker
{
	@Override
	public String ID()
	{
		return "RandomMonsters";
	}

	@Override
	protected int canImproveCode()
	{
		return Behavior.CAN_ROOMS|Behavior.CAN_AREAS;
	}

	protected List<MOB>		maintained			= new Vector<MOB>();
	protected int			tickStatus			= 0;
	protected int			minMonsters			= 1;
	protected int			maxMonsters			= 1;
	protected int			avgMonsters			= 1;
	protected List<Integer>	restrictedLocales	= null;
	protected boolean		alreadyTriedLoad	= false;

	@Override
	public String accountForYourself()
	{
		return "random monster generating";
	}

	@Override
	public List<String> externalFiles()
	{
		final Vector<String> xmlfiles=new Vector<String>();
		final String theseparms=getParms();
		final int x=theseparms.indexOf(';');
		String filename=(x>=0)?theseparms.substring(x+1):theseparms;
		if(filename.trim().length()==0)
			return null;
		final int start=filename.indexOf("<MOBS>");
		if((start<0)||(start>20))
		{
			final int extraSemicolon=filename.indexOf(';');
			if(extraSemicolon>=0)
				filename=filename.substring(0,extraSemicolon);
			if(filename.trim().length()>0)
				xmlfiles.addElement(filename.trim());
			return xmlfiles;
		}
		return null;
	}

	@Override
	public void setParms(final String newParms)
	{
		maintained=new Vector<MOB>();
		final int x=newParms.indexOf(';');
		String oldParms=newParms;
		restrictedLocales=null;
		if(x>=0)
		{
			oldParms=newParms.substring(0,x).trim();
			String extraParms=oldParms;
			int extraX=newParms.indexOf("<MOBS>");
			if(extraX<0)
			{
				final String xtra=newParms.substring(x+1);
				extraX=xtra.indexOf(';');
				if(extraX>=0)
					extraParms=xtra.substring(extraX+1);
			}
			final Vector<String> V=CMParms.parse(extraParms);
			for(int v=0;v<V.size();v++)
			{
				String s=V.elementAt(v);
				if((s.startsWith("+")||s.startsWith("-"))&&(s.length()>1))
				{
					if(restrictedLocales==null)
						restrictedLocales=new Vector<Integer>();
					if(s.equalsIgnoreCase("+ALL"))
						restrictedLocales.clear();
					else
					if(s.equalsIgnoreCase("-ALL"))
					{
						restrictedLocales.clear();
						for(int i=0;i<Room.DOMAIN_INDOORS_DESCS.length;i++)
							restrictedLocales.add(Integer.valueOf(Room.INDOORS+i));
						for(int i=0;i<Room.DOMAIN_OUTDOOR_DESCS.length;i++)
							restrictedLocales.add(Integer.valueOf(i));
					}
					else
					{
						final char c=s.charAt(0);
						s=s.substring(1).toUpperCase().trim();
						int code=-1;
						for(int i=0;i<Room.DOMAIN_INDOORS_DESCS.length;i++)
						{
							if(Room.DOMAIN_INDOORS_DESCS[i].startsWith(s))
								code=Room.INDOORS+i;
						}
						if(code>=0)
						{
							if((c=='+')&&(restrictedLocales.contains(Integer.valueOf(code))))
								restrictedLocales.remove(Integer.valueOf(code));
							else
							if((c=='-')&&(!restrictedLocales.contains(Integer.valueOf(code))))
								restrictedLocales.add(Integer.valueOf(code));
						}
						code=-1;
						for(int i=0;i<Room.DOMAIN_OUTDOOR_DESCS.length;i++)
						{
							if(Room.DOMAIN_OUTDOOR_DESCS[i].startsWith(s))
								code=i;
						}
						if(code>=0)
						{
							if((c=='+')&&(restrictedLocales.contains(Integer.valueOf(code))))
								restrictedLocales.remove(Integer.valueOf(code));
							else
							if((c=='-')&&(!restrictedLocales.contains(Integer.valueOf(code))))
								restrictedLocales.add(Integer.valueOf(code));
						}

					}
				}
			}
		}
		super.setParms(oldParms);
		minMonsters=CMParms.getParmInt(oldParms,"minmonsters",1);
		maxMonsters=CMParms.getParmInt(oldParms,"maxmonsters",1);
		if(maxMonsters<minMonsters)
			maxMonsters=minMonsters;
		avgMonsters=CMLib.dice().roll(1,(maxMonsters-minMonsters),minMonsters);
		parms=newParms;
		if((restrictedLocales!=null)&&(restrictedLocales.size()==0))
			restrictedLocales=null;
		alreadyTriedLoad=false;

		Log.infoOut("Parameters set: minMonsters=" + minMonsters + ", maxMonsters=" + maxMonsters + ", avgMonsters=" + avgMonsters);
	}

	public RandomMonsters()
	{
		super();
		tickReset();
	}

	public boolean okRoomForMe(final MOB M, final Room newRoom)
	{
		if(newRoom==null)
			return false;
		if(M==null)
			return false;
		if(restrictedLocales==null)
			return true;
		return !restrictedLocales.contains(Integer.valueOf(newRoom.domainType()));
	}

	@SuppressWarnings("unchecked")
	public List<MOB> getMonsters(final Tickable thang, final String theseparms)
	{
		List<MOB> monsters=null;
		final int x=theseparms.indexOf(';');
		String thangName="null";
		if(thang instanceof Room)
			thangName=CMLib.map().getExtendedRoomID((Room)thang);
		else
		if((thang instanceof MOB)&&(((MOB)thang).getStartRoom())!=null)
			thangName=CMLib.map().getExtendedRoomID(((MOB)thang).getStartRoom());
		else
		if(thang!=null)
			thangName=thang.name();
		final String thangID=CMClass.classID(thang);
		String filename=(x>=0)?theseparms.substring(x+1):theseparms;
		if(filename.trim().length()==0)
		{
			if(alreadyTriedLoad)
				return null;
			alreadyTriedLoad=true;
			Log.errOut("Blank XML/filename: '"+filename+"' on Behavior RandomMonsters on object "+thangName+" ("+thangID+").");
			return null;
		}
		final int start=filename.indexOf("<MOBS>");
		if((start>=0)&&(start<=20))
		{
			int end=start+20;
			if(end>filename.length())
				end=filename.length();
			monsters=(List<MOB>)Resources.getResource("RANDOMMONSTERS-XML/"+filename.length()+"/"+filename.hashCode());
			if(monsters!=null)
				return monsters;
			monsters=new Vector<MOB>();
			final String error=CMLib.coffeeMaker().addMOBsFromXML(filename,monsters,null);
			if(error.length()>0)
			{
				if(alreadyTriedLoad)
					return null;
				alreadyTriedLoad=true;
				Log.errOut("RandomMonsters: Error on import of xml for '"+thangName+"' ("+thangID+"): "+error+".");
				return null;
			}
			if(monsters.size()<=0)
			{
				if(alreadyTriedLoad)
					return null;
				alreadyTriedLoad=true;
				Log.errOut("RandomMonsters: No mobs loaded for '"+thangName+"' ("+thangID+").");
				return null;
			}
			for(final MOB M : monsters)
				CMLib.threads().unTickAll(M);
			Resources.submitResource("RANDOMMONSTERS-XML/"+filename.length()+"/"+filename.hashCode(),monsters);
		}
		else
		{
			final int extraSemicolon=filename.indexOf(';');
			if(extraSemicolon>=0)
				filename=filename.substring(0,extraSemicolon);
			filename=filename.trim();
			monsters=(List<MOB>)Resources.getResource("RANDOMMONSTERS-"+filename);
			if((monsters==null)&&(!alreadyTriedLoad))
			{
				alreadyTriedLoad=true;
				final StringBuffer buf=Resources.getFileResource(filename,true);

				if((buf==null)||(buf.length()<20))
				{
					Log.errOut("RandomMonsters: Unknown XML file: '"+filename+"' for '"+thangName+"' ("+thangID+").");
					return null;
				}
				if(buf.substring(0,20).indexOf("<MOBS>")<0)
				{
					Log.errOut("RandomMonsters: Invalid XML file: '"+filename+"' for '"+thangName+"' ("+thangID+").");
					return null;
				}
				monsters=new Vector<MOB>();
				final String error=CMLib.coffeeMaker().addMOBsFromXML(buf.toString(),monsters,null);
				if(error.length()>0)
				{
					Log.errOut("RandomMonsters: Error on import of: '"+filename+"' for '"+thangName+"' ("+thangID+"): "+error+".");
					return null;
				}
				if(monsters.size()<=0)
				{
					Log.errOut("RandomMonsters: No mobs loaded: '"+filename+"' for '"+thangName+"' ("+thangID+").");
					return null;
				}
				for(final MOB M : monsters)
					CMLib.threads().unTickAll(M);
				Resources.submitResource("RANDOMMONSTERS-"+filename,monsters);
			}
		}
		return monsters;
	}

	public boolean canFlyHere(final MOB M, final Room R)
	{
		if(R==null)
			return true;
		if(((R.domainType()&Room.DOMAIN_INDOORS_AIR)==Room.DOMAIN_INDOORS_AIR)
		||((R.domainType()&Room.DOMAIN_OUTDOORS_AIR)==Room.DOMAIN_OUTDOORS_AIR))
		{
			if(!CMLib.flags().isInFlight(M))
				return false;
		}
		return true;
	}

	@Override
	public int getTickStatus()
	{
		return tickStatus;
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		tickStatus=Tickable.STATUS_START;
		super.tick(ticking,tickID);
		if((!CMProps.getBoolVar(CMProps.Bool.MUDSTARTED))
		||(CMSecurity.isDisabled(CMSecurity.DisFlag.RANDOMMONSTERS)))
		{
			tickStatus=Tickable.STATUS_NOT;
			return true;
		}
		if(!canAct(ticking,tickID))
		{
			tickStatus=Tickable.STATUS_NOT;
			return true;
		}
		for(int i=maintained.size()-1;i>=0;i--)
		{
			try
			{
				final MOB M=maintained.get(i);
				if((M.amDead())||(M.amDestroyed())||(M.location()==null)||(!M.location().isInhabitant(M)))
					maintained.remove(M);
			}
			catch (final Exception e)
			{
			}
		}
		if(maintained.size()>=avgMonsters)
		{
			Log.infoOut("Monster count at or above average. Resetting tickDown to " + tickDown);

			tickDown=this.maxTicks;
			tickStatus=Tickable.STATUS_NOT;
			return true;
		}
		tickStatus=Tickable.STATUS_MISC;
		final List<MOB> monsters=getMonsters(ticking,getParms());
		tickStatus=Tickable.STATUS_MISC+1;
		if(monsters==null)
		{
			Log.warnOut("No monsters available to spawn");
			tickStatus=Tickable.STATUS_NOT;
			return true;
		}
		tickStatus=Tickable.STATUS_MISC+2;
		int attempts=10000;
		if((ticking instanceof Environmental)&&(((Environmental)ticking).amDestroyed()))
			return false;
		while((maintained.size()<avgMonsters)&&(((--attempts)>0)))
		{
			MOB M=monsters.get(CMLib.dice().roll(1,monsters.size(),-1));
			if(M!=null)
			{
				tickStatus=Tickable.STATUS_MISC+3;
				M=(MOB)M.copyOf();
				tickStatus=Tickable.STATUS_MISC+4;
				M.setStartRoom(null);
				M.basePhyStats().setRejuv(PhyStats.NO_REJUV);
				M.recoverPhyStats();
				tickStatus=Tickable.STATUS_MISC+5;
				M.text();
				maintained.add(M);
				Log.infoOut("Spawned new monster: " + M.name() + ". Total monsters: " + maintained.size());

				tickStatus=Tickable.STATUS_MISC+6;
				if(ticking instanceof Room)
				{
					tickStatus=Tickable.STATUS_MISC+7;
					if(ticking instanceof GridLocale)
					{
						tickStatus=Tickable.STATUS_MISC+8;
						final Room room=((GridLocale)ticking).getRandomGridChild();
						tickStatus=Tickable.STATUS_MISC+9;
						M.bringToLife(room,true);
					}
					else
						M.bringToLife(((Room)ticking),true);
					tickStatus=Tickable.STATUS_MISC+10;
					Resources.removeResource("HELP_"+((Room)ticking).getArea().name().toUpperCase());

					Log.infoOut("Monster spawned in room: " + ((Room)ticking).displayText());
				}
				else
				if((ticking instanceof Area)&&(((Area)ticking).metroSize()>0))
				{
					tickStatus=Tickable.STATUS_MISC+11;
					Resources.removeResource("HELP_"+ticking.name().toUpperCase());
					Room room=null;
					tickStatus=Tickable.STATUS_MISC+12;
					if(restrictedLocales==null)
					{
						tickStatus=Tickable.STATUS_MISC+13;
						int tries=0;
						while(((room==null)||(!canFlyHere(M,room)))
						&&((++tries)<100))
							room=((Area)ticking).getRandomMetroRoom();
						tickStatus=Tickable.STATUS_MISC+14;
					}
					else
					{
						tickStatus=Tickable.STATUS_MISC+15;
						int tries=0;
						while(((room==null)||(!okRoomForMe(M,room)))
						&&((++tries)<100))
							room=((Area)ticking).getRandomMetroRoom();
						tickStatus=Tickable.STATUS_MISC+16;
					}
					if((room!=null)&&(room instanceof GridLocale))
						room=((GridLocale)room).getRandomGridChild();
					if(room!=null)
						M.bringToLife(room,true);
					else
						maintained.remove(M);
					tickStatus=Tickable.STATUS_MISC+17;
					if(room!=null) {
						Log.infoOut("Monster spawned in area room: " + room.displayText());
					} else {
						Log.warnOut("Failed to find suitable room for monster spawn");
					}
				}
			}
		}
		tickStatus=Tickable.STATUS_NOT;
		return true;
	}
}
