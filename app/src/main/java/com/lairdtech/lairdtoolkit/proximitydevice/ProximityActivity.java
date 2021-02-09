/*****************************************************************************
 * Copyright (c) 2014 Laird Technologies. All Rights Reserved.
 * 
 * The information contained herein is property of Laird Technologies.
 * Licensees are granted free, non-transferable use of the information. NO WARRANTY of ANY KIND is provided. 
 * This heading must NOT be removed from the file.
 ******************************************************************************/

package com.lairdtech.lairdtoolkit.proximitydevice;

import java.math.BigInteger;

import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

import com.lairdtech.lairdtoolkit.BleBaseActivity;
import com.lairdtech.lairdtoolkit.R;

public class ProximityActivity extends BleBaseActivity implements
		ProximityActivityUiCallback, OnClickListener, OnCheckedChangeListener
{
	private ProximityManager mProximityManager;

	private TextView mValueTxPower;
	private Button btnImmediateAlert;
	private RadioGroup radioGroupLinkLoss, radioGroupImmediateAlert;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState, R.layout.activity_proximity);

		mProximityManager = new ProximityManager(this, this);
		setBleBaseDeviceManager(mProximityManager);

		initialiseDialogAbout(getResources()
				.getString(R.string.about_proximity));
		initialiseDialogFoundDevices("Proximity");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.proximity, menu);
		getActionBar().setIcon(R.drawable.icon_proximity);
		getActionBar().setDisplayHomeAsUpEnabled(true);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public void bindViews()
	{
		super.bindViews();
		bindCommonViews();
		mValueTxPower = (TextView) findViewById(R.id.valueTxPower);
		radioGroupLinkLoss = (RadioGroup) findViewById(R.id.radioGroupLinkLoss);
		radioGroupImmediateAlert = (RadioGroup) findViewById(R.id.radioGroupImmediateAlert);
		btnImmediateAlert = (Button) findViewById(R.id.btnImmediateAlert);
	};

	@Override
	public void setListeners()
	{
		super.setListeners();
		btnImmediateAlert.setOnClickListener(this);
		radioGroupLinkLoss.setOnCheckedChangeListener(this);
		radioGroupImmediateAlert.setOnCheckedChangeListener(this);
	}

	public void onClick(View view)
	{
		int btnId = view.getId();
		switch (btnId)
		{
		case R.id.btnImmediateAlert:
			int checkedRadioBtn = radioGroupImmediateAlert
					.getCheckedRadioButtonId();

			if (checkedRadioBtn == R.id.radioImmediateAlertLow)
			{
				// low value chosen for Immediate Alert
				mProximityManager.writeAlertCharValue("0x00", 1);
			}
			else if (checkedRadioBtn == R.id.radioImmediateAlertMedium)
			{
				// medium value chosen for Immediate Alert
				mProximityManager.writeAlertCharValue("0x01", 1);
			}
			else if (checkedRadioBtn == R.id.radioImmediateAlertHigh)
			{
				// high value chosen for Immediate Alert
				mProximityManager.writeAlertCharValue("0x02", 1);
			}
			else
			{
				// no radio button is chosen yet
			}
			break;
		}
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId)
	{
		int radioGroupId = group.getId();
		RadioButton radioButton;
		int radioButtonId;
		// get selected radio button
		radioButton = (RadioButton) group.findViewById(checkedId);
		radioButtonId = radioButton.getId();

		switch (radioGroupId)
		{
		case R.id.radioGroupLinkLoss:
			if (radioButtonId == R.id.radioLinkLossAlertLow)
			{
				// low value chosen for Link loss
				mProximityManager.writeAlertCharValue("0x00", 0);
			}
			else if (radioButtonId == R.id.radioLinkLossAlertMedium)
			{
				// medium value chosen for Link loss
				mProximityManager.writeAlertCharValue("0x01", 0);
			}
			else if (radioButtonId == R.id.radioLinkLossAlertHigh)
			{
				// high value chosen for Link loss
				mProximityManager.writeAlertCharValue("0x02", 0);
			}
			else
			{
				// no radio button is checked from this radio group
			}
			break;
		}
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

				mValueTxPower.setText(getResources().getString(
						R.string.non_applicable));
			}
		});
	}

	@Override
	public void onUiReadTxPower(final byte[] mTxValue)
	{
		final BigInteger result = new BigInteger(mTxValue);

		runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				mValueTxPower.setText(result + " dB");
			}
		});
	}
}
