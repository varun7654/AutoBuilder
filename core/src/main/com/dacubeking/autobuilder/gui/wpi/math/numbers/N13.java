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
 * A class representing the number 13.
*/
public final class N13 extends Num implements Nat<N13> {
  private N13() {
  }

  /**
   * The integer this class represents.
   *
   * @return The literal number 13.
  */
  @Override
  public int getNum() {
    return 13;
  }

  /**
   * The singleton instance of this class.
  */
  public static final N13 instance = new N13();
}
