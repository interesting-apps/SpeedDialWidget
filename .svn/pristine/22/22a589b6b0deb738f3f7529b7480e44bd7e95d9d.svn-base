package com.apps.interestingapps.speeddial.common;

import java.util.Collections;
import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.apps.interestingapps.speeddial.R;
import com.apps.interestingapps.speeddial.SpeedDialContact;

/**
 * Class that creates a custom adapter to display speed dial contact
 */
public class ContactDisplayAdapter extends ArrayAdapter<SpeedDialContact> {

	private final Context context;
	private final List<SpeedDialContact> values;

	static class ViewHolder {
		public TextView _id;
		public TextView contactName;
		public TextView phoneNumber;
		public TextView speedDialNumber;
	}

	public ContactDisplayAdapter(Context context, List<SpeedDialContact> values) {
		super(context, R.layout.contact_display_row_view, values);
		this.context = context;
		this.values = values;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View rowView = convertView;
		if (rowView == null) {
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			rowView = inflater.inflate(R.layout.contact_display_row_view, parent,
					false);
			ViewHolder viewHolder = new ViewHolder();
			viewHolder._id = (TextView) rowView.findViewById(R.id.contact_id);
			viewHolder.contactName = (TextView) rowView
					.findViewById(R.id.contactName);
			viewHolder.phoneNumber = (TextView) rowView
					.findViewById(R.id.phoneNumber);
			viewHolder.speedDialNumber = (TextView) rowView
					.findViewById(R.id.speedDialNumber);
			rowView.setTag(viewHolder);
		}
		ViewHolder viewHolder = (ViewHolder) rowView.getTag();
		viewHolder._id.setText(Long.toString(values.get(position).get_id()));
		viewHolder.contactName.setText(values.get(position).getName());
		viewHolder.phoneNumber.setText(values.get(position).getPhoneNumber());
		viewHolder.speedDialNumber.setText(Integer.toString(values.get(position)
				.getSpeedDialNumber()));

		return rowView;
	}

	/**
	 * Sorts the updated data according to the speed dial number
	 */
	@Override
	public void notifyDataSetChanged() {
		super.notifyDataSetChanged();
		Collections.sort(values);
	}

	/**
	 * 
	 * @param contactToRemove
	 */
	@Override
	public void remove(SpeedDialContact contactToRemove) {
		int indexOfContactToRemove = SpeedDialUtilities.binarySearch(values,
				contactToRemove);
		if (indexOfContactToRemove < 0) {
			Log.i("ContactsDisplayAdapter", "Entry for contact with SDN: "
					+ contactToRemove.getSpeedDialNumber() + " not found");
		}
		SpeedDialContact speedDialContactInList = values
				.get(indexOfContactToRemove);
		super.remove(speedDialContactInList);
	}
}