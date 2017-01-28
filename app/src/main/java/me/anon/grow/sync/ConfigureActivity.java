package me.anon.grow.sync;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

/**
 * // TODO: Add class description
 */
public class ConfigureActivity extends AppCompatActivity
{
	@Override protected void onCreate(@Nullable Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setContentView(R.layout.toolbar_activity);
		setSupportActionBar((Toolbar)findViewById(R.id.toolbar));
		setTitle("GrowSync configuration");

		if (savedInstanceState == null)
		{
			getFragmentManager().beginTransaction()
				.replace(R.id.fragment_holder, new ConfigureFragment(), "configure")
				.commit();
		}
	}

	public static class ConfigureFragment extends PreferenceFragment
	{
		@Override public void onCreate(Bundle savedInstanceState)
		{
			super.onCreate(savedInstanceState);

			addPreferencesFromResource(R.xml.preferences);
		}
	}
}
