package com.augtech.geopackage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.opengis.feature.simple.SimpleFeature;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.augtech.geoapi.feature.FeatureCollection;
import com.augtech.geoapi.geometry.BoundingBoxImpl;
import com.augtech.geoapi.geopackage.GeoPackage;
import com.augtech.geoapi.referncing.CoordinateReferenceSystemImpl;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

public class GoogleMapsActivity extends FragmentActivity implements
		OnMapReadyCallback {
	
	// declare map and bounds
	GoogleMap myMap = null;
	LatLngBounds bounds;
	
	String gpkgName = "ndby6";
	String datasetNumber = "6";

	// get the files from filesystem
	// TODO get file via Filechooser
	File testDir = getDirectory("GeoPackageTest");
	String testDirstr = testDir.toString();
	String testdb = "ndby" + datasetNumber + ".gpkg";

	
	//declare variables for the GeoPackage file
	private boolean overWrite = false;
	FeatureCollection featureCollection = null;
	GeoPackage geoPackage = null;
	AndroidSQLDatabase gpkgDB = new AndroidSQLDatabase(this, new File(testDir,
			testdb));
	
	// lists holding the loaded features
	List<Marker> gpkgMarkerList = new ArrayList<Marker>();
	List<Polyline> gpkgPolylineList = new ArrayList<Polyline>();
	List<Polygon> gpkgPolygonList = new ArrayList<Polygon>();
	List<Marker> jsonMarkerList = new ArrayList<Marker>();
	List<Polyline> jsonPolylineList = new ArrayList<Polyline>();
	List<Polygon> jsonPolygonList = new ArrayList<Polygon>();
	List<Marker> shpMarkerList = new ArrayList<Marker>();
	List<Polyline> shpPolylineList = new ArrayList<Polyline>();
	List<Polygon> shpPolygonList = new ArrayList<Polygon>();
	List<Marker> gmlMarkerList = new ArrayList<Marker>();
	List<Polyline> gmlPolylineList = new ArrayList<Polyline>();
	List<Polygon> gmlPolygonList = new ArrayList<Polygon>();
	
	long heapSize = Runtime.getRuntime().maxMemory();
	

// helper method to be able to find the directory "GeoPackageTest"
	public static File getDirectory(String directory) {
		if (directory == null)
			return null;
		String path = Environment.getExternalStorageDirectory().toString();
		path += directory.startsWith("/") ? "" : "/";
		path += directory.endsWith("/") ? directory : directory + "/";
		File file = new File(path);
		file.mkdirs();
		return file;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.googlemapslayout);
		MapFragment mapFragment = (MapFragment) getFragmentManager()
				.findFragmentById(R.id.map);
		mapFragment.getMapAsync(this);

		myMap = mapFragment.getMap();



		// GeoPackage Checkboxes
		CheckBox pointsChk = (CheckBox) findViewById(R.id.pointschk);
		pointsChk.setOnClickListener(loadPointsCheck);

		CheckBox linesChk = (CheckBox) findViewById(R.id.lineschk);
		linesChk.setOnClickListener(loadLinesCheck);

		CheckBox polygonChk = (CheckBox) findViewById(R.id.polygonschk);
		polygonChk.setOnClickListener(loadPolygonsCheck);
		

		
		// Reload Button (clears map and reloads the GeoPackage)
		Button reloadBtn = (Button) findViewById(R.id.reloadbtn);
		reloadBtn.setOnClickListener(reloadMap);
		
		Button saveButton = (Button)findViewById(R.id.saveBtn);
		final EditText filenameTxt = (EditText)findViewById(R.id.editText1);
		
		saveButton.setOnClickListener(
		        new View.OnClickListener()
		        {
		            public void onClick(View view)
		            {
		                System.out.println("EditText: "+ filenameTxt.getText().toString());
		                datasetNumber = filenameTxt.getText().toString();
		            }
		        });
		
	}

	@Override
	public void onMapReady(GoogleMap map) {
		CameraUpdate center = CameraUpdateFactory.newLatLngZoom(new LatLng(
				48.6033808, 12.87626895), 10);
		map.moveCamera(center);

		map.setOnCameraChangeListener(new OnCameraChangeListener() {
			@Override
			public void onCameraChange(CameraPosition position) {
				bounds = myMap.getProjection().getVisibleRegion().latLngBounds;
			}
		});
	};

// Listeners for the GeoPackage Checkboxes
	View.OnClickListener loadPointsCheck = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			if (((CheckBox) v).isChecked()) {
				loadPoints(myMap, true);
				System.out.println(gpkgName);
			} else {
				//myMap.clear();
				loadPoints(myMap, false);
			}
		}
	};

	View.OnClickListener loadLinesCheck = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			if (((CheckBox) v).isChecked()) {
				loadLines(myMap, true);
			} else {

				loadLines(myMap, false);
			}
		}
	};

	View.OnClickListener loadPolygonsCheck = new View.OnClickListener() {

		@Override
		public void onClick(View v) {

			if (((CheckBox) v).isChecked()) {
				loadPolygons(myMap, true);
			} else {

				loadPolygons(myMap, false);
			}

		}
	};


	

	

	
// Reload Button
	View.OnClickListener reloadMap = new View.OnClickListener() {

		@Override
		public void onClick(View v) {

			myMap.clear();

			loadPoints(myMap,
					((CheckBox) findViewById(R.id.pointschk)).isChecked());
			loadLines(myMap,
					((CheckBox) findViewById(R.id.lineschk)).isChecked());
			loadPolygons(myMap,
					((CheckBox) findViewById(R.id.polygonschk)).isChecked());
	
		}
	};


	
//Methods for reading GeoPackages
	public void loadPoints(GoogleMap map, boolean value) {
		long startTime = System.currentTimeMillis();
		testdb = "ndby" + datasetNumber + ".gpkg";
		gpkgDB = new AndroidSQLDatabase(this, new File(testDir,
				testdb));
		// Declare the needed variables
		geoPackage = new GeoPackage(gpkgDB, overWrite);
		List<SimpleFeature> feats = null;
		SimpleFeature feature = null;
		LatLng coords = null;
		Coordinate[] coordlist = null;
		Object attributename = null;
		// Object osmID = null;

		// Read the coordinates of the current view from "bounds" and set them
		// to bbox
		CoordinateReferenceSystemImpl crs = new CoordinateReferenceSystemImpl(
				"4326");
		BoundingBoxImpl bbox1 = new BoundingBoxImpl(bounds.southwest.longitude,
				bounds.northeast.longitude, bounds.southwest.latitude,
				bounds.northeast.latitude, crs);
		// value indicates if the checkbox was checked or not. If not, do
		// nothing. If yes, read the geopackage.
		if (value == false) {
			try {
				if (gpkgMarkerList.isEmpty()){
					System.out.println("the Markerlist was empty");
				}
				for (Marker mark : gpkgMarkerList){
					mark.remove();
				}
				gpkgMarkerList.clear();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			try {
				// read the feature of the "points" layer of the geopackage
				// TODO read the layername automatically from the geopackage
				feats = geoPackage.getFeatures("points", bbox1);
				System.out.println("features were queried in " + (System.currentTimeMillis()-startTime)+ " millis");
			} catch (Exception e) {
				System.out
						.println("Reading features from the geopackage failed. Probably no 'points' layer.");
				e.printStackTrace();
			}
			for (int i = 0; i < feats.size(); i++) {
				feature = feats.get(i);
				attributename = feature.getAttribute("name");
				Geometry geom = (Geometry) feature.getDefaultGeometry();
				coordlist = geom.getCoordinates();
				coords = new LatLng(coordlist[0].y, coordlist[0].x);
				Marker pointMarker = map.addMarker(new MarkerOptions().position(coords)
				// .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
						.title((String) attributename));
				gpkgMarkerList.add(pointMarker);
			}
			
		}

		//CameraUpdate center = CameraUpdateFactory.newLatLngBounds(bounds, 0);
		//myMap.moveCamera(center);
		System.out.println("features were visualized in " + (System.currentTimeMillis()-startTime)+ " millis");
		System.out.println(heapSize);
		Toast.makeText(getApplicationContext(), "features were visualized in " + (System.currentTimeMillis()-startTime)+ " millis", Toast.LENGTH_SHORT).show();

	}

	public void loadLines(GoogleMap map, boolean value) {
		long startTime = System.currentTimeMillis();
		testdb = "ndby" + datasetNumber + ".gpkg";
		gpkgDB = new AndroidSQLDatabase(this, new File(testDir,
				testdb));
		geoPackage = new GeoPackage(gpkgDB, overWrite);
		List<SimpleFeature> feats = null;
		SimpleFeature feature = null;
		LatLng coords = null;

		Coordinate[] coordlist = null;

		// Read the coordinates of the current view from "bounds" and set them
		// to bbox
		CoordinateReferenceSystemImpl crs = new CoordinateReferenceSystemImpl(
				"4326");
		BoundingBoxImpl bbox1 = new BoundingBoxImpl(bounds.southwest.longitude,
				bounds.northeast.longitude, bounds.southwest.latitude,
				bounds.northeast.latitude, crs);
		// value indicates if the checkbox was checked or not. If not, do
		// nothing. If yes, read the geopackage.
		if (value == false) {

				try {
					if (gpkgPolylineList.isEmpty()){
						System.out.println("the polylinelist was empty");
					}
					for (Polyline polyl : gpkgPolylineList){
						polyl.remove();
					}
					gpkgPolylineList.clear();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		

		} else {
			try {
				feats = geoPackage.getFeatures("lines", bbox1);
				System.out.println("features were queried in " + (System.currentTimeMillis()-startTime)+ " millis");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			for (int i = 0; i < feats.size(); i++) {
				feature = feats.get(i);
				// attributename = feature.getAttribute("name");
				// osmID = feature.getAttribute("osm_id");
				Geometry geom = (Geometry) feature.getDefaultGeometry();
				coordlist = geom.getCoordinates();
				PolylineOptions lineOpts = new PolylineOptions();
				for (int j = 0; j < coordlist.length; j++) {
					coords = new LatLng(coordlist[j].y, coordlist[j].x);
					lineOpts.add(coords).width(3).color(Color.rgb(115, 79, 127));
				}
				Polyline polyline = map.addPolyline(lineOpts);
				gpkgPolylineList.add(polyline);

			}
		}
		System.out.println("features were visualized in " + (System.currentTimeMillis()-startTime)+ " millis");
		Toast.makeText(getApplicationContext(), "features were visualized in " + (System.currentTimeMillis()-startTime)+ " millis", Toast.LENGTH_SHORT).show();
		 

	}

	public void loadPolygons(GoogleMap map, boolean value) {
		long startTime = System.currentTimeMillis();
		testdb = "ndby" + datasetNumber + ".gpkg";
		gpkgDB = new AndroidSQLDatabase(this, new File(testDir,
				testdb));
		geoPackage = new GeoPackage(gpkgDB, overWrite);
		List<SimpleFeature> feats = null;
		SimpleFeature feature = null;
		LatLng coords = null;
		Coordinate[] coordlist = null;

		// Read the coordinates of the current view from "bounds" and set them to bbox
		CoordinateReferenceSystemImpl crs = new CoordinateReferenceSystemImpl(
				"4326");
		BoundingBoxImpl bbox1 = new BoundingBoxImpl(bounds.southwest.longitude,
				bounds.northeast.longitude, bounds.southwest.latitude,
				bounds.northeast.latitude, crs);
		// "value" indicates if the checkbox was checked or not. If not, and the list wasn't empty, then empty it. If yes, read the geopackage.
		if (value == false) {
			try {
				if (gpkgPolygonList.isEmpty()){
					System.out.println("the polygonlist was empty");
				}
				for (Polygon polyg : gpkgPolygonList){
					polyg.remove();
				}
				gpkgPolygonList.clear();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {

			try {
				feats = geoPackage.getFeatures("multipolygons", bbox1);
				System.out.println("features were queried in " + (System.currentTimeMillis()-startTime)+ " millis");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			for (int i = 0; i < feats.size(); i++) {
				feature = feats.get(i);
				// attributename = feature.getAttribute("name");
				// osmID = feature.getAttribute("osm_id");
				Geometry geom = (Geometry) feature.getDefaultGeometry();
				coordlist = geom.getCoordinates();
				PolygonOptions polyOpts = new PolygonOptions();

				for (int j = 0; j < coordlist.length; j++) {
					coords = new LatLng(coordlist[j].y, coordlist[j].x);
					polyOpts.add(coords).strokeWidth(1)
							.fillColor(Color.argb(150, 115, 79, 127));
				}
				Polygon polygon = map.addPolygon(polyOpts);
				gpkgPolygonList.add(polygon);
			}
		}
		System.out.println("features were visualized in " + (System.currentTimeMillis()-startTime)+ " millis");
		Toast.makeText(getApplicationContext(), "features were visualized in " + (System.currentTimeMillis()-startTime)+ " millis", Toast.LENGTH_SHORT).show();
	}

	

}