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
 * A class representing the number 12.
*/
public final class N12 extends Num implements Nat<N12> {
  private N12() {
  }

  /**
   * The integer this class represents.
   *
   * @return The literal number 12.
  */
  @Override
  public int getNum() {
    return 12;
  }

  /**
   * The singleton instance of this class.
  */
  public static final N12 instance = new N12();
}
