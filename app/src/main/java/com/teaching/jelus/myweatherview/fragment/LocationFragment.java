package com.teaching.jelus.myweatherview.fragment;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.teaching.jelus.myweatherview.DataEvent;
import com.teaching.jelus.myweatherview.MessageType;
import com.teaching.jelus.myweatherview.MyApp;
import com.teaching.jelus.myweatherview.R;
import com.teaching.jelus.myweatherview.Settings;

import org.greenrobot.eventbus.EventBus;

public class LocationFragment extends Fragment {
    public static final String TAG = LocationFragment.class.getSimpleName();
    private EditText mCityNameEdit;
    private CheckBox mLocateCheck;
    private Button mCityConfirmBtn;
    private Settings mSettings;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_location, container, false);
        mCityNameEdit = (EditText) view.findViewById(R.id.edit_city_name);
        mLocateCheck = (CheckBox) view.findViewById(R.id.check_locate);
        mCityConfirmBtn = (Button) view.findViewById(R.id.btn_city_confirm);
        mSettings = MyApp.getSettings();
        checkLocationWidgetEnable();
        mCityConfirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeCityNameAndUpdateData();
            }
        });
        mCityNameEdit.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)){
                    changeCityNameAndUpdateData();
                    return true;
                }
                return false;
            }
        });
        mCityNameEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkLocationWidgetEnable();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        return view;
    }

    private void changeCityNameAndUpdateData() {
        trimCityName();
        if (!isCityNameNull()){
            String cityName = mCityNameEdit.getText().toString();
            mSettings.setCityNameValue(cityName);
            EventBus.getDefault().post(new DataEvent(MessageType.ALL_DATA_UPDATE, null));
            hideKeyboard();
        }
    }

    private boolean isCityNameNull() {
        String cityName = mCityNameEdit.getText().toString();
        return cityName.equals("");
    }

    private void trimCityName() {
        String trimmedCityName = mCityNameEdit.getText().toString().trim();
        mCityNameEdit.setText(trimmedCityName);
        mCityNameEdit.setSelection(mCityNameEdit.getText().length());
    }

    @Override
    public void onResume() {
        super.onResume();
        mCityNameEdit.setText(mSettings.getPreferCityNameValue());
        mLocateCheck.setChecked(mSettings.getLocateValue());
    }

    @Override
    public void onPause() {
        saveSettings();
        super.onPause();
    }

    private void saveSettings(){
        if (!mCityNameEdit.getText().toString().equals("")){
            mSettings.setLocateValue(mLocateCheck.isChecked());
        } else {
            mSettings.setLocateValue(false);
        }
        if (mLocateCheck.isChecked()) {
            mSettings.setPreferCityNameValue(mCityNameEdit.getText().toString());
        } else {
            mSettings.removePreferCityNameValue();
        }
    }

    private void checkLocationWidgetEnable(){
        if (mCityNameEdit.getText().toString().equals("")){
            mLocateCheck.setVisibility(View.GONE);
            mLocateCheck.setChecked(false);
            mCityConfirmBtn.setVisibility(View.GONE);
        } else {
            mLocateCheck.setVisibility(View.VISIBLE);
            mCityConfirmBtn.setVisibility(View.VISIBLE);
        }
    }

    private void hideKeyboard() {
        InputMethodManager inputManager =
                (InputMethodManager) getActivity().
                        getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(
                getActivity().getCurrentFocus().getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);
    }
}
