package com.lairdtech.lairdtoolkit;

import android.bluetooth.BluetoothProfile;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.lairdtech.bt.ble.BleBaseDeviceManager;

/**
 * Base activity for all the activities that are able to connect to multiple BLE
 * devices
 * 
 * @author Kyriakos.Alexandrou
 */
public abstract class BleBaseMultipleConnectedDevicesActivity extends
		BaseActivity implements IBleBaseActivityUiCallback
{
	private final static String TAG = "BleBaseMultipleConnectedDevicesActivity";

	protected BleBaseMultipleConnectedDevicesAdapter mListConnectedDevicesHandler = null;
	protected ListView mLvConnectedDevices;

	protected TextView mValueName, mValueDeviceAddress, mValueRSSI,
			mValueBattery;
	protected Button mBtnScan;

	@Override
	protected void onCreate(Bundle savedInstanceState, int layoutResID)
	{
		super.onCreate(savedInstanceState, layoutResID);
		toggleHint();
	}

	@Override
	public void bindViews()
	{
		super.bindViews();

		mLvConnectedDevices = findViewById(R.id.lvConnectedDevices);
		mValueName = findViewById(R.id.valueDeviceName);
		mValueDeviceAddress = findViewById(R.id.valueDeviceAddress);
		mValueRSSI = findViewById(R.id.valueDeviceRssi);
		mValueBattery = findViewById(R.id.valueDeviceBattery);
		mBtnScan = findViewById(R.id.btnScan);
	}

	@Override
	public void setAdapters()
	{
		super.setAdapters();
		mLvConnectedDevices.setAdapter(mListConnectedDevicesHandler);
	}

	@Override
	public void setListeners()
	{
		// set onClickListener for the scan button
		mBtnScan.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				switch (v.getId())
				{
				case R.id.btnScan:
				{
					if (!mBluetoothAdapterWrapper.isEnabled())
					{
						Log.e(TAG, "Bluetooth must be on to start scanning.");
						Toast.makeText(mActivity,
								"Bluetooth must be on to start scanning.",
								Toast.LENGTH_SHORT).show();
						return;
					}
					else
					{
						// do a scan operation
						if (isPrefPeriodicalScan)
						{
							mBluetoothAdapterWrapper.startBleScanPeriodically();
						}
						else
						{
							mBluetoothAdapterWrapper.startBleScan();
						}

						mDialogFoundDevices.show();
					}
					invalidateUiListConnectedDevicesHandler();
					break;
				}
				}
			}
		});

		/*
		 * what to do when a connected device is selected from the ListView
		 */
		mLvConnectedDevices.setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> arg0, View view,
					final int position, long id)
			{
				runOnUiThread(new Runnable()
				{

					@Override
					public void run()
					{
						BleBaseDeviceManager device = mListConnectedDevicesHandler
								.getDevice(position);

						if (device.getConnectionState() == BluetoothProfile.STATE_CONNECTED
								|| device.getConnectionState() == BluetoothProfile.STATE_CONNECTING)
						{
							Log.i("TAG", "disconnect with device at position: "
									+ position);
							device.disconnect();
						}
						else
						{
							Log.i("TAG",
									"already disconnected, just remove the device form the listview at position: "
											+ position);
							mListConnectedDevicesHandler.remove(position);
						}
					}
				});
			}
		});
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		if (item.getItemId() == android.R.id.home) {
			mListConnectedDevicesHandler.disconnectFromDevices();
			finish();
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onPause()
	{
		super.onPause();

		if (isInNewScreen || isPrefRunInBackground)
		{
			// let the app run normally in the background
		}
		else
		{
			mListConnectedDevicesHandler.disconnectFromDevices();
		}
	}

	@Override
	public void onBackPressed()
	{
		mListConnectedDevicesHandler.disconnectFromDevices();
		finish();
	}

	@Override
	protected void onDialogFoundDevicesItemClick(AdapterView<?> arg0,
			View view, int position, long id)
	{
		/*
		 * override this to create the specific device (ThermometerManager,
		 * HearRateManager etc.) that is needed once it's clicked
		 */
	}

	@Override
	protected void onDialogFoundDevicesCancel(DialogInterface arg0)
	{
		mBluetoothAdapterWrapper.stopBleScan();
		invalidateUiListConnectedDevicesHandler();
	}

	@Override
	public void onUiConnecting()
	{}

	@Override
	public void onUiDisconnecting()
	{}

	@Override
	public void onUiConnected()
	{
		invalidateUiListConnectedDevicesHandler();
	}

	@Override
	public void onUiDisconnected(int status)
	{
		removeDisconnectedDeviceFromView();
		invalidateUiListConnectedDevicesHandler();
	}

	@Override
	public void onUiBatteryRead(String result)
	{
		invalidateUiListConnectedDevicesHandler();
	}

	@Override
	public void onUiReadRemoteRssi(final int rssi)
	{
		invalidateUiListConnectedDevicesHandler();
	}

	private void removeDisconnectedDeviceFromView()
	{
		/*
		 * looping through all the connected devices and checking which one just
		 * got disconnected, so that we can remove it from the UI
		 */
		for (int i = 0; i < mListConnectedDevicesHandler.getCount(); i++)
		{
			final int tempI = i;
			/*
			 * if we are not connected with the current device remove it from
			 * the listview
			 */
			if (mListConnectedDevicesHandler.getDevice(i).getConnectionState() == BluetoothProfile.STATE_DISCONNECTED)
			{

				mActivity.runOnUiThread(new Runnable()
				{
					@Override
					public void run()
					{
						mListConnectedDevicesHandler.remove(tempI);
						mListConnectedDevicesHandler.notifyDataSetChanged();
					}
				});
			}
		}
	}

	@Override
	public void uiInvalidateBtnState()
	{
		invalidateOptionsMenu();
	}

	protected void invalidateUiListConnectedDevicesHandler()
	{
		runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				invalidateOptionsMenu();
				mListConnectedDevicesHandler.notifyDataSetChanged();
				toggleHint();
			}
		});
	}

	/**
	 * override this to toggle the display of the hint message in a specific
	 * multiple connected devices screen
	 * (BloodPressureMultipleConnectedDevicesActivity etc.)
	 */
	protected void toggleHint()
	{}
}
