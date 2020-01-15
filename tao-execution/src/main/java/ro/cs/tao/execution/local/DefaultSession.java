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
import ro.cs.tao.configuration.Configuration;
import ro.cs.tao.configuration.ConfigurationManager;
import ro.cs.tao.execution.Constants;
import ro.cs.tao.execution.monitor.NodeManager;
import ro.cs.tao.spi.ServiceRegistry;
import ro.cs.tao.spi.ServiceRegistryManager;
import ro.cs.tao.topology.NodeDescription;
import ro.cs.tao.topology.NodeFlavor;
import ro.cs.tao.utils.executors.*;

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
public class DefaultSession implements Session {
    private NodeDescription[] nodes;
    private volatile boolean initialized;
    private Map<String, JobTemplate> jobTemplates;
    private Map<String, Executor<?>> runningJobs;
    private Map<String, OutputAccumulator> jobOutputs;
    private AtomicInteger nodeCounter;
    // TODO: This should be configurable - add it to tao.properties or in a specific file?
    private Set<String> cmdsToRunAsSu = new HashSet<String>() {{add("docker");}};
    private Logger logger = Logger.getLogger(DefaultSession.class.getName());
    private boolean canHaveNodeList;

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
                    localNode.setUserName(ConfigurationManager.getInstance().getValue(Configuration.Topology.MASTER_USER));
                    localNode.setUserPass(ConfigurationManager.getInstance().getValue(Configuration.Topology.MASTER_PASSWORD));
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
        JobTemplate jobTemplate = new SimpleJobTemplate() {
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
                set.add(Constants.MEMORY_REQUIREMENTS_ATTRIBUTE);
                set.add(Constants.NODE_ATTRIBUTE);
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

    public String getJobOutput(String jobId) throws DrmaaException {
        OutputAccumulator consumer = this.jobOutputs.get(jobId);
        return consumer != null ? consumer.getOutput() : "n/a";
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
            if (jt instanceof JobTemplateExtension) {
                JobTemplateExtension job = (JobTemplateExtension) jt;
                if (job.hasAttribute(Constants.MEMORY_REQUIREMENTS_ATTRIBUTE)) {
                    Object value = job.getAttribute(Constants.MEMORY_REQUIREMENTS_ATTRIBUTE);
                    memConstraint = value != null ? Long.parseLong(value.toString()) : null;
                }
                if (job.hasAttribute(Constants.NODE_ATTRIBUTE)) {
                    String host = (String) job.getAttribute(Constants.NODE_ATTRIBUTE);
                    node = this.nodes != null ? Arrays.stream(this.nodes).filter(n -> host.equals(n.getId())).findFirst().orElse(null) : null;
                }
            }
            List<String> args = new ArrayList<>();
            args.add(jt.getRemoteCommand());
            args.addAll(jt.getArgs());
            final ExecutionUnit unit = node == null || isLocalHost(node.getId()) ?
                    new ExecutionUnit(ExecutorType.PROCESS, node.getId(), node.getUserName(), node.getUserPass(),
                                      args, false/*cmdsToRunAsSu.contains(jt.getRemoteCommand())*/, null) :
                    new ExecutionUnit(ExecutorType.SSH2, node.getId(), node.getUserName(), node.getUserPass(),
                                      //Crypto.decrypt(node.getUserPass(), node.getUserName()),
                                      args, false /*cmdsToRunAsSu.contains(jt.getRemoteCommand())*/, SSHMode.EXEC);
            if (memConstraint != null) {
                unit.setMinMemory(memConstraint);
            }
            String jobId = jt.getJobName() + ":" + System.nanoTime();
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
        Executor runner = this.runningJobs.get(jobId);
        if (runner != null) {
            switch (action) {
                case HOLD:
                    if (!runner.isRunning()) {
                        throw new HoldInconsistentStateException();
                    }
                    runner.suspend();
                    break;
                case SUSPEND:
                    if (!runner.isRunning()) {
                        throw new SuspendInconsistentStateException();
                    }
                    runner.suspend();
                    break;
                case TERMINATE:
                    runner.stop();
                    break;
                case RELEASE:
                    if (!runner.isSuspended()) {
                        throw new ReleaseInconsistentStateException();
                    }
                    runner.resume();
                    break;
                case RESUME:
                    if (!runner.isSuspended()) {
                        throw new ResumeInconsistentStateException();
                    }
                    runner.resume();
                    break;
            }
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
        boolean ret = false;
        try {
            final InetAddress localHost = InetAddress.getLocalHost();
            final String hostName = localHost.getHostName();
            final String ipAddress = localHost.getHostAddress();
            ret = hostName.equalsIgnoreCase(name) || ipAddress.equals(name);
        } catch (UnknownHostException e) {
            logger.severe(e.getMessage());
        }
        return ret;
    }

    private void    checkSession() throws NoActiveSessionException {
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

    /*private NodeDescription getAvailableNode(long memory) {
        NodeDescription node;
        if (NodeManager.isAvailable() && NodeManager.getInstance() != null) {
            node = NodeManager.getInstance().getAvailableNode(memory, 0);
        } else {
            if (nodeCounter.get() == this.nodes.length) {
                nodeCounter.set(0);
            }
            node = this.nodes[this.nodeCounter.getAndIncrement()];
        }
        return node;
    }*/
}
