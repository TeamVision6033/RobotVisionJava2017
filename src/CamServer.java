import org.opencv.core.Core;

import edu.wpi.first.wpilibj.CameraServer;

public class CamServer implements Runnable {
	private CameraServer cameraServer;
	
	public CamServer() {

	}
	static {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		System.out.println("Cam Server thread started");

		//cameraServer = CameraServer.getInstance();
		//cameraServer.putVideo("Jetson", 640, 480);

		while (true) {
			try {
				Thread.sleep(30);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				StopCamServer();
				return;
			}
			if (Thread.interrupted()) {
				StopCamServer();
				return;
			}
		}
	}
	
	private void StopCamServer()
	{
		System.out.println("Cam Server stopping");
	}
}
