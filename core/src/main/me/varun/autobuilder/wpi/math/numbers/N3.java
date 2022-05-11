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
 * A class representing the number 3.
*/
public final class N3 extends Num implements Nat<N3> {
  private N3() {
  }

  /**
   * The integer this class represents.
   *
   * @return The literal number 3.
  */
  @Override
  public int getNum() {
    return 3;
  }

  /**
   * The singleton instance of this class.
  */
  public static final N3 instance = new N3();
}
