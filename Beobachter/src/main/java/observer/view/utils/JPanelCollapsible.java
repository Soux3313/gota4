package observer.view.utils;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;

@SuppressWarnings("serial")
public class JPanelCollapsible extends JPanel {
	// The title to display on the border of the panel
	private String title = "Title";
	// The border of the panel
	protected TitledBorder border;

	/**
	 * Create a new collapsible with the default mouse listener
	 */
	public JPanelCollapsible() {
		// Add the default mouse listener
		this(true);
	}

	/**
	 * Create a new collapsible with a custom mouse listener
	 * 
	 * @param addMouseListener
	 */
	public JPanelCollapsible(boolean addMouseListener) {
		// Create the border
		this.border = BorderFactory.createTitledBorder(this.title);
		this.setBorder(this.border);

		// Set the panel's default layout
		BorderLayout borderLayout = new BorderLayout();
		this.setLayout(borderLayout);

		// Add the default mouse listener
		if (addMouseListener)
			this.addMouseListener(this.mouseListener);
	}

	// The default mouse listener (toggle the panel's visibility on click)
	protected MouseListener mouseListener = new MouseAdapter() {
		@Override
		public void mousePressed(MouseEvent e) {
			toggleVisibility();
		}

		public void mouseEntered(MouseEvent evt) {
			setBackground(UIManager.getColor("TabbedPane.hoverColor"));
		}

		public void mouseExited(MouseEvent evt) {
			setBackground(UIManager.getColor("TabbedPane.background"));
		}
	};

	// A component listener to update the border's title if the panel's content is shown or hidden
	private ComponentListener contentComponentListener = new ComponentAdapter() {
		@Override
		public void componentShown(ComponentEvent e) {
			updateBorderTitle();
		}

		@Override
		public void componentHidden(ComponentEvent e) {
			updateBorderTitle();
		}
	};

	/**
	 * Returns the title of the panel's border
	 * 
	 * @return The title displayed on the panel's border
	 */
	public String getTitle() {
		return this.title;
	}

	/**
	 * Updates the title displayed on the panel's border
	 * 
	 * @param title The new text to display on the border
	 */
	public void setTitle(String title) {
		this.firePropertyChange("title", this.title, this.title = title);
	}

	@Override
	public Component add(Component component) {
		component.addComponentListener(this.contentComponentListener);
		Component newComponent = super.add(component);
		this.updateBorderTitle();
		return newComponent;
	}

	@Override
	public Component add(String name, Component component) {
		component.addComponentListener(this.contentComponentListener);
		Component newComponent = super.add(name, component);
		this.updateBorderTitle();
		return newComponent;
	}

	@Override
	public Component add(Component component, int index) {
		component.addComponentListener(this.contentComponentListener);
		Component newComponent = super.add(component, index);
		this.updateBorderTitle();
		return newComponent;
	}

	@Override
	public void add(Component component, Object constraints) {
		component.addComponentListener(this.contentComponentListener);
		super.add(component, constraints);
		this.updateBorderTitle();
	}

	@Override
	public void add(Component component, Object constraints, int index) {
		component.addComponentListener(this.contentComponentListener);
		super.add(component, constraints, index);
		this.updateBorderTitle();
	}

	@Override
	public void remove(int index) {
		Component component = this.getComponent(index);
		component.removeComponentListener(this.contentComponentListener);

		super.remove(index);
	}

	@Override
	public void remove(Component component) {
		component.removeComponentListener(this.contentComponentListener);

		super.remove(component);
	}

	@Override
	public void removeAll() {
		for (Component component : this.getComponents()) {
			component.removeComponentListener(this.contentComponentListener);
		}

		super.removeAll();
	}

	/**
	 * Toggle the visibility of this label (false if it was true, true if it was false)
	 */
	public void toggleVisibility() {
		int count = this.getComponentCount();

		if (count > 0 && (count > 1 || !(this.getComponent(0) instanceof JPanel) || ((JPanel) this.getComponent(0)).getComponentCount() > 0)) {
			this.toggleVisibility(this.hasInvisibleComponent());
		} else {
			this.toggleVisibility(false);
		}
	}

	/**
	 * Toggles the visibility of the child components
	 * 
	 * @param visible true if child components should be visible, false if not
	 */
	public void toggleVisibility(boolean visible) {
		// Update all child component's visibility
		for (Component component : this.getComponents()) {
			component.setVisible(visible);
		}

		// Update the border title afterwards
		this.updateBorderTitle();
	}

	/**
	 * Updates the label's visibility depending on whether it is empty or not
	 */
	public void update() {
		this.revalidate();

		// Get the child component count
		int count = this.getComponentCount();

		// If a valid child component was found, the label gets updated, otherwise it is set invisible
		if (count > 0 && (count > 1 || !(this.getComponent(0) instanceof JPanel) || ((JPanel) this.getComponent(0)).getComponentCount() > 0)) {
			this.toggleVisibility(!this.hasInvisibleComponent());
		} else {
			this.toggleVisibility(false);
		}
	}

	/**
	 * Updates the title on the border
	 */
	protected void updateBorderTitle() {
		String arrow = "";
		int count = this.getComponentCount();

		// Don't display an arrow, if the panel is empty, otherwise display the arrow depending on the visibility state
		if (count > 0 && (count > 1 || !(this.getComponent(0) instanceof JPanel) || ((JPanel) this.getComponent(0)).getComponentCount() > 0)) {
			arrow = (this.hasInvisibleComponent() ? "▼ " : "▶ ");
		}

		// Update the border title
		this.border.setTitle(" " + arrow + this.title + " ");
		this.repaint();
	}

	/**
	 * Returns whether or not this label has an invisible child component
	 * 
	 * @return true if the label has an invisible child component, false if not
	 */
	protected final boolean hasInvisibleComponent() {
		// Check all child components for an invisible component and return true if one was found
		for (Component component : this.getComponents()) {
			if (!component.isVisible()) {
				return true;
			}
		}

		// Return false otherwise
		return false;
	}

	@Override
	public Dimension getMaximumSize() {
		// Stretch to parent width
		return new Dimension(this.getParent().getWidth(), this.getPreferredSize().height);
	}

	@Override
	public Dimension getMinimumSize() {
		// Stretch to parent width
		return new Dimension(this.getParent().getWidth(), this.getPreferredSize().height);
	}

	@Override
	public Dimension getPreferredSize() {
		Dimension size = super.getPreferredSize();

		// If the label has a child component return the default preferred size, otherwise add 10 to make its height a bit taller
		if (this.getComponentCount() > 0)
			return size;
		else
			return new Dimension(this.getParent().getWidth(), size.height + 10);
	}
}
