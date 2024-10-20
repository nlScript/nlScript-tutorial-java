package nlScript.tutorial.preprocessing;

import ij.IJ;
import ij.ImagePlus;
import nlScript.Parser;
import nlScript.ui.ACEditor;

/**
 * This tutorial uses the editor's 'beforeRun' hook to
 *
 * - work on the currently open image. This gives you the possibility to open your own image
 *   in ImageJ before clicking on the 'Run' button.
 *
 *  - make a copy of the input image before it is processed, to keep it for further runs.
 */
public class Tutorial11 {

	public static void main(String[] args) {
		ImagePlus image = IJ.openImage("http://imagej.net/images/clown.jpg");
		image.show();

		Preprocessing preprocessing = new Preprocessing(image);

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

		parser.defineSentence("Apply a Median filter with radius of {radius:filter-size}.", null);
		parser.defineSentence("Apply an Unsharp Mask with radius of {radius:filter-size} and mask weight of {weight:float}.", null);
		parser.defineSentence("Enhance local contrast via CLAHE with a blocksize of {block-size:int}, {bins:int} histogram bins and a max slope of {slope:int}, using {image:[a-z-]:+} as a mask.", null);

		ACEditor editor = new ACEditor(parser);
		editor.setBeforeRun(() -> {
			ImagePlus output = IJ.getImage().duplicate();
			preprocessing.setImage(output);
			output.show();
		});
		editor.setVisible(true);
	}
}
