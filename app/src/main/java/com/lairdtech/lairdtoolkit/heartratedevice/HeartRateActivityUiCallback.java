/*****************************************************************************
 * Copyright (c) 2014 Laird Technologies. All Rights Reserved.
 * 
 * The information contained herein is property of Laird Technologies.
 * Licensees are granted free, non-transferable use of the information. NO WARRANTY of ANY KIND is provided. 
 * This heading must NOT be removed from the file.
 ******************************************************************************/

package com.lairdtech.lairdtoolkit.heartratedevice;

public interface HeartRateActivityUiCallback
{
	/**
	 * gets called whenever the heart rate measurement changes from the remote
	 * BLE device
	 * 
	 * @param valueHRM
	 *            the new heart rate measurement value
	 */
	public void onUiHRM(final String valueHRM);

	/**
	 * called when the body sensor location characteristic is read with its
	 * current body sensor location value
	 * 
	 * @param valueBodySensorLocation
	 *            the current body sensor location value
	 */
	public void onUiBodySensorLocation(final String valueBodySensorLocation);
}
