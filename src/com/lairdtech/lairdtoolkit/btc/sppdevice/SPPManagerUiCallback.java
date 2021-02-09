package com.lairdtech.lairdtoolkit.btc.sppdevice;

public interface SPPManagerUiCallback
{
	/**
	 * called when the BT classic device gets connected
	 */
	public void onUiBtcRemoteDeviceConnected();

	/**
	 * called when the BT classic device gets disconnected
	 */
	public void onUiBtcRemoteDeviceDisconnected();

	public void onUiBtcRemoteDeviceFailed();

	/**
	 * called when data is received from the remote BT classic device
	 */
	public void onUiRemoteDeviceRead(String result);
}
