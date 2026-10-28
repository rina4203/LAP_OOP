package ua.lab.vouchers.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

class TouristVoucherTest {

    private static final Hotel HOTEL = new Hotel(4, Meals.BB, 2, true, false);

    @Test
    void totalCostIsSumOfIncludedServices() {
        TouristVoucher voucher = voucher(List.of(
                new ServiceCost(ServiceType.TRANSPORT, new BigDecimal("14500.00")),
                new ServiceCost(ServiceType.ACCOMMODATION, new BigDecimal("21000.50"))));

        assertEquals(new BigDecimal("35500.50"), voucher.totalCost());
    }

    @Test
    void totalCostWithoutServicesIsZero() {
        assertEquals(BigDecimal.ZERO, voucher(List.of()).totalCost());
    }

    @Test
    void costsAreCopiedAndUnmodifiable() {
        List<ServiceCost> costs = new ArrayList<>();
        costs.add(new ServiceCost(ServiceType.MEALS, BigDecimal.ONE));
        TouristVoucher voucher = voucher(costs);

        costs.clear();

        assertEquals(1, voucher.costs().size());
        assertThrows(UnsupportedOperationException.class,
                () -> voucher.costs().clear());
    }

    @Test
    void requiresMandatoryFields() {
        List<ServiceCost> costs = List.of();

        assertThrows(NullPointerException.class, () -> new TouristVoucher(null,
                VoucherType.REST, "Греція", 5, 4, Transport.AIR, HOTEL, costs));
        assertThrows(NullPointerException.class, () -> new TouristVoucher("v1",
                null, "Греція", 5, 4, Transport.AIR, HOTEL, costs));
        assertThrows(NullPointerException.class, () -> new TouristVoucher("v1",
                VoucherType.REST, null, 5, 4, Transport.AIR, HOTEL, costs));
        assertThrows(NullPointerException.class, () -> new TouristVoucher("v1",
                VoucherType.REST, "Греція", 5, 4, null, HOTEL, costs));
        assertThrows(NullPointerException.class, () -> new TouristVoucher("v1",
                VoucherType.REST, "Греція", 5, 4, Transport.AIR, null, costs));
        assertThrows(NullPointerException.class, () -> new TouristVoucher("v1",
                VoucherType.REST, "Греція", 5, 4, Transport.AIR, HOTEL, null));
    }

    @Test
    void hotelAndServiceRequireTheirValues() {
        assertThrows(NullPointerException.class,
                () -> new Hotel(3, null, 1, false, false));
        assertThrows(NullPointerException.class,
                () -> new ServiceCost(null, BigDecimal.ONE));
        assertThrows(NullPointerException.class,
                () -> new ServiceCost(ServiceType.MEALS, null));
    }

    private static TouristVoucher voucher(List<ServiceCost> costs) {
        return new TouristVoucher("v01", VoucherType.EXCURSION, "Італія", 8, 7,
                Transport.AIR, HOTEL, costs);
    }
}
