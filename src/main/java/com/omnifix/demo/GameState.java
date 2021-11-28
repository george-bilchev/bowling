package com.omnifix.demo;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(callSuper = false)
public class GameState extends GameStateHelper {
  @Setter(AccessLevel.PROTECTED)
  @Builder.Default
  private int score = 0;

  @Setter(AccessLevel.PROTECTED)
  @Builder.Default
  private int nextFrame = 1;

  @Setter(AccessLevel.PROTECTED)
  @Builder.Default
  private int nextRoll = 1;

  @Setter(AccessLevel.PROTECTED)
  @Builder.Default
  private boolean spareBonus = false;

  @Setter(AccessLevel.PROTECTED)
  @Builder.Default
  private int strikeBonus = 0;

  @Setter(AccessLevel.PROTECTED)
  @Builder.Default
  private int prevRollValue = 0;

  @Setter(AccessLevel.PROTECTED)
  @Builder.Default
  private boolean doubleStrike = false;
}
