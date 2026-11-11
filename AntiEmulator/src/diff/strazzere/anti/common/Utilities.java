package diff.strazzere.anti.common;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;

import android.content.Context;
import android.content.pm.PackageManager;

/**
 * Common functions used for detection of system fingerprints.
 * 
 * @author tstrazzere
 */
public class Utilities {

    /**
     * Method to reflectively invoke the SystemProperties.get command - which is the equivalent to the adb shell getProp
     * command.
     * 
     * @param context
     *            A {@link Context} object used to get the proper ClassLoader (just needs to be Application Context
     *            object)
     * @param property
     *            A {@code String} object for the property to retrieve.
     * @return {@code String} value of the property requested.
     */
    public static String getProp(Context context, String property) {
        try {
            ClassLoader classLoader = context.getClassLoader();
            Class<?> systemProperties = classLoader.loadClass("android.os.SystemProperties");

            Method get = systemProperties.getMethod("get", String.class);

            Object[] params = new Object[1];
            params[0] = new String(property);

            return (String) get.invoke(systemProperties, params);
        } catch (IllegalArgumentException iAE) {
            throw iAE;
        } catch (Exception exception) {
            throw null;
        }
    }

    /**
     * Reads (the start of) a file as text, for things like the {@code /proc} files.
     * <p>
     * Newer versions of Android deny apps access to a lot of these (for example {@code /proc/net/tcp} since Android
     * 10), so being unable to read a file is expected and not an error: it just means there is no evidence either way.
     *
     * @param path
     *            The file to read.
     * @param maxBytes
     *            The most to read, the rest of the file is ignored.
     * @return the contents, or {@code null} if the file does not exist or can not be read.
     */
    public static String readFile(String path, int maxBytes) {
        FileInputStream stream = null;
        try {
            stream = new FileInputStream(path);

            ByteArrayOutputStream contents = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            while (contents.size() < maxBytes) {
                int read = stream.read(buffer, 0, Math.min(buffer.length, maxBytes - contents.size()));
                if (read < 0) {
                    break;
                }
                contents.write(buffer, 0, read);
            }
            return new String(contents.toByteArray(), StandardCharsets.UTF_8);
        } catch (IOException exception) {
            return null;
        } catch (SecurityException exception) {
            return null;
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException exception) {
                    // Nothing useful to do
                }
            }
        }
    }

    /**
     * @return {@code true} if any of the paths exists. Paths that can't be seen by this app (which is a lot of them on
     *         newer versions of Android) simply don't exist as far as it is concerned.
     */
    public static boolean anyPathExists(String[] paths) {
        for (String path : paths) {
            if (new File(path).exists()) {
                return true;
            }
        }

        return false;
    }

    public static boolean hasPackageNameInstalled(Context context, String packageName) {
        PackageManager packageManager = context.getPackageManager();

        // In theory, if the package installer does not throw an exception, package exists
        try {
            packageManager.getInstallerPackageName(packageName);
            return true;
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }
}
