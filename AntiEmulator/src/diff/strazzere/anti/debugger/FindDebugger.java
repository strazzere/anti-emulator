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

    /** The state of a listening socket in /proc/net/tcp */
    private static final int TCP_LISTEN = 0x0A;
    /** The state of a connected socket in /proc/net/tcp */
    private static final int TCP_ESTABLISHED = 0x01;
    /** 10.0.2.2, the host as seen from inside the Android emulator's default (user mode) network, as in /proc/net/tcp */
    private static final long EMULATOR_HOST_ADDRESS = 0x0202000AL;

    /**
     * This was reversed from a sample someone was submitting to sandboxes for a thesis, can't find paper anymore
     * <p>
     * Looks for adb being connected to from the emulator's host. See {@link #isConnectedFromEmulatorHost(String)}.
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

        return isConnectedFromEmulatorHost(data);
    }

    /**
     * Checks the contents of {@code /proc/net/tcp} for a connection to a listening port (adb, which listens on
     * 0.0.0.0, when it is used over tcp) that comes from {@code 10.0.2.2}.
     * <p>
     * In the emulator the host is what connects to adb, and its connections always come from that address. This used
     * to flag any connection to the listening port, which also matched a real device being debugged with
     * {@code adb tcpip}, since a client on the local network connects the same way - just not from 10.0.2.2.
     *
     * @param procNetTcp
     *            The contents of {@code /proc/net/tcp}.
     * @return {@code true} if such a connection was found.
     */
    public static boolean isConnectedFromEmulatorHost(String procNetTcp) {
        if (procNetTcp == null) {
            return false;
        }

        ArrayList<tcp> sockets = new ArrayList<tcp>();
        // The first line is the column names
        String[] lines = procNetTcp.split("\n");
        for (int i = 1; i < lines.length; i++) {
            tcp socket = tcp.parse(lines[i]);
            if (socket != null) {
                sockets.add(socket);
            }
        }

        for (tcp listening : sockets) {
            // Adb is always bound to 0.0.0.0 - though the port can change
            if (listening.state != TCP_LISTEN || listening.localIp != 0) {
                continue;
            }

            for (tcp connection : sockets) {
                if (connection.state == TCP_ESTABLISHED && connection.localPort == listening.localPort
                                && connection.remoteIp == EMULATOR_HOST_ADDRESS) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * A single line of {@code /proc/net/tcp}, for example:
     * {@code 2: 2901A8C0:15B3 5E01A8C0:889E 01 00000018:00000000 01:00000014 00000000  2000 0 109277 ...}
     * where the addresses are {@code ip:port} in hex and the ip is in network byte order printed as a little endian
     * number (so 10.0.2.2 is 0202000A).
     */
    public static class tcp {

        public long localIp;
        public int localPort;
        public long remoteIp;
        public int remotePort;
        public int state;

        /**
         * @return the socket, or {@code null} if the line isn't in the expected format.
         */
        static tcp parse(String line) {
            // sl, local_address, rem_address, st, ...
            String[] fields = line.trim().split("\\s+");
            if (fields.length < 4) {
                return null;
            }

            try {
                String[] local = fields[1].split(":");
                String[] remote = fields[2].split(":");
                if (local.length != 2 || remote.length != 2) {
                    return null;
                }

                tcp socket = new tcp();
                socket.localIp = Long.parseLong(local[0], 16);
                socket.localPort = Integer.parseInt(local[1], 16);
                socket.remoteIp = Long.parseLong(remote[0], 16);
                socket.remotePort = Integer.parseInt(remote[1], 16);
                socket.state = Integer.parseInt(fields[3], 16);
                return socket;
            } catch (NumberFormatException exception) {
                return null;
            }
        }
    }
}
