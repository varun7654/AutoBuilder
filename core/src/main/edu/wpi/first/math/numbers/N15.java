/*----------------------------------------------------------------------------*/
/* Copyright (c) 2019 FIRST. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package edu.wpi.first.math.numbers;

import edu.wpi.first.math.Nat;
import edu.wpi.first.math.Num;

/**
 * A class representing the number 15.
*/
public final class N15 extends Num implements Nat<N15> {
  private N15() {
  }

  /**
   * The integer this class represents.
   *
   * @return The literal number 15.
  */
  @Override
  public int getNum() {
    return 15;
  }

  /**
   * The singleton instance of this class.
  */
  public static final N15 instance = new N15();
}
