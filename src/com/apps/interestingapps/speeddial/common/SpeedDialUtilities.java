package com.apps.interestingapps.speeddial.common;

import java.util.List;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import com.apps.interestingapps.speeddialwidget.SpeedDialContact;

public class SpeedDialUtilities {

	/**
	 * Method to find out a SpeedDialContact from the list
	 * 
	 * @param list
	 * @param keyToSearch
	 * @return The index of SpeedDialContact in the list if found
	 */
	public static int binarySearch(List<SpeedDialContact> list,
			SpeedDialContact keyToSearch) {
		int low = 0;
		int high = list.size() - 1;

		while (low <= high) {
			int mid = (low + high) / 2;
			int compareResult = list.get(mid).compareTo(keyToSearch);
			if (compareResult == 0) {
				return mid;
			} else if (compareResult > 0) {
				high = mid - 1;
			} else {
				low = mid + 1;
			}
		}
		return -1;
	}
	
	/**
	 * Method to check if the free is installed on the device or not
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isFreeAppInstalled(Context context) {
		try {
			ApplicationInfo info = context.getPackageManager().getApplicationInfo(
					SpeedDialConstants.FREE_APP_PACKAGE, 0);
			return true;
		} catch (PackageManager.NameNotFoundException e) {
			return false;
		}
	}
}
