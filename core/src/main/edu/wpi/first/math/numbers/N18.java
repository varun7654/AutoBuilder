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
 * A class representing the number 18.
*/
public final class N18 extends Num implements Nat<N18> {
  private N18() {
  }

  /**
   * The integer this class represents.
   *
   * @return The literal number 18.
  */
  @Override
  public int getNum() {
    return 18;
  }

  /**
   * The singleton instance of this class.
  */
  public static final N18 instance = new N18();
}
