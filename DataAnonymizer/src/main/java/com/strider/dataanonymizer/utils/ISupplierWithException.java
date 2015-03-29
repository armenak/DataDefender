package com.strider.dataanonymizer.utils;


/**
 * @author Akira Matsuo
 *
 * @param <T>
 * Much like a java.util.Supplier but throws an exception.
 */
@FunctionalInterface
public interface ISupplierWithException<T, E extends Exception> {
    public T get() throws E;
}
