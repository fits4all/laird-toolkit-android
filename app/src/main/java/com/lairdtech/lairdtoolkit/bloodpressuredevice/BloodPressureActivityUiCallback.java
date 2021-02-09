package com.lairdtech.lairdtoolkit.bloodpressuredevice;

public interface BloodPressureActivityUiCallback
{

	void onUIBloodPressureRead(final float valueSystolic,
			final float valueDiastolic, final float valueArterialPressure);

}