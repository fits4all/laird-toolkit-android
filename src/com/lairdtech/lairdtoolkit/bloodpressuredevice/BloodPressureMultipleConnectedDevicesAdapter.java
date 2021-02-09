package com.lairdtech.lairdtoolkit.bloodpressuredevice;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.lairdtech.lairdtoolkit.BleBaseMultipleConnectedDevicesAdapter;
import com.lairdtech.lairdtoolkit.R;

public class BloodPressureMultipleConnectedDevicesAdapter extends
		BleBaseMultipleConnectedDevicesAdapter
{

	private BloodPressureManager device = null;

	public BloodPressureMultipleConnectedDevicesAdapter(Activity par)
	{
		super(par);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		// get already available view or create new if necessary
		FieldReferences fields;
		if (convertView == null)
		{
			convertView = mInflater.inflate(
					R.layout.item_multiple_blood_pressure_connected_devices,
					parent, false);
			fields = new FieldReferences();
			bindViews(fields, convertView);
			convertView.setTag(fields);
		}
		else
		{
			fields = (FieldReferences) convertView.getTag();
		}

		/*
		 * making sure we are not going to try and get a device that was removed
		 * from the list
		 */
		if (mDevices.size() > position)
		{
			device = (BloodPressureManager) mDevices.get(position);
		}

		if (device != null)
		{
			// set proper values into the view
			setGenericViews(fields, convertView);
			setBloodPressureViews(fields, convertView);
		}

		return convertView;
	}

	private class FieldReferences
	{
		TextView valueDeviceName;
		TextView valueDeviceAddress;
		TextView valueDeviceBattery;
		TextView valueDeviceRssi;

		TextView valueSystolic;
		TextView valueDiastolic;
		TextView valueArterialPressure;
	}

	private void bindViews(FieldReferences fields, View convertView)
	{
		fields.valueDeviceAddress = (TextView) convertView
				.findViewById(R.id.valueDeviceAddress);
		fields.valueDeviceName = (TextView) convertView
				.findViewById(R.id.valueDeviceName);
		fields.valueDeviceBattery = (TextView) convertView
				.findViewById(R.id.valueDeviceBattery);
		fields.valueDeviceRssi = (TextView) convertView
				.findViewById(R.id.valueDeviceRssi);

		fields.valueSystolic = (TextView) convertView
				.findViewById(R.id.valueSystolic);
		fields.valueDiastolic = (TextView) convertView
				.findViewById(R.id.valueDiastolic);
		fields.valueArterialPressure = (TextView) convertView
				.findViewById(R.id.valueArterialPressure);
	}

	private void setGenericViews(final FieldReferences fields,
			final View convertView)
	{
		String name = device.getBluetoothDevice().getName();
		String address = device.getBluetoothDevice().getAddress();
		if (name == null || name.length() <= 0)
			name = "Unknown Device";

		fields.valueDeviceName.setText(name);

		if (address != null)
		{
			fields.valueDeviceAddress.setText(address);
		}
		else
		{
			fields.valueDeviceAddress.setText(convertView.getResources()
					.getString(R.string.non_applicable));
		}

		if (device.getValueRSSI() != null)
		{
			fields.valueDeviceRssi.setText(device.getValueRSSI());
		}
		else
		{
			fields.valueDeviceRssi.setText(convertView.getResources()
					.getString(R.string.non_applicable));
		}

		if (device.getValueBattery() != null)
		{
			fields.valueDeviceBattery.setText(device.getValueBattery());
		}
		else
		{
			fields.valueDeviceBattery.setText(convertView.getResources()
					.getString(R.string.non_applicable));
		}

	}

	private void setBloodPressureViews(final FieldReferences fields,
			final View convertView)
	{
		if (device.getUnitType() != null)
		{
			fields.valueSystolic.setText(device.getValueSystolic() + " "
					+ device.getUnitType());
			fields.valueDiastolic.setText(device.getValueDiastolic() + " "
					+ device.getUnitType());
			fields.valueArterialPressure.setText(device
					.getValueArterialPressure() + " " + device.getUnitType());
		}
		else
		{
			fields.valueSystolic.setText(convertView.getResources().getString(
					R.string.non_applicable));
			fields.valueDiastolic.setText(convertView.getResources().getString(
					R.string.non_applicable));
			fields.valueArterialPressure.setText(convertView.getResources()
					.getString(R.string.non_applicable));
		}
	}
}