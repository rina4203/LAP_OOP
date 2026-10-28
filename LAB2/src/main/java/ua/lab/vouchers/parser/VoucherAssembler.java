package ua.lab.vouchers.parser;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import ua.lab.vouchers.model.Hotel;
import ua.lab.vouchers.model.Meals;
import ua.lab.vouchers.model.ServiceCost;
import ua.lab.vouchers.model.ServiceType;
import ua.lab.vouchers.model.TouristVoucher;
import ua.lab.vouchers.model.Transport;
import ua.lab.vouchers.model.VoucherType;

/**
 * Збирає путівки з подій "початок/кінець елемента". Завдяки цьому
 * парсерам SAX, DOM і StAX достатньо лише передавати свої події
 * у {@link #start} та {@link #end}.
 */
public class VoucherAssembler {

    private final List<TouristVoucher> vouchers = new ArrayList<>();
    private Draft draft;
    private ServiceType service;

    public void start(String element, Function<String, String> attributes) {
        switch (element) {
            case "touristVoucher" -> draft = new Draft(
                    required(attributes, "id"),
                    VoucherType.valueOf(required(attributes, "type")));
            case "duration" -> {
                draft.days = Integer.parseInt(required(attributes, "days"));
                draft.nights =
                        Integer.parseInt(required(attributes, "nights"));
            }
            case "hotel" ->
                    draft.stars = Integer.parseInt(required(attributes, "stars"));
            case "service" ->
                    service = ServiceType.valueOf(required(attributes, "name"));
            default -> {
            }
        }
    }

    public void end(String element, String text) {
        String value = text.trim();
        switch (element) {
            case "country" -> draft.country = value;
            case "transport" -> draft.transport = Transport.valueOf(value);
            case "meals" -> draft.meals = Meals.valueOf(value);
            case "room" -> draft.room = Integer.parseInt(value);
            case "tv" -> draft.tv = parseBoolean(value);
            case "airConditioner" -> draft.airConditioner = parseBoolean(value);
            case "service" ->
                    draft.costs.add(new ServiceCost(service, new BigDecimal(value)));
            case "touristVoucher" -> vouchers.add(draft.build());
            default -> {
            }
        }
    }

    public List<TouristVoucher> getVouchers() {
        return List.copyOf(vouchers);
    }

    private static String required(Function<String, String> attributes,
            String name) {
        String value = attributes.apply(name);
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException("відсутній атрибут " + name);
        }
        return value;
    }

    private static boolean parseBoolean(String value) {
        return "true".equals(value) || "1".equals(value);
    }

    private static final class Draft {

        private final String id;
        private final VoucherType type;
        private final List<ServiceCost> costs = new ArrayList<>();
        private String country;
        private int days;
        private int nights;
        private Transport transport;
        private int stars;
        private Meals meals;
        private int room;
        private boolean tv;
        private boolean airConditioner;

        private Draft(String id, VoucherType type) {
            this.id = id;
            this.type = type;
        }

        private TouristVoucher build() {
            Hotel hotel = new Hotel(stars, meals, room, tv, airConditioner);
            return new TouristVoucher(id, type, country, days, nights,
                    transport, hotel, costs);
        }
    }
}
