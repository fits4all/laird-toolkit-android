package com.lairdtech.lairdtoolkit.serialdevice;

import android.app.Activity;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;

import com.lairdtech.bt.ble.vsp.VirtualSerialPortDevice;
import com.lairdtech.lairdtoolkit.IBleBaseActivityUiCallback;

public class SerialManager extends VirtualSerialPortDevice
{
	private SerialManagerUiCallback mSerialManagerUiCallback;

	public SerialManager(Activity activity,
			IBleBaseActivityUiCallback iBleBaseActivityUiCallback)
	{
		super(activity, iBleBaseActivityUiCallback);

		mSerialManagerUiCallback = (SerialManagerUiCallback) iBleBaseActivityUiCallback;
		SEND_DATA_TO_REMOTE_DEVICE_DELAY = 1;
	}

	@Override
	public void onVspSendDataSuccess(final BluetoothGatt gatt,
			final BluetoothGattCharacteristic ch)
	{
		sendDataHandler.postDelayed(new Runnable()
		{
			@Override
			public void run()
			{
				if (mFifoAndVspManagerState == FifoAndVspManagerState.UPLOADING) {/*
				 * what to do after the data was send successfully
				 */
					if (isBufferSpaceAvailable()) {
						uploadNextDataFromFifoToRemoteDevice();
					}
				}
			}
		}, SEND_DATA_TO_REMOTE_DEVICE_DELAY);

		mSerialManagerUiCallback.onUiSendDataSuccess(ch.getStringValue(0));
	}

	@Override
	public void onVspReceiveData(BluetoothGatt gatt,
			BluetoothGattCharacteristic ch)
	{
		mRxBuffer.write(ch.getStringValue(0));

		while (mRxBuffer.read(mRxDest) != 0)
		{
			/*
			 * found data
			 */
			String rxBufferDataRead = mRxDest.toString();
			mRxDest.delete(0, mRxDest.length());
			mSerialManagerUiCallback.onUiReceiveData(rxBufferDataRead);
		}
	}

	@Override
	public void onVspIsBufferSpaceAvailable(
			boolean isBufferSpaceAvailableOldState,
			boolean isBufferSpaceAvailableNewState)
	{
		if (mFifoAndVspManagerState == FifoAndVspManagerState.UPLOADING) {/*
		 * callback for what to do when data was send successfully from the
		 * android device and when the module buffer was full and now it has
		 * been cleared, which means it now has available space
		 */
			if (!isBufferSpaceAvailableOldState
					&& isBufferSpaceAvailableNewState) {
				uploadNextDataFromFifoToRemoteDevice();
			}
		}
	}

	@Override
	public void onUploaded()
	{
		super.onUploaded();
		mSerialManagerUiCallback.onUiUploaded();
	}
}