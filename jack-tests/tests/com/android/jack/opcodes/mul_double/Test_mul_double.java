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

package com.android.jack.opcodes.mul_double;

import com.android.jack.DxTestCase;
import com.android.jack.opcodes.mul_double.jm.T_mul_double_1;
import com.android.jack.opcodes.mul_double.jm.T_mul_double_3;
import com.android.jack.opcodes.mul_double.jm.T_mul_double_4;


public class Test_mul_double extends DxTestCase {

    /**
     * @title  Arguments = 2.7d, 3.14d
     */

    public void testN1() {
        T_mul_double_1 t = new T_mul_double_1();
        assertEquals(8.478000000000002d, t.run(2.7d, 3.14d));
    }

    /**
     * @title  Arguments = 0, -3.14d
     */
    public void testN2() {
        T_mul_double_1 t = new T_mul_double_1();
        assertEquals(-0d, t.run(0, -3.14d));
    }

    /**
     * @title  Arguments = -2.7d, -3.14d
     */
    public void testN3() {
        T_mul_double_1 t = new T_mul_double_1();
        assertEquals(8.478000000000002d, t.run(-3.14d, -2.7d));
    }

    /**
     * @title  Arguments = -2.7d, -3.14d
     */
    public void testN4() {
        T_mul_double_3 t = new T_mul_double_3();
        assertEquals(8.478000283241272d, t.run(-3.14f, -2.7d));
    }

    /**
     * @title  Arguments = -2.7d, -3.14d
     */
    public void testN5() {
        T_mul_double_4 t = new T_mul_double_4();
        assertEquals(847.8000000000001d, t.run(-314l, -2.7d));
    }

    /**
     * @title  Arguments = Double.MAX_VALUE, Double.NaN
     */
    public void testB1() {
        T_mul_double_1 t = new T_mul_double_1();
        assertEquals(Double.NaN, t.run(Double.MAX_VALUE, Double.NaN));
    }

    /**
     * @title  Arguments = Double.POSITIVE_INFINITY, 0
     */
    public void testB2() {
        T_mul_double_1 t = new T_mul_double_1();
        assertEquals(Double.NaN, t.run(Double.POSITIVE_INFINITY, 0));
    }

    /**
     * @title  Arguments = Double.POSITIVE_INFINITY, -2.7d
     */
    public void testB3() {
        T_mul_double_1 t = new T_mul_double_1();
        assertEquals(Double.NEGATIVE_INFINITY, t.run(Double.POSITIVE_INFINITY,
                -2.7d));
    }

    /**
     * @title  Arguments = Double.POSITIVE_INFINITY,
     * Double.NEGATIVE_INFINITY
     */
    public void testB4() {
        T_mul_double_1 t = new T_mul_double_1();
        assertEquals(Double.NEGATIVE_INFINITY, t.run(Double.POSITIVE_INFINITY,
                Double.NEGATIVE_INFINITY));
    }

    /**
     * @title  Arguments = +0, -0d
     */
    public void testB5() {
        T_mul_double_1 t = new T_mul_double_1();
        assertEquals(-0d, t.run(+0d, -0d));
    }

    /**
     * @title  Arguments = -0d, -0d
     */
    public void testB6() {
        T_mul_double_1 t = new T_mul_double_1();
        assertEquals(+0d, t.run(-0d, -0d));
    }

    /**
     * @title  Arguments = Double.MAX_VALUE, Double.MAX_VALUE
     */
    public void testB7() {
        T_mul_double_1 t = new T_mul_double_1();
        assertEquals(Double.POSITIVE_INFINITY, t.run(Double.MAX_VALUE,
                Double.MAX_VALUE));
    }

    /**
     * @title  Arguments = Double.MIN_VALUE, -1.4E-45f
     */
    public void testB8() {
        T_mul_double_1 t = new T_mul_double_1();
        assertEquals(-0d, t.run(Double.MIN_VALUE, -1.4E-45f));
    }
}
