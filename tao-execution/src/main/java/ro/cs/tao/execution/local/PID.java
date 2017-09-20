package ro.cs.tao.execution.local;

import com.sun.jna.Pointer;
import org.apache.commons.lang.SystemUtils;
import ro.cs.tao.execution.local.win.Kernel32;
import ro.cs.tao.execution.local.win.Win32Api;

import java.lang.reflect.Field;

/**
 * @author Cosmin Cara
 */
public final class PID {

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


}
