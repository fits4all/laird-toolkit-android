/*****************************************************************************
 * Copyright (c) 2014 Laird Technologies. All Rights Reserved.
 * 
 * The information contained herein is property of Laird Technologies.
 * Licensees are granted free, non-transferable use of the information. NO WARRANTY of ANY KIND is provided. 
 * This heading must NOT be removed from the file.
 ******************************************************************************/

package com.lairdtech.bt.ble.vsp;

/**
 * interface to be able to receive callback's for when doing operations on a
 * remote BLE device that has the Virtual Serial Port (VSP) service
 * 
 * Use the VirtualSerialPortDevice class to do VSP operations
 * 
 * @author Kyriakos.Alexandrou
 * 
 */
public interface VirtualSerialPortDeviceCallback
{
	/**
	 * Callback indicating if the VSP service was found or not on the remote BLE
	 * device
	 * 
	 * @param found
	 *            true if VSP service was found, otherwise false
	 */
	public void onUiVspServiceFound(final boolean found);

	/**
	 * Callback that notifies us that the data was sent successfully to the
	 * remote BLE device
	 * 
	 * @param dataSend
	 *            the value that was successfully send to the remote BLE device
	 */
	public void onUiSendDataSuccess(final String dataSend);

	/**
	 * Callback for when data is received from the remote device
	 * 
	 * @param dataReceived
	 *            the value that was received from the remote device
	 */
	public void onUiReceiveData(final String dataReceived);

	public void onUiUploaded();
}