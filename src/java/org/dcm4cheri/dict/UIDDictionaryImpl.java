/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 * Java(TM), hosted at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * TIANI Medgraph AG.
 * Portions created by the Initial Developer are Copyright (C) 2002-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Gunter Zeilinger <gunter.zeilinger@tiani.com>
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */

package org.dcm4cheri.dict;

import org.dcm4che.dict.UIDDictionary;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.util.HashMap;
import java.util.Iterator;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.0
 */
public class UIDDictionaryImpl implements UIDDictionary, java.io.Serializable {

    static final long serialVersionUID = -4793624142653062179L;

    private transient HashMap map = new HashMap(257);

    /** Creates a new instance of TagDictionaryImpl */
    public UIDDictionaryImpl() {
    }

    public Entry lookup(String uid) {
        Entry entry = (Entry)map.get(uid);
	return entry != null
	    ? entry
	    : new Entry(uid, "?");
    }
    
    public String toString(String uid) {
       return lookup(uid).toString();
    }
    
    public final void add(Entry entry) {
        map.put(entry.uid, entry);
    }
    
    public int size() {
        return map.size();
    }
    
    public void load(InputSource xmlSource) throws IOException, SAXException {
        new UIDDictionaryLoader(this).parse(xmlSource);
    }

    public void load(File xmlFile) throws IOException, SAXException {
        new UIDDictionaryLoader(this).parse(xmlFile);
    }
    
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(map.size());
        for (Iterator iter = map.values().iterator(); iter.hasNext();) {
            Entry e = (Entry)iter.next();
            out.writeUTF(e.uid);
            out.writeUTF(e.name);
       }
    }

    private void readObject(ObjectInputStream in) throws IOException {
        int n = in.readInt();
        map = new HashMap(n * 4 / 3 + 1);
        for (int i = 0; i < n; ++i) {
            add(new Entry(in.readUTF(), in.readUTF()));
        }
    }
}
