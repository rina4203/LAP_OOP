package ua.lab.vouchers.model;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

public record TouristVoucher(String id, VoucherType type, String country,
        int days, int nights, Transport transport, Hotel hotel,
        List<ServiceCost> costs) {

    public TouristVoucher {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(country, "country");
        Objects.requireNonNull(transport, "transport");
        Objects.requireNonNull(hotel, "hotel");
        costs = List.copyOf(costs);
    }

    public BigDecimal totalCost() {
        return costs.stream()
                .map(ServiceCost::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
