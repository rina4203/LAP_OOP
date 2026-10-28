package ua.lab.vouchers.parser;

import java.io.InputStream;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ua.lab.vouchers.model.TouristVoucher;

abstract class AbstractVoucherParser implements VoucherParser {

    protected static final String DISALLOW_DOCTYPE =
            "http://apache.org/xml/features/disallow-doctype-decl";

    private static final Logger LOG =
            LogManager.getLogger(AbstractVoucherParser.class);

    @Override
    public final List<TouristVoucher> parse(InputStream input)
            throws VoucherParseException {
        String parser = getClass().getSimpleName();
        VoucherAssembler assembler = new VoucherAssembler();
        try {
            read(input, assembler);
        } catch (Exception e) {
            throw new VoucherParseException(parser + ": " + e.getMessage(), e);
        }
        List<TouristVoucher> vouchers = assembler.getVouchers();
        LOG.debug("{} прочитав путівок: {}", parser, vouchers.size());
        return vouchers;
    }

    protected abstract void read(InputStream input, VoucherAssembler assembler)
            throws Exception;
}
