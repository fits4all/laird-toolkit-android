/*******************************************************************************
 * Copyright (c) 2014 Laird Technologies. All Rights Reserved.
 * 
 * The information contained herein is property of Laird Technologies.
 * Licensees are granted free, non-transferable use of the information. NO WARRANTY of ANY KIND is provided. 
 * This heading must NOT be removed from the file.
 *******************************************************************************/

package com.lairdtech.bt.ble;

import java.util.UUID;

/**
 * Contains UUIDs of some of the services, characteristics and descriptors. More
 * can be added if needed.
 * <p>
 * All of this can be used to help you make checks without actually knowing the
 * UUID number. <br>
 * For example when receiving a read characteristics callback you can have if
 * statements there to check which characteristic value was just read. Example:
 * 
 * <pre>
 * {@code
 * if(DefinedBleUUIDs.Characteristic.BATTERY.equals(currentCharacteristic.getUUID())){
 * 
 *     do specific battery characteristic logic
 * 
 * } else if(DefinedBleUUIDs.Characteristic.HEART_RATE_MEASUREMENT.equals(currentCharacteristic.getUUID())){
 * 
 *     do specific to heart rate measurement characteristic logic
 * 
 * }
 * </pre>
 * 
 * }
 */
public abstract class DefinedBleUUIDs
{
	public static class Service
	{
		final static public UUID GENERIC_ACCESS = UUID
				.fromString("00001800-0000-1000-8000-00805f9b34fb");
		final static public UUID GENERIC_ATTRIBUTE = UUID
				.fromString("00001801-0000-1000-8000-00805f9b34fb");
		final static public UUID IMMEDIATE_ALERT = UUID
				.fromString("00001802-0000-1000-8000-00805f9b34fb");
		final static public UUID LINK_LOSS = UUID
				.fromString("00001803-0000-1000-8000-00805f9b34fb");
		final static public UUID TX_POWER = UUID
				.fromString("00001804-0000-1000-8000-00805f9b34fb");
		final static public UUID CURRENT_TIME_SERVICE = UUID
				.fromString("00001805-0000-1000-8000-00805f9b34fb");
		final static public UUID REFERENCE_TIME_UPDATE_SERVICE = UUID
				.fromString("00001806-0000-1000-8000-00805f9b34fb");
		final static public UUID NEXT_DST_CHANGE_SERVICE = UUID
				.fromString("00001807-0000-1000-8000-00805f9b34fb");
		final static public UUID GLUCOSE = UUID
				.fromString("00001808-0000-1000-8000-00805f9b34fb");
		final static public UUID HEALTH_THERMOMETER = UUID
				.fromString("00001809-0000-1000-8000-00805f9b34fb");
		final static public UUID BLOOD_PRESSURE = UUID
				.fromString("00001810-0000-1000-8000-00805f9b34fb");
		final static public UUID ALERT_NOTIFICATION_SERVICE = UUID
				.fromString("00001811-0000-1000-8000-00805f9b34fb");
		final static public UUID HUMAN_INTERFACE_DEVICE = UUID
				.fromString("00001812-0000-1000-8000-00805f9b34fb");
		final static public UUID SCAN_PARAMETERS = UUID
				.fromString("00001813-0000-1000-8000-00805f9b34fb");
		final static public UUID RUNNING_SPEED_AND_CADENCE = UUID
				.fromString("00001814-0000-1000-8000-00805f9b34fb");
		final static public UUID CYCLING_SPEED_AND_CADENCE = UUID
				.fromString("00001816-0000-1000-8000-00805f9b34fb");
		final static public UUID CYCLING_POWER = UUID
				.fromString("00001818-0000-1000-8000-00805f9b34fb");
		final static public UUID LOCATION_AND_NAVIGATION = UUID
				.fromString("00001819-0000-1000-8000-00805f9b34fb");
		final static public UUID DEVICE_INFORMATION = UUID
				.fromString("0000180a-0000-1000-8000-00805f9b34fb");
		final static public UUID USER_DATA = UUID
				.fromString("0000180c-0000-1000-8000-00805f9b34fb");
		final static public UUID HEART_RATE = UUID
				.fromString("0000180d-0000-1000-8000-00805f9b34fb");
		final static public UUID PHONE_ALERT_STATUS_SERVICE = UUID
				.fromString("0000180e-0000-1000-8000-00805f9b34fb");
		final static public UUID BATTERY = UUID
				.fromString("0000180f-0000-1000-8000-00805f9b34fb");
	};

	public static class Characteristic
	{
		/*
		 * Generic Access
		 */
		final static public UUID DEVICE_NAME = UUID
				.fromString("00002a00-0000-1000-8000-00805f9b34fb");
		final static public UUID APPEARANCE = UUID
				.fromString("00002a01-0000-1000-8000-00805f9b34fb");
		final static public UUID ALERT_LEVEL = UUID
				.fromString("00002a06-0000-1000-8000-00805f9b34fb");
		final static public UUID TX_POWER_LEVEL = UUID
				.fromString("00002a07-0000-1000-8000-00805f9b34fb");
		final static public UUID BATTERY_LEVEL = UUID
				.fromString("00002a19-0000-1000-8000-00805f9b34fb");
		/*
		 * Device Information
		 */
		final static public UUID MODEL_NUMBER_STRING = UUID
				.fromString("00002a24-0000-1000-8000-00805f9b34fb");
		final static public UUID MANUFACTURER_STRING = UUID
				.fromString("00002a29-0000-1000-8000-00805f9b34fb");
		final static public UUID FIRMWARE_REVISION_STRING = UUID
				.fromString("00002a26-0000-1000-8000-00805f9b34fb");
		final static public UUID BLOOD_PRESSURE_MEASUREMENT = UUID
				.fromString("00002a35-0000-1000-8000-00805f9b34fb");
		final static public UUID HEART_RATE_MEASUREMENT = UUID
				.fromString("00002a37-0000-1000-8000-00805f9b34fb");
		final static public UUID BODY_SENSOR_LOCATION = UUID
				.fromString("00002a38-0000-1000-8000-00805f9b34fb");
		final static public UUID TEMPERATURE_MEASUREMENT = UUID
				.fromString("00002a1c-0000-1000-8000-00805f9b34fb");
	}

	public static class Descriptor
	{
		final static public UUID CLIENT_CHARACTERISTIC_CONFIGURATION_DESCRIPTOR = UUID
				.fromString("00002902-0000-1000-8000-00805f9b34fb");
	}
}