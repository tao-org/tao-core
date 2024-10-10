package ro.cs.tao.persistence;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

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
        TransactionTemplate transaction = null;
        try {
            V retVal = null;
            if (transactionManager != null) {
                transaction = new TransactionTemplate(transactionManager);
                retVal = transaction.execute(new TransactionCallback<V>() {
                    @Override
                    public V doInTransaction(TransactionStatus status) {
                        try {
                            return inner.call();
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
            } else {
                retVal = inner.call();
            }
            return retVal;
        } catch (Exception e) {
            if (e.getClass().isAssignableFrom(this.exceptionType)) {
                throw this.exceptionType.cast(e);
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

    /**
     * Executes the wrapped runnable without returning anything,
     * or rolls back the transaction and throws an exception of the checked type.
     * @param inner The runnable code
     * @throws E    The checked exception to be (re-)thrown
     */
    public void execute(Runnable inner) throws E {
        TransactionTemplate transaction = null;
        try {
            if (transactionManager != null) {
                transaction = new TransactionTemplate(transactionManager);
                transaction.execute(new TransactionCallback<Void>() {
                    @Override
                    public Void doInTransaction(TransactionStatus status) {
                        inner.run();
                        return null;
                    }
                });
            } else {
                inner.run();
            }
        } catch (Exception e) {
            if (e.getClass().isAssignableFrom(this.exceptionType)) {
                throw this.exceptionType.cast(e);
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
