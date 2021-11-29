package com.omnifix.demo;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Flux;

class FunctionalReactiveTenPinBwlingTests {

  private ReactiveBowling reactiveBowling;
  private FunctionalReactiveBowling standaloneReactiveBowling;

  @BeforeAll
  static void setUpBeforeClass() throws Exception {}

  @AfterAll
  static void tearDownAfterClass() throws Exception {}

  @BeforeEach
  void setUp() throws Exception {
    reactiveBowling = new ReactiveBowling();
    standaloneReactiveBowling = new FunctionalReactiveBowling();
  }

  @AfterEach
  void tearDown() throws Exception {}

  @ParameterizedTest(name = "{0}")
  @MethodSource("provideParameters")
  void multipleScenariosTests(String scenarioName, List<Integer> pins, Integer score) {

    Flux<Integer> pinsStream = Flux.fromIterable(pins);

    assertThat(reactiveBowling.play(pinsStream).blockLast())
        .isEqualTo(score); // Expected score sequence
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("provideParameters")
  void standalneScenariosTests(String scenarioName, List<Integer> pins, Integer score) {

    Flux<Integer> pinsStream = Flux.fromIterable(pins);

    assertThat(standaloneReactiveBowling.play(pinsStream).blockLast().getT1())
        .isEqualTo(score); // Expected score sequence
  }

  private static Stream<Arguments> provideParametersX() {
    return Stream.of(
        //        Arguments.of(
        //            "Alternate STRIKES start with frame",
        //            List.of(1, 2, 10, 1, 2, 10, 1, 2, 10, 1, 2, 10, 1, 2, 10, 1, 2),
        //            80)
        Arguments.of(
            "Max score - ALL STRIKES",
            List.of(10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10),
            300));
  }

  private static Stream<Arguments> provideParameters() {
    return Stream.of(
        Arguments.of("One frame", List.of(5, 3), 8),
        Arguments.of("Three STRIKES followed by a frame", List.of(10, 10, 10, 2, 3), 72),
        Arguments.of(
            "9 STRIKES followed by a frame",
            List.of(10, 10, 10, 10, 10, 10, 10, 10, 10, 2, 6),
            258),
        Arguments.of(
            "Max score - ALL STRIKES",
            List.of(10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10),
            300), // All STRIKEs
        Arguments.of(
            "Altenate STRIKES start with STRIKE",
            List.of(10, 1, 2, 10, 1, 2, 10, 1, 2, 10, 1, 2, 10, 1, 2),
            80), // Alternate STRIKEs
        Arguments.of(
            "Alternate STRIKES start with frame",
            List.of(1, 2, 10, 1, 2, 10, 1, 2, 10, 1, 2, 10, 1, 2, 10, 1, 2),
            80), // Alternate STRIKEs, start without a STRIKE
        Arguments.of(
            "All STRIKEs, finish with SPARE from single roll",
            List.of(10, 10, 10, 10, 10, 10, 10, 10, 10, 0, 10, 7),
            267),
        Arguments.of(
            "All STRIKEs, finish with SPARE from two rolls",
            List.of(10, 10, 10, 10, 10, 10, 10, 10, 10, 2, 8, 4),
            266),
        Arguments.of("SPARE from two rolls", List.of(4, 6, 7), 24),
        Arguments.of("SPARE from second roll only", List.of(0, 10, 5), 20),
        Arguments.of("STRIKE followed by a frame", List.of(10, 3, 2), 20),
        Arguments.of(
            "1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 10, 4, 6, 10, 3, 7",
            List.of(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 10, 4, 6, 10, 3, 7),
            74),
        Arguments.of(
            "10, 10, 4, 6, 3, 7, 10, 3, 4, 8, 2, 10, 10, 7, 3, 10",
            List.of(10, 10, 4, 6, 3, 7, 10, 3, 4, 8, 2, 10, 10, 7, 3, 10),
            188));
  }
}
