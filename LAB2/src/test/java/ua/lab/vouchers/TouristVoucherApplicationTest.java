package ua.lab.vouchers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import ua.lab.vouchers.model.Hotel;
import ua.lab.vouchers.model.Meals;
import ua.lab.vouchers.model.ServiceCost;
import ua.lab.vouchers.model.ServiceType;
import ua.lab.vouchers.model.TouristVoucher;
import ua.lab.vouchers.model.Transport;
import ua.lab.vouchers.model.VoucherType;

class TouristVoucherApplicationTest {

    @TempDir
    Path tempDir;

    @Test
    void processesBundledXml() throws Exception {
        Path output = tempDir.resolve("grouped.xml");

        assertTrue(TouristVoucherApplication.run(
                TestXml.resource(TouristVoucherApplication.XML), output));
        assertTrue(Files.size(output) > 0);
    }

    @Test
    void stopsWhenXmlDoesNotMatchSchema() throws Exception {
        URL xml = TestXml.write(tempDir, TestXml.document(
                TestXml.VOUCHER.replace("stars=\"4\"", "stars=\"9\"")));
        Path output = tempDir.resolve("grouped.xml");

        assertFalse(TouristVoucherApplication.run(xml, output));
        assertFalse(Files.exists(output));
    }

    @Test
    void mainTakesXmlAndOutputFromArguments() throws Exception {
        URL xml = TestXml.write(tempDir, TestXml.document(TestXml.VOUCHER));
        Path output = tempDir.resolve("result.xml");

        TouristVoucherApplication.main(new String[] {
            Path.of(xml.toURI()).toString(), output.toString()});

        assertTrue(Files.readString(output).contains("<EXCURSION>"));
    }

    @Test
    void mainWithoutArgumentsUsesBundledData() throws Exception {
        Path output = Path.of(TouristVoucherApplication.DEFAULT_OUTPUT);
        Files.deleteIfExists(output);

        TouristVoucherApplication.main(new String[0]);

        assertTrue(Files.exists(output));
    }

    @Test
    void mainSurvivesMissingFile() {
        Path output = tempDir.resolve("result.xml");

        TouristVoucherApplication.main(new String[] {
            tempDir.resolve("missing.xml").toString(), output.toString()});

        assertFalse(Files.exists(output));
    }

    @Test
    void describesVoucherInOneLine() {
        TouristVoucher voucher = new TouristVoucher("v03", VoucherType.WEEKEND,
                "Польща", 3, 2, Transport.AUTO,
                new Hotel(3, Meals.BB, 2, true, false),
                List.of(new ServiceCost(ServiceType.TRANSPORT,
                        new BigDecimal("8600"))));

        assertEquals("  v03  WEEKEND    Польща      3 дн./ 2 ноч. AUTO  3* BB  "
                        + "   8600.00 грн",
                TouristVoucherApplication.describe(voucher));
    }
}
