/*
 * Copyright (C) 2018 CS ROMANIA
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see http://www.gnu.org/licenses/
 */
package ro.cs.tao.execution.local;

import org.ggf.drmaa.*;
import ro.cs.tao.configuration.ConfigurationManager;
import ro.cs.tao.docker.ExecutionConfiguration;
import ro.cs.tao.execution.DrmaaJobExtensions;
import ro.cs.tao.execution.drmaa.JobExitHandler;
import ro.cs.tao.execution.monitor.NodeManager;
import ro.cs.tao.spi.ServiceRegistry;
import ro.cs.tao.spi.ServiceRegistryManager;
import ro.cs.tao.topology.NodeDescription;
import ro.cs.tao.topology.NodeFlavor;
import ro.cs.tao.utils.ExecutionUnitFormat;
import ro.cs.tao.utils.executors.*;
import ro.cs.tao.utils.executors.container.ContainerCmdBuilder;
import ro.cs.tao.utils.executors.container.ContainerUnit;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Implementation of the DRMAA {@link Session} interface for local invocations.
 *
 * @author Cosmin Cara
 */
public class DefaultSession implements Session, JobExitHandler {
    private static final String SSH_ASYNC_KEY = "tao.ssh.async";
    private final String localHost;
    private final String localIp;
    private NodeDescription[] nodes;
    private volatile boolean initialized;
    private Map<String, JobTemplate> jobTemplates;
    private Map<String, Executor<?>> runningJobs;
    private Map<String, OutputAccumulator> jobOutputs;
    private AtomicInteger nodeCounter;
    // TODO: This should be configurable - add it to tao.properties or in a specific file?
    private Set<String> cmdsToRunAsSu = new HashSet<String>() {{add("docker");}};
    private final Logger logger = Logger.getLogger(DefaultSession.class.getName());
    private boolean canHaveNodeList;
    //private final boolean isDevMode = ExecutionConfiguration.developmentModeEnabled();


    public DefaultSession() {
        try {
            final InetAddress localHost = InetAddress.getLocalHost();
            this.localHost = localHost.getHostName();
            this.localIp = localHost.getHostAddress();
        } catch (UnknownHostException e) {
            logger.severe(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public String serviceName() { return "NoCRM"; }

    @Override
    public void init(String contact) throws DrmaaException {
        synchronized (this) {
            this.initialized = true;
            try {
                final InetAddress inetAddress = InetAddress.getLocalHost();
                final String hostName = inetAddress.getHostName();
                final String ipAddress = inetAddress.getHostAddress();
                canHaveNodeList = NodeManager.isAvailable();
                if (nodes == null) {
                    nodes = new NodeDescription[0];
                }
                if (Arrays.stream(nodes).noneMatch(n -> hostName.equalsIgnoreCase(n.getId()) || hostName.equals(ipAddress))) {
                    NodeDescription localNode = new NodeDescription();
                    localNode.setId(hostName);
                    localNode.setUserName(ConfigurationManager.getInstance().getValue("topology.master.user"));
                    localNode.setUserPass(ConfigurationManager.getInstance().getValue("topology.master.password"));
                    NodeFlavor flavor = new NodeFlavor();
                    flavor.setId("master");
                    flavor.setCpu(Runtime.getRuntime().availableProcessors());
                    flavor.setMemory((int) (Runtime.getRuntime().maxMemory() / 0x40000000));
                    flavor.setDisk(256);
                    flavor.setRxtxFactor(1.0f);
                    localNode.setDescription("Master node (localhost)");
                    NodeDescription[] newNodes = new NodeDescription[this.nodes.length + 1];
                    System.arraycopy(this.nodes, 0, newNodes, 1, this.nodes.length);
                    newNodes[0] = localNode;
                    this.nodes = newNodes;
                }
            } catch (UnknownHostException e) {
                logger.severe(e.getMessage());
            }
            this.jobTemplates = Collections.synchronizedMap(new HashMap<>());
            this.runningJobs = Collections.synchronizedMap(new HashMap<>());
            this.jobOutputs = Collections.synchronizedMap(new HashMap<>());
            this.nodeCounter = new AtomicInteger(0);
        }
    }

    @Override
    public void exit() throws DrmaaException {
        synchronized (this) {
            checkSession();
            this.jobTemplates.clear();
            this.runningJobs.clear();
            this.jobOutputs.clear();
            this.initialized = false;
        }
    }

    @Override
    public JobTemplate createJobTemplate() throws DrmaaException {
        checkSession();
        if (canHaveNodeList) {
            this.nodes = NodeManager.getInstance().getCurrentNodes();
        }
        final JobTemplate jobTemplate = new SimpleJobTemplate() {
            private long softTimeLimit;
            @Override
            public void setSoftRunDurationLimit(long softRunDurationLimit) throws DrmaaException {
                this.softTimeLimit = softRunDurationLimit;
            }

            @Override
            public long getSoftRunDurationLimit() throws DrmaaException {
                return this.softTimeLimit;
            }

            @Override
            protected Set getOptionalAttributeNames() {
                Set set = new HashSet();
                set.add(DrmaaJobExtensions.MEMORY_REQUIREMENTS_ATTRIBUTE);
                set.add(DrmaaJobExtensions.NODE_ATTRIBUTE);
                set.add(DrmaaJobExtensions.CONTAINER_ATTRIBUTE);
                set.add(DrmaaJobExtensions.SIMULATE_EXECUTION_ATTRIBUTE);
                set.add(DrmaaJobExtensions.TASK_NAME);
                set.add(DrmaaJobExtensions.TASK_ID);
                set.add(DrmaaJobExtensions.TASK_ANCESTOR_ID);
                set.add(DrmaaJobExtensions.TASK_ANCESTOR_OUTPUT);
                set.add(DrmaaJobExtensions.TASK_OUTPUT);
                set.add(DrmaaJobExtensions.JOB_ID);
                set.add(DrmaaJobExtensions.USER);
                set.add(DrmaaJobExtensions.SCRIPT_FORMAT);
                set.add(DrmaaJobExtensions.SCRIPT_PATH);
                return set;
            }
        };
        jobTemplate.setJobName(UUID.randomUUID().toString());
        this.jobTemplates.put(jobTemplate.getJobName(), jobTemplate);
        return jobTemplate;
    }

    @Override
    public void deleteJobTemplate(JobTemplate jt) throws DrmaaException {
        checkSession();
        checkRegistered(jt);
        this.jobTemplates.remove(jt.getJobName());
    }

    @Override
    public int getJobExitCode(String jobId) {
        final Executor<?> executor = this.runningJobs.get(jobId);
        return executor != null ? executor.getReturnCode() : Integer.MIN_VALUE;
    }

    @Override
    public String getJobOutput(String jobId) {
        OutputAccumulator consumer = this.jobOutputs.get(jobId);
        return consumer != null ? consumer.getOutput() : "n/a";
    }

    @Override
    public void cleanupJob(String jobId) {
        this.jobOutputs.remove(jobId);
        this.runningJobs.remove(jobId);
    }

    @Override
    public String runJob(JobTemplate jt) throws DrmaaException {
        checkSession();
        checkRegistered(jt);
        if (jt.getRemoteCommand() == null || jt.getRemoteCommand().isEmpty() || jt.getArgs() == null) {
            throw new InvalidJobTemplateException();
        }
        synchronized (this) {
            Long memConstraint = null;
            NodeDescription node = null;
            final List<String> args = new ArrayList<>();
            boolean simulate = false;
            ExecutionUnitFormat format = ExecutionUnitFormat.TAO;
            if (jt instanceof JobTemplateExtension) {
                JobTemplateExtension job = (JobTemplateExtension) jt;
                if (job.hasAttribute(DrmaaJobExtensions.MEMORY_REQUIREMENTS_ATTRIBUTE)) {
                    Object value = job.getAttribute(DrmaaJobExtensions.MEMORY_REQUIREMENTS_ATTRIBUTE);
                    memConstraint = value != null ? Long.parseLong(value.toString()) : null;
                }
                if (job.hasAttribute(DrmaaJobExtensions.NODE_ATTRIBUTE)) {
                    String host = (String) job.getAttribute(DrmaaJobExtensions.NODE_ATTRIBUTE);
                    node = this.nodes != null ? Arrays.stream(this.nodes).filter(n -> host.equals(n.getId())).findFirst().orElse(null) : null;
                }
                if (job.hasAttribute(DrmaaJobExtensions.SIMULATE_EXECUTION_ATTRIBUTE)) {
                    final Object attrValue = job.getAttribute(DrmaaJobExtensions.SIMULATE_EXECUTION_ATTRIBUTE);
                    simulate = attrValue != null ? (Boolean) attrValue : false;
                }
                if (job.hasAttribute(DrmaaJobExtensions.CONTAINER_ATTRIBUTE) && !simulate) {
                    ContainerUnit unit = (ContainerUnit) job.getAttribute(DrmaaJobExtensions.CONTAINER_ATTRIBUTE);
                    args.addAll(ContainerCmdBuilder.buildCommandLineArguments(unit));
                }
                if (job.hasAttribute(DrmaaJobExtensions.SCRIPT_FORMAT) && simulate) {
                    format = ExecutionUnitFormat.valueOf((String) job.getAttribute(DrmaaJobExtensions.SCRIPT_FORMAT));
                }
            }
            args.add(jt.getRemoteCommand());
            args.addAll(jt.getArgs());
            final ExecutionUnit unit;
            if (simulate) {
                unit = new ExecutionUnit(ExecutorType.SCRIPT, node != null ? node.getId() : this.localHost,
                                         node != null ? node.getUserName() : null,
                                         node != null ? node.getUserPass() : null,
                                         args, false, SSHMode.EXEC, format);
                final JobTemplateExtension template = (JobTemplateExtension) jt;
                unit.setContainerUnit((ContainerUnit) template.getAttribute(DrmaaJobExtensions.CONTAINER_ATTRIBUTE));
                unit.addMetadata("id", template.getAttribute(DrmaaJobExtensions.TASK_ID));
                unit.addMetadata("name", template.getAttribute(DrmaaJobExtensions.TASK_NAME));
                unit.addMetadata("dependsOn", template.getAttribute(DrmaaJobExtensions.TASK_ANCESTOR_ID));
                unit.addMetadata("inputs", template.getAttribute(DrmaaJobExtensions.TASK_ANCESTOR_OUTPUT));
                unit.addMetadata("canDelete", template.getAttribute(DrmaaJobExtensions.TASK_OUTPUT));
                unit.addMetadata("scriptPath", template.getAttribute(DrmaaJobExtensions.SCRIPT_PATH));
            } else if (ExecutionConfiguration.developmentModeEnabled()) {
                unit = new ExecutionUnit(ExecutorType.MOCK, node != null ? node.getId() : this.localHost,
                                         node != null ? node.getUserName() : null,
                                         node != null ? node.getUserPass() : null,
                                         //Crypto.decrypt(node.getUserPass(), node.getUserName()),
                                         args, false /*cmdsToRunAsSu.contains(jt.getRemoteCommand())*/, SSHMode.EXEC);
            } else {
                unit = node == null || isLocalHost(node.getId()) ?
                        new ExecutionUnit(ExecutorType.PROCESS, this.localHost, null, null,
                                          args, false/*cmdsToRunAsSu.contains(jt.getRemoteCommand())*/, null) :
                        new ExecutionUnit(ExecutorType.SSH2, node.getId(), node.getUserName(), node.getUserPass(),
                                          //Crypto.decrypt(node.getUserPass(), node.getUserName()),
                                          args, false /*cmdsToRunAsSu.contains(jt.getRemoteCommand())*/, SSHMode.EXEC,
                                          ConfigurationManager.getInstance().getBooleanValue(SSH_ASYNC_KEY),
                                          jt.getJobName() + "_" + System.nanoTime(), ExecutionUnitFormat.TAO);            }
            if (memConstraint != null) {
                unit.setMinMemory(memConstraint);
            }
            final String jobId = jt.getJobName() + ":" + System.nanoTime();
            final OutputAccumulator consumer = new OutputAccumulator();
            this.runningJobs.put(jobId, Executor.execute(consumer, unit));
            this.jobOutputs.put(jobId, consumer);
            return jobId;
        }
    }

    @Override
    public List runBulkJobs(JobTemplate jt, int start, int end, int incr) throws DrmaaException {
        throw new DeniedByDrmException("Not supported");
    }

    @Override
    public void control(String jobId, int action) throws DrmaaException {
        checkSession();
        Executor<?> runner = this.runningJobs.get(jobId);
        switch (action) {
            case HOLD:
                if (runner == null) {
                    throw new NoActiveSessionException("No active executor for job " + jobId);
                }
                if (!runner.isRunning()) {
                    throw new HoldInconsistentStateException();
                }
                runner.suspend();
                break;
            case SUSPEND:
                if (runner == null) {
                    throw new NoActiveSessionException("No active executor for job " + jobId);
                }
                if (!runner.isRunning()) {
                    throw new SuspendInconsistentStateException();
                }
                runner.suspend();
                break;
            case TERMINATE:
                if (runner == null) {
                    throw new NoActiveSessionException("No active executor for job " + jobId);
                }
                runner.stop();
                break;
            case RELEASE:
                if (runner == null) {
                    throw new NoActiveSessionException("No active executor for job " + jobId);
                }
                if (!runner.isSuspended()) {
                    throw new ReleaseInconsistentStateException();
                }
                runner.resume();
                break;
            case RESUME:
                if (runner == null) {
                    throw new NoActiveSessionException("No active executor for job " + jobId);
                }
                if (!runner.isSuspended()) {
                    throw new ResumeInconsistentStateException();
                }
                runner.resume();
                break;
        }
    }

    @Override
    public void synchronize(List jobIds, long timeout, boolean dispose) throws DrmaaException {
        checkSession();
        if (jobIds == null || jobIds.isEmpty()) {
            throw new IllegalArgumentException("At least one jobId should be given");
        }
        List<Executor> runners = new ArrayList<>();
        for (Object obj : jobIds) {
            Executor runner = this.runningJobs.get(obj);
            if (runner == null) {
                throw new InvalidJobException();
            }
            runners.add(runner);
        }
        try {
            final CountDownLatch singleLatch = new CountDownLatch(runners.size());
            final ExecutorService threadPool = Executors.newCachedThreadPool();
            final boolean[] timeoutOccured = {false};
            runners.stream()
                    .map(Executor::getWaitObject)
                    .forEach(w -> threadPool.execute(() -> {
                        try {
                            w.await(timeout, TimeUnit.SECONDS);
                        } catch (InterruptedException e) {
                            timeoutOccured[0] = true;
                            logger.warning(e.getMessage());
                        } finally {
                            singleLatch.countDown();
                        }
                    }));
            if (timeoutOccured[0]) {
                throw new ExitTimeoutException();
            }
        } finally {
            for (Object obj : jobIds) {
                this.runningJobs.remove(obj);
                this.jobOutputs.remove(obj);
            }
        }
    }

    @Override
    public JobInfo wait(String jobId, long timeout) throws DrmaaException {
        checkSession();
        Executor runner = this.runningJobs.get(jobId);
        if (runner == null) {
            throw new InvalidJobException();
        }
        try {
            final CountDownLatch waitObject = runner.getWaitObject();
            try {
                waitObject.await(timeout, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                logger.warning(e.getMessage());
            }
            if (!runner.hasCompleted()) {
                runner.stop();
                throw new ExitTimeoutException(jobId);
            }
            return new DefaultJobInfo(jobId, runner);
        } finally {
            this.runningJobs.remove(jobId);
            this.jobOutputs.remove(jobId);
        }
    }

    @Override
    public int getJobProgramStatus(String jobId) throws DrmaaException {
        checkSession();
        Executor runner = this.runningJobs.get(jobId);
        if (runner == null) {
            throw new InvalidJobException();
        }
        return runner.isRunning() ? RUNNING :
                runner.isSuspended() ? USER_SYSTEM_SUSPENDED :
                    runner.hasCompleted() ?
                            runner.getReturnCode() != 0 ? FAILED : DONE
                : UNDETERMINED;
    }

    @Override
    public String getContact() {
        return null;
    }

    @Override
    public Version getVersion() {
        return new Version(1, 0);
    }

    @Override
    public String getDrmSystem() {
        List<String> drmFactories = new ArrayList<>();
        //drmFactories.add(AbstractSessionFactory.class.getPackage().getName());
        if (!this.initialized) {
            ServiceRegistry<SessionFactory> serviceRegistry =
                    ServiceRegistryManager.getInstance().getServiceRegistry(SessionFactory.class);
            if (serviceRegistry != null) {
                Set<SessionFactory> services = serviceRegistry.getServices();
                if (services != null) {
                    drmFactories.addAll(services.stream()
                                                .map(sf -> sf.getClass().getPackage().getName())
                                                .collect(Collectors.toList()));
                }
            }
        }
        return String.join(",", drmFactories);
    }

    @Override
    public String getDrmaaImplementation() {
        List<String> drmFactories = new ArrayList<>();
        drmFactories.add(DefaultSessionFactory.class.getName());
        if (!this.initialized) {
            ServiceRegistry<SessionFactory> serviceRegistry =
                    ServiceRegistryManager.getInstance().getServiceRegistry(SessionFactory.class);
            if (serviceRegistry != null) {
                Set<SessionFactory> services = serviceRegistry.getServices();
                if (services != null) {
                    drmFactories.addAll(services.stream()
                                                .map(sf -> sf.getClass().getName())
                                                .collect(Collectors.toList()));
                }
            }
        }
        return String.join(",", drmFactories);
    }

    private boolean isLocalHost(String name) {
        return this.localHost.equalsIgnoreCase(name) || this.localIp.equals(name);
    }

    private void checkSession() throws NoActiveSessionException {
        if (!this.initialized) {
            throw new NoActiveSessionException();
        }
    }

    private void checkRegistered(JobTemplate jobTemplate) throws DrmaaException {
        if (jobTemplate == null) {
            throw new IllegalArgumentException("JobTemplate cannot be null");
        }
        String jobName = jobTemplate.getJobName();
        if (!this.jobTemplates.containsKey(jobName)) {
            throw new InvalidJobTemplateException();
        }
    }
}
