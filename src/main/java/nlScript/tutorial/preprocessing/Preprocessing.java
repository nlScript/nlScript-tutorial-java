package nlScript.tutorial.preprocessing;

import ij.IJ;
import ij.ImagePlus;
import ij.process.ImageProcessor;

public class Preprocessing {

	private ImagePlus image;

	public Preprocessing() {
	}
	public Preprocessing(ImagePlus image) {
		this.image = image;
	}

	public void setImage(ImagePlus image) {
		this.image = image;
	}

	public ImagePlus getImage() {
		return image;
	}

	public void gaussianBlur(float stdDev) {
		IJ.run(image, "Gaussian Blur...", "sigma=" + stdDev);
	}

	public void medianFilter(int radius) {
		IJ.run(image, "Median...", "radius=" + radius);
	}

	public void subtractBackground(float radius) {
		IJ.run(image, "Subtract Background...", "rolling=" + radius);
	}

	public void convertToGray() {
		if(image.getType() == ImagePlus.COLOR_RGB)
			IJ.run(image, "8-bit", "");
	}

	public void intensityNormalization() {
		convertToGray();
		ImageProcessor ip = image.getProcessor();
		double min = ip.getMin();
		double max = ip.getMax();
		ip = ip.convertToFloat();
		ip.subtract(min);
		ip.multiply(1 / (max - min));
		image.setProcessor(ip);
	}
}
