package ro.cs.tao.datasource;

@FunctionalInterface
public interface FiveArgsFunction<A,B,C,D,E,F> {
    F apply(A arg1, B arg2, C arg3, D arg4, E arg5);
}
