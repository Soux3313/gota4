package observer.view.text;

import javax.swing.text.DefaultCaret;
import javax.swing.text.JTextComponent;

@SuppressWarnings("serial")
public class NoTextSelectionCaret extends DefaultCaret {
	/**
	 * Initialize this caret to prevent selection of the component's text
	 * 
	 * @param textComponent
	 */
	public NoTextSelectionCaret(JTextComponent textComponent) {
		setBlinkRate(textComponent.getCaret().getBlinkRate());
		textComponent.setHighlighter(null);
	}

	@Override
	public int getMark() {
		return getDot();
	}
}
