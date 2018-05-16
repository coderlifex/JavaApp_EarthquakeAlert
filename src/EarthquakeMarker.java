package earthquakeAlert;

import de.fhpotsdam.unfolding.data.PointFeature;
import processing.core.PConstants;
import processing.core.PGraphics;

/**
 * A visual marker for earthquakes on an earthquake map
 *
 */
public class EarthquakeMarker extends CommonMarker implements Comparable<EarthquakeMarker> {
	protected boolean isOnLand;
	protected float radius;
	protected String country;
	protected boolean alert = false;

	public EarthquakeMarker(PointFeature feature) {
		super(feature.getLocation());
		java.util.HashMap<String, Object> properties = feature.getProperties();
		float magnitude = Float.parseFloat(properties.get("magnitude").toString());
		properties.put("radius", 2 * magnitude);
		setProperties(properties);
		this.radius = 1.75f * getMagnitude();
		if (properties.containsKey("country")) {
			isOnLand = true;
			country = (String) properties.get("country");
		} else {
			country = "Ocean";
		}
	}

	public int compareTo(EarthquakeMarker marker) {
		Float f1 = new Float(this.getMagnitude());
		Float f2 = new Float(marker.getMagnitude());
		return -1 * f1.compareTo(f2);
	}

	@Override
	public void drawMarker(PGraphics pg, float x, float y) {
		pg.pushStyle();

		String age = getStringProperty("age");
		if ("Past Hour".equals(age)) {
			if (alert) {
				pg.fill(255, 0, 0);
			} else {
				pg.fill(0, 200, 0);
			}
			int circleSize = 10;
			pg.rect(x, y, circleSize, circleSize);

		} else {
			pg.fill(0, 0, 200);
			pg.ellipse(x, y, 2 * radius, 2 * radius);
		}

		pg.popStyle();
	}


	/**
	 *  Show the title of the earthquake if this marker is selected
	 */
	public void showTitle(PGraphics pg, float x, float y) {
		String title = getTitle();
		pg.pushStyle();

		pg.rectMode(PConstants.CORNER);

		pg.stroke(110);
		pg.fill(255, 255, 255);
		pg.rect(x, y + 15, pg.textWidth(title) + 6, 18, 5);

		pg.textAlign(PConstants.LEFT, PConstants.TOP);
		pg.fill(0);
		pg.text(title, x + 3, y + 18);

		pg.popStyle();

	}

	public double threatCircle() {
		double miles = 20.0f * Math.pow(1.8, 2 * getMagnitude() - 5);
		double km = (miles * 1.6);
		return km;
	}

	public String toString() {
		return getTitle();
	}

	public float getMagnitude() {
		return Float.parseFloat(getProperty("magnitude").toString());
	}

	public String getCountry() {
		return country;
	}

	public float getDepth() {
		return Float.parseFloat(getProperty("depth").toString());
	}

	public String getTitle() {
		return (String) getProperty("title");

	}

	public float getRadius() {
		return Float.parseFloat(getProperty("radius").toString());
	}

	public boolean isOnLand() {
		return isOnLand;
	}

	public void setAlert() {
		alert = true;
	}
}
