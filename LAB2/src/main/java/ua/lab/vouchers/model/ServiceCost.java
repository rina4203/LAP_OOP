package ua.lab.vouchers.model;

import java.math.BigDecimal;
import java.util.Objects;

public record ServiceCost(ServiceType service, BigDecimal amount) {

    public ServiceCost {
        Objects.requireNonNull(service, "service");
        Objects.requireNonNull(amount, "amount");
    }
}
