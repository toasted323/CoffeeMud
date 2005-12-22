package com.planet_ink.coffee_mud.WebMacros;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
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
public class AbilityData extends StdWebMacro
{
	public String name()	{return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}

	// valid parms include help, ranges, quality, target, alignment, domain,
	// qualifyQ, auto
	public String runMacro(ExternalHTTPRequests httpReq, String parm)
	{
		Hashtable parms=parseParms(parm);
		String last=httpReq.getRequestParameter("ABILITY");
		if(last==null) return " @break@";
		if(last.length()>0)
		{
			Ability A=CMClass.getAbility(last);
			if(A!=null)
			{
				StringBuffer str=new StringBuffer("");
				if(parms.containsKey("HELP"))
				{
					StringBuffer s=CMLib.help().getHelpText(A.ID(),null,false);
					if(s==null)
						s=CMLib.help().getHelpText(A.Name(),null,false);
					str.append(helpHelp(s));
				}
				if(parms.containsKey("RANGES"))
				{
					int min=A.minRange();
					int max=A.maxRange();
					if(min+max==0)
						str.append("Touch, or not applicable, ");
					else
					{
						if(min==0)
							str.append("Touch");
						else
							str.append("Range "+min);
						if(max>0)
							str.append(" - Range "+max);
						str.append(", ");
					}
				}
				if(parms.containsKey("QUALITY"))
				{
					switch(A.quality())
					{
					case Ability.MALICIOUS:
						str.append("Malicious, ");
						break;
					case Ability.BENEFICIAL_OTHERS:
					case Ability.BENEFICIAL_SELF:
						str.append("Always Beneficial, ");
						break;
					case Ability.OK_OTHERS:
					case Ability.OK_SELF:
						str.append("Sometimes Beneficial, ");
						break;
					case Ability.INDIFFERENT:
						str.append("Circumstantial, ");
						break;
					}
				}
				if(parms.containsKey("AUTO"))
				{
					if(A.isAutoInvoked())
						str.append("Automatic, ");
					else
						str.append("Requires invocation, ");
				}
				if(parms.containsKey("TARGET"))
				{
					switch(A.quality())
					{
					case Ability.INDIFFERENT:
						str.append("Item or Room, ");
						break;
					case Ability.MALICIOUS:
						str.append("Others, ");
						break;
					case Ability.BENEFICIAL_OTHERS:
					case Ability.OK_OTHERS:
						str.append("Caster or others, ");
						break;
					case Ability.BENEFICIAL_SELF:
					case Ability.OK_SELF:
						str.append("Caster only, ");
						break;
					}
				}

				if(parms.containsKey("ALIGNMENT"))
				{
				    for(Enumeration e=CMLib.factions().factionSet().elements();e.hasMoreElements();)
				    {
				        Faction F=(Faction)e.nextElement();
				        if(F.usageFactors(A).length()>0)
				            str.append(F.usageFactors(A)+", ");
				    }
				}
				if(parms.containsKey("DOMAIN"))
				{
					StringBuffer thang=new StringBuffer("");
					if((A.classificationCode()&Ability.ALL_CODES)==Ability.SPELL)
					{
						int domain=A.classificationCode()&Ability.ALL_DOMAINS;
						domain=domain>>5;
						thang.append(Ability.DOMAIN_DESCS[domain].toLowerCase());
					}
					else
						thang.append(Ability.TYPE_DESCS[A.classificationCode()&Ability.ALL_CODES].toLowerCase());
					if(thang.length()>0)
					{
						thang.setCharAt(0,Character.toUpperCase(thang.charAt(0)));

						int x=thang.toString().indexOf("/");
						if(x>0) thang.setCharAt(x+1,Character.toUpperCase(thang.charAt(x+1)));
						str.append(thang.toString()+", ");
					}
				}
				if(parms.containsKey("QUALIFYQ")&&(httpReq.isRequestParameter("CLASS")))
				{
					String className=httpReq.getRequestParameter("CLASS");
					if((className!=null)&&(className.length()>0))
					{
						boolean defaultGain=CMLib.ableMapper().getDefaultGain(className,true,A.ID());
						if(!defaultGain)
							str.append("(Qualify), ");
					}
				}
				String strstr=str.toString();
				if(strstr.endsWith(", "))
					strstr=strstr.substring(0,strstr.length()-2);
                return clearWebMacros(strstr);
			}
		}
		return "";
	}
}
