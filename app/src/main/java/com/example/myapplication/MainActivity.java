package com.example.myapplication;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    private Button button1 , button2, button3, button4, button5;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button1 = (Button) findViewById(R.id.lanbutton);
        button2 = (Button) findViewById(R.id.CityCountryButton);
        button3 = (Button) findViewById(R.id.PostlaCountryButton);
        button4 = (Button) findViewById(R.id.CityIDButton);
        button5 = (Button) findViewById(R.id.Manual);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openLanLatActivity();
            }
        });
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCityCountryActivity();
            }
        });
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openPostalCountryActivity();
            }
        });
        button4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCityIDActivity();
            }
        });
        button5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openManualActivity();
            }
        });
    }
    public void openLanLatActivity() {
        Intent intent = new Intent(this, LanLat.class);
        startActivity(intent);
    }
    public void openCityCountryActivity() {
        Intent intent = new Intent(this, CityCountry.class);
        startActivity(intent);
    }
    public void openPostalCountryActivity() {
        Intent intent = new Intent(this, PostalCity.class);
        startActivity(intent);
    }
    public void openCityIDActivity() {
        Intent intent = new Intent(this, CityID.class);
        startActivity(intent);
    }
    public void openManualActivity() {
        Intent intent = new Intent(this, Manual.class);
        startActivity(intent);
    }

}