package com.lairdtech.lairdtoolkit.otadevice;

import android.app.Activity;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.lairdtech.bt.ble.vsp.FileAndFifoAndVspManager.FileState;
import com.lairdtech.bt.ble.vsp.VirtualSerialPortDevice;
import com.lairdtech.bt.ble.vsp.VirtualSerialPortDevice.FifoAndVspManagerState;
import com.lairdtech.lairdtoolkit.BleBaseActivity;
import com.lairdtech.lairdtoolkit.R;

public class OTAActivity extends BleBaseActivity implements
		OTAManagerUiCallback
{
	private static final String TAG = "OTAActivity";

	private static final int FILE_SELECT_REQUEST_CODE = 2;

	private static final String COLOR_GREEN = "#009933";
	private static final String COLOR_RED = "#993333";

	private Button mBtnFileSelect;
	private Button mBtnFileDownload;
	private Button mBtnFileStopDownloading;

	private TextView mValueFileNameTv;
	private TextView mValueSavedAsTv;
	private TextView mValueValidDeviceTv;
	private TextView mValueStatusTv;
	private TextView mValueErrorsTv;

	private ProgressBar mProgressBar;
	private TextView mTvProgressBarProgress;

	private OTAManager mOTAManager;

	private boolean isPrefResetModuleAfterSending = false;

	@Override
	protected void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState, R.layout.activity_ota);

		mOTAManager = new OTAManager(this, this);
		setBleBaseDeviceManager(mOTAManager);

		initialiseDialogAbout(getResources().getString(R.string.about_ota));
		initialiseDialogFoundDevices("VSP");

		/*
		 * check if the user chose a .uwc file from a file browser app
		 */
		Intent i = getIntent();
		if (i != null)
		{
			Uri result = i.getData();
			if (result != null)
			{
				mOTAManager.setFile(result);
				mProgressBar.setMax((int) mOTAManager.getFileWrapper()
						.getFileTotalSize());
				mProgressBar.setProgress(0);
				mTvProgressBarProgress.setText(0 + "/" + mProgressBar.getMax());
			}
		}
	}

	@Override
	public void bindViews()
	{
		super.bindViews();

		mBtnFileSelect = findViewById(R.id.btnFileSelect);
		mBtnFileDownload = findViewById(R.id.btnFileDownload);
		mBtnFileStopDownloading = findViewById(R.id.btnFileStopDownloading);

		mValueFileNameTv = findViewById(R.id.valueFileNameTv);
		mValueSavedAsTv = findViewById(R.id.valueSavedAsTv);
		mValueValidDeviceTv = findViewById(R.id.valueValidDeviceTv);
		mValueStatusTv = findViewById(R.id.valueStatusTv);
		mValueErrorsTv = findViewById(R.id.valueErrorsTv);

		mProgressBar = findViewById(R.id.progressBar);
		mTvProgressBarProgress = findViewById(R.id.progressBarProgressTv);
	}

	@Override
	public void setListeners()
	{
		super.setListeners();

		mBtnFileSelect.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				/*
				 * open file browser to select a file
				 */
				Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
				intent.addCategory(Intent.CATEGORY_OPENABLE);

				intent.setType("*/*");
				startActivityForResult(intent, FILE_SELECT_REQUEST_CODE);
			}
		});

		mBtnFileDownload.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				if (mOTAManager == null)
					return;
				/*
				 * start uploading procedure
				 */
				if (mOTAManager.getConnectionState() == BluetoothProfile.STATE_CONNECTED)
				{
					mBtnFileDownload.setEnabled(false);
					mOTAManager.startDataTransfer();

					mBtnFileSelect.setEnabled(false);
					mBtnFileDownload.setEnabled(false);
					mBtnFileStopDownloading.setEnabled(true);

					mValueStatusTv.setText(R.string.value_status_uploading);
					mValueStatusTv.setTextColor(Color.BLUE);
					mValueErrorsTv.setText("");
				}
				else
				{
					Log.i(TAG, "Must be connected with a BLE device");
					Toast.makeText(mActivity,
							"Must be connected with a BLE device",
							Toast.LENGTH_SHORT).show();
				}
			}
		});

		mBtnFileStopDownloading.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				if (mOTAManager == null)
					return;
				/*
				 * Stop uploading procedure
				 */
				mOTAManager.stopFileUploading();

				mValueStatusTv.setText(R.string.value_status_stopped);
				mValueStatusTv.setTextColor(Color.parseColor(COLOR_RED));

				mBtnFileSelect.setEnabled(true);
				mBtnFileDownload.setEnabled(true);
				mBtnFileStopDownloading.setEnabled(false);
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{

		if (requestCode == FILE_SELECT_REQUEST_CODE)
		{
			if (resultCode == Activity.RESULT_OK)
			{
				/*
				 * a file was chosen
				 */
				if (data != null)
				{
					Uri result;
					result = data.getData();
					mOTAManager.setFile(result);
					// display file uploading progress
					mProgressBar.setMax((int) mOTAManager.getFileWrapper()
							.getFileTotalSize());
					mProgressBar.setProgress(0);
					mTvProgressBarProgress.setText(0 + "/" + mProgressBar.getMax());

					mValueFileNameTv.setText(mOTAManager.getFileWrapper()
							.getFileName());
					mValueSavedAsTv.setText(mOTAManager.getFileWrapper()
							.getModuleFileName());
					mValueStatusTv.setText(R.string.value_status_waiting);
					mValueStatusTv.setTextColor(Color.GRAY);

					if (mOTAManager.getFifoAndVspManagerState() == FifoAndVspManagerState.READY_TO_SEND_DATA)
					{
						mBtnFileSelect.setEnabled(true);
						mBtnFileDownload.setEnabled(true);
						mBtnFileStopDownloading.setEnabled(false);
					}
					else
					{
						mBtnFileSelect.setEnabled(true);
						mBtnFileDownload.setEnabled(false);
						mBtnFileStopDownloading.setEnabled(false);
					}
				}
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.ota_menu, menu);
		getActionBar().setIcon(R.drawable.icon_ota);
		getActionBar().setDisplayHomeAsUpEnabled(true);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public void onUiDisconnected(int status)
	{
		runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				if (mOTAManager.getFileState() == FileState.FILE_CHOSEN)
				{
					mProgressBar.setProgress(0);
					mTvProgressBarProgress.setText(0 + "/" + mProgressBar.getMax());
				}
				mValueStatusTv.setText("");
				mValueValidDeviceTv.setText("");
				mValueErrorsTv.setText("");

				mBtnFileSelect.setEnabled(true);
				mBtnFileDownload.setEnabled(false);
				mBtnFileStopDownloading.setEnabled(false);
			}
		});
		uiInvalidateBtnState();
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
					mValueValidDeviceTv.setText("Yes");
					mValueValidDeviceTv.setTextColor(Color
							.parseColor(COLOR_GREEN));

					if (mOTAManager.getFileState() == FileState.FILE_CHOSEN)
					{
						mBtnFileSelect.setEnabled(true);
						mBtnFileDownload.setEnabled(true);
						mBtnFileStopDownloading.setEnabled(false);
					}
					else
					{
						mBtnFileSelect.setEnabled(true);
						mBtnFileDownload.setEnabled(false);
						mBtnFileStopDownloading.setEnabled(false);
					}
				}
				else
				{
					mValueValidDeviceTv.setText("No");
					mValueValidDeviceTv.setTextColor(Color
							.parseColor(COLOR_RED));

					mBtnFileSelect.setEnabled(true);
					mBtnFileDownload.setEnabled(false);
					mBtnFileStopDownloading.setEnabled(false);
				}
			}
		});
		uiInvalidateBtnState();
	}

	@Override
	public void onUiSendDataSuccess(final String dataSend)
	{
		runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				/*
				 * update the progress bar and stopping it from filling beyond
				 * our limit
				 */
				if (mOTAManager.getFileWrapper().getFileCurrentSizeRead() > mOTAManager
						.getFileWrapper().getFileTotalSize())
				{
					mProgressBar.setProgress((int) mOTAManager.getFileWrapper()
							.getFileTotalSize());
					mTvProgressBarProgress.setText(mOTAManager.getFileWrapper()
							.getFileTotalSize() + "/" + mProgressBar.getMax());
				}
				else
				{
					mProgressBar.setProgress((int) mOTAManager.getFileWrapper()
							.getFileCurrentSizeRead());
					mTvProgressBarProgress.setText(mOTAManager.getFileWrapper()
							.getFileCurrentSizeRead()
							+ "/"
							+ mProgressBar.getMax());
				}
			}
		});
	}

	@Override
	public void onUiReceiveSuccessData(final String dataReceived)
	{}

	@Override
	public void onUiReceiveErrorData(final String errorCode)
	{
		runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				mValueStatusTv.setText(R.string.value_status_failed);

				if (VirtualSerialPortDevice.ERROR_CODE_FILE_NOT_OPEN
						.equals(errorCode))
				{
					mValueErrorsTv.append(errorCode + "(File not open)");
				}
				else if (VirtualSerialPortDevice.ERROR_CODE_FSA_FAIL_OPENFILE
						.equals(errorCode))
				{
					mValueErrorsTv.append(errorCode + "(Failed to open file)");
				}
				else if (VirtualSerialPortDevice.ERROR_CODE_INCORRECT_MODE
						.equals(errorCode))
				{
					mValueErrorsTv.append(errorCode + "(Incorrect mode)");
				}
				else if (VirtualSerialPortDevice.ERROR_CODE_MEMORY_FULL
						.equals(errorCode))
				{
					mValueErrorsTv.append(errorCode + "(Memory is full)");
				}
				else if (VirtualSerialPortDevice.ERROR_CODE_NO_FILE_TO_CLOSE
						.equals(errorCode))
				{
					mValueErrorsTv.append(errorCode + "(No file to close)");
				}
				else if (VirtualSerialPortDevice.ERROR_CODE_UNEXPECTED_PARM
						.equals(errorCode))
				{
					mValueErrorsTv.append(errorCode + "(Unexpected parameter)");
				}
				else if (VirtualSerialPortDevice.ERROR_CODE_UNKNOWN_COMMAND
						.equals(errorCode))
				{
					mValueErrorsTv.append(errorCode + "(Unknown command)");
				}
				else if (VirtualSerialPortDevice.ERROR_CODE_FSA_FILENAME_TOO_LONG
						.equals(errorCode))
				{
					mValueErrorsTv.append(errorCode + "(Filename too long)");
				}
				else
				{
					mValueErrorsTv.append(errorCode);
				}

				mValueStatusTv.setTextColor(Color.parseColor(COLOR_RED));
				mValueErrorsTv.setTextColor(Color.parseColor(COLOR_RED));

				mBtnFileSelect.setEnabled(true);
				mBtnFileDownload.setEnabled(true);
				mBtnFileStopDownloading.setEnabled(false);
			}
		});
	}

	@Override
	public void onUiUploaded()
	{
		runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				mValueStatusTv.setText(R.string.value_status_success);
				mValueStatusTv.setTextColor(Color.parseColor(COLOR_GREEN));
				mBtnFileSelect.setEnabled(false);

				mBtnFileSelect.setEnabled(true);
				mBtnFileDownload.setEnabled(true);
				mBtnFileStopDownloading.setEnabled(false);
			}
		});
		mOTAManager.resetModule(isPrefResetModuleAfterSending);
	}

	@Override
	protected void loadPref()
	{
		super.loadPref();
		isPrefResetModuleAfterSending = mSharedPreferences.getBoolean(
				"pref_reset_module_after_sending", false);
	}

	@Override
	public void onUiReceiveData(String dataReceived)
	{}
}