/*----------------------------------------------------------------------------*/
/* Copyright (c) 2019 FIRST. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package me.varun.autobuilder.wpi.math.numbers;

import me.varun.autobuilder.wpi.math.Nat;
import me.varun.autobuilder.wpi.math.Num;

/**
 * A class representing the number 17.
*/
public final class N17 extends Num implements Nat<N17> {
  private N17() {
  }

  /**
   * The integer this class represents.
   *
   * @return The literal number 17.
  */
  @Override
  public int getNum() {
    return 17;
  }

  /**
   * The singleton instance of this class.
  */
  public static final N17 instance = new N17();
}
