import java.time.LocalDate;

public class Main {
    private static final double OPERATING_EXPENSES = 520_000.00;
    private static final double INVESTMENT_A_RETURN = 15_600_000.00;
    private static final double INVESTMENT_B_RETURN = 10_400_000.00;
    private static final double INVESTMENT_C_RETURN = 5_200_000.00;
    private static final double FIRST_MORTGAGE_PRICE = 250_000.00;
    private static final double SECOND_MORTGAGE_PRICE = 275_000.00;
    private static final double LARGE_MORTGAGE_PRICE = 2_000_000.00;
    private static final double CENT_TOLERANCE = 0.01;

    public static void main(String[] args) {
        MSGFoundation foundation = new MSGFoundation();

        printTitle("MSG Foundation Pilot System");

        printContract("OC4/OC7 Arrange-Act-Assert: 데이터 준비");
        System.out.println(foundation.addInvestment("I-1001", "Municipal Bond Fund", INVESTMENT_A_RETURN));
        System.out.println(foundation.addInvestment("I-1002", "Dividend Index Fund", INVESTMENT_B_RETURN));
        System.out.println(foundation.addInvestment("I-1003", "Treasury Ladder", INVESTMENT_C_RETURN));
        System.out.println(foundation.updateOperatingExpenses(OPERATING_EXPENSES));
        assertEquals("투자 3개가 등록되었다", 3, foundation.investmentCount());

        System.out.println();
        printContract("OC1 Arrange: 모기지 성공 등록을 위한 직전 주간 자금 계산");
        System.out.println(foundation.estimateFundsForWeek());
        assertClose("초기 amountAvailable 계산", 590_000.00, foundation.getAmountAvailable());

        System.out.println();
        printContract("OC2 Arrange-Act-Assert: 신규 모기지 2개 성공 등록");
        System.out.println(foundation.addMortgage(
                "M-1001",
                "Kim Couple",
                FIRST_MORTGAGE_PRICE,
                LocalDate.of(2026, 1, 9),
                375.00,
                900.00,
                5_200.00,
                1_300.00
        ));
        System.out.println("현재 amountAvailable = " + Asset.formatMoney(foundation.getAmountAvailable()));
        System.out.println(foundation.addMortgage(
                "M-1002",
                "Lee Couple",
                SECOND_MORTGAGE_PRICE,
                LocalDate.of(2026, 2, 13),
                420.00,
                1_500.00,
                6_240.00,
                1_560.00
        ));
        System.out.println("현재 amountAvailable = " + Asset.formatMoney(foundation.getAmountAvailable()));
        assertEquals("모기지 2개가 등록되었다", 2, foundation.mortgageCount());
        assertClose("모기지 가격 차감 후 amountAvailable", 65_000.00, foundation.getAmountAvailable());

        System.out.println();
        printContract("OC1 Act-Assert: 주간 자금 계산");
        System.out.println(foundation.estimateFundsForWeek());
        assertClose("모기지 포함 amountAvailable", 590_672.00, foundation.getAmountAvailable());

        System.out.println();
        printContract("OC2 실패 분기 Act-Assert: 자금 부족 모기지 등록 거절");
        double beforeFailedAmount = foundation.getAmountAvailable();
        int beforeFailedMortgageCount = foundation.mortgageCount();
        String failedRegistration = foundation.addMortgage(
                "M-9999",
                "Park Couple",
                LARGE_MORTGAGE_PRICE,
                LocalDate.of(2026, 3, 20),
                900.00,
                1_600.00,
                7_800.00,
                1_950.00
        );
        System.out.println(failedRegistration);
        System.out.println("실패 전 amountAvailable = " + Asset.formatMoney(beforeFailedAmount));
        System.out.println("실패 후 amountAvailable = " + Asset.formatMoney(foundation.getAmountAvailable()));
        System.out.println("실패 전/후 mortgageCount = " + beforeFailedMortgageCount + " / " + foundation.mortgageCount());
        assertClose("실패 시 amountAvailable 불변", beforeFailedAmount, foundation.getAmountAvailable());
        assertEquals("실패 시 모기지 컬렉션 불변", beforeFailedMortgageCount, foundation.mortgageCount());

        System.out.println();
        printContract("OC3 + OC1 Act-Assert: 소득 갱신 후 교부금 변화 반영");
        System.out.println(foundation.updateMortgage("M-1001", Mortgage.FIELD_WEEKLY_INCOME, 2_000.00));
        System.out.println(foundation.estimateFundsForWeek());
        assertClose("소득 갱신 후 amountAvailable 증가", 590_920.00, foundation.getAmountAvailable());

        System.out.println();
        printContract("OC8/OC9 Act-Assert: 보고서 목록 생성");
        System.out.println(foundation.produceInvestmentListing());
        System.out.println();
        System.out.println(foundation.produceMortgageListing());
    }

    private static void printTitle(String title) {
        System.out.println("==================================================");
        System.out.println(title);
        System.out.println("==================================================");
    }

    private static void printContract(String label) {
        System.out.println("[" + label + "]");
    }

    private static void assertClose(String label, double expected, double actual) {
        if (Math.abs(expected - actual) > CENT_TOLERANCE) {
            throw new IllegalStateException(
                    "[FAIL] " + label + ": expected " + Asset.formatMoney(expected)
                            + ", actual " + Asset.formatMoney(actual)
            );
        }
        System.out.println("[PASS] " + label + ": " + Asset.formatMoney(actual));
    }

    private static void assertEquals(String label, int expected, int actual) {
        if (expected != actual) {
            throw new IllegalStateException(
                    "[FAIL] " + label + ": expected " + expected + ", actual " + actual
            );
        }
        System.out.println("[PASS] " + label + ": " + actual);
    }
}
