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

package com.android.jack.opcodes.cmpl_double;

import com.android.jack.DxTestCase;
import com.android.jack.opcodes.cmpl_double.jm.T_cmpl_double_1;


public class Test_cmpl_double extends DxTestCase {

    /**
     * @title  Arguments = Double.NaN, Double.MAX_VALUE
     */
    public void testB1() {
        T_cmpl_double_1 t = new T_cmpl_double_1();
        assertEquals(-1, t.run(Double.NaN, Double.MAX_VALUE));
    }
}
