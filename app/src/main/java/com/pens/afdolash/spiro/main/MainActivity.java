package com.pens.afdolash.spiro.main;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.text.Html;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.pens.afdolash.spiro.R;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    public static final String MAIN_TAG = MainActivity.class.getSimpleName();

    // Share preference
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    // Share preference tag
    public static final String USER_PREF = "USER_PREF";
    public static final String EXTRA_BLUETOOTH = "EXTRA_BLUETOOTH";
    public static final String EXTRA_USER_NAME = "EXTRA_USER_NAME";
    public static final String EXTRA_USER_AGE = "EXTRA_USER_AGE";
    public static final String EXTRA_USER_GENDER = "EXTRA_USER_GENDER";
    public static final String EXTRA_USER_GENDER_ID = "EXTRA_USER_GENDER_ID";
    public static final String EXTRA_USER_HEIGHT = "EXTRA_USER_HEIGHT";
    public static final String EXTRA_USER_WEIGHT = "EXTRA_USER_WEIGHT";
    public static final String EXTRA_USER_LUNGS = "EXTRA_USER_LUNGS";
    public static final String EXTRA_USER_RESULT = "EXTRA_USER_RESULT";

    // UUID service - This is the type of Bluetooth device that the BT module is
    // It is very likely yours will be the same, if not google UUID for your manufacturer
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private static final int NUM_PAGES = 4;

    // View
    private ViewPager mPager;
    private PagerAdapter mPagerAdapter;
    private TextView[] dots;
    private TextView tvBack, tvNext;
    private CardView cardBack, cardNext;
    private LinearLayout lnDots;

    // Fragment
    private IntroductionFragment introductionFragment = new IntroductionFragment();
    private ExhaleFragment exhaleFragment = new ExhaleFragment();
    private ResultFragment resultFragment = new ResultFragment();
    private FinishFragment finishFragment = new FinishFragment();

    // Bluetooth
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket bluetoothSocket;
    private OutputStream outputStream;
    private String bluetoothAddress = null;

    // Component to get data from AT Mega
    private StringBuilder recDataString = new StringBuilder();
    private List<Integer> dataSensor = new ArrayList<>();
    private String[] data;

    // Handle and thread
    private final int handlerState = 0;
    private ConnectedThread mConnectedThread;
    private Handler mBtIn;

    private boolean doubleBackToExit = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inisialisasi Shared Preference
        preferences = getApplicationContext().getSharedPreferences(USER_PREF, MODE_PRIVATE);
        editor = preferences.edit();

        // Bluetooth Adapter
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        checkBTState();

        // Initiatization View
        mPager = (ViewPager) findViewById(R.id.view_pager);
        cardBack = (CardView) findViewById(R.id.card_back);
        cardNext = (CardView) findViewById(R.id.card_next);
        tvNext = (TextView) findViewById(R.id.tv_next);
        tvBack = (TextView) findViewById(R.id.tv_back);
        lnDots = (LinearLayout) findViewById(R.id.ln_dots);

        // Adding bottom dots
        addBottomDots(0);

        mPagerAdapter = new SlidePagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);
        mPager.addOnPageChangeListener(pagerChangeListener);
        mPager.setOnTouchListener(pagerTouchListener);

        // When button next clicked
        cardNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (mPager.getCurrentItem())  {
                    case 0:
                        int selectedGender = introductionFragment.getRadGender().getCheckedRadioButtonId();

                        // Get all edit text
                        String name = introductionFragment.getEtName().getText().toString();
                        String gender = introductionFragment.getUserGender(selectedGender);
                        String age = introductionFragment.getEtAge().getText().toString();
                        String height = introductionFragment.getEtHeight().getText().toString();
                        String weight = introductionFragment.getEtWeight().getText().toString();

                        // Check condition
                        if (!name.isEmpty() && !gender.isEmpty() && !age.isEmpty() && !height.isEmpty() && !weight.isEmpty()) {
                            mPager.setCurrentItem(mPager.getCurrentItem() + 1);

                            editor.putString(EXTRA_USER_NAME, name);
                            editor.putString(EXTRA_USER_GENDER, gender);
                            editor.putInt(EXTRA_USER_GENDER_ID, selectedGender);
                            editor.putInt(EXTRA_USER_AGE, Integer.parseInt(age));
                            editor.putInt(EXTRA_USER_HEIGHT, Integer.parseInt(height));
                            editor.putInt(EXTRA_USER_WEIGHT, Integer.parseInt(weight));
                            editor.apply();
                        } else {
                            Toast.makeText(MainActivity.this, "Please fill the blank form!", Toast.LENGTH_SHORT).show();
                        }
                        break;

                    case 1:
                        // Check condition
                        if (exhaleFragment.isExhaleStatus()) {
                            mPager.setCurrentItem(mPager.getCurrentItem() + 1);

                            // Set value of exhale result
                            String title = preferences.getString(EXTRA_USER_RESULT, "Bad");
                            Log.i("Title", title);
                            if (title.equals("Good")) {
                                resultFragment.getTvTitle().setText(title);
                                resultFragment.getTvDescription().setText(R.string.lungs_is_good);
                                resultFragment.getImgResult().setImageResource(R.drawable.ai_lungs_good);
                            } else {
                                resultFragment.getTvTitle().setText(title);
                                resultFragment.getTvDescription().setText(R.string.lungs_is_bad);
                                resultFragment.getImgResult().setImageResource(R.drawable.ai_lungs_bad);
                            }
                        } else {
                            Toast.makeText(MainActivity.this, "Please check your lung health condition first!", Toast.LENGTH_SHORT).show();
                        }
                        break;

                    case 2:
                        // Check condition
                        mPager.setCurrentItem(mPager.getCurrentItem() + 1);
                        break;

                    case 3:
                        finish();
                }
            }
        });

        cardBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPager.setCurrentItem(mPager.getCurrentItem() - 1);
            }
        });

        // Handler to get data sensor from AT Mega
        mBtIn = new Handler() {
            public void handleMessage(android.os.Message msg) {
                if (msg.what == handlerState) {
                    // Get data from msg
                    String readMessage = (String) msg.obj;

                    // Append data from readMessage to String Builder
                    recDataString.append(readMessage);

                    // Trim data from String Builder to String Array
                    data = recDataString.toString().split("\n");

                    // Process data
                    if (data.length > 0) {
                        Log.i("Data Str2Hex", convertStringToHex(data[data.length - 1]));
                        Log.i("Data Hex2Dec", String.valueOf(convertHexToDecimal(convertStringToHex(data[data.length - 1]))));

                        try {
                            // Convert ASCII to Hex to Decimal
                            int dataInt = convertHexToDecimal(convertStringToHex(data[data.length - 1]));

                            // Check data is not empty
                            if (dataInt >= 1) {
                                if (dataInt > 5) {
                                    dataInt = 5;
                                    dataSensor.add(dataInt);
                                } else {
                                    dataSensor.add(dataInt);
                                }

                                // Check data is not empty
                                if (dataSensor.size() > 0) {
                                    // Get last data sensor
                                    int lastUpdate = dataSensor.get(dataSensor.size() - 1);

                                    // Create grafik from last data
                                    for (int i = 0; i < exhaleFragment.getDummyData().length - 1; i++) {
                                        exhaleFragment.getDummyData()[i] = exhaleFragment.getDummyData()[i + 1];
                                    }
                                    exhaleFragment.getDummyData()[exhaleFragment.getDummyData().length - 1] = lastUpdate;
                                    exhaleFragment.getChartExhale().updateValues(0, exhaleFragment.getDummyData());
                                    exhaleFragment.getChartExhale().notifyDataUpdate();

                                    // Condition if data sensor >= 50
                                    if (dataSensor.size() >= 30) {
                                        // Insert to shared preference
                                        int age = preferences.getInt(EXTRA_USER_AGE, 0);
                                        int weight = preferences.getInt(EXTRA_USER_WEIGHT, 0);
                                        int height = preferences.getInt(EXTRA_USER_HEIGHT, 0);
                                        String gender = preferences.getString(EXTRA_USER_GENDER, null);

                                        // Get data measurement and prediction
                                        double vcMeasurement = exhaleFragment.getVcMeasurement(dataSensor);
                                        double vcPrediction = exhaleFragment.getVcPrediction(height, age, gender);
                                        double getError = exhaleFragment.getErrorValue(vcMeasurement, vcPrediction);

                                        Log.i("VcMeasurement", String.valueOf(vcMeasurement));
                                        Log.i("VcPrediction", String.valueOf(vcPrediction));
                                        Log.i("Compare", String.valueOf(exhaleFragment.getComparison(vcMeasurement, vcPrediction)));

                                        // View data measurement and prediction
                                        exhaleFragment.getTvMeasurement().setText(String.format("%.3f", vcMeasurement));
                                        exhaleFragment.getTvPrediction().setText(String.format("%.3f", vcPrediction));

                                        // Compare data measurement and predition
                                        if (exhaleFragment.getComparison(vcMeasurement, vcPrediction)) {
                                            editor.putString(EXTRA_USER_RESULT, "Good");
                                        } else {
                                            editor.putString(EXTRA_USER_RESULT, "Bad");
                                        }

                                        // Insert data to shared preference
                                        editor.putString(EXTRA_USER_LUNGS, String.format("%.3f", exhaleFragment.getErrorValue(vcMeasurement, vcPrediction)));
                                        editor.commit();

                                        // Reset all cariable to default
                                        exhaleFragment.setExhaleStatus(true);
                                        mConnectedThread.interrupt();
                                        dataSensor.clear();
                                        data = null;
                                    }
                                }
                            }
                        } catch (Exception e) {
                            Log.e("Data Error", e.getMessage().toString());
                        }

                    }
                }
            }
        };
    }

    /**
     * Convert String to Hex
     */
    private String convertStringToHex(String string) {
        StringBuilder outer = new StringBuilder();

        for (int i = 0; i < string.length(); i++) {
            outer.append(String.format("%x ", (byte)(string.charAt(i))));
        }
        return outer.toString();
    }

    /**
     * Convert Hex to Decimal
     */
    private int convertHexToDecimal(String string) {
        StringBuilder outer = new StringBuilder();

        for (int i = 0; i < string.length(); i++) {
            if (!Character.isSpaceChar(string.charAt(i))) {
                outer.append(string.charAt(i));
            }
        }
        int value = Integer.parseInt(outer.toString(), 16);

        return value;
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Check bluetooth when phone connected again
        bluetoothAddress = preferences.getString(EXTRA_BLUETOOTH, null);
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(bluetoothAddress);

        try {
            // Create new Thread
            bluetoothSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e1) {
            Toast.makeText(this, "Could not create bluetooth socket.", Toast.LENGTH_SHORT).show();
        }

        try {
            bluetoothSocket.connect();
        } catch (IOException e) {
            try {
                bluetoothSocket.close();
            } catch (IOException e2) {
                Toast.makeText(getBaseContext(), "Could not close Bluetooth socket.", Toast.LENGTH_SHORT).show();
            }
        }

        try {
            outputStream = bluetoothSocket.getOutputStream();
        } catch (IOException e) {
            Toast.makeText(getBaseContext(), "Could not create bluetooth outstream.", Toast.LENGTH_SHORT).show();
        }

        mConnectedThread = new ConnectedThread(bluetoothSocket);
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            bluetoothSocket.close();
        } catch (IOException e2) {
            Toast.makeText(getBaseContext(), "Failed to close bluetooth socket.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            outputStream.close();
            bluetoothSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        if (mPager.getCurrentItem() == 0) {
            if (doubleBackToExit) {
                super.onBackPressed();
                try {
                    bluetoothSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return;
            }

            this.doubleBackToExit = true;
            Toast.makeText(this, "Please click back again to exit.", Toast.LENGTH_SHORT).show();

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    doubleBackToExit = false;
                }
            }, 2000);
        } else {
            mPager.setCurrentItem(mPager.getCurrentItem() - 1);
        }
    }


    /**
     * get activated Connection Thread
     *
     * @return Connection Thread Class
     */
    public ConnectedThread getmConnectedThread() {
        return mConnectedThread;
    }


    /**
     * Viewpager slide not activate
     */
    ViewPager.OnTouchListener pagerTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            return false;
        }
    };

    /**
     * Viewpager change listener
     */
    ViewPager.OnPageChangeListener pagerChangeListener = new ViewPager.OnPageChangeListener() {

        @Override
        public void onPageSelected(int position) {
            addBottomDots(position);

            // Changing the next button text 'NEXT' / 'BACK'
            if (position == 0) {
                // First page. make back button clickable false
                cardBack.setClickable(false);
                cardBack.setAlpha(0.5f);
                tvNext.setText("Next");
                cardNext.setCardBackgroundColor(Color.parseColor("#1971B6"));
            } else if (position == NUM_PAGES - 1){
                // Last page. make next button change text to finish
                cardBack.setClickable(false);
                cardBack.setAlpha(0.5f);
                cardNext.setCardBackgroundColor(Color.parseColor("#FF857A"));
                tvNext.setText("Close");
            } else {
                cardNext.setCardBackgroundColor(Color.parseColor("#1971B6"));
                tvNext.setText("Next");
                cardBack.setClickable(true);
                cardBack.setAlpha(1f);
            }
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }

        @Override
        public void onPageScrollStateChanged(int arg0) {
        }
    };


    /**
     * Connection thread for get data
     */
    public class ConnectedThread extends Thread {
        private InputStream mmInStream;
        private OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            try {
                // Create I/O streams for connection
                this.mmInStream = socket.getInputStream();
                this.mmOutStream = socket.getOutputStream();
            } catch (IOException e) {
                Toast.makeText(getApplication(), "Error - Unable to create IO Stream.", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void run() {
            byte[] buffer = new byte[256];
            int bytes;
            Log.i("Run", "Running...");

            while (!isInterrupted()) {
                try {
                    Log.i("Get Data", "Get Data...");

                    bytes = mmInStream.read(buffer);
                    String readMessage = new String(buffer, 0, bytes);

                    mBtIn.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }

        // Write method
        public void write(String input) {
            byte[] msgBuffer = input.getBytes();

            try {
                mmOutStream.write(msgBuffer);
            } catch (IOException e) {
                Toast.makeText(getBaseContext(), "Error - Connection failure.", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }


    /**
     * Slide pager adapter
     */
    private class SlidePagerAdapter extends FragmentStatePagerAdapter {
        public SlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 1:
                    return exhaleFragment;
                case 2:
                    return resultFragment;
                case 3:
                    return finishFragment;
                default:
                    return introductionFragment;
            }
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }


    /**
     * Adding bottom dots each view
     *
     * @param currentPage current page number
     */
    private void addBottomDots(int currentPage) {
        dots = new TextView[NUM_PAGES];

        int[] colorsActive = getResources().getIntArray(R.array.array_dot_active);
        int[] colorsInactive = getResources().getIntArray(R.array.array_dot_inactive);

        lnDots.removeAllViews();
        for (int i = 0; i < dots.length; i++) {
            dots[i] = new TextView(this);
            dots[i].setText(Html.fromHtml("&#8226;"));
            dots[i].setTextSize(35);
            dots[i].setTextColor(colorsInactive[currentPage]);
            lnDots.addView(dots[i]);
        }

        if (dots.length > 0)
            dots[currentPage].setTextColor(colorsActive[currentPage]);
    }


    /**
     * Checking bluetooth state
     */
    private void checkBTState() {
        if(bluetoothAdapter == null) {
            Toast.makeText(this, "Device does not support bluetooth.", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            if (!bluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }
}
