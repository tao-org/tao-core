package ro.cs.tao.executor.impl;

import org.ggf.drmaa.v1.*;
import ro.cs.tao.component.ProcessingComponent;
import ro.cs.tao.component.TaoComponent;
import ro.cs.tao.executor.ExecutionException;
import ro.cs.tao.executor.IExecutor;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by cosmin on 9/12/2017.
 */
public class DrmaaTaoExecutor implements IExecutor, Runnable {

    private static final ExecutorService executorService;
    static {
        executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }
    /* Flag for trying to close the monitoring thread in an elegant manner */
    private boolean isRunning;
    protected Session session;

    @Override
    public void initialize() throws ExecutionException {
        // start the thread for monitoring current executions
        isRunning = true;
        executorService.submit(this);
        SessionFactory factory = SessionFactory.getFactory();
        session = factory.getSession();
        try {
            session.init (null);
        } catch (DrmaaException e) {
            throw new ExecutionException("Error initiating DRMAA session", e);
        }
    }

    @Override
    public void close()  throws ExecutionException {
        // stop the monitoring thread
        isRunning = false;
        executorService.shutdownNow();
        try {
            session.exit ();
        } catch (DrmaaException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean supports(TaoComponent component) {
        return (component instanceof ProcessingComponent);
    }

    @Override
    public void executeComponent(TaoComponent component) throws ExecutionException  {
        try {
            JobTemplate jt = session.createJobTemplate ();

            jt.setRemoteCommand ("sleeper.sh");
            jt.setArgs (new ArrayList() {{add("5");}});
            String id = session.runJob (jt);
            session.deleteJobTemplate (jt);

            while (session.getJobProgramStatus (id) != Session.RUNNING) {
                try {
                    Thread.sleep (1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            session.control (id, Session.SUSPEND);
            session.control (id, Session.RELEASE);

            JobInfo info = session.wait (id, Session.TIMEOUT_WAIT_FOREVER);

        } catch (DrmaaException e) {
            e.printStackTrace();
            throw new ExecutionException("Error executing DRMAA session operation", e);
        }
    }

    @Override
    public void stopExecution(TaoComponent component)  throws ExecutionException {
    }

    @Override
    public void run() {
        while(isRunning) {
            monitorExecutions();
        }
    }

    protected void monitorExecutions() {
        // check for the finished
    }
}
