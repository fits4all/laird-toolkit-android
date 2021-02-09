package com.lairdtech.lairdtoolkit.healththermometerdevice;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.lairdtech.lairdtoolkit.BleBaseMultipleConnectedDevicesAdapter;
import com.lairdtech.lairdtoolkit.R;

public class ThermometerMultipleConnectedDevicesAdapter extends
		BleBaseMultipleConnectedDevicesAdapter
{
	private ThermometerManager device;

	public ThermometerMultipleConnectedDevicesAdapter(Activity par)
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
					R.layout.item_multiple_thermometer_connected_devices,
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
			device = (ThermometerManager) mDevices.get(position);
		}

		if (device != null)
		{
			// set proper values into the view
			setGenericViews(fields, convertView);
			setThermometerViews(fields, convertView);
		}
		return convertView;
	}

	private static class FieldReferences
	{
		TextView valueDeviceName;
		TextView valueDeviceAddress;
		TextView valueDeviceBattery;
		TextView valueDeviceRssi;
		TextView valueTempMeasurement;
	}

	private void bindViews(FieldReferences fields, View convertView)
	{
		fields.valueDeviceAddress = convertView
				.findViewById(R.id.valueDeviceAddress);
		fields.valueDeviceName = convertView
				.findViewById(R.id.valueDeviceName);
		fields.valueDeviceBattery = convertView
				.findViewById(R.id.valueDeviceBattery);

		fields.valueTempMeasurement = convertView
				.findViewById(R.id.valueTempMeasurement);
		fields.valueDeviceRssi = convertView
				.findViewById(R.id.valueDeviceRssi);
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

		if (device.getValueBattery() != null)
		{
			fields.valueDeviceBattery.setText("" + device.getValueBattery());
		}
		else
		{
			fields.valueDeviceBattery.setText(convertView.getResources()
					.getString(R.string.non_applicable));
		}

		if (device.getValueRSSI() != null)
		{
			fields.valueDeviceRssi.setText("" + device.getValueRSSI());
		}
		else
		{
			fields.valueDeviceRssi.setText(convertView.getResources()
					.getString(R.string.non_applicable));
		}
	}

	private void setThermometerViews(final FieldReferences fields,
			final View convertView)
	{
		if (device.getValueTempMeasurement() != null)
		{
			fields.valueTempMeasurement.setText(device
					.getValueTempMeasurement() + " C");
		}
		else
		{
			fields.valueTempMeasurement.setText(convertView.getResources()
					.getString(R.string.dash));
		}
	}
}