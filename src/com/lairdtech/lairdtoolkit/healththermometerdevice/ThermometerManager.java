/*****************************************************************************
 * Copyright (c) 2014 Laird Technologies. All Rights Reserved.
 * 
 * The information contained herein is property of Laird Technologies.
 * Licensees are granted free, non-transferable use of the information. NO WARRANTY of ANY KIND is provided. 
 * This heading must NOT be removed from the file.
 ******************************************************************************/

package com.lairdtech.lairdtoolkit.healththermometerdevice;

import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothProfile;
import android.widget.Toast;

import com.lairdtech.bt.ble.BleBaseDeviceManager;
import com.lairdtech.bt.ble.DefinedBleUUIDs;
import com.lairdtech.lairdtoolkit.IBleBaseActivityUiCallback;

public class ThermometerManager extends BleBaseDeviceManager
{
	private static final String TAG = "ThermometerManager";

	private ThermometerActivityUICallback mThermometerActivityUICallback;
	private String mValueTempMeasurement;
	private boolean isTempMeasurementFound = false;

	public ThermometerManager(
			IBleBaseActivityUiCallback iBleBaseActivityUiCallback,
			Activity activity)
	{
		super(activity, iBleBaseActivityUiCallback);
		mThermometerActivityUICallback = (ThermometerActivityUICallback) iBleBaseActivityUiCallback;
	}

	public String getValueTempMeasurement()
	{
		return mValueTempMeasurement;
	}

	@Override
	protected void onCharFound(BluetoothGattCharacteristic characteristic)
	{
		super.onCharFound(characteristic);

		if (DefinedBleUUIDs.Service.BATTERY.equals(characteristic.getService()
				.getUuid()))
		{
			if (DefinedBleUUIDs.Characteristic.BATTERY_LEVEL
					.equals(characteristic.getUuid()))
			{
				addCharToQueue(characteristic);
			}
		}
		else if (DefinedBleUUIDs.Service.HEALTH_THERMOMETER
				.equals(characteristic.getService().getUuid()))
		{
			if (DefinedBleUUIDs.Characteristic.TEMPERATURE_MEASUREMENT
					.equals(characteristic.getUuid()))
			{
				isTempMeasurementFound = true;
				addCharToQueue(characteristic);
			}
		}
	}

	@Override
	protected void onCharsFoundCompleted()
	{
		if (isTempMeasurementFound == false)
		{
			mActivity.runOnUiThread(new Runnable()
			{
				@Override
				public void run()
				{
					Toast.makeText(
							mActivity,
							"Temperature measurement "
									+ "characteristic was not found",
							Toast.LENGTH_LONG).show();
				}
			});

			disconnect();
		}
		else
		{
			// call execute after all services added to queue
			executeCharacteristicsQueue();
		}
	}

	@Override
	public void onConnectionStateChange(BluetoothGatt gatt, int status,
			int newState)
	{
		super.onConnectionStateChange(gatt, status, newState);
		if (status == BluetoothGatt.GATT_SUCCESS)
		{
			switch (newState)
			{
			case BluetoothProfile.STATE_CONNECTED:
				readRssiPeriodicaly(true, RSSI_UPDATE_INTERVAL);
				break;

			case BluetoothProfile.STATE_DISCONNECTED:
				isTempMeasurementFound = false;
				break;
			}
		}
		else
		{
			isTempMeasurementFound = false;
		}
	}

	@Override
	public void onCharacteristicChanged(BluetoothGatt gatt,
			BluetoothGattCharacteristic ch)
	{
		super.onCharacteristicChanged(gatt, ch);

		UUID currentCharUUID = ch.getUuid();

		if (DefinedBleUUIDs.Characteristic.TEMPERATURE_MEASUREMENT
				.equals(currentCharUUID))
		{
			float result = ch.getFloatValue(
					BluetoothGattCharacteristic.FORMAT_FLOAT, 1);
			mValueTempMeasurement = result + "";
			mThermometerActivityUICallback
					.onUiTemperatureChange(mValueTempMeasurement);
		}
	}
}
