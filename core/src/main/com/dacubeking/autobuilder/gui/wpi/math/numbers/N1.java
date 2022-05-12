/*----------------------------------------------------------------------------*/
/* Copyright (c) 2019 FIRST. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package com.dacubeking.autobuilder.gui.wpi.math.numbers;

import com.dacubeking.autobuilder.gui.wpi.math.Nat;
import com.dacubeking.autobuilder.gui.wpi.math.Num;

/**
 * A class representing the number 1.
*/
public final class N1 extends Num implements Nat<N1> {
  private N1() {
  }

  /**
   * The integer this class represents.
   *
   * @return The literal number 1.
  */
  @Override
  public int getNum() {
    return 1;
  }

  /**
   * The singleton instance of this class.
  */
  public static final N1 instance = new N1();
}
