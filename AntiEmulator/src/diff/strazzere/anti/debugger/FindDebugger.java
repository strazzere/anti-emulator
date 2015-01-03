package diff.strazzere.anti.debugger;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Class used to determine functionality specific to the Android debuggers
 * 
 * @author tstrazzere
 */
public class FindDebugger {

    /**
     * This was reversed from a sample someone was submitting to sandboxes for a thesis, can't find paper anymore
     */
    public static boolean hasAdbInEmulator() {
        boolean adbInEmulator = false;
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream("/proc/net/tcp")),
                            1000);
            String line;
            // Skip column names
            reader.readLine();

            ArrayList<tcp> tcpList = new ArrayList<tcp>();

            while ((line = reader.readLine()) != null) {
                tcpList.add(tcp.create(line.split("\\W+")));
            }

            reader.close();

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
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        return adbInEmulator;
    }

    public static class tcp {

        public int id;
        public int localIp;
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
            this.localIp = Integer.parseInt(localIp, 16);
            this.localPort = Integer.parseInt(localPort, 16);
        }
    }
}
