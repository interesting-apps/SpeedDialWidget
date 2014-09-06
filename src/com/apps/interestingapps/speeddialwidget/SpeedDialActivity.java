package com.apps.interestingapps.speeddialwidget;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.appwidget.AppWidgetManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import com.apps.interestingapps.speeddial.common.ContactDisplayAdapter;
import com.apps.interestingapps.speeddial.common.DatabaseHelper;
import com.apps.interestingapps.speeddial.common.Operation;
import com.apps.interestingapps.speeddial.common.PhoneNumberDialogAdapter;
import com.apps.interestingapps.speeddial.common.SpeedDialConstants;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class SpeedDialActivity extends Activity {

	private int selectedItemNumber = -1;
	private List<String> allPhoneNumbers = null;
	private String contactName = null;
	private int speedDialNumber = -1;
	private DatabaseHelper databaseHelper;
	private ArrayAdapter<SpeedDialContact> contactsAdapter;
	private int currentSpeedDialNumber = 4;
	private boolean speedDialNumberPresent = false, phoneNumberPresent = false;
	private SpeedDialContact contactSelectedForEdit = null;
	private Operation currentOperation = Operation.CREATE_NEW_COTNACT;
	private String dialedNumberDoesNotExist = null;
	private AdView adview;
	private EditText dialNumberText;
	private boolean startedFromWidget = false;
	private boolean startedToSaveContact = false;
	private boolean anotherActivityStarted = false;
	private boolean isPickContactCalled = false;
	private final String TAG = "SpeedDialActivity";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent intentStratingThisActivity = getIntent();
		startedFromWidget = intentStratingThisActivity.getBooleanExtra(
				SpeedDialConstants.STARTED_FROM_WIDGET, false);
		startedToSaveContact = intentStratingThisActivity.getBooleanExtra(
				SpeedDialConstants.STARTED_FROM_WIDGET_SAVE_CONTACT, false);

		SharedPreferences prefs = getSharedPreferences(
				SpeedDialConstants.PREFERENCES_FILE_NAME, 0);
		String numberToDial = "";
		if (startedFromWidget) {
			setTheme(android.R.style.Theme_NoDisplay);
			numberToDial = prefs.getString(SpeedDialConstants.NEW_TEXT, "");
			/*
			 * If the preferences contains default values, do make a call.
			 */
			if (numberToDial.equals("")
					|| numberToDial.contains(getResources().getString(
							R.string.speed_dial_number_hash_string))) {
				finish();
				return;
			}
		}
		databaseHelper = DatabaseHelper.getInstance(this);

		/*
		 * Open the database in Read mode. This is used to call onCreate method.
		 */
		// databaseHelper.getReadableDatabase();
		try {
			databaseHelper.createDataBase();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			databaseHelper.openDatabase();
			Log.i(this.getLocalClassName(), "Database opened");
		} catch (SQLException sqle) {
			throw sqle;
		}

		/*
		 * Do not show the theme if activity is started from widget
		 */
		if (startedFromWidget) {
			makeCall(numberToDial, true);
			finish();
			return;
		} else {
			setContentView(R.layout.contact_display_view);

			ListView listView = (ListView) findViewById(R.id.contactList);
			listView.setCacheColorHint(getResources().getColor(R.color.Black));
			final List<SpeedDialContact> allContactsList = databaseHelper
					.getAllValues();
			TextView emptySpeedDialNumberList = (TextView) findViewById(R.id.emptySpeedDialNumberList);
			if (allContactsList.size() == 0) {
				Log.i(this.getLocalClassName(), "List is 0 at onCreate");
				emptySpeedDialNumberList.setVisibility(View.VISIBLE);
			} else {
				emptySpeedDialNumberList.setVisibility(View.GONE);
			}
			Collections.sort(allContactsList);
			Log.i(this.getLocalClassName(), "Total records: "
					+ allContactsList.size());
			contactsAdapter = new ContactDisplayAdapter(this, allContactsList);
			listView.setAdapter(contactsAdapter);
			listView.setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick(AdapterView<?> arg0,
						View arg1,
						int itemNumber,
						long arg3) {
					makeCall(allContactsList.get(itemNumber).getPhoneNumber(), false);
				}
			});

			registerForContextMenu(listView);

			dialNumberText = (EditText) findViewById(R.id.dialNumberText);

			ImageView makeCallImage = (ImageView) findViewById(R.id.makeCallImage);
			makeCallImage.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					makeCall(dialNumberText, true);
				}
			});

			/*
			 * Android 4.0 device id: 64FFE02AABF389054771188E3CF39B63
			 *
			 * Sony Xperia X10 device id: 080A4A2357E9089FDAB344624A7181F5
			 *
			 * Nexus 4 - Varun's - device id: 7A107DF0AB377695D8973481767E5A76
			 */
			adview = (AdView) findViewById(R.id.adView);

			/*
			 * TODO: Uncomment it while running tests. Comment this part while
			 * creating APK for production
			 */
			// AdRequest adRequest = new
			// AdRequest.Builder().addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
			// .addTestDevice("080A4A2357E9089FDAB344624A7181F5")
			// .build();

			AdRequest adRequest = new AdRequest.Builder().build();
			adview.loadAd(adRequest);

			final View activityRootView = findViewById(R.id.activityRoot);
			activityRootView.getViewTreeObserver()
					.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
						public void onGlobalLayout() {
							int heightDiff = activityRootView.getRootView()
									.getHeight() - activityRootView.getHeight();
							if (heightDiff > 100) { // if more than 100 pixels, its
															// probably a keyboard...
								adview.setVisibility(View.GONE);
							} else {
								adview.setVisibility(View.VISIBLE);
							}
						}
					});

			if (startedToSaveContact) {
				String speedDialNumberToSave = intentStratingThisActivity
						.getStringExtra(SpeedDialConstants.SPEED_DIAL_NUMBER_TO_SAVE);
				showSpeedDialNumberNotExistAlert(speedDialNumberToSave);
			} else {
				showRateDialog();
			}
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (databaseHelper != null && !anotherActivityStarted) {
			databaseHelper.closeDatabase();
			databaseHelper.close();
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		if (!startedFromWidget) {
			showSoftInput(dialNumberText, this.getWindow());
			dialNumberText.requestFocus();
		}
	}

	@Override
	public void onStop() {
		super.onStop();
		if (!isPickContactCalled) {
			finish();
			return;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.options_menu_view, menu);
		return true;
	}

	private void pickContact() {
		Intent pickContactIntent = new Intent(Intent.ACTION_PICK,
				ContactsContract.Contacts.CONTENT_URI);

		/*
		 * Show user only contacts w/ phone numbers
		 */
		pickContactIntent.setType(ContactsContract.Contacts.CONTENT_TYPE);
		isPickContactCalled = true;
		if (pickContactIntent.resolveActivity(getPackageManager()) != null) {
			startActivityForResult(pickContactIntent,
					SpeedDialConstants.PICK_CONTACT_REQUEST);
		} else {
			Toast.makeText(this,
					"Sorry, no app to provide contacts list is currently available.",
					Toast.LENGTH_LONG)
					.show();
		}
	}

	/**
	 * Method to get the results of the activity and perform actions according to
	 * the requestCode
	 */
	@Override
	protected void
			onActivityResult(int requestCode, int resultCode, Intent data) {
		// Check which request it is that we're responding to

		switch (requestCode) {
		case SpeedDialConstants.PICK_CONTACT_REQUEST:
			if (resultCode == Activity.RESULT_OK) {
				Uri contactData = data.getData();
				Cursor selectedContactCursor = managedQuery(contactData, null,
						null, null, null);
				startManagingCursor(selectedContactCursor);
				allPhoneNumbers = retrieveAllPhoneNumbers(selectedContactCursor);
				if (allPhoneNumbers.size() == 1) {
					// Get the speed dial number from the user if its a new contact
					if (currentOperation == Operation.CREATE_NEW_COTNACT) {
						askUserToEnterSpeedDialNumber(0);
					} else {
						saveSelectedPhoneNumber(0, contactSelectedForEdit
								.getSpeedDialNumber());
						if (phoneNumberPresent) {
							showOverrideDialog(contactSelectedForEdit, 0,
									contactSelectedForEdit.getSpeedDialNumber());
						} else {
							resetAllFields();
						}
					}
				} else if (allPhoneNumbers.size() > 1) {
					askUserToSelectPhoneNumber(allPhoneNumbers);
				} else {
					Log.i(this.getLocalClassName(),
							"No phone numbers found for the contact");
					Toast.makeText(
							getApplicationContext(),
							"The selected contact does not have any phone numbers associated with it",
							Toast.LENGTH_LONG).show();
					resetAllFields();
				}
			}
			break;
		default:
			Log.e(this.getLocalClassName(),
					"Unexpected request code returned by activity");
		}
	}

	/**
	 * Method to retrieve all the phone numbers from a selected contact
	 *
	 * @param selectedContactCursor
	 * @return
	 */
	private List<String> retrieveAllPhoneNumbers(Cursor selectedContactCursor) {
		List<String> allPhoneNumbers = new ArrayList<String>();

		if (selectedContactCursor.moveToFirst()) {
			String id = selectedContactCursor.getString(selectedContactCursor
					.getColumnIndexOrThrow(ContactsContract.Contacts._ID));
			String hasPhone = selectedContactCursor
					.getString(selectedContactCursor
							.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));

			if (hasPhone.equalsIgnoreCase("1")) {
				Cursor contactCursor = getContentResolver().query(
						ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
						null,
						ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = "
								+ id, null, null);
				Log.i(this.getLocalClassName(), "Total phone numbers: "
						+ contactCursor.getCount());
				while (contactCursor.moveToNext()) {
					String phoneNumber = contactCursor.getString(contactCursor
							.getColumnIndex("data1"));
					allPhoneNumbers.add(phoneNumber);
				}
				contactCursor.close();
			}

			contactName = selectedContactCursor.getString(selectedContactCursor
					.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
		}

		return allPhoneNumbers;
	}

	/**
	 * A method to create a dialog to ask the use to select 1 number out of many
	 * phone numbers a contact may have
	 *
	 * @param phoneNumbers
	 * @return
	 */
	private void askUserToSelectPhoneNumber(List<String> phoneNumbers) {
		AlertDialog.Builder builder = new AlertDialog.Builder(
				SpeedDialActivity.this);
		builder.setTitle(R.string.title_pick_phone_number);
		final String[] phoneNumberArray = phoneNumbers.toArray(new String[0]);
		ArrayAdapter<String> adapter = new PhoneNumberDialogAdapter(this,
				phoneNumberArray);
		PhoneNumberDilaogItemListener dialogListener = new PhoneNumberDilaogItemListener();
		builder.setAdapter(adapter, dialogListener);
		AlertDialog alert = builder.create();

		alert.setButton(getResources().getString(R.string.cancel_button_string),
				new CancelDilaogButtonListener());
		alert.setOnCancelListener(new CancelDialogListener());
		alert.show();
	}

	/**
	 * Class to implement the onClick action to store the list item number
	 * selected by user on Select Phone number dialog
	 */
	private class PhoneNumberDilaogItemListener implements
			DialogInterface.OnClickListener {
		public void onClick(DialogInterface dialog, int item) {
			dialog.dismiss();
			/*
			 * Get the speed dial number from the user if a new contact is being
			 * created
			 */
			if (currentOperation == Operation.CREATE_NEW_COTNACT) {
				askUserToEnterSpeedDialNumber(item);
			} else {
				saveSelectedPhoneNumber(item, contactSelectedForEdit
						.getSpeedDialNumber());
				resetAllFields();
			}
		}
	}

	/**
	 * Class to implement the onClick action when the Cancel button on the select
	 * phone number dialog is pressed
	 */
	private class CancelDilaogButtonListener implements
			DialogInterface.OnClickListener {
		public void onClick(DialogInterface dialog, int id) {
			/*
			 * if this button is clicked, just close the dialog box and do nothing
			 */
			resetAllFields();
			dialog.cancel();
		}
	}

	/**
	 * Save the selected number in database. Checks if the number is already
	 * associated to a speed dial number, or the selected speed dial number
	 * already has another phone number associated to it
	 *
	 * @param selectedPhoneNumberIndex
	 *           The index representing the phone number that the use wants to
	 *           set the current speed dial entry for
	 * @param newSpeedDialNumber
	 *           The speedDialNumber selected by the user
	 * @return The new SpeedDialContact object if a new record is created, null
	 *         otherwise
	 */
	private SpeedDialContact
			saveSelectedPhoneNumber(int selectedPhoneNumberIndex,
					int newSpeedDialNumber) {
		SpeedDialContact newContact = null;
		boolean isOperationSuccessful = true;
		if (isDataValidForAdding(selectedPhoneNumberIndex, newSpeedDialNumber)) {
			String newPhoneNumber = allPhoneNumbers.get(selectedPhoneNumberIndex);
			switch (currentOperation) {
			case CREATE_NEW_COTNACT:
				try {
					newContact = addEntry(contactName, selectedPhoneNumberIndex,
							newSpeedDialNumber);
				} catch (Exception e) {
					isOperationSuccessful = false;
					e.printStackTrace();
				}
				break;
			case EDIT_SPEED_DIAL_NUMBER:
				if (databaseHelper.updateSpeedDialNumber(contactSelectedForEdit
						.getSpeedDialNumber(), newSpeedDialNumber)) {
					contactSelectedForEdit.setSpeedDialNumber(newSpeedDialNumber);
					Log.i(this.getLocalClassName(),
							"Successfully updated the Speed Dial number for contact "
									+ contactSelectedForEdit.getName() + " to "
									+ newSpeedDialNumber);
					newContact = contactSelectedForEdit;
				} else {
					Log.e(this.getLocalClassName(),
							"Error while updating speed dial for contact "
									+ contactSelectedForEdit.getName() + " to "
									+ newSpeedDialNumber);
					isOperationSuccessful = false;
				}
				break;
			case EDIT_PHONE_NUMBER:
				if (databaseHelper.updatePhoneNumber(contactSelectedForEdit
						.getPhoneNumber(), newPhoneNumber, contactName)) {
					contactSelectedForEdit.setPhoneNumber(newPhoneNumber);
					contactSelectedForEdit.setName(contactName);
					Log.i(this.getLocalClassName(),
							"Successfully updated the Phone number for contact "
									+ contactSelectedForEdit.getName() + " to "
									+ newPhoneNumber);
					newContact = contactSelectedForEdit;
				} else {
					Log.e(this.getLocalClassName(),
							"Error while updating phone number for contact for "
									+ contactSelectedForEdit.getName() + " to "
									+ newPhoneNumber);
					isOperationSuccessful = false;
				}
				break;
			default:
				Log.e(SpeedDialActivity.this.getLocalClassName(),
						"Wrong opertaion for save phone number:" + currentOperation);
			}
			if (isOperationSuccessful) {
				contactsAdapter.notifyDataSetChanged();
				TextView emptySpeedDialNumberList = (TextView) findViewById(R.id.emptySpeedDialNumberList);
				emptySpeedDialNumberList.setVisibility(View.GONE);
				allPhoneNumbers.clear();
			}
		}
		return newContact;
	}

	/**
	 * Performs a check whether the selected phone is associated to a speed dial
	 * number or if the selected speed dial number has a phone number associated
	 * to it
	 *
	 * @param selectedPhoneNumberIndex
	 * @param speedDialNumber
	 * @return true if the selected data satisfies the validity criteria
	 */
	private boolean isDataValidForAdding(int selectedPhoneNumberIndex,
			int speedDialNumber) {
		if (allPhoneNumbers == null) {
			Log.e(this.getLocalClassName(), "allPhoneNumbers is null");
			return false;
		}

		if (allPhoneNumbers.size() == 0) {
			Log.e(this.getLocalClassName(), "allPhoneNumbers size is 0");
			return false;
		}

		if (selectedPhoneNumberIndex < 0) {
			Log.e(this.getLocalClassName(),
					"Selected phone number Index is negative:"
							+ selectedPhoneNumberIndex);
			return false;
		}

		boolean result = true;
		/*
		 * Check if the current operation is not edit phone number, then the
		 * selected speed dial number exists or not. If the current operation is
		 * edit phone number, which means the given speed dial number already
		 * exists
		 */
		if (currentOperation != Operation.EDIT_PHONE_NUMBER
				&& databaseHelper.getRecordForSpeedDialNumber(speedDialNumber) != null) {
			Log.i(SpeedDialActivity.this.getLocalClassName(),
					"A Phone number is already associated with speed dial number "
							+ speedDialNumber + ". Choose another speed dial number");
			speedDialNumberPresent = true;
			result = false;
		}

		/*
		 * Check if the current operation is not edit speed dial number, then the
		 * selected speed dial number exists or not. If the current operation is
		 * edit speed dial number, which means the given phone number already
		 * exists
		 */
		if (currentOperation != Operation.EDIT_SPEED_DIAL_NUMBER
				&& databaseHelper.getRecordForPhoneNumber(allPhoneNumbers
						.get(selectedPhoneNumberIndex)) != null) {
			phoneNumberPresent = true;
			Log.i(this.getLocalClassName(), "Phone number "
					+ allPhoneNumbers.get(selectedPhoneNumberIndex)
					+ " is already associated with a speed dial number");
			result = false;
		}
		return result;
	}

	/**
	 * Method to handle the click events of menu items selected
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_add:
			currentOperation = Operation.CREATE_NEW_COTNACT;
			pickContact();
			return true;
		case 2:
			return true;
		case 3:
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void resetAllFields() {
		allPhoneNumbers = null;
		selectedItemNumber = -1;
		speedDialNumber = -1;
		contactName = null;
		speedDialNumberPresent = false;
		contactSelectedForEdit = null;
		currentOperation = Operation.CREATE_NEW_COTNACT;
		dialedNumberDoesNotExist = null;
		phoneNumberPresent = false;
		startedFromWidget = false;
		startedToSaveContact = false;
		anotherActivityStarted = false;
		isPickContactCalled = false;
	}

	/**
	 * Method to create a context menu when a list item pressed for long
	 */
	@Override
	public void onCreateContextMenu(ContextMenu menu,
			View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.contact_context_menu, menu);
	}

	/**
	 * Method to perform an action when an item on the context menu list is
	 * clicked
	 */
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		switch (item.getItemId()) {
		case R.id.deleteMenuItem:
			currentOperation = Operation.DELETE_SPEED_DIAL_CONTACT;
			SpeedDialContact selectedContact = contactsAdapter
					.getItem(info.position);
			try {
				deleteEntry(selectedContact);
			} catch (Exception e) {
				Toast.makeText(SpeedDialActivity.this, "Unable to delete record.",
						Toast.LENGTH_LONG).show();
			}
			return true;
		case R.id.editMenuItem:
			contactSelectedForEdit = contactsAdapter.getItem(info.position);
			/*
			 * Set the contactName and allPhoneNumbers list to current selected
			 * contact's details
			 */
			contactName = contactSelectedForEdit.getName();
			allPhoneNumbers = new ArrayList<String>();
			allPhoneNumbers.add(contactSelectedForEdit.getPhoneNumber());
			showEditOptionsRadioButtons();
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}

	/**
	 * Creates a new dialog to ask the use to enter the speed dial number for the
	 * selected phone number
	 *
	 * @param selectedPhoneNumberIndex
	 */
	private void
			askUserToEnterSpeedDialNumber(final int selectedPhoneNumberIndex) {

		final Dialog dialog = new Dialog(SpeedDialActivity.this);
		dialog.setTitle(R.string.title_select_speed_dial_number);
		LayoutInflater inflater = getLayoutInflater();

		final View dialogView = inflater.inflate(
				R.layout.select_speed_dial_number_dialog, null);
		dialog.setContentView(dialogView);

		TextView contactNameView = (TextView) dialogView
				.findViewById(R.id.sdn_dialog_contact_name);
		TextView phoneNumberView = (TextView) dialogView
				.findViewById(R.id.sdn_dialog_phone_number);
		final EditText speedDialNumberText = (EditText) dialogView
				.findViewById(R.id.sdn_dialog_speed_dial_number);

		contactNameView.setText(contactName);
		phoneNumberView.setText(allPhoneNumbers.get(selectedPhoneNumberIndex));

		if (dialedNumberDoesNotExist != null) {
			speedDialNumberText.setText(dialedNumberDoesNotExist);
		}

		showSoftInput(speedDialNumberText, dialog.getWindow());

		Button okButton = (Button) dialogView
				.findViewById(R.id.sdn_dialog_ok_button);
		okButton.setOnClickListener(new AssignSpeedDialNumberOkButtonListener(
				dialog, contactSelectedForEdit, speedDialNumberText,
				selectedPhoneNumberIndex));

		Button cancelButton = (Button) dialogView
				.findViewById(R.id.sdn_dialog_cancel_button);
		cancelButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				dialog.dismiss();
				resetAllFields();
			}
		});
		dialog.setOnCancelListener(new CancelDialogListener());
		dialog.show();
	}

	/**
	 * Implements listener for Ok Button for SelectSpeedDialNumberDialog
	 */
	private class AssignSpeedDialNumberOkButtonListener implements
			OnClickListener {
		private Dialog dialog;
		private EditText speedDialNumberText;
		private int selectedPhoneNumberIndex;
		final private SpeedDialContact contactForEdit;

		public AssignSpeedDialNumberOkButtonListener(Dialog dialog,
				SpeedDialContact cotactForEdit,
				EditText speedDialNumberText,
				int selectedPhoneNumberIndex) {
			this.dialog = dialog;
			this.speedDialNumberText = speedDialNumberText;
			this.selectedPhoneNumberIndex = selectedPhoneNumberIndex;
			this.contactForEdit = cotactForEdit;
		}

		public void onClick(View v) {
			final int enteredSpeedDialNumber;
			try {
				enteredSpeedDialNumber = Integer.parseInt(speedDialNumberText
						.getText().toString());
				if (enteredSpeedDialNumber < 1) {
					throw new Exception("Entered number is "
							+ enteredSpeedDialNumber);
				}
				SpeedDialContact newContact = saveSelectedPhoneNumber(
						selectedPhoneNumberIndex, enteredSpeedDialNumber);

				if (speedDialNumberPresent || phoneNumberPresent) {
					dialog.dismiss();
					showOverrideDialog(contactForEdit, selectedPhoneNumberIndex,
							enteredSpeedDialNumber);
				}

				if (newContact != null) {
					dialog.dismiss();
					if (currentOperation == Operation.CREATE_NEW_COTNACT) {
						Toast.makeText(
								SpeedDialActivity.this,
								"Successfully added an entry with speed dial number: "
										+ enteredSpeedDialNumber, Toast.LENGTH_SHORT)
								.show();
					}
					resetAllFields();
				}
			} catch (Exception e) {
				Toast.makeText(SpeedDialActivity.this,
						"Please enter a valid integer greater than 0.",
						Toast.LENGTH_LONG).show();
				e.printStackTrace();
			}
		}
	}

	/**
	 * Class to handle the click event of Edit radio buttons
	 *
	 * @param view
	 */
	private class EditRadioButtonClickListener implements
			DialogInterface.OnClickListener {
		public void onClick(DialogInterface dialog, int item) {
			String[] items = getResources()
					.getStringArray(R.array.edit_menu_items);
			if (items[item].equals(getResources().getString(
					R.string.edit_speed_dial_number_string))) {
				currentOperation = Operation.EDIT_SPEED_DIAL_NUMBER;
			} else if (items[item].equals(getResources().getString(
					R.string.edit_phone_number_string))) {
				currentOperation = Operation.EDIT_PHONE_NUMBER;
			}
			switch (currentOperation) {
			case EDIT_SPEED_DIAL_NUMBER:
				dialog.dismiss();
				askUserToEnterSpeedDialNumber(0);
				break;
			case EDIT_PHONE_NUMBER:
				dialog.dismiss();
				pickContact();
				break;
			case CREATE_NEW_COTNACT:
				dialog.dismiss();
				resetAllFields();
				break;
			default:
				Log.e(SpeedDialActivity.this.getLocalClassName(),
						"Wrong currentOperation: " + currentOperation);
				break;
			}
		}
	}

	public void showEditOptionsRadioButtons() {
		AlertDialog.Builder dialog = new AlertDialog.Builder(
				SpeedDialActivity.this);
		dialog.setTitle(R.string.title_edit_contact);

		dialog.setSingleChoiceItems(getResources().getStringArray(
				R.array.edit_menu_items), -1, new EditRadioButtonClickListener());
		dialog.setNegativeButton(getResources().getString(
				R.string.cancel_button_string), new CancelDilaogButtonListener());
		dialog.setOnCancelListener(new CancelDialogListener());
		dialog.create();
		dialog.show();
	}

	/**
	 * Method to make a call to give phone number
	 *
	 * @param number
	 * @param isSpeedDialNumber
	 *           true if the number that is passed is a speed dial number and not
	 *           a phone number
	 */
	private void makeCall(String number, boolean isSpeedDialNumber) {
		long numberToDial;
		String phoneNumber = null;
		/*
		 * TODO: Make a decision whether I should sanitize the phone number sent
		 * by removing all the extra characters other than numbers
		 */
		if (number.equals("")) {
			return;
		}
		try {
			numberToDial = Long.parseLong(number);
			if (numberToDial < 0) {
				Toast.makeText(SpeedDialActivity.this,
						"Please enter a positive number.", Toast.LENGTH_LONG).show();
				throw new Exception();
			}

			if (isSpeedDialNumber) {
				SpeedDialContact contactForSpeedDial = databaseHelper
						.getRecordForSpeedDialNumber(numberToDial);
				if (contactForSpeedDial != null) {
					phoneNumber = contactForSpeedDial.getPhoneNumber();
				} else {
					/*
					 * If the activity is started from launcher icon, ask the user to
					 * save or dial the number. Just dial the number entered if that
					 * does not exist
					 */
					if (!startedFromWidget) {
						showSpeedDialNumberNotExistAlert(number);
						return;
					} else {
						Intent speedDialActivityIntent = new Intent(
								SpeedDialActivity.this.getApplicationContext(),
								SpeedDialActivity.class);

						speedDialActivityIntent.setAction(getResources().getString(
								R.string.speed_dial_intent_filter_action));
						speedDialActivityIntent.putExtra(
								SpeedDialConstants.STARTED_FROM_WIDGET_SAVE_CONTACT,
								true);
						speedDialActivityIntent.putExtra(
								SpeedDialConstants.STARTED_FROM_WIDGET, false);
						speedDialActivityIntent.putExtra(
								SpeedDialConstants.SPEED_DIAL_NUMBER_TO_SAVE, number);
						startActivity(speedDialActivityIntent);

						resetRemoteViewsTextAndPreferences();
						anotherActivityStarted = true;
						return;
					}

				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		/*
		 * If the number is not parsed correctly, then try to dial the number
		 */
		if (phoneNumber == null) {
			phoneNumber = number;
		}
		try {
			Intent callIntent = new Intent(Intent.ACTION_CALL);
			callIntent.setData(Uri.parse("tel:" + phoneNumber));
			startActivity(callIntent);
		} catch (ActivityNotFoundException activityException) {
			Log.e(this.getClass().getName(), "Cannot make a call to: " + number,
					activityException);
		}

		if (startedFromWidget) {
			resetRemoteViewsTextAndPreferences();
		}

	}

	private void makeCall(EditText dialNumberText, boolean isSpeedDialNumber) {
		makeCall(dialNumberText.getText().toString(), true);
		dialNumberText.setText("");
	}

	private void showSpeedDialNumberNotExistAlert(final String number) {
		AlertDialog.Builder dialog = new AlertDialog.Builder(
				SpeedDialActivity.this);
		String[] messageStrings = getResources().getStringArray(
				R.array.message_speed_dial_number_not_exist);
		dialog.setMessage(messageStrings[0] + " " + number + " "
				+ messageStrings[1]);
		dialog.setPositiveButton(R.string.save_button_string,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialedNumberDoesNotExist = number;
						currentOperation = Operation.CREATE_NEW_COTNACT;
						pickContact();
					}
				});
		dialog.setNeutralButton(getResources().getString(
				R.string.dial_number_button_string),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						makeCall(number, false);
					}
				});
		dialog.setNegativeButton(getResources().getString(
				R.string.cancel_button_string), new CancelDilaogButtonListener());
		dialog.setOnCancelListener(new CancelDialogListener());
		dialog.create();
		dialog.show();
	}

	/**
	 * Class to implement the button listener of OK button on the Override speed
	 * dial number dialog
	 */
	private class OverrideYesButtonListener implements
			DialogInterface.OnClickListener {
		private SpeedDialContact contactForEdit;
		private int selectedPhoneNumberIndex;
		private int enteredSpeedDialNumber;

		public OverrideYesButtonListener(SpeedDialContact contactForEdit,
				int selectedPhoneNumberIndex,
				int enteredSpeedDialNumber) {
			this.contactForEdit = contactForEdit;
			this.selectedPhoneNumberIndex = selectedPhoneNumberIndex;
			this.enteredSpeedDialNumber = enteredSpeedDialNumber;
		}

		public void onClick(DialogInterface dialog, int which) {
			/*
			 * Delete the record that was changed
			 */
			try {
				SpeedDialContact existingContact = null;
				if (currentOperation == Operation.EDIT_PHONE_NUMBER
						|| currentOperation == Operation.EDIT_SPEED_DIAL_NUMBER) {
					deleteEntry(contactForEdit);
				}
				if (speedDialNumberPresent && phoneNumberPresent) {
					SpeedDialContact existingContactForSpeedDialNumber = databaseHelper
							.getRecordForSpeedDialNumber(enteredSpeedDialNumber);
					if (existingContactForSpeedDialNumber != null) {
						deleteEntry(existingContactForSpeedDialNumber);
					}
					SpeedDialContact existingContactForPhoneNumber = databaseHelper
							.getRecordForPhoneNumber(allPhoneNumbers
									.get(selectedPhoneNumberIndex));
					if (existingContactForPhoneNumber != null) {
						deleteEntry(existingContactForPhoneNumber);
					}
				} else if (speedDialNumberPresent) {
					existingContact = databaseHelper
							.getRecordForSpeedDialNumber(enteredSpeedDialNumber);
					if (existingContact != null) {
						deleteEntry(existingContact);
					}
				} else if (phoneNumberPresent) {
					existingContact = databaseHelper
							.getRecordForPhoneNumber(allPhoneNumbers
									.get(selectedPhoneNumberIndex));
					if (existingContact != null) {
						deleteEntry(existingContact);
					}
				}
				addEntry(contactName, selectedPhoneNumberIndex,
						enteredSpeedDialNumber);

				if (currentOperation == Operation.CREATE_NEW_COTNACT) {
					Toast.makeText(
							SpeedDialActivity.this,
							"Successfully added an entry with speed dial number: "
									+ enteredSpeedDialNumber, Toast.LENGTH_SHORT).show();
				}
			} catch (Exception e) {
				Toast.makeText(SpeedDialActivity.this, "Error in editing record.",
						Toast.LENGTH_LONG).show();
				e.printStackTrace();
			}
			resetAllFields();
		}
	}

	/**
	 * Method to delete a record from database and the contactsAdapter
	 *
	 * @param contactToDelete
	 * @throws Exception
	 */
	private void deleteEntry(SpeedDialContact contactToDelete) throws Exception {
		if (!databaseHelper.deleteRecord(contactToDelete.get_id())) {
			throw new Exception("Unable to delete the record with Id: "
					+ contactToDelete.get_id() + " and name: "
					+ contactToDelete.getName());
		} else {
			Log.i(this.getLocalClassName(),
					"Successfully deleted the record with Id "
							+ contactToDelete.get_id());
		}
		contactsAdapter.remove(contactToDelete);
		contactsAdapter.notifyDataSetChanged();
		if (contactsAdapter.isEmpty()
				&& currentOperation == Operation.DELETE_SPEED_DIAL_CONTACT) {
			TextView emptySpeedDialNumberList = (TextView) findViewById(R.id.emptySpeedDialNumberList);
			Log.i(this.getLocalClassName(), "List is 0 after delete");
			emptySpeedDialNumberList.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * Method to add an entry to database and contactsAdapter
	 *
	 * @param name
	 * @param phoneNumberIndex
	 * @param speedDialNumber
	 * @return New SpeedDialContact
	 * @throws Exception
	 */
	private SpeedDialContact addEntry(String name,
			int phoneNumberIndex,
			int speedDialNumber) throws Exception {
		SpeedDialContact newContact = databaseHelper.addRecord(name,
				allPhoneNumbers.get(phoneNumberIndex), speedDialNumber);
		if (newContact == null) {
			throw new Exception("Error while adding record");
		}
		contactsAdapter.add(newContact);
		Log.i(SpeedDialActivity.this.getLocalClassName(), "Added new record: "
				+ newContact);
		contactsAdapter.notifyDataSetChanged();
		return newContact;
	}

	private void showOverrideDialog(SpeedDialContact contactForEdit,
			int selectedPhoneNumberIndex,
			int enteredSpeedDialNumber) {
		AlertDialog.Builder overrideDialog = new AlertDialog.Builder(
				SpeedDialActivity.this);

		overrideDialog.setTitle(getResources().getString(
				R.string.title_override_dialog));
		if (speedDialNumberPresent && phoneNumberPresent) {
			overrideDialog.setMessage(getResources().getString(
					R.string.message_override_speed_dial_and_phone_number_dialog));
		} else if (speedDialNumberPresent) {
			overrideDialog.setMessage(getResources().getString(
					R.string.message_override_speed_dial_number_dialog));
		} else if (phoneNumberPresent) {
			overrideDialog.setMessage(getResources().getString(
					R.string.message_override_phone_number_dialog));
		}

		overrideDialog.setPositiveButton(getResources().getString(
				R.string.yes_button_string), new OverrideYesButtonListener(
				contactForEdit, selectedPhoneNumberIndex, enteredSpeedDialNumber));
		overrideDialog.setNegativeButton(getResources().getString(
				R.string.no_button_string), new CancelDilaogButtonListener());
		overrideDialog.setOnCancelListener(new CancelDialogListener());
		overrideDialog.create();
		overrideDialog.show();
	}

	/**
	 * Action to perform when back button is pressed while a dialog is open
	 */
	private class CancelDialogListener implements OnCancelListener {
		public void onCancel(DialogInterface dialog) {
			resetAllFields();
			dialog.dismiss();

		}
	}

	/**
	 * Update the launch count and show rate dialog, if the count equals the
	 * preset value
	 */
	private void showRateDialog() {
		SharedPreferences prefs = getSharedPreferences(
				SpeedDialConstants.PREFERENCES_FILE_NAME, 0);
		if (prefs.getBoolean(getResources().getString(
				R.string.never_show_again_preference_string), false)) {
			return;
		}
		SharedPreferences.Editor editor = prefs.edit();
		editor.putBoolean(getResources().getString(
				R.string.never_show_again_preference_string), false);
		editor.commit();
		// Increment launch counter
		long launchCount = prefs.getLong(getResources().getString(
				R.string.launch_count_preference_string), 0) + 1;
		Log.i(this.getLocalClassName(), "Number of Launches = "
				+ getResources().getInteger(R.integer.number_of_launches));
		if (launchCount >= getResources()
				.getInteger(R.integer.number_of_launches)) {
			showRateDialog(SpeedDialActivity.this, editor);
		} else {
			editor.putLong(getResources().getString(
					R.string.launch_count_preference_string), launchCount);
			editor.commit();
		}
	}

	/**
	 * Show the rate dialog
	 *
	 * @param mContext
	 * @param editor
	 */
	private void showRateDialog(final Context mContext,
			final SharedPreferences.Editor editor) {
		final Dialog dialog = new Dialog(mContext);
		dialog.setTitle(getResources().getString(R.string.title_rate_app_dialog));

		LayoutInflater inflater = getLayoutInflater();

		final View dialogView = inflater.inflate(R.layout.rate_app_dialog, null);
		dialog.setContentView(dialogView);

		Button rateAppButton = (Button) dialogView
				.findViewById(R.id.rateAppButton);
		rateAppButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				/*
				 * Set the launch count to negative so that it takes double to show
				 * the rate app dialog next time, if the use clicks on Rate button.
				 * This is to make sure that if the user clicks on Rate button, but
				 * does not actually rates it, the user would be asked again. But if
				 * the user has rated the App, he/she can then click on never rate
				 * again to stop the dialog to appear.
				 */
				editor.putLong(getResources().getString(
						R.string.launch_count_preference_string), (-1)
						* getResources().getInteger(R.integer.number_of_launches));
				editor.commit();
				mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri
						.parse("market://details?id="
								+ SpeedDialConstants.PAID_APP_PACKAGE)));

				dialog.dismiss();
			}
		});

		Button rateAppLaterButton = (Button) dialogView
				.findViewById(R.id.rateAppLaterButton);
		rateAppLaterButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				editor.putLong(getResources().getString(
						R.string.launch_count_preference_string), 0);
				editor.commit();
				dialog.dismiss();
			}
		});

		Button neverShowAgainButton = (Button) dialogView
				.findViewById(R.id.neverShowAgainButton);
		neverShowAgainButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (editor != null) {
					editor.putBoolean(getResources().getString(
							R.string.never_show_again_preference_string), true);
					editor.commit();
				}
				dialog.dismiss();
			}
		});
		dialog.show();
	}

	private void showSoftInput(EditText editText, final Window window) {
		window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
	}

	/**
	 * Method to reset the text view of the widget after the call is made. This
	 * method is supposed to be called from makeCall(String, boolean) method only
	 */
	private void resetRemoteViewsTextAndPreferences() {
		String speedDialString = getResources().getString(
				R.string.speed_dial_number_hash_string);

		SharedPreferences prefs = getSharedPreferences(
				SpeedDialConstants.PREFERENCES_FILE_NAME, 0);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(SpeedDialConstants.NEW_TEXT, speedDialString);

		RemoteViews remoteViews = new RemoteViews(this.getApplicationContext()
				.getPackageName(), R.layout.speed_dial_widget_layout);
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this
				.getApplicationContext());

		ComponentName thisWidget = new ComponentName(
				this.getApplicationContext(), SpeedDialWidget.class);
		int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);

		for (int widgetId : allWidgetIds) {
			remoteViews.setTextViewText(R.id.widget_speed_dial_Text,
					speedDialString);
			SpeedDialWidget.updateRemoteViews(this.getApplicationContext(),
					remoteViews, allWidgetIds);
			appWidgetManager.updateAppWidget(widgetId, remoteViews);
		}
		editor.commit();
		finish();
		return;
	}
}
