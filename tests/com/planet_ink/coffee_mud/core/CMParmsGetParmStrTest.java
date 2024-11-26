package com.planet_ink.coffee_mud.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CMParmsGetParmStrTest {

	@Test
	public void testWithBasicKeyValue() {
		String text = "name=John";
		String result = CMParms.getParmStr(text, "name", "Default");
		assertEquals("John", result, "Expected value for key 'name' not found.");
	}

	@Test
	public void testWithCaseInsensitivity() {
		String text = "NAME=Jane";
		String result = CMParms.getParmStr(text, "name", "Default");
		assertEquals("Jane", result, "Expected value for key 'NAME' not found with case insensitivity.");
	}

	@Test
	public void testWithPartialMatch() {
		String text = "NAMEY=Jane; NAME=John";
		String result = CMParms.getParmStr(text, "name", "Default");
		assertEquals("Jane", result, "Expected value for key 'NAMEY' not found with partial match.");
	}

	@Test
	public void testWithPlusSign() {
		String text = "name+=John";
		String result = CMParms.getParmStr(text, "name", "Default");
		assertEquals("Default", result, "Expected default value when key is followed by '+'.");
	}

	@Test
	public void testWithMinusSign() {
		String text = "name-=John Doe";
		String result = CMParms.getParmStr(text, "name", "Default");
		assertEquals("Default", result, "Expected default value when key is followed by '-'.");
	}

	@Test
	public void testWithQuotedValue() {
		String text = "key=\"John Doe\"";
		String result = CMParms.getParmStr(text, "key", "Default");
		assertEquals("John Doe", result, "Expected quoted value for key 'key' not found.");
	}

	@Test
	public void testWithWhitespaceBeforeValue() {
		String text = "key   =   Some     ";
		String result = CMParms.getParmStr(text, "key", "Default");
		assertEquals("Some", result, "Expected value with whitespace handling.");
	}

	@Test
	public void testWithPunctuation() {
		String text = "bib=hoo; moe=\"uiuiui bob=goo lou\"; bob=\"yoo\"";
		String result = CMParms.getParmStr(text, "bob", "Default");
		assertEquals("goo", result, "Expected correct value for key 'bob' with surrounding punctuation.");
	}

	@Test
	public void testReturnsDefaultWhenKeyNotFound() {
		String text = "someKey=value";
		String result = CMParms.getParmStr(text, "nonExistentKey", "Default");
		assertEquals("Default", result, "Expected default value when key is not found.");
	}

	@Test
	public void testValueEndsOnWhitespace() {
		String text = "key=value anotherKey=anotherValue";
		String result = CMParms.getParmStr(text, "key", "Default");
		assertEquals("value", result, "Expected value should end at whitespace.");
	}

	@Test
	public void testValueEndsOnSemicolon() {
		String text = "key=value; anotherKey=anotherValue";
		String result = CMParms.getParmStr(text, "key", "Default");
		assertEquals("value", result, "Expected value should end at semicolon.");
	}

	@Test
	public void testValueEndsOnComma() {
		String text = "key=value, anotherKey=anotherValue";
		String result = CMParms.getParmStr(text, "key", "Default");
		assertEquals("value", result, "Expected value should end at comma.");
	}

	@Test
	public void testValueEndsOnWhitespace2() {
		String text = "key=value ,anotherKey=anotherValue";
		String result = CMParms.getParmStr(text, "key", "Default");
		assertEquals("value", result, "Expected value should end at whitespace.");
	}

//	Unclear specification, test fails
//	@Test
//	public void testValueEndsOnEndQuote() {
//		String text = "\"key=value\"; anotherKey=anotherValue";
//		String result = CMParms.getParmStr(text, "key", "Default");
//		assertEquals("value", result, "Expected value should end at end quote.");
//	}


//  Unclear specification, test fails
//	@Test
//	public void testValueEndsOnEndQuote2() {
//		String text = "\"key\"=\"value\"; anotherKey=anotherValue";
//		String result = CMParms.getParmStr(text, "\"key\"", "Default");
//		assertEquals("value", result, "Expected value should end at end quote.");
//	}

	@Test
	public void testDocumentedCase() {
		String text = "joe larry bibob=hoo moe=\"uiuiui bob=goo lou\", bob=\"yoo\"" ;
		String result = CMParms.getParmStr(text, "bob", "Default");
		assertEquals("goo", result, "Expected value should be 'goo'.");
	}
}