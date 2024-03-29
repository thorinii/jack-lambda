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

package com.android.jack.opcodes.sub_int;

import com.android.jack.DxTestCase;
import com.android.jack.opcodes.sub_int.jm.T_sub_int_1;
import com.android.jack.opcodes.sub_int.jm.T_sub_int_3;
import com.android.jack.opcodes.sub_int.jm.T_sub_int_4;


public class Test_sub_int extends DxTestCase {

    /**
     * @title Arguments = 8, 4
     */
    public void testN1() {
        T_sub_int_1 t = new T_sub_int_1();
        assertEquals(4, t.run(8, 4));
    }

    /**
     * @title Arguments = 0, 255
     */
    public void testN2() {
        T_sub_int_1 t = new T_sub_int_1();
        assertEquals(-255, t.run(0, 255));
    }

    /**
     * @title Arguments = 0, -65536
     */
    public void testN3() {
        T_sub_int_1 t = new T_sub_int_1();
        assertEquals(65536, t.run(0, -65536));
    }

    /**
     * @title Arguments = 0, -2147483647
     */
    public void testN4() {
        T_sub_int_1 t = new T_sub_int_1();
        assertEquals(Integer.MAX_VALUE, t.run(0, -2147483647));
    }

    /**
     * @title Arguments = 0, -2147483647
     */
    public void testN5() {
        T_sub_int_3 t = new T_sub_int_3();
        assertEquals(Integer.MAX_VALUE, t.run(0, -2147483647));
    }

    /**
     * @title Arguments = 0, -2147483647
     */
    public void testN6() {
        T_sub_int_4 t = new T_sub_int_4();
        assertEquals(Integer.MAX_VALUE, t.run(0, -2147483647));
    }

    /**
     * @title Arguments = 0, Integer.MAX_VALUE
     */
    public void testB1() {
        T_sub_int_1 t = new T_sub_int_1();
        assertEquals(-2147483647, t.run(0, Integer.MAX_VALUE));
    }

    /**
     * @title Arguments = Integer.MAX_VALUE, Integer.MAX_VALUE
     */
    public void testB2() {
        T_sub_int_1 t = new T_sub_int_1();
        assertEquals(0, t.run(Integer.MAX_VALUE, Integer.MAX_VALUE));
    }

    /**
     * @title Arguments = Integer.MAX_VALUE, -1
     */
    public void testB3() {
        T_sub_int_1 t = new T_sub_int_1();
        assertEquals(Integer.MIN_VALUE, t.run(Integer.MAX_VALUE, -1));
    }

    /**
     * @title Arguments = Integer.MIN_VALUE, 1
     */
    public void testB4() {
        T_sub_int_1 t = new T_sub_int_1();
        assertEquals(Integer.MAX_VALUE, t.run(Integer.MIN_VALUE, 1));
    }

    /**
     * @title Arguments = 0, 0
     */
    public void testB5() {
        T_sub_int_1 t = new T_sub_int_1();
        assertEquals(0, t.run(0, 0));
    }

    /**
     * @title Arguments = 0, -Integer.MIN_VALUE
     */
    public void testB6() {
        T_sub_int_1 t = new T_sub_int_1();
        assertEquals(-2147483648, t.run(0, -Integer.MIN_VALUE));
    }

    /**
     * @title Arguments = Integer.MAX_VALUE, 1
     */
    public void testB7() {
        T_sub_int_1 t = new T_sub_int_1();
        assertEquals(2147483646, t.run(Integer.MAX_VALUE, 1));
    }

    /**
     * @title Arguments = 1, Integer.MIN_VALUE
     */
    public void testB8() {
        T_sub_int_1 t = new T_sub_int_1();
        assertEquals(-2147483647, t.run(1, Integer.MIN_VALUE));
    }

    /**
     * @title Arguments = Integer.MAX_VALUE, Integer.MIN_VALUE
     */
    public void testB9() {
        T_sub_int_1 t = new T_sub_int_1();
        assertEquals(-1, t.run(Integer.MAX_VALUE, Integer.MIN_VALUE));
    }

}
