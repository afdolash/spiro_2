package com.pens.afdolash.spiro.main;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.pens.afdolash.spiro.R;

import static com.pens.afdolash.spiro.main.MainActivity.EXTRA_USER_LUNGS;
import static com.pens.afdolash.spiro.main.MainActivity.EXTRA_USER_RESULT;
import static com.pens.afdolash.spiro.main.MainActivity.USER_PREF;


/**
 * A simple {@link Fragment} subclass.
 */
public class ResultFragment extends Fragment {
    // Shared prefrence
    private SharedPreferences preferences;

    // View
    private ImageView imgResult;
    private TextView tvResult, tvTitle, tvDescription;

    public ResultFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_result, container, false);

        // Initialization view
        imgResult = (ImageView) view.findViewById(R.id.img_result);
        tvTitle = (TextView) view.findViewById(R.id.tv_title);
        tvDescription = (TextView) view.findViewById(R.id.tv_description);

        return view;
    }

    /**
     * Setter and Getter
     */
    public ImageView getImgResult() {
        return imgResult;
    }

    public void setImgResult(ImageView imgResult) {
        this.imgResult = imgResult;
    }

    public TextView getTvTitle() {
        return tvTitle;
    }

    public void setTvTitle(TextView tvTitle) {
        this.tvTitle = tvTitle;
    }

    public TextView getTvDescription() {
        return tvDescription;
    }

    public void setTvDescription(TextView tvDescription) {
        this.tvDescription = tvDescription;
    }
}
