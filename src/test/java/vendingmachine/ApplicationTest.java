package vendingmachine;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.time.Duration;
import java.util.InputMismatchException;
import java.util.NoSuchElementException;

import com.woowahan.techcourse.utils.Randoms;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import org.assertj.core.api.ThrowableAssert;
import org.assertj.core.util.Strings;
import org.mockito.MockedStatic;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mockStatic;

class ApplicationTest {

    private static final Duration SIMPLE_TEST_TIMEOUT = Duration.ofSeconds(10000L);

    private PrintStream standardOut;
    private OutputStream captor;

    @BeforeEach
    void setUp() {
        standardOut = System.out;
        captor = new ByteArrayOutputStream();
        System.setOut(new PrintStream(captor));
    }

    @AfterEach
    void tearDown() {
        System.setOut(standardOut);
        System.out.println(captor.toString());
    }

    private void subject(final String... args) {
        command(args);
        Application.main(new String[]{});
    }

    private void command(final String... args) {
        final byte[] buf = Strings.join(args).with("\n").getBytes();
        System.setIn(new ByteArrayInputStream(buf));
    }

    private void assertSimpleTest(final Executable executable) {
        assertTimeoutPreemptively(SIMPLE_TEST_TIMEOUT, executable);
    }

    @DisplayName("전체 기능 동작 테스트")
    @Nested
    class AcceptanceTest {

        @DisplayName("상품 1개 정상 구매 후 잔돈 0원 반환 테스트")
        @Test
        void inputOneProductTest() {

            // given
            final String[] input = {"0",
                    "[콜라,20,1500]",
                    "1500",
                    "콜라"};

            // when
            Executable executable = () -> subject(input);

            // then
            assertSimpleTest(executable);
            assertThat(getOutput()).contains("남은 금액: 0원");
        }

        @DisplayName("상품 여러 개 정상 구매 후 잔돈 0원 반환 테스트")
        @Test
        void inputManyProductTest() {

            // given
            final String[] input = {"0",
                    "[콜라,20,1500];[사이다,10,1000]",
                    "2500",
                    "콜라",
                    "사이다"};

            // when
            final Executable executable = () -> subject(input);

            // then
            assertSimpleTest(executable);
            assertThat(getOutput()).contains("남은 금액: 0원");
        }

        @DisplayName("자판기가 보유중인 동전 1개 반환 테스트")
        @Test
        void returnTenTest() {

            // given
            final String[] input = {"10",
                    "[콜라,20,1500];[사이다,10,1000]",
                    "1510",
                    "콜라"};

            // when
            final Executable executable = () -> subject(input);

            // then
            assertSimpleTest(executable);
            assertThat(getOutput()).contains("10원 - 1개");
        }

        @DisplayName("자판기가 보유중인 여러 동전들 중 잔돈 1개 반환 테스트")
        @Test
        void returnTenTest2() {

            // given
            final String[] input = {"100",
                    "[콜라,20,1500];[사이다,10,1000]",
                    "1510",
                    "콜라"};

            // then
            final Executable executable = () -> {
                try (MockedStatic<Randoms> randoms = mockStatic(Randoms.class)) {
                    randoms.when(() -> Randoms.pick(anyList())).thenReturn(10);
                    subject(input);
                }
            };

            // when
            assertSimpleTest(executable);
            assertThat(getOutput()).contains("10원 - 1개");
        }

        @DisplayName("잔돈 여러 개 반환 테스트")
        @Test
        void returnChangesTest() {

            // given
            final String[] input = {"660",
                    "[콜라,20,500]",
                    "610",
                    "콜라"};

            // then
            final Executable executable = () -> {
                try (MockedStatic<Randoms> randoms = mockStatic(Randoms.class)) {
                    randoms.when(() -> Randoms.pick(anyList())).thenReturn(500, 100, 50, 10);
                    subject(input);
                }
            };

            // when
            assertSimpleTest(executable);
            assertThat(getOutput()).contains("100원 - 1개");
            assertThat(getOutput()).contains("10원 - 1개");
        }

        @DisplayName("남은 금액이 자판기 보유 금액보다 클 경우 남은 금액 반환 테스트")
        @Test
        void walletAmountIsGreaterThanCoins() {

            // given
            final String[] input = {"0",
                    "[콜라,1,1000]",
                    "1500",
                    "콜라"};

            // when
            final Executable executable = () -> {
                try (MockedStatic<Randoms> randoms = mockStatic(Randoms.class)) {
                    randoms.when(() -> Randoms.pick(anyList())).thenReturn(500);
                    subject(input);
                }
            };

            // then
            assertSimpleTest(executable);
            assertThat(getOutput()).contains("남은 금액\n500원");
        }

        @DisplayName("상품 구매 시 잔돈을 반환할 수 없을 경우 그대로 잔돈 반환 테스트")
        @Test
        void returnChangesIfMachineCannotReturn() {

            // given
            final String[] given = {"0",
                    "[콜라,20,600]",
                    "1000",
                    "콜라"};

            // when
            final Executable executable = () -> {
                try (MockedStatic<Randoms> randoms = mockStatic(Randoms.class)) {
                    randoms.when(() -> Randoms.pick(anyList())).thenReturn(500);
                    subject(given);
                }
            };

            // then
            assertSimpleTest(executable);
            assertThat(getOutput()).contains("잔돈\n400원");
        }
    }

    @DisplayName("자판기 보유 금액 입력 테스트")
    @Nested
    class HoldingMoneyTest {

        @DisplayName("자판기 보유 금액이 코인의 최소 금액보다 작을 경우 테스트")
        @Test
        void inputMoneyLessThanMinCoinTest() {

            // given
            String input = "5";

            // when
            final ThrowableAssert.ThrowingCallable callable = () -> subject(input);

            // then
            assertSimpleTest(() -> assertThatIllegalArgumentException().isThrownBy(callable));
        }

        @DisplayName("정수가 아닌 문자 입력 시 예외 발생 테스트")
        @ParameterizedTest
        @ValueSource(strings = {"a", "가", "10.1"})
        void inputNotDigitTest(final String holdingMoney) {

            // when
            final ThrowableAssert.ThrowingCallable callable = () -> subject(holdingMoney);

            // then
            assertSimpleTest(() -> assertThatThrownBy(callable).isExactlyInstanceOf(InputMismatchException.class));
        }

        @DisplayName("음수 입력 시 예외 발생 테스트")
        @ParameterizedTest
        @ValueSource(strings = {"-1", "-10", "-100"})
        void inputNegativeTest(final String holdingMoney) {

            // when
            final ThrowableAssert.ThrowingCallable callable = () -> subject(holdingMoney);

            // then
            assertSimpleTest(() -> assertThatIllegalArgumentException().isThrownBy(callable));
        }
    }

    @DisplayName("상품 정보 입력 테스트")
    @Nested
    class ProductTest {

        private final String holdingMoney = "1000";

        @DisplayName("상품 이름이 공백일 시 예외 발생 테스트")
        @ParameterizedTest
        @ValueSource(strings = {"[,20,1500]", "[ ,20,1500]", "[  ,20,1500]"})
        void inputBlankTest(final String product) {

            // when
            final ThrowableAssert.ThrowingCallable callable = () -> subject(holdingMoney, product);

            // then
            assertSimpleTest(() -> assertThatIllegalArgumentException().isThrownBy(callable)
                                                                       .withMessageContaining("형식이 잘못되었습니다."));
        }

        @DisplayName("수량이 정수가 아닌 문자일 경우 예외 발생 테스트")
        @ParameterizedTest
        @ValueSource(strings = {"[콜라,a,1500]", "[콜라,가,1500]", "[콜라,10.0,1500]"})
        void inputNotDigitForAmountTest(final String product) {

            // when
            final ThrowableAssert.ThrowingCallable callable = () -> subject(holdingMoney, product);

            // then
            assertSimpleTest(() -> assertThatIllegalArgumentException().isThrownBy(callable)
                                                                       .withMessageContaining("형식이 잘못되었습니다."));
        }

        @DisplayName("수량이 음수일 경우 예외 발생 테스트")
        @ParameterizedTest
        @ValueSource(strings = {"[콜라,-1,1500]", "[사이다,-10,1500]"})
        void inputNegativeForAmountTest(final String product) {

            // when
            final ThrowableAssert.ThrowingCallable callable = () -> subject(holdingMoney, product);

            // then
            assertSimpleTest(() -> assertThatIllegalArgumentException().isThrownBy(callable)
                                                                       .withMessageContaining("형식이 잘못되었습니다."));
        }

        @DisplayName("가격이 정수가 아닌 문자 입력 시 예외 발생 테스트")
        @ParameterizedTest
        @ValueSource(strings = {"[콜라,10,a]", "[콜라,10,가]", "[콜라,10,10.0]"})
        void inputNotDigitForPriceTest(final String product) {

            // when
            final ThrowableAssert.ThrowingCallable callable = () -> subject(holdingMoney, product);

            // then
            assertSimpleTest(() -> assertThatIllegalArgumentException().isThrownBy(callable)
                                                                       .withMessageContaining("형식이 잘못되었습니다."));
        }

        @DisplayName("가격이 음수일 경우 예외 발생 테스트")
        @ParameterizedTest
        @ValueSource(strings = {"[콜라,10,-1]", "[콜라,10,-10]", "[콜라,10,-100]"})
        void inputNegativeForPriceTest(final String product) {

            // when
            final ThrowableAssert.ThrowingCallable callable = () -> subject(holdingMoney, product);

            // then
            assertSimpleTest(() -> assertThatIllegalArgumentException().isThrownBy(callable)
                                                                       .withMessageContaining("형식이 잘못되었습니다."));
        }
    }

    @DisplayName("자판기에 투입할 금액 입력 테스트")
    @Nested
    class InputMoneyTest {

        private final String holdingMoney = "1000";
        private final String product = "[콜라,20,1500]";

        @DisplayName("정수가 아닌 문자 입력 시 예외 발생 테스트")
        @ParameterizedTest
        @ValueSource(strings = {"a", "가", "10.1"})
        void inputNotDigitTest(final String inputMoney) {

            // given
            final String[] input = {holdingMoney, product, inputMoney};

            // when
            ThrowableAssert.ThrowingCallable callable = () -> subject(input);

            // then
            assertSimpleTest(() -> assertThatThrownBy(callable).isExactlyInstanceOf(InputMismatchException.class));
        }

        @DisplayName("음수 입력 시 예외 발생 테스트")
        @ParameterizedTest
        @ValueSource(strings = {"-1", "-10", "-100"})
        void inputNegativeTest(final String inputMoney) {

            // given
            final String[] input = {holdingMoney, product, inputMoney};

            // when
            ThrowableAssert.ThrowingCallable callable = () -> subject(input);

            // then
            assertSimpleTest(() -> assertThatIllegalArgumentException().isThrownBy(callable)
                                                                       .withMessageContaining("금액은 0원 미만일 수 없습니다."));
        }

        @DisplayName("투입 금액을 동전으로 반환할 수 없을 경우의 테스트")
        @Test
        void inputCannotReturnChangeTest() {

            // given
            final String[] input = {holdingMoney, product, "1"};

            // when
            Executable executable = () -> subject(input);

            // then
            assertSimpleTest(executable);
            assertThat(captor.toString()).contains("잔돈\n1원");
        }
    }

    @DisplayName("상품 구매 테스트")
    @Nested
    class BuyProductTest {

        @DisplayName("상품 이름이 공백일 경우 예외 발생 테스트")
        @ParameterizedTest
        @ValueSource(strings = {" ", "  "})
        void inputBlankTest(final String productName) {

            // given
            final String[] input = {"1000",
                    "[콜라,20,1500]",
                    "1500",
                    productName};

            // when
            final ThrowableAssert.ThrowingCallable callable = () -> subject(input);

            // then
            assertSimpleTest(() -> assertThatThrownBy(callable).isInstanceOf(NoSuchElementException.class));
        }

        @DisplayName("구매 상품이 자판기에 존재하지 않을 경우 예외 발생 테스트")
        @Test
        void inputNotExistsProductTest() {

            // given
            final String[] input = {"1000",
                    "[콜라,20,1500]",
                    "1500",
                    "사이다"};

            // when
            final ThrowableAssert.ThrowingCallable callable = () -> subject(input);

            // then
            assertSimpleTest(() -> assertThatIllegalArgumentException().isThrownBy(callable).withMessage("존재하지 않는 상품입니다. name:사이다"));
        }

        @DisplayName("상품의 가격이 투입된 금액보다 비쌀 경우 예외 발생 테스트")
        @Test
        void inputExpensiveProductTest() {

            // given
            final String[] input = {"1000",
                    "[콜라,20,1500];[사이다,20,2000]",
                    "1500",
                    "사이다"};

            // when
            final ThrowableAssert.ThrowingCallable callable = () -> subject(input);

            // then
            assertSimpleTest(() -> assertThatIllegalArgumentException().isThrownBy(callable)
                                                                       .withMessage("금액은 0원 미만일 수 없습니다. amount:2000"));
        }

        @DisplayName("매진 상품을 구매하려고 할 경우 예외 발생 테스트")
        @Test
        void inputSoldProductTest() {

            // given
            final String[] input = {"1000",
                    "[콜라,0,1500]",
                    "1500"};

            // when
            final ThrowableAssert.ThrowingCallable callable = () -> subject(input);


            // then
            assertSimpleTest(() -> assertThatIllegalArgumentException().isThrownBy(callable));
        }
    }

    private String getOutput() {
        return captor.toString().trim();
    }
}
