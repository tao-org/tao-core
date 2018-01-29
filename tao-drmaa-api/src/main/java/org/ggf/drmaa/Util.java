package org.ggf.drmaa;

public class Util {
    public static boolean isTorqueSession(Session session) {
        return session.getDrmSystem().equals("Torque/PBS");
    }
    public static boolean isSlurmSession(Session session) {
        return session.getDrmSystem().startsWith("SLURM");
    }
}
