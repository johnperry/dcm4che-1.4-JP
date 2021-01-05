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

package org.dcm4cheri.srom;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.dict.Tags;
import org.dcm4che.srom.Code;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0
 */
class CodeImpl implements Code {
    // Constants -----------------------------------------------------
    static final Code[] EMPTY_ARRAY = {};
    private static final Pattern PATTERN = Pattern.compile(
            "\\( *([^,]+), *([^,\\[]+)(\\[ *([^\\]]+)\\])?, *\"([^\"]+)\" *\\)");

    // Attributes ----------------------------------------------------
    static Logger log = Logger.getLogger(CodeImpl.class);
    
    private final String codeValue;
    private final String codingSchemeDesignator;
    private final String codingSchemeVersion;
    private final String codeMeaning;

    // Constructors --------------------------------------------------
    private String check(String val, String prompt, String def) {
        if (val == null || val.length() == 0) {
            log.warn(prompt);
            return def;
        }
        return val;
    }
    
    public CodeImpl(String spec) {
        Matcher m = PATTERN.matcher(spec);
        if (!m.matches()) {
            throw new IllegalArgumentException(spec);
        }
        this.codeValue = m.group(1).trim();
        this.codingSchemeDesignator = m.group(2).trim();
        String version = m.group(4);
        this.codingSchemeVersion = version == null ? null : version.trim();
        this.codeMeaning = m.group(5);
    }

    public CodeImpl(
        String codeValue,
        String codingSchemeDesignator,
        String codingSchemeVersion,
        String codeMeaning)
    {
        this.codeValue = check(codeValue,
            "Missing (0008,0100) Code Value - insert \"__NULL__\"",
            "__NULL__");
        this.codingSchemeDesignator = check(codingSchemeDesignator,
            "Missing (0008,0102) Coding Scheme Designator - insert \"99NULL\"",
            "99NULL");
        this.codingSchemeVersion = codingSchemeVersion;
        this.codeMeaning = check(codeMeaning,
            "Missing (0008,0104) Code Meaning - insert \"__NULL__\"",
            "__NULL__");
    }

    public CodeImpl(Dataset ds) { 
        this(ds.getString(Tags.CodeValue),
            ds.getString(Tags.CodingSchemeDesignator),
            ds.getString(Tags.CodingSchemeVersion),
            ds.getString(Tags.CodeMeaning));
    }
    
    public static Code newCode(Dataset ds) {
        return ds != null ? new CodeImpl(ds) : null;
    }
        
    public static Code[] newCodes(DcmElement sq) {
        if (sq == null || sq.isEmpty())
            return EMPTY_ARRAY;
        Code[] a = new Code[sq.countItems()];
        for (int i = 0; i < a.length; ++i) {
            a[i] = new CodeImpl(sq.getItem(i));
        }
        return a;
    }
    // Methodes ------------------------------------------------------
    public final String getCodeValue() {
        return codeValue;
    }
    public final String getCodingSchemeDesignator() {
        return codingSchemeDesignator;
    }
    public final String getCodingSchemeVersion() {
        return codingSchemeVersion;
    }
    public final String getCodeMeaning() {
        return codeMeaning;
    }
    
    //compares code value,coding scheme designator only
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (!(obj instanceof CodeImpl))
            return false;
        CodeImpl o = (CodeImpl)obj;
        if (!codeValue.equals(o.codeValue))
            return false;
        if (!codingSchemeDesignator.equals(o.codingSchemeDesignator))
            return false;
        return (codingSchemeVersion == null
                ? o.codingSchemeVersion == null
                : codingSchemeVersion.equals(o.codingSchemeVersion));
    }        

    public int hashCode() { return codeValue.hashCode(); }
    
    public String toString() {
        return codingSchemeVersion != null
                ? "(" + codeValue + ", " + codingSchemeDesignator + " ["
                      + codingSchemeVersion + "], \"" + codeMeaning + "\")"
                : "(" + codeValue + ", " + codingSchemeDesignator
                                             + ", \"" + codeMeaning + "\")";
    }

    public void toDataset(Dataset ds) {
        ds.putSH(Tags.CodeValue, codeValue);
        ds.putSH(Tags.CodingSchemeDesignator, codingSchemeDesignator);
        if (codingSchemeVersion != null) {
            ds.putSH(Tags.CodingSchemeVersion, codingSchemeVersion);
        }
        ds.putLO(Tags.CodeMeaning, codeMeaning);
     }    
}
