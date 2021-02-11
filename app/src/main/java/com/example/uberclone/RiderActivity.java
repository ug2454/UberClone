package com.example.uberclone;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class RiderActivity extends FragmentActivity implements OnMapReadyCallback {

    boolean isUberClicked = true;
    Button callUber;
    private GoogleMap mMap;
    LocationManager locationManager;
    LocationListener locationListener;
    Handler handler = new Handler();
    TextView textView;
    boolean driverActive = false;

    ArrayList<Marker> markers = new ArrayList<>();

    public void checkForUpdates() {
        ParseQuery<ParseObject> query = new ParseQuery<>("Request");
        query.whereEqualTo("username", ParseUser.getCurrentUser().getUsername());
        query.whereExists("driverUsername");
        query.findInBackground((objects, e) -> {
            markers.clear();
            if (e == null) {
                if (objects.size() > 0) {
                    driverActive = true;
                    ParseQuery<ParseUser> query1 = ParseUser.getQuery();
                    query1.whereEqualTo("username", objects.get(0).getString("driverUsername"));
                    query1.findInBackground((objects1, e1) -> {
                        if (e1 == null && objects1.size() > 0) {

//
                            ParseGeoPoint driverLocation = objects1.get(0).getParseGeoPoint("location");
//                            ParseGeoPoint userLocation = objects.get(0).getParseGeoPoint("location");
                            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                                Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                                if (lastKnownLocation != null) {
                                    ParseGeoPoint userLocation = new ParseGeoPoint(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                                    double distance = 0;
                                    if (driverLocation != null) {
                                        distance = driverLocation.distanceInKilometersTo(userLocation);
                                    }


                                    distance = distance * 1000;
                                    if (distance < 15) {
                                        textView.setText("Your driver is here");
                                        ParseQuery<ParseObject> query2 = new ParseQuery<>("Request");
                                        query2.whereEqualTo("username", ParseUser.getCurrentUser().getUsername());
                                        query2.findInBackground((objects2, e2) -> {
                                            if (e2 == null) {
                                                for (ParseObject object : objects2) {
                                                    object.deleteInBackground();
                                                }
                                            }
                                        });
                                        handler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                callUber.setVisibility(View.VISIBLE);
                                                callUber.setText("CALL UBER");
                                                isUberClicked = true;
                                                driverActive = false;
                                                textView.setText("");

                                            }
                                        }, 5000);
                                    } else {
                                        DecimalFormat decimalFormat = new DecimalFormat("#,###.00");
                                        String distanceString = decimalFormat.format(distance);
                                        textView.setText("Your driver is " + distanceString + " metre away!");
                                        Location placeLocation = new Location(LocationManager.NETWORK_PROVIDER);

                                        assert driverLocation != null;
                                        placeLocation.setLatitude(driverLocation.getLatitude());
                                        placeLocation.setLongitude(driverLocation.getLongitude());
                                        centerMapOnLocation1(placeLocation);


                                        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
                                        locationListener = new LocationListener() {
                                            @Override
                                            public void onLocationChanged(@NonNull Location location) {
//                centerMapOnLocation(location);


                                            }

                                            @Override
                                            public void onProviderDisabled(@NonNull String provider) {

                                            }

                                            @Override
                                            public void onProviderEnabled(@NonNull String provider) {

                                            }

                                            @Override
                                            public void onStatusChanged(String provider, int status, Bundle extras) {

                                            }
                                        };
                                        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                                        } else {
                                            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
//                                        Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);


                                            LatLng lastKnown = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                                            markers.add(mMap.addMarker(new MarkerOptions().position(lastKnown).title("Your Current Location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))));


                                        }
                                        System.out.println(markers.size());
                                        LatLngBounds.Builder builder = new LatLngBounds.Builder();
                                        for (Marker marker : markers) {
                                            builder.include(marker.getPosition());
                                        }
                                        LatLngBounds bounds = builder.build();
                                        int padding = 200; // offset from edges of the map in pixels
                                        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                                        mMap.moveCamera(cu);
                                        callUber.setVisibility(View.INVISIBLE);
                                        handler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                checkForUpdates();
                                            }
                                        }, 2000);
                                    }

                                }

                            }


//                            if(driverLocation==objects.get(0).getParseGeoPoint("location")){
//                                textView.setText("Driver is at your destination");
//                                callUber.setVisibility(View.VISIBLE);
//                                callUber.setText("CALL UBER");
//                                isUberClicked = true;
//                            }
                        }
                    });


                    callUber.setVisibility(View.INVISIBLE);
                }
//                handler.postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        checkForUpdates();
//                    }
//                }, 2000);

            }
        });
    }


//    @Override
//    protected void onPause() {
//        super.onPause();
//        locationManager.removeUpdates(locationListener);
//
//    }

    public void logOut(View view) {
        ParseUser.logOut();
        Intent in = new Intent(this, MainActivity.class);
        finish();
        startActivity(in);

    }

    public void callUber(View view) {

        if (isUberClicked) {
//            callUber.setText("CANCEL UBER");
//            isUberClicked = false;


            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
                Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                if (lastKnownLocation != null) {
                    System.out.println("IN IF");

                    ParseObject request = new ParseObject("Request");
                    request.put("username", ParseUser.getCurrentUser().getUsername());
                    ParseGeoPoint parseGeoPoint = new ParseGeoPoint(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());

                    request.put("location", parseGeoPoint);
                    request.saveInBackground(e -> {
                        if (e == null) {
                            System.out.println("Location saved in db");
                            callUber.setText("CANCEL UBER");
                            isUberClicked = false;
                            checkForUpdates();
                        } else {
                            System.out.println(e.getMessage());
                        }
                    });
                } else {
                    Toast.makeText(this, "Could not find location, Please try again", Toast.LENGTH_SHORT).show();
                }

            }

        } else {
            ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Request");
            query.whereEqualTo("username", ParseUser.getCurrentUser().getUsername());
            query.findInBackground((objects, e) -> {
                if (e == null) {
                    if (objects.size() > 0) {
                        callUber.setText("CALL UBER");
                        isUberClicked = true;
                        for (ParseObject object : objects) {
                            object.deleteInBackground(e1 -> {
                                if (e1 == null) {
                                    System.out.println("Request Deleted");
                                } else {
                                    System.out.println("Request Failed to Delete");
                                }
                            });
                        }
                    }
                }
            });

        }

    }

    public void centerMapOnLocation1(Location location) {
        if (driverActive) {
            LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.clear();

            mMap.addMarker(new MarkerOptions().position(userLocation).title("Drivers Location"));
        }

    }


    public void centerMapOnLocation(Location location) {
        if (!driverActive) {
            LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.clear();

            mMap.addMarker(new MarkerOptions().position(userLocation).title("You are here"));
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
                Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                centerMapOnLocation(lastKnownLocation);
            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rider);
        callUber = findViewById(R.id.callUberButton);
        textView = findViewById(R.id.infoTextView);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Request");
        query.whereEqualTo("username", ParseUser.getCurrentUser().getUsername());
        query.findInBackground((objects, e) -> {
            if (e == null) {
                if (objects.size() > 0) {
                    callUber.setText("CANCEL UBER");
                    isUberClicked = false;
                    checkForUpdates();
                }
            }
        });
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                centerMapOnLocation(location);
                checkForUpdates();

            }

            @Override
            public void onProviderDisabled(@NonNull String provider) {

            }

            @Override
            public void onProviderEnabled(@NonNull String provider) {

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }
        };
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);


            if (lastKnownLocation != null) {
                centerMapOnLocation(lastKnownLocation);
                LatLng lastKnown = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastKnown, 16));

            }


        }
        // Add a marker in Sydney and move the camera

    }
}