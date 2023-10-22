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
 * A class representing the number 20.
*/
public final class N20 extends Num implements Nat<N20> {
  private N20() {
  }

  /**
   * The integer this class represents.
   *
   * @return The literal number 20.
  */
  @Override
  public int getNum() {
    return 20;
  }

  /**
   * The singleton instance of this class.
  */
  public static final N20 instance = new N20();
}
