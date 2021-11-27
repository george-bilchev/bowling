package com.omnifix.demo;

public interface Bowling {

  // called each time the player rolls a ball. The argument is the number of pins
  // knocked down
  void roll(int noOfPins);

  int score(); // returns the total score of the game
}
