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

package ro.cs.tao.utils.executors;

public class OutputAccumulator implements OutputConsumer {
    private boolean delimitLines;
    private final StringBuilder buffer = new StringBuilder();

    public void preserveLineSeparator(boolean delimitLines) {
        this.delimitLines = delimitLines;
    }

    @Override
    public void consume(String message) {
        synchronized (buffer) {
            buffer.append(message.replace('\n', ' '));
            if (delimitLines) {
                buffer.append("\n");
            }
        }
    }
    public String getOutput() {
        synchronized (buffer) {
            return this.buffer.toString();
        }
    }
    public void reset() {
        synchronized (buffer) {
            this.buffer.setLength(0);
        }
    }
}
