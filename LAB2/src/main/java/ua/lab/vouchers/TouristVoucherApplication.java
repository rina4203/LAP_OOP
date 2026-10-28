package ua.lab.vouchers;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import javax.xml.transform.TransformerException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.SAXException;

import ua.lab.vouchers.model.TouristVoucher;
import ua.lab.vouchers.parser.ParserType;
import ua.lab.vouchers.parser.VoucherParseException;
import ua.lab.vouchers.sort.VoucherOrder;
import ua.lab.vouchers.transform.XslTransformer;
import ua.lab.vouchers.validation.XmlValidator;

public final class TouristVoucherApplication {

    static final String XML = "vouchers.xml";
    static final String XSD = "vouchers.xsd";
    static final String XSL = "vouchers-by-type.xsl";
    static final String DEFAULT_OUTPUT = "target/vouchers-by-type.xml";

    private static final Logger LOG =
            LogManager.getLogger(TouristVoucherApplication.class);

    private TouristVoucherApplication() {
    }

    public static void main(String[] args) {
        try {
            URL xml = (args.length > 0) ? Path.of(args[0]).toUri().toURL()
                    : resource(XML);
            Path output = Path.of((args.length > 1) ? args[1] : DEFAULT_OUTPUT);
            run(xml, output);
        } catch (Exception e) {
            LOG.error("Програму зупинено: {}", e.getMessage());
        }
    }

    static boolean run(URL xml, Path output) throws IOException, SAXException,
            VoucherParseException, TransformerException {
        if (!new XmlValidator(resource(XSD)).isValid(xml)) {
            LOG.error("{} не відповідає схемі, обробку зупинено", xml);
            return false;
        }
        List<TouristVoucher> vouchers = List.of();
        for (ParserType type : ParserType.values()) {
            vouchers = type.create().parse(xml);
            LOG.info("{}: прочитано путівок: {}", type, vouchers.size());
        }
        for (VoucherOrder order : VoucherOrder.values()) {
            LOG.info("Сортування {}:", order);
            vouchers.stream().sorted(order)
                    .forEach(voucher -> LOG.info(describe(voucher)));
        }
        new XslTransformer(resource(XSL)).transform(xml, output);
        return true;
    }

    static String describe(TouristVoucher voucher) {
        return String.format(Locale.ROOT,
                "  %-4s %-10s %-10s %2d дн./%2d ноч. %-5s %d* %-4s %9.2f грн",
                voucher.id(), voucher.type(), voucher.country(), voucher.days(),
                voucher.nights(), voucher.transport(), voucher.hotel().stars(),
                voucher.hotel().meals(), voucher.totalCost());
    }

    private static URL resource(String name) {
        return Objects.requireNonNull(
                TouristVoucherApplication.class.getClassLoader().getResource(name),
                name);
    }
}
