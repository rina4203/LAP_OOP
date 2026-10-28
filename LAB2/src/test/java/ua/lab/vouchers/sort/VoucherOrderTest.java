package ua.lab.vouchers.sort;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import ua.lab.vouchers.model.Hotel;
import ua.lab.vouchers.model.Meals;
import ua.lab.vouchers.model.ServiceCost;
import ua.lab.vouchers.model.ServiceType;
import ua.lab.vouchers.model.TouristVoucher;
import ua.lab.vouchers.model.Transport;
import ua.lab.vouchers.model.VoucherType;

class VoucherOrderTest {

    private final TouristVoucher italy = voucher("v1", "Італія", 8, 7, 4, "40300");
    private final TouristVoucher egypt = voucher("v2", "Єгипет", 11, 10, 5, "57400");
    private final TouristVoucher greece = voucher("v3", "Греція", 8, 6, 2, "19600");
    private final TouristVoucher austria = voucher("v4", "Австрія", 3, 2, 5, "8600");
    private final List<TouristVoucher> vouchers =
            List.of(italy, egypt, greece, austria);

    @Test
    void sortsByTotalCost() {
        assertEquals(List.of("v4", "v3", "v1", "v2"), ids(VoucherOrder.BY_COST));
    }

    @Test
    void sortsByDaysThenNights() {
        assertEquals(List.of("v4", "v3", "v1", "v2"),
                ids(VoucherOrder.BY_DURATION));
    }

    @Test
    void sortsCountriesByUkrainianAlphabet() {
        assertEquals(List.of("v4", "v3", "v2", "v1"),
                ids(VoucherOrder.BY_COUNTRY));
    }

    @Test
    void sortsByStarsDescendingThenCost() {
        assertEquals(List.of("v4", "v2", "v1", "v3"),
                ids(VoucherOrder.BY_STARS_DESC));
    }

    private List<String> ids(VoucherOrder order) {
        return vouchers.stream().sorted(order).map(TouristVoucher::id)
                .collect(Collectors.toList());
    }

    private static TouristVoucher voucher(String id, String country, int days,
            int nights, int stars, String cost) {
        return new TouristVoucher(id, VoucherType.REST, country, days, nights,
                Transport.AIR, new Hotel(stars, Meals.BB, 2, true, true),
                List.of(new ServiceCost(ServiceType.TRANSPORT,
                        new BigDecimal(cost))));
    }
}
