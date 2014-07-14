package com.apps.interestingapps.speeddial.common;

import java.util.List;

import com.apps.interestingapps.speeddial.SpeedDialContact;

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
}
