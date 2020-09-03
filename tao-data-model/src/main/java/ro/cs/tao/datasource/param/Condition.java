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
package ro.cs.tao.datasource.param;

/**
 * Generic comparators that are used in query parameters dependencies evaluation.
 *
 * @author Cosmin Cara
 */
public abstract class Condition {

    /**
     * Equality condition
     */
    public static final Condition EQ = new Condition() {
        @Override
        public boolean evaluate(Comparable<Comparable> arg1, Comparable arg2) {
            return arg1!= null && arg2 != null && arg1.compareTo(arg2) == 0;
        }
    };
    /**
     * Difference condition
     */
    public static final Condition NE = new Condition() {
        @Override
        public boolean evaluate(Comparable<Comparable> arg1, Comparable arg2) {
            return arg1 == null || arg2 == null || arg1.compareTo(arg2) != 0;
        }
    };
    /**
     * 'Less than' condition
     */
    public static final Condition LT = new Condition() {
        @Override
        public boolean evaluate(Comparable<Comparable> arg1, Comparable arg2) {
            return arg1!= null && arg2 != null && arg1.compareTo(arg2) < 0;
        }
    };
    /**
     * 'Less than or equal' condition
     */
    public static final Condition LTE = new Condition() {
        @Override
        public boolean evaluate(Comparable<Comparable> arg1, Comparable arg2) {
            return arg1!= null && arg2 != null && arg1.compareTo(arg2) <= 0;
        }
    };
    /**
     * 'Greater than condition' condition
     */
    public static final Condition GT = new Condition() {
        @Override
        public boolean evaluate(Comparable<Comparable> arg1, Comparable arg2) {
            return arg1!= null && arg2 != null && arg1.compareTo(arg2) > 0;
        }
    };
    /**
     * 'Greater than or equal' condition
     */
    public static final Condition GTE = new Condition() {
        @Override
        public boolean evaluate(Comparable<Comparable> arg1, Comparable arg2) {
            return arg1!= null && arg2 != null && arg1.compareTo(arg2) >= 0;
        }
    };

    private Condition() { }

    public abstract boolean evaluate(Comparable<Comparable> arg1, Comparable arg2);

}
