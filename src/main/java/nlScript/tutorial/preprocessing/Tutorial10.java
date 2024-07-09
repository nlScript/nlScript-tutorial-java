package nlScript.tutorial.preprocessing;

import nlScript.Parser;
import nlScript.ui.ACEditor;
import ij.IJ;
import ij.ImagePlus;

/**
 * This tutorial uses the editor's 'beforeRun' hook to
 *
 * - work on the currently open image. This gives you the possibility to open your own image
 *   in ImageJ before clicking on the 'Run' button.
 *
 *  - make a copy of the input image before it is processed, to keep it for further runs.
 */
public class Tutorial10 {

	public static void main(String[] args) {

		Preprocessing preprocessing = new Preprocessing();

		Parser parser = new Parser();

		parser.addParseStartListener(() -> {
			String unitsString = preprocessing.getImage().getCalibration().getUnits();

			parser.undefineType("units");

			parser.defineType("units", "pixel(s)", pn -> false);
			parser.defineType("units", unitsString, pn -> true);
		});

		parser.defineType("units", "pixel(s)", pn -> false);

		parser.defineType("filter-size", "{stddev:float} {units:units}", pn -> {
			double stddev = (Double) pn.evaluate("stddev");
			boolean units = (Boolean) pn.evaluate("units");
			if(units)
				stddev /= preprocessing.getImage().getCalibration().pixelWidth;
			return stddev;
		}, true);

		// Gaussian Blurring
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

		ACEditor editor = new ACEditor(parser);
		editor.setBeforeRun(() -> {
			ImagePlus output = IJ.getImage().duplicate();
			preprocessing.setImage(output);
			output.show();
		});
		editor.setVisible(true);
	}
}
