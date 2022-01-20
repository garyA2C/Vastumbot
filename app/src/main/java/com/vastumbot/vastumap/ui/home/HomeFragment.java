package com.vastumbot.vastumap.ui.home;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.vastumbot.vastumap.BuildConfig;
import com.vastumbot.vastumap.ImageActivity;
import com.vastumbot.vastumap.MainActivity;
import com.vastumbot.vastumap.MySingleton;
import com.vastumbot.vastumap.R;
import com.vastumbot.vastumap.databinding.FragmentHomeBinding;
import com.vastumbot.vastumap.ui.Waste;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;



public class HomeFragment extends Fragment implements OnMapReadyCallback {

    private HomeViewModel homeViewModel;
    private FragmentHomeBinding binding;

    public static boolean actif = true;

    private static final String TAG = MainActivity.class.getSimpleName();
    public static GoogleMap map;
    private CameraPosition cameraPosition;

    // The entry point to the Places API.
    private PlacesClient placesClient;

    //private Marker marker;

    // The entry point to the Fused Location Provider.
    private FusedLocationProviderClient fusedLocationProviderClient;

    // A default location (Sydney, Australia) and default zoom to use when location permission is
    // not granted.
    private final LatLng defaultLocation = new LatLng(-33.8523341, 151.2106085);
    private static final int DEFAULT_ZOOM = 18;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean locationPermissionGranted;

    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    private Location lastKnownLocation;

    // Keys for storing activity state.
    // [START maps_current_place_state_keys]
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";
    // [END maps_current_place_state_keys]

    // Used for selecting the current place.
    private static final int M_MAX_ENTRIES = 5;
    private String[] likelyPlaceNames;
    private String[] likelyPlaceAddresses;
    private List[] likelyPlaceAttributions;
    private LatLng[] likelyPlaceLatLngs;

    private Switch swi;

    private static View view;
    private static Waste waste;

    public static ArrayList<Waste> allWaste;

    private boolean FirstDraw;
    private static float f=20f;

    private BitmapDescriptor bitPlastic, bitGlass, bitOrganic, bitBulky, bitNonRecyclable, bitPaper, bitCardboard;
    private static Bitmap bitSmallMarker;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textHome;
        homeViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        // [START_EXCLUDE silent]
        // [START maps_current_place_on_create_save_instance_state]
        // Retrieve location and camera position from saved instance state.
        if (savedInstanceState != null) {
            lastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            cameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }
        // [END maps_current_place_on_create_save_instance_state]
        // [END_EXCLUDE]

        // Retrieve the content view that renders the map.
        //setContentView(R.layout.activity_main);

        // [START_EXCLUDE silent]
        // Construct a PlacesClient
        Places.initialize(root.getContext().getApplicationContext(), BuildConfig.MAPS_API_KEY);
        placesClient = Places.createClient(root.getContext().getApplicationContext());

        // Construct a FusedLocationProviderClient.
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(root.getContext().getApplicationContext());

        // Build the map.
        // [START maps_current_place_map_fragment]
        SupportMapFragment mapFragment = (SupportMapFragment) this.getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        // [END maps_current_place_map_fragment]
        // [END_EXCLUDE]
        HomeFragment.view = root;
        return root;
    }

    /**
     * Saves the state of the map when the activity is paused.
     */
    // [START maps_current_place_on_save_instance_state]
    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (map != null) {
            outState.putParcelable(KEY_CAMERA_POSITION, map.getCameraPosition());
            outState.putParcelable(KEY_LOCATION, lastKnownLocation);
        }
        super.onSaveInstanceState(outState);
    }

    /**
     * Manipulates the map when it's available.
     * This callback is triggered when the map is ready to be used.
     */
    // [START maps_current_place_on_map_ready]
    @Override
    public void onMapReady(GoogleMap map) {

        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            boolean success = map.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            view.getContext(), R.raw.style_json));

            if (!success) {
                Log.e(TAG, "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Can't find style. Error: ", e);
        }
        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            boolean success = map.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            view.getContext(), R.raw.style_json));

            if (!success) {
                Log.e(TAG, "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Can't find style. Error: ", e);
        }

        this.map = map;

        // Prompt the user for permission.
        getLocationPermission();
        // [END_EXCLUDE]

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();

        // Get the current location of the device and set the position of the map.
        getDeviceLocation();

        map.setMinZoomPreference(16);
        map.setMaxZoomPreference(20);
        map.getUiSettings().setRotateGesturesEnabled(false);
        map.getUiSettings().setZoomControlsEnabled(true);
        map.getUiSettings().setTiltGesturesEnabled(false);

        bitPlastic = BitmapDescriptorFactory.fromBitmap(resizeBitmap("plastic", 75, 75));
        bitPaper = BitmapDescriptorFactory.fromBitmap(resizeBitmap("paper", 75, 75));
        bitCardboard = BitmapDescriptorFactory.fromBitmap(resizeBitmap("cardboard", 75, 75));
        bitNonRecyclable = BitmapDescriptorFactory.fromBitmap(resizeBitmap("nonrecyclable", 75, 75));
        bitBulky = BitmapDescriptorFactory.fromBitmap(resizeBitmap("bulky", 75, 75));
        bitOrganic = BitmapDescriptorFactory.fromBitmap(resizeBitmap("organic", 75, 75));
        bitGlass = BitmapDescriptorFactory.fromBitmap(resizeBitmap("glass", 75, 75));

        BitmapDrawable bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.drawable.miniblackdot);
        Bitmap b = bitmapdraw.getBitmap();
        bitSmallMarker = Bitmap.createScaledBitmap(b, 50, 50, false);

        FirstDraw=true;

        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(@NonNull Marker marker) {
                /*String latitude=Double.toString(marker.getPosition().latitude);
                String longitude=Double.toString(marker.getPosition().longitude);
                Uri gmmIntentUri = Uri.parse("google.navigation:q="+latitude+"+"+longitude+"&mode=w");
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);*/
                //MySingleton.getInstance(view.getContext()).addToRequestQueue(jsonObjectRequest);
                return false;
            }
        });

        map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(@NonNull Marker marker) {
                for (Waste w : allWaste){
                    if (actif){
                        if ((w.status.equals("disappeared")) || (w.status.equals("active"))) {
                            if (w.marker.equals(marker)){
                                waste=w;
                                switchActivities();
                            }
                        }
                    }else{
                        if (w.status.equals("found")){
                            if (w.marker.equals(marker)){
                                waste=w;
                                switchActivities();
                            }
                        }
                    }
                }
            }
        });

        map.setOnGroundOverlayClickListener(new GoogleMap.OnGroundOverlayClickListener() {
            @Override
            public void onGroundOverlayClick(@NonNull GroundOverlay groundOverlay) {
                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(groundOverlay.getPosition())
                        .zoom(map.getCameraPosition().zoom)
                        .build();
                CameraUpdate cu = CameraUpdateFactory.newCameraPosition(cameraPosition);
                map.animateCamera(cu);
                for (Waste w : allWaste){
                    if (actif){
                        System.out.println(w.status);
                        if ((w.status.equals("disappeared")) || (w.status.equals("active"))) {
                            if (groundOverlay.equals(w.groundOverlay)){
                                System.out.println("reached");

                                System.out.println(groundOverlay.getPosition());
                                w.marker.setVisible(true);
                                w.marker.setInfoWindowAnchor(0.5f, 0.3f);
                                w.marker.showInfoWindow();

                            }else {
                                w.marker.setVisible(false);
                                System.out.println("erased");

                            }
                        }else{
                            System.out.println("nani?");

                        }
                    }else{
                        System.out.println("non active");
                        if (w.status.equals("found")){
                            if (groundOverlay.equals(w.groundOverlay)){
                                System.out.println(groundOverlay.getPosition());
                                w.marker.setVisible(true);
                                w.marker.setInfoWindowAnchor(0.5f, 0.3f);
                                w.marker.showInfoWindow();

                            }else{
                                w.marker.setVisible(false);
                            }
                        }
                    }
                }
            }
        });

        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(@NonNull LatLng latLng) {
                if (FirstDraw){
                    drawOnMap();
                    FirstDraw=false;
                }

                for (Waste w : allWaste) {
                    if (actif){
                        if ((w.status.equals("disappeared")) || (w.status.equals("active"))) {
                            w.marker.setVisible(false);
                        }
                    }else{
                        if (w.status.equals("found")){
                            w.marker.setVisible(false);
                        }
                    }
                }
            }
        });

        initAllWaste();
        actualiseAllWaste();
        drawOnMap();
    }
    // [END maps_current_place_on_map_ready]

    /**
     * Gets the current location of the device, and positions the map's camera.
     */
    // [START maps_current_place_get_device_location]
    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (locationPermissionGranted) {
                Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(getActivity(), new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            lastKnownLocation = task.getResult();
                            if (lastKnownLocation != null) {
                                map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                        new LatLng(lastKnownLocation.getLatitude(),
                                                lastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                            }
                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
                            map.moveCamera(CameraUpdateFactory
                                    .newLatLngZoom(defaultLocation, DEFAULT_ZOOM));
                            map.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage(), e);
        }
    }
    // [END maps_current_place_get_device_location]

    /**
     * Prompts the user for permission to use the device location.
     */
    // [START maps_current_place_location_permission]
    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }
    // [END maps_current_place_location_permission]

    /**
     * Handles the result of the request for location permissions.
     */
    // [START maps_current_place_on_request_permissions_result]
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        locationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationPermissionGranted = true;
                }
            }
        }
        updateLocationUI();
    }
    // [END maps_current_place_on_request_permissions_result]

    /**
     * Updates the map's UI settings based on whether the user has granted location permission.
     */
    // [START maps_current_place_update_location_ui]
    private void updateLocationUI() {
        if (map == null) {
            return;
        }
        try {
            if (locationPermissionGranted) {
                map.setMyLocationEnabled(true);
                map.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                map.setMyLocationEnabled(false);
                map.getUiSettings().setMyLocationButtonEnabled(false);
                lastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }
    // [END maps_current_place_update_location_ui]

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;

    }

    public Bitmap resizeBitmap(String drawableName, int width, int height) {
        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(), getResources().getIdentifier(drawableName, "drawable", getActivity().getPackageName()));
        return Bitmap.createScaledBitmap(imageBitmap, width, height, false);
    }
    public static void drawOnMap(){
        map.clear();
        System.out.println("\n \n\nyooooo\n\n\n");
        if (actif){
            for (Waste w : allWaste) {
                if (w.status.equals("disappeared")) {
                    System.out.println(w.type);
                    if (w.isSameType("plastic")) {
                        GroundOverlayOptions newarkMap = new GroundOverlayOptions()
                                .image(BitmapDescriptorFactory.fromResource(R.drawable.plastic))
                                .position(w.coord, f, f)
                                .clickable(true)
                                .transparency(0.7f);

                        w.groundOverlay = map.addGroundOverlay(newarkMap);
                        w.marker = map.addMarker(new MarkerOptions()
                                .position(w.coord)
                                .title("Plastic")
                                .snippet(w.date.toString())
                                .visible(false)
                                .anchor(0.5f, 0.5f)
                                .icon(BitmapDescriptorFactory.fromBitmap(bitSmallMarker))
                                .alpha(0f));
                    } else if (w.isSameType("glass")) {
                        GroundOverlayOptions newarkMap = new GroundOverlayOptions()
                                .image(BitmapDescriptorFactory.fromResource(R.drawable.glass))
                                .position(w.coord, f, f)
                                .clickable(true)
                                .transparency(0.7f);
                        w.groundOverlay = map.addGroundOverlay(newarkMap);
                        w.marker = map.addMarker(new MarkerOptions()
                                .position(w.coord)
                                .title("Glass")
                                .snippet(w.date.toString())
                                .visible(false)
                                .anchor(0.5f, 0.5f)
                                .icon(BitmapDescriptorFactory.fromBitmap(bitSmallMarker))
                                .alpha(0f));
                    } else if (w.isSameType("paper")) {
                        GroundOverlayOptions newarkMap = new GroundOverlayOptions()
                                .image(BitmapDescriptorFactory.fromResource(R.drawable.paper))
                                .position(w.coord, f, f)
                                .clickable(true)
                                .transparency(0.7f);
                        w.groundOverlay = map.addGroundOverlay(newarkMap);
                        w.marker = map.addMarker(new MarkerOptions()
                                .position(w.coord)
                                .title("Paper")
                                .snippet(w.date.toString())
                                .visible(false)
                                .anchor(0.5f, 0.5f)
                                .icon(BitmapDescriptorFactory.fromBitmap(bitSmallMarker))
                                .alpha(0f));
                    } else if (w.isSameType("cardboard")) {
                        GroundOverlayOptions newarkMap = new GroundOverlayOptions()
                                .image(BitmapDescriptorFactory.fromResource(R.drawable.cardboard))
                                .position(w.coord, f, f)
                                .clickable(true)
                                .transparency(0.7f);
                        w.groundOverlay = map.addGroundOverlay(newarkMap);
                        w.marker = map.addMarker(new MarkerOptions()
                                .position(w.coord)
                                .title("Cardboard")
                                .snippet(w.date.toString())
                                .visible(false)
                                .anchor(0.5f, 0.5f)
                                .icon(BitmapDescriptorFactory.fromBitmap(bitSmallMarker))
                                .alpha(0f));
                    } else if (w.isSameType("nonrecyclable")) {
                        GroundOverlayOptions newarkMap = new GroundOverlayOptions()
                                .image(BitmapDescriptorFactory.fromResource(R.drawable.nonrecyclable))
                                .position(w.coord, f, f)
                                .clickable(true)
                                .transparency(0.7f);
                        w.groundOverlay = map.addGroundOverlay(newarkMap);
                        w.marker = map.addMarker(new MarkerOptions()
                                .position(w.coord)
                                .title("Non recyclable")
                                .snippet(w.date.toString())
                                .visible(false)
                                .anchor(0.5f, 0.5f)
                                .icon(BitmapDescriptorFactory.fromBitmap(bitSmallMarker))
                                .alpha(0f));
                    } else if (w.isSameType("organic")) {
                        GroundOverlayOptions newarkMap = new GroundOverlayOptions()
                                .image(BitmapDescriptorFactory.fromResource(R.drawable.organic))
                                .position(w.coord, f, f)
                                .clickable(true)
                                .transparency(0.7f);
                        w.groundOverlay = map.addGroundOverlay(newarkMap);
                        w.marker = map.addMarker(new MarkerOptions()
                                .position(w.coord)
                                .title("Organic")
                                .snippet(w.date.toString())
                                .visible(false)
                                .anchor(0.5f, 0.5f)
                                .icon(BitmapDescriptorFactory.fromBitmap(bitSmallMarker))
                                .alpha(0f));
                    } else if (w.isSameType("bulky")) {
                        GroundOverlayOptions newarkMap = new GroundOverlayOptions()
                                .image(BitmapDescriptorFactory.fromResource(R.drawable.bulky))
                                .position(w.coord, f, f)
                                .clickable(true)
                                .transparency(0.7f);
                        w.groundOverlay = map.addGroundOverlay(newarkMap);
                        w.marker = map.addMarker(new MarkerOptions()
                                .position(w.coord)
                                .title("Bulky")
                                .snippet(w.date.toString())
                                .visible(false)
                                .anchor(0.5f, 0.5f)
                                .icon(BitmapDescriptorFactory.fromBitmap(bitSmallMarker))
                                .alpha(0f));
                    } else {
                        LatLng c = new LatLng(45.783884, 4.872454);
                        GroundOverlayOptions newarkMap = new GroundOverlayOptions()
                                .image(BitmapDescriptorFactory.fromResource(R.drawable.logonobg))
                                .position(c, f, f)
                                .clickable(true)
                                .transparency(0.5f);
                        w.groundOverlay = map.addGroundOverlay(newarkMap);
                        w.marker = map.addMarker(new MarkerOptions()
                                .position(w.coord)
                                .title("Error")
                                .snippet(w.date.toString())
                                .visible(false)
                                .anchor(0.5f, 0.5f)
                                .icon(BitmapDescriptorFactory.fromBitmap(bitSmallMarker))
                                .alpha(0f));
                    }

                } else if (w.status.equals("active")) {
                    System.out.println(w.type);
                    if (w.isSameType("plastic")) {
                        GroundOverlayOptions newarkMap = new GroundOverlayOptions()
                                .image(BitmapDescriptorFactory.fromResource(R.drawable.plastic))
                                .position(w.coord, f, f)
                                .clickable(true);
                        w.groundOverlay = map.addGroundOverlay(newarkMap);
                        w.marker = map.addMarker(new MarkerOptions()
                                .position(w.coord)
                                .title("Plastic")
                                .snippet(w.date.toString())
                                .visible(false)
                                .anchor(0.5f, 0.5f)
                                .icon(BitmapDescriptorFactory.fromBitmap(bitSmallMarker))
                                .alpha(0f));
                    } else if (w.isSameType("glass")) {
                        GroundOverlayOptions newarkMap = new GroundOverlayOptions()
                                .image(BitmapDescriptorFactory.fromResource(R.drawable.glass))
                                .position(w.coord, f, f)
                                .clickable(true);
                        w.groundOverlay = map.addGroundOverlay(newarkMap);
                        w.marker = map.addMarker(new MarkerOptions()
                                .position(w.coord)
                                .title("Glass")
                                .snippet(w.date.toString())
                                .visible(false)
                                .anchor(0.5f, 0.5f)
                                .icon(BitmapDescriptorFactory.fromBitmap(bitSmallMarker))
                                .alpha(0f));
                    } else if (w.isSameType("paper")) {
                        GroundOverlayOptions newarkMap = new GroundOverlayOptions()
                                .image(BitmapDescriptorFactory.fromResource(R.drawable.paper))
                                .position(w.coord, f, f)
                                .clickable(true);
                        w.groundOverlay = map.addGroundOverlay(newarkMap);
                        w.marker = map.addMarker(new MarkerOptions()
                                .position(w.coord)
                                .title("Paper")
                                .snippet(w.date.toString())
                                .visible(false)
                                .anchor(0.5f, 0.5f)
                                .icon(BitmapDescriptorFactory.fromBitmap(bitSmallMarker))
                                .alpha(0f));
                    } else if (w.isSameType("cardboard")) {
                        GroundOverlayOptions newarkMap = new GroundOverlayOptions()
                                .image(BitmapDescriptorFactory.fromResource(R.drawable.cardboard))
                                .position(w.coord, f, f)
                                .clickable(true);
                        w.groundOverlay = map.addGroundOverlay(newarkMap);
                        w.marker = map.addMarker(new MarkerOptions()
                                .position(w.coord)
                                .title("Cardboard")
                                .snippet(w.date.toString())
                                .visible(false)
                                .anchor(0.5f, 0.5f)
                                .icon(BitmapDescriptorFactory.fromBitmap(bitSmallMarker))
                                .alpha(0f));
                    } else if (w.isSameType("nonrecyclable")) {
                        GroundOverlayOptions newarkMap = new GroundOverlayOptions()
                                .image(BitmapDescriptorFactory.fromResource(R.drawable.nonrecyclable))
                                .position(w.coord, f, f)
                                .clickable(true);
                        w.groundOverlay = map.addGroundOverlay(newarkMap);
                        w.marker = map.addMarker(new MarkerOptions()
                                .position(w.coord)
                                .title("Non recyclable")
                                .snippet(w.date.toString())
                                .visible(false)
                                .anchor(0.5f, 0.5f)
                                .icon(BitmapDescriptorFactory.fromBitmap(bitSmallMarker))
                                .alpha(0f));
                    } else if (w.isSameType("organic")) {
                        GroundOverlayOptions newarkMap = new GroundOverlayOptions()
                                .image(BitmapDescriptorFactory.fromResource(R.drawable.organic))
                                .position(w.coord, f, f)
                                .clickable(true);
                        w.groundOverlay = map.addGroundOverlay(newarkMap);
                        w.marker = map.addMarker(new MarkerOptions()
                                .position(w.coord)
                                .title("Organic")
                                .snippet(w.date.toString())
                                .visible(false)
                                .anchor(0.5f, 0.5f)
                                .icon(BitmapDescriptorFactory.fromBitmap(bitSmallMarker))
                                .alpha(0f));
                    } else if (w.isSameType("bulky")) {
                        GroundOverlayOptions newarkMap = new GroundOverlayOptions()
                                .image(BitmapDescriptorFactory.fromResource(R.drawable.bulky))
                                .position(w.coord, f, f)
                                .clickable(true);
                        w.groundOverlay = map.addGroundOverlay(newarkMap);
                        w.marker = map.addMarker(new MarkerOptions()
                                .position(w.coord)
                                .title("Bulky")
                                .snippet(w.date.toString())
                                .visible(false)
                                .anchor(0.5f, 0.5f)
                                .icon(BitmapDescriptorFactory.fromBitmap(bitSmallMarker))
                                .alpha(0f));
                    } else {
                        LatLng c = new LatLng(45.783884, 4.872454);
                        GroundOverlayOptions newarkMap = new GroundOverlayOptions()
                                .image(BitmapDescriptorFactory.fromResource(R.drawable.logonobg))
                                .position(c, f, f)
                                .clickable(true);
                        w.groundOverlay = map.addGroundOverlay(newarkMap);
                        w.marker = map.addMarker(new MarkerOptions()
                                .position(w.coord)
                                .title("Error")
                                .snippet(w.date.toString())
                                .visible(false)
                                .anchor(0.5f, 0.5f)
                                .icon(BitmapDescriptorFactory.fromBitmap(bitSmallMarker))
                                .alpha(0f));
                    }
                }
            }
        }else{
            for (Waste w : allWaste) {
                if (w.status.equals("found")) {
                    System.out.println(w.type);
                    if (w.isSameType("plastic")) {
                        GroundOverlayOptions newarkMap = new GroundOverlayOptions()
                                .image(BitmapDescriptorFactory.fromResource(R.drawable.plastic))
                                .position(w.coord, f, f)
                                .clickable(true);
                        w.groundOverlay = map.addGroundOverlay(newarkMap);
                        w.marker = map.addMarker(new MarkerOptions()
                                .position(w.coord)
                                .title("Plastic")
                                .snippet(w.date.toString())
                                .visible(false)
                                .anchor(0.5f, 0.5f)
                                .icon(BitmapDescriptorFactory.fromBitmap(bitSmallMarker))
                                .alpha(0f));
                    } else if (w.isSameType("glass")) {
                        GroundOverlayOptions newarkMap = new GroundOverlayOptions()
                                .image(BitmapDescriptorFactory.fromResource(R.drawable.glass))
                                .position(w.coord, f, f)
                                .clickable(true);
                        w.groundOverlay = map.addGroundOverlay(newarkMap);
                        w.marker = map.addMarker(new MarkerOptions()
                                .position(w.coord)
                                .title("Glass")
                                .snippet(w.date.toString())
                                .visible(false)
                                .anchor(0.5f, 0.5f)
                                .icon(BitmapDescriptorFactory.fromBitmap(bitSmallMarker))
                                .alpha(0f));
                    } else if (w.isSameType("paper")) {
                        GroundOverlayOptions newarkMap = new GroundOverlayOptions()
                                .image(BitmapDescriptorFactory.fromResource(R.drawable.paper))
                                .position(w.coord, f, f)
                                .clickable(true);
                        w.groundOverlay = map.addGroundOverlay(newarkMap);
                        w.marker = map.addMarker(new MarkerOptions()
                                .position(w.coord)
                                .title("Paper")
                                .snippet(w.date.toString())
                                .visible(false)
                                .anchor(0.5f, 0.5f)
                                .icon(BitmapDescriptorFactory.fromBitmap(bitSmallMarker))
                                .alpha(0f));
                    } else if (w.isSameType("cardboard")) {
                        GroundOverlayOptions newarkMap = new GroundOverlayOptions()
                                .image(BitmapDescriptorFactory.fromResource(R.drawable.cardboard))
                                .position(w.coord, f, f)
                                .clickable(true);
                        w.groundOverlay = map.addGroundOverlay(newarkMap);
                        w.marker = map.addMarker(new MarkerOptions()
                                .position(w.coord)
                                .title("Cardboard")
                                .snippet(w.date.toString())
                                .visible(false)
                                .anchor(0.5f, 0.5f)
                                .icon(BitmapDescriptorFactory.fromBitmap(bitSmallMarker))
                                .alpha(0f));
                    } else if (w.isSameType("nonrecyclable")) {
                        GroundOverlayOptions newarkMap = new GroundOverlayOptions()
                                .image(BitmapDescriptorFactory.fromResource(R.drawable.nonrecyclable))
                                .position(w.coord, f, f)
                                .clickable(true);
                        w.groundOverlay = map.addGroundOverlay(newarkMap);
                        w.marker = map.addMarker(new MarkerOptions()
                                .position(w.coord)
                                .title("Non recyclable")
                                .snippet(w.date.toString())
                                .visible(false)
                                .anchor(0.5f, 0.5f)
                                .icon(BitmapDescriptorFactory.fromBitmap(bitSmallMarker))
                                .alpha(0f));
                    } else if (w.isSameType("organic")) {
                        GroundOverlayOptions newarkMap = new GroundOverlayOptions()
                                .image(BitmapDescriptorFactory.fromResource(R.drawable.organic))
                                .position(w.coord, f, f)
                                .clickable(true);
                        w.groundOverlay = map.addGroundOverlay(newarkMap);
                        w.marker = map.addMarker(new MarkerOptions()
                                .position(w.coord)
                                .title("Organic")
                                .snippet(w.date.toString())
                                .visible(false)
                                .anchor(0.5f, 0.5f)
                                .icon(BitmapDescriptorFactory.fromBitmap(bitSmallMarker))
                                .alpha(0f));
                    } else if (w.isSameType("bulky")) {
                        GroundOverlayOptions newarkMap = new GroundOverlayOptions()
                                .image(BitmapDescriptorFactory.fromResource(R.drawable.bulky))
                                .position(w.coord, f, f)
                                .clickable(true);
                        w.groundOverlay = map.addGroundOverlay(newarkMap);
                        w.marker = map.addMarker(new MarkerOptions()
                                .position(w.coord)
                                .title("Bulky")
                                .snippet(w.date.toString())
                                .visible(false)
                                .anchor(0.5f, 0.5f)
                                .icon(BitmapDescriptorFactory.fromBitmap(bitSmallMarker))
                                .alpha(0f));
                    } else {
                        LatLng c = new LatLng(45.783884, 4.872454);
                        GroundOverlayOptions newarkMap = new GroundOverlayOptions()
                                .image(BitmapDescriptorFactory.fromResource(R.drawable.logonobg))
                                .position(c, f, f)
                                .clickable(true);
                        w.groundOverlay = map.addGroundOverlay(newarkMap);
                        w.marker = map.addMarker(new MarkerOptions()
                                .position(w.coord)
                                .title("Error")
                                .snippet(w.date.toString())
                                .visible(false)
                                .anchor(0.5f, 0.5f)
                                .icon(BitmapDescriptorFactory.fromBitmap(bitSmallMarker))
                                .alpha(0f));
                    }
                }
            }
        }
    }
    public static void initAllWaste(){
        allWaste=new ArrayList<Waste>();
    }

    public static void actualiseAllWaste(){

        String url = "http://192.168.137.1:8008";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Iterator<String> temp = response.keys();
                            while (temp.hasNext()) {
                                String key = temp.next();
                                JSONObject value = (JSONObject) response.get(key);
                                int id = Integer.parseInt(key);
                                double lat = (Double) value.get("lat");
                                double lon = (Double) value.get("lon");
                                LatLng coord = new LatLng(lat, lon);
                                long timeStamp = Long.valueOf((Integer) value.get("timestamp"));
                                Date date = new Date(timeStamp * 1000);
                                String type = (String) value.get("type");
                                String status = (String) value.get("status");
                                int id_user = (Integer) (value.get("id_user"));
                                Waste newWaste = new Waste(id, coord, date, type, status, id_user);
                                if (!allWaste.contains(newWaste)) {
                                    allWaste.add(newWaste);
                                    System.out.println("\n \n \n pouet \n \n \n");
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        System.out.println("Erroooooor" + error.toString());
                    }
                });

        // Access the RequestQueue through your singleton class.
        MySingleton.getInstance(view.getContext()).addToRequestQueue(jsonObjectRequest);
    }

    private void switchActivities() {
        Intent switchActivityIntent = new Intent(view.getContext(), ImageActivity.class);
        startActivity(switchActivityIntent);
    }

    public static Waste getWaste(){
        return waste;
    }
}