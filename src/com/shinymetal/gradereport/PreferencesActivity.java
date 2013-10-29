package com.shinymetal.gradereport;

import com.shinymetal.gradereport.R;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
// import android.widget.Button;

public class PreferencesActivity extends PreferenceActivity {

	public static class MyPreferenceFragment extends PreferenceFragment {
		@Override
		public void onCreate(final Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			
			addPreferencesFromResource(R.xml.preferences);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_preferences);
		
		getFragmentManager().beginTransaction()
				.replace(android.R.id.content, new MyPreferenceFragment())
				.commit();
		
//        if (hasHeaders()) {
//            Button button = new Button(this);
//            button.setText("Some action");
//            setListFooter(button);
//        }

	}
}