package ua.lab.vouchers.model;

import java.util.Objects;

public record Hotel(int stars, Meals meals, int roomCapacity, boolean tv,
        boolean airConditioner) {

    public Hotel {
        Objects.requireNonNull(meals, "meals");
    }
}
