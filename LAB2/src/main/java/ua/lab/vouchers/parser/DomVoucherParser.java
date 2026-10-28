package ua.lab.vouchers.parser;

import java.io.InputStream;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class DomVoucherParser extends AbstractVoucherParser {

    @Override
    protected void read(InputStream input, VoucherAssembler assembler)
            throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature(DISALLOW_DOCTYPE, true);
        Element root = factory.newDocumentBuilder().parse(input)
                .getDocumentElement();
        walk(root, assembler);
    }

    private static void walk(Element element, VoucherAssembler assembler) {
        assembler.start(element.getTagName(), element::getAttribute);
        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i) instanceof Element child) {
                walk(child, assembler);
            }
        }
        assembler.end(element.getTagName(), element.getTextContent());
    }
}
