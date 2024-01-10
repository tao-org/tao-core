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

package ro.cs.tao.execution.model;

import ro.cs.tao.datasource.beans.Query;

public class ExecutionQuery {
    private long id;
    private ExecutionJob job;
    private Query query;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public ExecutionJob getJob() { return job; }
    public void setJob(ExecutionJob job) { this.job = job; }

    public Query getQuery() { return query; }
    public void setQuery(Query query) { this.query = query; }
}
