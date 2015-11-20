/*

 * Copyright (C) 2014 Screw'd Android
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.screwd;

import android.provider.SearchIndexableResource;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.SystemProperties;
import android.provider.Settings;
import android.preference.ListPreference;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.preference.PreferenceCategory;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.SwitchPreference;

import android.text.TextUtils;
import android.widget.Toast;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

import java.io.File;
import java.io.IOException;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.List;

import com.android.internal.logging.MetricsLogger;

public class Misc extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener, Indexable {

	private static final String KEY_LOCK_CLOCK = "lock_clock";
    private static final String KEY_LOCK_CLOCK_PACKAGE_NAME = "com.cyanogenmod.lockclock";
    private static final String PREF_MEDIA_SCANNER_ON_BOOT = "media_scanner_on_boot";

	private PreferenceScreen mLockClock;
    private ListPreference mMsob;

	private final ArrayList<Preference> mAllPrefs = new ArrayList<Preference>();

	private final ArrayList<SwitchPreference> mResetSbPrefs
            = new ArrayList<SwitchPreference>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.screwd_misc_settings);

		Activity activity = getActivity();

		ContentResolver resolver = getActivity().getContentResolver();
		PreferenceScreen prefSet = getPreferenceScreen();

		mLockClock = (PreferenceScreen) findPreference(KEY_LOCK_CLOCK);
        if (!Utils.isPackageInstalled(getActivity(), KEY_LOCK_CLOCK_PACKAGE_NAME)) {
            prefSet.removePreference(mLockClock);
        }

        mMsob = (ListPreference) findPreference(PREF_MEDIA_SCANNER_ON_BOOT);
        mMsob.setValue(String.valueOf(Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.MEDIA_SCANNER_ON_BOOT, 0)));
        mMsob.setSummary(mMsob.getEntry());
        mMsob.setOnPreferenceChangeListener(this);

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
		if (preference == mMsob) {
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.MEDIA_SCANNER_ON_BOOT,
                    Integer.valueOf(String.valueOf(newValue)));

            mMsob.setValue(String.valueOf(newValue));
            mMsob.setSummary(mMsob.getEntry());
            return true;
        }

        return false;
    }



	public static final Indexable.SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
        new BaseSearchIndexProvider() {
        @Override
        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context,
                                                                    boolean enabled) {
            ArrayList<SearchIndexableResource> result =
                new ArrayList<SearchIndexableResource>();

            SearchIndexableResource sir = new SearchIndexableResource(context);
            sir.xmlResId = R.xml.screwd_misc_settings;
            result.add(sir);

            return result;
        }

        @Override
        public List<String> getNonIndexableKeys(Context context) {
            ArrayList<String> result = new ArrayList<String>();
            return result;
        }
    };

	@Override
    protected int getMetricsCategory() {
       return MetricsLogger.APPLICATION;
    }

}
