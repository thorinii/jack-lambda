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

package com.android.jack.opcodes.const_string;

import com.android.jack.DxTestCase;
import com.android.jack.opcodes.const_string.jm.T_const_string_1;


public class Test_const_string extends DxTestCase {

    /**
     * @title push string into stack
     */
    public void testN1() {
        T_const_string_1 t = new T_const_string_1();
        // lcd is hard to test isolated
        String res = t.run();
        assertEquals(5, res.length());
        assertEquals('h', res.charAt(0));
    }

}
