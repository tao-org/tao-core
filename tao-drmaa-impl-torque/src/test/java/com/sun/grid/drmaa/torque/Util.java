package com.sun.grid.drmaa.torque;

import org.ggf.drmaa.Session;

public class Util {
    public static boolean isTorqueSession(Session session) {
        return session.getDrmSystem().equals("Torque/PBS");
    }
    public static boolean isSlurmSession(Session session) {
        return session.getDrmSystem().startsWith("SLURM");
    }
}
