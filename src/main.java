
import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class main {
	private static String imageSource = "cam";
	private static String windowsShown = "all";
	private static int cameraNumber = 0;

	private static Thread imageThread;
	private static Thread cameraServerThread;

	public double test;

	public static void main(String args[]) {
		for (String arg : args) {
			if (arg.equalsIgnoreCase("file"))
				imageSource = "file";
			if (arg.equalsIgnoreCase("none"))
				windowsShown = "none";
			if (arg.equalsIgnoreCase("1"))
				cameraNumber = 1;
			if (arg.equalsIgnoreCase("2"))
				cameraNumber = 2;
			if (arg.equalsIgnoreCase("3"))
				cameraNumber = 3;
			System.out.println(arg);
		}

		String imageFileDir = getImageFileDir();

		if (imageSource.equals("cam"))
			imageThread = new Thread(new ImageSourceCam(cameraNumber, imageFileDir, windowsShown));
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

	private static String getImageFileDir() {
		String baseDir = "/media/ubuntu/";
		String imageDirectory = "";
		File imagePath;
		File dir = new File("/media/ubuntu/");
		if (dir.exists()) {
			String[] directories = dir.list(new FilenameFilter() {
				@Override
				public boolean accept(File current, String name) {
					return new File(current, name).isDirectory();
				}
			});
			for (String directory : directories) {
				imageDirectory = baseDir + directory + "/images";
				imagePath = new File(imageDirectory);
				if (!imagePath.exists()) {
					imagePath.mkdirs();
				}
				break;
			}
		}
		if (imageDirectory.length() == 0) {
			imageDirectory = "/tmp/images";
			imagePath = new File(imageDirectory);
			if (!imagePath.exists()) {
				imagePath.mkdirs();
			}
		}
		imagePath = new File(imageDirectory);
		if (imagePath.exists())
			return imageDirectory;
		else
			return null;
	}
}
