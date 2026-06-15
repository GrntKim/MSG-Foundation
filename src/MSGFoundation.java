import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

class MSGFoundation {
    private final List<Asset> assets = new ArrayList<>();
    private final OperatingExpense operatingExpense = new OperatingExpense(0);
    private double amountAvailable;
    private boolean amountAvailableEstimated;

    // OC1 estimateFundsForWeek
    // Pre: MSGFoundation and OperatingExpense exist; zero or more investments and mortgages may be registered.
    // Post: weekly investment income, operating cost, mortgage flow, grants, and amountAvailable were calculated; a report was created.
    public String estimateFundsForWeek() {
        double weeklyInvestmentIncome = 0;
        double weeklyAssetContribution = 0;
        double weeklyMortgageNet = 0;
        double weeklyMortgagePayments = 0;
        double weeklyGrants = 0;

        for (Asset asset : assets) {
            double weeklyAmount = asset.weeklyAmount();
            weeklyAssetContribution += weeklyAmount;
            if (asset instanceof Investment) {
                weeklyInvestmentIncome += weeklyAmount;
            } else if (asset instanceof Mortgage mortgage) {
                weeklyMortgageNet += weeklyAmount;
                weeklyMortgagePayments += mortgage.weeklyPaymentBeforeGrant();
                weeklyGrants += mortgage.weeklyGrant();
            }
        }

        double weeklyOperatingCost = operatingExpense.weeklyAmount();
        amountAvailable = weeklyAssetContribution - weeklyOperatingCost;
        amountAvailableEstimated = true;

        return buildWeeklyFundsReport(
                weeklyInvestmentIncome,
                weeklyOperatingCost,
                weeklyMortgagePayments,
                weeklyGrants,
                weeklyMortgageNet
        );
    }

    // OC2 addMortgage
    // Pre: amountAvailable was produced by OC1; no mortgage with the same account number exists.
    // Post: on success a mortgage was created and associated, and amountAvailable was reduced by price; on failure no state changed.
    public String addMortgage(
            String accountNumber,
            String mortgageeName,
            double price,
            LocalDate dateIssued,
            double weeklyPandI,
            double weeklyIncome,
            double realEstateTax,
            double insurancePremium
    ) {
        ensureAmountAvailableEstimated();
        if (findMortgage(accountNumber) != null) {
            throw new IllegalArgumentException("OC2 precondition failed: duplicate mortgage account number " + accountNumber);
        }
        if (price < 0) {
            throw new IllegalArgumentException("price must be zero or greater.");
        }

        if (price > amountAvailable) {
            return "자금 부족 경고: " + accountNumber + " 등록 거절. 필요 금액 "
                    + Asset.formatMoney(price) + ", 사용 가능 금액 " + Asset.formatMoney(amountAvailable);
        }

        Mortgage mortgage = new Mortgage(
                accountNumber,
                mortgageeName,
                price,
                dateIssued,
                weeklyPandI,
                weeklyIncome,
                realEstateTax,
                insurancePremium
        );
        assets.add(mortgage);
        amountAvailable -= price;
        return "등록 확인: " + accountNumber + " 모기지 등록 완료. 남은 사용 가능 금액 "
                + Asset.formatMoney(amountAvailable);
    }

    // OC3 updateMortgage
    // Pre: the mortgage exists; field is weeklyIncome, realEstateTax, or insurancePremium.
    // Post: the requested mortgage field and its update date were changed to the new value/current date.
    public String updateMortgage(String accountNumber, String field, double newValue) {
        Mortgage mortgage = requireMortgage(accountNumber, "OC3");
        mortgage.update(field, newValue);
        return "갱신 확인: " + accountNumber + " " + field + " = " + Asset.formatMoney(newValue)
                + " (" + mortgage.updateDatesLine() + ")";
    }

    // OC4 addInvestment
    // Pre: no investment with the same item number exists.
    // Post: a new investment was created, initialized, dated, and associated with MSGFoundation.
    public String addInvestment(String itemNumber, String name, double annualReturn) {
        if (findInvestment(itemNumber) != null) {
            throw new IllegalArgumentException("OC4 precondition failed: duplicate investment item number " + itemNumber);
        }
        Investment investment = new Investment(itemNumber, name, annualReturn);
        assets.add(investment);
        return "등록 확인: " + itemNumber + " 투자 등록 완료. 예상 주간 수익 "
                + Asset.formatMoney(investment.weeklyAmount());
    }

    // OC5 updateInvestmentReturn
    // Pre: the investment exists.
    // Post: annualReturn and dateUpdated were changed.
    public String updateInvestmentReturn(String itemNumber, double newReturn) {
        Investment investment = requireInvestment(itemNumber, "OC5");
        investment.updateReturn(newReturn);
        return "갱신 확인: " + itemNumber + " annualReturn = " + Asset.formatMoney(newReturn);
    }

    // OC6 deleteInvestment
    // Pre: the investment exists.
    // Post: the association between MSGFoundation and the investment was removed.
    public String deleteInvestment(String itemNumber) {
        Investment investment = requireInvestment(itemNumber, "OC6");
        assets.remove(investment);
        return "삭제 확인: " + itemNumber + " 투자 제거 완료.";
    }

    // OC7 updateOperatingExpenses
    // Pre: OperatingExpense exists.
    // Post: annualExpenses and dateUpdated were changed.
    public String updateOperatingExpenses(double newAmount) {
        operatingExpense.update(newAmount);
        return "갱신 확인: annualExpenses = " + Asset.formatMoney(newAmount)
                + ", updated = " + operatingExpense.getDateUpdated();
    }

    // OC8 produceInvestmentListing
    // Pre: MSGFoundation exists.
    // Post: an investment listing containing zero or more investments was created.
    public String produceInvestmentListing() {
        StringBuilder listing = new StringBuilder();
        listing.append("Investment Listing\n");
        listing.append("------------------\n");
        int count = 0;
        for (Asset asset : assets) {
            if (asset instanceof Investment investment) {
                listing.append(investment.listingLine()).append(System.lineSeparator());
                count++;
            }
        }
        if (count == 0) {
            listing.append("(등록된 투자가 없습니다.)").append(System.lineSeparator());
        }
        listing.append("total investments: ").append(count);
        return listing.toString();
    }

    // OC9 produceMortgageListing
    // Pre: MSGFoundation exists.
    // Post: a mortgage listing containing zero or more mortgages was created.
    public String produceMortgageListing() {
        StringBuilder listing = new StringBuilder();
        listing.append("Mortgage Listing\n");
        listing.append("----------------\n");
        int count = 0;
        for (Asset asset : assets) {
            if (asset instanceof Mortgage mortgage) {
                listing.append(mortgage.listingLine()).append(System.lineSeparator());
                count++;
            }
        }
        if (count == 0) {
            listing.append("(등록된 모기지가 없습니다.)").append(System.lineSeparator());
        }
        listing.append("total mortgages: ").append(count);
        return listing.toString();
    }

    double getAmountAvailable() {
        return amountAvailable;
    }

    int mortgageCount() {
        int count = 0;
        for (Asset asset : assets) {
            if (asset instanceof Mortgage) {
                count++;
            }
        }
        return count;
    }

    int investmentCount() {
        int count = 0;
        for (Asset asset : assets) {
            if (asset instanceof Investment) {
                count++;
            }
        }
        return count;
    }

    private String buildWeeklyFundsReport(
            double weeklyInvestmentIncome,
            double weeklyOperatingCost,
            double weeklyMortgagePayments,
            double weeklyGrants,
            double weeklyMortgageNet
    ) {
        return String.join(System.lineSeparator(),
                "Weekly Funds Report",
                "-------------------",
                "weekly investment income : " + Asset.formatMoney(weeklyInvestmentIncome),
                "weekly operating cost    : " + Asset.formatMoney(weeklyOperatingCost),
                "weekly mortgage payments : " + Asset.formatMoney(weeklyMortgagePayments),
                "weekly grants            : " + Asset.formatMoney(weeklyGrants),
                "weekly mortgage net flow  : " + Asset.formatMoney(weeklyMortgageNet),
                "amount available         : " + Asset.formatMoney(amountAvailable)
        );
    }

    private void ensureAmountAvailableEstimated() {
        if (!amountAvailableEstimated) {
            throw new IllegalStateException("OC2 precondition failed: estimateFundsForWeek() must run before addMortgage().");
        }
    }

    private Investment requireInvestment(String itemNumber, String contract) {
        Investment investment = findInvestment(itemNumber);
        if (investment == null) {
            throw new IllegalArgumentException(contract + " precondition failed: investment not found " + itemNumber);
        }
        return investment;
    }

    private Mortgage requireMortgage(String accountNumber, String contract) {
        Mortgage mortgage = findMortgage(accountNumber);
        if (mortgage == null) {
            throw new IllegalArgumentException(contract + " precondition failed: mortgage not found " + accountNumber);
        }
        return mortgage;
    }

    private Investment findInvestment(String itemNumber) {
        for (Asset asset : assets) {
            if (asset instanceof Investment investment && investment.hasItemNumber(itemNumber)) {
                return investment;
            }
        }
        return null;
    }

    private Mortgage findMortgage(String accountNumber) {
        for (Asset asset : assets) {
            if (asset instanceof Mortgage mortgage && mortgage.hasAccountNumber(accountNumber)) {
                return mortgage;
            }
        }
        return null;
    }
}
