package ua.lab.vouchers.parser;

import java.io.InputStream;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

public class StaxVoucherParser extends AbstractVoucherParser {

    @Override
    protected void read(InputStream input, VoucherAssembler assembler)
            throws Exception {
        XMLInputFactory factory = XMLInputFactory.newInstance();
        factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        XMLStreamReader reader = factory.createXMLStreamReader(input);
        StringBuilder text = new StringBuilder();
        try {
            while (reader.hasNext()) {
                switch (reader.next()) {
                    case XMLStreamConstants.START_ELEMENT -> {
                        text.setLength(0);
                        assembler.start(reader.getLocalName(),
                                name -> reader.getAttributeValue(null, name));
                    }
                    case XMLStreamConstants.CHARACTERS ->
                            text.append(reader.getText());
                    case XMLStreamConstants.END_ELEMENT -> {
                        assembler.end(reader.getLocalName(), text.toString());
                        text.setLength(0);
                    }
                    default -> {
                    }
                }
            }
        } finally {
            reader.close();
        }
    }
}
