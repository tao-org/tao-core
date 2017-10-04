package ro.cs.tao.utils.executors.win;

import com.sun.jna.Native;

/**
 * @author Cosmin Cara
 */
public interface Kernel32 extends Win32Api {
    Kernel32 INSTANCE = (Kernel32) Native.loadLibrary("kernel32", Kernel32.class, DEFAULT_OPTIONS);
    /* http://msdn.microsoft.com/en-us/library/ms683179(VS.85).aspx */
    HANDLE GetCurrentProcess();
    /* http://msdn.microsoft.com/en-us/library/ms683215.aspx */
    int GetProcessId(HANDLE Process);
}
