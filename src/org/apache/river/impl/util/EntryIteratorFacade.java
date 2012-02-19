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

package org.apache.river.impl.util;

import java.util.Iterator;

/**
 *
 * @author Peter Firmstone.
 */
class EntryIteratorFacade<O, R> implements Iterator<O> {
    private Iterator<R> iterator;
    private ReferenceQueuingFactory<O, R> wf;

    EntryIteratorFacade(Iterator<R> iterator, ReferenceQueuingFactory<O, R> wf) {
        this.iterator = iterator;
        this.wf = wf;
    }

    public boolean hasNext() {
        return iterator.hasNext();
    }

    public O next() {
        return wf.pseudoReferent(iterator.next());
    }

    public void remove() {
        iterator.remove();
    }
    
}
