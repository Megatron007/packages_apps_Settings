/*
 * Copyright (C) 2013 SlimRoms Project
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

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.graphics.Color;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.os.UserHandle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceGroup;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.text.Spannable;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;

import com.android.settings.Utils;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

import java.util.ArrayList;
import java.util.List;

import com.android.internal.logging.MetricsLogger;
import com.android.internal.util.omni.PackageUtils;
import com.android.settings.widget.SeekBarPreferenceCham;


public class StatusBar extends SettingsPreferenceFragment implements OnPreferenceChangeListener, Indexable {

    private static final String TAG = "StatusBarSettings";

    private static final String CUSTOM_HEADER_IMAGE_SHADOW = "status_bar_custom_header_shadow";
    private static final String STATUS_BAR_TEMPERATURE = "status_bar_temperature";
    private static final String STATUS_BAR_TEMPERATURE_STYLE = "status_bar_temperature_style";
    private static final String ENABLE_TASK_MANAGER = "enable_task_manager";

    private static final String CATEGORY_WEATHER = "statusbar_weather_category";
    private static final String WEATHER_ICON_PACK = "weather_icon_pack";
    private static final String DEFAULT_WEATHER_ICON_PACKAGE = "org.omnirom.omnijaws";
    private static final String WEATHER_SERVICE_PACKAGE = "org.omnirom.omnijaws";
    private static final String CHRONUS_ICON_PACK_INTENT = "com.dvtonder.chronus.ICON_PACK";
    private static final String LOCK_CLOCK_PACKAGE="com.cyanogenmod.lockclock";

    private SeekBarPreferenceCham mHeaderShadow;
    private ListPreference mStatusBarTemperature;
    private ListPreference mStatusBarTemperatureStyle;
    private SwitchPreference mEnableTaskManager;
    private PreferenceCategory mWeatherCategory;
    private ListPreference mWeatherIconPack;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.screwd_statusbar_settings);

        PreferenceScreen prefSet = getPreferenceScreen();

	ContentResolver resolver = getActivity().getContentResolver();

        PackageManager pm = getPackageManager();

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        PreferenceScreen prefSet = getPreferenceScreen();
        ContentResolver resolver = getActivity().getContentResolver();

        // Statusbar temp
        mStatusBarTemperature = (ListPreference) findPreference(STATUS_BAR_TEMPERATURE);
        int temperatureShow = Settings.System.getIntForUser(resolver,
                Settings.System.STATUS_BAR_SHOW_WEATHER_TEMP, 0,
                UserHandle.USER_CURRENT);
        mStatusBarTemperature.setValue(String.valueOf(temperatureShow));
        mStatusBarTemperature.setSummary(mStatusBarTemperature.getEntry());
        mStatusBarTemperature.setOnPreferenceChangeListener(this);

        mStatusBarTemperatureStyle = (ListPreference) findPreference(STATUS_BAR_TEMPERATURE_STYLE);
        int temperatureStyle = Settings.System.getIntForUser(resolver,
                Settings.System.STATUS_BAR_WEATHER_TEMP_STYLE, 0,
                UserHandle.USER_CURRENT);
        mStatusBarTemperatureStyle.setValue(String.valueOf(temperatureStyle));
        mStatusBarTemperatureStyle.setSummary(mStatusBarTemperatureStyle.getEntry());
        mStatusBarTemperatureStyle.setOnPreferenceChangeListener(this);

        enableStatusBarTemperatureDependents();

	mEnableTaskManager = (SwitchPreference) findPreference(ENABLE_TASK_MANAGER);
        mEnableTaskManager.setChecked((Settings.System.getInt(resolver,
                Settings.System.ENABLE_TASK_MANAGER, 0) == 1));

	mHeaderShadow = (SeekBarPreferenceCham) findPreference(CUSTOM_HEADER_IMAGE_SHADOW);
        int headerShadow = Settings.System.getInt(resolver,
                Settings.System.STATUS_BAR_CUSTOM_HEADER_SHADOW, 0);
        mHeaderShadow.setValue(headerShadow);
        mHeaderShadow.setOnPreferenceChangeListener(this);

	mWeatherCategory = (PreferenceCategory) prefSet.findPreference(CATEGORY_WEATHER);
        if (mWeatherCategory != null && !isOmniJawsServiceInstalled()) {
            prefSet.removePreference(mWeatherCategory);
        } else {
            String settingHeaderPackage = Settings.System.getString(getContentResolver(),
                    Settings.System.STATUS_BAR_WEATHER_ICON_PACK);
            if (settingHeaderPackage == null) {
                settingHeaderPackage = DEFAULT_WEATHER_ICON_PACKAGE;
            }
            mWeatherIconPack = (ListPreference) findPreference(WEATHER_ICON_PACK);
            List<String> entrieswi = new ArrayList<String>();
            List<String> valueswi = new ArrayList<String>();
            getAvailableWeatherIconPacks(entrieswi, valueswi);
            mWeatherIconPack.setEntries(entrieswi.toArray(new String[entrieswi.size()]));
            mWeatherIconPack.setEntryValues(valueswi.toArray(new String[valueswi.size()]));

            int valueIndex = mWeatherIconPack.findIndexOfValue(settingHeaderPackage);
            if (valueIndex == -1) {
                // no longer found
                settingHeaderPackage = DEFAULT_WEATHER_ICON_PACKAGE;
                Settings.System.putString(getContentResolver(),
                        Settings.System.STATUS_BAR_WEATHER_ICON_PACK, settingHeaderPackage);
                valueIndex = mWeatherIconPack.findIndexOfValue(settingHeaderPackage);
            }
            mWeatherIconPack.setValueIndex(valueIndex >= 0 ? valueIndex : 0);
            mWeatherIconPack.setSummary(mWeatherIconPack.getEntry());
            mWeatherIconPack.setOnPreferenceChangeListener(this);
        }

    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
		ContentResolver resolver = getActivity().getContentResolver();
		if (preference == mHeaderShadow) {
         int headerShadow = (Integer) newValue;
         Settings.System.putInt(getActivity().getContentResolver(),
                 Settings.System.STATUS_BAR_CUSTOM_HEADER_SHADOW, headerShadow);
        } else if (preference == mStatusBarTemperature) {
            int temperatureShow = Integer.valueOf((String) newValue);
            int index = mStatusBarTemperature.findIndexOfValue((String) newValue);
            Settings.System.putIntForUser(
                    resolver, Settings.System.STATUS_BAR_SHOW_WEATHER_TEMP, temperatureShow,
                    UserHandle.USER_CURRENT);
            mStatusBarTemperature.setSummary(
                    mStatusBarTemperature.getEntries()[index]);
            enableStatusBarTemperatureDependents();
        } else if (preference == mStatusBarTemperatureStyle) {
            int temperatureStyle = Integer.valueOf((String) newValue);
            int index = mStatusBarTemperatureStyle.findIndexOfValue((String) newValue);
            Settings.System.putIntForUser(
                    resolver, Settings.System.STATUS_BAR_WEATHER_TEMP_STYLE, temperatureStyle,
                    UserHandle.USER_CURRENT);
            mStatusBarTemperatureStyle.setSummary(
                    mStatusBarTemperatureStyle.getEntries()[index]);
		} else if (preference == mWeatherIconPack) {
            String value = (String) newValue;
            Settings.System.putString(getContentResolver(),
                    Settings.System.STATUS_BAR_WEATHER_ICON_PACK, value);
            int valueIndex = mWeatherIconPack.findIndexOfValue(value);
            mWeatherIconPack.setSummary(mWeatherIconPack.getEntries()[valueIndex]);
        }
        return true;
    }

	@Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
		if  (preference == mEnableTaskManager) {
            boolean checked = ((SwitchPreference)preference).isChecked();
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.ENABLE_TASK_MANAGER, checked ? 1:0);
            return true;
		}

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }


    @Override
    public void onResume() {
        super.onResume();
    }

	private boolean isOmniJawsServiceInstalled() {
        return PackageUtils.isAvailableApp(WEATHER_SERVICE_PACKAGE, getActivity());
    }

    private boolean isLockClockInstalled() {
        return PackageUtils.isAvailableApp(LOCK_CLOCK_PACKAGE, getActivity());
    }

    private String[] getAvailableWeatherIconPacksValues() {
        List<String> headerPacks = new ArrayList<String>();
        Intent i = new Intent();
        PackageManager packageManager = getPackageManager();
        i.setAction("org.omnirom.WeatherIconPack");
        for (ResolveInfo r : packageManager.queryIntentActivities(i, 0)) {
            String packageName = r.activityInfo.packageName;
            if (packageName.equals(DEFAULT_WEATHER_ICON_PACKAGE)) {
                headerPacks.add(0, r.activityInfo.name);
            } else {
                headerPacks.add(r.activityInfo.name);
            }
        }
        if (isLockClockInstalled()) {
            headerPacks.add(LOCK_CLOCK_PACKAGE + ".weather");
            headerPacks.add(LOCK_CLOCK_PACKAGE + ".weather_color");
            headerPacks.add(LOCK_CLOCK_PACKAGE + ".weather_vclouds");
        }
        return headerPacks.toArray(new String[headerPacks.size()]);
    }

    private void getAvailableWeatherIconPacks(List<String> entrieswi, List<String> valueswi) {
        Intent i = new Intent();
        PackageManager packageManager = getPackageManager();
        i.setAction("org.omnirom.WeatherIconPack");
        for (ResolveInfo r : packageManager.queryIntentActivities(i, 0)) {
            String packageName = r.activityInfo.packageName;
            if (packageName.equals(DEFAULT_WEATHER_ICON_PACKAGE)) {
                valueswi.add(0, r.activityInfo.name);
            } else {
                valueswi.add(r.activityInfo.name);
            }
            String label = r.activityInfo.loadLabel(getPackageManager()).toString();
            if (label == null) {
                label = r.activityInfo.packageName;
            }
            if (packageName.equals(DEFAULT_WEATHER_ICON_PACKAGE)) {
                entrieswi.add(0, label);
            } else {
                entrieswi.add(label);
            }
        }
        i = new Intent(Intent.ACTION_MAIN);
        i.addCategory(CHRONUS_ICON_PACK_INTENT);
        for (ResolveInfo r : packageManager.queryIntentActivities(i, 0)) {
            String packageName = r.activityInfo.packageName;
            valueswi.add(packageName + ".weather");
            String label = r.activityInfo.loadLabel(getPackageManager()).toString();
            if (label == null) {
                label = r.activityInfo.packageName;
            }
            entrieswi.add(label);
        }
        if (isLockClockInstalled()) {
            valueswi.add(LOCK_CLOCK_PACKAGE + ".weather");
            valueswi.add(LOCK_CLOCK_PACKAGE + ".weather_color");
            valueswi.add(LOCK_CLOCK_PACKAGE + ".weather_vclouds");

            entrieswi.add("LockClock (white)");
            entrieswi.add("LockClock (color)");
            entrieswi.add("LockClock (vclouds)");
        }
    }

    public static final Indexable.SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
        new BaseSearchIndexProvider() {
        @Override
        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context,
                                                                    boolean enabled) {
            ArrayList<SearchIndexableResource> result =
                new ArrayList<SearchIndexableResource>();

            SearchIndexableResource sir = new SearchIndexableResource(context);
            sir.xmlResId = R.xml.screwd_statusbar_settings;
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

	private void enableStatusBarTemperatureDependents() {
        int temperatureShow = Settings.System.getIntForUser(getActivity()
                .getContentResolver(), Settings.System.STATUS_BAR_SHOW_WEATHER_TEMP, 0,
                UserHandle.USER_CURRENT);
        if (temperatureShow == 0) {
            mStatusBarTemperatureStyle.setEnabled(false);
        } else {
            mStatusBarTemperatureStyle.setEnabled(true);
        }
    }

}
