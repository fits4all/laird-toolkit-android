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
	void onUiVspServiceFound(final boolean found);

	/**
	 * Callback that notifies us that the data was sent successfully to the
	 * remote BLE device
	 * 
	 * @param dataSend
	 *            the value that was successfully send to the remote BLE device
	 */
	void onUiSendDataSuccess(final String dataSend);

	/**
	 * Callback for when data is received from the remote device
	 * 
	 * @param dataReceived
	 *            the value that was received from the remote device
	 */
	void onUiReceiveData(final String dataReceived);

	void onUiUploaded();

}