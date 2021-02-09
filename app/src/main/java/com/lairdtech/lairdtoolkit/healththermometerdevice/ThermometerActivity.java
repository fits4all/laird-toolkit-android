/*****************************************************************************
 * Copyright (c) 2014 Laird Technologies. All Rights Reserved.
 * 
 * The information contained herein is property of Laird Technologies.
 * Licensees are granted free, non-transferable use of the information. NO WARRANTY of ANY KIND is provided. 
 * This heading must NOT be removed from the file.
 ******************************************************************************/

package com.lairdtech.lairdtoolkit.healththermometerdevice;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.lairdtech.lairdtoolkit.BleBaseActivity;
import com.lairdtech.lairdtoolkit.R;

public class ThermometerActivity extends BleBaseActivity implements
		ThermometerActivityUICallback
{
	private ThermometerManager mThermometerManager;
	private ThermometerGraph mGraph;
	private TextView mValueTempMeasurement;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState, R.layout.activity_thermometer);

		mThermometerManager = new ThermometerManager(this, mActivity);
		setBleBaseDeviceManager(mThermometerManager);

		initialiseDialogAbout(getResources().getString(
				R.string.about_thermometer));
		initialiseDialogFoundDevices("Thermometer");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.thermometer, menu);
		getActionBar().setIcon(R.drawable.icon_temp);
		getActionBar().setDisplayHomeAsUpEnabled(true);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
		case R.id.action_multiple_thermometer:
			mThermometerManager.disconnect();

			Intent intent = new Intent(this,
					ThermometerMultipleConnectedDevicesActivity.class);
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
		
		// Bind all generic views in the super. Bind specific views here.
		mValueTempMeasurement = (TextView) findViewById(R.id.valueTempMeasurement);
		/*
		 * getting the view of the whole activity, this is then passed to the
		 * base graph class to find the view for displaying the graph
		 */
		View wholeScreenView = ((ViewGroup) this
				.findViewById(android.R.id.content)).getChildAt(0);
		mGraph = new ThermometerGraph(mActivity, wholeScreenView);
	}

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
			}
		});
		invalidateUI();
	}
	
	@Override
	public void onUiDisconnected(int status)
	{
		super.onUiDisconnected(status);

		// set views to default
		runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				setCommonViewsToNonApplicable();
				
				mValueTempMeasurement.setText(getResources().getString(
						R.string.dash));

				if (mGraph != null)
				{
					mGraph.clearGraph();
				}
			}
		});
		mGraph.setStartTime(0);
	}

	@Override
	public void onUiTemperatureChange(final String result)
	{
		mGraph.startTimer();
		runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				mValueTempMeasurement.setText(result + " °C");
				mGraph.addNewData(Double.parseDouble(result));
			}
		});
	}
}
