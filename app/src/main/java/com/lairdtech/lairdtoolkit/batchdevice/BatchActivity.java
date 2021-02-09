package com.lairdtech.lairdtoolkit.batchdevice;

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
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.lairdtech.bt.ble.vsp.FileAndFifoAndVspManager.FileState;
import com.lairdtech.bt.ble.vsp.VirtualSerialPortDevice;
import com.lairdtech.bt.ble.vsp.VirtualSerialPortDevice.FifoAndVspManagerState;
import com.lairdtech.lairdtoolkit.BleBaseActivity;
import com.lairdtech.lairdtoolkit.R;
import com.lairdtech.misc.FileWrapper;

public class BatchActivity extends BleBaseActivity implements
		BatchManagerUiCallback
{
	private static final String TAG = "BatchActivity";

	private static final int FILE_SELECT_REQUEST_CODE = 2;

	private static final String COLOR_GREEN = "#009933";
	private static final String COLOR_RED = "#993333";

	private Button mBtnFileSelect;
	private Button mBtnFileSend;
	private Button mBtnFileStopSending;

	private TextView mValueFileNameTv;
	private TextView mValueValidDeviceTv;
	private TextView mValueStatusTv;
	private TextView mValueErrorsTv;
	private ScrollView mScrollViewDataSend;
	private TextView mValueDataSendTv;
	private TextView mTvProgressBarProgress;
	private ProgressBar mProgressBar;

	private BatchManager mBatchManager;

	@Override
	protected void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState, R.layout.activity_batch);

		mBatchManager = new BatchManager(this, this);
		setBleBaseDeviceManager(mBatchManager);

		initialiseDialogAbout(getResources().getString(R.string.about_batch));
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
				mBatchManager.setFile(result);
				mProgressBar.setMax((int) mBatchManager.getFileWrapper()
						.getFileTotalSize());
				mProgressBar.setProgress(0);
				mTvProgressBarProgress.setText(0 + "/" + mProgressBar.getMax());
			}
		}

		viewMessagesManager();
	}

	@Override
	public void bindViews()
	{
		super.bindViews();

		mBtnFileSelect = findViewById(R.id.btnFileSelect);
		mBtnFileSend = findViewById(R.id.btnFileSend);
		mBtnFileStopSending = findViewById(R.id.btnFileStopSending);
		mValueFileNameTv = findViewById(R.id.valueFileNameTv);
		mValueValidDeviceTv = findViewById(R.id.valueValidDeviceTv);
		mValueStatusTv = findViewById(R.id.valueStatusTv);
		mValueErrorsTv = findViewById(R.id.valueErrorsTv);
		mValueDataSendTv = findViewById(R.id.valueDataSendTv);
		mScrollViewDataSend = findViewById(R.id.scrollViewDataSend);
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

		mBtnFileSend.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if (mBatchManager == null)
					return;
				/*
				 * start uploading procedure
				 */
				if (mBatchManager.getConnectionState() == BluetoothProfile.STATE_CONNECTED)
				{

					mBatchManager.startFileTransfer();

					mBtnFileSelect.setEnabled(false);
					mBtnFileSend.setEnabled(false);
					mBtnFileStopSending.setEnabled(true);

					mValueStatusTv.setText(R.string.valueStatusDownloading);
					mValueStatusTv.setTextColor(Color.BLUE);
					mValueErrorsTv.setText("");
					mValueDataSendTv.setText("");
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

		mBtnFileStopSending.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if (mBatchManager == null)
					return;
				/*
				 * Stop uploading procedure
				 */
				mBatchManager.stopFileUploading();

				mValueStatusTv.setText(R.string.value_status_stopped);
				mValueStatusTv.setTextColor(Color.parseColor(COLOR_RED));

				mBtnFileSelect.setEnabled(true);
				mBtnFileSend.setEnabled(true);
				mBtnFileStopSending.setEnabled(false);
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
					mBatchManager.setFile(result);
					// display file uploading progress
					mProgressBar.setMax((int) mBatchManager.getFileWrapper()
							.getFileTotalSize());

					mProgressBar.setProgress(0);
					mTvProgressBarProgress.setText(0 + "/"
							+ mProgressBar.getMax());

					mValueFileNameTv.setText(mBatchManager.getFileWrapper()
							.getFileName());

					viewMessagesManager();

				}
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	/**
	 * sets the messages and button states based on the state of the chosen file
	 * and the connection of the remote BLE device
	 */
	private void viewMessagesManager()
	{
		if (mBatchManager.getFifoAndVspManagerState() == FifoAndVspManagerState.READY_TO_SEND_DATA)
		{
			mValueStatusTv.setText("Ready");
			mValueStatusTv.setTextColor(Color.parseColor(COLOR_GREEN));

			mBtnFileSelect.setEnabled(true);
			mBtnFileSend.setEnabled(true);
			mBtnFileStopSending.setEnabled(false);

			mValueValidDeviceTv.setText("" + mBatchManager.isValidVspDevice());
			mValueValidDeviceTv.setTextColor(Color.parseColor(COLOR_GREEN));

			mValueErrorsTv.setText("");
			mValueDataSendTv.setText("");
		}
		else if (mBatchManager.getFileState() == FileState.FILE_CHOSEN
				&& mBatchManager.getConnectionState() == BluetoothProfile.STATE_DISCONNECTED)
		{
			mValueStatusTv.setText("Waiting for connection");
			mValueStatusTv.setTextColor(Color.GRAY);

			mValueValidDeviceTv.setText("");
			mValueErrorsTv.setText("");
			mValueDataSendTv.setText("");

			mBtnFileSelect.setEnabled(true);
			mBtnFileSend.setEnabled(false);
			mBtnFileStopSending.setEnabled(false);
		}
		else if (mBatchManager.getConnectionState() == BluetoothProfile.STATE_CONNECTED
				&& mBatchManager.getFileState() == FileState.FILE_NOT_CHOSEN)
		{
			mValueStatusTv.setText("Waiting for file to be chosen");
			mValueStatusTv.setTextColor(Color.GRAY);

			if (mBatchManager.isValidVspDevice())
			{
				mValueValidDeviceTv.setText(""
						+ mBatchManager.isValidVspDevice());
				mValueValidDeviceTv.setTextColor(Color.parseColor(COLOR_GREEN));
			}
			else
			{
				mValueValidDeviceTv.setText(""
						+ mBatchManager.isValidVspDevice());
				mValueValidDeviceTv.setTextColor(Color.parseColor(COLOR_RED));
			}

			mValueErrorsTv.setText("");
			mValueDataSendTv.setText("");

			mBtnFileSelect.setEnabled(true);
			mBtnFileSend.setEnabled(false);
			mBtnFileStopSending.setEnabled(false);
		}
		else if (mBatchManager.getConnectionState() == BluetoothProfile.STATE_DISCONNECTED
				|| mBatchManager.getConnectionState() == BluetoothProfile.STATE_DISCONNECTING)
		{
			mValueStatusTv.setText("Awaiting connection & file selection");
			mValueStatusTv.setTextColor(Color.GRAY);

			mValueValidDeviceTv.setText("");
			mValueErrorsTv.setText("");
			mValueDataSendTv.setText("");

			mBtnFileSelect.setEnabled(true);
			mBtnFileSend.setEnabled(false);
			mBtnFileStopSending.setEnabled(false);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.batch_menu, menu);
		getActionBar().setIcon(R.drawable.icon_batch);
		getActionBar().setDisplayHomeAsUpEnabled(true);

		return super.onCreateOptionsMenu(menu);
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
				viewMessagesManager();
			}
		});
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
				viewMessagesManager();
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
				viewMessagesManager();
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
				appendSendDataToConsole(dataSend);
				updateProgressBarView();
			}
		});
	}

	@Override
	public void onUiReceiveSuccessData(final String dataReceived)
	{
		runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				mValueDataSendTv.append(dataReceived);
				mScrollViewDataSend.smoothScrollTo(0,
						mValueDataSendTv.getBottom());
			}
		});
	}

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
				mBtnFileSend.setEnabled(true);
				mBtnFileStopSending.setEnabled(false);
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

				mBtnFileSelect.setEnabled(true);
				mBtnFileSend.setEnabled(true);
				mBtnFileStopSending.setEnabled(false);
			}
		});
	}

	@Override
	public void onUiReceiveData(String dataReceived)
	{}

	private void appendSendDataToConsole(final String dataSend)
	{
		if (mValueDataSendTv.getText().length() > 0)
		{

			mValueDataSendTv.append("\n**********" + dataSend);
		}
		else
		{
			// append the first data to the console
			mValueDataSendTv.append("\n**********\n" + dataSend);
		}
		mScrollViewDataSend.smoothScrollTo(0, mValueDataSendTv.getBottom());
	}

	private void updateProgressBarView()
	{
		/*
		 * update the progress bar and stopping it from filling beyond our limit
		 */
		FileWrapper fileWrapper = mBatchManager.getFileWrapper();

		if (mBatchManager.getFileWrapper().getFileCurrentSizeRead() > fileWrapper
				.getFileTotalSize())
		{
			mProgressBar.setProgress((int) fileWrapper.getFileTotalSize());
			mTvProgressBarProgress.setText(fileWrapper.getFileTotalSize() + "/"
					+ mProgressBar.getMax());
		}
		else
		{
			mProgressBar
					.setProgress((int) fileWrapper.getFileCurrentSizeRead());
			mTvProgressBarProgress.setText(fileWrapper.getFileCurrentSizeRead()
					+ "/" + mProgressBar.getMax());
		}
	}

}