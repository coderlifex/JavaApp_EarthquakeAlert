package EarthquakeMap;

import de.fhpotsdam.unfolding.data.PointFeature;
import processing.core.PGraphics;

public class LandQuakeMarker extends EarthquakeMarker {
	public LandQuakeMarker(PointFeature quake) {
		super(quake);
		isOnLand = true;
	}
	
	@Override
	public void drawEarthquake(PGraphics pg, float x, float y) {
		pg.ellipse(x, y, 2*radius, 2*radius);		
	}

	// Get the country the earthquake is in
	public String getCountry() {
		return (String) getProperty("country");
	}

		
}