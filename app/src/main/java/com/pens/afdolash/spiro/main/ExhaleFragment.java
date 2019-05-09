package com.pens.afdolash.spiro.main;


import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.db.chart.model.LineSet;
import com.db.chart.model.Point;
import com.db.chart.renderer.AxisRenderer;
import com.db.chart.util.Tools;
import com.db.chart.view.ChartView;
import com.db.chart.view.LineChartView;
import com.pens.afdolash.spiro.R;

import java.util.List;

import static com.pens.afdolash.spiro.main.MainActivity.EXTRA_USER_LUNGS;
import static com.pens.afdolash.spiro.main.MainActivity.EXTRA_USER_RESULT;
import static com.pens.afdolash.spiro.main.MainActivity.USER_PREF;


/**
 * A simple {@link Fragment} subclass.
 */
public class ExhaleFragment extends Fragment {
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    private boolean exhaleStatus = false;
    private Handler handler = new Handler();
    private LineSet dataset;
    private final String[]  dummyLabel = {"", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", ""};
    private float dummyData[] = {0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f};

    private LinearLayout lnExhaleStart, lnExhaleChart;
    private CardView cardStart;
    private LineChartView chartExhale;
    private TextView tvMeasurement, tvPrediction;

    public ExhaleFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_exhale, container, false);

        preferences = getContext().getSharedPreferences(USER_PREF, Context.MODE_PRIVATE);
        editor = preferences.edit();

        lnExhaleChart = (LinearLayout) view.findViewById(R.id.ln_exhale_chart);
        lnExhaleStart = (LinearLayout) view.findViewById(R.id.ln_exhale_start);
        cardStart = (CardView) view.findViewById(R.id.card_start);
        chartExhale = (LineChartView) view.findViewById(R.id.chart_exhale);
        tvMeasurement = (TextView) view.findViewById(R.id.tv_measurement);
        tvPrediction = (TextView) view.findViewById(R.id.tv_prediction);

        cardStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.ConnectedThread mThread = ((MainActivity) getActivity()).getmConnectedThread();

                lnExhaleStart.setVisibility(View.GONE);
                lnExhaleChart.setVisibility(View.VISIBLE);

                dataset = new LineSet(dummyLabel, dummyData);
                dataset.setColor(Color.parseColor("#1971B6"))
                        .setThickness(Tools.fromDpToPx(2))
                        .setSmooth(true)
                        .beginAt(0)
                        .endAt(30);

                for (int i = 0; i < dummyLabel.length; i++) {
                    Point point = (Point) dataset.getEntry(i);
                    point.setColor(Color.parseColor("#FF857A"));
                }

                chartExhale.addData(dataset);

                Paint thresPaint = new Paint();
                thresPaint.setColor(Color.parseColor("#0079AE"));
                thresPaint.setStyle(Paint.Style.STROKE);
                thresPaint.setAntiAlias(true);
                thresPaint.setStrokeWidth(Tools.fromDpToPx(.50f));
                thresPaint.setPathEffect(new DashPathEffect(new float[]{10, 10}, 0));

                chartExhale.setXLabels(AxisRenderer.LabelPosition.NONE)
                        .setYLabels(AxisRenderer.LabelPosition.NONE)
                        .setValueThreshold(3f, 3f, thresPaint)
                        .setAxisBorderValues(0f, 6f)
                        .show();

                setExhaleStatus(false);
                mThread.start();
            }
        });

        return view;
    }


    /**
     * Setter and Getter
     */
    public boolean isExhaleStatus() {
        return exhaleStatus;
    }

    public void setExhaleStatus(boolean exhaleStatus) {
        this.exhaleStatus = exhaleStatus;
    }

    public LinearLayout getLnExhaleStart() {
        return lnExhaleStart;
    }

    public LinearLayout getLnExhaleChart() {
        return lnExhaleChart;
    }

    public LineChartView getChartExhale() {
        return chartExhale;
    }

    public TextView getTvMeasurement() {
        return tvMeasurement;
    }

    public TextView getTvPrediction() {
        return tvPrediction;
    }

    public float[] getDummyData() {
        return dummyData;
    }

    /**
     * Formula to get Vc Prediction
     */
    public double getVcPrediction(int height, int age, String gender) {
        double vc = 0;

        if (gender.equals("Male")) {
            vc = (0.052 * height) - (0.022 * age) - 3.00;
        } else if (gender.equals("Female")) {
            vc = (0.041 * height) - (0.018 * age) - 2.69;
        } else {
            return 0;
        }
        return vc;
    }

    /**
     * Formula to get Vc Measurement
     */
    public double getVcMeasurement(List<Integer> list) {
        double vcTop = 0;
        int countTop = 0;
        double vcBottom = 0;
        int countBottom = 0;
        double midLine = 0;

        for (int i : list) {
            midLine += i;
        }
        midLine = midLine / list.size();
        Log.i("MidLine", String.valueOf(midLine));

        for (int i : list) {
            if (i > midLine) {
                vcTop += i;
                countTop++;
            } else {
                vcBottom += i;
                countBottom++;
            }
        }
        Log.i("VcTop", String.valueOf(vcTop / countTop));
        Log.i("VcBottom", String.valueOf(vcBottom / countBottom));

        return Math.abs((vcTop / countTop) - (vcBottom / countBottom));
    }

    /**
     * Formula to get error value
     */
    public double getErrorValue(double vcSensor, double vcPrediction) {
        double result = ((vcSensor - vcPrediction) / vcSensor) * 100;
        return Math.abs(result);
    }

    /**
     * Compare Vc Measurement with Vc Prediction
     */
    public boolean getComparison(double vcSensor, double vcPrediction) {
        if (vcSensor >= (vcPrediction * 0.8)) {
            return true;
        } else {
            return false;
        }
    }
}
