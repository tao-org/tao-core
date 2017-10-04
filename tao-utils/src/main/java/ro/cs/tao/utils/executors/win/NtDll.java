package ro.cs.tao.utils.executors.win;

import com.sun.jna.Native;

/**
 * @author Cosmin Cara
 */
public interface NtDll extends Win32Api {
    NtDll INSTANCE = (NtDll) Native.loadLibrary("ntdll", NtDll.class, DEFAULT_OPTIONS);

    HANDLE NtSuspendProcess(HANDLE process);

    HANDLE NtResumeProcess(HANDLE process);
}
