package com.lairdtech.misc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.apache.commons.lang3.StringEscapeUtils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public class FileWrapper
{
	final private static String TAG = "FileWrapper";

	private File mFile;
	private String mFilePath;

	private Uri mUri;
	private String mFileName;
	private String mFileNameWithoutExtension;
	private String mExtension;
	private String mModuleFileName;
	private long mFileTotalSize = 0;
	private int mFileCurrentSizeRead = 0;
	private boolean isEOF;
	private FileInputStream mFileInputStream;

	private Context mContext;

	public FileWrapper(Uri uri, Context context)
	{
		isEOF = false;
		mUri = uri;
		mContext = context;

		if (mUri != null && mUri.getScheme().equals("content"))
		{
			/*
			 * google drive files
			 */
			Cursor c = context.getContentResolver().query(mUri, null, null,
					null, null);
			c.moveToFirst();
			int colCounter = c.getColumnCount();

			for (int i = 0; i < colCounter; i++)
			{

				if ("_id".equals(c.getColumnName(i)))
				{

				}
				else if ("document_id".equals(c.getColumnName(i)))
				{

				}
				else if ("_display_name".equals(c.getColumnName(i)))
				{
					mFileName = c.getString(i);

				}
				else if ("_size".equals(c.getColumnName(i)))
				{
					mFileTotalSize = Integer.parseInt(c.getString(i));

				}
				else if ("_mime_type".equals(c.getColumnName(i)))
				{

				}
				else if ("flags".equals(c.getColumnName(i)))
				{

				}
				else if ("last_modified".equals(c.getColumnName(i)))
				{

				}
				else if ("_icon".equals(c.getColumnName(i)))
				{

				}
			}
		}
		else
		{
			/*
			 * other file and cloud based explorers
			 */
			mFilePath = mUri.getPath();
			mFile = new File(mFilePath);
			mFileTotalSize = FileHandling.getFileSize(mFile);
			mFileName = mUri.getLastPathSegment();

		}

		setFileNameWithoutExtension();
		setExtension();
		setModuleFileName();
	}

	public Uri getUri()
	{
		return mUri;
	}

	public File getFile()
	{
		return mFile;
	}

	public String getFilePath()
	{
		return mFilePath;
	}

	public String getFileName()
	{
		return mFileName;
	}

	public String getFileNameWithoutExtension()
	{
		return mFileNameWithoutExtension;
	}

	public String getExtension()
	{
		return mExtension;
	}

	public String getModuleFileName()
	{
		return mModuleFileName;
	}

	public long getFileTotalSize()
	{
		return mFileTotalSize;
	}

	public long getFileCurrentSizeRead()
	{
		return mFileCurrentSizeRead;
	}

	public boolean getIsEOF()
	{
		return isEOF;
	}

	public FileInputStream getFileInputStream()
	{
		return mFileInputStream;
	}

	/**
	 * set the file name without an exception
	 */
	private void setFileNameWithoutExtension()
	{
		int dotPosition = mFileName.lastIndexOf(".");
		if (dotPosition != -1)
		{
			mFileNameWithoutExtension = mFileName.substring(0, dotPosition);
		}
		else
		{
			mFileNameWithoutExtension = "";
		}
	}

	/**
	 * sets the extension of the file.
	 */
	private void setExtension()
	{
		int dotposition = mFileName.lastIndexOf(".");
		if (dotposition != -1)
		{
			mExtension = mFileName.substring(dotposition + 1,
					mFileName.length());
		}
		else
		{
			mExtension = "";
		}
	}

	/**
	 * defines what the filename of the file on the external device will be.
	 */
	private void setModuleFileName()
	{

		if (mExtension.equalsIgnoreCase("uwc"))
		{
			// gets all the text found until the first occurrence of a dot "."
			int firstDotposition = mFileName.indexOf(".");
			if (firstDotposition != -1)
			{

				mModuleFileName = mFileName.substring(0, firstDotposition);
			}
			else
			{
				mModuleFileName = "";
			}
		}
		else
		{
			// not a .uwc extension so lets save this file on the device with
			// it's full name
			mModuleFileName = mFileName;
		}

	}

	/**
	 * set the current size read from the file
	 * 
	 * @param value
	 *            the new value to set it to
	 */
	protected void setFileCurrentSizeRead(int value)
	{
		mFileCurrentSizeRead = value;
	}

	protected void setIsEOF(boolean value)
	{
		isEOF = value;
	}

	/**
	 * sets to default the values of the current FileWrapper object. This should
	 * be called whenever is needed to start reading the file from the start
	 */
	public void setToDefaultValues()
	{
		mFileCurrentSizeRead = 0;
		isEOF = false;

		if (mFile != null)
		{
			Log.i(TAG, "Not a file from google drive");

			try
			{
				mFileInputStream = new FileInputStream(mFile);
			}
			catch (FileNotFoundException e)
			{
				e.printStackTrace();
			}

		}
		else
		{
			Log.i(TAG, "A file from google drive");

			try
			{
				mFileInputStream = (FileInputStream) mContext
						.getContentResolver().openInputStream(mUri);
			}
			catch (FileNotFoundException e)
			{
				e.printStackTrace();
			}
		}
	}

	/**
	 * reads from the file as many bytes as specified in the bytesToRead
	 * parameter and then converts them to a HEX string format
	 * 
	 * @param totalBytesToRead
	 *            the total bytes to read from the file
	 * @return the data read as HEX string or null if EOF was reached
	 */
	public String readNextHEXStringFromFile(int totalBytesToRead)
	{
		String result = DataManipulation.bytesToHex(FileHandling
				.readUntilTotalBytesToRead(this, totalBytesToRead));

		System.out.println("*********************************");
		Log.i(TAG, StringEscapeUtils
				.escapeJava("*********************************"
						+ "HEX Data that was just read from TXT: " + result));

		return result;
	}

	/**
	 * reads from the text file associated with this object data until the
	 * string parameter that is given is found.
	 * 
	 * @param readUntil
	 *            the string to read data until it's found
	 * @return the data from the file index started until and including the
	 *         "readUntil" string
	 */
	public String readUntilASpecificChar(String readUntil)
	{
		return FileHandling.readUntilASpecificChar(this, readUntil);
	}
}