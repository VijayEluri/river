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
package net.jini.lookup.entry;

import net.jini.entry.AbstractEntry;
import java.util.Set;
import java.rmi.MarshalledObject;
import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import org.apache.river.lookup.util.ConsistentSet;
import net.jini.io.MarshalledInstance;

/**
 * <CODE>Entry</CODE> that enables a UI for a service to be associated
 * with the service in the attribute sets of the service item.
 * <CODE>UIDescriptor</CODE> holds a marshalled UI factory object, as
 * well as a role string, a sub-role string, and set of attributes that describe the
 * UI generated by the factory.
 *
 * @author Bill Venners
 */
public class UIDescriptor extends AbstractEntry {

    private static final long serialVersionUID = -4862086482826820670L;

    /**
     * Gives the fully qualified name of the interface that represents
     * the role of the UI generated by the marshalled UI factory.
     * If the client program unmarshals the UI factory and invokes a factory method, the
     * UI returned by the factory method must implement the role the interface specified by
     * <CODE>role</CODE>.
     *
     * <P>
     * For a client program to be able to use a UI, the client has to have prior knowledge
     * of the UI semantics, which is defined by the UI's role type. Thus, for a client
     * to be able to use a UI, the client must understand the semantics
     * of the type whose fully qualified name is given in the <CODE>String</CODE>
     * referenced from the <CODE>role</CODE> field of that UI's <CODE>UIDescriptor</CODE>.
     *
     * <P>
     * For example, two role types that are defined in the <CODE>net.jini.lookup.ui</CODE>
     * package by the Jini Service UI Specification are <CODE>MainUI</CODE>, for a main UI
     * to a Jini service, and <CODE>AdminUI</CODE>, for an administration UI. Other role types
     * may be defined by the Jini Service UI Specification and by individual Jini service API
     * specifications.
     *
     * <P>
     * As the strings referenced from the <CODE>role</CODE> field are Java type names, they
     * are intended to be manipulated by client programs only. They should not be shown to a user.
     */
    public String role;

    /**
     * A <code>String</code> to facilitate searching whose value represents the main UI toolkit (for example
     * Swing or AWT) which the produced UI makes use of. The value to which this field should
     * be set is defined by the semantics of the factory type. (This field is intended
     * to facilitate searches. For example, a client can search for all blender services that have Swing
     * MainUI's.)
     */
    public String toolkit;

    /**
     * A set of objects that describe the UI generated by the marshalled UI factory.
     */
    public Set attributes;

    /**
     * The <CODE>get()</CODE> method of this <CODE>MarshalledObject</CODE>
     * must return an object that implements one or more UI factory interfaces. The actual
     * UI factory type or types implemented by the returned object
     * must be described by a <CODE>UIFactoryTypes</CODE> attribute placed in
     * the attributes set of this <CODE>UIDescriptor</CODE>.
     */
    public MarshalledObject factory;

    /**
     * Constructs a <CODE>UIDescriptor</CODE> with all fields set to <CODE>null</CODE>.
     */
    public UIDescriptor() {
    }

    /**
     * Constructs a <CODE>UIDescriptor</CODE> with the fields set to passed values.
     * This constructor copies the contents of the passed attributes <code>Set</code> into a
     * serializable read-only <code>Set</code> that has a
     * consistent serialized form across all VMs, and initializes the <code>attributes</code>
     * field with the consistent <code>Set</code>.
     *
     * @param role the role
     * @param toolkit the toolkit
     * @param attributes the attributes
     * @param factory the factory
     */
    public UIDescriptor(String role, String toolkit, Set attributes,
        MarshalledObject factory) {

        this.role = role;
        this.toolkit = toolkit;
        if (attributes != null) {
            this.attributes = new ConsistentSet(attributes);
        }
        else {
            this.attributes = null;
        }
        this.factory = factory;
    }

    /**
     * A convenience method for unmarshalling the UI factory stored
     * in the <CODE>MarshalledObject</CODE> referenced from the
     * <CODE>factory</CODE> field. This method saves a reference
     * to the current context class loader, sets the context class loader
     * to the class loader passed as <CODE>parentLoader</CODE>, invokes
     * <CODE>get()</CODE> on the marshalled object, then resets the
     * context class loader to the saved reference before returning
     * the object produced by <CODE>get()</CODE>.
     *
     * <P>The class loader
     * passed in <CODE>parentLoader</CODE> should be able to load classes
     * needed when the UI interacts with the <CODE>roleObject</CODE> passed as the first
     * parameter to the factory method. For example, if the <CODE>roleObject</CODE> is
     * the service item (as it is for the <CODE>MainUI</CODE> and <CODE>AdminUI</CODE>
     * roles), the class loader passed in <CODE>parentLoader</CODE> could be
     * the class loader with which the service proxy object referenced
     * from the service item's <CODE>service</CODE> field was loaded.
     * For example:
     *
     * <PRE>
     * Object uiFactory = uiDescriptor.getUIFactory(
     *     serviceItem.service.getClass().getClassLoader());
     * </PRE>
     *
     * @throws NullPointerException if <CODE>parentLoader</CODE> is null.
     * @return the unmarshalled UI factory
     */
    public final Object getUIFactory(final ClassLoader parentLoader)
        throws IOException, ClassNotFoundException {

        if (parentLoader == null) {
            throw new NullPointerException();
        }

        Object uiFactory;
        final Thread currentThread = Thread.currentThread();

        // First, get a reference to the current context class loader,
        // so it can be restored after the unmarshalling
        ClassLoader original = currentThread.getContextClassLoader();
        try {
            currentThread.setContextClassLoader(parentLoader);
            uiFactory = new MarshalledInstance(factory).get(false);
        }
        finally {
            currentThread.setContextClassLoader(original);
        }
        return uiFactory;
    }
}
