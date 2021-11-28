package com.omnifix.demo;

import lombok.extern.slf4j.Slf4j;
import net.jcip.annotations.NotThreadSafe;

@Slf4j
@NotThreadSafe
public abstract class GameStateHelper implements Bowling {

  /*
   * Bowling Interface contract
   */

  public int score() {
    return getScore();
  }

  public void roll(int noOfPins) {

    String currentStateStr = GameStateMachine.getStateStr(this);

    String newState = GameStateMachine.calculateState(this).nextState(noOfPins, this);

    logState(noOfPins, currentStateStr, newState);
  }

  /**
   * Helpers
   *
   * @param noOfPins
   * @param prevState
   * @param newState
   */
  private void logState(int noOfPins, String prevState, String newState) {
    String bonusStr = "";
    if (isSpareBonus()) {
      bonusStr = "SPARE";
    } else if (isStrike()) {
      bonusStr = "STRIKE";
    }

    log.info(
        "{}",
        String.format(
            "%-15s ->    %-15s   ROLL: %2d ==> SCORE: %3d   %-10s",
            prevState, newState, noOfPins, getScore(), bonusStr));
  }

  // Used for logging "STRIKE"
  public boolean isStrike() {
    return getStrikeBonus() == 2; // STRIKE has a bonus of two rolls
  }

  /*
   * Boilerplate to access model
   */

  public abstract int getScore();

  protected abstract void setScore(int score);

  public abstract int getNextFrame();

  protected abstract void setNextFrame(int frameIndex);

  public abstract int getNextRoll();

  protected abstract void setNextRoll(int rollIndex);

  public abstract boolean isSpareBonus();

  protected abstract void setSpareBonus(boolean spareBonus);

  public abstract int getPrevRollValue();

  protected abstract void setPrevRollValue(int value);

  public abstract int getStrikeBonus();

  protected abstract void setStrikeBonus(int value);

  public abstract boolean isDoubleStrike();

  protected abstract void setDoubleStrike(boolean value);

  public abstract String toString();
}
