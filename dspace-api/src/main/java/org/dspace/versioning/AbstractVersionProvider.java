/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.versioning;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

import org.dspace.authorize.AuthorizeException;
import org.dspace.authorize.ResourcePolicy;
import org.dspace.authorize.service.AuthorizeService;
import org.dspace.content.Bitstream;
import org.dspace.content.Bundle;
import org.dspace.content.Item;
import org.dspace.content.MetadataField;
import org.dspace.content.MetadataSchema;
import org.dspace.content.MetadataValue;
import org.dspace.content.RelationshipMetadataValue;
import org.dspace.content.service.BitstreamService;
import org.dspace.content.service.BundleService;
import org.dspace.content.service.ItemService;
import org.dspace.core.Context;
import org.dspace.storage.bitstore.service.BitstreamStorageService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Fabio Bolognesi (fabio at atmire dot com)
 * @author Mark Diggory (markd at atmire dot com)
 * @author Ben Bosman (ben at atmire dot com)
 */
public abstract class AbstractVersionProvider {

    private Set<String> ignoredMetadataFields;

    @Autowired(required = true)
    protected AuthorizeService authorizeService;
    @Autowired(required = true)
    protected BitstreamService bitstreamService;
    @Autowired(required = true)
    protected BitstreamStorageService bitstreamStorageService;
    @Autowired(required = true)
    protected BundleService bundleService;
    @Autowired(required = true)
    protected ItemService itemService;

    protected void copyMetadata(Context context, Item itemNew, Item nativeItem) throws SQLException {
        List<MetadataValue> md = itemService.getMetadata(nativeItem, Item.ANY, Item.ANY, Item.ANY, Item.ANY);
        for (MetadataValue aMd : md) {
            MetadataField metadataField = aMd.getMetadataField();
            MetadataSchema metadataSchema = metadataField.getMetadataSchema();
            String unqualifiedMetadataField = metadataSchema.getName() + "." + metadataField.getElement();
            if (getIgnoredMetadataFields().contains(metadataField.toString('.')) ||
                getIgnoredMetadataFields().contains(unqualifiedMetadataField + "." + Item.ANY) ||
                aMd instanceof RelationshipMetadataValue) {
                //Skip this metadata field (ignored and/or virtual)
                continue;
            }

            itemService.addMetadata(
                context,
                itemNew,
                metadataField.getMetadataSchema().getName(),
                metadataField.getElement(),
                metadataField.getQualifier(),
                aMd.getLanguage(),
                aMd.getValue(),
                aMd.getAuthority(),
                aMd.getConfidence(),
                aMd.getPlace()
            );
        }
    }

    protected void createBundlesAndAddBitstreams(Context c, Item itemNew, Item nativeItem)
        throws SQLException, AuthorizeException, IOException {
        for (Bundle nativeBundle : nativeItem.getBundles()) {
            Bundle bundleNew = bundleService.create(c, itemNew, nativeBundle.getName());
            // DSpace knows several types of resource policies (see the class
            // org.dspace.authorize.ResourcePolicy): Submission, Workflow, Custom
            // and inherited. Submission, Workflow and Inherited policies will be
            // set automatically as necessary. We need to copy the custom policies
            // only to preserve customly set policies and embargoes (which are
            // realized by custom policies with a start date).
            List<ResourcePolicy> bundlePolicies =
                authorizeService.findPoliciesByDSOAndType(c, nativeBundle, ResourcePolicy.TYPE_CUSTOM);
            authorizeService.addPolicies(c, bundlePolicies, bundleNew);

            for (Bitstream nativeBitstream : nativeBundle.getBitstreams()) {
                // Metadata and additional information like internal identifier,
                // file size, checksum, and checksum algorithm are set by the bitstreamStorageService.clone(...)
                // and respectively bitstreamService.clone(...) method.
                Bitstream bitstreamNew =  bitstreamStorageService.clone(c, nativeBitstream);

                bundleService.addBitstream(c, bundleNew, bitstreamNew);

                // NOTE: bundle.addBitstream() causes Bundle policies to be inherited by default.
                // So, we need to REMOVE any inherited TYPE_CUSTOM policies before copying over the correct ones.
                authorizeService.removeAllPoliciesByDSOAndType(c, bitstreamNew, ResourcePolicy.TYPE_CUSTOM);

                // Now, we need to copy the TYPE_CUSTOM resource policies from old bitstream
                // to the new bitstream, like we did above for bundles
                List<ResourcePolicy> bitstreamPolicies =
                    authorizeService.findPoliciesByDSOAndType(c, nativeBitstream, ResourcePolicy.TYPE_CUSTOM);
                authorizeService.addPolicies(c, bitstreamPolicies, bitstreamNew);

                if (nativeBundle.getPrimaryBitstream() != null && nativeBundle.getPrimaryBitstream()
                                                                              .equals(nativeBitstream)) {
                    bundleNew.setPrimaryBitstreamID(bitstreamNew);
                }

                bitstreamService.update(c, bitstreamNew);
            }
        }
    }


    public void setIgnoredMetadataFields(Set<String> ignoredMetadataFields) {
        this.ignoredMetadataFields = ignoredMetadataFields;
    }

    public Set getIgnoredMetadataFields() {
        return ignoredMetadataFields;
    }
}
