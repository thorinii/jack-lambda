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

package com.android.jack.opcodes.xor_long;

import com.android.jack.DxTestCase;
import com.android.jack.opcodes.xor_long.jm.T_xor_long_1;
import com.android.jack.opcodes.xor_long.jm.T_xor_long_3;
import com.android.jack.opcodes.xor_long.jm.T_xor_long_4;
import com.android.jack.opcodes.xor_long.jm.T_xor_long_5;


public class Test_xor_long extends DxTestCase {

    /**
     * @title Arguments = 23423432423777l, 23423432423778l
     */
    public void testN1() {
        T_xor_long_1 t = new T_xor_long_1();
        assertEquals(3, t.run(23423432423777l, 23423432423778l));
    }

    /**
     * @title Arguments = 0xfffffff5, 0xfffffff1
     */
    public void testN2() {
        T_xor_long_1 t = new T_xor_long_1();
        assertEquals(4, t.run(0xfffffff5, 0xfffffff1));
    }

    /**
     * @title  Arguments = 0xABCDEFAB & -1
     */
    public void testN3() {
        T_xor_long_1 t = new T_xor_long_1();
        assertEquals(0x54321054, t.run(0xABCDEFAB, -1l));
    }

    /**
     * @title  Arguments = 0xABCDEFAB & -1
     */
    public void testN4() {
        T_xor_long_3 t = new T_xor_long_3();
        assertEquals(0x54321054, t.run(0xABCDEFAB, -1l));
    }

    /**
     * @title  Arguments = 0xABCDEFAB & -1
     */
    public void testN5() {
        T_xor_long_4 t = new T_xor_long_4();
        assertEquals(0x54321054, t.run(0xABCDEFAB, -1l));
    }

    /**
     * @title  Arguments = 0xABCDEFAB & -1
     */
    public void testN6() {
        T_xor_long_5 t = new T_xor_long_5();
        assertEquals(0x5432107F, t.run(0xABCDEFAB, -1l));
    }

    /**
     * @title  Arguments = 0 & -1
     */
    public void testB1() {
        T_xor_long_1 t = new T_xor_long_1();
        assertEquals(-1l, t.run(0l, -1l));
    }

    /**
     * @title  Arguments = Long.MAX_VALUE & Long.MIN_VALUE
     */
    public void testB2() {
        T_xor_long_1 t = new T_xor_long_1();
        assertEquals(0xffffffff, t.run(Long.MAX_VALUE, Long.MIN_VALUE));
    }

    /**
     * @title  Arguments = Long.MAX_VALUE & Long.MAX_VALUE
     */
    public void testB3() {
        T_xor_long_1 t = new T_xor_long_1();
        assertEquals(0l, t.run(Long.MAX_VALUE, Long.MAX_VALUE));
    }

}
