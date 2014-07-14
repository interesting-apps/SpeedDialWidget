package com.apps.interestingapps.speeddial;

import android.database.Cursor;
import android.util.Log;

import com.apps.interestingapps.speeddial.common.SpeedDialConstants;

/**
 * Class to represent a contact in the context of speed dial
 */
public class SpeedDialContact implements Comparable<SpeedDialContact> {

	private String name;
	private String phoneNumber;
	private int speedDialNumber;
	private long _id;

	private SpeedDialContact(long _id,
			String name,
			String phoneNumber,
			int speedDialNumber) {
		this._id = _id;
		this.name = name;
		this.phoneNumber = phoneNumber;
		this.speedDialNumber = speedDialNumber;
	}

	public long get_id() {
		return _id;
	}

	public void set_id(long _id) {
		this._id = _id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		if (!verifyCorrectPhoneNumberFormat(phoneNumber)) {
			throw new IllegalArgumentException(
					"The phone number is null or of zero length");
		}
		this.phoneNumber = phoneNumber;
	}

	public int getSpeedDialNumber() {
		return speedDialNumber;
	}

	public void setSpeedDialNumber(int speedDialNumber) {
		if (!verifyCorrectSpeedDialNumberFormat(speedDialNumber)) {
			throw new IllegalArgumentException(
					"The speed dial number is zero or negative");
		}
		this.speedDialNumber = speedDialNumber;
	}

	/**
	 * Method to get a new instance of SpeedDialContact class. A new object will
	 * only be created, when a phone number and speed dial number are provided in
	 * a proper format
	 * 
	 * @param name
	 * @param phoneNumber
	 * @param speedDialNumber
	 * @return
	 * @throws IllegalArgumentException
	 */
	public static SpeedDialContact newInstance(long _id,
			String name,
			String phoneNumber,
			int speedDialNumber) {
		if (!verifyCorrectPhoneNumberFormat(phoneNumber)) {
			throw new IllegalArgumentException(
					"The phone number is null or of zero length");
		}
		if (!verifyCorrectSpeedDialNumberFormat(speedDialNumber)) {
			throw new IllegalArgumentException(
					"The speed dial number is zero or negative");
		}
		return new SpeedDialContact(_id, name, phoneNumber, speedDialNumber);
	}

	/**
	 * Method to verify that the given phone number is in the correct format or
	 * not
	 * 
	 * TODO: Add more checks once it is confirmed what types of values can be
	 * returned from Contacts cursor
	 * 
	 * @param phoneNumber
	 * @return
	 */
	private static boolean verifyCorrectPhoneNumberFormat(String phoneNumber) {
		return !(phoneNumber == null || phoneNumber.length() == 0);
	}

	/**
	 * Method to verify that the given speed dial number is in the correct format
	 * or not
	 * 
	 * @param phoneNumber
	 * @return
	 */
	private static boolean
			verifyCorrectSpeedDialNumberFormat(int speedDialNumber) {
		return !(speedDialNumber <= 0);
	}

	/**
	 * Generate a new object of SpeedDialContact from the given cursor pointing
	 * to the row, from which the SpeedDialContact has to be created
	 * 
	 * @param cursor
	 *           Cursor pointing to the record that has to be converted into
	 *           SpeedDialContact object
	 * @return The SpeedDialContact object having values of the record being
	 *         pointed by the cursor
	 */
	public static SpeedDialContact newInstance(Cursor cursor) {
		long _id = cursor.getLong(cursor
				.getColumnIndex(SpeedDialConstants.ID_COLUMN));
		String contactName = cursor.getString(cursor
				.getColumnIndex(SpeedDialConstants.NAME_COLUMN));
		String phoneNumber = cursor.getString(cursor
				.getColumnIndex(SpeedDialConstants.PHONE_NUMBER_COLUMN));
		int speedDialNumber = cursor.getInt(cursor
				.getColumnIndex(SpeedDialConstants.SPEED_DIAL_NUMBER_COLUMN));
		return newInstance(_id, contactName, phoneNumber, speedDialNumber);
	}

	public String toString() {
		return "SpeedDialContact:\n" + "Id:" + _id + "\nName:" + name
				+ "\nPhone_Number:" + phoneNumber + "\nSpeed_Dial_Number:"
				+ speedDialNumber;
	}

	public int compareTo(SpeedDialContact rhs) {
		/*
		 * Give preference to this object if rhs null
		 */
		if (rhs == null) {
			return 1;
		}

		return this.getSpeedDialNumber() - rhs.getSpeedDialNumber();
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof SpeedDialContact)) {
			return false;
		}
		SpeedDialContact contactToCompare = (SpeedDialContact) o;
		boolean result = contactToCompare.get_id() == _id;
		Log.i("SpeedDialContact", "The compared objects are equal = " + result);
		return result;
	}
}
