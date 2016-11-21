package com.github.jeppeter.extargsparse4j;

import com.sun.jna.Library;
import com.sun.jna.Native;

import java.util.Map;
import java.lang.reflect.Field;


public class Environ {
    public interface Kernel32 extends Library {
        // FREQUENCY is expressed in hertz and ranges from 37 to 32767
        // DURATION is expressed in milliseconds
        public boolean SetEnvironmentVariable(String lpName, String lpValue);
        public int GetEnvironmentVariable(String lpName, char[] lpBuffer, int nSize);
        public String GetEnvironmentStrings();
    }

    public interface LibC extends Library {
        public int setenv(String name, String value, int overwrite);
        public int unsetenv(String name);
        public String getenv(String name);
    }

    static private LibC m_libc;
    static private Kernel32 m_kernel32;

    static {
        String osname;
        try {
            m_libc = null;
            m_kernel32 = null;
            osname = System.getProperty("os.name").toLowerCase();
            if (osname.startsWith("linux")) {
                m_libc = (LibC) Native.loadLibrary("c", LibC.class);
            } else if (osname.startsWith("windows")) {
                ClassLoader clsloader = Environ.class.getClassLoader();
                Class ldcls;
                Field fld;
                Object obj;
                ldcls = clsloader.loadClass("com.sun.jna.win32.W32APIOptions");
                fld = ldcls.getDeclaredField("DEFAULT_OPTIONS");
                obj = fld.get(ldcls);
                m_kernel32 = (Kernel32) Native.loadLibrary("kernel32", Kernel32.class, (Map)obj);
            } else if (osname.startsWith("mac os x")) {
                m_libc = (LibC) Native.loadLibrary("System.B",LibC.class);
            } else {
                System.err.println(String.format("%s not supported", osname));
                System.exit(3);
            }
        } catch (Exception e) {
            System.err.printf("%s", e.toString());
            System.exit(4);
        }
    }


    public static int setenv(String key, String value) {
        if (m_kernel32 != null) {
            m_kernel32.SetEnvironmentVariable(key, value);
        } else if (m_libc != null) {
            m_libc.setenv(key, value, 1);
        }
        return 0;
    }
    public static int unsetenv(String key) {
        if (m_kernel32 != null) {
            m_kernel32.SetEnvironmentVariable(key, "");
        } else  if (m_libc != null) {
            m_libc.unsetenv(key);
        }
        return 0;
    }
    private static String _win_getenv(String key) {
        String retstr = "";
        char[] lpBuffer;
        int nSize;
        int i;
        int ret;
        nSize = 100;
        while (true) {
            lpBuffer = new char[nSize];
            ret = m_kernel32.GetEnvironmentVariable(key, lpBuffer, nSize);
            if (ret >= 0 && ret < nSize) {
                retstr = "";
                i = 0;
                while (i < nSize) {
                	if (lpBuffer[i] == '\0')  {
                		break;
                	}
                	retstr += String.valueOf(lpBuffer[i]);
                	i ++;
                }
                return retstr;
            }
            nSize <<= 1;
        }
    }

    private static String _linux_getenv(String key) {
        return m_libc.getenv(key);
    }

    public static String getenv(String key) {
        if (m_kernel32 != null) {
            return _win_getenv(key);
        } else if (m_libc != null) {
            return _linux_getenv(key);
        }
        return "";
    }
}
