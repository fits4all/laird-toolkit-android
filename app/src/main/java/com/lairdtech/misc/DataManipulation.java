/*******************************************************************************
 * Copyright (c) 2014 Laird Technologies. All Rights Reserved.
 * 
 * The information contained herein is property of Laird Technologies.
 * Licensees are granted free, non-transferable use of the information. NO WARRANTY of ANY KIND is provided. 
 * This heading must NOT be removed from the file.
 *******************************************************************************/

package com.lairdtech.misc;

import android.util.Log;

/**
 * A wrapper that contains String manipulation logic
 */
public class DataManipulation
{
	private final static String TAG = "DataManipulation";

	/**
	 * searches the given response string for a start and end string. if start
	 * or end parameters are null a NullPointerException will be thrown.
	 * 
	 * @param start
	 *            the string to search first
	 * @param end
	 *            the string to search for last
	 * @param response
	 *            the string to search
	 * 
	 * @return whatever is between the start and end strings (the start and end
	 *         characters are not included). returns returns what is between the
	 *         start and end string, else returns null
	 */
	public static String stripStringValue(String start, String end,
			String response)
	{
		if (start == null || end == null)
			throw new NullPointerException(
					"start or end string parameter values passed is NULL");

		int startPos = response.indexOf(start);
		int endPos = response.indexOf(end);

		if (startPos != -1 || endPos != -1)
		{
			Log.i(TAG, "stripStringValue response: " + response);
			Log.i(TAG, "stripStringValue startPos+1: " + startPos + 1);
			Log.i(TAG, "stripStringValue endPos: " + endPos);
			Log.i(TAG,
					"stripStringValue response.length(): " + response.length());

			return response.substring(startPos + 1, endPos);
		}
		return null;
	}

	final private static char[] hexArray = "0123456789ABCDEF".toCharArray();

	/**
	 * Converts an array of bytes to a HEX string
	 * 
	 * @param bytes
	 *            the data to be converted to HEX
	 * @return hexChars the converted bytes in a HEX string format
	 */
	public static String bytesToHex(byte[] bytes)
	{
		if (bytes == null)
			return null;

		String result;

		char[] hexChars = new char[bytes.length * 2];
		for (int j = 0; j < bytes.length; j++)
		{
			int v = bytes[j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}

		result = new String(hexChars);

		Log.i(TAG, "bytesToHex: " + result);
		return result;
	}
}