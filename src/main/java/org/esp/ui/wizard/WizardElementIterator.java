package org.esp.ui.wizard;

import java.util.List;

public class WizardElementIterator {
    
    private int nElems = 0;
    private int currentIndex = -1;
    
    private List<WizardElement> wizardElements;

    public WizardElementIterator(List<WizardElement> wizardElements) {
        this.wizardElements = wizardElements;
        this.nElems = wizardElements.size();
    }
    
    public WizardElement get(int idx) {
        currentIndex = idx;
        return wizardElements.get(idx);
    }

    public WizardElement next() {
        
        if (currentIndex < nElems - 1) {
            currentIndex++;
        }
        return wizardElements.get(currentIndex);
    }
    
    public WizardElement previous() {
        if (currentIndex > 0) {
            currentIndex--;
        } else if (currentIndex == -1 ) {
            return wizardElements.get(0);
        }
        return wizardElements.get(currentIndex);
    }

    public int getCurrentIdx() {
       return currentIndex; 
    }
}
