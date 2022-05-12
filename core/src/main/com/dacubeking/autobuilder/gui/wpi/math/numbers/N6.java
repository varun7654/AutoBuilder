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
 * A class representing the number 6.
*/
public final class N6 extends Num implements Nat<N6> {
  private N6() {
  }

  /**
   * The integer this class represents.
   *
   * @return The literal number 6.
  */
  @Override
  public int getNum() {
    return 6;
  }

  /**
   * The singleton instance of this class.
  */
  public static final N6 instance = new N6();
}
