package ua.lab.vouchers.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import ua.lab.vouchers.TestXml;
import ua.lab.vouchers.model.Hotel;
import ua.lab.vouchers.model.Meals;
import ua.lab.vouchers.model.ServiceCost;
import ua.lab.vouchers.model.ServiceType;
import ua.lab.vouchers.model.TouristVoucher;
import ua.lab.vouchers.model.Transport;
import ua.lab.vouchers.model.VoucherType;

class VoucherParsersTest {

    private static final URL BUNDLED = TestXml.resource("vouchers.xml");

    @TempDir
    Path tempDir;

    @ParameterizedTest
    @EnumSource(ParserType.class)
    void parsesAllBundledVouchers(ParserType type)
            throws VoucherParseException {
        List<TouristVoucher> vouchers = type.create().parse(BUNDLED);

        assertEquals(10, vouchers.size());
        assertEquals("v10", vouchers.get(9).id());
        assertEquals(new BigDecimal("67000.00"), vouchers.get(8).totalCost());
    }

    @ParameterizedTest
    @EnumSource(ParserType.class)
    void readsEveryFieldOfVoucher(ParserType type) throws Exception {
        URL xml = TestXml.write(tempDir, TestXml.document(TestXml.VOUCHER));

        List<TouristVoucher> vouchers = type.create().parse(xml);

        TouristVoucher expected = new TouristVoucher("v01",
                VoucherType.EXCURSION, "Італія", 8, 7, Transport.AIR,
                new Hotel(4, Meals.BB, 2, true, true),
                List.of(new ServiceCost(ServiceType.TRANSPORT,
                                new BigDecimal("14500.00")),
                        new ServiceCost(ServiceType.ACCOMMODATION,
                                new BigDecimal("21000.00"))));
        assertEquals(List.of(expected), vouchers);
    }

    @Test
    void allParsersReturnTheSameVouchers() throws VoucherParseException {
        List<TouristVoucher> sax = ParserType.SAX.create().parse(BUNDLED);

        assertEquals(sax, ParserType.DOM.create().parse(BUNDLED));
        assertEquals(sax, ParserType.STAX.create().parse(BUNDLED));
    }

    @ParameterizedTest
    @EnumSource(ParserType.class)
    void reportsInvalidValueWithParserName(ParserType type) throws IOException {
        URL xml = TestXml.write(tempDir, TestXml.document(
                TestXml.VOUCHER.replace(">AIR<", ">PLANE<")));
        VoucherParser parser = type.create();

        VoucherParseException e = assertThrows(VoucherParseException.class,
                () -> parser.parse(xml));

        assertTrue(e.getMessage().startsWith(
                parser.getClass().getSimpleName() + ": "));
        assertInstanceOf(IllegalArgumentException.class, e.getCause());
    }

    @ParameterizedTest
    @EnumSource(ParserType.class)
    void reportsMalformedXml(ParserType type) throws IOException {
        URL xml = TestXml.write(tempDir, "<touristVouchers><touristVoucher>");
        VoucherParser parser = type.create();

        assertThrows(VoucherParseException.class, () -> parser.parse(xml));
    }

    @ParameterizedTest
    @EnumSource(ParserType.class)
    void refusesDocumentsWithExternalEntities(ParserType type)
            throws IOException {
        URL xml = TestXml.write(tempDir, """
                <?xml version="1.0"?>
                <!DOCTYPE touristVouchers [
                    <!ENTITY secret SYSTEM "file:///C:/Windows/win.ini">
                ]>
                <touristVouchers>&secret;</touristVouchers>
                """);
        VoucherParser parser = type.create();

        assertThrows(VoucherParseException.class, () -> parser.parse(xml));
    }

    @Test
    void reportsUnreadableSource() throws IOException {
        URL missing = tempDir.resolve("missing.xml").toUri().toURL();
        VoucherParser parser = ParserType.DOM.create();

        VoucherParseException e = assertThrows(VoucherParseException.class,
                () -> parser.parse(missing));

        assertTrue(e.getMessage().startsWith("Не вдалося прочитати"));
    }

    @Test
    void createsParserOfRequestedType() {
        assertInstanceOf(SaxVoucherParser.class, ParserType.SAX.create());
        assertInstanceOf(DomVoucherParser.class, ParserType.DOM.create());
        assertInstanceOf(StaxVoucherParser.class, ParserType.STAX.create());
    }
}
