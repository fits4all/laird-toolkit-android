/*******************************************************************************
 * Copyright (c) 2014 Laird Technologies. All Rights Reserved.
 * 
 * The information contained herein is property of Laird Technologies.
 * Licensees are granted free, non-transferable use of the information. NO WARRANTY of ANY KIND is provided. 
 * This heading must NOT be removed from the file.
 *******************************************************************************/

package com.lairdtech.bt.classic;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

/**
 * Represents a remote BT classic device.
 * <p>
 * When a class extends this class it immediately has access to all the BT
 * classic methods needed to initialise a BT classic remote device connection
 * and communications
 */
public abstract class BaseBtClassicDeviceManager
{
	private final static String TAG = "BaseBtClassicDeviceManager";
	private Activity mActivity;

	private BluetoothDevice mBluetoothDevice = null;
	private ConnectThread mConnectThread;
	private BluetoothSocket mBluetoothSocket;
	private ConnectedThread mConnectedThread;
	private boolean mIsConnecting = false;
	private boolean mIsConnected = false;

	private InputStream mInputStream;
	private OutputStream mOutputStream;

	private UUID mUuidToConnectTo;
	/*
	 * defines how many bytes to be read from the connected remote device
	 * whenever data is received, default value is 1024 bytes
	 */
	private final int DEFAULT_TOTAL_BYTES_TO_READ_FROM_REMOTE_DEVICE = 1024;

	/**
	 * @param activity
	 * @param bluetoothDevice
	 *            the bluetooth device to associate this instance with
	 * @param uuidToConnectTo
	 *            the UUID to connect this remote device socket with
	 */
	public BaseBtClassicDeviceManager(Activity activity,
			BluetoothDevice bluetoothDevice, UUID uuidToConnectTo)
	{
		if (activity == null || bluetoothDevice == null)
			throw new NullPointerException(
					"activity or bluetoothDevice parameter passed is null!");

		mActivity = activity;
		mBluetoothDevice = bluetoothDevice;
		mUuidToConnectTo = uuidToConnectTo;
		mConnectThread = new ConnectThread();

	};

	/**
	 * @param activity
	 * @param uuidToConnectTo
	 *            the UUID to connect this remote device socket with
	 */
	public BaseBtClassicDeviceManager(Activity activity, UUID uuidToConnectTo)
	{
		if (activity == null)
			throw new NullPointerException("activity parameter passed is null!");

		mActivity = activity;
		mUuidToConnectTo = uuidToConnectTo;
		mConnectThread = new ConnectThread();
	};

	public boolean isConnecting()
	{
		return mIsConnecting;
	}

	public boolean isConnected()
	{
		return mIsConnected;
	}

	/**
	 * Connect to the remote BT Classic device
	 */
	public void connect()
	{
		if (mConnectThread == null)
			throw new NullPointerException(
					"BluetoothSocket is not initialised!");

		mIsConnecting = true;
		mConnectThread.start();
	}

	/** Will cancel an in-progress connection, and close the socket */
	public void disconnect()
	{
		if (mConnectThread == null)
			throw new NullPointerException(
					"BluetoothSocket is not initialised!");

		try
		{
			unregisterReceiver();
			mIsConnected = false;
			mIsConnecting = false;
			mBluetoothSocket.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		onBtClassicDisconnected();
	}

	/**
	 * Initiates listening of data from the connected remote BT Classic device.
	 * Whenever data is being retrieved the {@link #onBtClassicDataRead(byte[])}
	 * callback is called
	 */
	public void startDataListeningFromRemoteDevice(
			int totalBytesToReadFromRemoteDevice)
	{
		if (mConnectedThread == null)
			throw new NullPointerException("Streams are not initialised!");

		if (isConnected() == true)
		{
			mConnectedThread
					.startDataListeningFromRemoteDevice(totalBytesToReadFromRemoteDevice);
		}
	}

	/**
	 * Send data to the remote device
	 * 
	 * @param bytes
	 *            the data to send to the remote device
	 */
	public void writeDataToRemoteDevice(byte[] bytes)
	{
		if (mConnectedThread == null)
			throw new NullPointerException("Streams are not initialised!");

		try
		{
			mOutputStream.write(bytes);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Callback indicating success connection with the remote BT Classic device
	 */
	public void onBtClassicConnected()
	{}

	/**
	 * Callback indicating disconnection with the remote BT Classic device
	 */
	public void onBtClassicDisconnected()
	{}

	/**
	 * Callback indicating that connection to the remote BT Classic device has
	 * failed
	 */
	public void onBtClassicConnectFailed()
	{}

	/**
	 * Callback for when data gets retrieved from the Connected remote BT
	 * Classic device. To start listening for data from the connected remote BT
	 * Classic device call the {@link #startDataListeningFromRemoteDevice(int)}
	 * 
	 * @param buffer
	 *            the data that was received
	 */
	public void onBtClassicDataRead(byte[] buffer)
	{

	}

	/**
	 * the thread that is responsible for initiating a connection
	 */
	private class ConnectThread extends Thread
	{

		public ConnectThread()
		{
			// Get a BluetoothSocket to connect with the given BluetoothDevice
			try
			{
				registerReceiver();

				mBluetoothSocket = mBluetoothDevice
						.createRfcommSocketToServiceRecord(mUuidToConnectTo);
			}
			catch (IOException e)
			{
				onBtClassicConnectFailed();
			}
		}

		@Override
		public void run()
		{
			try
			{
				mIsConnected = false;
				mIsConnecting = true;

				/*
				 * Connect the device through the socket. This will block until
				 * it succeeds or throws an exception.
				 */
				mBluetoothSocket.connect();
				/*
				 * it didn't throw an exception so the broadcastReceiver will be
				 * called notifying us that we have connected with the remote
				 * device
				 */
			}
			catch (IOException connectException)
			{
				// Unable to connect; close the socket and get out
				try
				{
					mIsConnected = false;
					mIsConnecting = false;

					mBluetoothSocket.close();
					onBtClassicConnectFailed();
				}
				catch (IOException closeException)
				{
					onBtClassicConnectFailed();
				}
				return;
			}
		}
	}

	/**
	 * thread for when we are connected and it's used for receiving and sending
	 * data from/to the remote device
	 */
	private class ConnectedThread extends Thread
	{
		byte[] mDataReadBuffer;

		public ConnectedThread()
		{
			/*
			 * Get the input and output streams so that we can read incoming
			 * data and to write data to the remote device
			 */
			try
			{
				mInputStream = mBluetoothSocket.getInputStream();
				mOutputStream = mBluetoothSocket.getOutputStream();
			}
			catch (IOException e)
			{}
		}

		/*
		 * Note: if the data received is more than our buffer size for when
		 * receiving data then data will get lost (non-Javadoc)
		 * 
		 * @see java.lang.Thread#run()
		 */
		@Override
		public void run()
		{
			// Keep listening to the InputStream until an exception occurs
			while (true)
			{
				try
				{

					int bytesRead; // bytes returned from read()

					/*
					 * The buffer might have extra data, if the data that was
					 * sent from the remote device is less than the size of our
					 * buffer then the buffer will have extra unneeded data.
					 * 
					 * As we only want the data that was actually sent from the
					 * remote device, we get the actual bytes that were read. In
					 * this way we can use the bytesRead to go through the
					 * buffer again and get only the data that was sent if
					 * needed
					 */
					bytesRead = mInputStream.read(mDataReadBuffer);

					/*
					 * remove any extra empty data that might be in the buffer,
					 * this might occur if the size of the buffer we defined is
					 * larger than the data we received.
					 */
					if (mDataReadBuffer.length > bytesRead)
					{
						// remove extra data
						ByteArrayInputStream bais = new ByteArrayInputStream(
								mDataReadBuffer);
						// buffer that has the exact size as the data we
						// actually need
						byte[] actualDataReadBuffer = new byte[bytesRead];

						// store only the NOT extra data to the new buffer
						bais.read(actualDataReadBuffer);
						onBtClassicDataRead(actualDataReadBuffer);
					}
					else
					{
						// no extra data to remove
						onBtClassicDataRead(mDataReadBuffer);
					}
				}
				catch (IOException e)
				{
					Log.i(TAG, "IOException: " + e);
					break;
				}

			}

		}

		/**
		 * starts listening for data from the remote device. whenever data is
		 * received the callback
		 * {@link BtClassicDeviceBase#onDataReadFromRemoteDevice(byte[])} is
		 * called with the data received.
		 * 
		 * if the totalBytesToReadFromRemoteDevice parameter is <=0 then the
		 * default value will be set and it will read 1024 bytes.
		 * 
		 * @param totalBytesToReadFromRemoteDevice
		 *            the total bytes to read every time we receive data
		 */
		protected void startDataListeningFromRemoteDevice(
				int totalBytesToReadFromRemoteDevice)
		{

			if (totalBytesToReadFromRemoteDevice <= 0)
			{
				mDataReadBuffer = new byte[DEFAULT_TOTAL_BYTES_TO_READ_FROM_REMOTE_DEVICE];
			}
			else
			{
				mDataReadBuffer = new byte[totalBytesToReadFromRemoteDevice];
			}

			start();
		}
	}

	/**
	 * Create a BroadcastReceiver for ACTION_FOUND callback for disconnect
	 * callback's
	 */
	private final BroadcastReceiver mReceiver = new BroadcastReceiver()
	{
		public void onReceive(Context context, Intent intent)
		{
			String action = intent.getAction();
			if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action))
			{
				mIsConnecting = false;
				mIsConnected = true;

				mConnectedThread = new ConnectedThread();

				/*
				 * connection and input/output streams are ready
				 */
				onBtClassicConnected();

			}
			else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action))
			{
				try
				{
					mIsConnected = false;
					mIsConnecting = false;
					mBluetoothSocket.close();

				}
				catch (IOException e)
				{
					e.printStackTrace();
				}

				onBtClassicDisconnected();
				unregisterReceiver();
			}
		}
	};

	private void registerReceiver()
	{
		// Register the BroadcastReceiver
		IntentFilter filter = new IntentFilter();
		filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
		filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);

		// Don't forget to unregister once we are done
		mActivity.registerReceiver(mReceiver, filter);
	}

	private void unregisterReceiver()
	{
		// unRegister the BroadcastReceiver
		mActivity.unregisterReceiver(mReceiver);
	}

}