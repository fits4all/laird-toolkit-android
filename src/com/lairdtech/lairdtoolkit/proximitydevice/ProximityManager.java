/*****************************************************************************
 * Copyright (c) 2014 Laird Technologies. All Rights Reserved.
 * 
 * The information contained herein is property of Laird Technologies.
 * Licensees are granted free, non-transferable use of the information. NO WARRANTY of ANY KIND is provided. 
 * This heading must NOT be removed from the file.
 ******************************************************************************/

package com.lairdtech.lairdtoolkit.proximitydevice;

import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothProfile;
import android.util.Log;
import android.widget.Toast;

import com.lairdtech.bt.ble.BleBaseDeviceManager;
import com.lairdtech.bt.ble.DefinedBleUUIDs;
import com.lairdtech.lairdtoolkit.IBleBaseActivityUiCallback;

public class ProximityManager extends BleBaseDeviceManager
{
	private static final String TAG = "ProximityManager";

	private ProximityActivityUiCallback mProximityActivityUiCallback;
	private BluetoothGattCharacteristic mCharImmediateAlert, mCharLinkLoss;
	private int mCheckCharsFoundCount = 0;

	public ProximityManager(
			IBleBaseActivityUiCallback iBleBaseActivityUiCallback,
			Activity mActivity)
	{
		super(mActivity, iBleBaseActivityUiCallback);
		mProximityActivityUiCallback = (ProximityActivityUiCallback) iBleBaseActivityUiCallback;
	}

	@Override
	protected void onCharFound(BluetoothGattCharacteristic characteristic)
	{
		super.onCharFound(characteristic);

		if (DefinedBleUUIDs.Service.BATTERY.equals(characteristic.getService()
				.getUuid())
				&& DefinedBleUUIDs.Characteristic.BATTERY_LEVEL
						.equals(characteristic.getUuid()))
		{
			addCharToQueue(characteristic);
		}
		else if (DefinedBleUUIDs.Service.LINK_LOSS.equals(characteristic
				.getService().getUuid())
				&& DefinedBleUUIDs.Characteristic.ALERT_LEVEL
						.equals(characteristic.getUuid()))
		{
			mCheckCharsFoundCount++;
			mCharLinkLoss = characteristic;
		}
		else if (DefinedBleUUIDs.Service.IMMEDIATE_ALERT.equals(characteristic
				.getService().getUuid())
				&& DefinedBleUUIDs.Characteristic.ALERT_LEVEL
						.equals(characteristic.getUuid()))
		{
			mCheckCharsFoundCount++;
			mCharImmediateAlert = characteristic;
		}
		else if (DefinedBleUUIDs.Service.TX_POWER.equals(characteristic
				.getService().getUuid())
				&& DefinedBleUUIDs.Characteristic.TX_POWER_LEVEL
						.equals(characteristic.getUuid()))
		{
			mCheckCharsFoundCount++;
			addCharToQueue(characteristic);
		}
	}

	@Override
	protected void onCharsFoundCompleted()
	{
		if (mCheckCharsFoundCount != 3)
		{
			mActivity.runOnUiThread(new Runnable()
			{
				@Override
				public void run()
				{
					Toast.makeText(mActivity, "Not a valid proximity device",
							Toast.LENGTH_LONG).show();
				}
			});

			disconnect();
		}
		else
		{
			executeCharacteristicsQueue();
		}
	}

	@Override
	public void onCharacteristicRead(BluetoothGatt gatt,
			BluetoothGattCharacteristic ch, int status)
	{
		super.onCharacteristicRead(gatt, ch, status);
		UUID currentCharUUID = ch.getUuid();

		if (DefinedBleUUIDs.Characteristic.TX_POWER_LEVEL
				.equals(currentCharUUID))
		{
			byte[] txPower = ch.getValue();
			Log.i(TAG, "TX power: " + txPower);
			mProximityActivityUiCallback.onUiReadTxPower(txPower);
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
				mCheckCharsFoundCount = 0;
				break;
			}
		}
		else
		{
			mCheckCharsFoundCount = 0;
		}
	}

	public void writeAlertCharValue(final String hex,
			int linkLossOrImmediateAlert)
	{
		if (linkLossOrImmediateAlert == 0)
		{
			if (mCharLinkLoss == null)
				return;
			// first set it locally....
			mCharLinkLoss.setValue(parseHexStringToBytes(hex));
			// ... and then "commit" changes to the peripheral
			mBluetoothGatt.writeCharacteristic(mCharLinkLoss);
		}
		else if (linkLossOrImmediateAlert == 1)
		{
			if (mCharImmediateAlert == null)
				return;
			// first set it locally....
			mCharImmediateAlert.setValue(parseHexStringToBytes(hex));
			// ... and then "commit" changes to the peripheral
			mBluetoothGatt.writeCharacteristic(mCharImmediateAlert);
		}
	}

	public byte[] parseHexStringToBytes(final String hex)
	{
		String tmp = hex.substring(2).replaceAll("[^[0-9][a-f]]", "");
		byte[] bytes = new byte[tmp.length() / 2]; // every two letters in the
													// string are one byte
													// finally
		String part = "";

		for (int i = 0; i < bytes.length; ++i)
		{
			part = "0x" + tmp.substring(i * 2, i * 2 + 2);
			bytes[i] = Long.decode(part).byteValue();
		}
		return bytes;
	}
}
