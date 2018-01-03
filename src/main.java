
public class main {
	private static String imageSource = "cam";
	private static String windowsShown = "all";
	private static int cameraNumber = 0;
	private static String imageFileDir = "/home/ubuntu/images";

	private static Thread imageThread;
	private static Thread cameraServerThread;
	
	public double test;

	public static void main(String args[]) {
		for (String arg : args) {
			if (arg.equalsIgnoreCase("file"))
				imageSource = "file";
			if (arg.equalsIgnoreCase("none"))
				windowsShown = "none";
			System.out.println(arg);
		}
		if (imageSource.equals("cam"))
			imageThread = new Thread(new ImageSourceCam(cameraNumber, windowsShown));
		else
			imageThread = new Thread(new ImageSourceFile(imageFileDir, windowsShown));

		imageThread.start();

		cameraServerThread = new Thread(new CamServer());
		cameraServerThread.start();

		try {
			imageThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		cameraServerThread.interrupt();
		try {
			cameraServerThread.join(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("Main program done.");
	}
}
