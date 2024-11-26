package com.planet_ink.coffee_mud.core;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CMParmsParseAnyTest
{

	@Test
	public void testWithCommaDelimiter()
	{
		String input = "param1,param2,param3";
		List<String> result = CMParms.parseAny(input,',',false,false);

		assertEquals(3,result.size());
		assertEquals("param1",result.get(0));
		assertEquals("param2",result.get(1));
		assertEquals("param3",result.get(2));
	}

	@Test
	public void testWithIgnoreNulls()
	{
		String input = "param1,,param3";
		List<String> result = CMParms.parseAny(input,',',true,false);

		assertEquals(2,result.size());
		assertEquals("param1",result.get(0));
		assertEquals("param3",result.get(1));
	}

	@Test
	public void testWithEmptyString()
	{
		String input = "";
		List<String> result = CMParms.parseAny(input,',',false,false);

		assertTrue(result.isEmpty());
	}

	@Test
	public void testWithNullString()
	{
		List<String> result = CMParms.parseAny(null,',',false,false);

		assertTrue(result.isEmpty());
	}

	@Test
	public void testWithEscapedDelimiter()
	{
		String input = "param1\\,param2,param3";
		List<String> result = CMParms.parseAny(input,',',true,true);

		assertEquals(2,result.size());
		assertEquals("param1,param2",result.get(0));
		assertEquals("param3",result.get(1));
	}

	@Test
	public void testWithSafeTrueAndIgnoreNulls()
	{
		String input = "param1\\,,param2,,param3";
		List<String> result = CMParms.parseAny(input,',',true,true);

		assertEquals(3,result.size());
		assertEquals("param1,",result.get(0));
		assertEquals("param2",result.get(1));
		assertEquals("param3",result.get(2));
	}

	@Test
	public void testWithSafeFalse()
	{
		String input = "param1\\,param2,param3";
		List<String> result = CMParms.parseAny(input,',',false,false);

		assertEquals(3,result.size());
		assertEquals("param1",result.get(0));
		assertEquals("param2",result.get(1));
		assertEquals("param3",result.get(2));
	}

	@Test
	public void testWithMultipleEscapedBackslashes()
	{
		String input = "param1\\\\,param2,param3";
		List<String> result = CMParms.parseAny(input,',',true,true);

		assertEquals(3,result.size(),"Expected size of result list does not match.");
		assertEquals("param1\\",result.get(0),"The first element did not match expected value.");
		assertEquals("param2",result.get(1),"The second element did not match expected value.");
		assertEquals("param3",result.get(2),"The third element did not match expected value.");
	}
}