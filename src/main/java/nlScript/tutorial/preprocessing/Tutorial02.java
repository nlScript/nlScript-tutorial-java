package nlScript.tutorial.preprocessing;

import nlScript.Evaluator;
import nlScript.ParsedNode;
import nlScript.Parser;
import nlScript.ui.ACEditor;
import ij.IJ;
import ij.ImagePlus;

/**
 * Tutorial02 extends the previous one by specifying a 2nd parameter to
 * 'defineSentence', an object of type Evaluator. Evaluator is an interface with a
 * single function evaluate(), which is called upon parsing the corresponding sentence.
 *
 * Note:
 * The Evaluator can be specified more concisely using a lambda expression. Here, an anonymous
 * class is used to be explicit on the types.
 *
 * For details, see
 * https://nlScript.github.io/nlScript-java/#evaluating-the-parsed-text
 */
public class Tutorial02 {

	public static void main(String[] args) {
		// Load an example image
		ImagePlus image = IJ.openImage("http://imagej.net/images/clown.jpg");
		image.show();

		// Create an instance of the processing backend.
		Preprocessing preprocessing = new Preprocessing(image);

		Parser parser = new Parser();
		parser.defineSentence(
				"Apply Gaussian blurring with a standard deviation of {stddev:float} pixel(s).",

				// The function specified here will be called upon parsing the sentence above
				new Evaluator() {
					@Override
					public Object evaluate(ParsedNode pn) {

						// The argument given to evaluate(), a ParsedNode, can be used to
						// evaluate the value of the sentence's variables, here 'stddev'.
						// They are accessed by name.
						double stdDev = (double) pn.evaluate("stddev");

						// Do the actual blurring, using the processing backend.
						preprocessing.gaussianBlur((float) stdDev);
						return null;
					}
				});

		new ACEditor(parser).setVisible(true);
	}
}