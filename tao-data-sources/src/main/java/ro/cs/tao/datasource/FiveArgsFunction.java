package ro.cs.tao.datasource;

/**
 * Represents a functional interface that defines a method for applying a function
 * with five arguments and returning a result.
 *
 * @param <A> the type of the first argument to the function
 * @param <B> the type of the second argument to the function
 * @param <C> the type of the third argument to the function
 * @param <D> the type of the fourth argument to the function
 * @param <E> the type of the fifth argument to the function
 * @param <F> the type of the result of the function
 */
@FunctionalInterface
public interface FiveArgsFunction<A,B,C,D,E,F> {
    F apply(A arg1, B arg2, C arg3, D arg4, E arg5);
}
