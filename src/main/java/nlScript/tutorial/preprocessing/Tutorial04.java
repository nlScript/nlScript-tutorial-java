package nlScript.tutorial.preprocessing;

import nlScript.Parser;
import nlScript.ui.ACEditor;
import ij.IJ;
import ij.ImagePlus;

/**
 * The previous tutorial introduced custom types.
 * In Tutorial04, they will be used to change how they are autocompleted in the editor
 *
 * For details, see
 * https://nlScript.github.io/nlScript-java/#fine-tuning-autocompletion-parameterized-autocompletion
 */
public class Tutorial04 {

	public static void main(String[] args) {
		ImagePlus image = IJ.openImage("http://imagej.net/images/clown.jpg");
		image.show();
		Preprocessing preprocessing = new Preprocessing(image);

		Parser parser = new Parser();

		// In contrast to the previous tutorial, a third parameter is given, which specifies whether to
		// insert the entire sequence when auto-completing or not.
		//
		// The default is false, which means that once the user input reaches the filter size, the editor will wait
		// until a number is entered. This might be unintuitive, because it might not be clear to the user what is
		// expected (e.g. in which units to enter the value).
		//
		// If set to 'true', the entire sequence will be inserted, i.e. a placeholder for 'stddev' (which is selected
		// so that the user can readily overwrite it), concatenated with the literal 'pixel(s)'.
		parser.defineType("filter-size", "{stddev:float} pixel(s)", pn -> pn.evaluate("stddev"), true);

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