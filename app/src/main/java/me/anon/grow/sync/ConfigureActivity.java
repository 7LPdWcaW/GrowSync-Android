package me.anon.grow.sync;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import me.anon.grow.helper.PermissionHelper;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;

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
		private static final int REQUEST_STORAGE_PERMISSION = 0x1;

		@Override public void onCreate(Bundle savedInstanceState)
		{
			super.onCreate(savedInstanceState);

			addPreferencesFromResource(R.xml.preferences);

			if (PermissionHelper.hasPermission(getActivity(), READ_EXTERNAL_STORAGE))
			{
				getPreferenceScreen().removePreference(findPreference("permission_container"));
			}
			else
			{
				findPreference("permission").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
				{
					@Override public boolean onPreferenceClick(Preference preference)
					{
						PermissionHelper.doPermissionCheck(ConfigureFragment.this, READ_EXTERNAL_STORAGE, REQUEST_STORAGE_PERMISSION, "Storage permission is required to read files saved by the app");
						return true;
					}
				});
			}

			((CheckBoxPreference)findPreference("send_encrypted")).setOnPreferenceChangeListener(this);
			findPreference("encryption_key").setEnabled(((CheckBoxPreference)findPreference("send_encrypted")).isChecked());
		}

		@Override public boolean onPreferenceChange(Preference preference, Object value)
		{
			if (preference.getKey().equalsIgnoreCase("send_encrypted"))
			{
				((CheckBoxPreference)preference).setChecked((Boolean)value);
				findPreference("encryption_key").setEnabled((Boolean)value);
			}

			return false;
		}

		@Override public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
		{
			super.onRequestPermissionsResult(requestCode, permissions, grantResults);

			if (requestCode == REQUEST_STORAGE_PERMISSION && grantResults[0] == PackageManager.PERMISSION_GRANTED)
			{
				getPreferenceScreen().removePreference(findPreference("permission_container"));
			}
		}
	}
}
