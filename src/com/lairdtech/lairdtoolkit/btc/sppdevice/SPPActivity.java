package com.lairdtech.lairdtoolkit.btc.sppdevice;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.lairdtech.lairdtoolkit.BaseActivity;
import com.lairdtech.lairdtoolkit.R;

public class SPPActivity extends BaseActivity implements SPPManagerUiCallback
{
	private static final String TAG = "SPPActivity";

	private SPPManager mSppManager;

	private boolean isPrefClearTextAfterSending = false;
	private boolean isPrefSendCR = true;

	private int mCounterRx = 0;
	private int mCounterTx = 0;

	private Button mBtnSend, mBtnScan;

	private ScrollView mScrollViewConsoleOutput;
	private EditText mInputBox;
	private TextView mValueConsoleOutputTv, mValueRxCounterTv,
			mValueTxCounterTv;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState, R.layout.activity_spp);

		initialiseDialogAbout(getResources().getString(R.string.about_spp));
		initialiseDialogFoundDevices("BT Classic");
	}

	@Override
	public void bindViews()
	{
		super.bindViews();
		mBtnSend = (Button) findViewById(R.id.btnSend);
		mBtnScan = (Button) findViewById(R.id.btnScan);

		mScrollViewConsoleOutput = (ScrollView) findViewById(R.id.scrollViewConsoleOutput);

		mInputBox = (EditText) findViewById(R.id.inputBox);

		mValueConsoleOutputTv = (TextView) findViewById(R.id.valueConsoleOutputTv);
		mValueRxCounterTv = (TextView) findViewById(R.id.valueRxCounterTv);
		mValueTxCounterTv = (TextView) findViewById(R.id.valueTxCounterTv);
	}

	@Override
	public void setListeners()
	{
		/*
		 * override the default BLE scanning and add the BT classic scan
		 */
		mBtnScan.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				switch (v.getId())
				{
				case R.id.btnScan:
				{
					if (mBluetoothAdapterWrapper.isEnabled() == false)
					{
						Log.i(TAG, "Bluetooth must be on to start scanning.");
						Toast.makeText(mActivity,
								"Bluetooth must be on to start scanning.",
								Toast.LENGTH_SHORT);
						return;
					}
					else if (mSppManager == null)
					{
						/*
						 * not connected
						 */
						mBluetoothAdapterWrapper.startDiscovery();
						mDialogFoundDevices.show();
					}
					else if (mSppManager.isConnected() == false
							&& mSppManager.isConnecting() == false)
					{
						/*
						 * not connected
						 */
						mBluetoothAdapterWrapper.startDiscovery();
						mDialogFoundDevices.show();
					}
					else if (mSppManager.isConnected() == false
							&& mSppManager.isConnecting() == true)
					{
						/*
						 * connecting
						 */
					}
					else if (mSppManager.isConnected() == true
							&& mSppManager.isConnecting() == false)
					{
						/*
						 * connected
						 */
						mSppManager.disconnect();
					}

					uiInvalidateBtnState();
					break;
				}
				}
			}
		});

		mBtnSend.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				String data = null;

				if (isPrefSendCR == true)
				{
					data = mInputBox.getText().toString() + "\r";
				}
				else if (isPrefSendCR == false)
				{
					data = mInputBox.getText().toString();
				}

				if (data != null)
				{
					if (mValueConsoleOutputTv.getText().length() <= 0)
					{
						mValueConsoleOutputTv.append(data + "\n");
					}
					else
					{
						mValueConsoleOutputTv.append(data + "\n");
					}

					mCounterTx += data.length();

					mSppManager.writeDataToRemoteDevice((data).getBytes());

					InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

					inputManager.hideSoftInputFromWindow(getCurrentFocus()
							.getWindowToken(),
							InputMethodManager.HIDE_NOT_ALWAYS);

					if (isPrefClearTextAfterSending == true)
					{
						mInputBox.setText("");
					}

					runOnUiThread(new Runnable()
					{
						public void run()
						{
							mValueTxCounterTv.setText("" + mCounterTx);
							mScrollViewConsoleOutput.smoothScrollTo(0,
									mValueConsoleOutputTv.getBottom());
						};
					});
				}
			}
		});
	}

	@Override
	protected void onPause()
	{
		super.onPause();

		if (isInNewScreen == true || isPrefRunInBackground == true)
		{
			// let the app run normally in the background
		}
		else
		{
			// stop scanning or disconnect if we are connected
			if (mBluetoothAdapterWrapper.isDiscovering())
			{
				mBluetoothAdapterWrapper.stopDiscovery();
			}
			else if (mSppManager.isConnected())
			{
				mSppManager.disconnect();
			}
		}
	}

	@Override
	protected void onDialogFoundDevicesItemClick(AdapterView<?> arg0,
			View view, int position, long id)
	{
		final BluetoothDevice device = mListFoundDevicesHandler
				.getDevice(position);
		if (device == null)
			return;

		mBluetoothAdapterWrapper.stopDiscovery();
		mDialogFoundDevices.dismiss();
		mSppManager = new SPPManager(mActivity, this, device);
		mSppManager.connect();
		uiInvalidateBtnState();
	}

	@Override
	protected void onDialogFoundDevicesCancel(DialogInterface arg0)
	{
		mBluetoothAdapterWrapper.stopDiscovery();
		uiInvalidateBtnState();
	}

	@Override
	public void onBackPressed()
	{
		if (mSppManager != null && mSppManager.isConnected() == true)
		{
			mSppManager.disconnect();
		}
		uiInvalidateBtnState();
		finish();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.serial, menu);
		getActionBar().setIcon(R.drawable.icon_serial);
		getActionBar().setDisplayHomeAsUpEnabled(true);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{

		case android.R.id.home:
			if (mSppManager != null && mSppManager.isConnected() == true)
			{
				mSppManager.disconnect();
				invalidateOptionsMenu();
			}
			else
			{
				finish();
			}
			break;
		case R.id.action_clear:
			mValueConsoleOutputTv.setText("");

			mCounterRx = 0;
			mCounterTx = 0;

			mValueRxCounterTv.setText("" + mCounterRx);
			mValueTxCounterTv.setText("" + mCounterTx);

			break;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public void uiInvalidateBtnState()
	{
		this.runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{

				if (mSppManager == null)
				{
					mBtnScan.setText(R.string.btn_scan);
					mBtnSend.setEnabled(false);
				}
				else if (mSppManager.isConnected() == false
						&& mSppManager.isConnecting() == false)
				{
					mBtnScan.setText(R.string.btn_scan);
					mBtnSend.setEnabled(false);
				}
				else if (mSppManager.isConnected() == false
						&& mSppManager.isConnecting() == true)
				{
					mBtnScan.setText(R.string.btn_connecting);
					mBtnSend.setEnabled(false);
				}
				else if (mSppManager.isConnected() == true
						&& mSppManager.isConnecting() == false)
				{
					mBtnScan.setText(R.string.btn_disconnect);
					mBtnSend.setEnabled(true);
				}

				invalidateOptionsMenu();
			}
		});
	}

	@Override
	public void onUiBtcRemoteDeviceConnected()
	{
		uiInvalidateBtnState();
	}

	@Override
	public void onUiBtcRemoteDeviceDisconnected()
	{
		uiInvalidateBtnState();
	}

	@Override
	public void onUiBtcRemoteDeviceFailed()
	{
		uiInvalidateBtnState();
	}

	@Override
	public void onUiRemoteDeviceRead(final String result)
	{
		mCounterRx += result.length();

		runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				mValueConsoleOutputTv.append(result);
				mValueRxCounterTv.setText("" + mCounterRx);
				mScrollViewConsoleOutput.smoothScrollTo(0,
						mValueConsoleOutputTv.getBottom());
			}
		});
	}

	@Override
	protected void loadPref()
	{
		super.loadPref();
		isPrefClearTextAfterSending = mSharedPreferences.getBoolean(
				"pref_clear_text_after_sending", false);
		isPrefSendCR = mSharedPreferences.getBoolean(
				"pref_append_/r_at_end_of_data", true);
	}
}