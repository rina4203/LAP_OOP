package ua.lab.vouchers.sort;

import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;

import ua.lab.vouchers.model.TouristVoucher;

public enum VoucherOrder implements Comparator<TouristVoucher> {
    BY_COST(Comparator.comparing(TouristVoucher::totalCost)),
    BY_DURATION(Comparator.comparingInt(TouristVoucher::days)
            .thenComparingInt(TouristVoucher::nights)),
    BY_COUNTRY(Comparator.comparing(TouristVoucher::country,
                    Collator.getInstance(Locale.forLanguageTag("uk")))
            .thenComparing(TouristVoucher::totalCost)),
    BY_STARS_DESC(Comparator.comparingInt(
                    (TouristVoucher voucher) -> voucher.hotel().stars())
            .reversed()
            .thenComparing(TouristVoucher::totalCost));

    private final Comparator<TouristVoucher> order;

    VoucherOrder(Comparator<TouristVoucher> order) {
        this.order = order;
    }

    @Override
    public int compare(TouristVoucher first, TouristVoucher second) {
        return order.compare(first, second);
    }
}
