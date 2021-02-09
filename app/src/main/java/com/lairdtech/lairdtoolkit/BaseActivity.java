/*****************************************************************************
 * Copyright (c) 2014 Laird Technologies. All Rights Reserved.
 * 
 * The information contained herein is property of Laird Technologies.
 * Licensees are granted free, non-transferable use of the information. NO WARRANTY of ANY KIND is provided. 
 * This heading must NOT be removed from the file.
 ******************************************************************************/

package com.lairdtech.lairdtoolkit;

import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.lairdtech.bt.BluetoothAdapterHelper;
import com.lairdtech.bt.BluetoothAdapterHelperCallback;

/**
 * Base activity for all the activities that use BT classic or BLE
 * functionalities
 * 
 * @author Kyriakos.Alexandrou
 */
public class BaseActivity extends Activity implements ICommonUi,
		BluetoothAdapterHelperCallback
{
	private static final String TAG = "BaseActivity";
	private static final int ENABLE_BT_REQUEST_ID = 1;

	protected Activity mActivity;
	protected BluetoothAdapterHelper mBluetoothAdapterWrapper;

	protected Dialog mDialogFoundDevices;
	protected ListFoundDevicesHandler mListFoundDevicesHandler = null;

	private Dialog mDialogAbout;
	private View mViewAbout;

	protected SharedPreferences mSharedPreferences;
	protected boolean isInNewScreen = false;
	protected boolean isPrefRunInBackground = true;
	protected boolean isPrefPeriodicalScan = true;

	protected Dialog getDialogAbout()
	{
		return mDialogAbout;
	}

	protected void onCreate(Bundle savedInstanceState, int layoutResID)
	{
		setContentView(layoutResID);
		super.onCreate(savedInstanceState);

		mActivity = this;

		bindViews();
		setAdapters();
		setListeners();

		mBluetoothAdapterWrapper = new BluetoothAdapterHelper(this, this);

		mSharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(this);
	}

	@Override
	public void bindViews()
	{
		LayoutInflater li = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mViewAbout = li.inflate(R.layout.activity_about, null, false);
	}

	@Override
	public void setAdapters()
	{
		// setting handler and adapter for the dialog list view
		mListFoundDevicesHandler = new ListFoundDevicesHandler(this);
	}

	@Override
	public void setListeners()
	{}

	/**
	 * Callback for what to do when an item is clicked on the found devices
	 * dialog
	 * 
	 * @param arg0
	 *            The AdapterView where the click happened.
	 * @param view
	 *            The view within the AdapterView that was clicked (this will be
	 *            a view provided by the adapter)
	 * @param position
	 *            The position of the view in the adapter.
	 * @param id
	 *            The row id of the item that was clicked.
	 */
	protected void onDialogFoundDevicesItemClick(AdapterView<?> arg0,
			View view, int position, long id)
	{}

	/**
	 * Called when the dialog of found bluetooth devices is cancelled
	 * 
	 * @param dialogInterface
	 */
	protected void onDialogFoundDevicesCancel(DialogInterface dialogInterface)
	{}

	/**
	 * Initialise the dialog for the devices found from a scan.
	 * 
	 * @param title
	 *            the title to display for the dialog
	 */
	protected void initialiseDialogFoundDevices(String title)
	{
		/*
		 * create/set dialog ListView
		 */
		ListView mLvFoundDevices = new ListView(this);
		mLvFoundDevices.setAdapter(mListFoundDevicesHandler);
		mLvFoundDevices.setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> arg0, View view,
					int position, long id)
			{
				onDialogFoundDevicesItemClick(arg0, view, position, id);
			}
		});

		/*
		 * create and initialise Dialog
		 */
		mDialogFoundDevices = new Dialog(this);
		mDialogFoundDevices.setContentView(mLvFoundDevices);
		mDialogFoundDevices.setTitle("Select a " + title + " device");
		mDialogFoundDevices.setCanceledOnTouchOutside(false);

		mDialogFoundDevices.setOnCancelListener(new OnCancelListener()
		{
			@Override
			public void onCancel(DialogInterface arg0)
			{
				onDialogFoundDevicesCancel(arg0);
			}
		});

		mDialogFoundDevices.setOnDismissListener(new OnDismissListener()
		{
			@Override
			public void onDismiss(DialogInterface arg0)
			{
				mListFoundDevicesHandler.clearList();
			}
		});
	}

	/**
	 * Initialise the about popup dialog
	 * 
	 * @param text
	 *            the content message to display in the dialog
	 */
	protected void initialiseDialogAbout(String text)
	{
		mDialogAbout = new Dialog(this);
		Log.e(TAG, "mViewAbout: " + mViewAbout);
		mDialogAbout.setContentView(mViewAbout);
		mDialogAbout.setTitle("About");
		mDialogAbout.setCanceledOnTouchOutside(true);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
			mDialogAbout.findViewById(R.id.logo).setLayerType(
					View.LAYER_TYPE_SOFTWARE, null);

		/*
		 * set content to display in the about message
		 */
		TextView valueAbout = (TextView) mDialogAbout
				.findViewById(R.id.valueAbout);
		valueAbout.setMovementMethod(LinkMovementMethod.getInstance());
		valueAbout.setText(Html.fromHtml(text));
	}

	/**
	 * Add the found devices to a listView in the dialog.
	 * 
	 * @param device
	 *            the device to add
	 * @param rssi
	 *            the rssi value
	 * @param scanRecord
	 *            the advertised packet data that the device holds
	 */
	protected void handleFoundDevice(final BluetoothDevice device,
			final int rssi, final byte[] scanRecord)
	{
		// adds found devices to list view
		runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				mListFoundDevicesHandler.addDevice(device, rssi, scanRecord);
			}
		});
	}

	protected void handleFoundDevice(final BluetoothDevice device,
			final int rssi)
	{
		// adds found devices to list view
		runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				mListFoundDevicesHandler.addDevice(device, rssi);
			}
		});
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		loadPref();
		isInNewScreen = false;
		/*
		 * check that BT is enabled as the user could have turned it off during
		 * the onPause.
		 */
		if (!mBluetoothAdapterWrapper.isEnabled())
		{
			Intent enableBTIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBTIntent, ENABLE_BT_REQUEST_ID);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.home, menu);

		if (mBluetoothAdapterWrapper.isBleScanning()
				|| mBluetoothAdapterWrapper.isDiscovering())
		{
			menu.findItem(R.id.action_scanning_indicator).setActionView(
					R.layout.progress_indicator);
		}
		else
		{
			menu.findItem(R.id.action_scanning_indicator).setActionView(null);
		}

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		Intent intent;

		switch (item.getItemId())
		{
		case R.id.action_about:

			mDialogAbout.show();
			break;

		case R.id.action_settings:
			isInNewScreen = true;

			intent = new Intent(this, SettingsActivity.class);
			startActivity(intent);
			break;
		}

		return super.onOptionsItemSelected(item);
	}

	protected void invalidateUI()
	{
		invalidateOptionsMenu();
	}

	@Override
	public void uiInvalidateBtnState()
	{}

	@Override
	public void onBleStopScan()
	{
		// dismiss' dialog if no devices are found.
		if (mListFoundDevicesHandler.getCount() <= 0)
		{
			mDialogFoundDevices.dismiss();
		}
		uiInvalidateBtnState();
	}

	@Override
	public void onBleDeviceFound(BluetoothDevice device, int rssi,
			byte[] scanRecord)
	{
		handleFoundDevice(device, rssi, scanRecord);
	}

	@Override
	public void onDiscoveryStop()
	{
		// dismiss' dialog if no devices are found.
		if (mListFoundDevicesHandler.getCount() <= 0)
		{
			mDialogFoundDevices.dismiss();
		}
		uiInvalidateBtnState();
	}

	@Override
	public void onDiscoveryDeviceFound(BluetoothDevice device, int rssi)
	{
		if (device.getType() == BluetoothDevice.DEVICE_TYPE_CLASSIC)
		{
			handleFoundDevice(device, rssi);
		}
	}

	protected void loadPref()
	{
		isPrefRunInBackground = mSharedPreferences.getBoolean(
				"pref_run_in_background", true);
		isPrefPeriodicalScan = mSharedPreferences.getBoolean(
				"pref_periodical_scan", true);
	}
}
