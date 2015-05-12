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
package net.jini.lookup.ui.attribute;

import java.util.Locale;
import java.util.Iterator;
import java.util.Set;
import java.util.List;
import java.util.Collections;
import org.apache.river.lookup.util.ConsistentSet;

/**
 * UI attribute that lists the locales supported
 * by a generated UI.
 *
 * @author Bill Venners
 */
public class Locales implements java.io.Serializable {

    private static final long serialVersionUID = -8904904794408873606L;

    /**
     * @serial A <code>Set</code> of <code>java.util.Locale</code> objects,
     *     each of which represents one locale supported by the UI generated
     *     by the UI factory stored in marshalled form in the same
     *     <code>UIDescriptor</code>.
     */
    private Set supportedLocales;

    /**
     * Constructs a <CODE>Locales</CODE> using the
     * passed <CODE>Set</CODE>. The <CODE>Set</CODE> can
     * be mutable or immutable, and must contain only
     * <CODE>java.util.Locale</CODE> objects. Each <CODE>Locale</CODE> must
     * represent a locale that is supported by the UI generated
     * by the UI factory stored in marshalled form in the
     * same <CODE>UIDescriptor</CODE>. This constructor copies
     * the contents of the passed <code>Set</code> into a
     * serializable read-only <code>Set</code> that has a
     * consistent serialized form across all VMs.
     *
     * @param locales A <CODE>Set</CODE> of <CODE>Locale</CODE>
     * objects. Each element must be non-null and an instance
     * of <CODE>java.util.Locale</CODE>.
     *
     * @throws NullPointerException if <CODE>locales</CODE>
     * is <CODE>null</CODE> or any element of <CODE>locales</CODE>
     * set is <CODE>null</CODE>.
     *
     * @throws IllegalArgumentException if any non-null element of
     * <CODE>locales</CODE> set is not an instance of
     * <CODE>java.util.Locale</CODE>.
     */
    public Locales(Set locales) {

        if (locales == null) {
            throw new NullPointerException();
        }

        Iterator it = locales.iterator();
        while (it.hasNext()) {
            Object o = it.next();
            if (o == null) {
                throw new NullPointerException();
            }
            if (!(o instanceof Locale)) {
                throw new IllegalArgumentException();
            }
        }

        supportedLocales = new ConsistentSet(locales);
    }

    /**
     * Indicates whether or not a locale is supported
     * by the UI generated by the UI factory stored in
     * the marshalled object of the same <CODE>UIDescriptor</CODE>.
     * This method returns <CODE>true</CODE> only if the passed
     * <CODE>Locale</CODE> exactly matches a <CODE>Locale</CODE>
     * supported by the UI, as defined by the <CODE>equals()</CODE>
     * method of class <CODE>java.util.Locale</CODE>. For example, imagine the UI supports the
     * "en" (General English) locale, but not the "en_US" (US English) locale.
     * Then, if "en_US" is passed to this method, the method will return
     * <CODE>false</CODE>.
     *
     * @param locale the locale to check
     * @throws NullPointerException if <CODE>locale</CODE>
     * is <CODE>null</CODE>.
     */
    public boolean isLocaleSupported(Locale locale) {

        if (locale == null) {
            throw new NullPointerException();
        }

        return supportedLocales.contains(locale);
    }

    /**
     * Looks through the passed array of <CODE>Locale</CODE>s
     * (in the order they appear in the array)
     * and returns the first <CODE>Locale</CODE> that is
     * supported by the UI (as defined by <CODE>isLocaleSupported()</CODE>),
     * or <CODE>null</CODE>, if none of the <CODE>Locale</CODE>s in
     * the passed array are supported by the UI.
     *
     * @param locales an array of locales in order of most desired to
     *     least desired
     * @throws NullPointerException if <CODE>locales</CODE>
     * is <CODE>null</CODE>.
     */
    public Locale getFirstSupportedLocale(Locale[] locales) {

        if (locales == null) {
            throw new NullPointerException();
        }

        for (int i = 0; i < locales.length; ++i) {
            if (supportedLocales.contains(locales[i])) {
                return locales[i];
            }
        }

        return null;
    }

    /**
     * Iterates through the passed <CODE>List</CODE> of <CODE>Locale</CODE>s
     * and returns the first <CODE>Locale</CODE> that is
     * supported by the UI (as defined by <CODE>isLocaleSupported()</CODE>),
     * or <CODE>null</CODE>, if none of the <CODE>Locale</CODE>s in
     * the passed array are supported by the UI.
     *
     * @param locales a <code>List</code> of locales in order of most
     *      desired to least desired
     * @throws NullPointerException if <CODE>locales</CODE>
     * is <CODE>null</CODE>.
     */
    public Locale getFirstSupportedLocale(List locales) {

        if (locales == null) {
            throw new NullPointerException();
        }

        for (int i = 0; i < locales.size(); ++i) {
            if (supportedLocales.contains(locales.get(i))) {
                try {
                    return (Locale) locales.get(i);
                }
                catch (ClassCastException cce) {
                    // This should never happen given
                    // the checks in the constructor.
                    continue;
                }
            }
        }

        return null;
    }

    /**
     * Returns an iterator over the set of <CODE>java.util.Locale</CODE>
     * objects, one for each locale supported
     * by the UI generated by the UI factory stored in
     * the marshalled object of the same <CODE>UIDescriptor</CODE>.
     * The returned <CODE>Iterator</CODE> does not support
     * <CODE>remove()</CODE>.
     *
     * @return an iterator over the set of supported locales
     */
    public Iterator iterator() {

        // Since ConsistentSet is used for supportedLocales,
        // can simply return its iterator, which is known
        // to not support remove.
        return supportedLocales.iterator();
    }

    /**
     * Returns an unmodifiable <CODE>java.util.Set</CODE> that contains
     * <CODE>java.util.Locale</CODE> objects, one for each locale supported
     * by the UI generated by the UI factory stored in
     * the marshalled object of the same <CODE>UIDescriptor</CODE>.
     *
     * @return an unmodifiable set of the supported locales
     */
    public Set getLocales() {

        // Since ConsistentSet is used, can just return it. It is known
        // to be unmodifiable.
        return supportedLocales;
    }

    /**
     * Compares the specified object (the <CODE>Object</CODE> passed
     * in <CODE>o</CODE>) with this <CODE>Locales</CODE>
     * object for equality. Returns true if the specified object
     * is not null, if the specified object's class is
     * <CODE>Locales</CODE>, if the two sets of
     * supported locales are the same size, and if every locale mentioned in the
     * specified <CODE>Locales</CODE> object (passed in <CODE>o</CODE>) is also mentioned
     * in this <CODE>Locales</CODE> object.
     *
     * @param o the object to compare against
     * @return <code>true</code> if the objects are the same,
     *     <code>false</code> otherwise.
     */
    public boolean equals(Object o) {

        if (o == null) {
            return false;
        }

        if (o.getClass() != Locales.class) {
            return false;
        }

        Locales locales = (Locales) o;

        if (!supportedLocales.equals(locales.supportedLocales)) {
            return false;
        }

        return true;
    }

    /**
     * Returns the hash code value for this <CODE>Locales</CODE> object.
     *
     * @return the hashcode for this object
     */
    public int hashCode() {
        return supportedLocales.hashCode();
    }
}


