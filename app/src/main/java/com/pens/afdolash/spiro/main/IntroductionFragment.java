package com.pens.afdolash.spiro.main;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.pens.afdolash.spiro.R;

import java.util.Iterator;

import static com.pens.afdolash.spiro.main.MainActivity.EXTRA_USER_AGE;
import static com.pens.afdolash.spiro.main.MainActivity.EXTRA_USER_GENDER;
import static com.pens.afdolash.spiro.main.MainActivity.EXTRA_USER_GENDER_ID;
import static com.pens.afdolash.spiro.main.MainActivity.EXTRA_USER_HEIGHT;
import static com.pens.afdolash.spiro.main.MainActivity.EXTRA_USER_NAME;
import static com.pens.afdolash.spiro.main.MainActivity.EXTRA_USER_WEIGHT;
import static com.pens.afdolash.spiro.main.MainActivity.USER_PREF;


/**
 * A simple {@link Fragment} subclass.
 */
public class IntroductionFragment extends Fragment {
    // Shared preference
    private SharedPreferences preferences;

    // View
    private EditText etName, etAge, etHeight, etWeight;
    private RadioGroup radGender;
    private RadioButton radButton;


    public IntroductionFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_introduction, container, false);

        preferences = getContext().getSharedPreferences(USER_PREF, Context.MODE_PRIVATE);

        // Initialization view
        etName = (EditText) view.findViewById(R.id.et_name);
        etAge = (EditText) view.findViewById(R.id.et_age);
        etHeight = (EditText) view.findViewById(R.id.et_height);
        etWeight = (EditText) view.findViewById(R.id.et_weight);
        radGender = (RadioGroup) view.findViewById(R.id.rad_gender);

        // Get data from Shared preference
        String name = preferences.getString(EXTRA_USER_NAME, null);
        String gender = preferences.getString(EXTRA_USER_GENDER, null);
        int genderId = preferences.getInt(EXTRA_USER_GENDER_ID, 0);
        int age = preferences.getInt(EXTRA_USER_AGE, 0);
        int weight = preferences.getInt(EXTRA_USER_WEIGHT, 0);
        int height = preferences.getInt(EXTRA_USER_HEIGHT, 0);

        // Checking
        if (name != null && gender != null && genderId != 0 && age != 0 && weight != 0 && height != 0) {
            etName.setText(name);
            etAge.setText(String.valueOf(age));
            etWeight.setText(String.valueOf(weight));
            etHeight.setText(String.valueOf(height));
        }

        return view;
    }


    /**
     * Setter and Getter
     */
    public EditText getEtName() {
        return etName;
    }

    public EditText getEtAge() {
        return etAge;
    }

    public EditText getEtHeight() {
        return etHeight;
    }

    public EditText getEtWeight() {
        return etWeight;
    }

    public RadioGroup getRadGender() {
        return radGender;
    }

    public RadioButton getRadButton() {
        return radButton;
    }

    public String getUserGender(int selectedId) {
        radButton = (RadioButton) getView().findViewById(selectedId);
        String gender = radButton.getText().toString();
        return gender;
    }
}
