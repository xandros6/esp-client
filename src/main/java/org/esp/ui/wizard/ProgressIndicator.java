package org.esp.ui.wizard;

import java.util.ArrayList;
import java.util.List;

import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeButton;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Button.ClickEvent;

public class ProgressIndicator extends VerticalLayout {

    private static final String SELECTED_STYLE = "selected";

    private static final String UNSELECTED_STYLE = "progress-unselected";

    private List<NativeButton> buttons = new ArrayList<NativeButton>();

    public ProgressIndicator() {

        addStyleName("progress-indicator");

    }

    public NativeButton addElement(String caption) {
        NativeButton l = new NativeButton(caption);
        buttons.add(l);
        l.setStyleName(UNSELECTED_STYLE);
        addComponent(l);

        return l;
    }
    

    public void setSelected(int idx) {
        for (NativeButton label : buttons) {
            label.setStyleName(UNSELECTED_STYLE);
        }
        NativeButton label = buttons.get(idx);
        label.setStyleName(SELECTED_STYLE);
    }

}
