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

import org.apache.log4j.Logger;
import org.dcm4che.dict.TagDictionary;
import org.dcm4che.dict.UIDDictionary;

import java.io.*;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.0
 */
public class DictionaryFactoryImpl
        extends org.dcm4che.dict.DictionaryFactory {
    private static Logger log = Logger.getLogger(DictionaryFactoryImpl.class);

    private static final String DEF_TAG_DICT =
            "org/dcm4cheri/dict/TagDictionary.ser";    
    private static final String DEF_UID_DICT =
            "org/dcm4cheri/dict/UIDDictionary.ser";    
    private static TagDictionary defTagDict;
    private static UIDDictionary defUIDDict;
    
    /** Creates a new instance of DictionaryFactoryImpl */
    public DictionaryFactoryImpl() {
    }
    
    public TagDictionary newTagDictionary() {
        return new TagDictionaryImpl();
    }

    public TagDictionary getDefaultTagDictionary() {
        if (defTagDict != null)
            return defTagDict;
        synchronized (this) {
            if (defTagDict != null)
                return defTagDict;
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            InputStream in = loader.getResourceAsStream(DEF_TAG_DICT);
            if (in == null)
                throw new RuntimeException("Missing " + DEF_TAG_DICT);
            ObjectInputStream ois = null;
            try {
                ois = new ObjectInputStream(new BufferedInputStream(in));
                return (defTagDict = (TagDictionary)ois.readObject());
            } catch (Exception ex) {                
                throw new RuntimeException("Load DefaultTagDictionary from "
                        + DEF_TAG_DICT + " failed!", ex);
            } finally {
                try {
                    (ois != null ? ois : in).close();
                } catch (IOException ignore) {}
            }
        }           
    }
    
    static void initDefTagDict(File xmlFile, File serFile)
            throws Exception {
        TagDictionaryImpl dict = new TagDictionaryImpl();
        dict.load(xmlFile);
        serFile.getParentFile().mkdirs();
        ObjectOutputStream oos = new ObjectOutputStream(
            new BufferedOutputStream(new FileOutputStream(serFile)));
        try {
            oos.writeObject(dict);
            log.info("Create: " + serFile);
        } finally {
            oos.close();
        }
    }
    
    public UIDDictionary newUIDDictionary() {
        return new UIDDictionaryImpl();
    }

    public UIDDictionary getDefaultUIDDictionary() {
        if (defUIDDict != null)
            return defUIDDict;
        synchronized (this) {
            if (defUIDDict != null)
                return defUIDDict;
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            InputStream in = loader.getResourceAsStream(DEF_UID_DICT);
            if (in == null)
                throw new RuntimeException("Missing " + DEF_UID_DICT);
            ObjectInputStream ois = null;
            try {
                ois = new ObjectInputStream(new BufferedInputStream(in));
                return (defUIDDict = (UIDDictionary)ois.readObject());
            } catch (Exception ex) {                
                throw new RuntimeException("Load DefaultUIDDictionary from "
                        + DEF_UID_DICT + " failed!", ex);
            } finally {
                try {
                    (ois != null ? ois : in).close();
                } catch (IOException ignore) {}
            }
        }           
    }

    static void initDefUIDDict(File xmlFile, File serFile)
            throws Exception {
        UIDDictionaryImpl dict = new UIDDictionaryImpl();
        dict.load(xmlFile);
        serFile.getParentFile().mkdirs();
        ObjectOutputStream oos = new ObjectOutputStream(
            new BufferedOutputStream(new FileOutputStream(serFile)));
        try {
            oos.writeObject(dict);
            log.info("Create: " + serFile);
        } finally {
            oos.close();
        }
    }
    
    public static void main(String args[]) throws Exception {
        if (args.length != 2) {
            System.out.println(
"Usage: java -cp <classpath> org/dcm4cheri/dict/DictionaryFactoryImpl \\\n" +
"  <dictionary.xml> <resdir>");
            System.exit(1);
        }
        File resdir = new File(args[1]);
        initDefTagDict(new File(args[0]), new File(resdir, DEF_TAG_DICT));
        initDefUIDDict(new File(args[0]), new File(resdir, DEF_UID_DICT));
    }
}
