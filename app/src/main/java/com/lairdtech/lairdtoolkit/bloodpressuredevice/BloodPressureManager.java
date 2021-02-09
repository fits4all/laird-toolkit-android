

package com.lairdtech.lairdtoolkit.bloodpressuredevice;

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

public class BloodPressureManager extends BleBaseDeviceManager
{
	private static final String TAG = "BloodPressureManager";

	private BloodPressureActivityUiCallback mBloodPressureActivityUiCallback;

	private float mValueSystolic, mValueDiastolic, mValueArterialPressure;
	private String mUnitType = "";
	private boolean isBpMeasurementFound = false;

	public BloodPressureManager(
			IBleBaseActivityUiCallback iBleBaseActivityUiCallback,
			Activity activity)
	{
		super(activity, iBleBaseActivityUiCallback);
		mBloodPressureActivityUiCallback = (BloodPressureActivityUiCallback) iBleBaseActivityUiCallback;
	}

	public float getValueSystolic()
	{
		return mValueSystolic;
	}

	public float getValueDiastolic()
	{
		return mValueDiastolic;
	}

	public float getValueArterialPressure()
	{
		return mValueArterialPressure;
	}

	public String getUnitType()
	{
		return mUnitType;
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

				mValueSystolic = 0;
				mValueDiastolic = 0;
				mValueArterialPressure = 0;

				readRssiPeriodicaly(true, RSSI_UPDATE_INTERVAL);

				break;

			case BluetoothProfile.STATE_DISCONNECTED:
				isBpMeasurementFound = false;
				break;
			}
		}
		else
		{
			isBpMeasurementFound = false;
		}
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
		else if (DefinedBleUUIDs.Service.BLOOD_PRESSURE.equals(characteristic
				.getService().getUuid()))
		{
			if (DefinedBleUUIDs.Characteristic.BLOOD_PRESSURE_MEASUREMENT
					.equals(characteristic.getUuid()))
			{
				addCharToQueue(characteristic);
				isBpMeasurementFound = true;
			}
		}
	}

	@Override
	protected void onCharsFoundCompleted()
	{
		if (isBpMeasurementFound == false)
		{
			mActivity.runOnUiThread(new Runnable()
			{
				@Override
				public void run()
				{
					Toast.makeText(
							mActivity,
							"Blood pressure measurement "
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
	public void onCharacteristicChanged(BluetoothGatt gatt,
			BluetoothGattCharacteristic ch)
	{
		super.onCharacteristicChanged(gatt, ch);

		UUID currentCharUUID = ch.getUuid();
		if (DefinedBleUUIDs.Characteristic.BLOOD_PRESSURE_MEASUREMENT
				.equals(currentCharUUID))
		{
			mValueSystolic = ch.getFloatValue(
					BluetoothGattCharacteristic.FORMAT_SFLOAT, 1);
			mValueDiastolic = ch.getFloatValue(
					BluetoothGattCharacteristic.FORMAT_SFLOAT, 3);
			mValueArterialPressure = ch.getFloatValue(
					BluetoothGattCharacteristic.FORMAT_SFLOAT, 5);

			identifyAndSetUnitType(ch);

			mBloodPressureActivityUiCallback.onUIBloodPressureRead(
					mValueSystolic, mValueDiastolic, mValueArterialPressure);
		}
	}

	private void identifyAndSetUnitType(
			BluetoothGattCharacteristic bloodPressureChar)
	{
		/*
		 * first byte is the flag bloodPressureChar.getValue()[0] and first bit
		 * of that byte is the unit type (bloodPressureChar.getValue()[0] & 1)
		 * That is why we do a bitwise operation with 1.
		 * 
		 * https://developer.bluetooth.org/gatt/characteristics/Pages/
		 * CharacteristicViewer
		 * .aspx?u=org.bluetooth.characteristic.blood_pressure_measurement.xml
		 */
		byte[] unitType = bloodPressureChar.getValue();

		boolean kpa = ((unitType[0] & 1) == 1 ? true : false);

		Log.i(TAG, "is it in kpa: " + kpa);

		if (kpa)
		{
			mUnitType = "kpa";
		}
		else
		{
			mUnitType = "mmHg";
		}
	}
}