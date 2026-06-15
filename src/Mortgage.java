import java.time.LocalDate;
import java.util.Locale;

class Mortgage extends Asset {
    static final String FIELD_WEEKLY_INCOME = "weeklyIncome";
    static final String FIELD_REAL_ESTATE_TAX = "realEstateTax";
    static final String FIELD_INSURANCE_PREMIUM = "insurancePremium";
    static final double INCOME_PAYMENT_LIMIT_RATE = 0.28;

    private final String accountNumber;
    private final String mortgageeName;
    private final double purchasePrice;
    private final LocalDate dateIssued;
    private final double weeklyPandI;
    private double weeklyIncome;
    private double realEstateTax;
    private double insurancePremium;
    private LocalDate weeklyIncomeUpdated;
    private LocalDate realEstateTaxUpdated;
    private LocalDate insurancePremiumUpdated;

    Mortgage(
            String accountNumber,
            String mortgageeName,
            double purchasePrice,
            LocalDate dateIssued,
            double weeklyPandI,
            double weeklyIncome,
            double realEstateTax,
            double insurancePremium
    ) {
        requireText(accountNumber, "accountNumber");
        requireText(mortgageeName, "mortgageeName");
        requireNonNegative(purchasePrice, "purchasePrice");
        requireNonNegative(weeklyPandI, "weeklyPandI");
        requireNonNegative(weeklyIncome, "weeklyIncome");
        requireNonNegative(realEstateTax, "realEstateTax");
        requireNonNegative(insurancePremium, "insurancePremium");
        if (dateIssued == null) {
            throw new IllegalArgumentException("dateIssued must not be null.");
        }

        this.accountNumber = accountNumber;
        this.mortgageeName = mortgageeName;
        this.purchasePrice = purchasePrice;
        this.dateIssued = dateIssued;
        this.weeklyPandI = weeklyPandI;
        this.weeklyIncome = weeklyIncome;
        this.realEstateTax = realEstateTax;
        this.insurancePremium = insurancePremium;
        touchDate();
        weeklyIncomeUpdated = dateUpdated;
        realEstateTaxUpdated = dateUpdated;
        insurancePremiumUpdated = dateUpdated;
    }

    @Override
    double weeklyAmount() {
        return weeklyPandI + weeklyEscrow() - weeklyGrant();
    }

    double weeklyEscrow() {
        return (realEstateTax + insurancePremium) / WEEKS_PER_YEAR;
    }

    double weeklyGrant() {
        double weeklyPayment = weeklyPandI + weeklyEscrow();
        double incomeLimit = weeklyIncome * INCOME_PAYMENT_LIMIT_RATE;
        return Math.max(0, weeklyPayment - incomeLimit);
    }

    double weeklyPaymentBeforeGrant() {
        return weeklyPandI + weeklyEscrow();
    }

    void update(String field, double value) {
        requireNonNegative(value, "newValue");
        String normalized = normalizeField(field);
        LocalDate today = LocalDate.now();

        switch (normalized) {
            case FIELD_WEEKLY_INCOME:
                weeklyIncome = value;
                weeklyIncomeUpdated = today;
                break;
            case FIELD_REAL_ESTATE_TAX:
                realEstateTax = value;
                realEstateTaxUpdated = today;
                break;
            case FIELD_INSURANCE_PREMIUM:
                insurancePremium = value;
                insurancePremiumUpdated = today;
                break;
            default:
                throw new IllegalArgumentException(
                        "OC3 precondition failed: field must be weeklyIncome, realEstateTax, or insurancePremium."
                );
        }
        touchDate();
    }

    boolean hasAccountNumber(String candidate) {
        return accountNumber.equals(candidate);
    }

    String listingLine() {
        return String.format(
                "%-8s %-16s price=%11s issued=%s P&I=%9s income=%9s escrow=%9s grant=%9s net=%9s updated=%s",
                accountNumber,
                mortgageeName,
                Asset.formatMoney(purchasePrice),
                dateIssued,
                Asset.formatMoney(weeklyPandI),
                Asset.formatMoney(weeklyIncome),
                Asset.formatMoney(weeklyEscrow()),
                Asset.formatMoney(weeklyGrant()),
                Asset.formatMoney(weeklyAmount()),
                dateUpdated
        );
    }

    String updateDatesLine() {
        return String.format(
                "incomeUpdated=%s, taxUpdated=%s, premiumUpdated=%s",
                weeklyIncomeUpdated,
                realEstateTaxUpdated,
                insurancePremiumUpdated
        );
    }

    private static String normalizeField(String field) {
        if (field == null) {
            return "";
        }
        String trimmed = field.trim();
        if (FIELD_WEEKLY_INCOME.equals(trimmed)) {
            return FIELD_WEEKLY_INCOME;
        }
        if (FIELD_REAL_ESTATE_TAX.equals(trimmed)) {
            return FIELD_REAL_ESTATE_TAX;
        }
        if (FIELD_INSURANCE_PREMIUM.equals(trimmed)) {
            return FIELD_INSURANCE_PREMIUM;
        }
        return trimmed.toLowerCase(Locale.ROOT);
    }

    private static void requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank.");
        }
    }

    private static void requireNonNegative(double value, String fieldName) {
        if (value < 0) {
            throw new IllegalArgumentException(fieldName + " must be zero or greater.");
        }
    }
}
