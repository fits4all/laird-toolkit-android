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
	void onUiReceiveSuccessData(String dataReceived);

	/**
	 * Callback for when an error response data was sent from the remote BLE
	 * device
	 * 
	 * @param errorCode
	 *            the error code value that was send from the remote device
	 */
	void onUiReceiveErrorData(final String errorCode);

}
