/*******************************************************************************
 * Copyright (c) 2014 Laird Technologies. All Rights Reserved.
 * 
 * The information contained herein is property of Laird Technologies.
 * Licensees are granted free, non-transferable use of the information. NO WARRANTY of ANY KIND is provided. 
 * This heading must NOT be removed from the file.
 *******************************************************************************/

package com.lairdtech.bt;

import java.util.UUID;

import android.bluetooth.BluetoothDevice;

/**
 * Scanning/Stopping and finding BT classic and BLE devices callback's.
 * implement this interface in the class that you want to get this callback's.
 */
public interface BluetoothAdapterHelperCallback
{

	/**
	 * callback indicating that a BT classic scanning operation stopped
	 */
	public void onDiscoveryStop();

	/**
	 * Callback reporting a BT classic device found during a scan.
	 * 
	 * @param device
	 *            Identifies the remote device
	 * @param rssi
	 *            The RSSI value for the remote device as reported by the
	 *            Bluetooth hardware. 0 if no RSSI value is available.
	 */
	public void onDiscoveryDeviceFound(BluetoothDevice device, int rssi);

	/**
	 * callback indicating that a BLE scanning operation stopped
	 */
	public void onBleStopScan();

	/**
	 * Callback reporting a BLE device found during a scan that was initiated by
	 * the <br>
	 * {@link BluetoothAdapterHelper#startBleScan()} or <br>
	 * {@link BluetoothAdapterHelper#startBleScan(UUID[])} or <br>
	 * {@link BluetoothAdapterHelper#startBleScanPeriodically()} or <br>
	 * {@link BluetoothAdapterHelper#startBleScanPeriodically(UUID[])}
	 * 
	 * @param device
	 *            Identifies the remote device
	 * @param rssi
	 *            The RSSI value for the remote device as reported by the
	 *            Bluetooth hardware. 0 if no RSSI value is available.
	 * @param scanRecord
	 *            The content of the advertisement record offered by the remote
	 *            device.
	 */
	public void onBleDeviceFound(BluetoothDevice device, int rssi,
			byte[] scanRecord);
}