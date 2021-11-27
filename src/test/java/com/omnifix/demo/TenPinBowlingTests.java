package com.omnifix.demo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

@Slf4j
class TenPinBowlingTests {

  private static final int THIRD_FRAME = 3;
  private static final int SECOND_ROLL = 2;
  private static final int FIRST_ROLL = 1;
  private static final int SECOND_FRAME = 2;
  private static final int ROLL_TWO_VALUE = 5;
  private static final int ROLL_ONE_VALUE = 2;
  private static final int FOURTH_FRAME = 4;

  @BeforeAll
  static void setUpBeforeClass() throws Exception {}

  @AfterAll
  static void tearDownAfterClass() throws Exception {}

  @BeforeEach
  void setUp(TestInfo testInfo) {
    log.info(
        String.format(
            "===========================     %40s    ==============================",
            testInfo.getDisplayName()));
  }

  @AfterEach
  void tearDown() throws Exception {}

  @Test
  void catRecordOneRound() {
    GameState game = GameState.builder().build();

    int firstRoll = 5;
    int secondRoll = 3;

    game.roll(firstRoll);
    game.roll(secondRoll);

    Assertions.assertThat(game.score()).isEqualTo(firstRoll + secondRoll);

    assertStateAdvance(game, SECOND_FRAME, FIRST_ROLL);
    assertThat(GameStateMachine.calculateState(game)).isEqualTo(GameStateMachine.FIRST_GO);
  }

  @Test
  void canRecordOneRoundWithSpareFromTwoRolls() {
    GameState game = GameState.builder().build();

    int firstRoll = 3;
    int secondRoll = 7;

    game.roll(firstRoll);
    game.roll(secondRoll);

    Assertions.assertThat(game.score()).isEqualTo(firstRoll + secondRoll);

    assertStateAdvance(game, SECOND_FRAME, FIRST_ROLL);
    assertThat(GameStateMachine.calculateState(game)).isEqualTo(GameStateMachine.FIRST_GO);

    int thirdRoll = 5;

    game.roll(thirdRoll);

    Assertions.assertThat(game.score()).isEqualTo(firstRoll + secondRoll + thirdRoll + thirdRoll);

    assertStateAdvance(game, SECOND_FRAME, SECOND_ROLL);
    assertThat(GameStateMachine.calculateState(game)).isEqualTo(GameStateMachine.SECOND_GO);
  }

  @Test
  void canRecordOneRoundWithSpareFromOnlySecondRoll() {
    GameState game = GameState.builder().build();

    int firstRoll = 0;
    int secondRoll = 10;

    game.roll(firstRoll);
    game.roll(secondRoll);

    Assertions.assertThat(game.score()).isEqualTo(firstRoll + secondRoll);

    assertStateAdvance(game, SECOND_FRAME, FIRST_ROLL);
    assertThat(GameStateMachine.calculateState(game)).isEqualTo(GameStateMachine.FIRST_GO);

    int thirdRoll = 5;

    game.roll(thirdRoll);

    Assertions.assertThat(game.score()).isEqualTo(firstRoll + secondRoll + thirdRoll + thirdRoll);

    assertStateAdvance(game, SECOND_FRAME, SECOND_ROLL);
    assertThat(GameStateMachine.calculateState(game)).isEqualTo(GameStateMachine.SECOND_GO);
  }

  @Test
  void canRecordOneRoundWithStrike() {
    GameState game = GameState.builder().build();

    int firstRoll = 10;

    game.roll(firstRoll);

    Assertions.assertThat(game.score()).isEqualTo(firstRoll);

    assertStateAdvance(game, SECOND_FRAME, FIRST_ROLL);
    assertThat(GameStateMachine.calculateState(game)).isEqualTo(GameStateMachine.FIRST_GO);

    // Round 2, roll 1, to double
    int thirdRoll = 3;

    game.roll(thirdRoll);

    Assertions.assertThat(game.score()).isEqualTo(firstRoll + thirdRoll + thirdRoll);

    assertStateAdvance(game, SECOND_FRAME, SECOND_ROLL);
    assertThat(GameStateMachine.calculateState(game)).isEqualTo(GameStateMachine.SECOND_GO);

    // Round 2, roll 2, to double
    int fourthRoll = 2;

    game.roll(fourthRoll);

    Assertions.assertThat(game.score())
        .isEqualTo(firstRoll + thirdRoll + thirdRoll + fourthRoll + fourthRoll);

    assertStateAdvance(game, THIRD_FRAME, FIRST_ROLL);
    assertThat(GameStateMachine.calculateState(game)).isEqualTo(GameStateMachine.FIRST_GO);

    // Normal scoring rules apply

    int fifthRoll = 5;

    game.roll(fifthRoll);

    Assertions.assertThat(game.score())
        .isEqualTo(firstRoll + thirdRoll + thirdRoll + fourthRoll + fourthRoll + fifthRoll);
  }

  @Test
  void canRecordSpareOnRoundTenFomTwoRolls() {
    GameState game = GameState.builder().build();

    int score = recordRounds(game, 9, ROLL_ONE_VALUE, ROLL_TWO_VALUE);
    assertThat(GameStateMachine.calculateState(game)).isEqualTo(GameStateMachine.FIRST_GO);

    Assertions.assertThat(game.score()).isEqualTo(9 * (ROLL_ONE_VALUE + ROLL_TWO_VALUE));
    Assertions.assertThat(game.getNextFrame()).isEqualTo(10); // Advanced to next frame
    Assertions.assertThat(game.getNextRoll()).isEqualTo(1); // Reset roll

    int lastRoundRollOne = 2;
    int lastRoundRollTwo = 8;
    int lastRoundRollThree = 7;

    game.roll(lastRoundRollOne);
    assertThat(GameStateMachine.calculateState(game)).isEqualTo(GameStateMachine.SECOND_GO);
    game.roll(lastRoundRollTwo);
    assertThat(GameStateMachine.calculateState(game)).isEqualTo(GameStateMachine.BONUS_GO);
    game.roll(lastRoundRollThree);
    assertThat(GameStateMachine.calculateState(game)).isEqualTo(GameStateMachine.END_OF_GAME);

    Assertions.assertThat(game.score())
        .isEqualTo(
            score + lastRoundRollOne + lastRoundRollTwo + lastRoundRollThree + lastRoundRollThree);
  }

  @Test
  void canRecordSpareOnRoundTenFromOneRoll() {
    GameState game = GameState.builder().build();

    int score = recordRounds(game, 9, ROLL_ONE_VALUE, ROLL_TWO_VALUE);
    assertThat(GameStateMachine.calculateState(game)).isEqualTo(GameStateMachine.FIRST_GO);

    Assertions.assertThat(game.score()).isEqualTo(9 * (ROLL_ONE_VALUE + ROLL_TWO_VALUE));
    Assertions.assertThat(game.getNextFrame()).isEqualTo(10); // Advanced to next frame
    Assertions.assertThat(game.getNextRoll()).isEqualTo(1); // Reset roll

    int lastRoundRollOne = 0;
    int lastRoundRollTwo = 10;
    int lastRoundRollThree = 7;

    game.roll(lastRoundRollOne);
    assertThat(GameStateMachine.calculateState(game)).isEqualTo(GameStateMachine.SECOND_GO);
    game.roll(lastRoundRollTwo);
    assertThat(GameStateMachine.calculateState(game)).isEqualTo(GameStateMachine.BONUS_GO);
    game.roll(lastRoundRollThree);
    assertThat(GameStateMachine.calculateState(game)).isEqualTo(GameStateMachine.END_OF_GAME);

    Assertions.assertThat(game.score())
        .isEqualTo(
            score + lastRoundRollOne + lastRoundRollTwo + lastRoundRollThree + lastRoundRollThree);

    // assertEndOfGame(game);
    Assertions.assertThat(GameStateMachine.calculateState(game))
        .isEqualTo(GameStateMachine.END_OF_GAME);
  }

  @Test
  void canRecordStrikeOnRoundTen() {
    GameState game = GameState.builder().build();

    int score = recordRounds(game, 9, ROLL_ONE_VALUE, ROLL_TWO_VALUE);
    assertThat(GameStateMachine.calculateState(game)).isEqualTo(GameStateMachine.FIRST_GO);

    Assertions.assertThat(score).isEqualTo(9 * (ROLL_ONE_VALUE + ROLL_TWO_VALUE));
    Assertions.assertThat(game.getNextFrame()).isEqualTo(10); // Advanced to next frame
    Assertions.assertThat(game.getNextRoll()).isEqualTo(1); // Reset roll

    int lastRoundRollOne = 10;
    int lastRoundRollTwo = 8;
    int lastRoundRollThree = 7;

    game.roll(lastRoundRollOne);
    assertThat(GameStateMachine.calculateState(game)).isEqualTo(GameStateMachine.BONUS_GO);
    game.roll(lastRoundRollTwo);
    assertThat(GameStateMachine.calculateState(game)).isEqualTo(GameStateMachine.BONUS_GO);
    game.roll(lastRoundRollThree);
    assertThat(GameStateMachine.calculateState(game)).isEqualTo(GameStateMachine.END_OF_GAME);

    Assertions.assertThat(game.score())
        .isEqualTo(
            score
                + lastRoundRollOne
                + lastRoundRollTwo
                + lastRoundRollTwo
                + lastRoundRollThree
                + lastRoundRollThree);

    // assertEndOfGame(game);
    Assertions.assertThat(GameStateMachine.calculateState(game))
        .isEqualTo(GameStateMachine.END_OF_GAME);
  }

  @Test
  void willThrowExcpeionOnTryingToRecordPostEndOfGame() {
    GameState game = GameState.builder().build();

    assertThatThrownBy(
            () -> {
              recordRounds(game, BowlingConstants.LAST_FRAME + 1, ROLL_ONE_VALUE, ROLL_TWO_VALUE);
            })
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("End of game!");
  }

  @Test
  void canRecordOneRoundWithStrikeFollowedByStrike() {
    GameState game = GameState.builder().build();

    int firstRoll = 10;

    game.roll(firstRoll);

    Assertions.assertThat(game.score()).isEqualTo(firstRoll);

    assertStateAdvance(game, SECOND_FRAME, FIRST_ROLL);
    assertThat(GameStateMachine.calculateState(game)).isEqualTo(GameStateMachine.FIRST_GO);

    // Round 2, roll 1, to double
    int thirdRoll = 10;

    game.roll(thirdRoll);

    Assertions.assertThat(game.score()).isEqualTo(firstRoll + thirdRoll + thirdRoll);

    assertStateAdvance(game, THIRD_FRAME, FIRST_ROLL);
    assertThat(GameStateMachine.calculateState(game)).isEqualTo(GameStateMachine.FIRST_GO);

    // Round 3, roll 1, to double
    int fifthRoll = 2;

    game.roll(fifthRoll);

    Assertions.assertThat(game.score())
        .isEqualTo(firstRoll + thirdRoll + thirdRoll + fifthRoll + fifthRoll);

    assertStateAdvance(game, THIRD_FRAME, SECOND_ROLL);
    assertThat(GameStateMachine.calculateState(game)).isEqualTo(GameStateMachine.SECOND_GO);
  }

  @Test
  void canRecordOneRoundWithStrikeFollowedBySpare() {
    GameState game = GameState.builder().build();

    int firstRoll = 10;

    game.roll(firstRoll);

    Assertions.assertThat(game.score()).isEqualTo(firstRoll);

    assertStateAdvance(game, SECOND_FRAME, FIRST_ROLL);
    assertThat(GameStateMachine.calculateState(game)).isEqualTo(GameStateMachine.FIRST_GO);

    // Round 2, roll 1, to double
    int thirdRoll = 2;

    game.roll(thirdRoll);
    Assertions.assertThat(game.score()).isEqualTo(firstRoll + thirdRoll + thirdRoll);
    assertThat(GameStateMachine.calculateState(game)).isEqualTo(GameStateMachine.SECOND_GO);

    int fourthRoll = 8;

    game.roll(fourthRoll);
    Assertions.assertThat(game.score())
        .isEqualTo(firstRoll + thirdRoll + thirdRoll + fourthRoll + fourthRoll);

    assertStateAdvance(game, THIRD_FRAME, FIRST_ROLL);
    assertThat(GameStateMachine.calculateState(game)).isEqualTo(GameStateMachine.FIRST_GO);

    // Round 3, roll 1, to double
    int fifthRoll = 2;

    game.roll(fifthRoll);

    Assertions.assertThat(game.score())
        .isEqualTo(
            firstRoll + thirdRoll + thirdRoll + fourthRoll + fourthRoll + fifthRoll + fifthRoll);

    assertStateAdvance(game, THIRD_FRAME, SECOND_ROLL);
    assertThat(GameStateMachine.calculateState(game)).isEqualTo(GameStateMachine.SECOND_GO);

    int sixthRoll = 6;

    game.roll(sixthRoll);

    Assertions.assertThat(game.score())
        .isEqualTo(
            firstRoll
                + thirdRoll
                + thirdRoll
                + fourthRoll
                + fourthRoll
                + fifthRoll
                + fifthRoll
                + sixthRoll);

    assertStateAdvance(game, FOURTH_FRAME, FIRST_ROLL);
    assertThat(GameStateMachine.calculateState(game)).isEqualTo(GameStateMachine.FIRST_GO);
  }

  @Test
  void canRecordOneRoundWithSpareFromOnlySecondRollFollowedBySpare() {
    GameState game = GameState.builder().build();

    int firstRoll = 0;
    int secondRoll = 10;

    game.roll(firstRoll);
    game.roll(secondRoll);

    Assertions.assertThat(game.score()).isEqualTo(firstRoll + secondRoll);

    assertStateAdvance(game, SECOND_FRAME, FIRST_ROLL);
    assertThat(GameStateMachine.calculateState(game)).isEqualTo(GameStateMachine.FIRST_GO);

    int thirdRoll = 5;

    game.roll(thirdRoll);

    Assertions.assertThat(game.score()).isEqualTo(firstRoll + secondRoll + thirdRoll + thirdRoll);

    assertStateAdvance(game, SECOND_FRAME, SECOND_ROLL);
    assertThat(GameStateMachine.calculateState(game)).isEqualTo(GameStateMachine.SECOND_GO);

    int fourthRoll = 5;

    game.roll(fourthRoll);

    Assertions.assertThat(game.score())
        .isEqualTo(firstRoll + secondRoll + thirdRoll + thirdRoll + fourthRoll);

    assertStateAdvance(game, THIRD_FRAME, FIRST_ROLL);
    assertThat(GameStateMachine.calculateState(game)).isEqualTo(GameStateMachine.FIRST_GO);

    int fifthRoll = 7;

    game.roll(fifthRoll);

    Assertions.assertThat(game.score())
        .isEqualTo(
            firstRoll + secondRoll + thirdRoll + thirdRoll + fourthRoll + fifthRoll + fifthRoll);

    assertStateAdvance(game, THIRD_FRAME, SECOND_ROLL);
    assertThat(GameStateMachine.calculateState(game)).isEqualTo(GameStateMachine.SECOND_GO);
  }

  public int recordRounds(GameState game, int numOfRounds, int rollOne, int rollTwo) {
    for (int i = 0; i < numOfRounds; i++) {
      // One normal round
      game.roll(rollOne);
      game.roll(rollTwo);
    }

    return game.score();
  }

  public void assertStateAdvance(GameState game, int frameIndex, int rollIndex) {
    Assertions.assertThat(game.getNextFrame()).isEqualTo(frameIndex); // Advanced to next frame
    Assertions.assertThat(game.getNextRoll()).isEqualTo(rollIndex); // Reset roll
  }

  public void assertEndOfGame(GameState game) {
    assertThatThrownBy(
            () -> {
              game.roll(0); // Try to record anything after game is supposed to have finished
            })
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Reached end of game");
  }
}
