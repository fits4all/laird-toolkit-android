package com.lairdtech.lairdtoolkit.otadevice;

import org.apache.commons.lang3.StringEscapeUtils;

import android.app.Activity;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.os.Handler;
import android.util.Log;

import com.lairdtech.bt.ble.vsp.FileAndFifoAndVspManager;
import com.lairdtech.lairdtoolkit.IBleBaseActivityUiCallback;
import com.lairdtech.misc.DataManipulation;

/**
 * Responsible to read from a text file and send the appropriate length of data
 * to the VSPManager
 */
public class OTAManager extends FileAndFifoAndVspManager
{
	final static public String TAG = "OTAManager";

	private static final int AFTER_MODULE_RESET_TIMER_FOR_DISCONNECT = 300;

	private CommunicationState mCommunicationState;
	private OTAManagerUiCallback mOTAManagerUiCallback;

	public OTAManager(Activity activity,
			IBleBaseActivityUiCallback iBleBaseActivityUiCallback)
	{
		super(activity, iBleBaseActivityUiCallback);

		mOTAManagerUiCallback = (OTAManagerUiCallback) iBleBaseActivityUiCallback;
		mCommunicationState = CommunicationState.WAITING;

		SEND_DATA_TO_REMOTE_DEVICE_DELAY = 1;
		MAX_DATA_TO_READ_FROM_BUFFER = 16;
	}

	private String getDELCommand()
	{
		if (mBluetoothGatt == null)
		{
			return null;
		}
		return "AT+DEL \"" + mFileWrapper.getModuleFileName() + "\"\r";
	}

	private String getFOWCommand()
	{
		if (mBluetoothGatt == null)
		{
			return null;
		}
		return "AT+FOW \"" + mFileWrapper.getModuleFileName() + "\"\r";
	}

	private String getNextFileContentInHEXString()
	{
		if (mBluetoothGatt == null)
		{
			return null;
		}
		String start = "AT+FWRH \"";
		String end = "\"\r";
		String content = mFileWrapper
				.readNextHEXStringFromFile(MAX_DATA_TO_READ_FROM_TEXT_FILE);
		String result = null;

		if (content != null)
		{
			result = start + content + end;
		}
		return result;
	}

	private String getFCLCommand()
	{
		if (mBluetoothGatt == null)
		{
			return null;
		}
		return "AT+FCL\r";
	}

	private void closeOpenedFile()
	{
		flushBuffers();
		writeToFifoAndUploadDataToRemoteDevice("\r" + getFCLCommand());
	}

	public enum CommunicationState
	{
		WAITING, DEL, FOW, FWRH, FCL
	}

	public void startDataTransfer()
	{
		super.initialiseFileTransfer();
		mCommunicationState = CommunicationState.DEL;
		writeToFifoAndUploadDataToRemoteDevice(getDELCommand());
	}

	public void stopFileUploading()
	{
		mFifoAndVspManagerState = FifoAndVspManagerState.STOPPED;
		mCommunicationState = CommunicationState.WAITING;
		closeOpenedFile();
	}

	@Override
	public void onVspSendDataSuccess(final BluetoothGatt gatt,
			final BluetoothGattCharacteristic ch)
	{
		mOTAManagerUiCallback.onUiSendDataSuccess(ch.getStringValue(0));
		super.onVspSendDataSuccess(gatt, ch);
	}

	@Override
	public void onVspReceiveData(BluetoothGatt gatt,
			BluetoothGattCharacteristic ch)
	{
		mRxBuffer.write(ch.getStringValue(0));

		while (mRxBuffer.read(mRxDest, "\r") != 0)
		{
			Log.i(TAG,
					"Data received: "
							+ StringEscapeUtils.escapeJava(mRxDest.toString()));

			if (mFifoAndVspManagerState == FifoAndVspManagerState.UPLOADING) {// success code received from the remote device
				if (mRxDest.toString().contains("\n00\r")) {
					mRxDest.delete(0, mRxDest.length());

				}
				// error code received from remote device
				else if (mRxDest.toString().contains("\n01\t")) {
					String errorCode = mRxDest.toString();
					mRxDest.delete(0, mRxDest.length());
					/*
					 * get only what is between \t and \r. that is the error
					 * code
					 */
					errorCode = DataManipulation.stripStringValue("\t", "\r",
							errorCode);

					onUploadFailed(errorCode);
				}
			}
		}
	}

	@Override
	protected void uploadNextData()
	{
		switch (mCommunicationState)
		{
		case DEL:
			if (mTxBuffer.getSize() > 0)
			{
				// more data to write
				uploadNextDataFromFifoToRemoteDevice();

			}
			else
			{
				// done deleting the file, start the opening procedure
				mCommunicationState = CommunicationState.FOW;
				writeToFifoAndUploadDataToRemoteDevice(getFOWCommand());
			}
			break;
		case FOW:
			if (mTxBuffer.getSize() > 0)
			{
				// more data to write
				uploadNextDataFromFifoToRemoteDevice();

			}
			else
			{
				// done opening the file for writing, start writing file content
				mCommunicationState = CommunicationState.FWRH;
				writeToFifoAndUploadDataToRemoteDevice(getNextFileContentInHEXString());
			}
			break;
		case FWRH:
			if (mTxBuffer.getSize() > 0)
			{
				// more data to write
				uploadNextDataFromFifoToRemoteDevice();
			}
			else if (mTxBuffer.getSize() <= 0)
			{
				/*
				 * read more data from the text file
				 */
				final String content = getNextFileContentInHEXString();

				if (content != null)
				{
					writeToFifoAndUploadDataToRemoteDevice(content);
				}
				else
				{
					// done writing text file content, start the closure of the
					// file
					mCommunicationState = CommunicationState.FCL;
					writeToFifoAndUploadDataToRemoteDevice(getFCLCommand());
				}
			}
			break;
		case FCL:
			if (mTxBuffer.getSize() > 0)
			{
				// more data to write
				uploadNextDataFromFifoToRemoteDevice();
			}
			else
			{
				/*
				 * file has been closed and uploaded to the remote device
				 */
				Log.i(TAG, "File has been uploaded to the remote device");

				mCommunicationState = CommunicationState.WAITING;
				onUploaded();
			}
			break;
		default:
			break;

		}
	}

	@Override
	public void onUploaded()
	{
		super.onUploaded();
		mOTAManagerUiCallback.onUiUploaded();
	}

	public void resetModule(boolean reset)
	{
		if (reset == true)
		{
			startDataTransfer("atz\r");

			Handler disconnectAfterResetTimerHandler = new Handler();
			disconnectAfterResetTimerHandler.postDelayed(new Runnable()
			{
				@Override
				public void run()
				{
					disconnect();
				}
			}, AFTER_MODULE_RESET_TIMER_FOR_DISCONNECT);
		}
	}

	@Override
	public void onUploadFailed(final String errorCode)
	{
		super.onUploadFailed(errorCode);
		mCommunicationState = CommunicationState.WAITING;
		mOTAManagerUiCallback.onUiReceiveErrorData(errorCode);
	}
}