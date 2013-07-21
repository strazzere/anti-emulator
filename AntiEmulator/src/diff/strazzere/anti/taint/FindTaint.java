package diff.strazzere.anti.taint;

import java.io.FileDescriptor;
import java.lang.reflect.Field;

import android.content.Context;

import diff.strazzere.anti.common.Utilities;

/**
 * Class used to determine functionality
 * specific to Taintdroid.
 * 
 * @author tstrazzere
 */
public class FindTaint {
	
	/**
	 * Check if the "taint" java class used
	 *  by Taintdroid exists.
	 *  
	 * @return {@code true} if the Taintdroid class exists
	 * 		or {@code false} if not.
	 */
	public static boolean hasTaintClass() {
		try {
			Class.forName("dalvik.system.Taint");
			return true;
		}
		catch (ClassNotFoundException exception) {
			return false;
		}		
	}
	
	/**
	 * Check if specific member variables injected by
	 * Taintdroid exist, if any do, it is likely that
	 * Taintdroid is being used.
	 * 
	 * @return {@code true} if the Taintdroid member variables
	 * 		exist or {@code false} if not.
	 */
	@SuppressWarnings("unused")
	public static boolean hasTaintMemberVariables() {
		boolean taintDetected = false;
		Class<FileDescriptor> fileDescriptorClass = FileDescriptor.class;
		try {
			Field field = fileDescriptorClass.getField("name");
			taintDetected = true;
		} catch (NoSuchFieldException nsfe) {
			// This is normal - no need to do anything here, possibly add logging?
		}
		
		return taintDetected;
	}
	
	/**
	 * Check if the known Taintdroid application exists
	 * on the system.
	 * <P>
	 * Not very reliable and easy to have changed.
	 * 
	 * @param context A {link Context} object for the Android
	 * 			application.
	 * @return {@code true} if the package was found to
	 * 		exist or {@code false} if not.
	 */
	public static boolean hasAppAnalysisPackage(Context context) {
		return Utilities.hasPackageNameInstalled(context, "org.appanalysis");
	}
}
