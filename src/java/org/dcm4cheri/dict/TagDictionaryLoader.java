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

import org.dcm4che.dict.TagDictionary;

import java.io.File;
import java.io.IOException;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.0
 */
final class TagDictionaryLoader extends org.xml.sax.helpers.DefaultHandler {

    private final TagDictionaryImpl dict;
    private final SAXParser parser;
    
    /** Creates a new instance of TagDictionaryLoader */
    public TagDictionaryLoader(TagDictionaryImpl dict) {
        this.dict = dict;
        try {
            parser = SAXParserFactory.newInstance().newSAXParser();
        } catch (Exception ex) {
            throw new ConfigurationError("Could not create SAX Parser", ex);
        }
    }    

    public void startElement (String uri, String localName, String qName,
            Attributes attr) throws SAXException {
        if ("element".equals(qName)) {
            String str = attr.getValue("tag");
            if (str == null)
                throw new SAXException("Missing tag attribute");
            char[] tag = str.toCharArray();
            if (tag.length != 11
                    || tag[0] != '(' || tag[5] != ',' || tag[10] != ')')
                throw new SAXException("Illegal tag value: " + str);
            try {
                dict.add(new TagDictionary.Entry(toTag(tag),
                        str.indexOf('x') == -1 ? -1 : toMask(tag),
                        attr.getValue("vr"), attr.getValue("vm"),
                        attr.getValue("name")));
            } catch (NumberFormatException nfe) {
                throw new SAXException("Illegal tag value: " + str, nfe);
            }
        }
    }
    
    private int toTag(char[] s) {
        StringBuffer sb = new StringBuffer(8);
        sb.append(toTag(s[1])).append(toTag(s[2])).append(toTag(s[3]))
          .append(toTag(s[4])).append(toTag(s[6])).append(toTag(s[7]))
          .append(toTag(s[8])).append(toTag(s[9]));
        return (int)Long.parseLong(sb.toString(),16);
    }
    
    private char toTag(char ch) {
        return ch == 'x' ? '0' : ch;
    }
    
    private int toMask(char[] s) {
        StringBuffer sb = new StringBuffer(8);
        sb.append(toMask(s[1])).append(toMask(s[2])).append(toMask(s[3]))
          .append(toMask(s[4])).append(toMask(s[6])).append(toMask(s[7]))
          .append(toMask(s[8])).append(toMask(s[9]));
        return (int)Long.parseLong(sb.toString(),16);
    }
    
    private char toMask(char ch) {
        return ch == 'x' ? '0' : 'f';
    }

    public void parse(InputSource xmlSource) throws SAXException, IOException {
        parser.parse(xmlSource, this);
    }
    
    public void parse(File xmlFile) throws SAXException, IOException {
        parser.parse(xmlFile, this);
    }
    
    static class ConfigurationError extends Error {
        ConfigurationError(String msg, Exception x) {
            super(msg,x);
        }
    }
}
