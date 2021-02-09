package com.lairdtech.lairdtoolkit.heartratedevice;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.lairdtech.lairdtoolkit.BleBaseActivity;
import com.lairdtech.lairdtoolkit.R;

public class HeartRateActivity extends BleBaseActivity implements
		HeartRateActivityUiCallback
{
	private HeartRateManager mHeartRateManager;
	private HeartRateGraph mGraph;
	private TextView mValueHRM, mValueBodySensorPosition;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState, R.layout.activity_heart_rate);

		mHeartRateManager = new HeartRateManager(this, this);
		setBleBaseDeviceManager(mHeartRateManager);

		initialiseDialogAbout(getResources().getString(
				R.string.about_heart_rate));
		initialiseDialogFoundDevices("HRM");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.heart_rate, menu);
		getActionBar().setIcon(R.drawable.icon_heart_rate);
		getActionBar().setDisplayHomeAsUpEnabled(true);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{

		case R.id.action_multiple_heartrate:
			mHeartRateManager.disconnect();

			Intent intent = new Intent(this,
					HeartRateMultipleConnectedDevicesActivity.class);
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

		mValueHRM = findViewById(R.id.valueHRM);
		mValueBodySensorPosition = findViewById(R.id.valueBodySensor);
		/*
		 * getting the view of the whole activity, this is then passed to the
		 * base graph class to find the view for displaying the graph
		 */
		View wholeScreenView = ((ViewGroup) this
				.findViewById(android.R.id.content)).getChildAt(0);
		mGraph = new HeartRateGraph(this, wholeScreenView);
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

				mValueHRM.setText(getResources().getString(R.string.dash));
				mValueBodySensorPosition.setText(getResources().getString(
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
	public void onUiHRM(final String valueHRM)
	{
		mGraph.startTimer();

		runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				mValueHRM.setText(valueHRM + " bpm");
				mGraph.addNewData(Double.parseDouble(valueHRM));
			}
		});
	}

	@Override
	public void onUiBodySensorLocation(final String valueBodySensorLocation)
	{
		runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				mValueBodySensorPosition.setText(valueBodySensorLocation);
			}
		});
	}
}
