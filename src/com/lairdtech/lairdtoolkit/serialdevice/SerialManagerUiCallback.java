/*****************************************************************************
 * Copyright (c) 2014 Laird Technologies. All Rights Reserved.
 * 
 * The information contained herein is property of Laird Technologies.
 * Licensees are granted free, non-transferable use of the information. NO WARRANTY of ANY KIND is provided. 
 * This heading must NOT be removed from the file.
 ******************************************************************************/

package com.lairdtech.lairdtoolkit.serialdevice;

import com.lairdtech.bt.ble.vsp.VirtualSerialPortDeviceCallback;

public interface SerialManagerUiCallback extends
		VirtualSerialPortDeviceCallback
{
	/*
	 * currently empty but might be used int he future for having serial
	 * functionality specific callbacks
	 */
}
