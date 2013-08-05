package diff.strazzere.anti.monkey;

import android.app.ActivityManager;

/**
 * Class used to determine functionality
 * specific to Monkey.
 * 
 * @author tstrazzere
 */
public class FindMonkey {

	/**
	 * Check if the normal method of "isUserAMonkey"
	 * returns a quick win of who the user is.
	 *  
	 * @return {@code true} if the user is a monkey
	 * 		or {@code false} if not.
	 */
	public static boolean isUserAMonkey() {
		return ActivityManager.isUserAMonkey();
	}
}
