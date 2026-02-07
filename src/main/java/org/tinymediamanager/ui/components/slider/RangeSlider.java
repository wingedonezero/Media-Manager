/*
 * Copyright 2012 - 2026 Manuel Laggner
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.tinymediamanager.ui.components.slider;

import java.util.Dictionary;
import java.util.Enumeration;

import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JComponent;
import javax.swing.JSlider;

import org.tinymediamanager.ui.TmmFontHelper;

/**
 * A slider component that allows selection of a value range with two thumbs.
 * <p>
 * The {@code RangeSlider} extends {@link JSlider} to support a lower and upper value, enabling users to select a continuous interval within the
 * slider's bounds. It provides methods to get and set the low and high values, as well as to control whether the range between the thumbs is
 * draggable.
 * </p>
 *
 * @author Manuel Laggner
 */
public class RangeSlider extends JSlider {
  private static final String uiClassID                      = "RangeSliderUI";

  private boolean             _rangeDraggable                = true;
  public static final String  CLIENT_PROPERTY_MOUSE_POSITION = "RangeSlider.mousePosition";
  public static final String  CLIENT_PROPERTY_ADJUST_ACTION  = "RangeSlider.adjustAction";
  public static final String  PROPERTY_LOW_VALUE             = "lowValue";
  public static final String  PROPERTY_HIGH_VALUE            = "highValue";

  /**
   * Creates a horizontal slider using the specified min and max.
   *
   * @param min
   *          the minimum value of the slider.
   * @param max
   *          the maximum value of the slider.
   */
  public RangeSlider(int min, int max) {
    super(new DefaultBoundedRangeModel(min, max - min, min, max));
  }

  /**
   * Returns the UI class ID for this component.
   *
   * @return the UI class ID string
   */
  @Override
  public String getUIClassID() {
    return uiClassID;
  }

  /**
   * Checks if the range slider is in its initial, unchanged state.
   * 
   * @return true if the low value equals the minimum and the high value equals the maximum, false otherwise
   */
  public boolean isUnchanged() {
    return getLowValue() == getMinimum() && getHighValue() == getMaximum();
  }

  /**
   * Returns the current low value of the range slider.
   *
   * @return the low value
   */
  public int getLowValue() {
    return getModel().getValue();
  }

  /**
   * Returns the current high value of the range slider.
   *
   * @return the high value
   */
  public int getHighValue() {
    return getModel().getValue() + getModel().getExtent();
  }

  /**
   * Checks if the specified value is within the current range.
   *
   * @param value
   *          the value to check
   * @return true if the value is within the range, false otherwise
   */
  public boolean contains(int value) {
    return (value >= getLowValue() && value <= getHighValue());
  }

  /**
   * Sets the value of the slider. Depending on the mouse position property, this sets either the low or high value.
   *
   * @param value
   *          the value to set
   */
  @Override
  public void setValue(int value) {
    Object clientProperty = getClientProperty(CLIENT_PROPERTY_MOUSE_POSITION);
    if (clientProperty != null) {
      if (Boolean.TRUE.equals(clientProperty)) {
        setLowValue(value);
      }
      else {
        setHighValue(value);
      }
    }
    else {
      setLowValue(value);
    }
  }

  /**
   * Sets the low value of the range slider.
   *
   * @param lowValue
   *          the new low value
   */
  public void setLowValue(int lowValue) {
    int old = getLowValue();
    int high;
    if ((lowValue + getModel().getExtent()) > getMaximum()) {
      high = getMaximum();
    }
    else {
      high = getHighValue();
    }
    int extent = high - lowValue;

    Object property = getClientProperty(CLIENT_PROPERTY_ADJUST_ACTION);
    getModel().setRangeProperties(lowValue, extent, getMinimum(), getMaximum(),
        property == null || (!property.equals("scrollByBlock") && !property.equals("scrollByUnit")));
    firePropertyChange(PROPERTY_LOW_VALUE, old, getLowValue());

  }

  /**
   * Sets the high value of the range slider.
   *
   * @param highValue
   *          the new high value
   */
  public void setHighValue(int highValue) {
    int old = getHighValue();
    getModel().setExtent(highValue - getLowValue());
    firePropertyChange(PROPERTY_HIGH_VALUE, old, getHighValue());
  }

  /**
   * Returns whether the range is draggable. If true, the user can drag the area between the two thumbs to move the range.
   *
   * @return true if the range is draggable, false otherwise
   */
  public boolean isRangeDraggable() {
    return _rangeDraggable;
  }

  /**
   * Sets whether the range is draggable. If true, the user can drag the area between the two thumbs to move the range.
   *
   * @param rangeDraggable
   *          true to make the range draggable, false otherwise
   */
  public void setRangeDraggable(boolean rangeDraggable) {
    _rangeDraggable = rangeDraggable;
  }

  /**
   * Sets the label table for the slider.
   * 
   * @param labels
   *          new {@code Dictionary} of labels, or {@code null} to remove all labels
   */
  @Override
  public void setLabelTable(Dictionary labels) {
    super.setLabelTable(labels);

    // reduce font size of labels
    Dictionary<?, ?> labelTable = getLabelTable();

    Enumeration<?> enumeration = labelTable.keys();
    while (enumeration.hasMoreElements()) {
      Object value = labelTable.get(enumeration.nextElement());
      if (value instanceof JComponent component) {
        TmmFontHelper.changeFont(component, TmmFontHelper.L1);
      }
    }
  }
}
