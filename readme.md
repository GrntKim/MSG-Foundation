# MSG 재단 파일럿 시스템

MSG 재단 파일럿 시스템은 매주 신규 주택 구입에 사용할 수 있는 자금을 계산하는 콘솔 기반 Java 프로그램입니다.  
보고서의 Class Diagram, Sequence Diagram, Operation Contract를 기준으로 구현했으며, DB, 웹, GUI, 외부 라이브러리는 사용하지 않습니다.

## 작성자
단국대학교 소프트웨어학과 32211058 김준명

## 사용 언어 및 도구
- JAVA
- IntelliJ
- Codex 5.5


## 실행 환경

- Java 17 이상 필요
- 표준 라이브러리만 사용
- 운영체제는 macOS, Linux, Windows 모두 가능
- 별도 빌드 도구 Gradle/Maven 불필요

Java 설치 여부는 다음 명령으로 확인할 수 있습니다.

```bash
java -version
javac -version
```

## 빠른 실행

이 README가 있는 프로젝트 루트, 즉 `src` 폴더가 보이는 위치에서 다음 명령을 실행합니다.

macOS 또는 Linux:

```bash
javac -encoding UTF-8 -d out src/*.java
java -cp out Main
```

Windows PowerShell:

```powershell
javac -encoding UTF-8 -d out src\*.java
java -cp out Main
```

상위 저장소를 그대로 clone해서 `untitled` 폴더가 보이는 위치에 있다면 먼저 프로젝트 폴더로 이동합니다.

```bash
cd untitled
```

컴파일 결과는 `out` 디렉터리에 생성됩니다. 이 디렉터리는 실행 산출물이므로 저장소에 포함하지 않습니다.

## 프로젝트 구조

```text
untitled/
├── .gitignore
├── readme.md
└── src/
    ├── Asset.java
    ├── Investment.java
    ├── Mortgage.java
    ├── OperatingExpense.java
    ├── MSGFoundation.java
    └── Main.java
```

## 구현 범위

구현한 범위:

- 투자 데이터 등록, 갱신, 삭제
- 운영 비용 갱신
- 모기지 등록, 갱신
- 주간 자금 계산
- 투자 목록과 모기지 목록 출력
- `addMortgage`의 자금 부족 실패 분기 검증

구현하지 않은 범위:

- 모기지 승인 자격 심사
- 부부 신청 접수
- 실제 투자 행위
- 사용자 인증, 데이터베이스, 네트워크, GUI

## 클래스 책임

| 클래스 | 책임 |
| --- | --- |
| `Asset` | `Investment`와 `Mortgage`의 공통 추상 클래스입니다. 마지막 갱신일과 `weeklyAmount()` 계약을 제공합니다. |
| `Investment` | 연간 예상 수익을 보관하고, 주간 투자 소득을 `annualReturn / 52`로 계산합니다. |
| `Mortgage` | 모기지별 P&I, 소득, 세금, 보험료를 보관합니다. 에스크로, 교부금, 주간 순유입을 계산합니다. |
| `OperatingExpense` | 연간 운영 비용을 보관하고 주간 운영 비용을 `annualExpenses / 52`로 계산합니다. 자산이 아니므로 `Asset`을 상속하지 않습니다. |
| `MSGFoundation` | 시스템 컨트롤러입니다. Operation Contract OC1~OC9에 해당하는 public 메서드를 제공합니다. |
| `Main` | 재단 직원 역할의 콘솔 드라이버입니다. Arrange, Act, Assert 흐름으로 주요 계약을 시연합니다. |

## Operation Contract 추적성

| OC | 메서드 | 구현 내용 |
| --- | --- | --- |
| OC1 | `estimateFundsForWeek()` | 투자 주간 소득, 운영 비용, 모기지 상환액, 교부금, 사용 가능 금액을 계산하고 결산서를 반환합니다. |
| OC2 | `addMortgage(...)` | 자금이 충분하면 모기지를 생성하고 `amountAvailable`에서 가격을 차감합니다. 부족하면 상태 변경 없이 경고를 반환합니다. |
| OC3 | `updateMortgage(...)` | `weeklyIncome`, `realEstateTax`, `insurancePremium` 중 하나를 갱신하고 해당 갱신일을 기록합니다. |
| OC4 | `addInvestment(...)` | 중복 없는 투자 항목을 생성하고 자산 컬렉션에 연결합니다. |
| OC5 | `updateInvestmentReturn(...)` | 투자 연간 예상 수익과 갱신일을 변경합니다. |
| OC6 | `deleteInvestment(...)` | 투자 항목을 자산 컬렉션에서 제거합니다. |
| OC7 | `updateOperatingExpenses(...)` | 연간 운영 비용과 갱신일을 변경합니다. |
| OC8 | `produceInvestmentListing()` | 등록된 투자 목록을 문자열 보고서로 생성합니다. |
| OC9 | `produceMortgageListing()` | 등록된 모기지 목록을 문자열 보고서로 생성합니다. |

## 계산 공식

투자:

```text
weeklyAmount = annualReturn / 52
```

운영 비용:

```text
weeklyAmount = annualExpenses / 52
```

모기지:

```text
weeklyEscrow = (realEstateTax + insurancePremium) / 52
weeklyGrant = max(0, (weeklyPandI + weeklyEscrow) - weeklyIncome * 0.28)
weeklyAmount = weeklyPandI + weeklyEscrow - weeklyGrant
```

재단의 주간 사용 가능 금액:

```text
amountAvailable = 주간 투자 소득 - 주간 운영 비용 + 주간 모기지 순유입
```

## Main 시연 흐름

`Main`은 다음 순서로 실행됩니다.

1. 투자 3개와 운영 비용을 등록합니다.
2. 모기지 등록 전 OC1 계산을 수행해 `amountAvailable`을 준비합니다.
3. 모기지 2개를 성공 등록하고, 가격 차감 결과를 확인합니다.
4. 모기지를 포함해 다시 주간 자금을 계산합니다.
5. 일부러 큰 가격의 모기지를 등록 시도해 OC2 실패 분기를 검증합니다.
6. 실패 시 `amountAvailable`과 모기지 개수가 바뀌지 않았음을 출력합니다.
7. 기존 모기지의 `weeklyIncome`을 갱신한 뒤 교부금 변화가 `amountAvailable`에 반영되는지 확인합니다.
8. 투자 목록과 모기지 목록을 출력합니다.

## 주요 검증 수치

실행 중 다음 검증이 `[PASS]`로 출력되어야 합니다.

| 검증 항목 | 기대값 |
| --- | ---: |
| 초기 `amountAvailable` | `$590,000.00` |
| 모기지 2개 등록 후 `amountAvailable` | `$65,000.00` |
| 모기지 포함 주간 계산 후 `amountAvailable` | `$590,672.00` |
| 자금 부족 등록 실패 후 `amountAvailable` | `$590,672.00` 유지 |
| 소득 갱신 후 `amountAvailable` | `$590,920.00` |

검증 실패 시 프로그램은 `[FAIL]` 메시지와 함께 예외를 발생시킵니다. Java의 `assert` 키워드에 의존하지 않고 직접 검증하므로 `-ea` 옵션 없이도 항상 확인됩니다.

## 예시 출력 일부

```text
[OC2 실패 분기 Act-Assert: 자금 부족 모기지 등록 거절]
자금 부족 경고: M-9999 등록 거절. 필요 금액 $2,000,000.00, 사용 가능 금액 $590,672.00
실패 전 amountAvailable = $590,672.00
실패 후 amountAvailable = $590,672.00
실패 전/후 mortgageCount = 2 / 2
[PASS] 실패 시 amountAvailable 불변: $590,672.00
[PASS] 실패 시 모기지 컬렉션 불변: 2
```

## 설계 메모

- `MSGFoundation`은 `Asset` 컬렉션에 `Investment`와 `Mortgage`를 함께 보관합니다.
- 주간 자산 기여액 계산은 각 객체의 `weeklyAmount()`에 위임합니다.
- `OperatingExpense`는 유출 항목이므로 `Asset` 계층에 넣지 않고 별도 클래스로 유지합니다.
- 중복 등록, 존재하지 않는 항목 갱신, 잘못된 모기지 갱신 필드는 예외로 처리합니다.
- 금액 출력은 통화 형식과 소수점 둘째 자리로 통일했습니다.
