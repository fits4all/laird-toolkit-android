package com.lairdtech.lairdtoolkit.batchdevice;

import org.apache.commons.lang3.StringEscapeUtils;

import android.app.Activity;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.net.Uri;
import android.util.Log;

import com.lairdtech.bt.ble.vsp.FileAndFifoAndVspManager;
import com.lairdtech.lairdtoolkit.IBleBaseActivityUiCallback;
import com.lairdtech.misc.DataManipulation;

/**
 * Responsible to read from a textfile a series of AT commands, send those
 * commands to the module and display the responses from the remote BLE device.
 */
public class BatchManager extends FileAndFifoAndVspManager
{
	final static private String TAG = "BatchManager";

	private BatchManagerUiCallback mBatchManagerUiCallback;

	public BatchManager(Activity activity,
			IBleBaseActivityUiCallback iBleBaseActivityUiCallback)
	{
		super(activity, iBleBaseActivityUiCallback);
		mBatchManagerUiCallback = (BatchManagerUiCallback) iBleBaseActivityUiCallback;
	}

	public void startFileTransfer()
	{
		if (getBluetoothGatt() == null)
			return;

		super.initialiseFileTransfer();
		writeToFifoAndUploadDataToRemoteDevice(getNextFileContentUntilSpecificChar("\r"));
	}

	/**
	 * change this manager from UPLOADING state to STOPPED state
	 */
	public void stopFileUploading()
	{
		mFifoAndVspManagerState = FifoAndVspManagerState.STOPPED;
	}

	/**
	 * returns the next available command from the textfile that was set using
	 * the {@link #setFile(Uri uri)}
	 * 
	 * @param readUntil
	 *            the string data to search for
	 * @return returns the data from index 0 until the index of the readUntil
	 *         string
	 */
	private String getNextFileContentUntilSpecificChar(String readUntil)
	{
		if (getBluetoothGatt() == null)
			return null;
		return mFileWrapper.readUntilASpecificChar(readUntil);
	}

	@Override
	public void onVspSendDataSuccess(BluetoothGatt gatt,
			BluetoothGattCharacteristic ch)
	{
		/*
		 * no need to call the super method here as we only want to send the
		 * next data to the remote device after we receive a response from the
		 * remote device
		 */
		mBatchManagerUiCallback.onUiSendDataSuccess(ch.getStringValue(0));
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
					mBatchManagerUiCallback.onUiReceiveSuccessData(mRxDest
							.toString());

					mRxDest.delete(0, mRxDest.length());

					if (isBufferSpaceAvailable()) {
						uploadNextData();
					}
				}
				// error code received from remote device
				else if (mRxDest.toString().contains("\n01\t")) {
					String errorCode = mRxDest.toString();

					/*
					 * get only what is between \t and \r. That's the error code
					 */
					errorCode = DataManipulation.stripStringValue("\t", "\r",
							errorCode);

					mRxDest.delete(0, mRxDest.length());

					onUploadFailed(errorCode);
				}
			}
		}
	}

	protected void uploadNextData()
	{
		if (mTxBuffer.getSize() > 0)
		{
			// more data to write
			uploadNextDataFromFifoToRemoteDevice();
		}
		else if (mTxBuffer.getSize() <= 0)
		{
			/*
			 * read more data from the data file
			 */
			final String content = getNextFileContentUntilSpecificChar("\r");

			if (content == null)
			{
				onUploaded();
			}
			else if (mFileWrapper.getIsEOF()
					&& !(content.contains("\r")))
			{
				/*
				 * if the last values of the file do not contain a "\r" the we
				 * add a "\r" so that we can receive a response from the module
				 */
				writeToFifoAndUploadDataToRemoteDevice(content + "\r");
			}
			else
			{
				writeToFifoAndUploadDataToRemoteDevice(content);
			}
		}
	}

	@Override
	public void onUploaded()
	{
		super.onUploaded();
		mBatchManagerUiCallback.onUiUploaded();
	}

	@Override
	public void onUploadFailed(final String errorCode)
	{
		super.onUploadFailed(errorCode);
		mBatchManagerUiCallback.onUiReceiveErrorData(errorCode);
	}
}