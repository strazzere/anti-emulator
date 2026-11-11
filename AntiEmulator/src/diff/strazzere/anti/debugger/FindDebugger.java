package diff.strazzere.anti.debugger;

import java.io.IOException;
import java.util.ArrayList;

import android.os.Debug;
import android.util.Log;

import diff.strazzere.anti.common.Utilities;

/**
 * Class used to determine functionality specific to the Android debuggers
 * 
 * @author tstrazzere
 */
public class FindDebugger {

    private static String tracerpid = "TracerPid";

    /**
     * Believe it or not, there are packers that use this...
     */
    public static boolean isBeingDebugged() {
        return Debug.isDebuggerConnected();
    }

    /**
     * This is used by Alibaba to detect someone ptracing the application.
     * 
     * Easy to circumvent, the usage ITW was a native thread constantly doing this every three seconds - and would cause
     * the application to crash if it was detected.
     * 
     * @return
     * @throws IOException
     */
    public static boolean hasTracerPid() throws IOException {
        String status = Utilities.readFile("/proc/self/status", 8192);
        if (status == null) {
            // Not readable, so no evidence
            return false;
        }

        for (String line : status.split("\n")) {
            if (line.length() > tracerpid.length() && line.regionMatches(true, 0, tracerpid, 0, tracerpid.length())) {
                try {
                    return Integer.decode(line.substring(tracerpid.length() + 1).trim()) > 0;
                } catch (NumberFormatException exception) {
                    return false;
                }
            }
        }
        return false;
    }

    /**
     * This was reversed from a sample someone was submitting to sandboxes for a thesis, can't find paper anymore
     * 
     * @throws IOException
     */
    public static boolean hasAdbInEmulator() throws IOException {
        // Newer versions of Android don't let apps read this, which means there is no evidence either way
        String data = Utilities.readFile("/proc/net/tcp", 1 << 20);
        if (data == null) {
            Log.v("AntiEmu:FindDebugger", "Unable to read /proc/net/tcp, not checking for adb");
            return false;
        }

        boolean adbInEmulator = false;
        try {
            String[] lines = data.split("\n");
            ArrayList<tcp> tcpList = new ArrayList<tcp>();

            // Skip column names
            for (int i = 1; i < lines.length; i++) {
                tcpList.add(tcp.create(lines[i].split("\\W+")));
            }

            // Adb is always bounce to 0.0.0.0 - though the port can change
            // real devices should be != 127.0.0.1
            int adbPort = -1;
            for (tcp tcpItem : tcpList) {
                if (tcpItem.localIp == 0) {
                    adbPort = tcpItem.localPort;
                    break;
                }
            }

            if (adbPort != -1) {
                for (tcp tcpItem : tcpList) {
                    if ((tcpItem.localIp != 0) && (tcpItem.localPort == adbPort)) {
                        adbInEmulator = true;
                    }
                }
            }
        } catch (RuntimeException exception) {
            Log.v("AntiEmu:FindDebugger", "Unable to parse /proc/net/tcp: " + exception);
        }

        return adbInEmulator;
    }

    public static class tcp {

        public int id;
        public long localIp;
        public int localPort;
        public int remoteIp;
        public int remotePort;

        static tcp create(String[] params) {
            return new tcp(params[1], params[2], params[3], params[4], params[5], params[6], params[7], params[8],
                            params[9], params[10], params[11], params[12], params[13], params[14]);
        }

        public tcp(String id, String localIp, String localPort, String remoteIp, String remotePort, String state,
                        String tx_queue, String rx_queue, String tr, String tm_when, String retrnsmt, String uid,
                        String timeout, String inode) {
            this.id = Integer.parseInt(id, 16);
            this.localIp = Long.parseLong(localIp, 16);
            this.localPort = Integer.parseInt(localPort, 16);
        }
    }
}
