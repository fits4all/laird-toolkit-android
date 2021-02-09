/*****************************************************************************
 * Copyright (c) 2014 Laird Technologies. All Rights Reserved.
 * 
 * The information contained herein is property of Laird Technologies.
 * Licensees are granted free, non-transferable use of the information. NO WARRANTY of ANY KIND is provided. 
 * This heading must NOT be removed from the file.
 ******************************************************************************/

package com.lairdtech.lairdtoolkit.batchdevice;

import com.lairdtech.bt.ble.vsp.VirtualSerialPortDeviceCallback;

/**
 * responsible for the BatchActivity callback's
 * 
 * @author Kyriakos.Alexandrou
 * 
 */
public interface BatchManagerUiCallback extends VirtualSerialPortDeviceCallback
{

	/**
	 * Callback for when a success response data was sent from the remote BLE
	 * device
	 * 
	 * @param dataReceived
	 *            the success data that was send from the remote device
	 */
	public void onUiReceiveSuccessData(String dataReceived);

	/**
	 * Callback for when an error response data was sent from the remote BLE
	 * device
	 * 
	 * @param errorCode
	 *            the error code value that was send from the remote device
	 */
	public void onUiReceiveErrorData(final String errorCode);
}
