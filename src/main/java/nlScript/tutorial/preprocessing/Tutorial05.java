package nlScript.tutorial.preprocessing;

import nlScript.Parser;
import nlScript.ui.ACEditor;
import ij.IJ;
import ij.ImagePlus;

/**
 * Tutorial05 demonstrates how to provide multiple definitions for a type. The type 'units' is first
 * defined as the string literal "pixel(s)" and additionally as "calibrated units".
 *
 * As a result, once the user hits the corresponding position during input, an autocompletion menu will be
 * displayed to select between the two.
 *
 * For details, see
 * https://nlScript.github.io/nlScript-java/#multiple-choice-autocompletion-choose-a-unit-for-filter-size
 */
public class Tutorial05 {

	public static void main(String[] args) {
		ImagePlus image = IJ.openImage("http://imagej.net/images/clown.jpg");
		image.show();
		Preprocessing preprocessing = new Preprocessing(image);

		Parser parser = new Parser();

		// 1. Define the type "units" as the string literal "pixel(s)"
		parser.defineType("units", "pixel(s)", pn -> false);
		// 2. Define the type "units" as the string literal "calibrated units"
		parser.defineType("units", "calibrated units", pn -> true);

		parser.defineType("filter-size", "{stddev:float} {units:units}", pn -> {
			double stddev = (Double) pn.evaluate("stddev");

			// Note that the "units" type evaluates to a Boolean, which is true if "calibrated units" was
			// parsed, and false if "pixel(s)" was parsed
			boolean units = (Boolean) pn.evaluate("units");

			// Convert stddev to pixel units in case it was specified in calibrated units
			if(units)
				stddev /= image.getCalibration().pixelWidth;
			return stddev;
		}, true);

		parser.defineSentence(
				"Apply Gaussian blurring with a standard deviation of {stddev:filter-size}.",
				pn -> {
					double stdDev = (double)pn.evaluate("stddev");
					preprocessing.gaussianBlur((float)stdDev);
					return null;
				});

		new ACEditor(parser).setVisible(true);
	}
}