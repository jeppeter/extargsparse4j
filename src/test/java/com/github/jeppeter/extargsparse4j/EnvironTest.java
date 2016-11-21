package com.github.jeppeter.extargsparse4j;

import com.github.jeppeter.extargsparse4j.Environ;

import static org.junit.Assert.assertEquals;
import org.junit.*;

public class EnvironTest {
	@Test
	public void test_A001() throws Exception {
		String oldval;
		String newval;
		oldval = System.getenv("PATH");
		newval = Environ.getenv("PATH");
		assertEquals("Path",oldval,newval);
		return;
	}

	@Test
	public void test_A002() throws Exception {
		String oldval1,oldval2;
		String newval1,newval2;
		String key="newkey";
		oldval1 = System.getenv(key);
		oldval2 = Environ.getenv(key);
		newval1 = "newval";
		if (newval1.equals(oldval2)) {
			newval1 = "newval2";
		}
		Environ.setenv(key,newval1);
		newval2 = Environ.getenv(key);
		assertEquals(String.format("%s=%s",key,newval1),newval1,newval2);
		Environ.setenv(key,oldval1);
		return ;
	}
}

