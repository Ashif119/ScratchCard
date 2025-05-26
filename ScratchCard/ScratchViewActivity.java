package com.solution.easypay.xyz.ScratchCard;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.solution.easypay.xyz.Api.Response.LoginResponse;
import com.solution.easypay.xyz.ApiHits.ApiClient;
import com.solution.easypay.xyz.ApiHits.ApplicationConstant;
import com.solution.easypay.xyz.ApiHits.CouponList;
import com.solution.easypay.xyz.ApiHits.EndPointInterface;
import com.solution.easypay.xyz.ApiHits.GetRedeemCommissionRequest;
import com.solution.easypay.xyz.ApiHits.GetRedeemCommissionResponse;
import com.solution.easypay.xyz.ApiHits.UtilMethods;
import com.solution.easypay.xyz.BuildConfig;
import com.solution.easypay.xyz.R;
import com.solution.easypay.xyz.Util.AppPreferences;
import com.solution.easypay.xyz.Util.CustomAlertDialog;
import com.solution.easypay.xyz.Util.CustomLoader;
import com.solution.easypay.xyz.Util.Utility;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ScratchViewActivity extends AppCompatActivity {

    private CustomAlertDialog mCustomAlertDialog;
    private CustomLoader loader;
    private AppPreferences mAppPreferences;
    private LoginResponse mLoginDataResponse;
    private String deviceId, deviceSerialNum;
    private ImageView imageScratch, closeBtn;
    private Bitmap bitmap;
    private Canvas canvas;
    private Paint paint;
    private boolean isScratched = false;
    private TextView txtTransactionId, txtOperator, txtRedeemAmount, txtTid, txtIsRedeemed;
    private View layoutDetails;
    private CouponList couponList;
    private double redeemAmount;
    private int Id;
    private GetRedeemCommissionResponse mGetRedeemCommissionResponse;
    private float transparencyPercentage;

    private boolean isApiCallMade = false;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scratch_view);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mCustomAlertDialog = new CustomAlertDialog(this);
        loader = new CustomLoader(this, R.style.TransparentTheme);
        mAppPreferences = new AppPreferences(this);
        if (UtilMethods.INSTANCE.mLoginResponse == null) {
            UtilMethods.INSTANCE.mLoginResponse = new Gson().fromJson(mAppPreferences.getString(ApplicationConstant.INSTANCE.LoginPref), LoginResponse.class);
        }
        mLoginDataResponse = UtilMethods.INSTANCE.mLoginResponse;
        deviceId = mAppPreferences.getNonRemovalString(ApplicationConstant.INSTANCE.DeviceIdPref);
        deviceSerialNum = mAppPreferences.getNonRemovalString(ApplicationConstant.INSTANCE.DeviceSerialNumberPref);
        Intent intent = this.getIntent();
        redeemAmount = intent.getDoubleExtra("redeemAmount", 0.00);
        Id = intent.getIntExtra("Id",0);
        findViews();
        setCouponListData(redeemAmount);
        // Set up the scratch card
        ViewTreeObserver viewTreeObserver = imageScratch.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                imageScratch.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                // Set up the scratch card
                bitmap = Bitmap.createBitmap(imageScratch.getWidth(), imageScratch.getHeight(), Bitmap.Config.ARGB_8888);
                canvas = new Canvas(bitmap);
                paint = new Paint();
                paint.setColor(0xFF8660A5);
                paint.setStyle(Paint.Style.FILL_AND_STROKE);
                canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), paint);
                imageScratch.setImageBitmap(bitmap);
            }
        });
        imageScratch.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int action = event.getAction();
                final int x = (int) event.getX();
                final int y = (int) event.getY();

                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_MOVE:
                        scratchCard(x, y);
                        break;
                    case MotionEvent.ACTION_UP:
                        if (!isApiCallMade) { // Check if API call has not been made yet
                            checkIfScratchedAsync();
                        }
//                        checkIfScratchedAsync();
                        break;
                }

                return true;
            }
        });


    }

    private void findViews() {
        imageScratch = findViewById(R.id.imageScratch);
        // Retrieve the necessary views from the XML layout
        txtTransactionId = findViewById(R.id.txtTransactionId);
        txtOperator = findViewById(R.id.txtOperator);
        txtRedeemAmount = findViewById(R.id.txtRedeemAmount);
        txtTid = findViewById(R.id.txtTid);
        txtIsRedeemed = findViewById(R.id.txtIsRedeemed);
        layoutDetails = findViewById(R.id.layoutDetails);
    }


    private void setCouponListData(double redeemAmount) {
        txtRedeemAmount.setText(Utility.INSTANCE.formatedAmountWithRupees(String.valueOf(redeemAmount)));
    }

    private void checkIfScratchedAsync() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final boolean isScratched = checkIfScratched();

                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        if (isScratched) {
                            getRedeemCommission(ScratchViewActivity.this);
//                            Toast.makeText(ScratchViewActivity.this, "Succes -"+transparencyPercentage, Toast.LENGTH_LONG).show();
//                            showFullImage();
                            // Show all the scratch card details
                            layoutDetails.setVisibility(View.VISIBLE);
                            // Additionally, you can update the scratch card image visibility if needed
                            imageScratch.setVisibility(View.GONE);
                            isApiCallMade = true;
//                            Toast.makeText(ScratchViewActivity.this, "Congratulations! You've revealed the full image.", Toast.LENGTH_SHORT).show();
//                        } else {
//                            Toast.makeText(ScratchViewActivity.this, "Keep scratching!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }).start();
    }

    private void getRedeemCommission(ScratchViewActivity mActivity) {
        try {
            if (UtilMethods.INSTANCE.isNetworkAvialable(this)) {
                try {
                    loader.show();
                    loader.setCancelable(false);
                    EndPointInterface git = ApiClient.getClient().create(EndPointInterface.class);
                    HashMap<String, String> map = new HashMap<>();
                    map.put("appid", ApplicationConstant.INSTANCE.APP_ID);
                    map.put("userID", mLoginDataResponse.getData().getUserID());
                    map.put("sessionID", mLoginDataResponse.getData().getSessionID());
                    map.put("version", BuildConfig.VERSION_NAME);
                    map.put("session", mLoginDataResponse.getData().getSession());
                    Call<GetRedeemCommissionResponse> call = git.getRedeemCommission(map,new GetRedeemCommissionRequest(Id));
                    call.enqueue(new Callback<GetRedeemCommissionResponse>() {
                        @Override
                        public void onResponse(Call<GetRedeemCommissionResponse> call, Response<GetRedeemCommissionResponse> response) {
                            loader.dismiss();
                            if (response.isSuccessful()) {
                                try {
                                    if (response.body() != null) {
                                        mGetRedeemCommissionResponse = response.body();
                                        if (mGetRedeemCommissionResponse.getStatuscode() == 1) {
                                            UtilMethods.INSTANCE.Successful(mActivity, mGetRedeemCommissionResponse.getMsg());

                                        }else if (mGetRedeemCommissionResponse.getStatuscode() == -1) {
                                            UtilMethods.INSTANCE.Processing(mActivity, mGetRedeemCommissionResponse.getMsg());

                                        }

                                    }
                                } catch (Exception ex) {
                                    Log.e("exception", ex.getMessage());
                                    UtilMethods.INSTANCE.Error(mActivity, ex.getMessage());
                                }

                            }else {
                                UtilMethods.INSTANCE.Error(mActivity, response.message());
                            }
                        }

                        @Override
                        public void onFailure(Call<GetRedeemCommissionResponse> call, Throwable t) {
                            try {
                                loader.dismiss();
                                if (t.getMessage() != null && !t.getMessage().isEmpty()) {

                                    if (t.getMessage().contains("No address associated with hostname")) {
                                        UtilMethods.INSTANCE.NetworkError(mActivity);

                                    } else {
                                        UtilMethods.INSTANCE.Error(mActivity, t.getMessage());

                                    }

                                } else {
                                    UtilMethods.INSTANCE.Error(mActivity, getString(R.string.something_error));

                                }
                            } catch (IllegalStateException ise) {
                                UtilMethods.INSTANCE.Error(mActivity, ise.getMessage());

                            }
                        }
                    });
                } catch (Exception ex) {
                    loader.dismiss();
                    UtilMethods.INSTANCE.Error(mActivity, ex.getMessage());

                }
            }
        } catch (Exception ex) {
            loader.dismiss();
            UtilMethods.INSTANCE.Error(mActivity, ex.getMessage());

        }
    }


    private void showFullImage() {
        // Load the full image from a drawable or any other source
        Drawable fullImageDrawable = getResources().getDrawable(R.drawable.scratch_card);

        // Set the full image drawable to the ImageView
        imageScratch.setImageDrawable(fullImageDrawable);
    }


    private void scratchCard(int x, int y) {
        if (!isScratched) {
            paint.setColor(Color.TRANSPARENT);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
            canvas.drawCircle(x, y, 100, paint);
            imageScratch.invalidate();
        }
    }

    private boolean checkIfScratched() {
        int pixels = bitmap.getWidth() * bitmap.getHeight();
        int transparentPixels = 0;

        for (int i = 0; i < bitmap.getWidth(); i++) {
            for (int j = 0; j < bitmap.getHeight(); j++) {
                int pixel = bitmap.getPixel(i, j);
                if (Color.alpha(pixel) == 0) {
                    transparentPixels++;
                }
            }
        }
        transparencyPercentage = (float) transparentPixels / pixels * 100;

        float revealThreshold = 50;  // Adjust the threshold as needed

        return transparencyPercentage >= revealThreshold;
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_OK);
        super.onBackPressed();
    }
}
