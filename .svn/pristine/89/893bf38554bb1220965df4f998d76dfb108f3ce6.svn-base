package com.apps.interestingapps.speeddial.common;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.apps.interestingapps.speeddial.R;

public class PhoneNumberDialogAdapter extends ArrayAdapter<String> {

	private final Context context;
	private final String[] values;

	static class ViewHolder {
		public TextView phoneNumber;
	}

	public PhoneNumberDialogAdapter(Context context, String[] values) {
		super(context, R.layout.phone_number_dialog_row_view, values);
		this.context = context;
		this.values = values;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		if (convertView == null) {
			convertView = inflater.inflate(R.layout.phone_number_dialog_row_view,
					null);

			holder = new ViewHolder();

			holder.phoneNumber = (TextView) convertView.findViewById(R.id.phoneNumberForDialog);
			convertView.setTag(holder);
		} else {
			// view already defined, retrieve view holder
			holder = (ViewHolder) convertView.getTag();
		}
		holder.phoneNumber.setText(values[position]);
		return convertView;
	}
}