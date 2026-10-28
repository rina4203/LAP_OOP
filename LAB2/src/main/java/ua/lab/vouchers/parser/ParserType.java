package ua.lab.vouchers.parser;

import java.util.function.Supplier;

public enum ParserType {
    SAX(SaxVoucherParser::new),
    DOM(DomVoucherParser::new),
    STAX(StaxVoucherParser::new);

    private final Supplier<VoucherParser> factory;

    ParserType(Supplier<VoucherParser> factory) {
        this.factory = factory;
    }

    public VoucherParser create() {
        return factory.get();
    }
}
