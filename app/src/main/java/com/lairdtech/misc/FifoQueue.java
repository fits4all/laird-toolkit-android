package com.lairdtech.misc;

import org.apache.commons.lang3.StringEscapeUtils;

import android.util.Log;

/**
 * storing data into a buffer and makes it easy to read, write data to it. <br>
 * Used by the VSP functionalities to store the data that is receiving and the
 * data that will be sending
 */
public class FifoQueue
{
	private static final String TAG = "FifoQueue";

	private StringBuilder mBufferData = new StringBuilder();

	public int getSize()
	{
		return mBufferData.length();
	}

	/**
	 * clears the whole buffer
	 */
	public void flush()
	{
		mBufferData.delete(0, mBufferData.length());
	}

	/**
	 * Add new data in buffer, the data will be kept in the buffer and get
	 * merged with new data whenever this method is called
	 * 
	 * @param value
	 *            the data to be appended in the buffer
	 */
	public void write(String value)
	{
		mBufferData.append(value);
	}

	/**
	 * read all data from buffer, data read gets removed from the buffer
	 * 
	 * @param dest
	 *            the object to store the data read
	 * @return the total number of characters read
	 */
	public int read(StringBuilder dest)
	{
		if (mBufferData.length() > 0)
		{
			// we have data in the buffer
			dest.append(mBufferData.substring(0, mBufferData.length()));
			mBufferData.delete(0, mBufferData.length());

			return dest.length();
		}

		return 0;
	}

	/**
	 * read the maximum data which is defined by the mMaxDataToBeReadFromBuffer
	 * parameter. If there is less data than the mMaxDataToBeReadFromBuffer then
	 * all the data gets read from the buffer. The data read gets removed from
	 * the buffer.
	 * 
	 * @param dest
	 *            the object to store the data read
	 * @param maxDataToBeReadFromBuffer
	 *            the maximum data to read from the buffer
	 * @return the total number of characters read
	 */
	public int read(StringBuilder dest, int maxDataToBeReadFromBuffer)
	{
		Log.i(TAG, "reading " + maxDataToBeReadFromBuffer
				+ " bytes from the FIFO queue");

		if (mBufferData.length() <= maxDataToBeReadFromBuffer)
		{
			/*
			 * ignore the mMaxDataToBeReadFromBuffer variable and return the
			 * last data remaining from the buffer
			 */
			dest.append(mBufferData.substring(0, mBufferData.length()));
			mBufferData.delete(0, mBufferData.length());
			return dest.length();
		}
		else if (mBufferData.length() > maxDataToBeReadFromBuffer)
		{
			/*
			 * return data from the buffer until the mMaxDataToBeReadFromBuffer
			 */
			dest.append(mBufferData.substring(0, maxDataToBeReadFromBuffer));
			mBufferData.delete(0, maxDataToBeReadFromBuffer);
			return dest.length();
		}

		return 0;
	}

	/**
	 * searches from the buffer the searchFor parameter string value. The search
	 * for the character starts at the beginning and moves towards the end of
	 * the buffer. If the searchFor string is found then the data from index 0
	 * to and including the searchFor string is read from the buffer. The data
	 * read gets removed from the buffer.
	 * 
	 * @param dest
	 *            the object to append the data read
	 * @param searchFor
	 *            the string to search the buffer for
	 * @return the total number of characters read
	 */
	public int read(StringBuilder dest, String searchFor)
	{
		Log.i(TAG, "reading from the FIFO queue until the character '"
				+ StringEscapeUtils.escapeJava(searchFor) + "' is found");

		int stringToSearchForIndex = mBufferData.indexOf(searchFor);

		if (stringToSearchForIndex != -1)
		{
			dest.append(mBufferData.substring(0, stringToSearchForIndex + 1));
			mBufferData.delete(0, stringToSearchForIndex + 1);
			return dest.length();
		}

		return 0;
	}

}