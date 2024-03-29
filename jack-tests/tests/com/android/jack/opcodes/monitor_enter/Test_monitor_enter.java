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

package com.android.jack.opcodes.monitor_enter;

import com.android.jack.DxTestCase;
import com.android.jack.opcodes.monitor_enter.jm.T_monitor_enter_1;
import com.android.jack.opcodes.monitor_enter.jm.T_monitor_enter_2;
import com.android.jack.opcodes.monitor_enter.jm.T_monitor_enter_4;


public class Test_monitor_enter extends DxTestCase {

    /**
     * @title normal test
     *
     * @throws InterruptedException
     */
    public void testN1() throws InterruptedException {
        //@uses com.android.jack.opcodes.monitor_enter.TestRunnable
        final T_monitor_enter_1 t1 = new T_monitor_enter_1();
        Runnable r1 = new TestRunnable(t1);
        Runnable r2 = new TestRunnable(t1);
        Thread tr1 = new Thread(r1);
        Thread tr2 = new Thread(r2);
        tr1.start();
        tr2.start();

        tr1.join();
        tr2.join();
        assertEquals(2, t1.counter);
    }

    /**
     * @title  Tests behavior when monitor owned by current thread.
     *
     * @throws InterruptedException
     */
    public void testN2() throws InterruptedException {
        //@uses com.android.jack.opcodes.monitor_enter.TestRunnable2
        final T_monitor_enter_2 t1 = new T_monitor_enter_2();
        Runnable r1 = new TestRunnable2(t1, 10);
        Runnable r2 = new TestRunnable2(t1, 20);
        Thread tr1 = new Thread(r1);
        Thread tr2 = new Thread(r2);
        tr1.start();
        tr2.start();

        tr1.join();
        tr2.join();
        assertTrue(t1.result);
    }


    /**
     * @title expected NullPointerException
     */
    public void testE1() {
        T_monitor_enter_4 t = new T_monitor_enter_4();
        try {
            t.run();
            fail("expected NullPointerException");
        } catch (NullPointerException npe) {
            // expected
        }
    }

}

class TestRunnable implements Runnable {
    private T_monitor_enter_1 t1;
    TestRunnable(T_monitor_enter_1 t1) {
        this.t1 = t1;
    }

    public void run() {
        try {
            t1.run();
        } catch (InterruptedException e) {
            throw new RuntimeException("interrupted!");
        }
    }
}

class TestRunnable2 implements Runnable {
    private T_monitor_enter_2 t2;
    private int val;
    TestRunnable2(T_monitor_enter_2 t2, int val) {
        this.t2 = t2;
        this.val = val;
    }

    public void run() {
        try {
            t2.run(val);
        } catch (InterruptedException e) {
            throw new RuntimeException("interrupted!");
        }
    }
}
