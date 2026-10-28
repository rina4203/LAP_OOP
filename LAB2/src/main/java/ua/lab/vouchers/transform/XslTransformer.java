package ua.lab.vouchers.transform;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.xml.XMLConstants;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class XslTransformer {

    private static final Logger LOG = LogManager.getLogger(XslTransformer.class);

    private final Templates templates;

    public XslTransformer(URL xsl) throws TransformerConfigurationException {
        TransformerFactory factory = TransformerFactory.newInstance();
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        templates = factory.newTemplates(new StreamSource(xsl.toExternalForm()));
    }

    public void transform(URL xml, Path output)
            throws IOException, TransformerException {
        Path target = output.toAbsolutePath();
        Files.createDirectories(target.getParent());
        try (OutputStream out = Files.newOutputStream(target)) {
            templates.newTransformer().transform(
                    new StreamSource(xml.toExternalForm()),
                    new StreamResult(out));
        } catch (TransformerException e) {
            Files.deleteIfExists(target);
            throw e;
        }
        LOG.info("Путівки згруповано за типом у файлі {}", target);
    }
}
