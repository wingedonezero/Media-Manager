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
package org.tinymediamanager.ui;

import java.awt.Component;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import javax.swing.AbstractButton;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JSpinner;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.SwingPropertyChangeSupport;
import javax.swing.text.JTextComponent;

import org.jetbrains.annotations.NotNull;
import org.tinymediamanager.core.TmmResourceBundle;
import org.tinymediamanager.ui.components.button.TriStateCheckBox;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * An abstract implementation for easier usage of the ITmmUIFilter
 * 
 * @author Manuel Laggner
 */
public abstract class AbstractTmmUIFilter<E> implements ITmmUIFilter<E> {
  /**
   * an object mapper which can be used to transform filters via/to JSON
   */
  protected static ObjectMapper           objectMapper          = new ObjectMapper();

  protected final TriStateCheckBox        checkBox;
  protected final JLabel                  label;
  protected final JComboBox<FilterOption> cbOption;
  protected final JComponent              filterComponent;
  protected final ActionListener          checkBoxActionListener;
  protected final ActionListener          filterComponentActionListener;
  protected final ChangeListener          changeListener;

  protected final PropertyChangeSupport   propertyChangeSupport = new SwingPropertyChangeSupport(this, true);

  public AbstractTmmUIFilter() {
    // always fire the change event if the checkbox has been changed
    checkBoxActionListener = e -> filterChanged();
    // the filter components only need to fire the change listener if the checkbox is active
    filterComponentActionListener = e -> {
      if (getFilterState() != FilterState.INACTIVE) {
        filterChanged();
      }
    };
    changeListener = e -> {
      if (getFilterState() != FilterState.INACTIVE) {
        filterChanged();
      }
    };

    this.checkBox = new TriStateCheckBox();
    this.checkBox.setToolTipText(TmmResourceBundle.getString("filter.hint"));
    this.label = createLabel();
    this.filterComponent = createFilterComponent();
    this.cbOption = createOptionComboBox();
    if (this.cbOption != null) {
      installFilterOptionTooltips();
      this.cbOption.addActionListener(filterComponentActionListener);
    }
    this.checkBox.addActionListener(checkBoxActionListener);

    if (this.filterComponent instanceof JTextComponent textComponent) {
      textComponent.getDocument().addDocumentListener(new DocumentListener() {
        @Override
        public void removeUpdate(DocumentEvent e) {
          if (getFilterState() != FilterState.INACTIVE) {
            filterChanged();
          }
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
          if (getFilterState() != FilterState.INACTIVE) {
            filterChanged();
          }
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
          if (getFilterState() != FilterState.INACTIVE) {
            filterChanged();
          }
        }
      });
    }
    else if (this.filterComponent instanceof AbstractButton abstractButton) {
      abstractButton.addActionListener(filterComponentActionListener);
    }
    else if (this.filterComponent instanceof JComboBox) {
      ((JComboBox<?>) this.filterComponent).addActionListener(filterComponentActionListener);
    }
    else if (this.filterComponent instanceof JSpinner jSpinner) {
      jSpinner.addChangeListener(changeListener);
    }
  }

  @Override
  public JCheckBox getCheckBox() {
    return checkBox;
  }

  @Override
  public JLabel getLabel() {
    return label;
  }

  @Override
  public JComponent getFilterComponent() {
    return filterComponent;
  }

  protected abstract JLabel createLabel();

  /**
   * create the combobox for the option - per default only EQ is offered. Subclasses may override this to offer other options
   *
   * @return the created {@link JComboBox}
   */
  protected JComboBox<FilterOption> createOptionComboBox() {
    // when there is _no_ filter component or only EQ, we don't need a combo box
    return null;
  }

  /**
   * Installs tooltips for filter option comboboxes (popup entries + selected value tooltip).
   */
  private void installFilterOptionTooltips() {
    cbOption.setRenderer(new FilterOptionRenderer());
    updateFilterOptionToolTip();
    cbOption.addActionListener(e -> updateFilterOptionToolTip());
  }

  /**
   * Updates the tooltip of the selected filter option.
   */
  private void updateFilterOptionToolTip() {
    cbOption.setToolTipText(getFilterOptionToolTip(getFilterOption()));
  }

  /**
   * Gets the tooltip text for the given filter option.
   *
   * @param filterOption
   *          the filter option
   * @return the localized tooltip text
   */
  private String getFilterOptionToolTip(FilterOption filterOption) {
    return switch (filterOption) {
      case LT -> TmmResourceBundle.getString("filteroption.lt");
      case LE -> TmmResourceBundle.getString("filteroption.le");
      case EQ -> TmmResourceBundle.getString("filteroption.eq");
      case GE -> TmmResourceBundle.getString("filteroption.ge");
      case GT -> TmmResourceBundle.getString("filteroption.gt");
      case BT -> TmmResourceBundle.getString("filteroption.bt");
      case ANY -> TmmResourceBundle.getString("filteroption.any");
      case ALL -> TmmResourceBundle.getString("filteroption.all");
    };
  }

  /**
   * Renderer for displaying tooltip texts for filter options in the combobox popup.
   */
  private class FilterOptionRenderer extends DefaultListCellRenderer {
    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
      Component component = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

      if (index >= 0) {
        if (isSelected && value instanceof FilterOption filterOption) {
          list.setToolTipText(getFilterOptionToolTip(filterOption));
        }
        else {
          list.setToolTipText(null);
        }
      }

      if (component instanceof JComponent jComponent && value instanceof FilterOption filterOption) {
        jComponent.setToolTipText(getFilterOptionToolTip(filterOption));
      }

      return component;
    }
  }

  @Override
  public JComboBox<FilterOption> getFilterOptionComboBox() {
    return cbOption;
  }

  protected abstract JComponent createFilterComponent();

  /**
   * get the filter state
   *
   * @return the filter state (ACTIVE, ACTIVE_NEGATIVE, INACTIVE)
   */
  @Override
  public FilterState getFilterState() {
    if (checkBox.isMixed()) {
      return FilterState.ACTIVE_NEGATIVE;
    }
    else if (!checkBox.isMixed() && checkBox.isSelected()) {
      return FilterState.ACTIVE;
    }
    return FilterState.INACTIVE;
  }

  /**
   * set the filter state (ACTIVE, ACTIVE_NEGATIVE, INACTIVE)
   *
   * @param state
   *          the state
   */
  @Override
  public void setFilterState(FilterState state) {
    switch (state) {
      case ACTIVE -> checkBox.setSelected(true);
      case ACTIVE_NEGATIVE -> checkBox.setMixed(true);
      case INACTIVE -> checkBox.setSelected(false);
    }
  }

  @Override
  public @NotNull FilterOption getFilterOption() {
    if (cbOption != null) {
      Object selectedItem = cbOption.getSelectedItem();
      if (selectedItem instanceof FilterOption filterOption) {
        return filterOption;
      }
    }

    return FilterOption.EQ;
  }

  @Override
  public void setFilterOption(@NotNull FilterOption filterOption) {
    if (cbOption != null) {
      cbOption.setSelectedItem(filterOption);
    }
  }

  /**
   * delegate the filter changed event to our listeners
   */
  protected void filterChanged() {
    SwingUtilities.invokeLater(() -> firePropertyChange(ITmmUIFilter.FILTER_CHANGED, false, true));
  }

  /**
   * Adds the property change listener.
   * 
   * @param listener
   *          the listener
   */
  @Override
  public void addPropertyChangeListener(PropertyChangeListener listener) {
    propertyChangeSupport.addPropertyChangeListener(listener);
  }

  /**
   * Adds the property change listener.
   * 
   * @param propertyName
   *          the property name
   * @param listener
   *          the listener
   */
  @Override
  public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
    propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
  }

  /**
   * Removes the property change listener.
   * 
   * @param listener
   *          the listener
   */
  @Override
  public void removePropertyChangeListener(PropertyChangeListener listener) {
    propertyChangeSupport.removePropertyChangeListener(listener);
  }

  /**
   * Removes the property change listener.
   * 
   * @param propertyName
   *          the property name
   * @param listener
   *          the listener
   */
  @Override
  public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
    propertyChangeSupport.removePropertyChangeListener(propertyName, listener);
  }

  /**
   * Fire property change.
   * 
   * @param propertyName
   *          the property name
   * @param oldValue
   *          the old value
   * @param newValue
   *          the new value
   */
  protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
    propertyChangeSupport.firePropertyChange(propertyName, oldValue, newValue);
  }

  /**
   * Fire property change.
   * 
   * @param evt
   *          the evt
   */
  protected void firePropertyChange(PropertyChangeEvent evt) {
    propertyChangeSupport.firePropertyChange(evt);
  }
}
