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

import org.dcm4che.srom.Template;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmValueException;
import org.dcm4che.dict.Tags;

import java.util.Date;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0
 */
class TemplateImpl implements Template {
    // Constants -----------------------------------------------------
    public static final Template TID_2010 =
            new TemplateImpl("2010","DCMR",null,null);
    
    private static final int MEANING_PROMPT_LEN = 20;

    // Attributes ----------------------------------------------------
    private final String templateIdentifier;
    private final String mappingResource;
    private final Long templateVersion;
    private final Long templateLocalVersion;

    // Constructors --------------------------------------------------
    public TemplateImpl(String templateIdentifier, String mappingResource,
            Date templateVersion, Date templateLocalVersion)
    {
        if ((this.templateIdentifier = templateIdentifier).length() == 0)
            throw new IllegalArgumentException();
        if ((this.mappingResource = mappingResource).length() == 0)
            throw new IllegalArgumentException();
        this.templateVersion = templateVersion != null
                ? new Long(templateVersion.getTime()) : null;
        this.templateLocalVersion = templateLocalVersion != null
                ? new Long(templateLocalVersion.getTime()) : null;
    }

    public TemplateImpl(Dataset ds) throws DcmValueException {
        this(ds.getString(Tags.TemplateIdentifier),
            ds.getString(Tags.MappingResource),
            ds.getDate(Tags.TemplateVersion),
            ds.getDate(Tags.TemplateLocalVersion));
    }
    
    public static Template newTemplate(Dataset ds) throws DcmValueException {
        return ds != null ? new TemplateImpl(ds) : null;
    }
        
    // Methodes ------------------------------------------------------
    public String getTemplateIdentifier() { return templateIdentifier; }
    public String getMappingResource() { return mappingResource; }
    public Date getTemplateVersion() {
        return templateVersion != null
                ? new Date(templateVersion.longValue()) : null;
    }
    public Date getTemplateLocalVersion() {
        return templateLocalVersion != null
                ? new Date(templateLocalVersion.longValue()) : null;
    }
    
    //compares code value,coding scheme designator only
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (!(obj instanceof TemplateImpl))
            return false;
        TemplateImpl o = (TemplateImpl)obj;
        if (!templateIdentifier.equals(o.templateIdentifier))
            return false;
        if (!mappingResource.equals(o.mappingResource))
            return false;
        if (templateVersion == null
                ? o.templateVersion != null
                : !templateVersion.equals(o.templateVersion))
            return false;
        if (templateLocalVersion == null
                ? o.templateLocalVersion != null
                : !templateLocalVersion.equals(o.templateVersion))
            return false;
        return true;
    }        

    public int hashCode() { return templateIdentifier.hashCode(); }
    
    public String toString() {
        return "TID" + templateIdentifier + "@" + mappingResource;
    }

    public void toDataset(Dataset ds) {
        ds.putCS(Tags.TemplateIdentifier, templateIdentifier);
        ds.putCS(Tags.MappingResource, mappingResource);
        if (templateVersion != null) {
            ds.putDT(Tags.TemplateVersion,
                    new Date(templateVersion.longValue()));
        }
        if (templateLocalVersion != null) {
            ds.putDT(Tags.TemplateLocalVersion,
                    new Date(templateLocalVersion.longValue()));
            ds.putCS(Tags.TemplateExtensionFlag, "Y");
        }
    }
}
