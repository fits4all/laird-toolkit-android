package com.lairdtech.lairdtoolkit.healththermometerdevice;

public interface ThermometerActivityUICallback
{

	/**
	 * Used on temperature change.
	 * @param result Result of temperature change.
	 */
	void onUiTemperatureChange(final String result);

}
