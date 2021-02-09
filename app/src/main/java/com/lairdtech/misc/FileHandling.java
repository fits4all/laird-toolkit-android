/*******************************************************************************
 * Copyright (c) 2014 Laird Technologies. All Rights Reserved.
 * 
 * The information contained herein is property of Laird Technologies.
 * Licensees are granted free, non-transferable use of the information. NO WARRANTY of ANY KIND is provided. 
 * This heading must NOT be removed from the file.
 *******************************************************************************/

package com.lairdtech.misc;

import java.io.File;
import java.io.FileInputStream;

import org.apache.commons.lang3.StringEscapeUtils;

import android.util.Log;

/**
 * This class is used for file manipulation <li>Reading data from a text
 * file</li> <li>Getting file size</li>
 */
public abstract class FileHandling
{
	final public static String TAG = "FileHandling";

	/**
	 * reads from the file as many bytes as specified in the totalBytesToRead
	 * variable of the FileWrapper object. This value is specified when the
	 * FileWrapper object gets instantiated.
	 * 
	 * @param fileWrapper
	 *            the fileWrapper object associated with the file you want to
	 *            read
	 * @return the data read as HEX string or null if EOF was reached
	 */
	public static byte[] readUntilTotalBytesToRead(FileWrapper fileWrapper,
			int totalBytesToRead)
	{
		FileInputStream is = fileWrapper.getFileInputStream();
		int fileEnd = 0;
		byte[] bFile = new byte[(int) totalBytesToRead];

		try
		{

			fileEnd = is.read(bFile, 0, (int) totalBytesToRead);

			if (fileEnd == -1)
			{
				// EOF
				is.close();
				fileWrapper.setIsEOF(true);
				Log.i(TAG, "isFileFinishedRead TRUE");
				return null;
			}
			else
			{
				fileWrapper.setFileCurrentSizeRead((int) (fileWrapper
						.getFileCurrentSizeRead() + totalBytesToRead));
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		Log.i(TAG, "readUntilTotalBytesToRead bFile: " + bFile);

		return bFile;
	}

	/**
	 * reads from the file the data until the specified string parameter value
	 * is found.
	 * 
	 * @param fileWrapper
	 *            the fileWrapper object associated with the file you want to
	 *            read
	 * @param readUntil
	 *            the string to read data until it's found
	 * @return the data from the file index started until and including the
	 *         "readUntil" string, returns null when EOF
	 */
	public static String readUntilASpecificChar(FileWrapper fileWrapper,
			String readUntil)
	{
		FileInputStream is = fileWrapper.getFileInputStream();
		StringBuilder result = new StringBuilder();
		int charAsInteger = 0;
		char ch = 0;

		if (fileWrapper.getIsEOF() == true)
		{
			return null;
		}

		try
		{
			while (true)
			{
				charAsInteger = is.read();

				if (charAsInteger == -1)
				{
					// EOF
					Log.i(TAG, "isFileFinishedRead TRUE");

					is.close();
					fileWrapper.setIsEOF(true);

					if (result.length() > 0)
					{
						return result.toString();
					}
					else
					{
						return null;
					}
				}
				else
				{
					ch = (char) charAsInteger;
					result.append(ch);
					// updated the total bytes read whenever we read 1 byte
					fileWrapper.setFileCurrentSizeRead((int) (fileWrapper
							.getFileCurrentSizeRead() + 1));

					if ((result.toString().contains(readUntil)))
					{
						break;
					}
				}
			}

			System.out.println("*********************************");
			Log.i(TAG, StringEscapeUtils
					.escapeJava("Data that was just read from TXT: " + result));

			return result.toString();

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Calculates the size of a file in bytes
	 * 
	 * @return the file size or -1 if file is not found
	 */
	public static long getFileSize(File file)
	{
		if (!file.exists() || !file.isFile())
		{
			System.out.println("File doesn't exist");
			return -1;
		}
		return file.length();
	}
}