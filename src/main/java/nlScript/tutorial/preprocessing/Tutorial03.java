package nlScript.tutorial.preprocessing;

import nlScript.Parser;
import nlScript.ui.ACEditor;
import ij.IJ;
import ij.ImagePlus;

/**
 * Tutorial03 introduces custom types.
 *
 * Custom types are specified similar to sentences, but
 * - an explicit type name is given
 * - their evaluator typically doesn't return null, but an arbitrary Java object representing that type
 *
 * In this particular example, using a custom type for the filter size doesn't provide any advantage. In general, one
 * would use custom types to
 * - reuse them in multiple sentences or other types
 * - to fine-tune the way they are auto-completed in the editor.
 *
 * This will be demonstrated in subsequent tutorials.
 *
 * For details, see
 * https://nlScript.github.io/nlScript-java/#built-in-types
 * https://nlScript.github.io/nlScript-java/#custom-types-and-type-hierarchy
 * https://nlScript.github.io/nlScript-java/custom-types.html
 * https://nlScript.github.io/nlScript-java/type-hierarchy.html
 */
public class Tutorial03 {

	public static void main(String[] args) {
		ImagePlus image = IJ.openImage("http://imagej.net/images/clown.jpg");
		image.show();
		Preprocessing preprocessing = new Preprocessing(image);

		Parser parser = new Parser();

		// Create a custom type 'filter-size'
		parser.defineType(
				// The name of the type:
				"filter-size",
				// The pattern to parse (i.e. a floating point number, followed by the literal " pixel(s)".
				"{stddev:float} pixel(s)",
				// An Evaluator, which in this case just returns the parsed standard deviation as a Double.
				// In principle, a custom type can evaluate to any Java object
				pn -> pn.evaluate("stddev"));

		// Custom types can then be used to define other custom types, or to define sentences
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