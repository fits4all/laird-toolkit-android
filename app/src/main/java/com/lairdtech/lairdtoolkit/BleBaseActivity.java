package com.lairdtech.lairdtoolkit;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.DialogInterface;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.lairdtech.bt.ble.BleBaseDeviceManager;

/**
 * Base activity for all the activities that will be doing BLE functionalities
 * 
 * @author Kyriakos.Alexandrou
 */
public abstract class BleBaseActivity extends BaseActivity implements
		IBleBaseActivityUiCallback
{
	private final static String TAG = "BleBaseActivity";
	private BleBaseDeviceManager mBleBaseDeviceManager;

	protected TextView mValueName, mValueDeviceAddress, mValueRSSI,
			mValueBattery;
	protected Button mBtnScan;

	@Override
	public void onCreate(Bundle savedInstanceState, int layoutResID)
	{
		super.onCreate(savedInstanceState, layoutResID);
	}

	/**
	 * Set the mBleBaseDeviceManager to the specific device manager for each
	 * application within the toolkit. This ensures it is not null
	 * 
	 * @param bleBaseDeviceManager
	 *            the specific BLE manager to set it to.
	 */
	protected void setBleBaseDeviceManager(
			BleBaseDeviceManager bleBaseDeviceManager)
	{
		mBleBaseDeviceManager = bleBaseDeviceManager;
	}

	/**
	 * get the generic BLE base manager and cast it in order to get the correct
	 * manager.
	 * <p>
	 * example: (ThermometerManager) getBleBaseDeviceManager();
	 * 
	 * @return
	 */
	protected BleBaseDeviceManager getBleBaseDeviceManager()
	{
		return mBleBaseDeviceManager;
	}

	@Override
	public void bindViews()
	{
		super.bindViews();
		mBtnScan = findViewById(R.id.btnScan);
	}

	@Override
	public void setListeners()
	{
		super.setListeners();

		// set onClickListener for the scan button
		mBtnScan.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if (v.getId() == R.id.btnScan) {
					if (!mBluetoothAdapterWrapper.isEnabled()) {
						Log.w(TAG, "Bluetooth must be on to start scanning.");
						Toast.makeText(getApplication(),
								"Bluetooth must be on to start scanning.",
								Toast.LENGTH_SHORT).show();
						return;
					}

					if (!isLocationEnabled(getApplicationContext())) {
						Log.w(TAG, "Location services must be on to start scanning.");
						Toast.makeText(getApplication(),
								"Location services must be on to start scanning.",
								Toast.LENGTH_SHORT).show();
						return;
					}

					if (mBleBaseDeviceManager.getConnectionState() != BluetoothProfile.STATE_CONNECTED
							&& mBleBaseDeviceManager.getConnectionState() != BluetoothProfile.STATE_CONNECTING) {

						// do a scan operation
						if (isPrefPeriodicalScan) {
							mBluetoothAdapterWrapper.startBleScanPeriodically();
						} else {
							mBluetoothAdapterWrapper.startBleScan();
						}

						mDialogFoundDevices.show();
					} else if (mBleBaseDeviceManager.getConnectionState() == BluetoothProfile.STATE_CONNECTED) {
						mBleBaseDeviceManager.disconnect();
					} else if (mBleBaseDeviceManager.getConnectionState() == BluetoothProfile.STATE_CONNECTING) {
						Toast.makeText(getApplication(), "Wait for connection!", Toast.LENGTH_SHORT)
								.show();
					}

					uiInvalidateBtnState();
				}
			}
		});
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		if (item.getItemId() == android.R.id.home) {
			mBleBaseDeviceManager.disconnect();
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
			// stop scanning or disconnect if we are connected
			if (mBluetoothAdapterWrapper.isBleScanning())
			{
				mBluetoothAdapterWrapper.stopBleScan();
			}
			else if (mBleBaseDeviceManager.getConnectionState() == BluetoothProfile.STATE_CONNECTING
					|| mBleBaseDeviceManager.getConnectionState() == BluetoothProfile.STATE_CONNECTED)
			{
				mBleBaseDeviceManager.disconnect();
			}
		}
	}

	@Override
	public void onBackPressed()
	{
		mBleBaseDeviceManager.disconnect();
		finish();
	}

	@Override
	public void uiInvalidateBtnState()
	{
		this.runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{

				if (mBleBaseDeviceManager.getConnectionState() != BluetoothProfile.STATE_CONNECTED
						&& mBleBaseDeviceManager.getConnectionState() != BluetoothProfile.STATE_CONNECTING)
				{
					mBtnScan.setText(R.string.btn_scan);
				}
				else if (mBleBaseDeviceManager.getConnectionState() == BluetoothProfile.STATE_CONNECTED)
				{
					mBtnScan.setText(R.string.btn_disconnect);
				}
				else if (mBleBaseDeviceManager.getConnectionState() == BluetoothProfile.STATE_CONNECTING)
				{
					mBtnScan.setText(R.string.btn_connecting);
				}

				invalidateOptionsMenu();
			}
		});
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
		runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				mBtnScan.setText(R.string.btn_disconnect);
			}
		});
		invalidateUI();
	}

	@Override
	public void onUiDisconnected(final int status)
	{
		// set views to default
		runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				Log.i(TAG, "onUiDisconnected status: " + status);
				mBtnScan.setText(getResources().getString(R.string.btn_scan));
			}
		});
	}

	@Override
	public void onUiBatteryRead(final String valueBattery)
	{
		runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				mValueBattery.setText(valueBattery);
			}
		});
	}

	@Override
	public void onUiReadRemoteRssi(final int valueRSSI)
	{
		runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				mValueRSSI.setText(valueRSSI + " db");
			}
		});
	}

	@Override
	protected void onDialogFoundDevicesItemClick(AdapterView<?> arg0,
			View view, int position, long id)
	{
		final BluetoothDevice device = mListFoundDevicesHandler
				.getDevice(position);
		if (device == null)
			return;

		mBluetoothAdapterWrapper.stopBleScan();
		mDialogFoundDevices.dismiss();
		mBleBaseDeviceManager.connect(device, false);
		uiInvalidateBtnState();
	}

	@Override
	protected void onDialogFoundDevicesCancel(DialogInterface arg0)
	{
		mBluetoothAdapterWrapper.stopBleScan();
		uiInvalidateBtnState();
	}

	/**
	 * bind the device name, device address, device RSSI and device battery to
	 * its views
	 */
	protected void bindCommonViews()
	{
		mValueName = (TextView) findViewById(R.id.valueDeviceName);
		mValueDeviceAddress = (TextView) findViewById(R.id.valueDeviceAddress);
		mValueRSSI = (TextView) findViewById(R.id.valueDeviceRssi);
		mValueBattery = (TextView) findViewById(R.id.valueDeviceBattery);
	}

	/**
	 * set device battery, device name, device address
	 */
	protected void setCommonViews()
	{
		mValueBattery
				.setText(getResources().getString(R.string.non_applicable));
		mValueName
				.setText(mBleBaseDeviceManager.getBluetoothDevice().getName());
		mValueDeviceAddress.setText(mBleBaseDeviceManager.getBluetoothDevice()
				.getAddress());
	}

	/**
	 * set device battery, device name, device address to non applicable
	 */
	protected void setCommonViewsToNonApplicable()
	{
		mValueBattery
				.setText(getResources().getString(R.string.non_applicable));
		mValueName.setText(getResources().getString(R.string.non_applicable));
		mValueRSSI.setText(getResources().getString(R.string.non_applicable));
		mValueDeviceAddress.setText(getResources().getString(
				R.string.non_applicable));
	}

	@SuppressWarnings("deprecation")
	public static Boolean isLocationEnabled(Context context) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
			// This is a new method provided in API 28
			LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
			return lm.isLocationEnabled();
		} else {
			// This was deprecated in API 28
			int mode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE,
					Settings.Secure.LOCATION_MODE_OFF);
			return (mode != Settings.Secure.LOCATION_MODE_OFF);
		}
	}

}
