package ua.lab.vouchers.parser;

import java.io.InputStream;

import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

public class SaxVoucherParser extends AbstractVoucherParser {

    @Override
    protected void read(InputStream input, VoucherAssembler assembler)
            throws Exception {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setFeature(DISALLOW_DOCTYPE, true);
        factory.newSAXParser().parse(input, new Handler(assembler));
    }

    private static final class Handler extends DefaultHandler {

        private final VoucherAssembler assembler;
        private final StringBuilder text = new StringBuilder();

        private Handler(VoucherAssembler assembler) {
            this.assembler = assembler;
        }

        @Override
        public void startElement(String uri, String localName, String qName,
                Attributes attributes) {
            text.setLength(0);
            assembler.start(qName, attributes::getValue);
        }

        @Override
        public void characters(char[] chars, int start, int length) {
            text.append(chars, start, length);
        }

        @Override
        public void endElement(String uri, String localName, String qName) {
            assembler.end(qName, text.toString());
            text.setLength(0);
        }
    }
}
