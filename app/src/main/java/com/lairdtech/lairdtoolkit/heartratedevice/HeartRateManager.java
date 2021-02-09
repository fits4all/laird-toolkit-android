package com.lairdtech.lairdtoolkit.heartratedevice;

import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothProfile;
import android.widget.Toast;

import com.lairdtech.bt.ble.BleBaseDeviceManager;
import com.lairdtech.bt.ble.BleNamesResolver;
import com.lairdtech.bt.ble.DefinedBleUUIDs;
import com.lairdtech.lairdtoolkit.IBleBaseActivityUiCallback;

public class HeartRateManager extends BleBaseDeviceManager
{

	private static final String TAG = "HeartRateManager";
	private HeartRateActivityUiCallback mHrActivityUiCallback;

	private String mValueHRM, mValueBodySensorLocation;
	private boolean isHrMeasurementFound = false;

	public HeartRateManager(
			IBleBaseActivityUiCallback iBleBaseActivityUiCallback,
			Activity mActivity)
	{
		super(mActivity, iBleBaseActivityUiCallback);
		mHrActivityUiCallback = (HeartRateActivityUiCallback) iBleBaseActivityUiCallback;
	}

	public String getValueBodySensorLocation()
	{
		return mValueBodySensorLocation;
	}

	public String getValueHRM()
	{
		return mValueHRM;
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
		else if (DefinedBleUUIDs.Service.HEART_RATE.equals(characteristic
				.getService().getUuid()))
		{
			if (DefinedBleUUIDs.Characteristic.HEART_RATE_MEASUREMENT
					.equals(characteristic.getUuid()))
			{
				isHrMeasurementFound = true;
				addCharToQueue(characteristic);

			}
			else if (DefinedBleUUIDs.Characteristic.BODY_SENSOR_LOCATION
					.equals(characteristic.getUuid()))
			{
				addCharToQueue(characteristic);
			}
		}
	}

	@Override
	protected void onCharsFoundCompleted()
	{
		if (!isHrMeasurementFound)
		{
			mActivity.runOnUiThread(new Runnable()
			{
				@Override
				public void run()
				{
					Toast.makeText(
							mActivity,
							"Heart rate measurement "
									+ "characteristic was not found",
							Toast.LENGTH_SHORT).show();
				}
			});

			disconnect();
		}
		else
		{
			// call execute after all characteristics added to queue
			executeCharacteristicsQueue();
		}
	}

	@Override
	public void onCharacteristicRead(BluetoothGatt gatt,
			BluetoothGattCharacteristic characteristic, int status)
	{
		super.onCharacteristicRead(gatt, characteristic, status);

		UUID characteristicUUID = characteristic.getUuid();

		if (status == BluetoothGatt.GATT_SUCCESS) {
			if (DefinedBleUUIDs.Characteristic.BODY_SENSOR_LOCATION
					.equals(characteristicUUID)) {
				int result = characteristic.getIntValue(
						BluetoothGattCharacteristic.FORMAT_UINT8, 0);

				mValueBodySensorLocation = BleNamesResolver
						.resolveHeartRateSensorLocation(result);
				mHrActivityUiCallback
						.onUiBodySensorLocation(mValueBodySensorLocation);
			}
		} else {// failed for reasons other than requiring bonding etc.
			if (DefinedBleUUIDs.Characteristic.BODY_SENSOR_LOCATION
					.equals(characteristicUUID)) {
				mValueBodySensorLocation = "N/A";
				mHrActivityUiCallback
						.onUiBodySensorLocation(mValueBodySensorLocation);
			}
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
				isHrMeasurementFound = false;
				break;
			}
		}
		else
		{
			isHrMeasurementFound = false;
		}
	}

	@Override
	public void onCharacteristicChanged(BluetoothGatt gatt,
			BluetoothGattCharacteristic ch)
	{
		super.onCharacteristicChanged(gatt, ch);
		UUID currentCharUUID = ch.getUuid();

		if (DefinedBleUUIDs.Characteristic.HEART_RATE_MEASUREMENT
				.equals(currentCharUUID))
		{
			int result = ch.getIntValue(
					BluetoothGattCharacteristic.FORMAT_UINT8, 1);
			mValueHRM = result + "";
			mHrActivityUiCallback.onUiHRM(mValueHRM);
		}
	}
}
