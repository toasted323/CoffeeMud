package com.planet_ink.coffee_mud.common;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class DefaultTimeClock implements TimeClock
{
	public String ID(){return "DefaultTimeClock";}
	public String name(){return "Time Object";}
	public static final TimeClock globalClock=new DefaultTimeClock();
	
	protected long tickStatus=Tickable.STATUS_NOT;
	public long getTickStatus(){return tickStatus;}
	private boolean loaded=false;
	private String loadName=null;
	public void setLoadName(String name){loadName=name;}
	private int year=1;
	private int month=1;
	private int day=1;
	private int time=0;
	private int hoursInDay=16;
	private String[] monthsInYear={
			 "the 1st month","the 2nd month","the 3rd month","the 4th month",
			 "the 5th month","the 6th month","the 7th month","the 8th month",
			 "the 9th month","the 10th month","the 11th month","the 12th month"
	};
	private int daysInMonth=30;
	private int[] dawnToDusk={0,1,12,13};
	
	public int getHoursInDay(){return hoursInDay;}
	public void setHoursInDay(int h){hoursInDay=h;}
	public int getDaysInMonth(){return daysInMonth;}
	public void setDaysInMonth(int d){daysInMonth=d;}
	public int getMonthsInYear(){return monthsInYear.length;}
	public String[] getMonthNames(){return monthsInYear;}
	public void setMonthsInYear(String[] months){monthsInYear=months;}
	public int[] getDawnToDusk(){return dawnToDusk;}
	public void setDawnToDusk(int dawn, int day, int dusk, int night)
	{ 
		dawnToDusk[TIME_DAWN]=dawn;
		dawnToDusk[TIME_DAY]=day;
		dawnToDusk[TIME_DUSK]=dusk;
		dawnToDusk[TIME_NIGHT]=night;
	}
	
	public String timeDescription(MOB mob, Room room)
	{
		StringBuffer timeDesc=new StringBuffer("");

		if((Sense.canSee(mob))&&(getTODCode()>=0))
			timeDesc.append(TOD_DESC[getTODCode()]);
		timeDesc.append("(Hour: "+getTimeOfDay()+"/"+(getHoursInDay()-1)+")");
		timeDesc.append("\n\rIt is the "+getDayOfMonth()+numAppendage(getDayOfMonth()));
		timeDesc.append(" day of the "+getMonth()+numAppendage(getMonth()));
		timeDesc.append(" month.  It is "+(TimeClock.SEASON_DESCS[getSeasonCode()]).toLowerCase()+".");
		if((Sense.canSee(mob))
		&&(getTODCode()==TimeClock.TIME_NIGHT)
		&&(CoffeeUtensils.hasASky(room)))
		{
			switch(room.getArea().getClimateObj().weatherType(room))
			{
			case Climate.WEATHER_BLIZZARD:
			case Climate.WEATHER_HAIL:
			case Climate.WEATHER_SLEET:
			case Climate.WEATHER_SNOW:
			case Climate.WEATHER_RAIN:
			case Climate.WEATHER_THUNDERSTORM:
				timeDesc.append("\n\r"+room.getArea().getClimateObj().weatherDescription(room)+" You can't see the moon."); break;
			case Climate.WEATHER_CLOUDY:
				timeDesc.append("\n\rThe clouds obscure the moon."); break;
			case Climate.WEATHER_DUSTSTORM:
				timeDesc.append("\n\rThe dust obscures the moon."); break;
			default:
				if(getMoonPhase()>=0)
					timeDesc.append("\n\r"+MOON_PHASES[getMoonPhase()]);
				break;
			}
		}
		return timeDesc.toString();
	}

	private String numAppendage(int num)
	{
		switch(num)
		{
		case 1: return "st";
		case 2: return "nd";
		case 3: return "rd";
		}
		return "th";
	}

	public int getYear(){return year;}
	public void setYear(int y){year=y;}

	public int getSeasonCode(){ 
		switch(month)
		{
		case 1: return TimeClock.SEASON_WINTER;
		case 2: return TimeClock.SEASON_WINTER; 
		case 3: return TimeClock.SEASON_SPRING; 
		case 4: return TimeClock.SEASON_SPRING; 
		case 5: return TimeClock.SEASON_SPRING; 
		case 6: return TimeClock.SEASON_SUMMER; 
		case 7: return TimeClock.SEASON_SUMMER; 
		case 8: return TimeClock.SEASON_SUMMER; 
		case 9: return TimeClock.SEASON_FALL; 
		case 10:return TimeClock.SEASON_FALL; 
		case 11:return TimeClock.SEASON_FALL; 
		case 12:return TimeClock.SEASON_WINTER; 
		}
		return TimeClock.SEASON_WINTER;
	}
	public int getMonth(){return month;}
	public void setMonth(int m){month=m;}
	public int getMoonPhase(){return (int)Math.round(Math.floor(Util.mul(Util.div(getDayOfMonth(),getDaysInMonth()),8.0)));}

	public int getDayOfMonth(){return day;}
	public void setDayOfMonth(int d){day=d;}
	public int getTimeOfDay(){return time;}
	public int getTODCode()
	{
		if(time>=getDawnToDusk()[TimeClock.TIME_NIGHT])
			return TimeClock.TIME_NIGHT;
		if(time>=getDawnToDusk()[TimeClock.TIME_DUSK])
			return TimeClock.TIME_DUSK;
		if(time>=getDawnToDusk()[TimeClock.TIME_DAY])
			return TimeClock.TIME_DAY;
		if(time>=getDawnToDusk()[TimeClock.TIME_DAWN])
			return TimeClock.TIME_DAWN;
		return TimeClock.TIME_DAY;
	}
	public boolean setTimeOfDay(int t)
	{
		int oldCode=getTODCode();
		time=t;
		return getTODCode()!=oldCode;
	}

	public void raiseLowerTheSunEverywhere()
	{
		for(Enumeration r=CMMap.rooms();r.hasMoreElements();)
		{
			Room R=(Room)r.nextElement();
			if((R!=null)&&((R.numInhabitants()>0)||(R.numItems()>0)))
			{
				R.recoverEnvStats();
				for(int m=0;m<R.numInhabitants();m++)
				{
					MOB mob=R.fetchInhabitant(m);
					if(!mob.isMonster())
					{
						if(CoffeeUtensils.hasASky(R)
						&&(!Sense.isSleeping(mob))
						&&(Sense.canSee(mob)))
						{
							switch(getTODCode())
							{
							case TimeClock.TIME_DAWN:
								mob.tell("The sun begins to rise in the west.");
								break;
							case TimeClock.TIME_DAY:
								break;
								//mob.tell("The sun is now shining brightly."); break;
							case TimeClock.TIME_DUSK:
								mob.tell("The sun begins to set in the east."); break;
							case TimeClock.TIME_NIGHT:
								mob.tell("The sun has set and darkness again covers the world."); break;
							}
						}
						else
						{
							switch(getTODCode())
							{
							case TimeClock.TIME_DAWN:
								mob.tell("It is now daytime."); break;
							case TimeClock.TIME_DAY: break;
								//mob.tell("The sun is now shining brightly."); break;
							case TimeClock.TIME_DUSK: break;
								//mob.tell("It is almost nighttime."); break;
							case TimeClock.TIME_NIGHT:
								mob.tell("It is nighttime."); break;
							}
						}
					}
				}
			}
			R.recoverRoomStats();
		}
	}

	public void tickTock(int howManyHours)
	{
		if(howManyHours!=0)
		{
			boolean raiseLowerTheSun=setTimeOfDay(getTimeOfDay()+howManyHours);
			lastTicked=System.currentTimeMillis();
			if(getTimeOfDay()>=getHoursInDay())
			{
				raiseLowerTheSun=setTimeOfDay(getTimeOfDay()-getHoursInDay());
				setDayOfMonth(getDayOfMonth()+1);
				if(getDayOfMonth()>getDaysInMonth())
				{
					setDayOfMonth(1);
					setMonth(getMonth()+1);
					if(getMonth()>getMonthsInYear())
					{
						setMonth(1);
						setYear(getYear()+1);
					}
				}
			}
			else
			if(getTimeOfDay()<0)
			{
				raiseLowerTheSun=setTimeOfDay(getHoursInDay()+getTimeOfDay());
				setDayOfMonth(getDayOfMonth()-1);
				if(getDayOfMonth()<1)
				{
					setDayOfMonth(getDaysInMonth());
					setMonth(getMonth()-1);
					if(getMonth()<1)
					{
						setMonth(getMonthsInYear());
						setYear(getYear()-1);
					}
				}
			}
			if(raiseLowerTheSun) raiseLowerTheSunEverywhere();
		}
	}
	public void save()
	{
		if((loaded)&&(loadName!=null))
		{
			CMClass.DBEngine().DBDeleteData(loadName,"TIMECLOCK");
			CMClass.DBEngine().DBCreateData(loadName,"TIMECLOCK","TIMECLOCK/"+loadName,
			"<DAY>"+getDayOfMonth()+"</DAY><MONTH>"+getMonth()+"</MONTH><YEAR>"+getYear()+"</YEAR>");
		}
	}

	public long lastTicked=0;
	public boolean tick(Tickable ticking, int tickID)
	{
		tickStatus=Tickable.STATUS_NOT;
		synchronized(this)
		{
			if((loadName!=null)&&(!loaded))
			{
				loaded=true;
				Vector V=CMClass.DBEngine().DBReadData(loadName,"TIMECLOCK");
				String timeRsc=null;
				if((V==null)||(V.size()==0)||(!(V.elementAt(0) instanceof Vector)))
					timeRsc="<TIME>-1</TIME><DAY>1</DAY><MONTH>1</MONTH><YEAR>1</YEAR>";
				else
					timeRsc=(String)((Vector)V.elementAt(0)).elementAt(3);
				V=XMLManager.parseAllXML(timeRsc);
				setTimeOfDay(XMLManager.getIntFromPieces(V,"TIME"));
				setDayOfMonth(XMLManager.getIntFromPieces(V,"DAY"));
				setMonth(XMLManager.getIntFromPieces(V,"MONTH"));
				setYear(XMLManager.getIntFromPieces(V,"YEAR"));
				setHoursInDay(globalClock.getHoursInDay());
				setDaysInMonth(globalClock.getDaysInMonth());
				setMonthsInYear(globalClock.getMonthNames());
				setDawnToDusk(globalClock.getDawnToDusk()[TIME_DAWN],
							  globalClock.getDawnToDusk()[TIME_DAY],
							  globalClock.getDawnToDusk()[TIME_DUSK],
							  globalClock.getDawnToDusk()[TIME_NIGHT]);
			}
			if((System.currentTimeMillis()-lastTicked)>MudHost.TIME_MILIS_PER_MUDHOUR)
				tickTock(1);
		}
		return true;
	}
}
