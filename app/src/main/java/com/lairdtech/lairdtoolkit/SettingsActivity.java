package com.lairdtech.lairdtoolkit;

import android.app.ActionBar;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.MenuItem;

public class SettingsActivity extends PreferenceActivity
{

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		getFragmentManager().beginTransaction()
				.replace(android.R.id.content, new SettingsFragment()).commit();

		ActionBar ab = getActionBar();
		ab.setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item)
	{
		if (item.getItemId() == android.R.id.home) {
			finish();
		}
		return super.onMenuItemSelected(featureId, item);
	}
}