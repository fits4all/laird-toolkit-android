package com.lairdtech.lairdtoolkit;

public interface IBleBaseActivityUiCallback
{

	void onUiConnected();

	void onUiConnecting();

	void onUiDisconnected(final int status);

	void onUiDisconnecting();

	void onUiBatteryRead(final String valueBattery);

	void onUiReadRemoteRssi(final int valueRSSI);

}