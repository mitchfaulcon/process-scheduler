package se306.scheduler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.SimpleJSAP;

public class ArgParseTest {
	private static SimpleJSAP jsap;
	
	@BeforeAll
	static void setup() {
		jsap = ProcessScheduler.buildParser();
	}

	@Test
	void validSimple() {
		JSAPResult result = jsap.parse("input.dot 5");
		assertTrue(result.success());
		//Arguments
		assertEquals("input.dot", result.getString("INPUT"));
		assertEquals(5, result.getInt("P"));
		//Test optional defaults
		assertEquals(1, result.getInt("N"));
		assertEquals(false, result.getBoolean("V"));
		assertEquals("input.dot-output.dot", result.getString("OUTPUT", result.getString("INPUT") + "-output.dot"));
	}
	
	@Test
	void customN() {
		JSAPResult result = jsap.parse("input.dot 5 -p 5");
		assertTrue(result.success());
		assertEquals(5, result.getInt("N"));
	}
	
	@Test
	void visualEnabled() {
		JSAPResult result = jsap.parse("input.dot 5 -v");
		assertTrue(result.success());
		assertEquals(true, result.getBoolean("V"));
	}
	
	@Test
	void customOutput() {
		JSAPResult result = jsap.parse("input.dot 5 -o out.dot");
		assertTrue(result.success());
		assertEquals("out.dot", result.getString("OUTPUT", result.getString("INPUT") + "-output.dot"));
	}
	
	@Test
	void missingArgument() {
		JSAPResult result = jsap.parse("input.dot 5 -p ");
		assertFalse(result.success());
	}
	
	@Test
	void invlaidArgument() {
		JSAPResult result = jsap.parse("input.dot 5 -p two");
		assertFalse(result.success());
	}
	
	@Test
	void unknownFlag() {
		JSAPResult result = jsap.parse("input.dot 5 -e");
		assertFalse(result.success());
	}
	
	
	public static void mhnain(String[] args) {
		jsap = ProcessScheduler.buildParser();
		
		JSAPResult result = jsap.parse("input.dot");
		System.out.println(result.success());
		
		result = jsap.parse("input.dot 3");
		System.out.println(result.success());
		
		result = jsap.parse("input.dot 3 -p 5 -v");
		System.out.println(result.success());
		
	}
	
	
}
