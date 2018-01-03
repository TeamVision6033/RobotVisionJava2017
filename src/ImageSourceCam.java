
public class ImageSourceCam implements Runnable {
	private int cameraNumber;
	private ImageProcessor imageProcessor;

	public ImageSourceCam(int cameraNumber, String windowsShown) {
		this.cameraNumber = cameraNumber;
		imageProcessor = new ImageProcessor(windowsShown);
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		System.out.println("File source thread started");
	}
}
