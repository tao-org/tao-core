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
package ro.cs.tao.messaging;

import ro.cs.tao.messaging.progress.*;
import ro.cs.tao.security.SessionStore;
import ro.cs.tao.utils.executors.monitoring.ProgressListener;

import java.security.Principal;
import java.util.Map;

/**
 * Default implementation for a progress listener that sends progress information on the message bus.
 *
 * @author Cosmin Cara
 */
public class ProgressNotifier implements ProgressListener {
    private final Topic topic;
    private final Object owner;
    private final Principal principal;
    private final Map<String, String> additionalInfo;
    protected String taskName;
    protected double taskCounter;
    private double subTaskCounter;
    private final ProgressListener subListener;

    public ProgressNotifier(Object source, Topic topic) {
        this(SessionStore.currentContext().getPrincipal(), source, topic);
    }

    public ProgressNotifier(Principal principal, Object source, Topic topic) {
        this(principal, source, topic, null);
    }

    public ProgressNotifier(Principal principal, Object source, Topic topic, Map<String, String> additionalInfo) {
        this.owner = source;
        this.topic = topic;
        if (this.topic == null) {
            throw new NullPointerException("topic");
        }
        this.principal = principal;
        if (this.principal == null) {
            throw new NullPointerException("principal");
        }
        this.additionalInfo = additionalInfo;
        this.subListener = new SubListener();
    }

    @Override
    public void started(String taskName) {
        this.taskCounter = 0;
        this.taskName = taskName;
        sendTransientMessage(createStartMessage());
    }

    @Override
    public void subActivityStarted(String subTaskName) {
        this.subTaskCounter = 0;
        this.subListener.started(subTaskName);
    }

    @Override
    public void subActivityEnded(String subTaskName) {
        this.subTaskCounter = 1;
        this.subListener.ended();
    }

    @Override
    public void subActivityEnded(String subTaskName, boolean succeeded) {
        this.subTaskCounter = 1;
        this.subListener.ended(succeeded);
    }
    
    @Override
    public void ended() {
        this.taskCounter = 1;
        sendTransientMessage(createEndMessage());
    }

    @Override
    public void ended(boolean succeeded) {
        this.taskCounter = 1;
        sendTransientMessage(createEndMessage(succeeded));
    }

    @Override
    public void notifyProgress(double progressValue) {
        /*if (progressValue < taskCounter) {
            throw new IllegalArgumentException(
                    String.format(Locale.US, "Progress taskCounter cannot go backwards [actual:%.2f%%, received:%.2f%%]",
                                  taskCounter, progressValue));
        }*/
        taskCounter = progressValue;
        if (taskCounter < 1) {
            sendTransientMessage(new ActivityProgress(taskName, progressValue));
        } else {
            ended();
        }
    }

    @Override
    public void notifyProgress(String subTaskName, double subTaskProgress) {
        notifyProgress(subTaskName, subTaskProgress, taskCounter);
    }

    @Override
    public void notifyProgress(String subTaskName, double subTaskProgress, double overallProgress) {
        /*if (subTaskProgress < subTaskCounter) {
            throw new IllegalArgumentException(
                    String.format(Locale.US, "Progress counter cannot go backwards [actual:%.2f%%, received:%.2f%%]",
                                  subTaskCounter, subTaskProgress));
        }*/
        subTaskCounter = subTaskProgress;
        taskCounter = overallProgress;
        if (subTaskCounter < 1) {
            sendTransientMessage(new SubActivityProgress(taskName, subTaskName, taskCounter, subTaskCounter));
        } else {
            subActivityEnded(subTaskName);
        }
    }

    @Override
    public ProgressListener subProgressListener() {
        return this.subListener;
    }

    protected void sendTransientMessage(Message message) {
        message.setId(System.currentTimeMillis());
        message.setTimestamp(System.currentTimeMillis());
        if (additionalInfo != null) {
            additionalInfo.forEach(message::addItem);
        }
        message.setUserId(this.principal.getName());
        Messaging.send(this.principal, this.topic.value(), message, false);
    }


    private void sendMessage(Message message) {
        message.setTimestamp(System.currentTimeMillis());
        if (additionalInfo != null) {
            additionalInfo.forEach(message::addItem);
        }
        Messaging.send(this.principal, this.topic.value(), message, true);
    }

    private ActivityStart createStartMessage() {
        ActivityStart message;
        if (this.topic.equals(Topic.TRANSFER_PROGRESS)) {
            message = new TransferStart(this.taskName);
        } else {
            message = new ActivityStart(this.taskName);
        }
        message.setUserId(this.principal.getName());
        message.setTopic(this.topic.value());
        return message;
    }

    private ActivityEnd createEndMessage() {
        ActivityEnd message;
        if (this.topic.equals(Topic.TRANSFER_PROGRESS)) {
            message = new TransferEnd(this.taskName);
        } else {
            message = new ActivityEnd(this.taskName);
        }
        message.setUserId(this.principal.getName());
        message.setTopic(this.topic.value());
        return message;
    }

    private ActivityEnd createEndMessage(boolean succeeded) {
        ActivityEnd message;
        if (this.topic.equals(Topic.TRANSFER_PROGRESS)) {
            message = new TransferEnd(this.taskName);
        } else {
            message = new ActivityEnd(this.taskName);
        }
        message.setUserId(this.principal.getName());
        message.setTopic(this.topic.value());
        message.setPayload(String.valueOf(succeeded));
        return message;
    }

    private SubActivityStart createSubStartMessage(String subTask) {
        SubActivityStart message;
        if (this.topic.equals(Topic.TRANSFER_PROGRESS)) {
            message = new SubTransferStart(this.taskName, subTask);
        } else {
            message = new SubActivityStart(this.taskName, subTask);
        }
        message.setUserId(this.principal.getName());
        message.setTopic(this.topic.value());
        return message;
    }

    private SubActivityEnd createSubEndMessage(String subTask) {
        SubActivityEnd message;
        if (this.topic.equals(Topic.TRANSFER_PROGRESS)) {
            message = new SubTransferEnd(this.taskName, subTask);
        } else {
            message = new SubActivityEnd(this.taskName, subTask);
        }
        message.setUserId(this.principal.getName());
        message.setTopic(this.topic.value());
        return message;
    }

    private SubActivityEnd createSubEndMessage(String subTask, boolean succeeded) {
        SubActivityEnd message;
        if (this.topic.equals(Topic.TRANSFER_PROGRESS)) {
            message = new SubTransferEnd(this.taskName, subTask, succeeded);
        } else {
            message = new SubActivityEnd(this.taskName, subTask);
        }
        message.setUserId(this.principal.getName());
        message.setTopic(this.topic.value());
        return message;
    }

    private class SubListener implements ProgressListener {
        private String subTask;
        private boolean theOnlyItem;
        @Override
        public void started(String taskName) {
            this.subTask = taskName;
            final int idx = taskName.lastIndexOf('/');
            final int idx2 = ProgressNotifier.this.taskName.lastIndexOf('/');
            theOnlyItem = idx < 0 || (idx2 > 0 && taskName.substring(idx).equals(ProgressNotifier.this.taskName.substring(idx2)));
            sendTransientMessage(createSubStartMessage(this.subTask));
        }

        @Override
        public void ended() {
            sendTransientMessage(createSubEndMessage(this.subTask));
        }

        @Override
        public void ended(boolean succeeded) {
            sendTransientMessage(createSubEndMessage(this.subTask, succeeded));
        }

        @Override
        public void notifyProgress(double progressValue) {
            if (theOnlyItem) {
                ProgressNotifier.this.notifyProgress(progressValue);
            } else {
                ProgressNotifier.this.notifyProgress(subTask, progressValue);
            }
        }

        @Override
        public void subActivityStarted(String subTaskName) {
        }

        @Override
        public void subActivityEnded(String subTaskName) {
        }

        @Override
        public void notifyProgress(String subTaskName, double subTaskProgress) {
        }

        @Override
        public void notifyProgress(String subTaskName, double subTaskProgress, double overallProgress) {
        }
    }
}
