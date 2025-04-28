package ro.cs.tao.execution.model;

/**
 * QueueOperation is an enumeration representing the possible operations
 * that can be performed on a queue. This enum provides constants
 * for commonly used queue modification operations.
 *
 * Enum Constants:
 * - MOVE_UP: Represents an operation where an element is moved one position up in the queue.
 * - MOVE_DOWN: Represents an operation where an element is moved one position down in the queue.
 * - REMOVE: Represents an operation to remove an element from the queue.
 */
public enum QueueOperation {
    MOVE_UP,
    MOVE_DOWN,
    REMOVE
}