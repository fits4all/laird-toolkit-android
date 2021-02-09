package com.lairdtech.lairdtoolkit.heartratedevice;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.lairdtech.lairdtoolkit.BleBaseMultipleConnectedDevicesAdapter;
import com.lairdtech.lairdtoolkit.R;

public class HeartRateMultipleConnectedDevicesAdapter extends
		BleBaseMultipleConnectedDevicesAdapter
{
	private HeartRateManager device;

	public HeartRateMultipleConnectedDevicesAdapter(Activity par)
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
					R.layout.item_multiple_heart_rate_connected_devices,
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
			device = (HeartRateManager) mDevices.get(position);
		}

		if (device != null)
		{
			// set proper values into the view
			setGenericViews(fields, convertView);
			setHeartRateViews(fields, convertView);
		}
		return convertView;
	}

	private class FieldReferences
	{
		TextView valueDeviceName;
		TextView valueDeviceRssi;
		TextView valueDeviceAddress;
		TextView valueDeviceBattery;
		TextView valueBodySensor;
		TextView valueHRM;
	}

	private void bindViews(FieldReferences fields, View convertView)
	{
		fields.valueDeviceName = (TextView) convertView
				.findViewById(R.id.valueDeviceName);
		fields.valueDeviceRssi = (TextView) convertView
				.findViewById(R.id.valueDeviceRssi);
		fields.valueDeviceAddress = (TextView) convertView
				.findViewById(R.id.valueDeviceAddress);
		fields.valueDeviceBattery = (TextView) convertView
				.findViewById(R.id.valueDeviceBattery);
		fields.valueBodySensor = (TextView) convertView
				.findViewById(R.id.valueBodySensor);
		fields.valueHRM = (TextView) convertView.findViewById(R.id.valueHRM);
	}

	private void setGenericViews(final FieldReferences fields,
			final View convertView)
	{
		// set proper values into the view
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

	private void setHeartRateViews(final FieldReferences fields,
			final View convertView)
	{
		if (device.getValueHRM() != null)
		{
			fields.valueHRM.setText(device.getValueHRM() + " BPM");
		}
		else
		{
			fields.valueHRM.setText(convertView.getResources().getString(
					R.string.dash));
		}

		if (device.getValueBodySensorLocation() != null)
		{
			fields.valueBodySensor.setText(""
					+ device.getValueBodySensorLocation());
		}
		else
		{
			fields.valueBodySensor.setText(convertView.getResources()
					.getString(R.string.non_applicable));
		}
	}
}