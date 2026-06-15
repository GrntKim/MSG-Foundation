import java.time.LocalDate;
import java.util.Locale;

abstract class Asset {
    static final int WEEKS_PER_YEAR = 52;

    protected LocalDate dateUpdated;

    Asset() {
        touchDate();
    }

    abstract double weeklyAmount();

    void touchDate() {
        dateUpdated = LocalDate.now();
    }

    LocalDate getDateUpdated() {
        return dateUpdated;
    }

    static String formatMoney(double value) {
        return String.format(Locale.US, "$%,.2f", value);
    }
}
