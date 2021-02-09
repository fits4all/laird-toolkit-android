package com.lairdtech.lairdtoolkit.proximitydevice;

public interface ProximityActivityUiCallback
{
	/**
	 * called when the TX power characteristic is read
	 * 
	 * @param txPower
	 *            the tx power value from the remote device
	 */
	void onUiReadTxPower(final byte[] txPower);

}
