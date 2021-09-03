package vendingmachine;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.time.Duration;

import com.woowahan.techcourse.utils.Randoms;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import org.assertj.core.util.Strings;
import org.mockito.MockedStatic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mockStatic;

public class ApplicationTest {
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

        @DisplayName("코인의 최소 금액보다 적은 금액 입력 테스트")
        @Test
        void inputMoneyLessThanMinCoinTest() {
            assertSimpleTest(() -> subject(
                    "5",
                    "[콜라,20,1500]",
                    "1500",
                    "콜라")
            );

            assertThat(getOutput()).contains("잔돈\n0원");
        }

        @DisplayName("상품 1개 입력 시 정상 작동 테스트")
        @Test
        void inputOneProductTest() {
            assertSimpleTest(() -> subject(
                    "5",
                    "[콜라,20,1500]",
                    "1500",
                    "콜라")
            );

            assertThat(getOutput()).contains("잔돈\n0원");
        }

        @DisplayName("상품 여러 개 입력 시 정상 작동 테스트")
        @Test
        void inputManyProductTest() {
            assertSimpleTest(() -> subject(
                    "5",
                    "[콜라,20,1500],[사이다,10,1000]",
                    "2500",
                    "콜라",
                    "사이다")
            );

            assertThat(getOutput()).contains("잔돈\n0원");
        }

        @DisplayName("반환되는 잔돈이 없을 경우 잔돈 반환 테스트")
        @Test
        void returnChangeZeroTest() {
            assertSimpleTest(() -> subject(
                    "5",
                    "[콜라,20,1500]",
                    "1500",
                    "콜라")
            );

            assertThat(getOutput()).contains("잔돈\n0원");
        }

        @DisplayName("상품 구매 시 잔돈을 반환할 수 없을 경우 그대로 잔돈 반환 테스트")
        @Test
        void returnChangesIfMachineCannotReturn() {
            assertSimpleTest(() -> {
                try (MockedStatic<Randoms> randoms = mockStatic(Randoms.class)) {
                    randoms.when(() -> Randoms.pick(anyList())).thenReturn(500);
                    subject("0",
                            "[콜라,20,600]",
                            "1000",
                            "콜라");

                    assertThat(getOutput()).contains("잔돈\n1000원");
                }
            });
        }
    }

    @DisplayName("자판기 보유 금액 입력 테스트")
    @Nested
    class HoldingMoneyTest {

        @DisplayName("정수가 아닌 문자 입력 시 예외 발생 테스트")
        @ParameterizedTest
        @ValueSource(strings = {"a", "가", "10.1"})
        void inputNotDigitTest(final String holdingMoney) {
            assertSimpleTest(
                    () -> assertThatIllegalArgumentException().isThrownBy(() -> subject(holdingMoney))
            );
        }

        @DisplayName("음수 입력 시 예외 발생 테스트")
        @ParameterizedTest
        @ValueSource(strings = {"-1", "-10", "-100"})
        void inputNegativeTest(final String holdingMoney) {
            assertSimpleTest(
                    () -> assertThatIllegalArgumentException().isThrownBy(() -> subject(holdingMoney))
            );
        }
    }

    @DisplayName("상품 정보 입력 테스트")
    @Nested
    class ProductTest {

        // 정규표현식은 파싱이 어려워서 테스트를 하지 않는 것이 좋을 것 같다.
        //        @DisplayName("상품 정보 포맷이 일치하지 않을 경우 예외 발생 테스트")
        //        @ParameterizedTest
        //        @ValueSource(strings = {"콜라,20,1500", "콜라,20", "[콜라,20,1500,20000]"})
        //        void inputNotDigitTest(final String productInfo) {
        //            assertSimpleTest(
        //                    () -> assertThatIllegalArgumentException().isThrownBy(() -> subject("1000", productInfo))
        //            );
        //        }

        @DisplayName("상품 이름이 공백일 시 예외 발생 테스트")
        @ParameterizedTest
        @ValueSource(strings = {"[,20,1500]", "[ ,20,1500]", "[  ,20,1500]"})
        void inputBlankTest(final String blank) {
            assertSimpleTest(
                    () -> assertThatIllegalArgumentException().isThrownBy(() -> subject("1000", blank))
            );
        }

        @DisplayName("수량이 정수가 아닌 문자일 경우 예외 발생 테스트")
        @ParameterizedTest
        @ValueSource(strings = {"[콜라,a,1500]", "[콜라,가,1500]", "[콜라,10.0,1500]"})
        void inputNotDigitForAmountTest(final String notDigit) {
            assertSimpleTest(
                    () -> assertThatIllegalArgumentException().isThrownBy(() -> subject("1000", notDigit))
            );
        }

        @DisplayName("수량이 음수일 경우 예외 발생 테스트")
        @ParameterizedTest
        @ValueSource(strings = {"-1", "-10", "-100"})
        void inputNegativeForAmountTest(final String holdingMoney) {
            assertSimpleTest(
                    () -> assertThatIllegalArgumentException().isThrownBy(() -> subject(holdingMoney))
            );
        }

        @DisplayName("가격이 정수가 아닌 문자 입력 시 예외 발생 테스트")
        @ParameterizedTest
        @ValueSource(strings = {"[콜라,10,a]", "[콜라,10,가]", "[콜라,10,10.0]"})
        void inputNotDigitForPriceTest(final String holdingMoney) {
            assertSimpleTest(
                    () -> assertThatIllegalArgumentException().isThrownBy(() -> subject(holdingMoney))
            );
        }

        @DisplayName("가격이 음수일 경우 예외 발생 테스트")
        @ParameterizedTest
        @ValueSource(strings = {"[콜라,10,-1]", "[콜라,10,-10]", "[콜라,10,-100]"})
        void inputNegativeForPriceTest(final String holdingMoney) {
            assertSimpleTest(
                    () -> assertThatIllegalArgumentException().isThrownBy(() -> subject(holdingMoney))
            );
        }
    }

    @DisplayName("자판기에 투입할 금액 입력 테스트")
    @Nested
    class InputMoneyTest {

        @DisplayName("정수가 아닌 문자 입력 시 예외 발생 테스트")
        @ParameterizedTest
        @ValueSource(strings = {"a", "가", "10.1"})
        void inputNotDigitTest(final String inputMoney) {
            assertSimpleTest(
                    () -> assertThatIllegalArgumentException().isThrownBy(() -> subject(
                            "1000",
                            "[콜라,20,1500]",
                            inputMoney
                    ))
            );
        }

        @DisplayName("음수 입력 시 예외 발생 테스트")
        @ParameterizedTest
        @ValueSource(strings = {"-1", "-10", "-100"})
        void inputNegativeTest(final String inputMoney) {
            assertSimpleTest(
                    () -> assertThatIllegalArgumentException().isThrownBy(() -> subject(
                            "1000",
                            "[콜라,20,1500]",
                            inputMoney
                    ))
            );
        }

        @DisplayName("투입 금액을 동전으로 반환할 수 없을 경우의 테스트")
        @Test
        void inputCannotReturnChangeTest() {
            assertSimpleTest(() -> subject(
                    "1000",
                    "[콜라,20,1500]",
                    "1")
            );

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
            assertSimpleTest(
                    () -> assertThatIllegalArgumentException().isThrownBy(() -> subject(
                            "1000",
                            "[콜라,20,1500]",
                            "1500",
                            productName
                    ))
            );
        }

        @DisplayName("구매 상품이 자판기에 존재하지 않을 경우 예외 발생 테스트")
        @Test
        void inputNotExistsProductTest() {
            assertSimpleTest(
                    () -> assertThatIllegalArgumentException().isThrownBy(() -> subject(
                            "1000",
                            "[콜라,20,1500]",
                            "1500",
                            "사이다"
                    ))
            );
        }

        @DisplayName("상품의 가격이 투입된 금액보다 비쌀 경우 예외 발생 테스트")
        @Test
        void inputExpensiveProductTest() {
            assertSimpleTest(
                    () -> assertThatIllegalArgumentException().isThrownBy(() -> subject(
                            "1000",
                            "[콜라,20,1500],[사이다,20,2000]",
                            "1500",
                            "사이다"
                    ))
            );
        }

        @DisplayName("매진 상품을 구매하려고 할 경우 예외 발생 테스트")
        @Test
        void inputSoldProductTest() {
            assertSimpleTest(
                    () -> assertThatIllegalArgumentException().isThrownBy(() -> subject(
                            "1000",
                            "[콜라,0,1500]",
                            "1500",
                            "콜라"
                    ))
            );
        }
    }

    private String getOutput() {
        return captor.toString().trim();
    }
}
