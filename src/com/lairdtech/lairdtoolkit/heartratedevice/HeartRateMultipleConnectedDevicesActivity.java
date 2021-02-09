
package com.lairdtech.lairdtoolkit.heartratedevice;

import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import com.lairdtech.bt.ble.BleBaseDeviceManager;
import com.lairdtech.lairdtoolkit.BleBaseMultipleConnectedDevicesActivity;
import com.lairdtech.lairdtoolkit.R;

public class HeartRateMultipleConnectedDevicesActivity extends
		BleBaseMultipleConnectedDevicesActivity implements
		HeartRateActivityUiCallback
{
	private final static String TAG = "HeartRateMultipleConnectedDevicesActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState,
				R.layout.activity_multiple_connected_devices);

		initialiseDialogAbout(getResources().getString(
				R.string.about_multiple_heart_rate));
		initialiseDialogFoundDevices("HRM");

		toggleHint();
	}

	@Override
	public void setAdapters()
	{
		mListConnectedDevicesHandler = new HeartRateMultipleConnectedDevicesAdapter(
				this);
		super.setAdapters();
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

		BleBaseDeviceManager heartRateManager = new HeartRateManager(
				this, mActivity);
		boolean isDeviceAddedToTheList = mListConnectedDevicesHandler
				.addDevice(heartRateManager);

		if (isDeviceAddedToTheList == true)
		{
			heartRateManager.connect(device, false);
			invalidateUiListConnectedDevicesHandler();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		super.onCreateOptionsMenu(menu);
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.heart_rate_multiple, menu);
		getActionBar().setIcon(R.drawable.icon_heart_rate_multiple);
		getActionBar().setDisplayHomeAsUpEnabled(true);

		return true;
	}

	@Override
	protected void toggleHint()
	{
		TextView hintTv = (TextView) findViewById(R.id.hintTv);

		if (mListConnectedDevicesHandler == null
				|| mListConnectedDevicesHandler.getCount() <= 0)
		{
			hintTv.setText(R.string.about_multiple_bloodpressure);
			hintTv.setVisibility(View.VISIBLE);
		}
		else
		{
			hintTv.setVisibility(View.GONE);
		}
	}

	@Override
	public void onUiBodySensorLocation(final String valueBodySensorLocation)
	{
		invalidateUiListConnectedDevicesHandler();
	}

	@Override
	public void onUiHRM(final String valueHRM)
	{
		invalidateUiListConnectedDevicesHandler();
	}

}