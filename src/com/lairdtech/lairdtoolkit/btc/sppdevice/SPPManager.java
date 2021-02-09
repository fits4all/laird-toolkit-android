package com.lairdtech.lairdtoolkit.btc.sppdevice;

import java.util.UUID;

import org.apache.commons.lang3.StringEscapeUtils;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.util.Log;

import com.lairdtech.bt.classic.BaseBtClassicDeviceManager;

public class SPPManager extends BaseBtClassicDeviceManager
{
	private static final String TAG = "SPPManager";
	private static final int TOTAL_BYTES_TO_READ_FROM_REMOTE_DEVICE = 100;
	public static final UUID UUID_SPP = UUID
			.fromString("00001101-0000-1000-8000-00805F9B34FB");
	private SPPManagerUiCallback mSPPManagerUiCallback;

	public SPPManager(Activity activity,
			SPPManagerUiCallback SPPManagerUiCallback,
			BluetoothDevice bluetoothDevice)
	{
		super(activity, bluetoothDevice, UUID_SPP);
		mSPPManagerUiCallback = SPPManagerUiCallback;
	}

	@Override
	public void onBtClassicConnected()
	{
		startDataListeningFromRemoteDevice(TOTAL_BYTES_TO_READ_FROM_REMOTE_DEVICE);
		mSPPManagerUiCallback.onUiBtcRemoteDeviceConnected();
	}

	@Override
	public void onBtClassicDisconnected()
	{
		mSPPManagerUiCallback.onUiBtcRemoteDeviceDisconnected();
	}

	@Override
	public void onBtClassicConnectFailed()
	{
		mSPPManagerUiCallback.onUiBtcRemoteDeviceFailed();
	}

	@Override
	public void onBtClassicDataRead(byte[] buffer)
	{
		/*
		 * The buffer might have extra data, if the data that was sent from the
		 * remote device is less than the size of our buffer then the buffer
		 * will have extra unneeded data.
		 * 
		 * As we only want the data that was actually sent from the remote
		 * device, we discard the extra data.
		 */
		String result = new String(buffer);

		Log.i(TAG,
				"Received data from remote device: "
						+ StringEscapeUtils.escapeJava(result));

		mSPPManagerUiCallback.onUiRemoteDeviceRead(result);
	}
}
