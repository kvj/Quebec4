package org.kvj.quebec4.ui;

import org.kvj.quebec4.R;
import org.kvj.quebec4.service.Q4App;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class ConfigActivity extends PreferenceActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.config);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Q4App.getInstance().updateWidgets(-1);
	}
}
