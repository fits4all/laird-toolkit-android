package com.lairdtech.bt.ble.vsp;

import java.util.UUID;

import org.apache.commons.lang3.StringEscapeUtils;

import android.app.Activity;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.lairdtech.bt.ble.BleBaseDeviceManager;
import com.lairdtech.lairdtoolkit.IBleBaseActivityUiCallback;
import com.lairdtech.misc.FifoQueue;

/**
 * Responsible for the communication between the android device and a module
 * that has the Virtual Serial Port (VSP) service
 * 
 * <br>
 * Include's the error codes that the module can send in case of an error and
 * the UUIDs of the service and of the characteristics
 * 
 * <br>
 * Give callback's to the class that implements the
 * VirtualSerialPortDeviceCallback interface.
 * 
 */
public class VirtualSerialPortDevice extends BleBaseDeviceManager
{
	private static final String TAG = "VirtualSerialPortDevice";

	public final static UUID VSP_SERVICE = UUID
			.fromString("569a1101-b87f-490c-92cb-11ba5ea5167c");
	/**
	 * for receiving data from the remote device
	 */
	public final static UUID VSP_CHAR_TX = UUID
			.fromString("569a2000-b87f-490c-92cb-11ba5ea5167c");
	/**
	 * doe sending data to the remote device
	 */
	public final static UUID VSP_CHAR_RX = UUID
			.fromString("569a2001-b87f-490c-92cb-11ba5ea5167c");
	public final static UUID VSP_CHAR_MODEM_OUT = UUID
			.fromString("569a2002-b87f-490c-92cb-11ba5ea5167c");
	public final static UUID VSP_CHAR_MODEM_IN = UUID
			.fromString("569a2003-b87f-490c-92cb-11ba5ea5167c");

	/*
	 * module VSP responses when in command mode
	 */
	public final static String SUCCESS_CODE = "00";
	public final static String ERROR_CODE_NO_FILE_TO_CLOSE = "E037";
	public final static String ERROR_CODE_MEMORY_FULL = "5002";
	public final static String ERROR_CODE_FSA_FAIL_OPENFILE = "1809";
	public final static String ERROR_CODE_FSA_FILENAME_TOO_LONG = "1803";
	public final static String ERROR_CODE_FILE_NOT_OPEN = "E038";
	public final static String ERROR_CODE_INCORRECT_MODE = "E00E";
	public final static String ERROR_CODE_UNKNOWN_COMMAND = "E007";
	public final static String ERROR_CODE_UNKNOWN_SUBCOMMAND = "E00D";
	public final static String ERROR_CODE_UNEXPECTED_PARM = "E002";

	private boolean mIsValidVspDevice = false;
	protected FifoAndVspManagerState mFifoAndVspManagerState;

	private VirtualSerialPortDeviceCallback mVSPUiDeviceCallback;
	/**
	 * module char that retrieves data. This means we use it to send data from
	 * the android device to the remote module. We write the char to the remote
	 * device to send data
	 */
	private BluetoothGattCharacteristic mCharRx;
	/**
	 * module char that sends data. This means we use it to receive data through
	 * notifications from the remote module
	 */
	private BluetoothGattCharacteristic mCharTx;
	/**
	 * module: i can send some data to the other device now Note: This char is
	 * found if the BLE device is in bridge mode, otherwise if the module is not
	 * in bridge mode it will need to be enabled. This characteristic is
	 * currently not used as android has a big buffer limit
	 */
	private BluetoothGattCharacteristic mCharModemIn;
	/**
	 * module: am ready to retrieve some data - when value is 1 it means that
	 * the module buffer is not full and it can retrieve more data - when the
	 * value is 0 it means that the module buffer is full and therefore if we
	 * send it any more data they will get lost Note: This char is found if the
	 * BLE device is in bridge mode, otherwise if the module is not in bridge
	 * mode it will need to be enabled
	 */
	private BluetoothGattCharacteristic mCharModemOut;
	/**
	 * BLE module buffer full or not?
	 */
	private boolean mIsBufferSpaceAvailableNewState = true;

	/*
	 * counters for the total data send and received
	 */
	private int mRxCounter;
	private int mTxCounter;

	/**
	 * this should be no more than 20 as the Laird module can only receive a
	 * total of 20 bytes on every sent
	 */
	protected static int MAX_DATA_TO_READ_FROM_BUFFER = 15;
	/**
	 * Sending data to module
	 */
	protected FifoQueue mTxBuffer;
	/**
	 * Receiving data from module
	 */
	protected FifoQueue mRxBuffer;
	protected Handler sendDataHandler = new Handler();
	protected int SEND_DATA_TO_REMOTE_DEVICE_DELAY = 10;
	/**
	 * this is used to get the previously read data from the TX buffer and store
	 * it temporary into this variable
	 */
	protected StringBuilder mTxDest = new StringBuilder();
	/**
	 * this is used to get the previously read data from the RX buffer and store
	 * it temporary into this variable
	 */
	protected StringBuilder mRxDest = new StringBuilder();

	public VirtualSerialPortDevice(Activity activity,
			IBleBaseActivityUiCallback iBleBaseActivityUiCallback)
	{
		super(activity, iBleBaseActivityUiCallback);

		mRxBuffer = new FifoQueue();
		mTxBuffer = new FifoQueue();

		mFifoAndVspManagerState = FifoAndVspManagerState.WAITING;
		mVSPUiDeviceCallback = (VirtualSerialPortDeviceCallback) iBleBaseActivityUiCallback;
	}

	public FifoAndVspManagerState getFifoAndVspManagerState()
	{
		return mFifoAndVspManagerState;
	}

	public BluetoothGattCharacteristic getCharRx()
	{
		return mCharRx;
	}

	public BluetoothGattCharacteristic getCharTx()
	{
		return mCharTx;
	}

	public BluetoothGattCharacteristic getCharModemIn()
	{
		return mCharModemIn;
	}

	public BluetoothGattCharacteristic getCharModemOut()
	{
		return mCharModemOut;
	}

	public boolean isBufferSpaceAvailable()
	{
		return mIsBufferSpaceAvailableNewState;
	}

	public int getRxCounter()
	{
		return mRxCounter;
	}

	public int getTxCounter()
	{
		return mTxCounter;
	}

	public FifoQueue getRxBuffer()
	{
		return mRxBuffer;
	}

	public FifoQueue getTxBuffer()
	{
		return mTxBuffer;
	}

	public boolean isValidVspDevice()
	{
		return mIsValidVspDevice;
	}

	public void clearRxCounter()
	{
		mRxCounter = 0;
	}

	public void clearTxCounter()
	{
		mTxCounter = 0;
	}

	public void clearRxAndTxCounter()
	{
		mRxCounter = 0;
		mTxCounter = 0;
	}

	public enum FifoAndVspManagerState
	{
		WAITING, READY_TO_SEND_DATA, UPLOADING, UPLOADED, STOPPED, FAILED
	}

	/**
	 * Send data to remote device
	 * 
	 * @param dataToBeSend
	 *            the data to send to the remote device
	 */
	public void startDataTransfer(String dataToBeSend)
	{
		mFifoAndVspManagerState = FifoAndVspManagerState.UPLOADING;
		writeToFifoAndUploadDataToRemoteDevice(dataToBeSend);
	}

	/**
	 * reads from the RX buffer content based on the
	 * MAX_DATA_TO_READ_FROM_BUFFER and sends it to the remote device
	 */
	protected void uploadNextDataFromFifoToRemoteDevice()
	{
		if (mBluetoothGatt == null
				|| getConnectionState() == BluetoothProfile.STATE_DISCONNECTED)
		{
			return;
		}

		if (mTxBuffer.read(mTxDest, MAX_DATA_TO_READ_FROM_BUFFER) != 0)
		{
			String dataToWriteToRemoteBleDevice = mTxDest.toString();
			mTxDest.delete(0, MAX_DATA_TO_READ_FROM_BUFFER);

			Log.i(TAG, StringEscapeUtils
					.escapeJava("uploadNextDataFromFifoToRemoteDevice: "
							+ dataToWriteToRemoteBleDevice));

			sendToModule(dataToWriteToRemoteBleDevice);
		}
		else
		{
			onUploaded();
		}
	}

	/**
	 * writes the string data passed to the TX buffer and then sends the data to
	 * the remote device
	 * 
	 * @param data
	 *            the data to write to the TX buffer and to send to the remote
	 *            device
	 */
	protected void writeToFifoAndUploadDataToRemoteDevice(String data)
	{
		mTxBuffer.write(data);
		uploadNextDataFromFifoToRemoteDevice();
	};

	protected boolean sendToModule(String dataToBeSend)
	{
		if (mBluetoothGatt != null && mCharRx != null && dataToBeSend != null)
		{
			mCharRx.setValue(dataToBeSend);

			System.out.println("char data Just send:"
					+ mCharRx.getStringValue(0));

			return mBluetoothGatt.writeCharacteristic(mCharRx);
		}

		return false;
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
			case BluetoothProfile.STATE_CONNECTING:
				// Might be used in the future.
				break;

			case BluetoothProfile.STATE_CONNECTED:
				mFifoAndVspManagerState = FifoAndVspManagerState.WAITING;
				break;

			case BluetoothProfile.STATE_DISCONNECTING:
				// Might be used in the future.
				break;

			case BluetoothProfile.STATE_DISCONNECTED:
				mFifoAndVspManagerState = FifoAndVspManagerState.WAITING;
				setToDefault();
				flushBuffers();
				break;
			}
		}
		else
		{
			mFifoAndVspManagerState = FifoAndVspManagerState.WAITING;
			setToDefault();
			flushBuffers();
		}

	}

	@Override
	protected void onServiceFound(final BluetoothGattService service)
	{
		if (VSP_SERVICE.equals(service.getUuid()))
		{
			mIsValidVspDevice = true;
		}
	}

	@Override
	protected void onCharFound(final BluetoothGattCharacteristic characteristic)
	{
		if (VSP_CHAR_RX.equals(characteristic.getUuid()))
		{
			mCharRx = characteristic;
		}
		else if (VSP_CHAR_TX.equals(characteristic.getUuid()))
		{
			// add to queue as we want to enable notifications
			addCharToQueue(characteristic);
		}
		else if (VSP_CHAR_MODEM_IN.equals(characteristic.getUuid()))
		{
			mCharModemIn = characteristic;
		}
		else if (VSP_CHAR_MODEM_OUT.equals(characteristic.getUuid()))
		{
			// add to queue as we want to enable notifications
			addCharToQueue(characteristic);
		}
	}

	@Override
	protected void onCharsFoundCompleted()
	{
		super.onCharsFoundCompleted();

		if (!mIsValidVspDevice)
		{
			mActivity.runOnUiThread(new Runnable()
			{
				@Override
				public void run()
				{
					Toast.makeText(mActivity,
							"Device does not have the VSP service",
							Toast.LENGTH_SHORT).show();
				}
			});

			disconnect();
		}
		else
		{
			executeCharacteristicsQueue();
		}

		mVSPUiDeviceCallback.onUiVspServiceFound(mIsValidVspDevice);
	}

	@Override
	protected void onCharacteristicsQueueCompleted()
	{
		Log.i(TAG, "QUEUE COMPLETED");
		enableModemIn();
	}

	@Override
	public void onCharacteristicWrite(BluetoothGatt gatt,
			BluetoothGattCharacteristic characteristic, int status)
	{
		super.onCharacteristicWrite(gatt, characteristic, status);

		UUID serviceUUID = characteristic.getService().getUuid();
		UUID charUUID = characteristic.getUuid();

		if (status == BluetoothGatt.GATT_SUCCESS) {
			if (VSP_SERVICE.equals(serviceUUID)) {
				if (VSP_CHAR_RX.equals(charUUID)) {
					Log.i(TAG, "Data was sent successfully");

					// keep count of total bytes send to the remote BLE device
					mTxCounter = mTxCounter + characteristic.getValue().length;
					onVspSendDataSuccess(gatt, characteristic);
				}
			}
		}
	}

	@Override
	public void onCharacteristicChanged(BluetoothGatt gatt,
			BluetoothGattCharacteristic ch)
	{
		UUID serviceUUID = ch.getService().getUuid();
		UUID charUUID = ch.getUuid();

		if (VSP_SERVICE.equals(serviceUUID))
		{
			if (VSP_CHAR_TX.equals(charUUID))
			{
				Log.i(TAG, "Data was received successfully");

				// keep count of total bytes received from the remote BLE device
				mRxCounter = mRxCounter + ch.getValue().length;
				onVspReceiveData(gatt, ch);
			}
			else if (VSP_CHAR_MODEM_OUT.equals(charUUID))
			{
				/*
				 * getting the buffer space state and then we use it to identify
				 * if there is space in the remote device or not.
				 * 
				 * when the buffer old state is 0 and the buffer new state is 1
				 * then it means that there was a transition from the remote
				 * device not able to receive data to able to receive data.
				 */
				boolean isBufferSpaceAvailableOldState = mIsBufferSpaceAvailableNewState;

				int isBufferSpaceAvailableNewState = ch.getIntValue(
						BluetoothGattCharacteristic.FORMAT_UINT8, 0);

				mIsBufferSpaceAvailableNewState = isBufferSpaceAvailableNewState == 1;

				Log.i(TAG, "Was the buffer full previously: "
						+ isBufferSpaceAvailableOldState);
				Log.i(TAG, "Is the buffer full now: "
						+ isBufferSpaceAvailableNewState);

				onVspIsBufferSpaceAvailable(isBufferSpaceAvailableOldState,
						mIsBufferSpaceAvailableNewState);
			}
		}
	}

	/**
	 * for notifying the remote device that we can't send anymore data
	 */
	public void enableModemIn()
	{
		if (mCharModemIn != null)
		{
			byte[] enable =
			{
				(byte) 1
			};
			mCharModemIn.setValue(enable);
			mBluetoothGatt.writeCharacteristic(mCharModemIn);
		}
	}

	/**
	 * Clears all values
	 */
	private void setToDefault()
	{
		mIsValidVspDevice = false;
		mCharTx = null;
		mCharRx = null;
		mCharModemOut = null;
		mCharModemIn = null;
	}

	/**
	 * clears the RX buffer and the TX buffer
	 */
	protected void flushBuffers()
	{
		mRxBuffer.flush();
		mTxBuffer.flush();
	};

	/**
	 * override this method to define what data to sent to the remote BLE device
	 */
	protected void uploadNextData()
	{}

	protected void onUploaded()
	{
		mFifoAndVspManagerState = FifoAndVspManagerState.UPLOADED;
		flushBuffers();
	}

	/**
	 * used when sending data fails because of a response error from the remote
	 * device and not because of a disconnection issue. For example if the
	 * memory of the module has become full it will give a response error that
	 * it cannot store any more data
	 */
	protected void onUploadFailed(final String errorCode)
	{
		mFifoAndVspManagerState = FifoAndVspManagerState.FAILED;
		flushBuffers();
	}

	/**
	 * callback for notifying if the VSP service was found
	 * 
	 * @param found
	 *            true if VSP service was found, otherwise false
	 */
	protected void onVspServiceFound(final boolean found)
	{}

	/**
	 * Callback for whenever data was send to the remote device successful
	 * 
	 * @param gatt
	 *            GATT client
	 * @param ch
	 *            the RX characteristic with the updated value
	 */
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
						uploadNextData();
					}
				}
			}
		}, SEND_DATA_TO_REMOTE_DEVICE_DELAY);
	}

	/**
	 * Callback for whenever data failed to be send to the remote device
	 * 
	 * @param gatt
	 *            GATT client
	 * @param ch
	 *            the RX characteristic with the updated value
	 * @param status
	 *            error of the failure
	 */
	public void onVspSendDataFailure(final BluetoothGatt gatt,
			final BluetoothGattCharacteristic ch, final int status)
	{}

	/**
	 * Callback for when data is received from the remote device
	 * 
	 * @param gatt
	 *            GATT client
	 * @param ch
	 *            the TX characteristic with the updated value
	 */
	public void onVspReceiveData(final BluetoothGatt gatt,
			final BluetoothGattCharacteristic ch)
	{}

	/**
	 * Callback that notifies the android device if the remote device can
	 * currently accept data or not
	 * 
	 * if the new state is false it means it cannot receive any more data as its
	 * buffer is full, when true it can receive data
	 * 
	 * @param isBufferSpaceAvailableOldState
	 *            the old state of the buffer
	 * @param isBufferSpaceAvailableNewState
	 *            the new state of the buffer
	 */
	public void onVspIsBufferSpaceAvailable(
			final boolean isBufferSpaceAvailableOldState,
			final boolean isBufferSpaceAvailableNewState)
	{

		if (mFifoAndVspManagerState == FifoAndVspManagerState.UPLOADING) {/*
		 * callback for what to do when data was send successfully from the
		 * android device and when the module buffer was full and now it has
		 * been cleared, which means it now has available space
		 */
			if (!isBufferSpaceAvailableOldState
					&& isBufferSpaceAvailableNewState) {
				uploadNextData();
			}
		}
	}

}