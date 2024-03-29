/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.jack.opcodes.aget_wide;

import com.android.jack.DxTestCase;
import com.android.jack.opcodes.aget_wide.jm.T_aget_wide_1;
import com.android.jack.opcodes.aget_wide.jm.T_aget_wide_2;
import com.android.jack.opcodes.aget_wide.jm.T_aget_wide_3;
import com.android.jack.opcodes.aget_wide.jm.T_aget_wide_4;
import com.android.jack.opcodes.aget_wide.jm.T_aget_wide_5;


public class Test_aget_wide extends DxTestCase {

    /**
     * @title normal test. Trying different indexes
     */
    public void testN1() {
        T_aget_wide_1 t = new T_aget_wide_1();
        double[] arr = new double[2];
        arr[1] = 3.1415d;
        assertEquals(3.1415d, t.run(arr, 1));
    }

    /**
     * @title normal test. Trying different indexes
     */
    public void testN2() {
        T_aget_wide_1 t = new T_aget_wide_1();
        double[] arr = new double[2];
        arr[0] = 3.1415d;
        assertEquals(3.1415d, t.run(arr, 0));
    }

    /**
     * @title expected ArrayIndexOutOfBoundsException
     */
    public void testE1() {
        T_aget_wide_1 t = new T_aget_wide_1();
        double[] arr = new double[2];
        try {
            t.run(arr, 2);
            fail("expected ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException aie) {
            // expected
        }
    }

    /**
     * @title expected NullPointerException
     */
    public void testE2() {
        T_aget_wide_1 t = new T_aget_wide_1();
        try {
            t.run(null, 2);
            fail("expected NullPointerException");
        } catch (NullPointerException aie) {
            // expected
        }
    }

    /**
     * @title expected ArrayIndexOutOfBoundsException
     */
    public void testE3() {
        T_aget_wide_1 t = new T_aget_wide_1();
        double[] arr = new double[2];
        try {
            t.run(arr, -1);
            fail("expected ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException aie) {
            // expected
        }
    }

    /**
     * @title normal test. trying different indexes
     */
    public void testN1Long() {
        T_aget_wide_2 t = new T_aget_wide_2();
        long[] arr = new long[2];
        arr[1] = 1000000000000000000l;
        assertEquals(1000000000000000000l, t.run(arr, 1));
    }

    /**
     * @title normal test. trying different indexes
     */
    public void testN2Long() {
        T_aget_wide_2 t = new T_aget_wide_2();
        long[] arr = new long[2];
        arr[0] = 1000000000000000000l;
        assertEquals(1000000000000000000l, t.run(arr, 0));
    }

    /**
     * @title normal test. trying different indexes
     */
    public void testN3Long() {
        T_aget_wide_3 t = new T_aget_wide_3();
        long[] arr = new long[2];
        arr[0] = 1000000000000000000l;
        assertEquals(1000000000000000000l, t.run(arr, 0));
    }

    /**
     * @title normal test. trying different indexes
     */
    public void testN4Long() {
        T_aget_wide_4 t = new T_aget_wide_4();
        long[] arr = new long[2];
        arr[0] = 1000000000000000000l;
        assertEquals(1000000000000000000l, t.run(arr, 0));
    }

    /**
     * @title normal test. trying different indexes
     */
    public void testN5Long() {
        T_aget_wide_5 t = new T_aget_wide_5();
        long[] arr = new long[2];
        arr[0] = 1000000000000000000l;
        assertEquals(1000000000000000000l, t.run(arr, 0));
    }

    /**
     * @title  Exception - ArrayIndexOutOfBoundsException
     */
    public void testE1Long() {
        T_aget_wide_2 t = new T_aget_wide_2();
        long[] arr = new long[2];
        try {
            t.run(arr, 2);
            fail("expected ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException aie) {
            // expected
        }
    }

    /**
     * @title  Exception - NullPointerException
     */
    public void testE2Long() {
        T_aget_wide_2 t = new T_aget_wide_2();
        try {
            t.run(null, 2);
            fail("expected NullPointerException");
        } catch (NullPointerException np) {
            // expected
        }
    }

    /**
     * @title  Exception - ArrayIndexOutOfBoundsException
     */
    public void testE3Long() {
        T_aget_wide_2 t = new T_aget_wide_2();
        long[] arr = new long[2];
        try {
            t.run(arr, -1);
            fail("expected ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException aie) {
            // expected
        }
    }
}
