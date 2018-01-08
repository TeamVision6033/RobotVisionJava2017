
public class VisionData {
	private double angleToTarget;
	private double distanceToTarget;
	private double offCenterPixels;
	private double offCenterDistance;
	private int imagesProcessed = 0;
	private int goodImages = 0;
	private int dataSeqNo = 0;
	
	public double getAngleToTarget() {
		return angleToTarget;
	}
	public void setAngleToTarget(double angleToTarget) {
		this.angleToTarget = angleToTarget;
	}
	public double getDistanceToTarget() {
		return distanceToTarget;
	}
	public void setDistanceToTarget(double distanceToTarget) {
		this.distanceToTarget = distanceToTarget;
	}
	public double getOffCenterPixels() {
		return offCenterPixels;
	}
	public void setOffCenterPixels(double offCenterPixels) {
		this.offCenterPixels = offCenterPixels;
	}
	public int getImagesProcessed() {
		return imagesProcessed;
	}
	public void incrementImagesProcessed() {
		this.imagesProcessed++;
	}
	public int getGoodImages() {
		return goodImages;
	}
	public void incrementGoodImages() {
		this.goodImages++;
		this.dataSeqNo = this.imagesProcessed;
	}
	public int getDataSeqNo()
	{
		return this.getDataSeqNo();
	}
	public int getDataAge()
	{
		return this.imagesProcessed - this.dataSeqNo;
	}
	public double getOffCenterDistance() {
		return this.offCenterDistance;
	}
	public void setOffCenterDistance(double offCenterDistance) {
		this.offCenterDistance = offCenterDistance;
	}
	
	
}
