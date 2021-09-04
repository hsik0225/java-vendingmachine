package vendingmachine;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Field;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;

class ScannerTest {

    @ParameterizedTest
    @MethodSource
    @DisplayName("Scanner#hasNext() 가 Scanners#scannerIsClosed() 와 항상 정반대의 값을 반환하는지 테스트")
    void scannerTest(String input, Consumer<Scanner> scannerMethod) {

        // given
        final byte[] buf = input.getBytes();
        System.setIn(new ByteArrayInputStream(buf));
        Scanner scanner = new Scanner(System.in);

        // when
        try {
            scannerMethod.accept(scanner);
        } catch (NoSuchElementException e) {

        }

        // then
        assertThat(scanner.hasNext()).isNotEqualTo(scannerIsClosed(scanner));
    }

    private static boolean scannerIsClosed(Scanner scanner) {
        try {
            Field sourceClosedField = Scanner.class.getDeclaredField("sourceClosed");
            sourceClosedField.setAccessible(true);
            return sourceClosedField.getBoolean(scanner);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            System.out.println("리플렉션 중 에러 발생");
        }
        return true;
    }

    private static Stream<Arguments> scannerTest() {
        Consumer<Scanner> oneCallNext = Scanner::next;
        Consumer<Scanner> twoCallNext = scanner -> {
            scanner.next();
            scanner.next();
        };

        Consumer<Scanner> oneCallNextLine = Scanner::nextLine;
        Consumer<Scanner> twoCallNextLine = scanner -> {
            scanner.nextLine();
            scanner.nextLine();
        };

        return Stream.of(
                Arguments.of("", oneCallNext),
                Arguments.of(" ", oneCallNext),
                Arguments.of("apple", oneCallNext),
                Arguments.of("apple ", oneCallNext),
                Arguments.of("apple\n", oneCallNext),
                Arguments.of("apple\nbanana", oneCallNext),

                Arguments.of("apple\n", twoCallNext),
                Arguments.of("apple\nbanana", twoCallNext),

                Arguments.of("", oneCallNextLine),
                Arguments.of(" ", oneCallNextLine),
                Arguments.of("apple", oneCallNextLine),
                Arguments.of("apple\n", oneCallNextLine),
                Arguments.of("apple\nbanana", oneCallNextLine),

                Arguments.of("apple\n", twoCallNextLine),
                Arguments.of("apple\nbanana", twoCallNextLine)
        );
    }
}
