package nlScript.tutorial.preprocessing;

import nlScript.Parser;
import nlScript.ui.ACEditor;
import ij.IJ;
import ij.ImagePlus;
import ij.measure.Calibration;

/**
 * The previous tutorial showed how to use a custom Autocompleter. Using a ParseStartListener, the pixel
 * calibration unit string is saved. The 'units' autocompleter then suggests the two options 'pixel(s)'
 * and the actual calibration unit string.
 *
 * A much better solution would be, if the 'units' type could be defined as 'pixel(s)' and as 'mm' in the
 * first place (similar to Tutorial05), and this would work here because the image to be processed is fixed.
 *
 * However, in a more general case, the designed language should work on any image, and therefore, at the
 * time of designing the language the actual pixel units are not known yet, and this is why Tutorial05
 * used the fixed literal 'calibrated units' instead of the actual units string.
 *
 * This tutorial shows how the 'units' type can be re-defined dynamically, and this is again best done
 * within in the ParseStartListener's parsingStarted() function.
 *
 * The result will be similar to that of Tutorial05, but instead of the general 'calibrated units'
 * autocompletion option, the actual units string will be shown as an option.
 *
 * For details, see
 * https://nlScript.github.io/nlScript-java/#dynamically-re-defining-types
 */
public class Tutorial08 {

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

			// At the start of parsing (remember this is done whenever auto-completion
			// needs to be performed), the 'units' type is undefined and then
			// re-defined, according to the pixel calibration unit string of the current image:
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