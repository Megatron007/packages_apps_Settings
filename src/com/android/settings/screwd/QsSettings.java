/*
 * Copyright (C) 2015 The CyanogenMod Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.settings.screwd;

import android.app.ActivityManager;
import android.content.Context;
import android.content.ContentResolver;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;
import android.preference.ListPreference;
import android.preference.SwitchPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.preference.PreferenceCategory;
import android.preference.Preference.OnPreferenceChangeListener;

import com.android.settings.widget.SeekBarPreferenceCham;
import net.margaritov.preference.colorpicker.ColorPickerPreference;

import java.util.ArrayList;
import java.util.List;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.internal.logging.MetricsLogger;

public class QsSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String PREF_QS_TRANSPARENT_SHADE = "qs_transparent_shade";
	private static final String PREF_QS_TRANSPARENT_HEADER = "qs_transparent_header";
	private static final String PREF_TILE_ANIM_STYLE = "qs_tile_animation_style";
    private static final String PREF_TILE_ANIM_DURATION = "qs_tile_animation_duration";
    private static final String PREF_TILE_ANIM_INTERPOLATOR = "qs_tile_animation_interpolator";
    private static final String PREF_QS_STROKE = "qs_stroke";
    private static final String PREF_QS_STROKE_COLOR = "qs_stroke_color";
    private static final String PREF_QS_STROKE_THICKNESS = "qs_stroke_thickness";
    private static final String PREF_QS_CORNER_RADIUS = "qs_corner_radius";
    private static final String PREF_QS_PANEL_LOGO_STYLE = "qs_panel_logo_style";
    private static final String PREF_QS_PANEL_LOGO_ALPHA = "qs_panel_logo_alpha";

    static final int DEFAULT_QS_PANEL_LOGO_COLOR = 0xFF80CBC4;

	private SeekBarPreferenceCham mQSShadeAlpha;
	private SeekBarPreferenceCham mQSHeaderAlpha;
	private ListPreference mNumColumns;
	private ListPreference mTileAnimationStyle;
    private ListPreference mTileAnimationDuration;
	private ListPreference mTileAnimationInterpolator;
    private ListPreference mQSStroke;
    private ColorPickerPreference mQSStrokeColor;
    private SeekBarPreferenceCham mQSStrokeThickness;
    private SeekBarPreferenceCham mQSCornerRadius;
    private ListPreference mQSPanelLogoStyle;
    private SeekBarPreferenceCham mQSPanelLogoAlpha;

	static final int DEFAULT_QS_STROKE_COLOR = 0xFF59007F;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.screwd_qs_settings);

		final ContentResolver resolver = getActivity().getContentResolver();
		PreferenceScreen prefSet = getPreferenceScreen();


		// QS shade alpha
        mQSShadeAlpha =
        (SeekBarPreferenceCham) prefSet.findPreference(PREF_QS_TRANSPARENT_SHADE);
        int qSShadeAlpha = Settings.System.getInt(resolver,
                    Settings.System.QS_TRANSPARENT_SHADE, 255);
        mQSShadeAlpha.setValue(qSShadeAlpha / 1);
        mQSShadeAlpha.setOnPreferenceChangeListener(this);

		// QS header alpha
        mQSHeaderAlpha =
        	(SeekBarPreferenceCham) prefSet.findPreference(PREF_QS_TRANSPARENT_HEADER);
        int qSHeaderAlpha = Settings.System.getInt(resolver,
        	Settings.System.QS_TRANSPARENT_HEADER, 255);
        mQSHeaderAlpha.setValue(qSHeaderAlpha / 1);
        mQSHeaderAlpha.setOnPreferenceChangeListener(this);

		mNumColumns = (ListPreference) findPreference("sysui_qs_num_columns");
        int numColumns = Settings.Secure.getIntForUser(resolver,
                Settings.Secure.QS_NUM_TILE_COLUMNS, getDefaultNumColums(),
                UserHandle.USER_CURRENT);
        mNumColumns.setValue(String.valueOf(numColumns));
        updateNumColumnsSummary(numColumns);
        mNumColumns.setOnPreferenceChangeListener(this);

		mTileAnimationStyle = (ListPreference) findPreference(PREF_TILE_ANIM_STYLE);
        int tileAnimationStyle = Settings.System.getIntForUser(getContentResolver(),
                Settings.System.ANIM_TILE_STYLE, 0,
                UserHandle.USER_CURRENT);
        mTileAnimationStyle.setValue(String.valueOf(tileAnimationStyle));
        updateTileAnimationStyleSummary(tileAnimationStyle);
        updateAnimTileStyle(tileAnimationStyle);
        mTileAnimationStyle.setOnPreferenceChangeListener(this);

        mTileAnimationDuration = (ListPreference) findPreference(PREF_TILE_ANIM_DURATION);
        int tileAnimationDuration = Settings.System.getIntForUser(getContentResolver(),
                Settings.System.ANIM_TILE_DURATION, 2000,
                UserHandle.USER_CURRENT);
        mTileAnimationDuration.setValue(String.valueOf(tileAnimationDuration));
        updateTileAnimationDurationSummary(tileAnimationDuration);
        mTileAnimationDuration.setOnPreferenceChangeListener(this);

		mTileAnimationInterpolator = (ListPreference) findPreference(PREF_TILE_ANIM_INTERPOLATOR);
        int tileAnimationInterpolator = Settings.System.getIntForUser(getContentResolver(),
                Settings.System.ANIM_TILE_INTERPOLATOR, 0,
                UserHandle.USER_CURRENT);
        mTileAnimationInterpolator.setValue(String.valueOf(tileAnimationInterpolator));
        updateTileAnimationInterpolatorSummary(tileAnimationInterpolator);
        mTileAnimationInterpolator.setOnPreferenceChangeListener(this);

		// QS stroke
        mQSStroke =
                (ListPreference) findPreference(PREF_QS_STROKE);
        int qSStroke = Settings.System.getIntForUser(getContentResolver(),
                        Settings.System.QS_STROKE, 1,
                        UserHandle.USER_CURRENT);
        mQSStroke.setValue(String.valueOf(qSStroke));
        mQSStroke.setSummary(mQSStroke.getEntry());
        mQSStroke.setOnPreferenceChangeListener(this);

        // QS stroke color
        mQSStrokeColor =
                (ColorPickerPreference) findPreference(PREF_QS_STROKE_COLOR);
        mQSStrokeColor.setOnPreferenceChangeListener(this);
        int qSIntColor = Settings.System.getInt(getContentResolver(),
                Settings.System.QS_STROKE_COLOR, DEFAULT_QS_STROKE_COLOR);
        String qSHexColor = String.format("#%08x", (0xFF80CBC4 & qSIntColor));
        mQSStrokeColor.setSummary(qSHexColor);
        mQSStrokeColor.setNewPreviewColor(qSIntColor);

        // QS stroke thickness
        mQSStrokeThickness =
                (SeekBarPreferenceCham) findPreference(PREF_QS_STROKE_THICKNESS);
        int qSStrokeThickness = Settings.System.getInt(getContentResolver(),
                Settings.System.QS_STROKE_THICKNESS, 4);
        mQSStrokeThickness.setValue(qSStrokeThickness / 1);
        mQSStrokeThickness.setOnPreferenceChangeListener(this);

        // QS corner radius
        mQSCornerRadius =
                (SeekBarPreferenceCham) findPreference(PREF_QS_CORNER_RADIUS);
        int qSCornerRadius = Settings.System.getInt(getContentResolver(),
                Settings.System.QS_CORNER_RADIUS, 5);
        mQSCornerRadius.setValue(qSCornerRadius / 1);
        mQSCornerRadius.setOnPreferenceChangeListener(this);

        QSSettingsDisabler(qSStroke);

         // QS panel logo style
         mQSPanelLogoStyle =
                  (ListPreference) findPreference(PREF_QS_PANEL_LOGO_STYLE);
         mQSPanelLogoStyle.setOnPreferenceChangeListener(this);
         int qSPanelLogoStyle = Settings.System.getInt(getContentResolver(),
                 Settings.System.QS_PANEL_LOGO_STYLE, 0);
         mQSPanelLogoStyle.setValue(String.valueOf(qSPanelLogoStyle));
         mQSPanelLogoStyle.setSummary(mQSPanelLogoStyle.getEntry());
         mQSPanelLogoStyle.setOnPreferenceChangeListener(this);

         // QS panel logo alpha
         mQSPanelLogoAlpha =
                 (SeekBarPreferenceCham) findPreference(PREF_QS_PANEL_LOGO_ALPHA);
         int qSPanelLogoAlpha = Settings.System.getInt(getContentResolver(),
                 Settings.System.QS_PANEL_LOGO_ALPHA, 51);
         mQSPanelLogoAlpha.setValue(qSPanelLogoAlpha / 1);
         mQSPanelLogoAlpha.setOnPreferenceChangeListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();

    }

	public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getContentResolver();
		if (preference == mQSShadeAlpha) {
                int alpha = (Integer) newValue;
                Settings.System.putInt(resolver,
                        Settings.System.QS_TRANSPARENT_SHADE, alpha * 1);
                return true;
		} else if (preference == mQSHeaderAlpha) {
                int alpha = (Integer) newValue;
                Settings.System.putInt(resolver,
                        Settings.System.QS_TRANSPARENT_HEADER, alpha * 1);
                return true;
		} else if (preference == mNumColumns) {
            int numColumns = Integer.valueOf((String) newValue);
            Settings.Secure.putIntForUser(resolver, Settings.Secure.QS_NUM_TILE_COLUMNS,
                    numColumns, UserHandle.USER_CURRENT);
            updateNumColumnsSummary(numColumns);
            return true;
		} else if (preference == mTileAnimationStyle) {
            int tileAnimationStyle = Integer.valueOf((String) newValue);
            Settings.System.putIntForUser(getContentResolver(), Settings.System.ANIM_TILE_STYLE,
                    tileAnimationStyle, UserHandle.USER_CURRENT);
            updateTileAnimationStyleSummary(tileAnimationStyle);
            updateAnimTileStyle(tileAnimationStyle);
            return true;
        } else if (preference == mTileAnimationDuration) {
            int tileAnimationDuration = Integer.valueOf((String) newValue);
            Settings.System.putIntForUser(getContentResolver(), Settings.System.ANIM_TILE_DURATION,
                    tileAnimationDuration, UserHandle.USER_CURRENT);
            updateTileAnimationDurationSummary(tileAnimationDuration);
            return true;
		} else if (preference == mTileAnimationInterpolator) {
            int tileAnimationInterpolator = Integer.valueOf((String) newValue);
            Settings.System.putIntForUser(getContentResolver(), Settings.System.ANIM_TILE_INTERPOLATOR,
                    tileAnimationInterpolator, UserHandle.USER_CURRENT);
            updateTileAnimationInterpolatorSummary(tileAnimationInterpolator);
            return true;
		} else if (preference == mQSStroke) {
                int qSStroke = Integer.parseInt((String) newValue);
                int index = mQSStroke.findIndexOfValue((String) newValue);
                Settings.System.putIntForUser(getContentResolver(), Settings.System.
                        QS_STROKE, qSStroke, UserHandle.USER_CURRENT);
                mQSStroke.setSummary(mQSStroke.getEntries()[index]);
                QSSettingsDisabler(qSStroke);
                return true;
            } else if (preference == mQSStrokeColor) {
                String hex = ColorPickerPreference.convertToARGB(
                        Integer.valueOf(String.valueOf(newValue)));
                preference.setSummary(hex);
                int intHex = ColorPickerPreference.convertToColorInt(hex);
                Settings.System.putInt(getContentResolver(),
                        Settings.System.QS_STROKE_COLOR, intHex);
                return true;
            } else if (preference == mQSStrokeThickness) {
                int val = (Integer) newValue;
                Settings.System.putInt(getContentResolver(),
                        Settings.System.QS_STROKE_THICKNESS, val * 1);
                return true;
            } else if (preference == mQSCornerRadius) {
                int val = (Integer) newValue;
                Settings.System.putInt(getContentResolver(),
                        Settings.System.QS_CORNER_RADIUS, val * 1);
                return true;
             } else if (preference == mQSPanelLogoStyle) {
                 int qSPanelLogoStyle = Integer.parseInt((String) newValue);
                 int index = mQSPanelLogoStyle.findIndexOfValue((String) newValue);
                 Settings.System.putIntForUser(getContentResolver(), Settings.System.
                         QS_PANEL_LOGO_STYLE, qSPanelLogoStyle, UserHandle.USER_CURRENT);
                 mQSPanelLogoStyle.setSummary(mQSPanelLogoStyle.getEntries()[index]);
                 return true;
             } else if (preference == mQSPanelLogoAlpha) {
                 int val = (Integer) newValue;
                 Settings.System.putInt(getContentResolver(),
                        Settings.System.QS_PANEL_LOGO_ALPHA, val * 1);
                return true;
        }
		return false;
    }

	private void QSSettingsDisabler(int qSStroke) {
            if (qSStroke == 0) {
                mQSStrokeColor.setEnabled(false);
                mQSStrokeThickness.setEnabled(false);
            } else if (qSStroke == 1) {
                mQSStrokeColor.setEnabled(false);
                mQSStrokeThickness.setEnabled(true);
            } else {
                mQSStrokeColor.setEnabled(true);
                mQSStrokeThickness.setEnabled(true);
            }
    }



	private void updateNumColumnsSummary(int numColumns) {
        String prefix = (String) mNumColumns.getEntries()[mNumColumns.findIndexOfValue(String
                .valueOf(numColumns))];
        mNumColumns.setSummary(getActivity().getResources().getString(R.string.qs_num_columns_showing, prefix));
    }

	private void updateTileAnimationStyleSummary(int tileAnimationStyle) {
        String prefix = (String) mTileAnimationStyle.getEntries()[mTileAnimationStyle.findIndexOfValue(String
                .valueOf(tileAnimationStyle))];
        mTileAnimationStyle.setSummary(getResources().getString(R.string.qs_set_animation_style, prefix));
    }

    private void updateTileAnimationDurationSummary(int tileAnimationDuration) {
        String prefix = (String) mTileAnimationDuration.getEntries()[mTileAnimationDuration.findIndexOfValue(String
                .valueOf(tileAnimationDuration))];
        mTileAnimationDuration.setSummary(getResources().getString(R.string.qs_set_animation_duration, prefix));
    }

    private void updateTileAnimationInterpolatorSummary(int tileAnimationInterpolator) {
        String prefix = (String) mTileAnimationInterpolator.getEntries()[mTileAnimationInterpolator.findIndexOfValue(String
                .valueOf(tileAnimationInterpolator))];
        mTileAnimationInterpolator.setSummary(getResources().getString(R.string.qs_set_animation_interpolator, prefix));
    }

    private void updateAnimTileStyle(int tileAnimationStyle) {
        if (mTileAnimationDuration != null) {
            if (tileAnimationStyle == 0) {
                mTileAnimationDuration.setSelectable(false);
                mTileAnimationInterpolator.setSelectable(false);
            } else {
                mTileAnimationDuration.setSelectable(true);
                mTileAnimationInterpolator.setSelectable(true);
            }
        }
    }

    private int getDefaultNumColums() {
        try {
            Resources res = getActivity().getPackageManager()
                    .getResourcesForApplication("com.android.systemui");
            int val = res.getInteger(res.getIdentifier("quick_settings_num_columns", "integer",
                    "com.android.systemui")); // better not be larger than 5, that's as high as the
                                              // list goes atm
            return Math.max(1, val);
        } catch (Exception e) {
            return 3;
        }
    }

    @Override
    protected int getMetricsCategory() {
        return MetricsLogger.SCREWD_SETTINGS;
    }
}
