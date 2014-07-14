package com.apps.interestingapps.speeddial.common;

import java.util.Comparator;

import com.apps.interestingapps.speeddial.SpeedDialContact;

/**
 * Class to implement comparison of SpeedDialContact on the basis of SpeedDialNumber
 */
public class SpeedDialNumberComparable implements Comparator<SpeedDialContact> {
	public int compare(SpeedDialContact lhs, SpeedDialContact rhs) {
		/*
		 * Give preference to lhs object if both are null
		 */
		if (lhs == null && rhs == null) {
			return 1;
		}

		/*
		 * Give preference to the object that is not null
		 */
		if (lhs == null) {
			return -1;
		}

		if (rhs == null) {
			return 1;
		}
		return lhs.getSpeedDialNumber() - rhs.getSpeedDialNumber();
	}
}