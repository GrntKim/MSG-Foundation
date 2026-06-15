class Investment extends Asset {
    private final String itemNumber;
    private final String itemName;
    private double annualReturn;

    Investment(String itemNumber, String itemName, double annualReturn) {
        requireText(itemNumber, "itemNumber");
        requireText(itemName, "itemName");
        requireNonNegative(annualReturn, "annualReturn");
        this.itemNumber = itemNumber;
        this.itemName = itemName;
        this.annualReturn = annualReturn;
        touchDate();
    }

    @Override
    double weeklyAmount() {
        return annualReturn / WEEKS_PER_YEAR;
    }

    void updateReturn(double newReturn) {
        requireNonNegative(newReturn, "newReturn");
        annualReturn = newReturn;
        touchDate();
    }

    boolean hasItemNumber(String candidate) {
        return itemNumber.equals(candidate);
    }

    String listingLine() {
        return String.format(
                "%-8s %-24s annualReturn=%12s weeklyAmount=%10s updated=%s",
                itemNumber,
                itemName,
                Asset.formatMoney(annualReturn),
                Asset.formatMoney(weeklyAmount()),
                dateUpdated
        );
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
