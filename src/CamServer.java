
public class CamServer implements Runnable {
	public CamServer() {

	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		System.out.println("Cam Server thread started");

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
