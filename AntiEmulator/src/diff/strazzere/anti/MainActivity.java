package diff.strazzere.anti;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import diff.strazzere.anti.debugger.FindDebugger;
import diff.strazzere.anti.emulator.FindEmulator;
import diff.strazzere.anti.monkey.FindMonkey;
import diff.strazzere.anti.taint.FindTaint;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new Thread() {
            @Override
            public void run() {
                super.run();
                isTaintTrackingDetected();

                isMonkeyDetected();

                isDebugged();

                isQEmuEnvDetected();

            }
        }.start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public boolean isQEmuEnvDetected() {
        log("Checking for QEmu env...");
        log("hasKnownDeviceId : " + FindEmulator.hasKnownDeviceId(getApplicationContext()));
        log("hasKnownPhoneNumber : " + FindEmulator.hasKnownPhoneNumber(getApplicationContext()));
        log("isOperatorNameAndroid : " + FindEmulator.isOperatorNameAndroid(getApplicationContext()));
        log("hasKnownImsi : " + FindEmulator.hasKnownImsi(getApplicationContext()));
        log("hasEmulatorBuild : " + FindEmulator.hasEmulatorBuild(getApplicationContext()));
        log("hasPipes : " + FindEmulator.hasPipes());
        log("hasQEmuDriver : " + FindEmulator.hasQEmuDrivers());
        log("hasQEmuFiles : " + FindEmulator.hasQEmuFiles());
        log("hasGenyFiles : " + FindEmulator.hasGenyFiles());
        log("hasEmulatorAdb :" + FindEmulator.hasEmulatorAdb());
        for(String abi : Build.SUPPORTED_ABIS) {
            if (abi.equalsIgnoreCase("armeabi-v7a")) {
                log("hitsQemuBreakpoint : " + FindEmulator.checkQemuBreakpoint());
            }
        }
        if (FindEmulator.hasKnownDeviceId(getApplicationContext())
                        || FindEmulator.hasKnownImsi(getApplicationContext())
                        || FindEmulator.hasEmulatorBuild(getApplicationContext())
                        || FindEmulator.hasKnownPhoneNumber(getApplicationContext()) || FindEmulator.hasPipes()
                        || FindEmulator.hasQEmuDrivers() || FindEmulator.hasEmulatorAdb()
                        || FindEmulator.hasQEmuFiles()
                        || FindEmulator.hasGenyFiles()) {
            log("QEmu environment detected.");
            return true;
        } else {
            log("QEmu environment not detected.");
            return false;
        }
    }

    public boolean isTaintTrackingDetected() {
        log("Checking for Taint tracking...");
        log("hasAppAnalysisPackage : " + FindTaint.hasAppAnalysisPackage(getApplicationContext()));
        log("hasTaintClass : " + FindTaint.hasTaintClass());
        log("hasTaintMemberVariables : " + FindTaint.hasTaintMemberVariables());
        if (FindTaint.hasAppAnalysisPackage(getApplicationContext()) || FindTaint.hasTaintClass()
                        || FindTaint.hasTaintMemberVariables()) {
            log("Taint tracking was detected.");
            return true;
        } else {
            log("Taint tracking was not detected.");
            return false;
        }
    }

    public boolean isMonkeyDetected() {
        log("Checking for Monkey user...");
        log("isUserAMonkey : " + FindMonkey.isUserAMonkey());

        if (FindMonkey.isUserAMonkey()) {
            log("Monkey user was detected.");
            return true;
        } else {
            log("Monkey user was not detected.");
            return false;
        }
    }

    public boolean isDebugged() {
        log("Checking for debuggers...");

        boolean tracer = false;
        try {
            tracer = FindDebugger.hasTracerPid();
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        if (FindDebugger.isBeingDebugged() || tracer) {
            log("Debugger was detected");
            return true;
        } else {
            log("No debugger was detected.");
            return false;
        }
    }

    public void log(String msg) {
        Log.v("AntiEmulator", msg);
    }
}
