import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import com.atul.JavaOpenCV.Imshow;

public class ImageProcessor {
	private final int BRIGHT_ADJUST = -20;
	private final int BLUR_SIZE = 3;
	private final int THRESH = 100;
	private final int MIN_CONTOUR_AREA = 10;
	private final int MAX_ROTATED_REC_ANGLE = 5;
	private final double ASPECT_RATIO_MIN = 0.3;
	private final double ASPECT_RATIO_MAX = 0.7;
	private final int MIN_RECT_HEIGHT = 25;
	private final int MAX_RECT_HEIGHT = 999;

	private Imshow originalImageWindow;
	private Imshow countoursImageWindow;
	private Imshow markupImageWindow;
	private boolean showOriginalImage = false;
	private boolean showCountoursImage = false;
	private boolean showMarkupImage = false;
	private String lightColor = "blue";
	private int cycles = 0;
	
	private Mat originalImage;
	private Mat processedImage;
	private Mat markupImage;
	private Mat contourImage;

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
		this.originalImage = originalImage;
		processedImage = originalImage;
		markupImage = originalImage;
		contourImage = originalImage;
		List<ContourData> contourDataList = new ArrayList<ContourData>();
		List<ContourData> potentialContourDataList = new ArrayList<ContourData>();
		List<ContourData> acceptedContourDataList = new ArrayList<ContourData>();

		cycles++;
		if (originalImageWindow != null)
			originalImageWindow.showImage(originalImage);

		// Add Image Processing Here
		processedImage = applyBrightAdjust(processedImage);
		processedImage = blurImage(processedImage);
		processedImage = applyColorFilter(processedImage);

		contourDataList = findCoutours(processedImage);
		
		potentialContourDataList = drawBouningBoxes(contourDataList);
		
		acceptedContourDataList = findAcceptedContours(potentialContourDataList);

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
		image.convertTo(newImage, -1, alpha, beta);
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

	private List<ContourData> findCoutours(Mat image) {
		/// Detect edges using canny
		Mat cannyOutputImage = new Mat();
		int apertureSize = 3;
		boolean L2gradient = false;
		List<ContourData> contourDataList = new ArrayList<ContourData>();

		Imgproc.Canny(image, cannyOutputImage, THRESH, THRESH * 2, apertureSize, L2gradient);
		/// Find contours

		Mat hierarchyMat = new Mat();
		ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		Imgproc.findContours(cannyOutputImage, contours, hierarchyMat, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

		///
		double contourArea;
		MatOfPoint2f thisContour2f = new MatOfPoint2f();
		MatOfPoint2f approxContour2f = new MatOfPoint2f();

		Rect boundRect;
		RotatedRect rotatedRect;

		for (int i = 0; i < contours.size(); i++) {
			contourArea = Imgproc.contourArea(contours.get(i));

			if (contourArea > MIN_CONTOUR_AREA) {
				contours.get(i).convertTo(thisContour2f, CvType.CV_32FC2);

				// epsilon = max distance from contour to approximate poly (accuracy)
				double epsilon = Imgproc.arcLength(thisContour2f, true) * 0.02;
				Imgproc.approxPolyDP(thisContour2f, approxContour2f, epsilon, true);

				// Convert back to MatOfPoint
				MatOfPoint points = new MatOfPoint(approxContour2f.toArray());

				// Get bounding rect of contour
				boundRect = Imgproc.boundingRect(points);
				rotatedRect = Imgproc.minAreaRect(approxContour2f);
				contourDataList.add(new ContourData(contours.get(i), boundRect, rotatedRect));
			}
		}

		return contourDataList;
	}

	private List<ContourData>  drawBouningBoxes(List<ContourData> contourDataList) {
		List<ContourData> potentialContourDataList = new ArrayList<ContourData>();
	    for (int i = 0; i < contourDataList.size(); i++)
	    {
	    	Rect boundRect = contourDataList.get(i).getBoundRect();
	    	RotatedRect rotatedRect = contourDataList.get(i).getRotatedRect();
	    	MatOfPoint contour = contourDataList.get(i).getContour();
	    		    	
	        if (boundRect.height > MIN_RECT_HEIGHT && boundRect.height < MAX_RECT_HEIGHT && rotatedRect.angle < MAX_ROTATED_REC_ANGLE)
	        {
	            double aspect_ratio = (double) boundRect.width / (double) boundRect.height;

	            if (aspect_ratio >= ASPECT_RATIO_MIN && aspect_ratio <= ASPECT_RATIO_MAX)
	            {
	            	potentialContourDataList.add(new ContourData(contour, boundRect, rotatedRect));

	            	List<MatOfPoint> coutours = new ArrayList<MatOfPoint>();
	            	coutours.add(contour);
	            	
	                if (showCountoursImage)
	                {
	                    Scalar color = new Scalar(192, 192, 192);
	                    Imgproc.drawContours(contourImage, contours, i, color);
	                    color = new Scalar(255, 0, 0);
	                    Imgproc.rectangle(contourImage, boundRect.tl(), boundRect.br(), color, 2, 8, 0);
	                }
	                if (showOriginalImage)
	                {
	                    Scalar color = new Scalar(192, 192, 192);
	                    Imgproc.drawContours(originalImage, contours, i, color);
	                    color = new Scalar(255, 0, 0);
	                    Imgproc.rectangle(originalImage, boundRect.tl(), boundRect.br(), color, 2, 8, 0);
	                }
	            }
	        }
	    }
	    return potentialContourDataList;

	}
	
	private List<ContourData> findAcceptedContours(List<ContourData> potentialContourDataList)
	{
		List<ContourData> acceptedContourDataList = new ArrayList<ContourData>();
	    int last_x = 0;
	    int last_y = 0;
	    for (int i = 0; i < potentialContourDataList.size(); i++)
	    {
	    	Rect boundRect = potentialContourDataList.get(i).getBoundRect();
	    	RotatedRect rotatedRect = potentialContourDataList.get(i).getRotatedRect();
	    	MatOfPoint contour = potentialContourDataList.get(i).getContour();
	    	
	        double MAX_X_OFF = 1.7 * boundRect.height;
	        double MIN_X_OFF = .8 * boundRect.height;
	        double MAX_Y_OFF = .15 * boundRect.height;

	        for (int x = 0; x < potentialContourDataList.size(); x++)
	        {
	        	Rect otherPotentialRect = potentialContourDataList.get(x).getBoundRect();
	            int delta_x = Math.abs(boundRect.x - otherPotentialRect.x);
	            int delta_y = Math.abs(boundRect.y - otherPotentialRect.y);
	            if (x != i && delta_x > 0 & delta_y < MAX_Y_OFF)
	            {
	                double height_diff_pct = (double) Math.abs(boundRect.height - otherPotentialRect.height) / (double) boundRect.height;
	                double width_diff_pct = (double) Math.abs(boundRect.width - otherPotentialRect.width) / (double) boundRect.width;

	                if (last_x != otherPotentialRect.x && last_y != otherPotentialRect.y)
	                {
	                    if (delta_x >= MIN_X_OFF && delta_x <= MAX_X_OFF && height_diff_pct < 0.1 && width_diff_pct < 0.1)
	                    {
	                        last_x = otherPotentialRect.x;
	                        last_y = otherPotentialRect.y;

	                        acceptedContourDataList.add(new ContourData(contour, boundRect, rotatedRect));
	                        break;
	                    }
	                }
	            }
	        }
	    }
	    return acceptedContourDataList;
	}
	
	public int getCycles() {
		return cycles;
	}

}
