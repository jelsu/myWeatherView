package com.teaching.jelus.myweatherview.fragments;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;

import com.teaching.jelus.myweatherview.DataEvent;
import com.teaching.jelus.myweatherview.R;

import org.greenrobot.eventbus.EventBus;

public class LocationFragment extends Fragment {
    private final String CITY_NAME = "city_name";
    private final String LOCATE = "locate";
    private EditText mCityNameEdit;
    private CheckBox mLocateCheck;
    private SharedPreferences mPreferences;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_location, container, false);
        mCityNameEdit = (EditText) view.findViewById(R.id.edit_city_name);
        mLocateCheck = (CheckBox) view.findViewById(R.id.check_locate);
        mPreferences = getActivity().getSharedPreferences("preferences", Context.MODE_PRIVATE);
        mCityNameEdit.setText(mPreferences.getString(CITY_NAME, ""));
        mLocateCheck.setChecked(mPreferences.getBoolean(LOCATE, false));
        checkLocationEnable();
        mCityNameEdit.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)){
                    String trimmedCityName = mCityNameEdit.getText().toString().trim();
                    mCityNameEdit.setText(trimmedCityName);
                    mCityNameEdit.setSelection(mCityNameEdit.getText().length());
                    if (!trimmedCityName.equals("")){
                        savePreferences();
                        EventBus.getDefault().post(new DataEvent("Update request",
                                trimmedCityName));
                    }
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
                checkLocationEnable();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        return view;
    }

    @Override
    public void onDestroyView() {
        savePreferences();
        super.onDestroyView();
    }

    private void savePreferences(){
        mPreferences.edit().clear().apply();
        if (!mCityNameEdit.getText().toString().equals("")){
            mPreferences.edit().putBoolean(LOCATE, mLocateCheck.isChecked()).apply();
        } else {
            mPreferences.edit().putBoolean(LOCATE, false).apply();
        }
        if (mLocateCheck.isChecked()) {
            mPreferences.edit().putString(CITY_NAME, mCityNameEdit.getText().toString()).apply();
        } else {
            mPreferences.edit().putString(CITY_NAME, "").apply();
        }
    }

    private void checkLocationEnable(){
        if (mCityNameEdit.getText().toString().equals("")){
            mLocateCheck.setVisibility(View.GONE);
            mLocateCheck.setChecked(false);
        } else {
            mLocateCheck.setVisibility(View.VISIBLE);
        }
    }
}
