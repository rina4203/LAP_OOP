package ua.lab.vouchers.validation;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.SAXException;

public class XmlValidator {

    private static final Logger LOG = LogManager.getLogger(XmlValidator.class);

    private final Schema schema;

    public XmlValidator(URL xsd) throws SAXException {
        SchemaFactory factory =
                SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        schema = factory.newSchema(xsd);
    }

    public List<String> validate(URL xml) throws IOException {
        Validator validator = schema.newValidator();
        ValidationErrors errors = new ValidationErrors();
        validator.setErrorHandler(errors);
        try {
            validator.validate(new StreamSource(xml.toExternalForm()));
        } catch (SAXException e) {
            LOG.debug("Валідацію перервано: {}", e.getMessage());
        }
        List<String> messages = errors.getMessages();
        if (messages.isEmpty()) {
            LOG.info("{} відповідає схемі", xml);
        } else {
            messages.forEach(message -> LOG.warn("Невідповідність схемі, {}",
                    message));
        }
        return messages;
    }

    public boolean isValid(URL xml) throws IOException {
        return validate(xml).isEmpty();
    }
}
