package com.omnifix.demo;

import static com.omnifix.demo.BowlingConstants.MULTIPLYER_IDENTITY;
import static com.omnifix.demo.BowlingConstants.RESET_PREV_VALUE;

import java.util.function.Function;
import reactor.util.function.Tuple6;

public interface BowlingAlgorithmHelper {
  /** Operations */
  default Function<Integer, Integer> moveToNextRollIndex(
      Integer noOfPins, Tuple6<Integer, Integer, Integer, Integer, Integer, Integer> gameState) {
    return index -> isStrikeNonLastFrame(noOfPins, gameState) ? index + 2 : index + 1;
  }

  default Function<Integer, Integer> storeOrResetPrevRollValue(
      Integer noOfPins, Tuple6<Integer, Integer, Integer, Integer, Integer, Integer> gameState) {
    return ignore -> isFirstRollAndNotStrike(noOfPins, gameState) ? noOfPins : RESET_PREV_VALUE;
  }

  default Function<Integer, Integer> updateNextNextMultiplierIfStrike(
      Integer noOfPins, Tuple6<Integer, Integer, Integer, Integer, Integer, Integer> gameState) {
    return multiplier -> isStrikeNonLastFrame(noOfPins, gameState) ? multiplier + 1 : multiplier;
  }

  default Function<Integer, Integer> updateNextMultiplierIfStrikeOrSpare(
      Integer noOfPins, Tuple6<Integer, Integer, Integer, Integer, Integer, Integer> gameState) {
    return multiplier ->
        isStrikeOrSpareNonLastFrame(noOfPins, gameState) ? multiplier + 1 : multiplier;
  }

  default Function<Integer, Integer> updateScore(
      Integer noOfPins, Tuple6<Integer, Integer, Integer, Integer, Integer, Integer> gameState) {
    return score -> score + gameState.getT3() * noOfPins;
  }

  /** Left shift of multiplier registry T3 <- T3, T3 <- T4, T5 <- MULTIPLYER_IDENTITY */
  default Function<Integer, Integer> shiftNextNextMultiplierToNext(
      Tuple6<Integer, Integer, Integer, Integer, Integer, Integer> gameState) {
    return ignore -> isNotLastBonusRoll(gameState) ? gameState.getT5() : MULTIPLYER_IDENTITY;
  }

  default Function<Integer, Integer> shiftNextMultiplierToCurrent(
      Tuple6<Integer, Integer, Integer, Integer, Integer, Integer> gameState) {
    return ignore -> isNotLastBonusRoll(gameState) ? gameState.getT4() : MULTIPLYER_IDENTITY;
  }

  /** Predicates */
  default boolean isNotLastBonusRoll(
      Tuple6<Integer, Integer, Integer, Integer, Integer, Integer> gameState) {
    return gameState.getT2() <= 20; // Last bonus roll is 21
  }

  private boolean isFirstRollAndNotStrike(
      Integer noOfPins, Tuple6<Integer, Integer, Integer, Integer, Integer, Integer> gameState) {
    return (gameState.getT2() % 2 == 1) && (noOfPins != 10); // First roll is odd
  }

  private boolean isStrikeOrSpareNonLastFrame(
      Integer noOfPins, Tuple6<Integer, Integer, Integer, Integer, Integer, Integer> gameState) {
    return isStrikeNonLastFrame(noOfPins, gameState) || isSpareNonLastFrame(noOfPins, gameState);
  }

  private boolean isSpareNonLastFrame(
      Integer noOfPins, Tuple6<Integer, Integer, Integer, Integer, Integer, Integer> gameState) {
    return (gameState.getT2() % 2 == 0) // Spare can only happen at even rolls
        && (noOfPins + gameState.getT6() == 10) // Check cumulative score from bth rolls
        && (gameState.getT2() < 20); // Last spare is at roll 20
  }

  private boolean isStrikeNonLastFrame(
      Integer noOfPins, Tuple6<Integer, Integer, Integer, Integer, Integer, Integer> gameState) {
    return (gameState.getT2() % 2 == 1)
        && (noOfPins == 10)
        && (gameState.getT2()
            < 19); // Strike can only happen from odd roll, last frame starts at roll 19
  }
}
