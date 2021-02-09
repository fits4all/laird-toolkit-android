package com.lairdtech.lairdtoolkit.healththermometerdevice;

import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import com.lairdtech.bt.ble.BleBaseDeviceManager;
import com.lairdtech.lairdtoolkit.BleBaseMultipleConnectedDevicesActivity;
import com.lairdtech.lairdtoolkit.R;

public class ThermometerMultipleConnectedDevicesActivity extends
		BleBaseMultipleConnectedDevicesActivity implements
		ThermometerActivityUICallback
{

	private final static String TAG = "ThermometerMultipleConnectedDevicesActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState,
				R.layout.activity_multiple_connected_devices);

		initialiseDialogAbout(getResources().getString(
				R.string.about_multiple_thermometer));
		initialiseDialogFoundDevices("HTM");

		toggleHint();
	}

	@Override
	public void setAdapters()
	{
		mListConnectedDevicesHandler = new ThermometerMultipleConnectedDevicesAdapter(
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

		BleBaseDeviceManager thermometerManager = new ThermometerManager(this,
				mActivity);
		boolean isDeviceAddedToTheList = mListConnectedDevicesHandler
				.addDevice(thermometerManager);

		if (isDeviceAddedToTheList)
		{
			thermometerManager.connect(device, false);
			invalidateUiListConnectedDevicesHandler();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		super.onCreateOptionsMenu(menu);
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.thermometer_multiple, menu);
		getActionBar().setIcon(R.drawable.icon_temp_multiple);
		getActionBar().setDisplayHomeAsUpEnabled(true);

		return true;
	}

	@Override
	protected void toggleHint()
	{
		TextView hintTv = findViewById(R.id.hintTv);
		hintTv.setText(R.string.about_multiple_thermometer);

		if (mListConnectedDevicesHandler == null
				|| mListConnectedDevicesHandler.getCount() <= 0)
		{
			hintTv.setVisibility(View.VISIBLE);
		}
		else
		{
			hintTv.setVisibility(View.GONE);
		}
	}

	@Override
	public void onUiTemperatureChange(String result)
	{
		invalidateUiListConnectedDevicesHandler();
	}
}