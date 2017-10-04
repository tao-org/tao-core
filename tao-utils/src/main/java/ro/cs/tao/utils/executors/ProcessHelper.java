package ro.cs.tao.utils.executors;

import com.sun.jna.Pointer;
import org.apache.commons.lang.SystemUtils;
import ro.cs.tao.utils.executors.win.Kernel32;
import ro.cs.tao.utils.executors.win.NtDll;
import ro.cs.tao.utils.executors.win.Win32Api;

import java.io.IOException;
import java.lang.reflect.Field;

/**
 * @author Cosmin Cara
 */
public final class ProcessHelper {

    private static final String WIN_KILL = "taskkill /PID %s /F /T";
    private static final String LINUX_KILL = "kill -9 %s";
    private static final String LINUX_SUSPEND = "kill -STOP %s";
    private static final String LINUX_RESUME = "kill -CONT %s";

    public static int getPID(Process process) {
        int retVal = -1;
        if (SystemUtils.IS_OS_WINDOWS) {
            try {
                Field field = process.getClass().getDeclaredField("handle");
                field.setAccessible(true);
                long handle = field.getLong(process);
                Kernel32 kernel = Kernel32.INSTANCE;
                Win32Api.HANDLE h = new Win32Api.HANDLE();
                h.setPointer(Pointer.createConstant(handle));
                retVal = kernel.GetProcessId(h);
            } catch (Throwable ignored) { }
        } else if (SystemUtils.IS_OS_LINUX) {
            try {
                Field field = process.getClass().getDeclaredField("pid");
                field.setAccessible(true);
                retVal = field.getInt(process);
            } catch (Throwable ignored) { }
        } else {
            throw new UnsupportedOperationException();
        }
        return retVal;
    }

    public static void suspend(Process process) {
        if (SystemUtils.IS_OS_WINDOWS) {
            try {
                Field field = process.getClass().getDeclaredField("handle");
                field.setAccessible(true);
                long handle = field.getLong(process);
                NtDll ntDll = NtDll.INSTANCE;
                Win32Api.HANDLE h = new Win32Api.HANDLE();
                h.setPointer(Pointer.createConstant(handle));
                ntDll.NtSuspendProcess(h);
            } catch (Throwable ignored) {
                ignored.printStackTrace();
            }
        } else if (SystemUtils.IS_OS_LINUX) {
            try {
                Runtime.getRuntime().exec(String.format(LINUX_SUSPEND, getPID(process)));
            } catch (IOException ignored) { }
        } else {
            throw new UnsupportedOperationException();
        }
    }

    public static void resume(Process process) {
        if (SystemUtils.IS_OS_WINDOWS) {
            try {
                Field field = process.getClass().getDeclaredField("handle");
                field.setAccessible(true);
                long handle = field.getLong(process);
                NtDll ntDll = NtDll.INSTANCE;
                Win32Api.HANDLE h = new Win32Api.HANDLE();
                h.setPointer(Pointer.createConstant(handle));
                ntDll.NtResumeProcess(h);
            } catch (Throwable ignored) { }
        } else if (SystemUtils.IS_OS_LINUX) {
            try {
                Runtime.getRuntime().exec(String.format(LINUX_RESUME, getPID(process)));
            } catch (IOException ignored) { }
        } else {
            throw new UnsupportedOperationException();
        }
    }

    public static void terminate(Process process) {
        int pid = getPID(process);
        if (SystemUtils.IS_OS_WINDOWS) {
            try {
                Runtime.getRuntime().exec(String.format(WIN_KILL, pid));
            } catch (IOException ignored) { }
        } else if (SystemUtils.IS_OS_LINUX) {
            try {
                Runtime.getRuntime().exec(String.format(LINUX_KILL, pid));
            } catch (IOException ignored) { }
        } else {
            throw new UnsupportedOperationException();
        }
    }

}
