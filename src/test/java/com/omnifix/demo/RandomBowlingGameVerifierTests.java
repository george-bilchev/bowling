package com.omnifix.demo;

import static org.assertj.core.api.Assertions.assertThat;

import com.omnifix.demo.verifier.BowlingGame;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

@Slf4j
class RandomBowlingGameVerifierTests {

  @BeforeAll
  static void setUpBeforeClass() throws Exception {}

  @AfterAll
  static void tearDownAfterClass() throws Exception {}

  @BeforeEach
  void setUp() throws Exception {}

  @AfterEach
  void tearDown() throws Exception {}

  @Test
  void randomGeneratedVerifiedTests() {
    for (int i = 1; i < 1000; i++) {
      generateOneBowlingInstanceAndVerify();
      log.info("Verifying {}", i);
    }
  }

  public void generateOneBowlingInstanceAndVerify() {
    BowlingGame verifier = new BowlingGame();
    Random rand = new Random();
    int frameMax = 10;

    List<Integer> rolls = new ArrayList<>();
    int score = -1;

    for (int i = 0; i < 100; i++) {

      int roll = rand.nextInt((frameMax - 1) + 1) + 1;
      frameMax = frameMax - roll;

      if (roll == 10) {
        frameMax = 10;
        i = i + 1;
        continue;
      }

      if (i % 2 == 1) {
        frameMax = 10;
      }

      verifier.roll(roll);

      if (verifier.isFinished()) {
        rolls.add(roll);
        score = verifier.getScore();
        break;
      } else {
        rolls.add(roll);
      }
    }
    log.info("Generated: {} with score: {}", rolls, score);

    FunctionalReactiveBowling standaloneReactiveBowling = new FunctionalReactiveBowling();

    Flux<Integer> pinsStream = Flux.fromIterable(rolls);

    assertThat(standaloneReactiveBowling.play(pinsStream).blockLast().getT1())
        .isEqualTo(score); // Expected score sequence
  }
}
