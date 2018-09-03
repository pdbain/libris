package org.lasalledebain;

import java.util.ArrayList;

import org.junit.Test;
import org.lasalledebain.libris.util.Murmur3;

import junit.framework.TestCase;

public class UtilitiesTest extends TestCase {
	byte[][] testData = {
			"".getBytes(),
			"a".getBytes(),
			"bc".getBytes(),
			"def".getBytes(),
			"ghij".getBytes(),
			"klmno".getBytes(),
			"pqrstu".getBytes(),
			"It was a dark and stormy night".getBytes()
	};

	
	@Test
	public void testSantiy() {
		ArrayList<Integer> exisitingHashes = new ArrayList<>();
		
		for (byte[] d: testData) {
			Integer result = Integer.valueOf(Murmur3.hash32(d));
			assertFalse("duplicate hash", exisitingHashes.contains(result));
			exisitingHashes.add(result);
		}
		for (byte[] d: testData) {
			Integer result = Integer.valueOf(Murmur3.hash32(d, 0, d.length, 56789));
			assertFalse("duplicate hash", exisitingHashes.contains(result));
			exisitingHashes.add(result);
		}
	}

	@Test
	public void testOffset() {
		byte baseString[] = "abcdefghij".getBytes();
		byte omitFirstByte[] = "bcdefghij".getBytes();
		byte omitLastByte[] = "abcdefghi".getBytes();
		byte middleBytes[] = "defg".getBytes();
		assertEquals(Murmur3.hash32(baseString, 1, omitFirstByte.length,Murmur3.DEFAULT_SEED), Murmur3.hash32(omitFirstByte));
		assertEquals(Murmur3.hash32(baseString, 0, omitLastByte.length,Murmur3.DEFAULT_SEED), Murmur3.hash32(omitLastByte));
		assertEquals(Murmur3.hash32(baseString, 3, middleBytes.length,Murmur3.DEFAULT_SEED), Murmur3.hash32(middleBytes));
	}
}
