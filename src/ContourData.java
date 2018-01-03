import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;

public class ContourData {
	private MatOfPoint contour;
	private Rect boundRect;
	private RotatedRect rotatedRect;

	public ContourData(MatOfPoint contour, Rect boundRect, RotatedRect rotatedRect) {
		this.contour = contour;
		this.boundRect = boundRect;
		this.rotatedRect = rotatedRect;
	}

	public MatOfPoint getContour() {
		return this.contour;
	}

	public Rect getBoundRect() {
		return this.boundRect;
	}

	public RotatedRect getRotatedRect() {
		return this.rotatedRect;
	}
}
