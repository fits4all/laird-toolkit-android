/*****************************************************************************
 * Copyright (c) 2014 Laird Technologies. All Rights Reserved.
 * 
 * The information contained herein is property of Laird Technologies.
 * Licensees are granted free, non-transferable use of the information. NO WARRANTY of ANY KIND is provided. 
 * This heading must NOT be removed from the file.
 ******************************************************************************/

package com.lairdtech.lairdtoolkit.bloodpressuredevice;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.lairdtech.lairdtoolkit.BleBaseActivity;
import com.lairdtech.lairdtoolkit.R;

public class BloodPressureActivity extends BleBaseActivity implements
		BloodPressureActivityUiCallback
{
	private BloodPressureManager mBloodPressureManager;
	private BloodPressureGraph mGraph;

	private TextView mSystolicResult, mDiastolicResult,
			mArterialPressureResult;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState, R.layout.activity_blood_pressure);

		mBloodPressureManager = new BloodPressureManager(this, mActivity);

		setBleBaseDeviceManager(mBloodPressureManager);
		initialiseDialogAbout(getResources().getString(
				R.string.about_blood_pressure));
		initialiseDialogFoundDevices("Blood Pressure");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.blood_pressure, menu);
		getActionBar().setIcon(R.drawable.icon_blood_pressure);
		getActionBar().setDisplayHomeAsUpEnabled(true);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{

		case R.id.action_multiple_bloodpressure:
			mBloodPressureManager.disconnect();

			Intent intent = new Intent(this,
					BloodPressureMultipleConnectedDevicesActivity.class);
			startActivity(intent);
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void bindViews()
	{
		super.bindViews();

		bindCommonViews();

		mSystolicResult = (TextView) findViewById(R.id.valueSystolic);
		mDiastolicResult = (TextView) findViewById(R.id.valueDiastolic);
		mArterialPressureResult = (TextView) findViewById(R.id.valueArterialPressure);

		View wholeScreenView = ((ViewGroup) this
				.findViewById(android.R.id.content)).getChildAt(0);
		mGraph = new BloodPressureGraph(mActivity, wholeScreenView);
	};

	@Override
	public void onUiConnected()
	{
		super.onUiConnected();

		runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				setCommonViews();

				mValueName.setText(mBloodPressureManager.getBluetoothDevice()
						.getName());
				mValueDeviceAddress.setText(mBloodPressureManager
						.getBluetoothDevice().getAddress());
			}
		});
		invalidateUI();
	}

	@Override
	public void onUiDisconnected(int status)
	{
		super.onUiDisconnected(status);

		runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				setCommonViewsToNonApplicable();

				mSystolicResult.setText(getResources().getString(
						R.string.non_applicable));
				mDiastolicResult.setText(getResources().getString(
						R.string.non_applicable));
				mArterialPressureResult.setText(getResources().getString(
						R.string.non_applicable));

				if (mGraph != null)
				{
					mGraph.clearGraph();
				}

			}
		});
		mGraph.setStartTime(0);
	}

	@Override
	public void onUIBloodPressureRead(final float valueSystolic,
			final float valueDiastolic, final float valueArterialPressure)
	{
		mGraph.startTimer();

		runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				mSystolicResult.setText(valueSystolic + " "
						+ mBloodPressureManager.getUnitType());
				mDiastolicResult.setText(valueDiastolic + " "
						+ mBloodPressureManager.getUnitType());
				mArterialPressureResult.setText(valueArterialPressure + " "
						+ mBloodPressureManager.getUnitType());

				mGraph.addNewData(valueSystolic, valueDiastolic,
						valueArterialPressure);
			}
		});
	}
}
