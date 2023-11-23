package com.example.salahnow;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextView textViewFajr, textViewDhuhr, textViewAsr, textViewMaghrib, textViewIsha, textViewDateTime;
    private ProgressBar progressBar;
    private RequestQueue requestQueue;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textViewFajr = findViewById(R.id.textViewFajr);
        textViewDhuhr = findViewById(R.id.textViewDhuhr);
        textViewAsr = findViewById(R.id.textViewAsr);
        textViewMaghrib = findViewById(R.id.textViewMaghrib);
        textViewIsha = findViewById(R.id.textViewIsha);
        textViewDateTime = findViewById(R.id.textViewDateTime);
        progressBar = findViewById(R.id.progressBar);

        requestQueue = Volley.newRequestQueue(this);

        // Automatically load prayer times when the activity is created
        getPrayerTimes();
        // Update the current date and time every second
        updateDateTime();
    }

    private void updateDateTime() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Update the TextView with the current date and time
                SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM dd, yyyy hh:mm:ss a", Locale.getDefault());
                String currentDateAndTime = sdf.format(new Date());
                textViewDateTime.setText("Current Date and Time: " + currentDateAndTime);
                // Repeat this every second
                updateDateTime();
            }
        }, 1000);
    }

    private void getPrayerTimes() {
        // Replace with your city and country
        String city = "";
        String country = "";
        String method = "2"; // You can change the calculation method if needed

        // Build the parameters
        String apiUrl = "https://api.aladhan.com/v1/timingsByCity";
        String url = apiUrl + "?city=" + city + "&country=" + country + "&method=" + method;

        // Show the ProgressBar while loading
        progressBar.setVisibility(View.VISIBLE);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject data = response.getJSONObject("data");
                            JSONObject timings = data.getJSONObject("timings");

                            String fajr = convertTo12HourFormat(timings.getString("Fajr"));
                            String dhuhr = convertTo12HourFormat(timings.getString("Dhuhr"));
                            String asr = convertTo12HourFormat(timings.getString("Asr"));
                            String maghrib = convertTo12HourFormat(timings.getString("Maghrib"));
                            String isha = convertTo12HourFormat(timings.getString("Isha"));

                            // Update individual TextViews
                            textViewFajr.setText("Fajr: " + fajr);
                            textViewDhuhr.setText("Dhuhr: " + dhuhr);
                            textViewAsr.setText("Asr: " + asr);
                            textViewMaghrib.setText("Maghrib: " + maghrib);
                            textViewIsha.setText("Isha: " + isha);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        } finally {
                            // Hide the ProgressBar after loading is complete
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        // Hide the ProgressBar in case of an error
                        progressBar.setVisibility(View.GONE);
                    }
                });

        // Add the request to the RequestQueue
        requestQueue.add(request);
    }

    private String convertTo12HourFormat(String time24) {
        try {
            SimpleDateFormat sdf24 = new SimpleDateFormat("HH:mm", Locale.getDefault());
            SimpleDateFormat sdf12 = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            Date dateObj = sdf24.parse(time24);
            return sdf12.format(dateObj);
        } catch (ParseException e) {
            e.printStackTrace();
            return time24; // return original time in case of an error
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Cancel all pending requests when the activity is stopped
        if (requestQueue != null) {
            requestQueue.cancelAll(this);
        }
        // Remove callbacks to prevent memory leaks
        handler.removeCallbacksAndMessages(null);
    }
}
