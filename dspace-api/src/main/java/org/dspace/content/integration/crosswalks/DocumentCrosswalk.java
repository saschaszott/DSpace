/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content.integration.crosswalks;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Optional;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.content.crosswalk.CrosswalkException;
import org.dspace.content.crosswalk.CrosswalkObjectNotSupported;
import org.dspace.content.service.ItemService;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.services.ConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Implementation of {@link ItemExportCrosswalk} to produce a document in a
 * specific format (pdf, rtf etc...) from an item.
 *
 * @author Luca Giamminonni (luca.giamminonni at 4science.it)
 *
 */
public class DocumentCrosswalk implements ItemExportCrosswalk {

    @Autowired
    private ConfigurationService configurationService;

    @Autowired
    private ItemService itemService;

    private String fileName;

    private String mimeType;

    private String templateFileName;

    private String entityType;

    private ReferCrosswalk referCrosswalk;

    @Override
    public void disseminate(Context context, DSpaceObject dso, OutputStream out)
        throws CrosswalkException, IOException, SQLException, AuthorizeException {

        if (!canDisseminate(context, dso)) {
            throw new CrosswalkObjectNotSupported("Can only crosswalk an Item with the configured type: " + entityType);
        }

        Item item = (Item) dso;

        ByteArrayInputStream xmlInputStream = getItemAsXml(context, item);

        try {
            transformToDocument(out, xmlInputStream);
        } catch (Exception e) {
            throw new CrosswalkException(e);
        }

    }

    @Override
    public boolean canDisseminate(Context context, DSpaceObject dso) {
        return (dso.getType() == Constants.ITEM) && hasExpectedEntityType((Item) dso);
    }

    @Override
    public String getFileName() {
        return fileName;
    }

    @Override
    public String getMIMEType() {
        return mimeType;
    }

    private ByteArrayInputStream getItemAsXml(Context context, Item item)
        throws CrosswalkException, IOException, SQLException, AuthorizeException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        referCrosswalk.disseminate(context, item, baos);
        return new ByteArrayInputStream(baos.toByteArray());
    }

    private void transformToDocument(OutputStream out, ByteArrayInputStream xmlInputStream) throws Exception {

        // the XML file which provides the input
        StreamSource xmlSource = new StreamSource(xmlInputStream);

        // create an instance of fop factory
        FopFactory fopFactory = FopFactory.newInstance(getFopConfigurationFile());
        // a user agent is needed for transformation
        FOUserAgent foUserAgent = fopFactory.newFOUserAgent();
        // Construct fop with desired output format
        Fop fop = fopFactory.newFop(getMIMEType(), foUserAgent, out);

        // Setup XSLT
        Transformer transformer = setupXSLTransformerFactory();

        // Resulting SAX events (the generated FO) must be piped through to FOP
        Result res = new SAXResult(fop.getDefaultHandler());

        // Start XSLT transformation and FOP processing
        // That's where the XML is first transformed to XSL-FO and then PDF is created
        transformer.transform(xmlSource, res);
    }

    private Transformer setupXSLTransformerFactory() throws Exception {
        Transformer transformer = TransformerFactory.newInstance().newTransformer(getTemplateFile());
        transformer.setParameter("imageDir", getImageDir());
        transformer.setParameter("dspaceDir", getDSpaceDir());
        transformer.setParameter("currentDate", getCurrentDate());
        transformer.setParameter("fontFamily", getFontFamily());
        return transformer;
    }

    private String getCurrentDate() {
        return DateTimeFormatter.ISO_DATE.format(LocalDate.now());
    }

    private String getDSpaceDir() {
        return configurationService.getProperty("dspace.dir");
    }

    private StreamSource getTemplateFile() {
        return new StreamSource(new File(getConfigDir(), templateFileName));
    }

    private File getFopConfigurationFile() {
        return new File(configurationService.getProperty("crosswalk.fop.configuration-path"));
    }

    private String getFontFamily() {
        return String.join(",", configurationService.getArrayProperty("crosswalk.fop.font-family"));
    }

    private String getConfigDir() {
        return getDSpaceDir() + File.separator + "config";
    }

    private String getImageDir() {
        String tempDir = configurationService.getProperty("crosswalk.virtualfield.bitstream.tempdir", " export");
        return new File(System.getProperty("java.io.tmpdir"), tempDir).getAbsolutePath();
    }

    private boolean hasExpectedEntityType(Item item) {
        return Objects.equals(itemService.getEntityType(item), entityType);
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setTemplateFileName(String templateFileName) {
        this.templateFileName = templateFileName;
    }

    public void setReferCrosswalk(ReferCrosswalk referCrosswalk) {
        this.referCrosswalk = referCrosswalk;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public Optional<String> getEntityType() {
        return Optional.ofNullable(entityType);
    }


}
