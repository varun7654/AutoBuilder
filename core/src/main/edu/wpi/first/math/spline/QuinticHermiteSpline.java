// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package edu.wpi.first.math.spline;

import org.ejml.simple.SimpleMatrix;

public class QuinticHermiteSpline extends Spline {
  private static SimpleMatrix hermiteBasis;
  private final SimpleMatrix m_coefficients;

  /**
   * Constructs a quintic hermite spline with the specified control vectors. Each control vector
   * contains into about the location of the point, its first derivative, and its second derivative.
   *
   * @param xInitialControlVector The control vector for the initial point in the x dimension.
   * @param xFinalControlVector The control vector for the final point in the x dimension.
   * @param yInitialControlVector The control vector for the initial point in the y dimension.
   * @param yFinalControlVector The control vector for the final point in the y dimension.
   */
  @SuppressWarnings("ParameterName")
  public QuinticHermiteSpline(
      double[] xInitialControlVector,
      double[] xFinalControlVector,
      double[] yInitialControlVector,
      double[] yFinalControlVector) {
    super(5);

    // Populate the coefficients for the actual spline equations.
    // Row 0 is x coefficients
    // Row 1 is y coefficients
    final var hermite = makeHermiteBasis();
    final var x = getControlVectorFromArrays(xInitialControlVector, xFinalControlVector);
    final var y = getControlVectorFromArrays(yInitialControlVector, yFinalControlVector);

    final var xCoeffs = (hermite.mult(x)).transpose();
    final var yCoeffs = (hermite.mult(y)).transpose();

    m_coefficients = new SimpleMatrix(6, 6);

    for (int i = 0; i < 6; i++) {
      m_coefficients.set(0, i, xCoeffs.get(0, i));
      m_coefficients.set(1, i, yCoeffs.get(0, i));
    }
    for (int i = 0; i < 6; i++) {
      // Populate Row 2 and Row 3 with the derivatives of the equations above.
      // Here, we are multiplying by (5 - i) to manually take the derivative. The
      // power of the term in index 0 is 5, index 1 is 4 and so on. To find the
      // coefficient of the derivative, we can use the power rule and multiply
      // the existing coefficient by its power.
      m_coefficients.set(2, i, m_coefficients.get(0, i) * (5 - i));
      m_coefficients.set(3, i, m_coefficients.get(1, i) * (5 - i));
    }
    for (int i = 0; i < 5; i++) {
      // Then populate row 4 and 5 with the second derivatives.
      // Here, we are multiplying by (4 - i) to manually take the derivative. The
      // power of the term in index 0 is 4, index 1 is 3 and so on. To find the
      // coefficient of the derivative, we can use the power rule and multiply
      // the existing coefficient by its power.
      m_coefficients.set(4, i, m_coefficients.get(2, i) * (4 - i));
      m_coefficients.set(5, i, m_coefficients.get(3, i) * (4 - i));
    }
  }

  /**
   * Returns the coefficients matrix.
   *
   * @return The coefficients matrix.
   */
  @Override
  protected SimpleMatrix getCoefficients() {
    return m_coefficients;
  }

  /**
   * Returns the hermite basis matrix for quintic hermite spline interpolation.
   *
   * @return The hermite basis matrix for quintic hermite spline interpolation.
   */
  private SimpleMatrix makeHermiteBasis() {
    if (hermiteBasis == null) {
      // Given P(i), P'(i), P''(i), P(i+1), P'(i+1), P''(i+1), the control
      // vectors, we want to find the coefficients of the spline
      // P(t) = a5 * t^5 + a4 * t^4 + a3 * t^3 + a2 * t^2 + a1 * t + a0.
      //
      // P(i)     = P(0)   = a0
      // P'(i)    = P'(0)  = a1
      // P''(i)   = P''(0) = 2 * a2
      // P(i+1)   = P(1)   = a5 + a4 + a3 + a2 + a1 + a0
      // P'(i+1)  = P'(1)  = 5 * a5 + 4 * a4 + 3 * a3 + 2 * a2 + a1
      // P''(i+1) = P''(1) = 20 * a5 + 12 * a4 + 6 * a3 + 2 * a2
      //
      // [ P(i)     ] = [  0  0  0  0  0  1 ][ a5 ]
      // [ P'(i)    ] = [  0  0  0  0  1  0 ][ a4 ]
      // [ P''(i)   ] = [  0  0  0  2  0  0 ][ a3 ]
      // [ P(i+1)   ] = [  1  1  1  1  1  1 ][ a2 ]
      // [ P'(i+1)  ] = [  5  4  3  2  1  0 ][ a1 ]
      // [ P''(i+1) ] = [ 20 12  6  2  0  0 ][ a0 ]
      //
      // To solve for the coefficients, we can invert the 6x6 matrix and move it
      // to the other side of the equation.
      //
      // [ a5 ] = [  -6.0  -3.0  -0.5   6.0  -3.0   0.5 ][ P(i)     ]
      // [ a4 ] = [  15.0   8.0   1.5 -15.0   7.0  -1.0 ][ P'(i)    ]
      // [ a3 ] = [ -10.0  -6.0  -1.5  10.0  -4.0   0.5 ][ P''(i)   ]
      // [ a2 ] = [   0.0   0.0   0.5   0.0   0.0   0.0 ][ P(i+1)   ]
      // [ a1 ] = [   0.0   1.0   0.0   0.0   0.0   0.0 ][ P'(i+1)  ]
      // [ a0 ] = [   1.0   0.0   0.0   0.0   0.0   0.0 ][ P''(i+1) ]
      hermiteBasis =
          new SimpleMatrix(
              6,
              6,
              true,
              new double[] {
                -06.0, -03.0, -00.5, +06.0, -03.0, +00.5, +15.0, +08.0, +01.5, -15.0, +07.0, -01.0,
                -10.0, -06.0, -01.5, +10.0, -04.0, +00.5, +00.0, +00.0, +00.5, +00.0, +00.0, +00.0,
                +00.0, +01.0, +00.0, +00.0, +00.0, +00.0, +01.0, +00.0, +00.0, +00.0, +00.0, +00.0
              });
    }
    return hermiteBasis;
  }

  /**
   * Returns the control vector for each dimension as a matrix from the user-provided arrays in the
   * constructor.
   *
   * @param initialVector The control vector for the initial point.
   * @param finalVector The control vector for the final point.
   * @return The control vector matrix for a dimension.
   */
  private SimpleMatrix getControlVectorFromArrays(double[] initialVector, double[] finalVector) {
    if (initialVector.length != 3 || finalVector.length != 3) {
      throw new IllegalArgumentException("Size of vectors must be 3");
    }
    return new SimpleMatrix(
        6,
        1,
        true,
        new double[] {
          initialVector[0], initialVector[1], initialVector[2],
          finalVector[0], finalVector[1], finalVector[2]
        });
  }
}
