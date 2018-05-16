package earthquakeAlert;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Scanner;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.data.Feature;
import de.fhpotsdam.unfolding.data.GeoJSONReader;
import de.fhpotsdam.unfolding.data.PointFeature;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.marker.AbstractShapeMarker;
import de.fhpotsdam.unfolding.marker.Marker;
import de.fhpotsdam.unfolding.marker.MarkerManager;
import de.fhpotsdam.unfolding.marker.MultiMarker;
import de.fhpotsdam.unfolding.providers.OpenStreetMap.*;
import de.fhpotsdam.unfolding.providers.MBTilesMapProvider;
import de.fhpotsdam.unfolding.utils.MapUtils;
import processing.core.PApplet;
import processing.core.PFont;

/* 
 * An application with an interactive map displaying earthquake data.
 * @author D. Mou
 * */
public class EarthquakeCityMap extends PApplet {

	private static final long serialVersionUID = 1L;
	private String earthquakesURL;
	private String countryFile = "countries.geo.json";
	private UnfoldingMap map;
	private MarkerManager<Marker> earthQuakeManager;
	private List<Marker> quakeList;
	private List<Marker> quakePastHour;
	private List<Marker> quakeAlert;
	private Location location;
	private double range;
	private String earthquakeUpdateTime;
	private List<Marker> countryMarkers;
	private CommonMarker lastSelected;
	private CommonMarker lastClicked;

	public void setup() {
		// Initializing canvas and map tiles
		size(1200, 680, OPENGL);
		map = new UnfoldingMap(this, 355, 30, 820, 600, new OpenStreetMapProvider());
		map.setZoomRange(2, 10);
		map.zoomToLevel(2);
		MapUtils.createDefaultEventDispatcher(this, map);
		getAlertLocation();
		List<Feature> countries = GeoJSONReader.loadData(this, countryFile);
		countryMarkers = MapUtils.createSimpleMarkers(countries);
		earthquakesURL = "https://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/all_day.atom";
		updateEarthQuakeData();
		if (location != null) {
			map.addMarker(new AlertLocationMarker(location));
			map.zoomAndPanTo(4, location);
		}
	}

	public void draw() {
		// Draw on canvas
		background(230);
		if (frameCount % 6000 == 0) {
			// Update earthquake data every 10 min
			thread("updateEarthQuakeData");
		}
		map.draw();
		locationInfo();
		earthquakeInfo();
		if ((frameCount / 10) % 2 == 0) {
			// Flash alert very second
			AlertKey();
		}
	}

	/**
	 * Create input dialog to set alert location and range. Default location is at Seattle, WA and range at 500 miles.
	 */
	private void getAlertLocation() {
		JTextField latField = new JTextField("47.6");
		JTextField lonField = new JTextField("-122.5");
		JTextField rangeField = new JTextField("500");
		String message = "You can find your geographic coordinates on google map.\n Click cancel if you do not want alerting.";
		Object[] fields = { "Latitude:", latField, "Longitude:", lonField, "Distance Range (Miles):", rangeField, message};
		int result = JOptionPane.showConfirmDialog(null, fields, "Input Alert Location", JOptionPane.OK_CANCEL_OPTION);
		if (result == JOptionPane.OK_OPTION) {
			location = new Location(Double.valueOf(latField.getText()), Double.valueOf(lonField.getText()));
			range = Double.valueOf(rangeField.getText());
			range *= 1.60934;
		}
	}

	/**
	 * Fetch earthquake data and update markers in the map. Earthquake list are
	 * sorted with magnitude.
	 */
	public void updateEarthQuakeData() {
		List<PointFeature> earthquakes = ParseFeed.parseEarthquake(this, earthquakesURL);
		PriorityQueue<EarthquakeMarker> quakePQ = new PriorityQueue<>();
		PriorityQueue<EarthquakeMarker> pastHourPQ = new PriorityQueue<>();
		for (PointFeature feature : earthquakes) {
			setCountry(feature);
			EarthquakeMarker tempMaker = new EarthquakeMarker(feature);
			quakePQ.offer(tempMaker);
			String age = tempMaker.getStringProperty("age");
			if ("Past Hour".equals(age)) {
				pastHourPQ.offer(tempMaker);
			}
		}
		quakeList = new ArrayList<Marker>();
		quakePastHour = new ArrayList<Marker>();
		quakeAlert = new ArrayList<Marker>();
		while (quakePQ.size() > 0)
			quakeList.add(quakePQ.poll());
		while (pastHourPQ.size() > 0) {
			EarthquakeMarker tempMaker = pastHourPQ.poll();
			quakePastHour.add(tempMaker);
			if (location != null && tempMaker.getDistanceTo(location) < range) {
				quakeAlert.add(tempMaker);
				tempMaker.setAlert();
			}
		}
		if (earthQuakeManager != null) {
			map.removeMarkerManager(earthQuakeManager);
		}
		earthQuakeManager = new MarkerManager<>(quakeList);
		map.addMarkerManager(earthQuakeManager);
		earthquakeUpdateTime = LocalDateTime.now().toString();
		System.out.println("earthquake data updated");
	}

	/**
	 * Display earthquake information happened in past hour and past day.
	 */
	private void earthquakeInfo() {
		int xbase = 25;
		int ybase = 95;
		int yWidth = 25;
		int hourListNum = 5;
		int topListNum = 11;
		PFont font1, font2;
		font1 = createFont("Arial Bold", 12);
		font2 = createFont("Arial", 12);

		fill(0);
		textFont(font2);
		textAlign(LEFT, CENTER);
		textSize(14);
		String update = "(updated: " + earthquakeUpdateTime + ")";
		text(update, xbase + 10, ybase + yWidth);

		ybase += 45;

		fill(255, 255, 255);
		rect(xbase, ybase, 310, yWidth * (hourListNum + topListNum + 4));

		int xmag = xbase + 20;
		int xdep = xmag + 70;
		int xcou = xdep + 70;

		fill(0, 200, 0);
		int circleSize = 10;
		rect(xbase + 85, ybase + yWidth - 14, circleSize, circleSize);

		fill(0, 200, 0);
		textFont(font2);
		textAlign(LEFT, CENTER);
		textSize(16);
		text("Past Hour", xbase + 100, ybase + yWidth - 10);
		line(xbase + 10, ybase + yWidth + 3, xbase + 295, ybase + yWidth + 3);

		int yHourStart = ybase + yWidth + 15;
		textFont(font2);
		fill(0);
		textAlign(LEFT, CENTER);
		textSize(14);
		text("Magnitude", xmag, yHourStart);

		fill(0);
		textAlign(LEFT, CENTER);
		textSize(14);
		text("Depth", xdep, yHourStart);

		fill(0);
		textAlign(LEFT, CENTER);
		textSize(14);
		text("Country", xcou, yHourStart);

		for (int i = 0; i < hourListNum; i++) {
			EarthquakeMarker currQuake = (EarthquakeMarker) quakePastHour.get(i);

			fill(0);
			textAlign(LEFT);
			textSize(12);
			text(currQuake.getMagnitude(), xmag, yHourStart + yWidth * (i + 1));

			fill(0);
			textAlign(LEFT);
			textSize(12);
			text(currQuake.getDepth(), xdep, yHourStart + yWidth * (i + 1));

			fill(0);
			textAlign(LEFT);
			textSize(12);
			text(currQuake.getCountry(), xcou, yHourStart + yWidth * (i + 1));
		}

		ybase = yHourStart + yWidth * (hourListNum);

		fill(0, 0, 200);
		ellipse(xbase + 90, ybase + yWidth + 1, circleSize, circleSize);

		fill(0, 0, 200);
		textFont(font2);
		textAlign(LEFT, CENTER);
		textSize(16);
		text("Past Day", xbase + 100, ybase + yWidth);
		line(xbase + 10, ybase + yWidth + 15, xbase + 295, ybase + yWidth + 15);

		int yQuakeStart = ybase + yWidth + 25;
		textFont(font2);
		fill(0);
		textAlign(LEFT, CENTER);
		textSize(14);
		text("Magnitude", xmag, yQuakeStart);

		fill(0);
		textAlign(LEFT, CENTER);
		textSize(14);
		text("Depth", xdep, yQuakeStart);

		fill(0);
		textAlign(LEFT, CENTER);
		textSize(14);
		text("Country", xcou, yQuakeStart);

		for (int i = 0; i < topListNum; i++) {
			EarthquakeMarker currQuake = (EarthquakeMarker) quakeList.get(i);

			fill(0);
			textAlign(LEFT);
			textSize(12);
			text(currQuake.getMagnitude(), xmag, yQuakeStart + yWidth * (i + 1));

			fill(0);
			textAlign(LEFT);
			textSize(12);
			text(currQuake.getDepth(), xdep, yQuakeStart + yWidth * (i + 1));

			fill(0);
			textAlign(LEFT);
			textSize(12);
			text(currQuake.getCountry(), xcou, yQuakeStart + yWidth * (i + 1));
		}

	}

	/**
	 * Display alert location information.
	 */
	private void locationInfo() {
		PFont font1, font2;
		font1 = createFont("Arial Bold", 12);
		font2 = createFont("Arial", 12);

		fill(255, 255, 255);
		rect(25, 25, 310, 65);

		int size = 7;
		float centerx = 40, centery = 38;
		float x1 = centerx, y1 = centery - size;
		float x2 = (float) (centerx - size * Math.sqrt(3) / 2), y2 = centery + size / 2;
		float x3 = (float) (centerx + size * Math.sqrt(3) / 2), y3 = centery + size / 2;
		fill(255, 0, 0);
		triangle(x1, y1, x2, y2, x3, y3);

		fill(50);
		textAlign(LEFT, CENTER);
		textFont(font1);
		textSize(14);
		text("your location:", 50, 35);
		textFont(font2);
		textSize(14);
		String lat = "Latitue: ";
		if (location != null)
			lat += location.getLat();
		String lon = "Longitute: ";
		if (location != null)
			lon += location.getLon();
		text(lat, 40, 55);
		text(lon, 40, 75);
	}

	/**
	 * Display alert massage if an earthquake happened in the past hour and within
	 * the range.
	 */
	private void AlertKey() {
		PFont font1, font2;
		font1 = createFont("Arial Bold", 12);
		font2 = createFont("Arial", 12);
		if (quakeAlert.size() > 0) {
			int xbase = 400;
			int ybase = 645;
			textFont(font2);
			fill(200, 0, 0);
			textAlign(LEFT, CENTER);
			textSize(18);
			EarthquakeMarker alertQuake = (EarthquakeMarker) quakeAlert.get(0);
			String mag = String.format(java.util.Locale.US, "%.1f", alertQuake.getMagnitude());
			String alert = "Alert! An earthquake happened near you with magnitute of " + mag + " !";
			text(alert, xbase, ybase);
		}
	}

	/**
	 * Checks whether this quake occurred on land.
	 * 
	 * @param earthquake
	 */
	private void setCountry(PointFeature earthquake) {
		for (Marker country : countryMarkers) {
			if (isInCountry(earthquake, country)) {
				return;
			}
		}
	}

	/**
	 * test whether a given earthquake is in a given country
	 * 
	 * @param earthquake
	 * @param country
	 * @return
	 */
	private boolean isInCountry(PointFeature earthquake, Marker country) {
		Location checkLoc = earthquake.getLocation();

		if (country.getClass() == MultiMarker.class) {
			for (Marker marker : ((MultiMarker) country).getMarkers()) {
				if (((AbstractShapeMarker) marker).isInsideByLocation(checkLoc)) {
					earthquake.addProperty("country", country.getProperty("name"));
					return true;
				}
			}
		} else if (((AbstractShapeMarker) country).isInsideByLocation(checkLoc)) {
			earthquake.addProperty("country", country.getProperty("name"));
			return true;
		}
		return false;
	}

	/**
	 * Mouse move event handler
	 */
	@Override
	public void mouseMoved() {
		// clear the last selection
		if (lastSelected != null) {
			lastSelected.setSelected(false);
			lastSelected = null;
		}
		selectMarkerIfHover(quakeList);
	}

	/**
	 * Select the marker that mouse hovers on.
	 * 
	 * @param markers
	 */
	private void selectMarkerIfHover(List<Marker> markers) {
		if (lastSelected != null) {
			return;
		}
		for (Marker m : markers) {
			CommonMarker marker = (CommonMarker) m;
			if (marker.isInside(map, mouseX, mouseY)) {
				lastSelected = marker;
				marker.setSelected(true);
				return;
			}
		}
	}

	/**
	 * The event handler for mouse clicks
	 */
	@Override
	public void mouseClicked() {
		if (lastClicked != null) {
			unhideMarkers();
			lastClicked = null;
		} else if (lastClicked == null) {
			checkEarthquakesForClick();
		}
	}

	/**
	 * Helper method that will check if an earthquake marker was clicked on
	 */
	private void checkEarthquakesForClick() {
		if (lastClicked != null)
			return;
		for (Marker m : quakeList) {
			EarthquakeMarker marker = (EarthquakeMarker) m;
			if (!marker.isHidden() && marker.isInside(map, mouseX, mouseY)) {
				lastClicked = marker;
				for (Marker mhide : quakeList) {
					if (mhide != lastClicked) {
						mhide.setHidden(true);
					}
				}
				return;
			}
		}
	}

	/**
	 * Loop over and unhide all markers
	 */
	private void unhideMarkers() {
		for (Marker marker : quakeList) {
			marker.setHidden(false);
		}
	}

}
