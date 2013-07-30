package diff.strazzere.anti;

import diff.strazzere.anti.emulator.FindEmulator;
import diff.strazzere.anti.taint.FindTaint;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		isQEmuEnvDetected();

		isTaintTrackingDetected();
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
		log("hasKnownImei : " + FindEmulator.hasKnownImei(getApplicationContext()));
		log("hasKnownPhoneNumber : " + FindEmulator.hasKnownPhoneNumber(getApplicationContext()));
		log("hasPipes : " + FindEmulator.hasPipes());
		log("hasQEmuDriver : " + FindEmulator.hasQEmuDriver());
		log("hasQEmuFiles : " + FindEmulator.hasQEmuFiles());
		if(FindEmulator.hasKnownDeviceId(getApplicationContext()) ||
				FindEmulator.hasKnownImei(getApplicationContext()) ||
				FindEmulator.hasKnownPhoneNumber(getApplicationContext()) ||
				FindEmulator.hasPipes() ||
				FindEmulator.hasQEmuDriver() ||
				FindEmulator.hasQEmuFiles()) {
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
		if(FindTaint.hasAppAnalysisPackage(getApplicationContext()) ||
				FindTaint.hasTaintClass() ||
				FindTaint.hasTaintMemberVariables()) {
			log("Taint tracking was detected.");
			return true;
		} else {
			log("Taint tracking was not detected.");
			return false;
		}
	}

    public void log(String msg) {
    	Log.v("AntiEmulator", msg);
    }
}
