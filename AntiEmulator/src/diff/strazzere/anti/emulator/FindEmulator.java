package diff.strazzere.anti.emulator;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import android.content.Context;

import diff.strazzere.anti.common.Property;
import diff.strazzere.anti.common.Utilities;

/**
 * Class used to determine functionality
 * specific to the Android QEmu.
 * 
 * @author tstrazzere
 */
public class FindEmulator {
	
	// Need to check the format of these
	private static String[] known_numbers = {
		"15555215554" // Default emulator phone number
	};
	
	private static String[] known_imei = {
		"012345678912345" // Default emulator imei
	};
	
	private static String[] known_device_id = {
		"000000000000000" // Default emulator id
	};
	
	private static String[] known_pipes = {
		"/dev/socket/qemud",
		"/dev/qemu_pipe"
	};
	
	private static String[] known_files = {
		"/system/lib/libc_malloc_debug_qemu.so",
		"/sys/qemu_trace", 
		"/system/bin/qemu-props"
	};
	
	private static String[] known_qemu_drivers = {
		"goldfish"
	};
	
	/**
	 * Known props, in the format of [property name, value to seek]
	 * if value to seek is null, then it is assumed that the existence
	 * of this property (anything not null) indicates the QEmu environment.
	 */
	private static Property[] known_props = {
		new Property( "init.svc.qemud", null ),
		new Property( "init.svc.qemu-props", null ),
		new Property( "qemu.hw.mainkeys", null ),
		new Property( "qemu.sf.fake_camera", null ),
		new Property( "qemu.sf.lcd_density", null ),
		new Property( "ro.bootloader", "unknown" ),
		new Property( "ro.bootmode", "unknown" ),
		new Property( "ro.hardware", "goldfish" ),
		new Property( "ro.kernel.android.qemud", null ),
		new Property( "ro.kernel.qemu.gles", null ),
		new Property( "ro.kernel.qemu", "1" ),
		new Property( "ro.product.device", "generic" ),
		new Property( "ro.product.model", "sdk" ),
		new Property( "ro.product.name", "sdk" ),
		 // Need to double check that an "empty" string ("") returns null
		new Property( "ro.serialno", null)
	};
	
	/**
	 * The "known" props have the potential for false-positiving due to
	 * interesting (see: poorly) made Chinese devices/odd ROMs. Keeping
	 * this threshold low will result in better QEmu detection with possible
	 * side affects.
	 */
	private static int MIN_PROPERTIES_THRESHOLD = 0x5;
	
	/**
	 * Check the existence of known pipes used
	 * by the Android QEmu environment.
	 * 
	 * @return {@code true} if any pipes where found to
	 * 		exist or {@code false} if not.
	 */
	public static boolean hasPipes() {
		for(String pipe : known_pipes) {
	        File qemu_socket = new File(pipe);
			if (qemu_socket.exists())
				return true;
		}
		
		return false;
	}

	/**
	 * Will query specific system properties to try and
	 * fingerprint a QEmu environment. A minimum threshold
	 * must be met in order to prevent false positives.
	 * 
	 * @param context A {link Context} object for the Android
	 * 			application.
	 * @return {@code true} if enough properties where found to
	 * 		exist or {@code false} if not.
	 */
	public boolean hasQEmuProps(Context context) {
		int found_props = 0;
		
		for(Property property : known_props) {
			String property_value = Utilities.getProp(context, property.name);
			// See if we expected just a non-null
			if(property.seek_value == null && property_value != null)
				found_props++;
			// See if we expected a value to seek
			if(property.seek_value != null && property_value.indexOf(property.seek_value) != -1)
				found_props++;
				
		}
		
		if(found_props >= MIN_PROPERTIES_THRESHOLD)
			return true;
		
		return false;
	}
	
	/**
	 * Check the existence of known files used
	 * by the Android QEmu environment.
	 * 
	 * @return {@code true} if any files where found to
	 * 		exist or {@code false} if not.
	 */
	public static boolean hasQEmuFiles() {
		for(String pipe : known_files) {
	        File qemu_file = new File(pipe);
			if (qemu_file.exists())
				return true;
		}
		
		return false;
	}
	
	/**
	 * Reads in the driver file, then checks a list for
	 * known QEmu drivers.
	 * 
	 * @return {@code true} if any known drivers where
	 * 		found to exist or {@code false} if not.
	 */
	public static boolean hasQEmuDriver() {
		File drivers_file = new File("/proc/tty/drivers");
		if(drivers_file.exists() && drivers_file.canRead()) {
			byte[] data =  new byte[(int) drivers_file.length()];
			try {
				InputStream is = new FileInputStream(drivers_file);
				is.read(data);
				is.close();
			} catch (Exception exception) {
				exception.printStackTrace();
			}
			
			String driver_data = new String(data);
			for(String known_qemu_driver : FindEmulator.known_qemu_drivers) {
				if(driver_data.indexOf(known_qemu_driver) != -1)
					return true;
			}
		}
		
		return false;
	}
	
	public static boolean hasKnownPhoneNumber() {
		return false;
	}
	
	public static boolean hasKnownImei() {
		return false;
	}
	
	public static boolean hasKnownDeviceId() {
		return false;
	}
}
