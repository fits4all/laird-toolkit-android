package com.lairdtech.lairdtoolkit.otadevice;

import com.lairdtech.bt.ble.vsp.VirtualSerialPortDeviceCallback;

public interface OTAManagerUiCallback extends VirtualSerialPortDeviceCallback{

    /**
     * Callback for when a success response data was sent
     * from the remote device
     * 
	 * @param dataReceived the data that was send from the remote device
     */
	void onUiReceiveSuccessData(String dataReceived);
	
    /**
     * Callback for when an error response data was sent
     * from the remote device
     * 
     * @param errorCode the error code value that was send from the remote device
     */
	void onUiReceiveErrorData(final String errorCode);

}
