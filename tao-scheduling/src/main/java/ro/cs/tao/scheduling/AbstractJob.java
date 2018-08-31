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
package ro.cs.tao.scheduling;

import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import ro.cs.tao.persistence.PersistenceManager;

import java.util.logging.Logger;

/**
 * @author Cosmin Cara
 */
public abstract class AbstractJob implements Job {

    protected static final String MESSAGE = "[site '%s',sensor '%s'] %s";

    protected final PersistenceManager persistenceManager;
    protected Logger logger = Logger.getLogger(getClass().getName());

    public AbstractJob() {
        persistenceManager = persistenceManager();
    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        logger.info(String.format("Job '%s' started", jobExecutionContext.getJobDetail().getKey()));
        final JobDataMap dataMap = jobExecutionContext.getMergedJobDataMap();
        try {
            executeImpl(dataMap);
        } catch (Throwable t) {
            logger.warning(t.getMessage());
        }
    }

    protected abstract void executeImpl(JobDataMap dataMap);

    protected abstract PersistenceManager persistenceManager();
}
