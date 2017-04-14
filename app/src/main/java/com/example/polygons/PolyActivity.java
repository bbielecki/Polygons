package com.example.polygons;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CustomCap;
import com.google.android.gms.maps.model.Dash;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import static com.example.polygons.R.id.map;


/**
 * An activity that displays a Google map with polylines to represent paths or routes,
 * and polygons to represent areas.
 */
public class PolyActivity extends AppCompatActivity
        implements
                OnMapReadyCallback,
                GoogleMap.OnPolylineClickListener,
                GoogleMap.OnPolygonClickListener {

    private static final int COLOR_BLACK_ARGB = 0xff000000;
    private static final int COLOR_WHITE_ARGB = 0xffffffff;
    private static final int COLOR_GREEN_ARGB = 0xff388E3C;
    private static final int COLOR_PURPLE_ARGB = 0xff81C784;
    private static final int COLOR_ORANGE_ARGB = 0xffF57F17;
    private static final int COLOR_BLUE_ARGB = 0xffF9A825;

    private static final int POLYLINE_STROKE_WIDTH_PX = 12;
    private static final int POLYGON_STROKE_WIDTH_PX = 8;
    private static final int PATTERN_DASH_LENGTH_PX = 20;
    private static final int PATTERN_GAP_LENGTH_PX = 20;
    private static final PatternItem DOT = new Dot();
    private static final PatternItem DASH = new Dash(PATTERN_DASH_LENGTH_PX);
    private static final PatternItem GAP = new Gap(PATTERN_GAP_LENGTH_PX);
    private static final int MY_LOCATION_PERMISSION = 0;
    private static final String ROUTE_MODE_MESSAGE = "Touch on map where you want to \n place start and end point for route";
    private int clicCounter = 0;

    // Create a stroke pattern of a gap followed by a dot.
    private static final List<PatternItem> PATTERN_POLYLINE_DOTTED = Arrays.asList(GAP, DOT);

    // Create a stroke pattern of a gap followed by a dash.
    private static final List<PatternItem> PATTERN_POLYGON_ALPHA = Arrays.asList(GAP, DASH);

    // Create a stroke pattern of a dot followed by a gap, a dash, and another gap.
    private static final List<PatternItem> PATTERN_POLYGON_BETA =
            Arrays.asList(DOT, GAP, DASH, GAP);

    //Line on a map made from LatLngs objects (points)
    private Polyline polyline;
    private Vector<LatLng> routeLatLng;

    //Point on map describing user presence
    private LatLng pointOfPresence;
    //Any point to be printed on map
    private LatLng pointOnMap;
    // reference to googlemap
    private GoogleMap myMap;
    //allow to recognize route creation mode
    private boolean routeModeOn = false;


    //interface of GoogleMaps for our application
    //Create poliline from latlngs: objects representing google maps coordinates
    public void createRoute(Vector<LatLng> latLngs) {
        routeLatLng = latLngs;

    }

    public void setPointOnMap(LatLng pointOnMap) {
        this.pointOnMap = pointOnMap;
    }

    //TODO: uzupelnij
    public LatLng getLocation() {
        return new LatLng(0, 0);
    }

    public void setLocation(LatLng location) {
        this.pointOfPresence = location;
    }

    // delete marker indicating position of user
    public void deleteLocationPoint() {
        pointOfPresence = null;
    }

    // delete settled marker
    public void deletePointOnMap() {
        pointOnMap = null;
    }
    //quite unusefull method
    public void drawPolilineMode() {
        myMap.setContentDescription(ROUTE_MODE_MESSAGE);

    }
    //enable user to create his own route
    public void setRouteModeOn(){
        routeModeOn = true;
    }
    //disable user ability to creating own route
    public void setRouteModeOff(){
        routeModeOn = false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Retrieve the content view that renders the map.
        setContentView(R.layout.activity_maps);
        //myLocationListener = new MyLocationListener(PolyActivity.this);

        // Get the SupportMapFragment and request notification when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(map);
        mapFragment.getMapAsync(this);

        //some trenning point in Australia ;)
        routeLatLng = new Vector<>();
        routeLatLng.add(new LatLng(-27.457, 153.040));
        routeLatLng.add(new LatLng(-33.852, 151.211));
        routeLatLng.add(new LatLng(-37.813, 144.9620));
        routeLatLng.add(new LatLng(-34.928, 138.599));

        CommunicationLayer communicationLayer = new CommunicationLayer();
        communicationLayer.registerPolyActivity(this);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CONTACTS)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_LOCATION_PERMISSION);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_LOCATION_PERMISSION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    /**
     * Manipulates the map when it's available.
     * The API invokes this callback when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
     * In this tutorial, we add polylines and polygons to represent routes and areas on the map.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {

        myMap = googleMap;
        // Add polylines to the map.
        // Polylines are useful to show a route or some other connection between points.
        Vector<Polyline> polylines = new Vector<>();
        if (!routeLatLng.isEmpty()) {
            for (int i = 0; i < routeLatLng.size() - 1; ++i)
                polylines.add(googleMap.addPolyline(new PolylineOptions()
                        .clickable(true)
                        .add(routeLatLng.get(i), routeLatLng.get(i + 1))));

            for (Polyline p : polylines) {
                // Store a data object with the polyline, used here to indicate an arbitrary type.
                p.setTag("A");
                // Style the polyline.
                stylePolyline(p);
            }

        }
        // Position the map's camera near Warsaw
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(52.2297700, 21.0117800), 4));

        // Set listeners for click events.
        googleMap.setOnPolylineClickListener(this);
        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                marker.remove();
                //TODO: jakas madra obsluga znacznikow-> usuwanie co drugi, przenoszenie znacznika itd

                return false;
            }
        });
        googleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                if(routeModeOn){
                    if(clicCounter==0 ){
                        routeLatLng.add(latLng);
                        clicCounter++;
                    }else {
                        routeLatLng.add(latLng);
                        new GetDirection().execute();

                        routeLatLng.clear();
                        clicCounter=0;
                    }
                }else {
                    pointOnMap = latLng;
                    drawMarkerOnMap();
                }
            }
        });
    }


    private void drawMarkerOnMap() {
        if (pointOnMap != null) {
            myMap.addMarker(new MarkerOptions().position(pointOnMap));
            myMap.moveCamera(CameraUpdateFactory.newLatLng(pointOnMap));
        }

        if (pointOfPresence != null) {
            myMap.addMarker(new MarkerOptions().position(pointOfPresence).title("tu jestem!"));
            myMap.moveCamera(CameraUpdateFactory.newLatLng(pointOfPresence));
        }
    }

    /**
     * Styles the polyline, based on type.
     *
     * @param polyline The polyline object that needs styling.
     */
    private void stylePolyline(Polyline polyline) {
        String type = "";
        // Get the data object stored with the polyline.
        if (polyline.getTag() != null) {
            type = polyline.getTag().toString();
        }

        switch (type) {
            // If no type is given, allow the API to use the default.
            case "A":
                // Use a custom bitmap as the cap at the start of the line.
                polyline.setStartCap(
                        new CustomCap(
                                BitmapDescriptorFactory.fromResource(R.drawable.ic_arrow), 10));
                break;
            case "B":
                // Use a round cap at the start of the line.
                polyline.setStartCap(new RoundCap());
                break;
        }

        polyline.setEndCap(new RoundCap());
        polyline.setWidth(POLYLINE_STROKE_WIDTH_PX);
        polyline.setColor(COLOR_BLACK_ARGB);
        polyline.setJointType(JointType.ROUND);
    }

    /**
     * Listens for clicks on a polyline.
     *
     * @param polyline The polyline object that the user has clicked.
     */
    @Override
    public void onPolylineClick(Polyline polyline) {
        // Flip from solid stroke to dotted stroke pattern.
        if ((polyline.getPattern() == null) || (!polyline.getPattern().contains(DOT))) {
            polyline.setPattern(PATTERN_POLYLINE_DOTTED);
        } else {
            // The default pattern is a solid stroke.
            polyline.setPattern(null);
        }

        Toast.makeText(this, "Route type " + polyline.getTag().toString(),
                Toast.LENGTH_SHORT).show();

        //       Location location = myLocationListener.getLocation();

    }

    /**
     * Listens for clicks on a polygon.
     *
     * @param polygon The polygon object that the user has clicked.
     */
    @Override
    public void onPolygonClick(Polygon polygon) {
        // Flip the values of the red, green, and blue components of the polygon's color.
        int color = polygon.getStrokeColor() ^ 0x00ffffff;
        polygon.setStrokeColor(color);
        color = polygon.getFillColor() ^ 0x00ffffff;
        polygon.setFillColor(color);

        Toast.makeText(this, "Area type " + polygon.getTag().toString(), Toast.LENGTH_SHORT).show();
    }


    private class GetDirection extends AsyncTask<String, String, String> {

        private ProgressDialog dialog;
        private List<LatLng> pontos = new ArrayList<>();
        private String origin = "Chicago,IL";
        private String destination = "Los Angeles,CA";

        public void setDestination(String destinationLat, String destinationLng) {
            destination = destinationLat+","+destinationLng;
        }

        public void setOrigin(String originLat, String originLng) {
            origin = originLat+","+originLng;
        }


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(PolyActivity.this);
            dialog.setMessage("Drawing the route, please wait!");
            dialog.setIndeterminate(false);
            dialog.setCancelable(false);
            dialog.show();

            destination = routeLatLng.lastElement().latitude+","+routeLatLng.lastElement().longitude;
            origin = routeLatLng.get(routeLatLng.size()-2).latitude+","+routeLatLng.get(routeLatLng.size()-2).longitude;
        }

        protected String doInBackground(String... args) {
            String stringUrl = "http://maps.googleapis.com/maps/api/directions/json?origin=" + origin + "&destination=" + destination + "&sensor=false";
            StringBuilder response = new StringBuilder();
            try {
                URL url = new URL(stringUrl);
                HttpURLConnection httpconn = (HttpURLConnection) url.openConnection();
                if (httpconn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    BufferedReader input = new BufferedReader(new InputStreamReader(httpconn.getInputStream()), 8192);
                    String strLine = null;

                    while ((strLine = input.readLine()) != null) {
                        response.append(strLine);
                    }
                    input.close();
                }

                String jsonOutput = response.toString();

                JSONObject jsonObject = new JSONObject(jsonOutput);

                // routesArray contains ALL routes
                JSONArray routesArray = jsonObject.getJSONArray("routes");
                // Grab the first route
                JSONObject route = routesArray.getJSONObject(0);

                JSONObject poly = route.getJSONObject("overview_polyline");
                String polyline = poly.getString("points");
                pontos = decodePoly(polyline);

            } catch (Exception e) {
                dialog.setMessage("siec..");
                dialog.show();
            }

            return null;

        }

        protected void onPostExecute(String file_url) {
            for (int i = 0; i < pontos.size() - 1; i++) {
                LatLng src = pontos.get(i);
                LatLng dest = pontos.get(i + 1);
                try {
                    //here is where it will draw the polyline in your map
                    Polyline line = myMap.addPolyline(new PolylineOptions().add(new LatLng(src.latitude, src.longitude),
                            new LatLng(dest.latitude, dest.longitude)).width(2).color(Color.RED).geodesic(true));
                } catch (NullPointerException e) {
                    Log.e("Error", "NullPointerException onPostExecute: " + e.toString());
                } catch (Exception e2) {
                    Log.e("Error", "Exception onPostExecute: " + e2.toString());
                }

                if(i==pontos.size()-2){
                    myMap.moveCamera(CameraUpdateFactory.newLatLng(pontos.get(i)));
                }
            }
            dialog.dismiss();

        }
    }

    private List<LatLng> decodePoly(String encoded) {

        List<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }
}