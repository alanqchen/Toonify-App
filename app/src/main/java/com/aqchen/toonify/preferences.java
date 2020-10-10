package com.aqchen.toonify;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.fragment.app.FragmentActivity;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.appcompat.widget.Toolbar;

import android.transition.Transition;
import android.transition.TransitionInflater;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;


public class preferences extends AppCompatActivity {

    private static String manipulationType;
    private Button btnCancel;
    private Button btnApply;

    private static int seekBarVal1;
    private static int seekBarVal2;
    private static int seekBarVal3;
    private static int seekBarVal4;
    private static int seekBarVal5;
    private static int seekBarVal6;

    com.pavelsikun.seekbarpreference.SeekBarPreferenceCompat numDownSamples;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            setTheme(R.style.PreferencesDarkTheme);
        } else {
            setTheme(R.style.PreferencesTheme);
        }

        super.onCreate(savedInstanceState);
        setTitle("Preferences");
        setContentView(R.layout.activity_preferences);

        Bundle bundle = getIntent().getExtras();

        // or other values
        if(bundle != null) {
            manipulationType = bundle.getString("manipulationType");
            seekBarVal1 = bundle.getInt("seekBarVal1", 7);
            seekBarVal2 = bundle.getInt("seekBarVal2", 9);
            seekBarVal3 = bundle.getInt("seekBarVal3", 9);
            seekBarVal4 = bundle.getInt("seekBarVal4", 7);
            seekBarVal5 = bundle.getInt("seekBarVal5", 7);
            seekBarVal6 = bundle.getInt("seekBarVal6", 9);
        } else {
            finish();
            return;
        }

        Toolbar toolbar = findViewById(R.id.toolbarPreferences);
        toolbar.setTitleTextColor(0xFFFFFFFF);
        //ContextCompat.getDrawable(this, R.drawable.abc_ic_ab_back_material).setColorFilter(getResources().getColor(R.color.colorWhite), PorterDuff.Mode.SRC_ATOP);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // Display the fragment as the main content.


        btnCancel = (Button)findViewById(R.id.btnCancel);
        btnApply = (Button)findViewById(R.id.btnApply);

        btnCancel.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                SharedPreferences sharedPref = getSharedPreferences("settings", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putBoolean("applied", false);
                editor.commit();
                finish();
            }
        });

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.content_view, new PrefsFragment()).commit();
        /*
        btnApply.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                finish();
            }
        });
        */
    }
    /*
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.preference_menu, menu);
        return true;
    }
    */
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuCancel:
                finish();
                return true;
            case R.id.menuApply:
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public static class PrefsFragment extends PreferenceFragmentCompat {

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            // Load the preferences from an XML resource
            Context activityContext = getActivity();

            PreferenceScreen preferenceScreen = getPreferenceManager().createPreferenceScreen(activityContext);
            setPreferenceScreen(preferenceScreen);

            TypedValue themeTypedValue = new TypedValue();
            activityContext.getTheme().resolveAttribute(R.attr.preferenceTheme, themeTypedValue, true);
            ContextThemeWrapper contextThemeWrapper = new ContextThemeWrapper(activityContext, themeTypedValue.resourceId);

            // We instance each Preference using our ContextThemeWrapper object
            PreferenceCategory preferenceCategory = new PreferenceCategory(contextThemeWrapper);
            preferenceCategory.setTitle("Category test");


            final com.pavelsikun.seekbarpreference.SeekBarPreferenceCompat seekBar1;
            final com.pavelsikun.seekbarpreference.SeekBarPreferenceCompat seekBar2;
            final com.pavelsikun.seekbarpreference.SeekBarPreferenceCompat seekBar3;
            //final com.pavelsikun.seekbarpreference.SeekBarPreferenceCompat seekBar4;
            final com.pavelsikun.seekbarpreference.SeekBarPreferenceCompat seekBar5;
            final com.pavelsikun.seekbarpreference.SeekBarPreferenceCompat seekBar6;
            //final com.pavelsikun.seekbarpreference.SeekBarPreferenceCompat seekBar7;


            getActivity().setContentView(R.layout.activity_preferences);
            Toolbar toolbar = getActivity().findViewById(R.id.toolbarPreferences);
            toolbar.setTitleTextColor(0xFFFFFFFF);
            ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);

            if ( ((AppCompatActivity)getActivity()).getSupportActionBar() != null){
                ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
            }

            Button btnApply = ((FragmentActivity) activityContext).findViewById(R.id.btnApply);

            switch (manipulationType) {
                case "cartoon":

                    seekBar1 = new com.pavelsikun.seekbarpreference.SeekBarPreferenceCompat(contextThemeWrapper);
                    seekBar1.setMinValue(1);
                    seekBar1.setMaxValue(21);
                    seekBar1.setCurrentValue(seekBarVal1);
                    seekBar1.setTitle("Bilateral Filter Pass Amount");
                    seekBar1.setSummary("Intensity of color smoothing (major)");

                    seekBar2 = new com.pavelsikun.seekbarpreference.SeekBarPreferenceCompat(contextThemeWrapper);
                    seekBar2.setMinValue(3);
                    seekBar2.setMaxValue(23);
                    seekBar2.setCurrentValue(seekBarVal2);
                    seekBar2.setTitle("Bilateral Filter Width");
                    seekBar2.setSummary("Intensity of color smoothing (major)");

                    seekBar3 = new com.pavelsikun.seekbarpreference.SeekBarPreferenceCompat(contextThemeWrapper);
                    seekBar3.setMinValue(1);
                    seekBar3.setMaxValue(21);
                    seekBar3.setCurrentValue(seekBarVal3);
                    seekBar3.setTitle("Sigma Color");
                    seekBar3.setSummary("Intensity of color smoothing (fine)");

                    /*
                    seekBar4 = new com.pavelsikun.seekbarpreference.SeekBarPreferenceCompat(contextThemeWrapper);
                    seekBar4.setMinValue(1);
                    seekBar4.setMaxValue(21);
                    seekBar4.setCurrentValue(seekBarVal4);
                    seekBar4.setTitle("Sigma Space");
                     */

                    seekBar5 = new com.pavelsikun.seekbarpreference.SeekBarPreferenceCompat(contextThemeWrapper);
                    seekBar5.setMinValue(3);
                    seekBar5.setMaxValue(23);
                    seekBar5.setCurrentValue(seekBarVal5);
                    seekBar5.setTitle("Median Filter Width");
                    seekBar5.setSummary("Reduces the amount of outlines");

                    seekBar6 = new com.pavelsikun.seekbarpreference.SeekBarPreferenceCompat(contextThemeWrapper);
                    seekBar6.setMinValue(3);
                    seekBar6.setMaxValue(23);
                    seekBar6.setCurrentValue(seekBarVal6);
                    seekBar6.setTitle("Edge Filter Width");
                    seekBar6.setSummary("Width of outlines");

                    getPreferenceScreen().addPreference(preferenceCategory);



                    preferenceCategory.addPreference((Preference)seekBar1);
                    preferenceCategory.addPreference((Preference)seekBar2);
                    preferenceCategory.addPreference((Preference)seekBar3);
                    //preferenceCategory.addPreference((Preference)seekBar4);
                    preferenceCategory.addPreference((Preference)seekBar5);
                    preferenceCategory.addPreference((Preference)seekBar6);

                    btnApply.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {
                            int numBilateral = seekBar1.getCurrentValue();
                            int bDiameter = seekBar2.getCurrentValue();
                            int sigmaColor = seekBar3.getCurrentValue();
                            //int sigmaSpace = seekBar4.getCurrentValue();
                            int sigmaSpace = seekBarVal4;
                            int mDiameter = seekBar5.getCurrentValue();
                            int eDiameter = seekBar6.getCurrentValue();
                            /*
                            if(bDiameter % 2 != 1 || mDiameter % 2 != 1 || eDiameter % 2 != 1) {
                                Toast.makeText(getActivity(), "Diameter Not Odd!", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            */
                            if(bDiameter % 2 != 1) {
                                bDiameter++;
                            }
                            if(mDiameter % 2 != 1) {
                                mDiameter++;
                            }
                            if(eDiameter % 2 != 1) {
                                eDiameter++;
                            }

                            SharedPreferences sharedPref = getActivity().getSharedPreferences("settings", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPref.edit();

                            editor.putInt("seekBar1", numBilateral);
                            editor.putInt("seekBar2", bDiameter);
                            editor.putInt("seekBar3", sigmaColor);
                            editor.putInt("seekBar4", sigmaSpace);
                            editor.putInt("seekBar5", mDiameter);
                            editor.putInt("seekBar6", eDiameter);
                            editor.putBoolean("applied", true);
                            editor.apply();

                            getActivity().finish();
                        }
                    });
                    break;

                case "pencilColor":
                case "pencilBW":
                    seekBar1 = new com.pavelsikun.seekbarpreference.SeekBarPreferenceCompat(contextThemeWrapper);
                    seekBar1.setMinValue(5);
                    seekBar1.setMaxValue(100);
                    seekBar1.setInterval(5);
                    seekBar1.setCurrentValue(seekBarVal1);
                    seekBar1.setTitle("Sigma Space");
                    seekBar1.setSummary("Size of neighborhood");

                    seekBar2 = new com.pavelsikun.seekbarpreference.SeekBarPreferenceCompat(contextThemeWrapper);
                    seekBar2.setMinValue(1);
                    seekBar2.setMaxValue(80);
                    seekBar2.setCurrentValue(seekBarVal2);
                    seekBar2.setTitle("Sigma Color");
                    seekBar2.setSummary("How much colors are smoothed");

                    seekBar3 = new com.pavelsikun.seekbarpreference.SeekBarPreferenceCompat(contextThemeWrapper);
                    seekBar3.setMinValue(1);
                    seekBar3.setMaxValue(40);
                    seekBar3.setCurrentValue(seekBarVal3);
                    seekBar3.setTitle("Shade Factor");
                    seekBar3.setSummary("Intensity of output");

                    getPreferenceScreen().addPreference(preferenceCategory);

                    preferenceCategory.addPreference((Preference)seekBar1);
                    preferenceCategory.addPreference((Preference)seekBar2);
                    preferenceCategory.addPreference((Preference)seekBar3);

                    btnApply.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {
                            int sigmaSpace = seekBar1.getCurrentValue();
                            int sigmaColor = seekBar2.getCurrentValue();
                            int shadeFactor = seekBar3.getCurrentValue();


                            SharedPreferences sharedPref = getActivity().getSharedPreferences("settings", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPref.edit();

                            editor.putInt("seekBar1", sigmaSpace);
                            editor.putInt("seekBar2", sigmaColor);
                            editor.putInt("seekBar3", shadeFactor);
                            editor.putBoolean("applied", true);
                            editor.apply();

                            getActivity().finish();
                        }
                    });
                    break;

                case "watercolor":
                    seekBar1 = new com.pavelsikun.seekbarpreference.SeekBarPreferenceCompat(contextThemeWrapper);
                    seekBar1.setMinValue(5);
                    seekBar1.setMaxValue(100);
                    seekBar1.setInterval(5);
                    seekBar1.setCurrentValue(seekBarVal1);
                    seekBar1.setTitle("Sigma Space");
                    seekBar1.setSummary("Size of neighborhood");

                    seekBar2 = new com.pavelsikun.seekbarpreference.SeekBarPreferenceCompat(contextThemeWrapper);
                    seekBar2.setMinValue(1);
                    seekBar2.setMaxValue(80);
                    seekBar2.setCurrentValue(seekBarVal2);
                    seekBar2.setTitle("Sigma Color");
                    seekBar2.setSummary("How much colors are smoothed");

                    getPreferenceScreen().addPreference(preferenceCategory);

                    preferenceCategory.addPreference((Preference)seekBar1);
                    preferenceCategory.addPreference((Preference)seekBar2);

                    btnApply.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {
                            int sigmaSpace = seekBar1.getCurrentValue();
                            int sigmaColor = seekBar2.getCurrentValue();

                            SharedPreferences sharedPref = getActivity().getSharedPreferences("settings", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPref.edit();

                            editor.putInt("seekBar1", sigmaSpace);
                            editor.putInt("seekBar2", sigmaColor);
                            editor.putBoolean("applied", true);
                            editor.apply();

                            getActivity().finish();
                        }
                    });
                    break;

                default:
                    break;
            }

            Button btnCancel = ((FragmentActivity) activityContext).findViewById(R.id.btnCancel);
            btnCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    getActivity().finish();
                }
            });
            /*
            Button btnApply = ((FragmentActivity) activityContext).findViewById(R.id.btnApply);
            btnApply.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    switch (manipulationType) {
                        case "cartoon":
                            if(seekBar1 != null)
                                seekBar1.getCurrentValue();
                            getActivity().finish();
                            break;
                    }
                }
            });
            */
            /*
            com.pavelsikun.seekbarpreference.SeekBarPreferenceCompat seekBarPreference = new com.pavelsikun.seekbarpreference.SeekBarPreferenceCompat(contextThemeWrapper);
            seekBarPreference.setMinValue(1);
            seekBarPreference.setMaxValue(10);
            seekBarPreference.setCurrentValue(5);

            */

            // It's REALLY IMPORTANT to add Preferences with child Preferences to the Preference Hierarchy first
            // Otherwise, the PreferenceManager will fail to load their keys
            /*
            // First we add the category to the root PreferenceScreen
            getPreferenceScreen().addPreference(preferenceCategory);

            // Then their child to it
            preferenceCategory.addPreference((Preference)seekBarPreference);
            //preferenceCategory.addPreference(checkBoxPreference);
            */
        }
    }

/*
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.preview_menu, menu);
        return true;
    }

*/
}