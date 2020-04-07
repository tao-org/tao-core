package ro.cs.tao.transfer;

import java.util.Map;

/**
 * Interface to be implemented by any class that wants to submit file copy requests and to be notified
 * when the transfer completes.
 *
 * @author Cosmin Cara
 * @since 1.1.0
 */
public interface FileTransferCallback {
    /**
     * Method called by the file transfer service when a transfer completes.
     * @param taskId    The execution task identifier for which the request was submitted
     * @param transferredFiles  The results of the transfer (key = source file, value = copied file)
     */
    void onCompleted(long taskId, Map<String, String> transferredFiles);
}
