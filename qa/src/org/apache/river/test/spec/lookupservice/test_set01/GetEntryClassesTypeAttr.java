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
package org.apache.river.test.spec.lookupservice.test_set01;
import org.apache.river.qa.harness.QAConfig;
import org.apache.river.qa.harness.Test;

import java.util.logging.Level;
import org.apache.river.qa.harness.TestException;

import org.apache.river.test.spec.lookupservice.QATestRegistrar;
import org.apache.river.test.spec.lookupservice.QATestUtils;
import net.jini.core.lookup.ServiceRegistrar;
import net.jini.core.lookup.ServiceItem;
import net.jini.core.lookup.ServiceRegistration;
import net.jini.core.lookup.ServiceTemplate;
import net.jini.core.entry.Entry;
import net.jini.core.lease.UnknownLeaseException;
import java.rmi.RemoteException;

/** This class is used to verify that the method getEntryClasses() operates
 *  as expected when a template containing only an attribute class having
 *  only null fields is input.
 *
 *  In particular, this test wishes to verify the following statement from
 *  the lookup specification:
 *
 *    "The getEntryClasses method looks at all service items that match the
 *     specified template, finds every entry (among those service items)
 *     that either doesn't match any entry templates or is a subclass of
 *     at least one matching entry template, and returns the set of the 
 *     (most specific) classes of those entries ..."
 *  
 *  The difference between this test class and the other classes that
 *  test getEntryClasses() is in the template that is used for matching
 *  the registered services. This class will input a template containing
 *  only attibutes -- and only attributes containing all null fields;
 *  whereas the other test classes will employ templates that match
 *  on other criteria.
 * 
 *  To perform this test, N instances of each of the test service classes
 *  are created and registered with the lookup service. Each of the test
 *  attribute classes are instantiated with the attribute's no-arg
 *  constructor; resulting in each attribute class having only null fields.
 *  After instantiation, each attribute class is added to each of the
 *  services. Thus, each of the N instances of the M services has assigned
 *  to it L different attributes containing only null fields.
 *
 *  An array of templates is created in which each element of the template
 *  contains all but one of the attribute class instances. That is, 
 *  template element T[i] will contain all attributes A[j] where i!=j. For 
 *  example, T[3] contains {A[0],A[1],A[2],A[4],A[5],...}. The template is 
 *  constructed in this way to test the specification item "The getEntryClasses
 *  method looks at all service items that match the specified template, (and)
 *  finds every entry ... that ... doesn't match any entry templates".
 *
 *  For each of the elements of the template array, getEntryClasses() is
 *  called with that template element as input argument. The array of 
 *  class types that is returned is then compared against the expected 
 *  set of class type descriptors. 
 *
 *  @see org.apache.river.qa.harness.TestEnvironment
 *  @see org.apache.river.test.spec.lookupservice.QATestRegistrar
 *  @see org.apache.river.test.spec.lookupservice.QATestUtils
 */
public class GetEntryClassesTypeAttr extends QATestRegistrar {

    private ServiceItem[] srvcItems ;
    private ServiceRegistration[] srvcRegs ;
    private ServiceTemplate[][] tmpls;
    private ServiceRegistrar proxy;

    private int nClasses = getNTestClasses();
    private int nAttrClasses = getNAttrClasses();

    private String[][] expectedTypeDescs
     = {
         {ATTR_CLASSES[0], ATTR_CLASSES[3], ATTR_CLASSES[5],
          ATTR_CLASSES[10],ATTR_CLASSES[11],ATTR_CLASSES[12]},

         {ATTR_CLASSES[1], ATTR_CLASSES[3], ATTR_CLASSES[5],
          ATTR_CLASSES[10],ATTR_CLASSES[11],ATTR_CLASSES[12]},

         {ATTR_CLASSES[1], ATTR_CLASSES[2], ATTR_CLASSES[5],
          ATTR_CLASSES[10],ATTR_CLASSES[11],ATTR_CLASSES[12]},

         {ATTR_CLASSES[1], ATTR_CLASSES[3], ATTR_CLASSES[5],
          ATTR_CLASSES[10],ATTR_CLASSES[11],ATTR_CLASSES[12]},

         {ATTR_CLASSES[1], ATTR_CLASSES[3], ATTR_CLASSES[4],
          ATTR_CLASSES[10],ATTR_CLASSES[11],ATTR_CLASSES[12]},

         {ATTR_CLASSES[1], ATTR_CLASSES[3], ATTR_CLASSES[5],
          ATTR_CLASSES[10],ATTR_CLASSES[11],ATTR_CLASSES[12]},

         {ATTR_CLASSES[1], ATTR_CLASSES[3], ATTR_CLASSES[5],
          ATTR_CLASSES[6], ATTR_CLASSES[10],ATTR_CLASSES[11],ATTR_CLASSES[12]},

         {ATTR_CLASSES[1], ATTR_CLASSES[3], ATTR_CLASSES[5],
          ATTR_CLASSES[7], ATTR_CLASSES[10],ATTR_CLASSES[11],ATTR_CLASSES[12]},

         {ATTR_CLASSES[1], ATTR_CLASSES[3], ATTR_CLASSES[5],
          ATTR_CLASSES[8], ATTR_CLASSES[10],ATTR_CLASSES[11],ATTR_CLASSES[12]},

         {ATTR_CLASSES[1], ATTR_CLASSES[3], ATTR_CLASSES[5],
          ATTR_CLASSES[9], ATTR_CLASSES[11],ATTR_CLASSES[12]},

         {ATTR_CLASSES[1], ATTR_CLASSES[3], ATTR_CLASSES[5],
          ATTR_CLASSES[10],ATTR_CLASSES[11],ATTR_CLASSES[12]},

         {ATTR_CLASSES[1], ATTR_CLASSES[3],ATTR_CLASSES[5],
          ATTR_CLASSES[10],ATTR_CLASSES[11],ATTR_CLASSES[12]},

         {ATTR_CLASSES[1], ATTR_CLASSES[3], ATTR_CLASSES[5],
          ATTR_CLASSES[10],ATTR_CLASSES[11],ATTR_CLASSES[12]},
       };

    /** Performs actions necessary to prepare for execution of the 
     *  current QA test.
     *
     *  Creates the lookup service. Loads all of the attribute classes and
     *  creates two different sets of instances of those classes (one set of 
     *  instances to be added to each service; one set of instances to be
     *  used in the template). Creates and registers all the service items.
     *  Creates the service template to be used by getEntryClasses(); this
     *  template should contain only the attributes from the second set
     *  of attributes. Adds each attribute instance from the first set of
     *  attributes to each registered service.
     *  @exception QATestException will usually indicate an "unresolved"
     *  condition because at this point the test has not yet begun.
     */
    public Test construct(QAConfig sysConfig) throws Exception {
        int i,j,k;

	super.construct(sysConfig);

        /* create the array of Entry arrays holding the attributes */
        Entry[][] srvcAttrs    = new Entry[nAttrClasses][1];
        Entry[][] tmplAttrs    = new Entry[nAttrClasses][nAttrClasses-1];
        Entry[]   attrInstance = new Entry[nAttrClasses];

        for (i=0;i<nAttrClasses;i++) {
	    /* load each attribute class */
	    Class loadedAttrObj = Class.forName(ATTR_CLASSES[i]);
	    /* create a "no-arg" instance of the class just loaded */
	    srvcAttrs[i][0] = (Entry)loadedAttrObj.newInstance();
	    /* create another "no-arg" instance of the class just loaded */
	    attrInstance[i] = (Entry)loadedAttrObj.newInstance();
        }

        /* create the array-of-arrays of type Entry where each element is
         * an attribute instance; and where each array of attributes is
         * missing one of the attributes from the complete set of attributes
         */
        for (i=0;i<nAttrClasses;i++) {
            for (j=0;j<(nAttrClasses-1);j++) {
		if (j>=i) {;
	            tmplAttrs[i][j] = attrInstance[j+1];
		} else {
	            tmplAttrs[i][j] = attrInstance[j];
		}
	    }
        }

        /* instantiate and register all of the test service classes */
	srvcItems = super.createServiceItems(TEST_SRVC_CLASSES);
	srvcRegs = super.registerAll();
	proxy = super.getProxy();

        /* create the attribute templates to be used with getEntryClasses() */
        tmpls = new ServiceTemplate[nClasses][nAttrClasses];
        for (i=0;i<nClasses;i++) {
	    Class c = Class.forName(TEST_SRVC_CLASSES[i]);
	    Class[] classType = {c};
	    for (j=0;j<nAttrClasses;j++) {
		tmpls[i][j] = new ServiceTemplate
		    (null,classType,tmplAttrs[j]);
	    }
        }

        /* add each attribute to each service item */
        for(i=0; i<srvcRegs.length; i++) {
	    for(j=0; j<nAttrClasses; j++) {
		srvcRegs[i].addAttributes(srvcAttrs[j]);
	    }
        }
        return this;
    }

    /** Executes the current QA test.
     *
     *  Using the ServiceTemplate created during construct, invokes the method
     *  getEntryClasses(). Verifies that the set of class types returned
     *  matches the expected set of type descriptors.
     *  @exception QATestException usually indicates test failure
     */
    public void run() throws Exception {
	Class[] classTypes = null;
	for (int i=0;i<nClasses;i++) {
	    for(int j=0;j<nAttrClasses;j++){
		classTypes = proxy.getEntryClasses(tmpls[i][j]);
		if (!QATestUtils.classTypesEqualTypeDescriptors(classTypes,
						       expectedTypeDescs[j]))
	        {
		    throw new TestException("not ALL service type "
					    + "descriptors were returned");
		}
	    }
	}
    }
}
