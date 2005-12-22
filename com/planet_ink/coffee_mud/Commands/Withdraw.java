package com.planet_ink.coffee_mud.Commands;
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
public class Withdraw extends StdCommand
{
	public Withdraw(){}

	private String[] access={"WITHDRAW"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		Environmental shopkeeper=CMLib.english().parseShopkeeper(mob,commands,"Withdraw what or how much from whom?");
		if(shopkeeper==null) return false;
        ShopKeeper SHOP=CMLib.coffeeShops().getShopKeeper(shopkeeper);
		if((!(SHOP instanceof Banker))&&(!(SHOP instanceof PostOffice)))
		{
			mob.tell("You can not withdraw anything from "+shopkeeper.name()+".");
			return false;
		}
		if(commands.size()==0)
		{
			mob.tell("Withdraw what or how much?");
			return false;
		}
		String str=CMParms.combine(commands,0);
		if(str.equalsIgnoreCase("all")) str=""+Integer.MAX_VALUE;
	    long numCoins=CMLib.english().numPossibleGold(null,str);
	    String currency=CMLib.english().numPossibleGoldCurrency(shopkeeper,str);
	    double denomination=CMLib.english().numPossibleGoldDenomination(shopkeeper,currency,str);
		Item thisThang=null;
        if(SHOP instanceof Banker)
        {
    		if(numCoins>0)
    		{
    		    if(denomination==0.0)
    		    {
    				mob.tell("Withdraw how much?");
    				return false;
    		    }
				thisThang=((Banker)SHOP).findDepositInventory(mob,""+Integer.MAX_VALUE);
				if(thisThang instanceof Coins)
				    thisThang=CMLib.beanCounter().makeCurrency(currency,denomination,numCoins);
    		}
    		else
    			thisThang=((Banker)SHOP).findDepositInventory(mob,str);
    
    		if(((thisThang==null)||((thisThang instanceof Coins)&&(((Coins)thisThang).getNumberOfCoins()<=0)))
    		&&(((Banker)SHOP).whatIsSold()!=ShopKeeper.DEAL_CLANBANKER)
    		&&(mob.isMarriedToLiege()))
    		{
    			MOB mob2=CMLib.map().getPlayer(mob.getLiegeID());
    			if(numCoins>0)
    			{
    				thisThang=((Banker)SHOP).findDepositInventory(mob2,""+Integer.MAX_VALUE);
    				if(thisThang instanceof Coins)
    				    thisThang=CMLib.beanCounter().makeCurrency(currency,denomination,numCoins);
    				else
    			    {
    					mob.tell("Withdraw how much?");
    					return false;
    			    }
    			}
    			else
    				thisThang=((Banker)SHOP).findDepositInventory(mob2,str);
    		}
        }
        else
        if(SHOP instanceof PostOffice)
        {
            thisThang=((PostOffice)SHOP).findBoxContents(mob,str);
            if((thisThang==null)
            &&(((PostOffice)SHOP).whatIsSold()!=ShopKeeper.DEAL_CLANPOSTMAN)
            &&(mob.isMarriedToLiege()))
            {
                MOB mob2=CMLib.map().getPlayer(mob.getLiegeID());
                thisThang=((PostOffice)SHOP).findBoxContents(mob2,str);
            }
        }

		if((thisThang==null)||(!CMLib.flags().canBeSeenBy(thisThang,mob)))
		{
			mob.tell("That doesn't appear to be available.  Try LIST.");
			return false;
		}
        String str2="<S-NAME> withdraw(s) <O-NAME> from <S-HIS-HER> account with "+shopkeeper.name()+".";
        if(SHOP instanceof PostOffice)
            str2="<S-NAME> withdraw(s) <O-NAME> from <S-HIS-HER> postal box with "+shopkeeper.name()+".";
		CMMsg newMsg=CMClass.getMsg(mob,shopkeeper,thisThang,CMMsg.MSG_WITHDRAW,str2);
		if(!mob.location().okMessage(mob,newMsg))
			return false;
		mob.location().send(mob,newMsg);
		return false;
	}
    public double combatActionsCost(){return 1.0;}
    public double actionsCost(){return 0.25;}
	public boolean canBeOrdered(){return false;}

	
}
