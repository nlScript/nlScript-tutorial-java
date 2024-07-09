package nlScript.tutorial.preprocessing;

import nlScript.Parser;
import nlScript.ui.ACEditor;
import ij.IJ;
import ij.ImagePlus;
import ij.measure.Calibration;

/**
 * This tutorial extends the previous functionality by median filtering, background subtraction and intensity
 * normalization, simply by adding more sentence definitions similar to the existing one for Gaussian blurring.
 *
 * The new sentences also re-use the 'filter-size' type.
 */
public class Tutorial09 {

	public static void main(String[] args) {
		new ij.ImageJ();
		ImagePlus image = IJ.openImage("http://imagej.net/images/clown.jpg");

		Calibration cal = image.getCalibration();
		cal.pixelWidth = cal.pixelHeight = 0.25;
		cal.setUnit("mm");
		image.show();
		Preprocessing preprocessing = new Preprocessing(image);

		Parser parser = new Parser();

		parser.addParseStartListener(() -> {
			String unitsString = image.getCalibration().getUnits();

			parser.undefineType("units");

			parser.defineType("units", "pixel(s)", pn -> false);
			parser.defineType("units", unitsString, pn -> true);
		});

		parser.defineType("units", "pixel(s)", pn -> false);

		parser.defineType("filter-size", "{stddev:float} {units:units}", pn -> {
			double stddev = (Double) pn.evaluate("stddev");
			boolean units = (Boolean) pn.evaluate("units");
			if(units)
				stddev /= image.getCalibration().pixelWidth;
			return stddev;
		}, true);

		// Gaussian blurring
		parser.defineSentence(
				"Apply Gaussian blurring with a standard deviation of {stddev:filter-size}.",
				pn -> {
					double stdDev = (double)pn.evaluate("stddev");
					preprocessing.gaussianBlur((float)stdDev);
					return null;
				});

		// Median filtering
		parser.defineSentence(
				"Apply Median filtering with a window of radius {window-size:filter-size}.",
				pn -> {
					double windowSize = (double)pn.evaluate("window-size");
					preprocessing.medianFilter((int) Math.round(windowSize));
					return null;
				});

		// Intensity normalization
		parser.defineSentence(
				"Normalize intensities.",
				pn -> {
					preprocessing.intensityNormalization();
					return null;
				});

		// Background subtraction
		parser.defineSentence(
				"Subtract the background with a standard deviation of {window-size:filter-size}.",
				pn -> {
					double windowSize = (double)pn.evaluate("window-size");
					preprocessing.subtractBackground((int) Math.round(windowSize));
					return null;
				});

		new ACEditor(parser).setVisible(true);
	}
}
