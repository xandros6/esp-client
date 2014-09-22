package org.esp.publisher.ui;

import java.util.HashMap;
import java.util.Map;

import org.esp.publisher.MapPublisher;

import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.UI;

/**
 * 
 * @author Will Temperley
 *
 *         A simple navigation bar
 *
 */
public class NavMenu extends MenuBar implements ViewChangeListener {

	private Map<String, MenuItem> menuMap = new HashMap<String, MenuItem>();

	private MenuItem checkedItem;

	public NavMenu() {
		add("Home", ViewModule.HOME);
		add("Publish", ViewModule.PUBLISH);
		addStyleName("nav-menu");
	}

	public void select(String viewName) {
		MenuItem menuItem = menuMap.get(viewName);
		setCheckedItem(menuItem);
	}

	private void add(String name, final String viewName) {

		final MenuItem menuItem = addItem(name, null);
		menuMap.put(viewName, menuItem);
		menuItem.setCheckable(true);
		menuItem.setCommand(new Command() {
			@Override
			public void menuSelected(MenuItem selectedItem) {
				selectedItem.setChecked(false);
				navigate(viewName);
			}
		});
	}

	private void setCheckedItem(final MenuItem menuItem) {
		if (checkedItem != null) {
			checkedItem.setChecked(false);
		}
		checkedItem = menuItem;
		if (menuItem != null) {
			menuItem.setChecked(true);
		}
	}

	private void navigate(String whereTo) {
		Navigator nav = UI.getCurrent().getNavigator();
		nav.navigateTo(whereTo);

	}

	String previousViewName = null;

	@Override
	public boolean beforeViewChange(ViewChangeEvent event) {
		if (event.getOldView() instanceof MapPublisher) {
			this.select(previousViewName);
		}
		return true;
	}

	@Override
	public void afterViewChange(ViewChangeEvent event) {
		previousViewName = event.getViewName();
		this.select(event.getViewName());
	}

}
