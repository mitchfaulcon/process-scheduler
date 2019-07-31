package se306.scheduler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.SimpleJSAP;

/**
 * This class tests the ability to parse arguments from the command line
 * in {@link ProcessScheduler}
 */
public class ArgParseTest {
	private static SimpleJSAP jsap;
	
	@BeforeAll
	static void setup() {
		jsap = ProcessScheduler.buildParser();
	}

	/**
	 * Test parsing of a simple input with no extra options.
	 */
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

	/**
	 * Test parsing of input with a custom number of processors/threads
	 */
	@Test
	void customN() {
		JSAPResult result = jsap.parse("input.dot 5 -p 3");
		assertTrue(result.success());
		assertEquals(3, result.getInt("N"));
	}

	/**
	 * Test parsing of input with visual enabled
	 */
	@Test
	void visualEnabled() {
		JSAPResult result = jsap.parse("input.dot 5 -v");
		assertTrue(result.success());
		assertTrue(result.getBoolean("V"));
	}

	/**
	 * Test parsing of input with a specified output file
	 */
	@Test
	void customOutput() {
		JSAPResult result = jsap.parse("input.dot 5 -o out.dot");
		assertTrue(result.success());
		assertEquals("out.dot", result.getString("OUTPUT", result.getString("INPUT") + "-output.dot"));
	}

	/**
	 * Test parsing of input with an option selected but no
	 * argument entered
	 */
	@Test
	void missingArgument() {
		JSAPResult result = jsap.parse("input.dot 5 -p ");
		assertFalse(result.success());
	}

	/**
	 * Test parsing of input with an invalid argument entered
	 * for an option
	 */
	@Test
	void invalidArgument() {
		JSAPResult result = jsap.parse("input.dot 5 -p two");
		assertFalse(result.success());
	}

	/**
	 * Test parsing of input with an unknown flag
	 */
	@Test
	void unknownFlag() {
		JSAPResult result = jsap.parse("input.dot 5 -e");
		assertFalse(result.success());
	}

	/**
	 * Test parsing of input with multiple flags
	 */
	@Test
	void multipleFlags() {
		JSAPResult result = jsap.parse("input.dot 5 -v -p 2 -o out.dot");
		assertTrue(result.success());
		assertTrue(result.getBoolean("V"));
		assertEquals(2, result.getInt("N"));
		assertEquals("out.dot", result.getString("OUTPUT", result.getString("INPUT") + "-output.dot"));
	}
}
