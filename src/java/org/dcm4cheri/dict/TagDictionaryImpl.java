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

import org.dcm4che.dict.Tags;
import org.dcm4cheri.util.IntHashtable2;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.util.Iterator;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.0
 */
public class TagDictionaryImpl implements org.dcm4che.dict.TagDictionary,
                                          java.io.Serializable {

    static final long serialVersionUID = 5940638712350400261L;

    private transient IntHashtable2[] tables = {
        new IntHashtable2()
    };

    /** Creates a new instance of TagDictionaryImpl */
    public TagDictionaryImpl() {
    }

    public Entry lookup(int tag) {
        Object ret;
        for (int i = 0; i < tables.length; ++i) {
            if ((ret = tables[i].get(tag)) != null) {
                return (Entry)ret;
            }
        }
        return null;
    }

    public String toString(int tag) {
       Entry e = lookup(tag);
       return e != null
            ? (Tags.toString(tag) + " " + e.name)
            : Tags.toString(tag);
    }    
    
    /** Adds record to dictionary
     * @param entry dictionary record
     */
    public final void add(Entry entry) {
        getTableForMask(entry.mask).put(entry.tag, entry);
    }
    
    public int size() {
        int count = 0;
        for (int i = 0; i < tables.length; ++i) {
            count += tables[i].size();
        }
        return count;
    }

    private IntHashtable2 getTableForMask(int mask) {
        for (int i = 0; i < tables.length; ++i) {
            if (mask == tables[i].mask()) {
                return tables[i];
            }
        }
        IntHashtable2[] tmp = tables;
        tables = new IntHashtable2[tmp.length + 1];
        System.arraycopy(tmp,0,tables,0,tmp.length);
        IntHashtable2 newTable = new IntHashtable2();
        newTable.mask(mask);
        tables[tmp.length] = newTable;
        tmp = null;
        return newTable;
    }
    
    public void load(InputSource xmlSource) throws IOException, SAXException {
        new TagDictionaryLoader(this).parse(xmlSource);
    }

    public void load(File xmlFile) throws IOException, SAXException {
        new TagDictionaryLoader(this).parse(xmlFile);
    }
    
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(tables.length);
        for (int i = 0; i < tables.length; ++i)
            writeTable(out, tables[i]);
    }

    private void writeTable(ObjectOutputStream out, IntHashtable2 table)
            throws IOException {
        out.writeInt(table.mask());
        out.writeInt(table.size());
        for (Iterator it = table.iterator(); it.hasNext();) {
            Entry entry = (Entry)it.next();
            out.writeInt(entry.tag);
            out.writeUTF(entry.vr);
            out.writeUTF(entry.vm);
            out.writeUTF(entry.name);
        }        
    }
    
    private void readObject(ObjectInputStream in) throws IOException {
        tables = new IntHashtable2[in.readInt()];
        for (int i = 0; i < tables.length; ++i)
            tables[i] = readTable(in);
    }

    private IntHashtable2 readTable(ObjectInputStream in) throws IOException {
        int mask = in.readInt();
        int n = in.readInt();
        IntHashtable2 table = new IntHashtable2(n);
        table.mask(mask);
        for (int i = 0; i < n; ++i) {
            Entry entry = new Entry(in.readInt(), mask, in.readUTF(),
                    in.readUTF(), in.readUTF());
            table.put(entry.tag, entry);
        }
        return table;
    }
}
