package ua.lab.vouchers.transform;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import ua.lab.vouchers.TestXml;

class XslTransformerTest {

    @TempDir
    Path tempDir;

    private final XslTransformer transformer;

    XslTransformerTest() throws TransformerConfigurationException {
        transformer = new XslTransformer(
                TestXml.resource("vouchers-by-type.xsl"));
    }

    @Test
    void groupsVouchersByTypeInAlphabeticalOrder() throws Exception {
        Path output = tempDir.resolve("grouped.xml");

        transformer.transform(TestXml.resource("vouchers.xml"), output);

        Element root = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                .parse(output.toFile()).getDocumentElement();
        assertEquals("touristVouchers", root.getTagName());
        List<String> groups = new ArrayList<>();
        int vouchers = 0;
        for (Element group : children(root)) {
            groups.add(group.getTagName() + "=" + children(group).size());
            for (Element voucher : children(group)) {
                assertEquals(group.getTagName(), voucher.getAttribute("type"));
                vouchers++;
            }
        }
        assertEquals(List.of("EXCURSION=2", "HEALTH=1", "PILGRIMAGE=2",
                "REST=3", "WEEKEND=2"), groups);
        assertEquals(10, vouchers);
    }

    @Test
    void outputHasNoExtraNamespaceDeclarations() throws Exception {
        Path output = tempDir.resolve("grouped.xml");

        transformer.transform(TestXml.resource("vouchers.xml"), output);

        String text = Files.readString(output, StandardCharsets.UTF_8);
        assertFalse(text.contains("xmlns"));
        assertTrue(text.contains("<country>Італія</country>"));
    }

    @Test
    void createsMissingOutputDirectories() throws Exception {
        Path output = tempDir.resolve("a/b/grouped.xml");
        URL xml = TestXml.write(tempDir, TestXml.document(TestXml.VOUCHER));

        transformer.transform(xml, output);

        assertTrue(Files.exists(output));
    }

    @Test
    void rejectsBrokenStylesheet() throws IOException {
        URL xsl = TestXml.write(tempDir, "<xsl:stylesheet/>");

        assertThrows(TransformerConfigurationException.class,
                () -> new XslTransformer(xsl));
    }

    @Test
    void reportsMalformedInput() throws IOException {
        URL xml = TestXml.write(tempDir, "<touristVouchers>");
        Path output = tempDir.resolve("out.xml");

        assertThrows(TransformerException.class,
                () -> transformer.transform(xml, output));
        assertFalse(Files.exists(output));
    }

    private static List<Element> children(Element parent) {
        List<Element> elements = new ArrayList<>();
        for (Node node = parent.getFirstChild(); node != null;
                node = node.getNextSibling()) {
            if (node instanceof Element element) {
                elements.add(element);
            }
        }
        return elements;
    }
}
