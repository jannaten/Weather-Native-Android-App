package com.example.myapplication;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONException;
import org.json.JSONObject;
import java.text.DecimalFormat;

public class LanLat extends AppCompatActivity implements LocationListener {
    double latitude=67, longitude=23;
    private Button goBack;
    private LocationManager locationManager;
    private Location lastKnownLocation;
    // PRIMARILY INITIALIZE A LOCAL VALUE FOR LAT & LONG
    // THEN AFTER USING onLocationChanged METHOD WE CHANGE TIS VALUE FOR FURTHER USE
    DecimalFormat dFormatter = new DecimalFormat("#.##");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lan_lat);
        goBack = (Button) findViewById(R.id.goback);
        goBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMainActivity();
            }
        });
        if( savedInstanceState != null ) {
            latitude = savedInstanceState.getDouble("latitude_value", latitude);
            longitude = savedInstanceState.getDouble("longitude_value", longitude);
        }
    }
    @Override
    protected void onSaveInstanceState( Bundle savedInstanceState ){
        super.onSaveInstanceState(savedInstanceState);
        // Save the latitude and longitude here. For each value we give a key and a value.
        savedInstanceState.putString("latitude_value", String.valueOf(latitude));
        savedInstanceState.putString("longitude_value", String.valueOf(longitude));
    }

    public void openMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }


    public void startGps(View view) {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
            return;
        }
        ;
        lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
    }

    @Override
    public void onLocationChanged(Location location) {
        lastKnownLocation = location;
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        TextView latituteTextView = (TextView) findViewById(R.id.latituteTextView);
        TextView longitudeTextView = (TextView) findViewById(R.id.longitudeTextView);
        latituteTextView.setText( "" + dFormatter.format(latitude));
        longitudeTextView.setText(""+ dFormatter.format(longitude));
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }



    public void getWeather( View v ){
        // Get weather data from openweathermap.org server
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        String url ="https://api.openweathermap.org/data/2.5/weather?lat="+latitude+"&lon="+longitude+"&appid=034e4c1a00d9f959337a5f7b1cccd8eb";
        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // response now contains the response from the server
                        Toast.makeText(getApplicationContext(), response, Toast.LENGTH_LONG).show();
                        parseJsonAndUpdateUI( response );
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // here we come if the server responded with an error
                        Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_LONG).show();
                    }
                });
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }


    private void parseJsonAndUpdateUI(String response) {
        // Collecting the response from the json api link
        try {
            //Setting the values for particular objects
            JSONObject obj = new JSONObject(response);
            JSONObject main = obj.getJSONObject("main");
            JSONObject wind = obj.getJSONObject("wind");
            JSONObject sys = obj.getJSONObject("sys");

            // Reading the values from the json objects
            double temperature = main.getDouble("temp") - 273;
            double feelslike = main.getDouble("feels_like") - 273;
            int humidity = main.getInt("humidity");
            double windSpeed = wind.getDouble("speed");
            String country = sys.getString("country");

            // Setting the values on the Front TextViews
            TextView temperatureTextView = (TextView)findViewById(R.id.temperatureTextView);
            TextView feelsText = (TextView)findViewById(R.id.feelsText);
            TextView windTextView = (TextView)findViewById(R.id.windTextView);
            TextView humidityTextView = (TextView)findViewById(R.id.humidityTextView);
            TextView CountryText = (TextView)findViewById(R.id.CountryText);


            temperatureTextView.setText( "" + dFormatter.format(temperature) + " °C");
            feelsText.setText( "" + dFormatter.format(feelslike) + " °C");
            windTextView.setText( "" + dFormatter.format(windSpeed) + " ms⁻¹");
            humidityTextView.setText( "" + dFormatter.format(humidity) + " %");
            CountryText.setText(""+ country);
            LinearLayout myLayout1 = (LinearLayout) findViewById(R.id.myLayout1);

            if( temperature > 10 ){ // Overweight
                myLayout1.setBackgroundColor(Color.parseColor("#ff0000"));
            }
            else if( temperature > -5 ) { // Normal Weight BMI between 22 and 26
                myLayout1.setBackgroundColor(Color.parseColor("#00cc00"));
            }
            else { // Underweight
                myLayout1.setBackgroundColor(Color.parseColor("#0000ff"));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
