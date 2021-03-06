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
package org.apache.river.test.spec.io.marshalinputstream;

import java.util.logging.Level;

import org.apache.river.qa.harness.QATestEnvironment;
import org.apache.river.qa.harness.QAConfig;
import org.apache.river.qa.harness.Test;

import org.apache.river.test.spec.io.util.FakeMarshalInputStream;

import net.jini.io.MarshalOutputStream;

import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.logging.Level;

/**
 * <pre>
 * Purpose
 *   This test verifies that NullPointerExceptions are thrown as specified
 *   in the protected MarshalInputStream methods.
 *
 * Test Cases
 *   This test contains these test cases for these methods:
 *     1) resolveClass(null)
 *     2) resolveProxyClass(null)
 *     3) resolveProxyClass(String[] with a null element)
 *
 * Infrastructure
 *   This test requires the following infrastructure:
 *     1) FakeMarshalInputStream
 *          -extends MarshalInputStream
 *          -provides access to protected methods
 *
 * Actions
 *   The test performs the following steps:
 *     1) create FakeMarhsalInputStream
 *     2) call protected MarshalInputStream methods
 *        with null arguments and assert that NullPointerExceptions are thrown
 * </pre>
 */
public class Resolve_NullArgsTest extends QATestEnvironment implements Test {

    // inherit javadoc
    public Test construct(QAConfig sysConfig) throws Exception {
        return this;
    }

    // inherit javadoc
    public void run() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        MarshalOutputStream output = 
            new MarshalOutputStream(baos,new ArrayList());
        FakeMarshalInputStream input = new FakeMarshalInputStream(
            new ByteArrayInputStream(baos.toByteArray()),null,null);

        logger.log(Level.FINE,"=================================");
        logger.log(Level.FINE,"test case 1: resolveClass(null)");
        logger.log(Level.FINE,"");

        try {
            input.resolveClass(null);
        } catch (NullPointerException ignore) { }

        logger.log(Level.FINE,"=================================");
        logger.log(Level.FINE,"test case 2: resolveProxyClass(null)");
        logger.log(Level.FINE,"");

        try {
            input.resolveProxyClass(null);
        } catch (NullPointerException ignore) { }

        logger.log(Level.FINE,"=================================");
        logger.log(Level.FINE,"test case 3: "
            + "resolveProxyClass(String[] with a null element)");
        logger.log(Level.FINE,"");

        try {
            input.resolveProxyClass(new String[] {null,"bar"});
        } catch (NullPointerException ignore) { }
    }

    // inherit javadoc
    public void tearDown() {
    }

}

