package com.lairdtech.lairdtoolkit;

import java.util.ArrayList;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.lairdtech.bt.ble.BleBaseDeviceManager;

/**
 * Base class for all the adapters that are responsible for connecting with
 * multiple BLE devices
 * 
 * @author Kyriakos.Alexandrou
 * 
 */
public class BleBaseMultipleConnectedDevicesAdapter extends BaseAdapter
{
	protected ArrayList<BleBaseDeviceManager> mDevices;
	protected LayoutInflater mInflater;

	public BleBaseMultipleConnectedDevicesAdapter(Activity par)
	{
		super();
		mDevices = new ArrayList<BleBaseDeviceManager>();
		mInflater = par.getLayoutInflater();
	}

	/**
	 * Adds a BLE device into the list
	 * 
	 * @param device
	 *            the device to add to the list
	 * @return true if device was added to the list, otherwise returns false
	 */
	public boolean addDevice(BleBaseDeviceManager device)
	{
		if (mDevices.contains(device) == false)
		{
			mDevices.add(device);
			return true;
		}
		else
		{
			return false;
		}
	}

	public BleBaseDeviceManager getDevice(int index)
	{
		return mDevices.get(index);
	}

	public ArrayList<BleBaseDeviceManager> getDevices()
	{
		return mDevices;
	}

	public void clearList()
	{
		mDevices.clear();
	}

	public void disconnectFromDevices()
	{
		for (int i = 0; i < getCount(); i++)
		{
			mDevices.get(i).disconnect();
		}
	}

	public void remove(int position)
	{
		/*
		 * making sure we are not going to try and get a device that was already
		 * removed from the list
		 */
		if (mDevices.size() > position)
		{
			mDevices.remove(position);
		}
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

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		return null;
	}
}