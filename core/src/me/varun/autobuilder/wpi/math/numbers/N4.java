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
 * A class representing the number 4.
*/
public final class N4 extends Num implements Nat<N4> {
  private N4() {
  }

  /**
   * The integer this class represents.
   *
   * @return The literal number 4.
  */
  @Override
  public int getNum() {
    return 4;
  }

  /**
   * The singleton instance of this class.
  */
  public static final N4 instance = new N4();
}
