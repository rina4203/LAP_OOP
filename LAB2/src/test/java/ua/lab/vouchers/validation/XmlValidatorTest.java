package ua.lab.vouchers.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import ua.lab.vouchers.TestXml;

class XmlValidatorTest {

    @TempDir
    Path tempDir;

    private final XmlValidator validator;

    XmlValidatorTest() throws SAXException {
        validator = new XmlValidator(TestXml.resource("vouchers.xsd"));
    }

    @Test
    void bundledXmlMatchesSchema() throws IOException {
        URL xml = TestXml.resource("vouchers.xml");

        assertTrue(validator.validate(xml).isEmpty());
        assertTrue(validator.isValid(xml));
    }

    @ParameterizedTest(name = "{0} -> {1}")
    @CsvSource(delimiter = '|', value = {
        "stars=\"4\"                | stars=\"6\"",
        "id=\"v01\"                 | id=\"x1\"",
        "type=\"EXCURSION\"         | type=\"CRUISE\"",
        "type=\"EXCURSION\"         | ''",
        "days=\"8\" nights=\"7\"    | days=\"8\"",
        "days=\"8\"                 | days=\"0\"",
        "<room>2</room>             | <room>4</room>",
        "<country>Італія</country>  | <country>I</country>",
        "<meals>BB</meals>          | <meals>UAI</meals>",
        ">14500.00<                 | >-1.00<",
        ">14500.00<                 | >14500.005<"
    })
    void rejectsValuesOutsideSchema(String valid, String invalid)
            throws IOException {
        URL xml = TestXml.write(tempDir,
                TestXml.document(TestXml.VOUCHER.replace(valid, invalid)));

        assertFalse(validator.isValid(xml));
    }

    @Test
    void rejectsDuplicateIds() throws IOException {
        URL xml = TestXml.write(tempDir,
                TestXml.document(TestXml.VOUCHER + TestXml.VOUCHER));

        List<String> errors = validator.validate(xml);

        assertTrue(errors.stream().anyMatch(e -> e.contains("cvc-id.2")
                && e.contains("v01")));
    }

    @Test
    void collectsEveryErrorWithLineNumber() throws IOException {
        URL xml = TestXml.write(tempDir, TestXml.document(TestXml.VOUCHER
                .replace("stars=\"4\"", "stars=\"9\"")
                .replace("<room>2</room>", "<room>7</room>")));

        List<String> errors = validator.validate(xml);

        assertTrue(errors.size() >= 2);
        assertTrue(errors.stream().allMatch(e -> e.startsWith("рядок ")));
    }

    @Test
    void reportsMalformedXml() throws IOException {
        URL xml = TestXml.write(tempDir, "<touristVouchers><touristVoucher>");

        assertFalse(validator.validate(xml).isEmpty());
    }

    @Test
    void rejectsBrokenSchema() throws IOException {
        URL xsd = TestXml.write(tempDir, "<xs:schema/>");

        assertThrows(SAXException.class, () -> new XmlValidator(xsd));
    }

    @Test
    void warningsAreCollectedToo() {
        ValidationErrors errors = new ValidationErrors();

        errors.warning(new SAXParseException("увага", null, null, 3, 1));

        assertEquals(List.of("рядок 3: увага"), errors.getMessages());
    }
}
