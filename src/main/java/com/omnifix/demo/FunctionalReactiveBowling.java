package com.omnifix.demo;

import java.util.function.Function;
import reactor.core.publisher.Flux;
import reactor.util.function.Tuple6;
import reactor.util.function.Tuples;

/**
 * Functional Reactive Implementation of the Bowling Scoring Algorithm
 *
 * @author George Bilchev
 */

// State is modelled by a Registry of 6 tuples
//
// T1: Current Score
// T2: Current roll index (there are a maximum of 21 and during strike some can be skipped)
// T3: Current bonus multiplier (user to multiply the current NoOfPins)
// T4: The next multiplier
// T5: The next next multiplier, e.g., the multiplier after the next
// T6: Previous noOfPins value
//
public class FunctionalReactiveBowling {

  private static final int STARTING_SCORE = 0;
  private static final int STARTING_ROLL_INDEX = 1;
  private static final int MULTIPLYER_IDENTITY = 1;
  private static final int RESET_PREV_VALUE = 0;

  public Flux<Tuple6<Integer, Integer, Integer, Integer, Integer, Integer>> play(
      Flux<Integer> pinsStream) {
    MutableContext mutableContext = new MutableContext();
    return pinsStream
        .flatMap(
            noOfPins ->
                Flux.deferContextual(
                    ctx ->
                        Flux.just(
                                ctx
                                    .<Tuple6<Integer, Integer, Integer, Integer, Integer, Integer>>
                                        get(Tuple6.class))
                            .map(
                                gameState ->
                                    gameState
                                        .mapT1(
                                            updateScore(noOfPins, gameState)) // T1 holds the score
                                        .mapT4(
                                            updateNextMultiplierIfStrikeOrSpare(
                                                noOfPins, gameState)) // T4 hold the next multiplier
                                        .mapT5(
                                            updateNextNextMultiplierIfStrike(
                                                noOfPins,
                                                gameState)) // T5 holds the next next multiplier
                                        .mapT6(
                                            storePrevRollValue(
                                                noOfPins,
                                                gameState)) // T6 hold the previous roll value
                                        .mapT2(moveToNextRollIndex(noOfPins, gameState)))
                            .map(
                                gameState ->
                                    gameState
                                        .mapT3(shiftNextMultiplierToCurrent(gameState))
                                        .mapT4(shiftNextNextMultiplierToNextMultiplier(gameState))
                                        .mapT5(ignore -> MULTIPLYER_IDENTITY))
                            .map(
                                state -> {
                                  ((MutableContext) ctx).put(Tuple6.class, state);
                                  return state;
                                })))
        .contextWrite(
            ctx ->
                mutableContext.put(
                    Tuple6.class,
                    Tuples.of(
                        STARTING_SCORE,
                        STARTING_ROLL_INDEX,
                        MULTIPLYER_IDENTITY,
                        MULTIPLYER_IDENTITY,
                        MULTIPLYER_IDENTITY,
                        RESET_PREV_VALUE)));
  }

  private Function<Integer, Integer> shiftNextNextMultiplierToNextMultiplier(
      Tuple6<Integer, Integer, Integer, Integer, Integer, Integer> gameState) {
    return ignore -> shiftNextNextMultiplierToNext(gameState);
  }

  private int shiftNextNextMultiplierToNext(
      Tuple6<Integer, Integer, Integer, Integer, Integer, Integer> gameState) {
    return isNotLastBonusRoll(gameState) ? gameState.getT5() : MULTIPLYER_IDENTITY;
  }

  private Function<Integer, Integer> shiftNextMultiplierToCurrent(
      Tuple6<Integer, Integer, Integer, Integer, Integer, Integer> gameState) {
    return ignore -> isNotLastBonusRoll(gameState) ? gameState.getT4() : MULTIPLYER_IDENTITY;
  }

  private Function<Integer, Integer> moveToNextRollIndex(
      Integer noOfPins, Tuple6<Integer, Integer, Integer, Integer, Integer, Integer> gameState) {
    return index -> isStrikeNonLastFrame(noOfPins, gameState) ? index + 2 : index + 1;
  }

  private Function<Integer, Integer> storePrevRollValue(
      Integer noOfPins, Tuple6<Integer, Integer, Integer, Integer, Integer, Integer> gameState) {
    return ignore -> isFirstRollAndNotStrike(noOfPins, gameState) ? noOfPins : RESET_PREV_VALUE;
  }

  private Function<Integer, Integer> updateNextNextMultiplierIfStrike(
      Integer noOfPins, Tuple6<Integer, Integer, Integer, Integer, Integer, Integer> gameState) {
    return multiplier -> isStrikeNonLastFrame(noOfPins, gameState) ? multiplier + 1 : multiplier;
  }

  private Function<Integer, Integer> updateNextMultiplierIfStrikeOrSpare(
      Integer noOfPins, Tuple6<Integer, Integer, Integer, Integer, Integer, Integer> gameState) {
    return multiplier ->
        isStrikeOrSpareNonLastFrame(noOfPins, gameState) ? multiplier + 1 : multiplier;
  }

  private Function<Integer, Integer> updateScore(
      Integer noOfPins, Tuple6<Integer, Integer, Integer, Integer, Integer, Integer> gameState) {
    return score -> score + gameState.getT3() * noOfPins;
  }

  private boolean isNotLastBonusRoll(
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
