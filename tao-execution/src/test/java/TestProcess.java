import org.apache.commons.lang.SystemUtils;
import org.ggf.drmaa.DrmaaException;
import org.ggf.drmaa.ExitTimeoutException;
import org.ggf.drmaa.JobInfo;
import org.ggf.drmaa.JobTemplate;
import org.ggf.drmaa.Session;
import org.ggf.drmaa.SessionFactory;

import java.util.Arrays;
import java.util.List;

import org.junit.runner.JUnitCore;

/**
 * @author Cosmin Cara
 */
public class TestProcess {

    private static final String PING_WIN = "ping.exe %s -n 100";
    private static final String PING_LIN = "ping %s -c 100";

    public static void main(String[] args) throws DrmaaException, InterruptedException {
        System.out.println("TestProcess");
        SessionFactory sessionFactory = SessionFactory.getFactory();
        Session session = sessionFactory.getSession();
        System.out.println(session.getDrmSystem());
        session.init(null);
        String cmdLine = SystemUtils.IS_OS_WINDOWS ? PING_WIN : PING_LIN;
        JobTemplate jobTemplate1 = session.createJobTemplate();
        List<String> arguments = Arrays.asList(String.format(cmdLine, "127.0.0.1").split(" "));
        jobTemplate1.setRemoteCommand(arguments.get(0));
        jobTemplate1.setArgs(arguments.subList(1, arguments.size()));

        JobTemplate jobTemplate2 = session.createJobTemplate();
        arguments = Arrays.asList(String.format(cmdLine, "8.8.8.8").split(" "));
        jobTemplate2.setRemoteCommand(arguments.get(0));
        jobTemplate2.setArgs(arguments.subList(1, arguments.size()));
        String id1 = session.runJob(jobTemplate1);
        String id2 = session.runJob(jobTemplate2);
        try {
            JobInfo info1 = session.wait(id1, 5);
            System.out.println("Job1 exit code: " + info1.getExitStatus());
        } catch (ExitTimeoutException ex) {
            System.out.println("Job1 timed out");
        }
        session.control(id2, Session.SUSPEND);
        System.out.println("Job2 suspended; should resume in 3 seconds");
        Thread.sleep(3000);
        session.control(id2, Session.RESUME);
        System.out.println("Job2 resumed; should be terminated in 3 seconds");
        Thread.sleep(3000);
        session.control(id2, Session.TERMINATE);
        session.deleteJobTemplate(jobTemplate1);
        session.deleteJobTemplate(jobTemplate2);
        session.exit();
        System.exit(0);

        //JUnitCore.main(com.sun.grid.drmaa.slurm.SessionImplJobTest);
    }
}
