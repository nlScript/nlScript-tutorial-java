package nlScript.tutorial.preprocessing;

import nlScript.Parser;
import nlScript.core.Autocompletion;
import nlScript.ui.ACEditor;
import ij.IJ;
import ij.ImagePlus;
import ij.measure.Calibration;

/**
 * The previous tutorial showed how to use a custom Autocompleter, but suffered from one
 * autocompletion issue:
 *
 * Once the user starts typing the text for the unit (let's say a 'p' for 'pixel(s)'), completion
 * should stop (or even better, only suggest 'pixel(s)', because 'mm' doesn't start with 'p').
 * However, here it will just continue to suggest 'pixel(s)' as well as 'mm'.
 *
 * This tutorial demonstrates how to stop autocompletion once the user started typing a value.
 *
 * In the case here, it would actually be better to filter suggested options according to what the user typed,
 * (and this will be shown in the next tutorial), but there are cases where stopping auto-completion after the
 * user started to type is important: This is particularly the case if e.g. entering numbers: As long as nothing
 * is entered, auto-completion should indicate what needs to be entered (e.g. a placeholder with a name), but once
 * the user started typing a number, auto-completion should be quiet. BTW: This is not only true for numbers, but
 * for also e.g. when entering a name for something.
 *
 * For details, see
 * https://nlScript.github.io/nlScript-java/#prohibit-further-autocompletion-autocompleterveto
 */
public class Tutorial07 {

	public static void main(String[] args) {
		ImagePlus image = IJ.openImage("http://imagej.net/images/clown.jpg");

		Calibration cal = image.getCalibration();
		cal.pixelWidth = cal.pixelHeight = 0.25;
		cal.setUnit("mm");
		image.show();
		Preprocessing preprocessing = new Preprocessing(image);

		Parser parser = new Parser();

		StringBuilder imageUnits = new StringBuilder();
		parser.addParseStartListener(() -> {
			imageUnits.setLength(0);
			imageUnits.append(image.getCalibration().getUnits());
		});

		parser.defineType("units", "{unitstring:[a-zA-Z()]:+}",
				pn -> !pn.getParsedString().equals("pixel(s)"),
				// The only change here, compared to the previous version, is
				// to check whether the user has started typing for 'units', in
				// which case pn.getParsedString() returns what's already entered.
				// If something was entered, a special 'veto' autocompleter is returned,
				// which prohibits further auto-completion.
				(pn, justCheck) -> pn.getParsedString().isEmpty()
						? Autocompletion.literal(pn, "pixel(s)", imageUnits)
						: Autocompletion.veto(pn));

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