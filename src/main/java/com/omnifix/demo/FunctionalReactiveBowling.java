package com.omnifix.demo;

import static com.omnifix.demo.BowlingConstants.MULTIPLYER_IDENTITY;
import static com.omnifix.demo.BowlingConstants.RESET_PREV_VALUE;
import static com.omnifix.demo.BowlingConstants.STARTING_ROLL_INDEX;
import static com.omnifix.demo.BowlingConstants.STARTING_SCORE;

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
public class FunctionalReactiveBowling implements BowlingAlgorithmHelper {

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
                                                noOfPins,
                                                gameState)) // T4 holds the next multiplier
                                        .mapT5(
                                            updateNextNextMultiplierIfStrike(
                                                noOfPins,
                                                gameState)) // T5 holds the next next multiplier
                                        .mapT6(
                                            storeOrResetPrevRollValue(
                                                noOfPins,
                                                gameState)) // T6 holds the previous roll value
                                        .mapT2(moveToNextRollIndex(noOfPins, gameState)))
                            .map(
                                gameState ->
                                    gameState
                                        .mapT3(shiftNextMultiplierToCurrent(gameState))
                                        .mapT4(shiftNextNextMultiplierToNext(gameState))
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
}
