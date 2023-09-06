package observer.view.text;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;

public class IntegerDocumentFilter extends DocumentFilter {
	private int maxChars;

	/**
	 * Initializes this document filter with an infinte max length
	 */
	public IntegerDocumentFilter() {
		this(-1);
	}

	/**
	 * Intializes this document filter with the given max length
	 * 
	 * @param maxCharAmount
	 */
	public IntegerDocumentFilter(int maxCharAmount) {
		this.maxChars = maxCharAmount;
	}

	@Override
	public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
		this.replace(fb, offset, 0, string, attr);
	}

	private boolean test(String text) {
		if (text == null || text.length() == 0)
			return true;

		try {
			Integer.parseInt(text);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	@Override
	public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attr) throws BadLocationException {

		Document doc = fb.getDocument();
		StringBuilder sb = new StringBuilder();
		sb.append(doc.getText(0, doc.getLength()));
		sb.replace(offset, offset + length, text);

		if (test(sb.toString())) {
			if (this.maxChars < 0) {
				super.replace(fb, offset, length, text, attr);
			} else {
				int overLimit = (fb.getDocument().getLength() + text.length()) - this.maxChars - length;

				if (overLimit > 0) {
					text = text.substring(0, text.length() - overLimit);
				}

				if (text.length() > 0) {
					super.replace(fb, offset, length, text, attr);
				}
			}
		} else {
		}
	}

	@Override
	public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
		Document doc = fb.getDocument();
		StringBuilder sb = new StringBuilder();
		sb.append(doc.getText(0, doc.getLength()));
		sb.delete(offset, offset + length);

		if (test(sb.toString())) {
			super.remove(fb, offset, length);
		} else {
		}
	}
}
