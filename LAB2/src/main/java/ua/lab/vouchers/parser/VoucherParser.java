package ua.lab.vouchers.parser;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import ua.lab.vouchers.model.TouristVoucher;

public interface VoucherParser {

    List<TouristVoucher> parse(InputStream input) throws VoucherParseException;

    default List<TouristVoucher> parse(URL source)
            throws VoucherParseException {
        try (InputStream input = source.openStream()) {
            return parse(input);
        } catch (IOException e) {
            throw new VoucherParseException("Не вдалося прочитати " + source, e);
        }
    }
}
