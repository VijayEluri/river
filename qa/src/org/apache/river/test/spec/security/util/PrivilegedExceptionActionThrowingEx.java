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
package org.apache.river.test.spec.security.util;

// java
import java.security.AccessController;


/**
 * Class implementing PrivilegedExceptionAction interface whose run method
 * always throws FakeException
 */
public class PrivilegedExceptionActionThrowingEx
        extends TestPrivilegedExceptionAction {

    /** Exception which was thrown by run method. */
    protected Exception ex = null;

    /**
     * Gets current DomainCombiner and stores it.
     *
     * @throws FakeException always
     */
    public Object run() throws Exception {
       comb = AccessController.getContext().getDomainCombiner();
       ex = new FakeException();
       throw ex;
    }

    /**
     * Returns Exception thrown by run method.
     *
     * @return Exception thrown by run method
     */
    public Exception getException() {
        return ex;
    }
}
