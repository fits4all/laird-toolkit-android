package com.lairdtech.lairdtoolkit.btc.sppdevice;

public interface SPPManagerUiCallback
{
	/**
	 * Called when the BT classic device gets connected
	 */
	void onUiBtcRemoteDeviceConnected();

	/**
	 * Called when the BT classic device gets disconnected
	 */
	void onUiBtcRemoteDeviceDisconnected();

	/**
	 * Called when the btc classic devices fails or errors
	 */
	void onUiBtcRemoteDeviceFailed();

	/**
	 * Called when data is received from the remote BT classic device
	 */
	void onUiRemoteDeviceRead(String result);

}
