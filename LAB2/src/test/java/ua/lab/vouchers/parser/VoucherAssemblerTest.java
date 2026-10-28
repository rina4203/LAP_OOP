package ua.lab.vouchers.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.junit.jupiter.api.Test;

import ua.lab.vouchers.model.Hotel;
import ua.lab.vouchers.model.Meals;
import ua.lab.vouchers.model.ServiceCost;
import ua.lab.vouchers.model.ServiceType;
import ua.lab.vouchers.model.TouristVoucher;
import ua.lab.vouchers.model.Transport;
import ua.lab.vouchers.model.VoucherType;

class VoucherAssemblerTest {

    private static final Function<String, String> NO_ATTRIBUTES = name -> null;

    private final VoucherAssembler assembler = new VoucherAssembler();

    @Test
    void buildsVoucherFromElementEvents() {
        assembler.start("touristVouchers", NO_ATTRIBUTES);
        assembler.start("touristVoucher",
                Map.of("id", "v07", "type", "HEALTH")::get);
        leaf("country", "  Чехія ");
        assembler.start("duration", Map.of("days", "12", "nights", "11")::get);
        assembler.end("duration", "");
        leaf("transport", "RAIL");
        assembler.start("hotel", Map.of("stars", "4")::get);
        leaf("meals", "FB");
        leaf("room", "1");
        leaf("tv", "true");
        leaf("airConditioner", "0");
        assembler.end("hotel", "");
        assembler.start("service", Map.of("name", "ACCOMMODATION")::get);
        assembler.end("service", "33000.00");
        assembler.end("touristVoucher", "");
        assembler.end("touristVouchers", "");

        TouristVoucher expected = new TouristVoucher("v07", VoucherType.HEALTH,
                "Чехія", 12, 11, Transport.RAIL,
                new Hotel(4, Meals.FB, 1, true, false),
                List.of(new ServiceCost(ServiceType.ACCOMMODATION,
                        new BigDecimal("33000.00"))));
        assertEquals(List.of(expected), assembler.getVouchers());
    }

    @Test
    void noVouchersBeforeAnyEvents() {
        assertTrue(assembler.getVouchers().isEmpty());
    }

    @Test
    void rejectsMissingOrEmptyAttribute() {
        Function<String, String> empty = name -> "";

        IllegalArgumentException missing = assertThrows(
                IllegalArgumentException.class,
                () -> assembler.start("touristVoucher", NO_ATTRIBUTES));
        assertThrows(IllegalArgumentException.class,
                () -> assembler.start("hotel", empty));
        assertEquals("відсутній атрибут id", missing.getMessage());
    }

    private void leaf(String element, String text) {
        assembler.start(element, NO_ATTRIBUTES);
        assembler.end(element, text);
    }
}
