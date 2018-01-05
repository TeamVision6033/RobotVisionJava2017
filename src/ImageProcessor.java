import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
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
	private final double PI = 3.14159265;

	private Imshow originalImageWindow;
	private Imshow countoursImageWindow;
	private boolean showOriginalImage = false;
	private boolean showCountoursImage = false;

	private String lightColor = "blue";

	private Mat originalImage;
	private Mat processedImage;
	private Mat contourImage;

	private VisionData visionData = new VisionData();

	private List<MatOfPoint> contours;

	public ImageProcessor(String windowsShown) {
		int xPos = 0;
		int yPos = 0;
		int xIncrement = 100;
		int yIncrement = 100;
		
		if (windowsShown.equals("all")) {
			showOriginalImage = true;
			showCountoursImage = true;
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
		contourImage = originalImage;
		List<ContourData> contourDataList = new ArrayList<ContourData>();
		List<ContourData> potentialContourDataList = new ArrayList<ContourData>();
		List<ContourData> acceptedContourDataList = new ArrayList<ContourData>();

		visionData.incrementImagesProcessed();

		// Add Image Processing Here
		processedImage = applyBrightAdjust(processedImage);
		processedImage = blurImage(processedImage);
		processedImage = applyColorFilter(processedImage);

		contourDataList = findCoutours(processedImage);

		potentialContourDataList = drawBouningBoxes(contourDataList);

		acceptedContourDataList = findAcceptedContours(potentialContourDataList);

		calculateDistances(acceptedContourDataList);

		applyImageOverlay();

		if (originalImageWindow != null)
			originalImageWindow.showImage(originalImage);

		if (countoursImageWindow != null)
			countoursImageWindow.showImage(processedImage);
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

	private List<ContourData> drawBouningBoxes(List<ContourData> contourDataList) {
		List<ContourData> potentialContourDataList = new ArrayList<ContourData>();
		for (int i = 0; i < contourDataList.size(); i++) {
			Rect boundRect = contourDataList.get(i).getBoundRect();
			RotatedRect rotatedRect = contourDataList.get(i).getRotatedRect();
			MatOfPoint contour = contourDataList.get(i).getContour();

			if (boundRect.height > MIN_RECT_HEIGHT && boundRect.height < MAX_RECT_HEIGHT
					&& rotatedRect.angle < MAX_ROTATED_REC_ANGLE) {
				double aspect_ratio = (double) boundRect.width / (double) boundRect.height;

				if (aspect_ratio >= ASPECT_RATIO_MIN && aspect_ratio <= ASPECT_RATIO_MAX) {
					potentialContourDataList.add(new ContourData(contour, boundRect, rotatedRect));

					List<MatOfPoint> coutours = new ArrayList<MatOfPoint>();
					coutours.add(contour);

					if (showCountoursImage) {
						Scalar color = new Scalar(192, 192, 192);
						Imgproc.drawContours(contourImage, contours, i, color);
						color = new Scalar(255, 0, 0);
						Imgproc.rectangle(contourImage, boundRect.tl(), boundRect.br(), color, 2, 8, 0);
					}
					if (showOriginalImage) {
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

	private List<ContourData> findAcceptedContours(List<ContourData> potentialContourDataList) {
		List<ContourData> acceptedContourDataList = new ArrayList<ContourData>();
		int last_x = 0;
		int last_y = 0;
		for (int i = 0; i < potentialContourDataList.size(); i++) {
			Rect boundRect = potentialContourDataList.get(i).getBoundRect();
			RotatedRect rotatedRect = potentialContourDataList.get(i).getRotatedRect();
			MatOfPoint contour = potentialContourDataList.get(i).getContour();

			double MAX_X_OFF = 1.7 * boundRect.height;
			double MIN_X_OFF = .8 * boundRect.height;
			double MAX_Y_OFF = .15 * boundRect.height;

			for (int x = 0; x < potentialContourDataList.size(); x++) {
				Rect otherPotentialRect = potentialContourDataList.get(x).getBoundRect();
				int delta_x = Math.abs(boundRect.x - otherPotentialRect.x);
				int delta_y = Math.abs(boundRect.y - otherPotentialRect.y);
				if (x != i && delta_x > 0 & delta_y < MAX_Y_OFF) {
					double height_diff_pct = (double) Math.abs(boundRect.height - otherPotentialRect.height)
							/ (double) boundRect.height;
					double width_diff_pct = (double) Math.abs(boundRect.width - otherPotentialRect.width)
							/ (double) boundRect.width;

					if (last_x != otherPotentialRect.x && last_y != otherPotentialRect.y) {
						if (delta_x >= MIN_X_OFF && delta_x <= MAX_X_OFF && height_diff_pct < 0.1
								&& width_diff_pct < 0.1) {
							last_x = otherPotentialRect.x;
							last_y = otherPotentialRect.y;

							acceptedContourDataList.add(new ContourData(contour, boundRect, rotatedRect));
							break;
						}
					}
				}
			}
		}

		Scalar color = new Scalar(0, 255, 0);
		Scalar circleColor = new Scalar(0, 0, 255);
		for (int i = 0; i < acceptedContourDataList.size(); i++) {
			Rect acceptedRect = acceptedContourDataList.get(i).getBoundRect();
			if (showCountoursImage)
				Imgproc.rectangle(contourImage, acceptedRect.tl(), acceptedRect.br(), color, 2, 8, 0);
			Imgproc.rectangle(originalImage, acceptedRect.tl(), acceptedRect.br(), color, 2, 8, 0);

			Imgproc.circle(contourImage, new Point(acceptedRect.x, acceptedRect.y), 3, circleColor);
		}

		return acceptedContourDataList;
	}

	private void calculateDistances(List<ContourData> acceptedContourDataList) {
		double avg_distance = 0;
		double img_h_center = (double) (originalImage.cols() / 2);
		double off_center = 0.0;
		double angle_calc = 0.0;

		if (acceptedContourDataList.size() == 2) {
			Rect acceptedRect0 = acceptedContourDataList.get(0).getBoundRect();
			Rect acceptedRect1 = acceptedContourDataList.get(1).getBoundRect();

			int FOCAL_LENGTH = 836 - (2 * acceptedRect0.height);
			double dist1 = ((double) (5 * FOCAL_LENGTH) / (double) acceptedRect0.height);
			double dist2 = ((double) (5 * FOCAL_LENGTH) / (double) acceptedRect1.height);

			avg_distance += (double) (dist1 + dist2) / 2.0;

			double pt1 = (double) acceptedRect0.x + (double) acceptedRect0.width;
			double pt2 = (double) acceptedRect1.x;

			off_center = ((pt1 - img_h_center) / 2) + ((pt2 - img_h_center) / 2);

			double dist_off_center = acceptedRect0.height / 5 * off_center;
			angle_calc = Math.asin((off_center / acceptedRect0.height * 5) / dist1) * 180.0 / PI;

			visionData.setAngleToTarget(angle_calc);
			visionData.setDistanceToTarget(avg_distance);
			visionData.setOffCenterPixels(off_center);
			visionData.setOffCenterDistance(dist_off_center);
			visionData.incrementGoodImages();
		}
	}

	private void applyImageOverlay() {
		double img_h_center = (double) (originalImage.cols() / 2);
		double img_v_center = (double) (originalImage.rows() / 2);

		Scalar grid_color = new Scalar(153, 255, 255);

		// vertical center line
		Imgproc.line(originalImage, new Point(img_h_center, 0), new Point(img_h_center, originalImage.rows()),
				grid_color, 1, 8, 0);
		int pt1 = (int) (img_h_center - 20);
		int pt2 = (int) (img_h_center + 20);
		int space = (int) (originalImage.rows() / 8);
		for (int intersect_point = space; intersect_point < originalImage.rows(); intersect_point += space) {
			Imgproc.line(originalImage, new Point(pt1, intersect_point), new Point(pt2, intersect_point), grid_color, 1,
					8, 0);
		}

		// horizontal center line
		Imgproc.line(originalImage, new Point(0, img_v_center), new Point(originalImage.cols(), img_v_center),
				grid_color, 1, 8, 0);
		pt1 = (int) (img_v_center - 20);
		pt2 = (int) (img_v_center + 20);
		space = (int) (originalImage.cols() / 8);
		for (int intersect_point = space; intersect_point < originalImage.cols(); intersect_point += space) {
			Imgproc.line(originalImage, new Point(intersect_point, pt1), new Point(intersect_point, pt2), grid_color, 1,
					8, 0);
		}

		// ADD GRAY BACKGROUND FOR DATA DISPLAY
		int box_height = 50;
		Scalar color = new Scalar(75, 75, 75);
		Imgproc.rectangle(originalImage, new Point(0, originalImage.rows()),
				new Point(originalImage.cols(), originalImage.rows() - box_height), color, -1, 8, 0);

		color = new Scalar(190, 255, 255);
		int x1 = 15;
		int x2 = 270;
		int x3 = 430;
		int bottom_row = originalImage.rows() - 10;
		int top_row = bottom_row - 22;
		boolean bottomLeftOrigin = false;
		String tmpString;

		if (visionData.getDataAge() < 3) {
			int lock_limit = 4; // how many pixels off center is good
			Scalar circle_color = (Math.abs(visionData.getOffCenterPixels()) < lock_limit) ? new Scalar(0, 255, 0)
					: new Scalar(255, 0, 0);
			int circle_dia = (Math.abs(visionData.getOffCenterPixels()) < lock_limit) ? 15 : 10;
			int circle_thickness = (Math.abs(visionData.getOffCenterPixels()) < lock_limit) ? -1 : 2;

			Imgproc.circle(originalImage, new Point(img_h_center + visionData.getOffCenterPixels(), img_v_center),
					circle_dia, circle_color, circle_thickness);
		}

		if (visionData.getDataAge() > 2 || visionData.getGoodImages() == 0)
			color = new Scalar(185, 185, 185);
		tmpString = "Dist Calc: " + Math.round(visionData.getDistanceToTarget() * 10) / 10 + "in";
		Imgproc.putText(originalImage, tmpString, new Point(x1, top_row), 1, 1.0, color, 1, 8, bottomLeftOrigin);

		tmpString = "Angle: " + Math.round(visionData.getAngleToTarget() * 10) / 10;
		Imgproc.putText(originalImage, tmpString, new Point(x2, top_row), 1, 1.0, color, 1, 8, bottomLeftOrigin);

		tmpString = "Off Center: " + Math.round(visionData.getOffCenterDistance() * 10) / 10 + "in";
		Imgproc.putText(originalImage, tmpString, new Point(x3, top_row), 1, 1.0, color, 1, 8, bottomLeftOrigin);
		
		//---------------------------------------------------------------------------------------------------------//
		
		tmpString = "Frames: " + visionData.getImagesProcessed();
		Imgproc.putText(originalImage, tmpString, new Point(x1, bottom_row), 1, 1.0, color, 1, 8, bottomLeftOrigin);

		tmpString = "Found: " + visionData.getGoodImages();
		Imgproc.putText(originalImage, tmpString, new Point(x2, bottom_row), 1, 1.0, color, 1, 8, bottomLeftOrigin);

		tmpString = "Data Age: " + visionData.getDataAge();
		Imgproc.putText(originalImage, tmpString, new Point(x3, bottom_row), 1, 1.0, color, 1, 8, bottomLeftOrigin);
	}

}
