import java.io.File;
import java.util.*;
import java.io.FilenameFilter;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Scalar;

import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgcodecs.Imgcodecs;

public class ImageSourceFile implements Runnable {
	private String imageFileDir = "";
	private ImageProcessor imageProcessor;

	public ImageSourceFile(String imageFileDir, String windowsShown) {
		this.imageFileDir = imageFileDir;
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		imageProcessor = new ImageProcessor(windowsShown);
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		System.out.println("File source thread started");
        File folder = new File(imageFileDir);
        
        FilenameFilter jpgFileFilter = new FilenameFilter()
        {    
            @Override
            public boolean accept(File dir, String name)
            {
                if(name.endsWith(".jpg"))
                {
                    return true;
                }
                else
                {
                    return false;
                }
            }
        };
         
        File[] files = folder.listFiles(jpgFileFilter);
        Arrays.sort(files);
        Mat originalImage = new Mat();
        for (File file : files)
        {
            originalImage = Imgcodecs.imread(file.getPath());
            imageProcessor.ProcessImage(originalImage);
            try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
		
	}
}
