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
	void onUiHRM(final String valueHRM);

	/**
	 * called when the body sensor location characteristic is read with its
	 * current body sensor location value
	 * 
	 * @param valueBodySensorLocation
	 *            the current body sensor location value
	 */
	void onUiBodySensorLocation(final String valueBodySensorLocation);

}
