/**
 * Computational Intelligence Library (CIlib)
 * Copyright (C) 2003 - 2010
 * Computational Intelligence Research Group (CIRG@UP)
 * Department of Computer Science
 * University of Pretoria
 * South Africa
 *
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, see <http://www.gnu.org/licenses/>.
 */
package net.sourceforge.cilib.io.transform;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract DataOperator that applies the data operation to a selected
 * number of elements only. Selection of elements occurs numerically as a list
 * integers. The concrete extension of the class processes the list to determine
 * selected items.
 * @author andrich
 */
public abstract class SelectiveDataOperator implements DataOperator {

    protected List<Integer> selectedItems;

    /**
     * Default constructor. Initializes selectedItems.
     */
    public SelectiveDataOperator() {
        selectedItems = new ArrayList<Integer>();
    }

    /**
     * Add number to selected items.
     * @param selected a selected item.
     */
    public void addSelection(int selected) {
        selectedItems.add(selected);
    }

    /**
     * Add all numbers to selected items.
     * @param selection a number of selected items.
     */
    public void addSelection(List<Integer> selection) {
        selectedItems.addAll(selection);
    }

    /**
     * Parses the string and adds the selection to the selected items.
     * @param selection
     */
    public void addSelection(String selection) {
        List<Integer> selectionList = parseSelectionString(selection);
        for (Integer item : selectionList) {
            this.addSelection(item);
        }
    }

    /**
     * Parses the string for a range representing a selection in the form:
     * lower_bound : upper_bound
     * @param selection string in the form: "int colon int".
     * @return list of integers representing selection.
     */
    public static List<Integer> parseSelectionString(String selection) {
        String[] tokens = selection.split("[:]");
        if (tokens.length != 2)
            throw new UnsupportedOperationException("Expected string: \"lower_bound : upper_bound\"");
        int lower = -1;
        try {
            lower = Integer.parseInt(tokens[0].trim());
        }
        catch (NumberFormatException ex) {
            throw new UnsupportedOperationException("Lower bound not a valid integer: "+tokens[0]);
        }

        int upper = -1;
        try {
            upper = Integer.parseInt(tokens[1].trim());
        }
        catch (NumberFormatException ex) {
            throw new UnsupportedOperationException("Upper bound not a valid integer: "+tokens[1]);
        }

        if (lower > upper) {
            throw new UnsupportedOperationException("Lower bound is larger than upper bound.");
        }

        List<Integer> selectionList = new ArrayList<Integer>();
        for (int i = lower; i <= upper; i++) {
            selectionList.add(i);
        }
        return selectionList;
    }

    /**
     * Gets the selected items.
     * @return the selected items.
     */
    public List<Integer> getSelectedItems() {
        return selectedItems;
    }

    /**
     * Sets the selected items.
     * @param selectedItems the new selected items.
     */
    public void setSelectedItems(List<Integer> selectedItems) {
        this.selectedItems = selectedItems;
    }
}
