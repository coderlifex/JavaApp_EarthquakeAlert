package EarthquakeMap;

import de.fhpotsdam.unfolding.data.PointFeature;
import processing.core.PConstants;
import processing.core.PGraphics;

/*
 * A visual marker for earthquakes on an earthquake map
 *
 */
public abstract class EarthquakeMarker extends CommonMarker implements Comparable<EarthquakeMarker>
{
	protected boolean isOnLand;
	protected float radius;
	
	protected static final float kmPerMile = 1.6f;
	
	//Moderate earthquake threshold
	public static final float THRESHOLD_MODERATE = 5;
	//Light earthquake threshold
	public static final float THRESHOLD_LIGHT = 4;

	//Intermediate depth threshold
	public static final float THRESHOLD_INTERMEDIATE = 70;
	//Deep depth threshold
	public static final float THRESHOLD_DEEP = 300;

	public abstract void drawEarthquake(PGraphics pg, float x, float y);

	public EarthquakeMarker (PointFeature feature) 
	{
		super(feature.getLocation());
		java.util.HashMap<String, Object> properties = feature.getProperties();
		float magnitude = Float.parseFloat(properties.get("magnitude").toString());
		properties.put("radius", 2*magnitude );
		setProperties(properties);
		this.radius = 1.75f*getMagnitude();
	}

	 public int compareTo(EarthquakeMarker marker){
		 Float f1= new Float(this.getMagnitude());
		 Float f2= new Float(marker.getMagnitude());
		 return -1*f1.compareTo(f2);
	 }
	 
	@Override
	public void drawMarker(PGraphics pg, float x, float y) {
		pg.pushStyle();
		colorDetermine(pg);
		
		drawEarthquake(pg, x, y);
		String age = getStringProperty("age");
		if ("Past Hour".equals(age) || "Past Day".equals(age)) {
			
			pg.strokeWeight(2);
			int buffer = 2;
			pg.line(x-(radius+buffer), 
					y-(radius+buffer), 
					x+radius+buffer, 
					y+radius+buffer);
			pg.line(x-(radius+buffer), 
					y+(radius+buffer), 
					x+radius+buffer, 
					y-(radius+buffer));
			
		}
		pg.popStyle();
		
	}

	// Show the title of the earthquake if this marker is selected
	public void showTitle(PGraphics pg, float x, float y)
	{
		String title = getTitle();
		pg.pushStyle();
		
		pg.rectMode(PConstants.CORNER);
		
		pg.stroke(110);
		pg.fill(255,255,255);
		pg.rect(x, y + 15, pg.textWidth(title) +6, 18, 5);
		
		pg.textAlign(PConstants.LEFT, PConstants.TOP);
		pg.fill(0);
		pg.text(title, x + 3 , y +18);
		
		
		pg.popStyle();
		
	}

	public double threatCircle() {	
		double miles = 20.0f * Math.pow(1.8, 2*getMagnitude()-5);
		double km = (miles * kmPerMile);
		return km;
	}
	
	// determine color of marker from depth
	private void colorDetermine(PGraphics pg) {
		float depth = getDepth();
		
		if (depth < THRESHOLD_INTERMEDIATE) {
			pg.fill(255, 255, 0);
		}
		else if (depth < THRESHOLD_DEEP) {
			pg.fill(0, 0, 255);
		}
		else {
			pg.fill(255, 0, 0);
		}
	}
	
	public String toString()
	{
		return getTitle();
	}
	
	public float getMagnitude() {
		return Float.parseFloat(getProperty("magnitude").toString());
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
	
	public boolean isOnLand()
	{
		return isOnLand;
	}
	

	
	
}
