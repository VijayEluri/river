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

import com.sun.jini.qa.harness.QAConfig;
import com.sun.jini.qa.harness.QAConfiguration;
import com.sun.jini.qa.harness.QATestEnvironment;
import com.sun.jini.qa.harness.Test;
import com.sun.jini.qa.harness.TestException;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.util.LinkedList;
import java.util.logging.Level;
import net.jini.config.Configuration;
import net.jini.config.ConfigurationException;
import net.jini.core.entry.Entry;
import net.jini.core.event.EventRegistration;
import net.jini.core.lease.Lease;
import net.jini.core.transaction.Transaction;
import net.jini.core.transaction.TransactionFactory;
import net.jini.core.transaction.server.TransactionManager;
import net.jini.security.ProxyPreparer;
import net.jini.space.JavaSpace;

/**
 *
 * @author peter
 */
public abstract class JavaSpaceTest extends QATestEnvironment implements Test {
    /** Holds the value which is used for read/take check operations. */
    protected volatile long checkTime = 0;

    public JavaSpaceTest() {
        super();
    }
    /** Value to check operations which require instant result. */
    protected volatile long instantTime = 0;
    /**
     * Holds the value which is used instead of Lease.FOREVER one
     * to avoid infinite lease times.
     */
    protected volatile long leaseForeverTime = 0;
    /** Holds TransactionManager instance for transaction's testing. */
    protected volatile TransactionManager mgr = null;
    /** The name of current package to read current settings. */
    protected final String pkgName = "com.sun.jini.test.spec." + "javaspace.conformance";
    /** Holds JavaSpace instance of tested space. */
    protected volatile JavaSpace space;
    /** The name of service for which these tests are written. */
    protected final String spaceName = "net.jini.space.JavaSpace";
    /** First timeout for testing in ms. */
    protected volatile long timeout1 = 0;
    /**
     * Second timeout for testing in ms.
     * Must be greater then ({@link #timeout1} + 5000)
     */
    protected volatile long timeout2 = 0;
    /** Holds transactions instances. */
    private final LinkedList<Transaction> txns = new LinkedList<Transaction>();
    /** value to wait for notifications time in ms. */
    protected volatile long waitingNotificationsToComeTime = 0;

    /**
     * Checks whether space is empty or not outside all transactions.
     *
     * @param space JavaSpace to be checked.
     *
     * @return True if space is empty, otherwise - false.
     *
     * @exception TestException
     *          If an exception has been thrown during space checking.
     */
    public boolean checkSpace(JavaSpace space) throws TestException {
        return checkSpace(space, null);
    }

    /**
     * Checks whether space is empty or not.
     *
     * @param space JavaSpace to be checked.
     * @param txn Transaction under wich check needed to be performed.
     *
     * @return True if space is empty, otherwise - false.
     *
     * @exception TestException
     *          If an exception has been thrown during space checking.
     */
    public boolean checkSpace(JavaSpace space, Transaction txn) throws TestException {
        try {
            Entry result = space.readIfExists(null, txn, JavaSpace.NO_WAIT);
            if (result != null) {
                return false;
            }
        } catch (Exception ex) {
            throw new TestException("Exception has been caught while space checking: ", ex);
        }
        return true;
    }

    /**
     * Cleans tested space without transaction.
     *
     * @param space Javaspace for cleaning.
     *
     * @exception TestException
     *          If an exception has been thrown during space cleaning.
     */
    public void cleanSpace(JavaSpace space) throws TestException {
        cleanSpace(space, null);
    }

    /**
     * Cleans tested space within specified transaction.
     *
     * @param space Javaspace for cleaning.
     * @param txn Transaction within which we will clean the space.
     *
     * @exception TestException
     *          If an exception has been thrown during space cleaning.
     */
    public void cleanSpace(JavaSpace space, Transaction txn) throws TestException {
        try {
            while (!checkSpace(space, txn)) {
                space.takeIfExists(null, txn, JavaSpace.NO_WAIT);
            }
        } catch (Exception ex) {
            throw new TestException("Exception has been caught while cleaning the space: " + ex.getMessage());
        }
    }

    /**
     * Runs tested JavaSpace.
     *
     * @return JavaSpace instance of started space.
     *
     * @exception TestException
     *          If an exception has been thrown during JavaSpace creation or
     *          created JavaSpace is null.
     */
    public JavaSpace getSpace() throws TestException {
        JavaSpace js = null;
        try {
            if (System.getSecurityManager() == null) {
                System.setSecurityManager(new RMISecurityManager());
            }
            printSpaceInfo();
            js = (JavaSpace) getManager().startService(spaceName); // prepared
        } catch (Exception e) {
            throw new TestException("Exception has been caught while trying to start space:", e);
        }
        if (js == null) {
            throw new TestException("Null space has been obtained.");
        }
        return js;
    }

    /**
     * Creates transaction with default lease time.
     *
     * @return Created transaction.
     */
    public Transaction getTransaction() throws TestException {
        return getTransaction(leaseForeverTime * 2);
    }

    /**
     * Creates transaction with specified lease time.
     *
     * @param lTime Transaction's lease time.
     *
     * @return Created transaction.
     */
    public Transaction getTransaction(long lTime) throws TestException {
        Transaction.Created trc = null;
        try {
            trc = TransactionFactory.create(mgr, lTime);
            if (trc == null) {
                throw new TestException("Null transaction has been obtained.");
            }
            synchronized (txns){
            txns.add(trc.transaction);
            }
            return trc.transaction;
        } catch (Exception e) {
            throw new TestException("Could not create transaction.", e);
        }
    }

    /**
     * Runs Transaction Manager.
     *
     * @return TransactionManager instance of started manager.
     *
     * @exception TestException
     *          If an exception has been thrown during TransactionManager
     *          creation or created TransactionManager is null.
     */
    public TransactionManager getTxnManager() throws TestException {
        TransactionManager mgr = null;
        try {
            String txnMgrName = getConfig().getStringConfigVal(pkgName + ".txnManager", TransactionManager.class.getName());
            printTxnMgrInfo(txnMgrName);
            mgr = (TransactionManager) getManager().startService(txnMgrName); //prepared
        } catch (Exception e) {
            throw new TestException("Exception has been caught while" + "trying to start Transaction Manager.", e);
        }
        if (mgr == null) {
            throw new TestException("Null Transaction Manager" + " has been obtained.");
        }
        return mgr;
    }

    /**
     * Writes debug text to the log.
     *
     * @param text Text to be written to the log.
     */
    public void logDebugText(String text) {
        logger.log(Level.FINE,": {0}", text);
    }

    protected Lease prepareLease(Lease l) throws ConfigurationException, RemoteException {
        if (l != null) {
            Configuration c = getConfig().getConfiguration();
            if (!(c instanceof QAConfiguration)) {
                // none configuration
                return l;
            }
            ProxyPreparer p = (ProxyPreparer) c.getEntry("test", "outriggerLeasePreparer", ProxyPreparer.class);
            if (p != null) {
                l = (Lease) p.prepareProxy(l);
            }
        }
        return l;
    }

    protected EventRegistration prepareRegistration(EventRegistration reg) throws ConfigurationException, RemoteException {
        if (reg != null) {
            Configuration c = getConfig().getConfiguration();
            if (!(c instanceof QAConfiguration)) {
                // none configuration
                return reg;
            }
            ProxyPreparer p = (ProxyPreparer) c.getEntry("test", "outriggerEventRegistrationPreparer", ProxyPreparer.class);
            if (p != null) {
                reg = (EventRegistration) p.prepareProxy(reg);
            }
        }
        return reg;
    }

    /**
     * Prints configuration of tested JavaSpace for the current test.
     */
    protected void printSpaceInfo() {
        logDebugText("----- JavaSpace Info -----");
        String serviceImplClassname = getConfig().getStringConfigVal(spaceName + ".impl", "no implClassname");
        logDebugText("JavaSpace impl class     -- " + serviceImplClassname);
        String serviceCodebase = getConfig().getStringConfigVal(spaceName + ".codebase", "no codebase");
        logDebugText("JavaSpace codebase       -- " + serviceCodebase);
        String serviceClasspath = getConfig().getStringConfigVal(spaceName + ".classpath", "no classpath");
        logDebugText("JavaSpace classpath      -- " + serviceClasspath);
        String servicePolicyFile = getConfig().getStringConfigVal(spaceName + ".policyfile", "no policyFile");
        logDebugText("JavaSpace policy file    -- " + servicePolicyFile);
        logDebugText("--------------------------");
    }

    /**
     * Prints configuration of tested JavaSpace for the current test.
     *
     * @param txnMgrName Package name for transaction manager.
     */
    protected void printTxnMgrInfo(String txnMgrName) {
        logDebugText("----- Transaction Manager Info -----");
        String txnMgrImplClassname = getConfig().getStringConfigVal(txnMgrName + ".impl", "no implClassname");
        logDebugText("TxnManager impl class     -- " + txnMgrImplClassname);
        String txnMgrCodebase = getConfig().getStringConfigVal(txnMgrName + ".codebase", "no codebase");
        logDebugText("TxnManager codebase       -- " + txnMgrCodebase);
        String txnMgrClasspath = getConfig().getStringConfigVal(txnMgrName + ".classpath", "no classpath");
        logDebugText("TxnManager classpath      -- " + txnMgrClasspath);
        String txnMgrPolicyFile = getConfig().getStringConfigVal(txnMgrName + ".policyfile", "no policyFile");
        logDebugText("TxnManager policy file    -- " + txnMgrPolicyFile);
        logDebugText("------------------------------------");
    }

    /**
     * Sets up the testing environment.
     *
     * @param config QAConfig from the runner for construct.
     */
    public Test construct(QAConfig config) throws Exception {
        // mandatory call to parent
        super.construct(config);
        // avoid changing a whole bunch of constructors to include the configuration
        NotifyCounter.setConfiguration(config.getConfiguration());
        ParticipantImpl.setConfiguration(config.getConfiguration());
        // output the name of this test
        logDebugText("Test Name = " + this.getClass().getName());
        // run tested JavaSpace
        space = getSpace();
        // set up lease and check times
        leaseForeverTime = getConfig().getLongConfigVal(pkgName + ".lease.forever", Lease.FOREVER);
        checkTime = getConfig().getLongConfigVal(pkgName + ".checkTime", 10000);
        timeout1 = getConfig().getLongConfigVal(pkgName + ".timeout1", 10000);
        timeout2 = getConfig().getLongConfigVal(pkgName + ".timeout2", 20000);
        waitingNotificationsToComeTime = getConfig().getLongConfigVal(pkgName + ".waitingNotificationsTime", 10000);
        // ensure, that timeout2 > (timeout1 + 5000)
        if (timeout1 > timeout2) {
            long tmp = timeout1;
            timeout1 = timeout2;
            timeout2 = tmp;
        }
        if (timeout2 < (timeout1 + 5000)) {
            timeout2 += 5000;
        }
        instantTime = getConfig().getLongConfigVal(pkgName + ".instantTime", 500);
        return this;
    }

    /**
     * Performs cleaning operations after test's completion.
     */
    public void tearDown() {
        try {
            // abort noncommitted transactions
            txnsAbort();
            // clean the space
            cleanSpace(space);
        } catch (Exception ex) {
            logDebugText("Exception has been caught in tearDown method: " + ex);
            ex.printStackTrace();
        } finally {
            super.tearDown();
        }
    }

    /**
     * Aborts specified transaction.
     *
     * @param txn Transaction to be committed.
     *
     * @exception TestException
     *          If an exception has been thrown during transaction's aborting.
     */
    public void txnAbort(Transaction txn) throws TestException {
        try {
            if (txn != null) {
                synchronized (txns) {
                    txns.remove(txns.indexOf(txn));
                }
                txn.abort();
            }
        } catch (Exception e) {
            throw new TestException("Exception has been caught while transaction aborting:", e);
        }
    }

    /**
     * Commits specified transaction.
     *
     * @param txn Transaction to be committed.
     *
     * @exception TestException
     *          If an exception has been thrown during transaction's committing.
     */
    public void txnCommit(Transaction txn) throws TestException {
        try {
            if (txn != null) {
                synchronized (txns){
                    txn.commit(); // If commit fails CannotCommitException thrown and it isn't removed.
                    txns.remove(txn);
                }
            }
        } catch (Exception e) {
            throw new TestException("Exception has been caught while transaction committing:", e);
        }
    }

    /**
     * Aborts noncommitted transactions.
     */
    public void txnsAbort() {
        try {
            Transaction trans;
            while (true) {
                synchronized (txns) {
                    trans = txns.poll();
                }
                if (trans != null) txnAbort(trans);
                else break;
            }
        } catch (Exception e) {
        }
    }
    
}
