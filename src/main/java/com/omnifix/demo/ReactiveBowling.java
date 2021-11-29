package com.omnifix.demo;

import reactor.core.publisher.Flux;

public class ReactiveBowling {

  /**
   * Given a stream of noOfPins
   *
   * @param pinsStream
   * @return A stream of current score (no "X" and "/" allowed)
   */
  public Flux<Integer> play(Flux<Integer> pinsStream) {
    return pinsStream
        .flatMap(
            noOfPins ->
                Flux.deferContextual(
                    ctx ->
                        Flux.just(ctx.get(GameState.class))
                            .doOnNext(
                                gameState ->
                                    GameStateMachine.calculateState(gameState)
                                        .nextState(noOfPins, gameState))
                            .map(GameState::getScore)))
        .contextWrite(ctx -> ctx.put(GameState.class, GameState.builder().build()));
  }
}
