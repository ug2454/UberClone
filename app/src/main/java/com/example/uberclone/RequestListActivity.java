package com.example.uberclone;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.model.LatLng;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class RequestListActivity extends AppCompatActivity {

    ListView requestListView;
    ArrayList<String> requestList = new ArrayList<>();
    LocationManager locationManager;
    LocationListener locationListener;
    static ArrayList<ParseGeoPoint> locations = new ArrayList<>();
    ArrayList<String> usernames = new ArrayList<>();

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
                Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
//                centerMapOnLocation(lastKnownLocation);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        if (item.getItemId() == R.id.logOut) {
            ParseUser.logOut();
            Intent in = new Intent(this, MainActivity.class);
            finish();
            startActivity(in);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_list);
        requestListView = findViewById(R.id.requestsListView);


        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                ParseUser.getCurrentUser().put("location",new ParseGeoPoint(location.getLatitude(),location.getLongitude()));
                ParseUser.getCurrentUser().saveInBackground(e -> {
                    if(e==null){
                        System.out.println("data saved");
                    }
                    else{
                        System.out.println(e.getMessage());
                    }
                });

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

                LatLng lastKnown = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());

            }


        }


        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, requestList);


        requestList.clear();
        requestList.add("Getting nearby Requests");
        requestListView.setAdapter(adapter);
        Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        ParseGeoPoint parseGeoPoint = new ParseGeoPoint(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
        ParseQuery<ParseObject> query = new ParseQuery<>("Request");
        query.whereNear("location", parseGeoPoint);
        query.whereDoesNotExist("driverUsername");
        query.setLimit(10);
        query.findInBackground((objects, e) -> {

            if (e == null) {
                requestList.clear();
                locations.clear();
                if (objects.size() > 0) {
                    for (ParseObject object : objects) {
//                         lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//                         parseGeoPoint = new ParseGeoPoint(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                        double distance = parseGeoPoint.distanceInKilometersTo(object.getParseGeoPoint("location"));
                        distance = distance * 1000;

                        DecimalFormat decimalFormat = new DecimalFormat("#,###.00");
                        String distanceString = decimalFormat.format(distance);

                        requestList.add(distanceString + " m");
                        usernames.add(object.getString("username"));
                        locations.add(object.getParseGeoPoint("location"));

                    }

                } else {
                    requestList.add("No active nearby Requests");
                }
                adapter.notifyDataSetChanged();

            }
        });

        requestListView.setOnItemClickListener((adapterView, view, i, l) -> {
            Intent in = new Intent(this, DriverLocationActivity.class);
            in.putExtra("placeLocation", i);
            in.putExtra("username", usernames.get(i));
            in.putExtra("currentLocationLat",lastKnownLocation.getLatitude());
            in.putExtra("currentLocationLon",lastKnownLocation.getLongitude());

            startActivity(in);
        });


    }
}