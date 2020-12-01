package com.example.myapplication;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

public class CityID extends AppCompatActivity {
    private Button goBack;
    String CityID;
    EditText CityIDEditText;
    private Button setParseValues;
    DecimalFormat dFormatter = new DecimalFormat("#.##");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city_i_d);
        goBack = (Button) findViewById(R.id.goback);
        goBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMainActivity();
            }
        });
        CityIDEditText = (EditText) findViewById(R.id.CityIDEditText);
        setParseValues = (Button) findViewById(R.id.setValues);
        setParseValues.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CityID = CityIDEditText.getText().toString();
            }
        });

    }
    public void openMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void getWeather( View v ){
        // Get weather data from openweathermap.org server
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "https://api.openweathermap.org/data/2.5/weather?id="+ CityID +"&appid=034e4c1a00d9f959337a5f7b1cccd8eb";
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
            LinearLayout myLayout2 = (LinearLayout) findViewById(R.id.myLayout2);

            if( temperature > 10 ){ // Overweight
                myLayout2.setBackgroundColor(Color.parseColor("#ff0000"));
            }
            else if( temperature > -5 ) { // Normal Weight BMI between 22 and 26
                myLayout2.setBackgroundColor(Color.parseColor("#00ff00"));
            }
            else { // Underweight
                myLayout2.setBackgroundColor(Color.parseColor("#0000ff"));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
