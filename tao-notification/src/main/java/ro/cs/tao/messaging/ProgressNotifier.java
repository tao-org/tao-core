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

import ro.cs.tao.ProgressListener;
import ro.cs.tao.messaging.progress.*;
import ro.cs.tao.security.SessionStore;

import java.security.Principal;
import java.util.Locale;
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
    private String taskName;
    private double taskCounter;
    private double subTaskCounter;

    public ProgressNotifier(Object source, Topic topic) {
        this(SessionStore.currentContext().getPrincipal(), source, topic);
    }

    public ProgressNotifier(Principal principal, Object source, Topic topic) {
        this(principal, source, topic, null);
    }

    public ProgressNotifier(Principal principal, Object source, Topic topic, Map<String, String> additionalInfo) {
        this.owner = source;
        this.topic = topic;
        this.principal = principal;
        this.additionalInfo = additionalInfo;
    }

    @Override
    public void started(String taskName) {
        this.taskCounter = 0;
        this.taskName = taskName;
        sendMessage(new ActivityStartMessage(taskName));
    }

    @Override
    public void subActivityStarted(String subTaskName) {
        this.subTaskCounter = 0;
        sendTransientMessage(new SubActivityStartMessage(taskName, subTaskName));
    }

    @Override
    public void subActivityEnded(String subTaskName) {
        this.subTaskCounter = 1;
        sendTransientMessage(new SubActivityEndMessage(taskName, subTaskName));
    }

    @Override
    public void ended() {
        this.taskCounter = 1;
        sendMessage(new ActivityEndMessage(taskName));
    }

    @Override
    public void notifyProgress(double progressValue) {
        if (progressValue < taskCounter) {
            throw new IllegalArgumentException(
                    String.format(Locale.US, "Progress taskCounter cannot go backwards [actual:%.2f%%, received:%.2f%%]",
                                  taskCounter, progressValue));
        }
        taskCounter = progressValue;
        if (taskCounter < 1) {
            sendTransientMessage(new ActivityProgressMessage(taskName, progressValue));
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
        if (subTaskProgress < subTaskCounter) {
            throw new IllegalArgumentException(
                    String.format(Locale.US, "Progress counter cannot go backwards [actual:%.2f%%, received:%.2f%%]",
                                  subTaskCounter, subTaskProgress));
        }
        subTaskCounter = subTaskProgress;
        taskCounter = overallProgress;
        if (subTaskCounter < 1) {
            sendTransientMessage(new SubActivityProgressMessage(taskName, subTaskName, taskCounter, subTaskCounter));
        } else {
            subActivityEnded(subTaskName);
        }
    }

    private void sendMessage(Message message) {
        message.setTimestamp(System.currentTimeMillis());
        if (additionalInfo != null) {
            additionalInfo.forEach(message::addItem);
        }
        Messaging.send(this.principal, this.topic.value(), message, true);
    }

    private void sendTransientMessage(Message message) {
        message.setTimestamp(System.currentTimeMillis());
        if (additionalInfo != null) {
            additionalInfo.forEach(message::addItem);
        }
        Messaging.send(this.principal, this.topic.value(), message, false);
    }
}
