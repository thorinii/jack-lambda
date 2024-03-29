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

package com.android.jack.opcodes.opc_goto;

import com.android.jack.DxTestCase;
import com.android.jack.opcodes.opc_goto.jm.T_opc_goto_1;
import com.android.jack.opcodes.opc_goto.jm.T_opc_goto_5;


public class Test_opc_goto extends DxTestCase {

    /**
     * @title normal test
     */
    public void testN1() {
        T_opc_goto_1 t = new T_opc_goto_1();
        assertEquals(0, t.run(20));
    }

    /**
     * @title normal test
     */
    public void testN2() {
        T_opc_goto_1 t = new T_opc_goto_1();
        assertEquals(-20, t.run(-20));
    }

    /**
     * @title  negative offset
     */
    public void testN3() {
        T_opc_goto_5 t = new T_opc_goto_5();
        assertTrue(t.run());
    }

}
