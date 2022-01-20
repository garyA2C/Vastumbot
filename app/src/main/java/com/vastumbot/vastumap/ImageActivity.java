package com.vastumbot.vastumap;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.vastumbot.vastumap.ui.Waste;
import com.vastumbot.vastumap.ui.home.HomeFragment;

import org.json.JSONException;
import org.json.JSONObject;

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
                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                builder.setCancelable(true);
                builder.setTitle("Confirmer le ramassage ? ");
                builder.setMessage("Nous marquerons sur nos serveurs le déchet comme ramassé");
                builder.setPositiveButton("Oui",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                statusUpdate("found");
                                waste.status="found";
                                textStatus.setText("Status : found");

                                HomeFragment.allWaste.get(waste.id).status="found";
                                HomeFragment.drawOnMap();

                            }
                        });
                builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
        butUntraceable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                AlertDialog.Builder okbuilder = new AlertDialog.Builder(v.getContext());
                okbuilder.setCancelable(true);
                okbuilder.setTitle("Confirmation");
                okbuilder.setMessage("Changement effectué sur le serveur");


                builder.setCancelable(true);
                builder.setTitle("Confirmer que le déchet est manquant ? ");
                builder.setMessage("Nous marquerons sur nos serveurs le déchet comme introuvable");
                builder.setPositiveButton("Oui",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                statusUpdate("disappeared");
                                waste.status="disappeared";
                                textStatus.setText("Status : disappeared");

                                HomeFragment.allWaste.set(waste.id,waste);
                                HomeFragment.drawOnMap();

                            }
                        });
                builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();
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
        String strUrl = "http://192.168.137.1:8008/"+waste.id+"/image.jpg";
        URL url = new URL(strUrl);
        HttpURLConnection connection  = (HttpURLConnection) url.openConnection();

        InputStream is = connection.getInputStream();
        Bitmap img = BitmapFactory.decodeStream(is);

        image.setImageBitmap(img);
    }

    public void statusUpdate(String status) {
        String url = "http://192.168.137.1:8008/"+waste.id;

        RequestQueue queue = Volley.newRequestQueue(this);

        final JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("status", status);
        } catch (JSONException e) {
            // handle exception
        }

        JsonObjectRequest putRequest = new JsonObjectRequest(Request.Method.PUT, url, jsonObject,
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response) {
                        // response
                        System.out.println("Response" + response.toString());
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        System.out.println("Error.Response" + error.toString());
                    }
                }
        ) {
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json");
                return headers;
            }

            @Override
            public String getBodyContentType() {
                return "application/json";
            }
        };
        queue.add(putRequest);
    }
}