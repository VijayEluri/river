/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sun.jini.test.spec.javaspace.conformance;

import java.util.logging.Level;

// net.jini
import net.jini.space.JavaSpace;

// com.sun.jini
import com.sun.jini.qa.harness.TestException;
import com.sun.jini.qa.harness.QAConfig;

/**
 * MultipleTakeIfExistsTest asserts that for takeIfExists with timeouts
 * other then NO_WAIT:
 * 1) If a takeIfExists returns a non-null value, the entry has been removed
 *    from the space.
 * 2) If no match is found, null is returned.
 * 3) Passing a null reference for the template will match any entry.
 *
 * It tests multiple takeIfExists operations for different templates.
 *
 * 
 */
public class MultipleTakeIfExistsTest extends AbstractTakeTestBase {

    /**
     * This method asserts that for takeIfExists with timeouts
     * other then NO_WAIT:
     * 1) If a takeIfExists returns a non-null value, the entry has been
     *    removed from the space.
     * 2) If no match is found, null is returned.
     * 3) Passing a null reference for the template will match any entry.
     *
     * It tests multiple takeIfExists operations for different templates.
     *
     * <P>Notes:<BR>For more information see the JavaSpaces specification
     * section 2.5.</P>
     */
    public void run() throws Exception {
        SimpleEntry sampleEntry1 = new SimpleEntry("TestEntry #1", 1);
        SimpleEntry sampleEntry2 = new SimpleEntry("TestEntry #2", 2);
        SimpleEntry sampleEntry3 = new SimpleEntry("TestEntry #1", 2);
        SimpleEntry template;
        String msg;

        // first check that space is empty
        if (!checkSpace(space)) {
            throw new TestException("Space is not empty in the beginning.");
        }

        // write two sample entries twice to the space
        space.write(sampleEntry1, null, leaseForeverTime);
        space.write(sampleEntry1, null, leaseForeverTime);
        space.write(sampleEntry2, null, leaseForeverTime);
        space.write(sampleEntry2, null, leaseForeverTime);

        // takeIfExists 1-st entry twice from the space using the same one
        for (int i = 1; i <= 2; i++) {
            msg = testTemplate(sampleEntry1, null, Long.MAX_VALUE, i, 2,
                    true);

            if (msg != null) {
                throw new TestException(msg);
            }
        }

        // write taken entries to the space again
        space.write(sampleEntry1, null, leaseForeverTime);
        space.write(sampleEntry1, null, leaseForeverTime);

        // write 3-rd sample entry twice to the space
        space.write(sampleEntry3, null, leaseForeverTime);
        space.write(sampleEntry3, null, leaseForeverTime);

        /*
         * TakeIfExists entry from the space using null as a template
         * 6 times to take all written entries from the space.
         */
        for (int i = 1; i <= 6; i++) {
            msg = testTemplate(null, null, checkTime, i, 6, true);

            if (msg != null) {
                throw new TestException(msg);
            }
        }

        // write all taken entries to the space again
        space.write(sampleEntry1, null, leaseForeverTime);
        space.write(sampleEntry1, null, leaseForeverTime);
        space.write(sampleEntry2, null, leaseForeverTime);
        space.write(sampleEntry2, null, leaseForeverTime);
        space.write(sampleEntry3, null, leaseForeverTime);
        space.write(sampleEntry3, null, leaseForeverTime);

        /*
         * TakeIfExists entry from the space using template with
         * 1-st null field 4 times to take all matching written
         * entries from the space.
         */
        template = new SimpleEntry(null, 2);

        for (int i = 1; i <= 4; i++) {
            msg = testTemplate(template, null, timeout1, i, 4, true);

            if (msg != null) {
                throw new TestException(msg);
            }
        }

        // write taken entries back to the space
        space.write(sampleEntry2, null, leaseForeverTime);
        space.write(sampleEntry2, null, leaseForeverTime);
        space.write(sampleEntry3, null, leaseForeverTime);
        space.write(sampleEntry3, null, leaseForeverTime);

        /*
         * TakeIfExists entry from the space using template with
         * 2-nd null field 4 times to take all matching written
         * entries from the space.
         */
        template = new SimpleEntry("TestEntry #1", null);

        for (int i = 1; i <= 4; i++) {
            msg = testTemplate(template, null, timeout2, i, 4, true);

            if (msg != null) {
                throw new TestException(msg);
            }
        }

        // write taken entries back to the space
        space.write(sampleEntry1, null, leaseForeverTime);
        space.write(sampleEntry1, null, leaseForeverTime);
        space.write(sampleEntry3, null, leaseForeverTime);
        space.write(sampleEntry3, null, leaseForeverTime);

        /*
         * TakeIfExists entry from the space using template with
         * both null fields 6 times to take all matching written
         * entries from the space.
         */
        template = new SimpleEntry(null, null);

        for (int i = 1; i <= 6; i++) {
            msg = testTemplate(template, null, checkTime, i, 6, true);

            if (msg != null) {
                throw new TestException(msg);
            }
        }
    }
}
