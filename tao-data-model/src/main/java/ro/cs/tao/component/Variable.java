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
package ro.cs.tao.component;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 * @author Cosmin Cara
 */
@XmlRootElement(name = "variable")
public class Variable implements Serializable{

    private String key;
    private String value;

    public Variable() {
        super();
    }

    public Variable(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    /**
     * Gets the value of the system variable
     */
    public String getValue() { return this.value; }

    /**
     * Sets the value of the system variable
     */
    public void setValue(String value) { this.value = value; }

    public String defaultName() {
        return "NewVariable";
    }

    @SuppressWarnings("MethodDoesntCallSuperMethod")
    @Override
    protected Variable clone() throws CloneNotSupportedException {
        return new Variable(this.key, this.value);
    }

}
