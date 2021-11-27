package com.omnifix.demo;

import static com.omnifix.demo.BowlingConstants.ALL_PINS;
import static com.omnifix.demo.BowlingConstants.STRIKE_BONUS_ROLLS_TWO;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public enum GameStateMachine {
  FIRST_GO {
    @Override
    public String nextState(int noOfPins, GameStateHelper stateProxy) {
      recordScoreAndBonus(noOfPins, stateProxy);
      if (noOfPins == ALL_PINS) {
        log.debug("Detected a STRIKE: {}", this);
        stateProxy.setStrikeBonus(STRIKE_BONUS_ROLLS_TWO);
        advanceToNextFrame(noOfPins, stateProxy);
      } else {
        stateProxy.setNextRoll(stateProxy.getNextRoll() + 1);
        stateProxy.setPrevRollValue(noOfPins);
      }
      return getStateStr(stateProxy);
    }
  },
  SECOND_GO {
    @Override
    public String nextState(int noOfPins, GameStateHelper stateProxy) {
      recordScoreAndBonus(noOfPins, stateProxy);
      if (noOfPins == BowlingConstants.ALL_PINS) { // SPARE from second go only
        stateProxy.setSpareBonus(true);
        advanceToNextFrame(noOfPins, stateProxy);
      } else {
        if (stateProxy.getPrevRollValue() + noOfPins
            == BowlingConstants.ALL_PINS) { // SPARE from two rolls
          log.debug("Detected a SPARE from two rolls: {}", this);
          stateProxy.setSpareBonus(true);
        }
        // Reset next roll and advance frame
        advanceToNextFrame(noOfPins, stateProxy);
      }
      return getStateStr(stateProxy);
    }
  },
  BONUS_GO {
    @Override
    public String nextState(int noOfPins, GameStateHelper stateProxy) {
      recordScoreAndBonus(noOfPins, stateProxy);
      if (isStrikeBonus(stateProxy)) {
        stateProxy.setNextRoll(stateProxy.getNextRoll() + 1);
        stateProxy.setPrevRollValue(noOfPins);
      }
      return getStateStr(stateProxy);
    }
  },
  END_OF_GAME {
    @Override
    public String nextState(int noOfPins, GameStateHelper stateProxy) {
      throw new IllegalStateException("End of game! " + stateProxy);
    }
  };

  public abstract String nextState(int noOfPins, GameStateHelper stateProxy);

  /**
   * State is implicitly modelled by GameState so this function just calculates the state and maps
   * to this ENUM
   *
   * @param stateProxy
   * @return
   */
  public static GameStateMachine calculateState(GameStateHelper stateProxy) {
    if (stateProxy.getNextFrame() > BowlingConstants.LAST_FRAME + 1) {
      throw new IllegalStateException("Game has finished! " + stateProxy);
    } else if (stateProxy.getNextFrame()
        == BowlingConstants.LAST_FRAME + 1) { // +1 models the bonus frame
      if (stateProxy.isSpareBonus() || isStrikeBonus(stateProxy)) {
        return BONUS_GO;
      } else {
        return END_OF_GAME;
      }
    } else { // Non-last round
      if (stateProxy.getNextRoll() == BowlingConstants.ROLL_ONE) {
        return FIRST_GO;
      } else if (stateProxy.getNextRoll() == BowlingConstants.ROLL_TWO) {
        return SECOND_GO;
      } else {
        throw new IllegalStateException("Unupported roll number! " + stateProxy);
      }
    }
  }

  public static boolean isStrikeBonus(GameStateHelper stateProxy) {
    return stateProxy.getStrikeBonus() > 0;
  }

  /**
   * String representation of this state for logging
   *
   * @param stateProxy
   * @return
   */
  public static String getStateStr(GameStateHelper stateProxy) {
    GameStateMachine state = calculateState(stateProxy);
    if (GameStateMachine.BONUS_GO.equals(state)) {
      return state + "[" + stateProxy.getNextRoll() + "]"; // Bonus index shows the roll
    } else {
      return state + "[" + stateProxy.getNextFrame() + "]"; // Round index shows the frame
    }
  }

  private static void advanceToNextFrame(int currentRoll, GameStateHelper stateProxy) {
    stateProxy.setNextRoll(BowlingConstants.ROLL_ONE);
    stateProxy.setNextFrame(stateProxy.getNextFrame() + 1);
    stateProxy.setPrevRollValue(currentRoll);
  }

  /*
   *  Implementation details for score recording
   */

  private static void recordScoreAndBonus(int noOfPins, GameStateHelper stateProxy) {
    stateProxy.setScore(stateProxy.getScore() + noOfPins);

    // Apply Bonus from previous round
    if (stateProxy.isSpareBonus()) {
      stateProxy.setScore(stateProxy.getScore() + noOfPins); // Apply SPARE bonus
      stateProxy.setSpareBonus(false); // Clear SPARE bonus
    }

    if (isStrikeBonus(stateProxy)) {
      stateProxy.setScore(stateProxy.getScore() + noOfPins); // Apply STRIKE bonus
      stateProxy.setStrikeBonus(stateProxy.getStrikeBonus() - 1); // Decrement STRIKE bonus count
    }
  }
}
