package me.varun.autobuilder.scriptengine;

/**
 * Allows a class to be interpolated using a double from 0.0 to 1.0
 *
 * @param <T>
 *            Class that implements this interface
 */
public interface Interpolable<T> {

    /**
     * Interpolates between two Interpolables with the same type
     *
     * @param other
     *            Value of Interpolable that corresponds to 1 for percentage
     * @param percentage
     *            Percentage to interpolate. A value of 1 will return other and
     *            0 will return this Interpolable
     * @return New interpolated Interpolable
     */
    public T interpolate(T other, double percentage);
}
