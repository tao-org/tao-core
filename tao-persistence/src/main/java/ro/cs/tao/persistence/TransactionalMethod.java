package ro.cs.tao.persistence;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.Callable;

/**
 * Utility class that encapsulates a method invocation (body) in a Spring transaction,
 * so that if any exception is thrown along the way, the transaction is rolled back.
 *
 * For example:
 * - original method:
 * <code>
 *      public Object someMethod(Object arg1, Object arg2) throws CheckedException {
 *          // original code
 *      }
 * </code>
 * - modified method to be transactional:
 * <code>
 *     public Object someMethod(Object arg1, Object arg2) throws CheckedException {
 *          return TransactionalMethod.withExceptionType(CheckedException.class).execute(() -> {
 *              // original code
 *          });
 *     }
 * </code>
 *
 * @param <E>   The checked type of the exception thrown by the original method
 */
public class TransactionalMethod<E extends Exception> {
    private static PlatformTransactionManager transactionManager;
    private final Class<E> exceptionType;

    /**
     * Sets the reference to the Spring transaction manager
     * @param transactionManager    The transaction manager
     */
    public static void setTransactionManager(PlatformTransactionManager transactionManager) {
        TransactionalMethod.transactionManager = transactionManager;
    }

    /**
     * Sets the exception type thrown by the original method.
     *
     * @param exceptionType The exception class (type)
     * @param <T>   The checked exception type.
     */
    public static <T extends Exception> TransactionalMethod<T> withExceptionType(Class<T> exceptionType) {
        return new TransactionalMethod<>(exceptionType);
    }

    private TransactionalMethod(Class<E> exceptionType) {
        this.exceptionType = exceptionType;
    }

    /**
     * Executes the wrapped callable and returns what the callable is supposed to return,
     * or rolls back the transaction and throws an exception of the checked type.
     * @param inner The callable code
     * @param <V>   The return type of the callable code
     * @throws E    The checked exception to be (re-)thrown
     */
    public <V> V execute(Callable<V> inner) throws E {
        TransactionStatus transaction = null;
        try {
            if (transactionManager != null) {
                transaction = transactionManager.getTransaction(null);
            }
            V retVal = inner.call();
            if (transaction != null) {
                transactionManager.commit(transaction);
            }
            return retVal;
        } catch (Exception e) {
            if (transaction != null) {
                transactionManager.rollback(transaction);
            }
            if (e.getClass().isAssignableFrom(this.exceptionType)) {
                throw (E) e;
            } else {
                try {
                    throw this.exceptionType.getConstructor(Throwable.class).newInstance(e);
                } catch (InstantiationException | NoSuchMethodException | IllegalAccessException |
                         InvocationTargetException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
    }
}
