/*****************************************************************************
 * Copyright (c) 2014 Laird Technologies. All Rights Reserved.
 * 
 * The information contained herein is property of Laird Technologies.
 * Licensees are granted free, non-transferable use of the information. NO WARRANTY of ANY KIND is provided. 
 * This heading must NOT be removed from the file.
 ******************************************************************************/

package com.lairdtech.lairdtoolkit;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

/**
 * Responsible for handling the bluetooth classic and BLE devices
 * 
 * @author Kyriakos.Alexandrou
 */
public class ListFoundDevicesHandler extends BaseAdapter
{
	private String TAG = "ListFoundDevicesHandler";
	private ArrayList<BluetoothDevice> mDevices;
	private ArrayList<Integer> mRSSIs;
	private LayoutInflater mInflater;

	public ListFoundDevicesHandler(Activity par)
	{
		super();
		mDevices = new ArrayList<BluetoothDevice>();
		mRSSIs = new ArrayList<Integer>();
		mInflater = par.getLayoutInflater();
	}

	public void addDevice(BluetoothDevice device, int rssi, byte[] scanRecord)
	{
		Log.i(TAG, "Device added in found devices list: " + device.getAddress());

		if (mDevices.contains(device) == false)
		{
			mDevices.add(device);
			mRSSIs.add(rssi);
		}
		else
		{
			mRSSIs.set(mDevices.indexOf(device), rssi);
		}
		notifyDataSetChanged();
	}

	public void addDevice(BluetoothDevice device, int rssi)
	{
		Log.i(TAG, "Device added in found devices list: " + device.getAddress());

		if (mDevices.contains(device) == false)
		{
			mDevices.add(device);
			mRSSIs.add(rssi);
		}
		else
		{
			mRSSIs.set(mDevices.indexOf(device), rssi);
		}
		notifyDataSetChanged();
	}

	public BluetoothDevice getDevice(int index)
	{
		return mDevices.get(index);
	}

	public int getRssi(int index)
	{
		return mRSSIs.get(index);
	}

	public void clearList()
	{
		mDevices.clear();
		mRSSIs.clear();
	}

	@Override
	public int getCount()
	{
		return mDevices.size();
	}

	@Override
	public Object getItem(int position)
	{
		return getDevice(position);
	}

	@Override
	public long getItemId(int position)
	{
		return position;
	}

	@SuppressLint("InflateParams")
	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		// get already available view or create new if necessary
		FieldReferences fields;
		if (convertView == null)
		{
			convertView = mInflater.inflate(R.layout.item_scanned, null);
			fields = new FieldReferences();
			fields.valueDeviceName = (TextView) convertView
					.findViewById(R.id.valueDeviceNameItem);
			fields.valueDeviceAddress = (TextView) convertView
					.findViewById(R.id.valueDeviceAddressItem);
			fields.valueDeviceRssi = (TextView) convertView
					.findViewById(R.id.valueDeviceRssiItem);
			convertView.setTag(fields);
		}
		else
		{
			fields = (FieldReferences) convertView.getTag();
		}

		// set proper values into the view
		BluetoothDevice device = mDevices.get(position);
		int rssi = mRSSIs.get(position);
		String rssiString = (rssi == 0) ? "N/A" : rssi + " db";
		String name = device.getName();
		String address = device.getAddress();
		if (name == null || name.length() <= 0)
			name = "Unknown Device";

		fields.valueDeviceName.setText(name);
		fields.valueDeviceAddress.setText(address);
		fields.valueDeviceRssi.setText(rssiString);

		return convertView;
	}

	private class FieldReferences
	{
		TextView valueDeviceName;
		TextView valueDeviceAddress;
		TextView valueDeviceRssi;
	}
}