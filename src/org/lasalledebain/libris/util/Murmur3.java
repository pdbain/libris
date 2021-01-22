package org.lasalledebain.libris.util;

public class Murmur3 {
	/**
	 * Licensed to the Apache Software Foundation (ASF) under one
	 * or more contributor license agreements.  See the NOTICE file
	 * distributed with this work for additional information
	 * regarding copyright ownership.  The ASF licenses this file
	 * to you under the Apache License, Version 2.0 (the
	 * "License"); you may not use this file except in compliance
	 * with the License.  You may obtain a copy of the License at
	 *
	 *     http://www.apache.org/licenses/LICENSE-2.0
	 *
	 * Unless required by applicable law or agreed to in writing, software
	 * distributed under the License is distributed on an "AS IS" BASIS,
	 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	 * See the License for the specific language governing permissions and
	 * limitations under the License.
	 */
	
	/**
	 * Modfied from 
	 * https://github.com/apache/hive/blob/master/storage-api/src/java/org/apache/hive/common/util/Murmur3.java
	 */


	/**
	 * Murmur3 is successor to Murmur2 fast non-crytographic hash algorithms.
	 *
	 * Murmur3 32 and 128 bit variants.
	 * 32-bit Java port of https://code.google.com/p/smhasher/source/browse/trunk/MurmurHash3.cpp#94
	 * 128-bit Java port of https://code.google.com/p/smhasher/source/browse/trunk/MurmurHash3.cpp#255
	 *
	 * This is a public domain code with no copyrights.
	 * From homepage of MurmurHash (https://code.google.com/p/smhasher/),
	 * "All MurmurHash versions are public domain software, and the author disclaims all copyright
	 * to their code."
	 */
	  // from 64-bit linear congruential generator
	  public static final long NULL_HASHCODE = 2862933555777941757L;

	  // Constants for 32 bit variant
	  private static final int C1_32 = 0xcc9e2d51;
	  private static final int C2_32 = 0x1b873593;
	  private static final int R1_32 = 15;
	  private static final int R2_32 = 13;
	  private static final int M_32 = 5;
	  private static final int N_32 = 0xe6546b64;

	  // Constants for 128 bit variant
	  @SuppressWarnings("unused")
	private static final long C1 = 0x87c37b91114253d5L;
	  /*
	  private static final long C2 = 0x4cf5ad432745937fL;
	  private static final int R1 = 31;
	  private static final int R2 = 27;
	  private static final int R3 = 33;
	  private static final int M = 5;
	  private static final int N1 = 0x52dce729;
	  private static final int N2 = 0x38495ab5;
	  */

	  public static final int DEFAULT_SEED = 104729;

	  /**
	   * Murmur3 32-bit variant.
	   *
	   * @param data - input byte array
	   * @return - hashcode
	   */
	  public static int hash32(byte[] data) {
	    return hash32(data, 0, data.length, DEFAULT_SEED);
	  }

	  public static int hash32(byte[] data, int length) {
		    return hash32(data, 0, length, DEFAULT_SEED);
		  }

	  /**
	   * Murmur3 32-bit variant.
	   *
	   * @param data   - input byte array
	   * @param length - length of array
	   * @param seed   - seed. (default 0)
	   * @return - hashcode
	   */
	  public static int hash32(byte[] data, int offset, int length, int seed) {
	    int hash = seed;
	    final int nblocks = length >> 2;

	    // body
	    for (int i = 0; i < nblocks; i++) {
	      int i_4 = i << 2;
	      int k = (data[i_4 + offset] & 0xff)
	          | ((data[i_4 + offset + 1] & 0xff) << 8)
	          | ((data[i_4 + offset + 2] & 0xff) << 16)
	          | ((data[i_4 + offset + 3] & 0xff) << 24);

	      // mix functions
	      k *= C1_32;
	      k = Integer.rotateLeft(k, R1_32);
	      k *= C2_32;
	      hash ^= k;
	      hash = Integer.rotateLeft(hash, R2_32) * M_32 + N_32;
	    }

	    // tail
	    int idx = nblocks << 2;
	    int k1 = 0;
	    switch (length - idx) {
	      case 3:
	        k1 ^= data[idx + offset + 2] << 16;
	      case 2:
	        k1 ^= data[idx + offset + 1] << 8;
	      case 1:
	        k1 ^= data[idx + offset];

	        // mix functions
	        k1 *= C1_32;
	        k1 = Integer.rotateLeft(k1, R1_32);
	        k1 *= C2_32;
	        hash ^= k1;
	    }

	    // finalization
	    hash ^= length;
	    hash ^= (hash >>> 16);
	    hash *= 0x85ebca6b;
	    hash ^= (hash >>> 13);
	    hash *= 0xc2b2ae35;
	    hash ^= (hash >>> 16);

	    return hash;
	  }

	  @SuppressWarnings("unused")
	private static long fmix64(long h) {
	    h ^= (h >>> 33);
	    h *= 0xff51afd7ed558ccdL;
	    h ^= (h >>> 33);
	    h *= 0xc4ceb9fe1a85ec53L;
	    h ^= (h >>> 33);
	    return h;
	  }

	  public static class IncrementalHash32 {
	    byte[] tail = new byte[3];
	    int tailLen;
	    int totalLen;
	    int hash;

	    public final void start(int hash) {
	      tailLen = totalLen = 0;
	      this.hash = hash;
	    }

	    public final void add(byte[] data, int offset, int length) {
	      if (length == 0) return;
	      totalLen += length;
	      if (tailLen + length < 4) {
	        System.arraycopy(data, offset, tail, tailLen, length);
	        tailLen += length;
	        return;
	      }
	      int offset2 = 0;
	      if (tailLen > 0) {
	        offset2 = (4 - tailLen);
	        int k = -1;
	        switch (tailLen) {
	        case 1:
	          k = orBytes(tail[0], data[offset], data[offset + 1], data[offset + 2]);
	          break;
	        case 2:
	          k = orBytes(tail[0], tail[1], data[offset], data[offset + 1]);
	          break;
	        case 3:
	          k = orBytes(tail[0], tail[1], tail[2], data[offset]);
	          break;
	        default: throw new AssertionError(tailLen);
	        }
	        // mix functions
	        k *= C1_32;
	        k = Integer.rotateLeft(k, R1_32);
	        k *= C2_32;
	        hash ^= k;
	        hash = Integer.rotateLeft(hash, R2_32) * M_32 + N_32;
	      }
	      int length2 = length - offset2;
	      offset += offset2;
	      final int nblocks = length2 >> 2;

	      for (int i = 0; i < nblocks; i++) {
	        int i_4 = (i << 2) + offset;
	        int k = orBytes(data[i_4], data[i_4 + 1], data[i_4 + 2], data[i_4 + 3]);

	        // mix functions
	        k *= C1_32;
	        k = Integer.rotateLeft(k, R1_32);
	        k *= C2_32;
	        hash ^= k;
	        hash = Integer.rotateLeft(hash, R2_32) * M_32 + N_32;
	      }

	      int consumed = (nblocks << 2);
	      tailLen = length2 - consumed;
	      if (consumed == length2) return;
	      System.arraycopy(data, offset + consumed, tail, 0, tailLen);
	    }

	    public final int end() {
	      int k1 = 0;
	      switch (tailLen) {
	        case 3:
	          k1 ^= tail[2] << 16;
	        case 2:
	          k1 ^= tail[1] << 8;
	        case 1:
	          k1 ^= tail[0];

	          // mix functions
	          k1 *= C1_32;
	          k1 = Integer.rotateLeft(k1, R1_32);
	          k1 *= C2_32;
	          hash ^= k1;
	      }

	      // finalization
	      hash ^= totalLen;
	      hash ^= (hash >>> 16);
	      hash *= 0x85ebca6b;
	      hash ^= (hash >>> 13);
	      hash *= 0xc2b2ae35;
	      hash ^= (hash >>> 16);
	      return hash;
	    }
	  }

	  private static int orBytes(byte b1, byte b2, byte b3, byte b4) {
	    return (b1 & 0xff) | ((b2 & 0xff) << 8) | ((b3 & 0xff) << 16) | ((b4 & 0xff) << 24);
	  }
}
