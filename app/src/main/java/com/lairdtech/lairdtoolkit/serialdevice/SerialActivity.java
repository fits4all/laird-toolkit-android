

package com.lairdtech.lairdtoolkit.serialdevice;

import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import com.lairdtech.lairdtoolkit.BleBaseActivity;
import com.lairdtech.lairdtoolkit.R;

public class SerialActivity extends BleBaseActivity implements
		SerialManagerUiCallback
{
	private Button mBtnSend;
	private ScrollView mScrollViewConsoleOutput;
	private EditText mInputBox;
	private TextView mValueConsoleOutputTv;
	private TextView mValueRxCounterTv;
	private TextView mValueTxCounterTv;

	private SerialManager mSerialManager;

	private boolean isPrefClearTextAfterSending = false;
	private boolean isPrefSendCR = true;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState, R.layout.activity_serial);

		mSerialManager = new SerialManager(this, this);
		setBleBaseDeviceManager(mSerialManager);

		initialiseDialogAbout(getResources().getString(R.string.about_serial));
		initialiseDialogFoundDevices("VSP");
	}

	@Override
	public void bindViews()
	{
		super.bindViews();

		mBtnSend = findViewById(R.id.btnSend);
		mScrollViewConsoleOutput = findViewById(R.id.scrollViewConsoleOutput);
		mInputBox = findViewById(R.id.inputBox);
		mValueConsoleOutputTv = findViewById(R.id.valueConsoleOutputTv);
		mValueRxCounterTv = findViewById(R.id.valueRxCounterTv);
		mValueTxCounterTv = findViewById(R.id.valueTxCounterTv);
	}

	@Override
	public void setListeners()
	{
		super.setListeners();

		mBtnSend.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				// to send data to module
				String data = mInputBox.getText().toString();
				if (data != null)
				{
					mBtnSend.setEnabled(false);
					if (mValueConsoleOutputTv.getText().length() <= 0)
					{
						mValueConsoleOutputTv.append(">");
					}
					else
					{
						mValueConsoleOutputTv.append("\n\n>");
					}

					if (isPrefSendCR)
					{
						mSerialManager.startDataTransfer(data + "\r");
					}
					else {
						mSerialManager.startDataTransfer(data);
					}

					InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

					inputManager.hideSoftInputFromWindow(getCurrentFocus()
							.getWindowToken(),
							InputMethodManager.HIDE_NOT_ALWAYS);

					if (isPrefClearTextAfterSending)
					{
						mInputBox.setText("");
					}
					else
					{
						// do not clear the text from the editText
					}
				}
			}
		});
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
		if (item.getItemId() == R.id.action_clear) {
			mValueConsoleOutputTv.setText("");
			mSerialManager.clearRxAndTxCounter();

			mValueRxCounterTv.setText("0");
			mValueTxCounterTv.setText("0");
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onUiVspServiceFound(final boolean found)
	{
		runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				if (found)
				{
					mBtnSend.setEnabled(true);
				}
				else
				{
					mBtnSend.setEnabled(false);
				}
			}
		});
	}

	@Override
	public void onUiSendDataSuccess(final String dataSend)
	{
		runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				mValueConsoleOutputTv.append(dataSend);
				mValueTxCounterTv.setText("" + mSerialManager.getTxCounter());
				mScrollViewConsoleOutput.smoothScrollTo(0,
						mValueConsoleOutputTv.getBottom());
			}
		});
	}

	@Override
	public void onUiReceiveData(final String dataReceived)
	{
		runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				mValueConsoleOutputTv.append(dataReceived);
				mValueRxCounterTv.setText("" + mSerialManager.getRxCounter());
				mScrollViewConsoleOutput.smoothScrollTo(0,
						mValueConsoleOutputTv.getBottom());
			}
		});
	}

	@Override
	public void onUiUploaded()
	{
		mBtnSend.setEnabled(true);
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