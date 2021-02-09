/*****************************************************************************
 * Copyright (c) 2014 Laird Technologies. All Rights Reserved.
 * 
 * The information contained herein is property of Laird Technologies.
 * Licensees are granted free, non-transferable use of the information. NO WARRANTY of ANY KIND is provided. 
 * This heading must NOT be removed from the file.
 ******************************************************************************/

package com.lairdtech.lairdtoolkit;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import com.lairdtech.lairdtoolkit.batchdevice.BatchActivity;
import com.lairdtech.lairdtoolkit.bloodpressuredevice.BloodPressureActivity;
import com.lairdtech.lairdtoolkit.btc.sppdevice.SPPActivity;
import com.lairdtech.lairdtoolkit.healththermometerdevice.ThermometerActivity;
import com.lairdtech.lairdtoolkit.heartratedevice.HeartRateActivity;
import com.lairdtech.lairdtoolkit.otadevice.OTAActivity;
import com.lairdtech.lairdtoolkit.proximitydevice.ProximityActivity;
import com.lairdtech.lairdtoolkit.serialdevice.SerialActivity;

public class HomeActivity extends BaseActivity
{
	private GridView mIconsGrid;
	private List<String> mIconNames = new ArrayList<String>();
	private List<Integer> mIconImages = new ArrayList<Integer>();;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState, R.layout.activity_home);

		initialiseDialogAbout(getResources()
				.getString(R.string.disclaimer_text));
	}

	@Override
	public void bindViews()
	{
		super.bindViews();
		mIconsGrid = (GridView) findViewById(R.id.grid);
	}

	@Override
	public void setAdapters()
	{
		super.setAdapters();

		// set names
		mIconNames.add(getResources().getString(R.string.label_blood));
		mIconNames.add(getResources().getString(R.string.label_heart));
		mIconNames.add(getResources().getString(R.string.label_thermometer));
		mIconNames.add(getResources().getString(R.string.label_prox));
		mIconNames.add(getResources().getString(R.string.label_serial));
		mIconNames.add(getResources().getString(R.string.label_batch));
		mIconNames.add(getResources().getString(R.string.label_ota));
		mIconNames.add(getResources().getString(R.string.label_spp));

		// set images
		mIconImages.add(R.drawable.icon_blood_pressure);
		mIconImages.add(R.drawable.icon_heart_rate);
		mIconImages.add(R.drawable.icon_temp);
		mIconImages.add(R.drawable.icon_proximity);
		mIconImages.add(R.drawable.icon_serial);
		mIconImages.add(R.drawable.icon_batch);
		mIconImages.add(R.drawable.icon_ota);
		mIconImages.add(R.drawable.icon_serial);

		mIconsGrid.setAdapter(new HomeIconsAdapter(this, mIconNames,
				mIconImages));
	}

	@Override
	public void setListeners()
	{
		super.setListeners();

		mIconsGrid.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id)
			{

				Intent intent;
				switch (position)
				{

				case 0:
					isInNewScreen = true;

					intent = new Intent(mActivity, BloodPressureActivity.class);
					startActivity(intent);
					break;

				case 1:
					isInNewScreen = true;

					intent = new Intent(mActivity, HeartRateActivity.class);
					startActivity(intent);
					break;
				case 2:
					isInNewScreen = true;

					intent = new Intent(mActivity, ThermometerActivity.class);
					startActivity(intent);
					break;
				case 3:
					isInNewScreen = true;

					intent = new Intent(mActivity, ProximityActivity.class);
					startActivity(intent);
					break;

				case 4:
					isInNewScreen = true;

					intent = new Intent(mActivity, SerialActivity.class);
					startActivity(intent);
					break;

				case 5:
					isInNewScreen = true;

					intent = new Intent(mActivity, BatchActivity.class);
					startActivity(intent);
					break;
				case 6:
					isInNewScreen = true;

					intent = new Intent(mActivity, OTAActivity.class);
					startActivity(intent);
					break;
				case 7:
					isInNewScreen = true;

					intent = new Intent(mActivity, SPPActivity.class);
					startActivity(intent);
					break;

				}
			}
		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		super.onCreateOptionsMenu(menu);

		/*
		 * home screen has no need for the scanning indicator which is being
		 * setup from the super class
		 */
		menu.findItem(R.id.action_scanning_indicator).setVisible(false);
		return true;
	}
}
