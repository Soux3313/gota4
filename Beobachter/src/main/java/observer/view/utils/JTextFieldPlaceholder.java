package observer.view.utils;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;

import javax.swing.JTextField;
import javax.swing.text.Document;

@SuppressWarnings("serial")
public class JTextFieldPlaceholder extends JTextField {
	// The placeholder text
	private String placeholder;

	public JTextFieldPlaceholder() {
	}

	public JTextFieldPlaceholder(Document document, String text, int columns) {
		super(document, text, columns);
	}

	public JTextFieldPlaceholder(int columns) {
		super(columns);
	}

	public JTextFieldPlaceholder(String text) {
		super(text);
	}

	public JTextFieldPlaceholder(String text, int columns) {
		super(text, columns);
	}

	/**
	 * Returns the placeholder text that is being displayed if no text is entered in this text field
	 * 
	 * @return The placeholder text
	 */
	public String getPlaceholder() {
		return this.placeholder;
	}

	/**
	 * Updates the placeholder text
	 * 
	 * @param string The new placeholder text
	 */
	public void setPlaceholder(String string) {
		this.placeholder = string;
	}

	@Override
	protected void paintComponent(Graphics graphics) {
		super.paintComponent(graphics);

		// Don't draw the placeholder if it is undefined or a text is entered in the text field
		if (this.placeholder == null || this.placeholder.length() == 0 || this.getText().length() > 0) {
			return;
		}

		// Draw the placeholder
		Graphics2D graphics2d = (Graphics2D) graphics;
		graphics2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		graphics2d.setColor(this.getDisabledTextColor());
		graphics2d.drawString(this.placeholder, this.getInsets().left, graphics.getFontMetrics().getMaxAscent() + this.getInsets().top);
	}

	@Override
	public Dimension getPreferredSize() {
		Dimension size = super.getPreferredSize();

		// Scale the text field to show the whole placeholder text
		if (this.placeholder != null && this.placeholder.length() != 0) {
			Insets insets = this.getInsets();
			size.width = this.placeholder.length() * this.getColumnWidth() + insets.left + insets.right;
		}

		return size;
	}
}
