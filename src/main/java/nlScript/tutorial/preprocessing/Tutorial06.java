package nlScript.tutorial.preprocessing;

import nlScript.Parser;
import nlScript.core.Autocompletion;
import nlScript.ui.ACEditor;
import ij.IJ;
import ij.ImagePlus;
import ij.measure.Calibration;

/**
 * Tutorial06 shows how to use a fully custom Autocompleter, with the goal to replace the
 * static "calibrated units" option for the "units" type with the actual units string of
 * the currently open image.
 *
 * There is one (autocompletion) issue here, which will be addressed in the next tutorial:
 * Once the user starts typing the text for the unit (let's say a 'p' for 'pixel(s)'), completion
 * should stop (or even better, only suggest 'pixel(s)', because 'mm' doesn't start with 'p').
 * However, here it will just continue to suggest 'pixel(s)' as well as 'mm'.
 *
 * For details, see
 * https://nlScript.github.io/nlScript-java/#dynamic-autocompletion-at-runtime-using-a-custom-autocompleter
 *
 */
public class Tutorial06 {

	public static void main(String[] args) {
		ImagePlus image = IJ.openImage("http://imagej.net/images/clown.jpg");

		// Unfortunately, the clown image doesn't have calibrated pixels. For demonstration
		// purposes, we artificially set a calibration here
		Calibration cal = image.getCalibration();
		cal.pixelWidth = cal.pixelHeight = 0.25;
		cal.setUnit("mm");
		image.show();
		Preprocessing preprocessing = new Preprocessing(image);

		Parser parser = new Parser();

		// The new feature is the use of the ParseStartListener, whose parsingStarted() function
		// gets called when parsing is started. At that time, the image to process is known, so its
		// pixel calibration unit string can be stored, to be used for autocompletion later.
		// Note: Parsing is not only performed once the user clicks on 'Run', but whenever the user's
		// text changes, for auto-completion.
		StringBuilder imageUnits = new StringBuilder();
		parser.addParseStartListener(() -> {
			imageUnits.setLength(0);
			imageUnits.append(image.getCalibration().getUnits());
		});

		parser.defineType("units", "{unitstring:[a-zA-Z()]:+}",
				pn -> !pn.getParsedString().equals("pixel(s)"),
				// Here we use the saved pixel calibration unit string to specify a custom Autocompleter,
				// which in this case returns 2 literals, "pixel(s)" and the value of imageUnits.
				// The Autocompletion class provides many convenience functions for creating Autocompleters.
				(pn, justCheck) -> Autocompletion.literal(pn, "pixel(s)", imageUnits));

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