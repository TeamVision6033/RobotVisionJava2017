import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import com.atul.JavaOpenCV.Imshow;

public class ImageProcessor {
	private final int BRIGHT_ADJUST = -20;
	private final int BLUR_SIZE = 3;
	private final int THRESH = 100;
	
	private Imshow originalImageWindow;
	private Imshow countoursImageWindow;
	private Imshow markupImageWindow;
	private boolean showOriginalImage = false;
	private boolean showCountoursImage = false;
	private boolean showMarkupImage = false;
	private String lightColor = "blue";
	private int cycles = 0;
	
	private List<MatOfPoint> contours;
	

	public ImageProcessor(String windowsShown) {
		int xPos = 0;
		int yPos = 0;
		int xIncrement = 100;
		int yIncrement = 100;
		if (windowsShown.equals("all")) {
			showOriginalImage = true;
			showCountoursImage = true;
			showMarkupImage = true;
		}
		if (showMarkupImage) {
			markupImageWindow = new Imshow("Markup", xPos, yPos, false);
			xPos += xIncrement;
			yPos += yIncrement;
		}
		if (showCountoursImage) {
			countoursImageWindow = new Imshow("Countours", xPos, yPos, false);
			xPos += xIncrement;
			yPos += yIncrement;
		}
		if (showOriginalImage) {
			originalImageWindow = new Imshow("Original", xPos, yPos, false);
			xPos += xIncrement;
			yPos += yIncrement;
		}
	}

	public void ProcessImage(Mat originalImage) {
		Mat processedImage = originalImage;
		Mat markupImage = originalImage;
		Mat hierarchyMat = new Mat();
		
		cycles++;
		if (originalImageWindow != null)
			originalImageWindow.showImage(originalImage);
		
		// Add Image Processing Here
		processedImage = applyBrightAdjust(processedImage);
		processedImage = blurImage(processedImage);
		processedImage = applyColorFilter(processedImage);
		
		hierarchyMat = findCoutours(processedImage);

		if (countoursImageWindow != null)
			countoursImageWindow.showImage(processedImage);

		// Add Markups to Original Here

		if (markupImageWindow != null)
			markupImageWindow.showImage(markupImage);
	}

	private Mat applyBrightAdjust(Mat image) {
		int alpha = 1;
		int beta = BRIGHT_ADJUST;
		Mat newImage = new Mat();
		image.convertTo(newImage, -1 , alpha, beta);
		return newImage;

	}

	private Mat blurImage(Mat image) {
		Mat newImage = new Mat();
		Size blurSize = new Size();
		blurSize.height = BLUR_SIZE;
		blurSize.width = BLUR_SIZE;
		Imgproc.blur(image, newImage, blurSize);
		return newImage;

	}

	private Mat applyColorFilter(Mat image) {
		Mat newImage = new Mat();
		Scalar botomLimitcolor;
		Scalar topLimitColor;
		topLimitColor = new Scalar(255, 255, 255);
		if (lightColor.equals("blue")) {
			botomLimitcolor = new Scalar(0, 0, 200);
		} else {
			botomLimitcolor = new Scalar(0, 0, 0);
		}

		Core.inRange(image, botomLimitcolor, topLimitColor, newImage);

		return newImage;
	}
	
	private Mat findCoutours(Mat image) {
	    /// Detect edges using canny
		Mat cannyOutputImage = new Mat();
		int apertureSize = 3;
		boolean L2gradient = false;
		
		Imgproc.Canny(image, cannyOutputImage, THRESH, THRESH * 2, apertureSize, L2gradient);
	    /// Find contours

		Mat hierarchyMat = new Mat();
		contours  = new ArrayList<MatOfPoint>();
		Imgproc.findContours(cannyOutputImage, contours, hierarchyMat, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
		

		return hierarchyMat;
	}


	public int getCycles() {
		return cycles;
	}

}
