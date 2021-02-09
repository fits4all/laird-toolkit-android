/*****************************************************************************
 * Copyright (c) 2014 Laird Technologies. All Rights Reserved.
 * 
 * The information contained herein is property of Laird Technologies.
 * Licensees are granted free, non-transferable use of the information. NO WARRANTY of ANY KIND is provided. 
 * This heading must NOT be removed from the file.
 ******************************************************************************/

package com.lairdtech.lairdtoolkit;

public interface IBleBaseActivityUiCallback
{

	public void onUiConnected();

	public void onUiConnecting();

	public void onUiDisconnected(final int status);

	public void onUiDisconnecting();

	public void onUiBatteryRead(final String valueBattery);

	public void onUiReadRemoteRssi(final int valueRSSI);
}