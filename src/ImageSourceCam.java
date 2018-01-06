import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

public class ImageSourceCam implements Runnable {
	private int cameraNumber;
	private ImageProcessor imageProcessor;
	private String imageFileDir;
	
	private final int MAX_SAVED_IMAGES = 1000;
	private final int SAVE_FREQUENCY = 5;

	public ImageSourceCam(int cameraNumber, String imageFileDir, String windowsShown) {
		this.cameraNumber = cameraNumber;
		this.imageFileDir = imageFileDir;
		System.out.println("Using Camers " + this.cameraNumber);
		imageProcessor = new ImageProcessor(windowsShown);
	}
	
	static {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}

	@Override
	public void run() {
		int imageNumber = 0;
		int savedImageNumber = 0;
		
		VideoCapture webCam = new VideoCapture();
		webCam.open(cameraNumber);
		Mat originalImage = new Mat();

		while (true) {
			if (webCam.isOpened())
			{
				if (webCam.read(originalImage))
				{
					imageProcessor.ProcessImage(originalImage);
					if (imageNumber % SAVE_FREQUENCY == 0)
					{
						// save
						if (imageFileDir != null)
							saveImage(originalImage,savedImageNumber);
						savedImageNumber++;
					}
					if (savedImageNumber > MAX_SAVED_IMAGES)
					{
						 imageNumber = 0;
						 savedImageNumber = 0;
					}
					imageNumber++;
				}
				else
					System.out.println("Failed to get frame.");
			}
			else
			{
				System.out.println("Camera not opened.");
				webCam.open(cameraNumber);
			}
			if (Thread.interrupted())
				break;
            try {
				Thread.sleep(30);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		webCam.release();
	}
	
	private void saveImage(Mat originalImage,int savedImageNumber)
	{
		String fileName = imageFileDir + "/" + "camImage" + String.format("%05d", savedImageNumber) + ".jpg";
        Imgcodecs.imwrite(fileName, originalImage);
	}
}
