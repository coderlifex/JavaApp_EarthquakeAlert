package earthquakeAlert;

import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.data.PointFeature;
import processing.core.PConstants;
import processing.core.PGraphics;

public class AlertLocationMarker extends CommonMarker {
	public AlertLocationMarker(Location location) {
		super(location);
	}

	@Override
	public void drawMarker(PGraphics pg, float x, float y) {
		pg.pushStyle();
		pg.fill(255, 0, 0);
		int size = 10;
		float centerx = x, centery = y;
		float x1 = centerx, y1 = centery - size;
		float x2 = (float) (centerx - size * Math.sqrt(3) / 2), y2 = centery + size/2;
		float x3 = (float) (centerx + size * Math.sqrt(3) / 2), y3 = centery + size/2;
		pg.triangle(x1, y1, x2, y2, x3, y3);
		pg.popStyle();
		
	}

	@Override
	public void showTitle(PGraphics pg, float x, float y) {
		String title = "Alert Location";
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

		
}