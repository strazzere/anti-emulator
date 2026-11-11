package diff.strazzere.anti.debugger;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class FindDebuggerTest {

    private static final String HEADER =
            "  sl  local_address rem_address   st tx_queue rx_queue tr tm->when retrnsmt   uid  timeout inode\n";

    /**
     * The /proc/net/tcp of a real device (a Nexus 5) with adb enabled over tcp, from issue #11. adbd listens on port
     * 5555 (15B3) and is connected to from 192.168.1.94, a client on the local network.
     */
    private static final String REAL_DEVICE_WITH_TCP_ADB = HEADER
            + "   0: 00000000:15B3 00000000:0000 0A 00000000:00000000 00:00000000 00000000  2000        0 106221 1 00000000 100 0 0 10 -1\n"
            + "   1: 2901A8C0:A572 33DB1CB8:01BB 01 00000000:00000000 00:00000000 00000000 10095        0 106752 1 00000000 22 4 32 10 -1\n"
            + "   2: 2901A8C0:15B3 5E01A8C0:889E 01 00000018:00000000 01:00000014 00000000  2000        0 109277 3 00000000 21 4 29 10 -1\n"
            + "   3: 2901A8C0:DAE7 2D00D9AC:01BB 08 00000000:0000026D 00:00000000 00000000 10022        0 24576 1 00000000 21 4 30 10 -1\n"
            + "   4: 2901A8C0:C6F4 D088592F:01BB 09 00000001:00000000 01:000007E2 00000007     0        0 0 2 00000000 3584 4 14 2 7\n"
            + "   5: 2901A8C0:9A34 3BDB1CB8:01BB 01 00000000:00000000 00:00000000 00000000 10095        0 109637 1 00000000 21 4 33 10 -1\n";

    /**
     * What an emulator looks like: adbd on 0.0.0.0:5555, and the host connecting through the emulator's network
     * (the guest is 10.0.2.15, the host appears as 10.0.2.2).
     */
    private static final String EMULATOR_WITH_ADB = HEADER
            + "   0: 00000000:15B3 00000000:0000 0A 00000000:00000000 00:00000000 00000000  2000        0 20871 1 00000000 100 0 0 10 -1\n"
            + "   1: 0F00020A:15B3 0202000A:C3E8 01 00000000:00000000 00:00000000 00000000  2000        0 21342 1 00000000 21 4 29 10 -1\n";

    @Test
    public void realDeviceUsingAdbOverTcpIsNotAnEmulator() {
        assertFalse(FindDebugger.isConnectedFromEmulatorHost(REAL_DEVICE_WITH_TCP_ADB));
    }

    @Test
    public void adbConnectedToFromTheEmulatorHostIsAnEmulator() {
        assertTrue(FindDebugger.isConnectedFromEmulatorHost(EMULATOR_WITH_ADB));
    }

    @Test
    public void listenerWithoutAnyConnectionIsNotAnEmulator() {
        String listenOnly = HEADER
                + "   0: 00000000:15B3 00000000:0000 0A 00000000:00000000 00:00000000 00000000  2000        0 20871 1 00000000 100 0 0 10 -1\n";

        assertFalse(FindDebugger.isConnectedFromEmulatorHost(listenOnly));
    }

    @Test
    public void connectionFromTheHostToSomeOtherPortIsNotAnEmulator() {
        // Established from 10.0.2.2, but to a port nothing is listening on
        String otherPort = HEADER
                + "   0: 00000000:15B3 00000000:0000 0A 00000000:00000000 00:00000000 00000000  2000        0 20871 1 00000000 100 0 0 10 -1\n"
                + "   1: 0F00020A:1F90 0202000A:C3E8 01 00000000:00000000 00:00000000 00000000  2000        0 21342 1 00000000 21 4 29 10 -1\n";

        assertFalse(FindDebugger.isConnectedFromEmulatorHost(otherPort));
    }

    @Test
    public void connectionThatIsNotEstablishedDoesNotCount() {
        // 08 is CLOSE_WAIT
        String closing = HEADER
                + "   0: 00000000:15B3 00000000:0000 0A 00000000:00000000 00:00000000 00000000  2000        0 20871 1 00000000 100 0 0 10 -1\n"
                + "   1: 0F00020A:15B3 0202000A:C3E8 08 00000000:00000000 00:00000000 00000000  2000        0 21342 1 00000000 21 4 29 10 -1\n";

        assertFalse(FindDebugger.isConnectedFromEmulatorHost(closing));
    }

    @Test
    public void unparsableInputIsNotAnEmulatorAndDoesNotThrow() {
        assertFalse(FindDebugger.isConnectedFromEmulatorHost(null));
        assertFalse(FindDebugger.isConnectedFromEmulatorHost(""));
        assertFalse(FindDebugger.isConnectedFromEmulatorHost(HEADER));
        assertFalse(FindDebugger.isConnectedFromEmulatorHost("not\nproc net tcp\nat all\n\n   \n"));
        assertFalse(FindDebugger.isConnectedFromEmulatorHost(HEADER + "   0: ZZZZZZZZ:15B3 00000000:0000 0A\n"));
    }

    @Test
    public void garbledLinesAreSkippedButTheRestIsStillChecked() {
        String mixed = HEADER + "   garbage line\n   1: nonsense\n" + EMULATOR_WITH_ADB.substring(HEADER.length());

        assertTrue(FindDebugger.isConnectedFromEmulatorHost(mixed));
    }
}
