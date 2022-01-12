package com.vastumbot.vastumap;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.vastumbot.vastumap.ui.Waste;
import com.vastumbot.vastumap.ui.home.HomeFragment;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ImageActivity extends AppCompatActivity {
    private TextView textType, textStatus, textTime;
    private ImageView image;
    private Button butUntraceable, butCleared;
    private Waste waste;
    private RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);
        textType=findViewById(R.id.textType);
        textStatus=findViewById(R.id.textStatus);
        textTime=findViewById(R.id.textTime);
        image=findViewById(R.id.imageView);
        butCleared = findViewById(R.id.buttonCleared);
        butUntraceable=findViewById(R.id.buttonUntraceable);
        RequestQueue queue = Volley.newRequestQueue(this); // this = context

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);


        butCleared.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*try {
                    statusUpdate("found");
                } catch (IOException e) {
                    e.printStackTrace();
                }*/
            }
        });
        butUntraceable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                statusUpdate("disappeared");
            }
        });
        waste= HomeFragment.getWaste();
            String timeStamp = new SimpleDateFormat("EEE dd MMM yyyy hh:mm:ss", Locale.FRANCE).format(waste.date);
        textTime.setText("Ajouté le : "+timeStamp);
        textType.setText("Type : "+waste.type);
        textStatus.setText("Status : "+waste.status);

        try {
            getImageFromServer();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void getImageFromServer() throws IOException {
        String strUrl = "http://192.168.137.1:8008/"+waste.id+"/image.png";
        URL url = new URL(strUrl);
        HttpURLConnection connection  = (HttpURLConnection) url.openConnection();

        InputStream is = connection.getInputStream();
        Bitmap img = BitmapFactory.decodeStream(is);

        image.setImageBitmap(img);
    }

    public void statusUpdate(String status) {
        String url = "http://192.168.137.1:8008/"+waste.id;
        StringRequest putRequest = new StringRequest(Request.Method.PUT, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        System.out.println(response);
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        System.out.println(error);
                    }
                }
        ) {

            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<String, String>();
                params.put("status", status);
                return params;
            }

        };

        MySingleton.getInstance(this).addToRequestQueue(putRequest);
    }
}