import java.time.LocalDate;

class OperatingExpense {
    private double annualExpenses;
    private LocalDate dateUpdated;

    OperatingExpense(double annualExpenses) {
        update(annualExpenses);
    }

    double weeklyAmount() {
        return annualExpenses / Asset.WEEKS_PER_YEAR;
    }

    void update(double newAmount) {
        if (newAmount < 0) {
            throw new IllegalArgumentException("annualExpenses must be zero or greater.");
        }
        annualExpenses = newAmount;
        touchDate();
    }

    LocalDate getDateUpdated() {
        return dateUpdated;
    }

    private void touchDate() {
        dateUpdated = LocalDate.now();
    }
}
