package com.lairdtech.lairdtoolkit.bloodpressuredevice;

import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import com.lairdtech.bt.ble.BleBaseDeviceManager;
import com.lairdtech.lairdtoolkit.BleBaseMultipleConnectedDevicesActivity;
import com.lairdtech.lairdtoolkit.R;

public class BloodPressureMultipleConnectedDevicesActivity extends
		BleBaseMultipleConnectedDevicesActivity implements
		BloodPressureActivityUiCallback
{
	private final static String TAG = "BloodPressureMultipleConnectedDevicesActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState,
				R.layout.activity_multiple_connected_devices);

		initialiseDialogAbout(getResources().getString(
				R.string.about_multiple_bloodpressure));
		initialiseDialogFoundDevices("BPM");

		toggleHint();
	}

	@Override
	public void setAdapters()
	{
		mListConnectedDevicesHandler = new BloodPressureMultipleConnectedDevicesAdapter(
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

		BleBaseDeviceManager thermometerManager = new BloodPressureManager(
				this, mActivity);
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
		// Inflate the menu; this adds items to the action bar if it's present.
		getMenuInflater().inflate(R.menu.blood_pressure_multiple, menu);
		getActionBar().setIcon(R.drawable.icon_blood_pressure_multiple);
		getActionBar().setDisplayHomeAsUpEnabled(true);

		return true;
	}

	@Override
	protected void toggleHint()
	{
		TextView hintTv = findViewById(R.id.hintTv);

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
	public void onUIBloodPressureRead(float mValueBloodPressureSystolicResult,
			float mValueBloodPressureDiastolicResult,
			float mValueBloodPressureArterialPressureResult)
	{
		invalidateUiListConnectedDevicesHandler();
	}
}