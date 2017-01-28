package me.anon.grow.sync;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

/**
 * // TODO: Add class description
 */
public class ConfigureActivity extends AppCompatActivity
{
	@Override protected void onCreate(@Nullable Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setContentView(R.layout.toolbar_activity);
		setTitle("GrowSync configuration");

		if (savedInstanceState == null)
		{
			getFragmentManager().beginTransaction()
				.replace(R.id.fragment_holder, new ConfigureFragment(), "configure")
				.commit();
		}
	}

	public static class ConfigureFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener
	{
		@Override public void onCreate(Bundle savedInstanceState)
		{
			super.onCreate(savedInstanceState);

			addPreferencesFromResource(R.xml.preferences);

			((CheckBoxPreference)findPreference("send_encrypted")).setOnPreferenceChangeListener(this);
		}

		@Override public boolean onPreferenceChange(Preference preference, Object value)
		{
			if (preference.getKey().equalsIgnoreCase("send_encrypted"))
			{
				findPreference("encryption_key").setEnabled((Boolean)value);
			}

			return false;
		}
	}
}
