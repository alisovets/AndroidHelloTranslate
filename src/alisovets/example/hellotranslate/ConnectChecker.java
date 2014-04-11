package alisovets.example.hellotranslate;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * 
 * @author Alexander Lisovets 
 *
 */
public class ConnectChecker {

	/**
	 * checks if a network connect is available
	 * @param context context
	 * @return true if a network connect is available
	 */
	public static boolean isNetworkAvailable(Context context) {
		ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

		if (connectivityManager == null) {
			return false;
		}

		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		if (activeNetworkInfo == null) {
			return false;
		}
		return activeNetworkInfo.isConnectedOrConnecting();
	}

}
