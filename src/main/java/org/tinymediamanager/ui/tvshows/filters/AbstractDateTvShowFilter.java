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
package org.tinymediamanager.ui.tvshows.filters;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.tinymediamanager.core.tvshow.entities.TvShow;
import org.tinymediamanager.core.tvshow.entities.TvShowEpisode;
import org.tinymediamanager.ui.components.datepicker.DatePicker;

import net.miginfocom.swing.MigLayout;

/**
 * The class {@link AbstractDateTvShowFilter} provides a base implementation for date-based TV show filters with various comparison options.
 * <p>
 * Subclasses can use this to filter TV shows based on date fields with options like less than, greater than, equals, and between.
 * </p>
 *
 * @author Manuel Laggner
 */
public abstract class AbstractDateTvShowFilter extends AbstractTvShowUIFilter {
  protected DatePicker datePickerLow;
  protected DatePicker datePickerHigh;
  protected JLabel     lblTo;

  @Override
  public String getFilterValueAsString() {
    StringBuilder sb = new StringBuilder();
    Date dateLow = datePickerLow.getDate();
    Date dateHigh = datePickerHigh.getDate();

    if (dateLow != null) {
      sb.append(dateLow.getTime());
    }
    sb.append(",");
    if (dateHigh != null) {
      sb.append(dateHigh.getTime());
    }

    return sb.toString();
  }

  @Override
  public void setFilterValue(Object value) {
    if (value != null && StringUtils.isNotBlank(value.toString())) {
      String[] values = value.toString().split(",");
      try {
        if (values.length > 0 && StringUtils.isNotBlank(values[0])) {
          datePickerLow.setDate(new Date(Long.parseLong(values[0])));
        }
        if (values.length > 1 && StringUtils.isNotBlank(values[1])) {
          datePickerHigh.setDate(new Date(Long.parseLong(values[1])));
        }
      }
      catch (Exception e) {
        // ignored
      }
    }
  }

  @Override
  public void clearFilter() {
    datePickerLow.setDate(null);
    datePickerHigh.setDate(null);
  }

  @Override
  protected JComboBox<FilterOption> createOptionComboBox() {
    JComboBox<FilterOption> comboBox = new JComboBox<>(
        new FilterOption[] { FilterOption.LT, FilterOption.LE, FilterOption.EQ, FilterOption.GT, FilterOption.GE, FilterOption.BT });
    comboBox.addActionListener(l -> {
      lblTo.setVisible(comboBox.getSelectedItem() == FilterOption.BT);
      datePickerHigh.setVisible(comboBox.getSelectedItem() == FilterOption.BT);
    });
    comboBox.setSelectedItem(FilterOption.EQ);

    return comboBox;
  }

  @Override
  protected JComponent createFilterComponent() {
    JPanel panelFilterComponent = new JPanel(new MigLayout("insets 0", "[][][]", "[]"));

    datePickerLow = new DatePicker();
    datePickerLow.addPropertyChangeListener("date", e -> {
      if (getFilterState() != FilterState.INACTIVE) {
        filterChanged();
      }
    });
    panelFilterComponent.add(datePickerLow, "cell 0 0");

    lblTo = new JLabel("-");
    panelFilterComponent.add(lblTo, "cell 1 0");

    datePickerHigh = new DatePicker();
    datePickerHigh.addPropertyChangeListener("date", e -> {
      if (getFilterState() != FilterState.INACTIVE) {
        filterChanged();
      }
    });
    panelFilterComponent.add(datePickerHigh, "cell 2 0");

    return panelFilterComponent;
  }

  /**
   * Gets the date to compare for the given episode. Subclasses must implement this method.
   *
   * @param episode
   *          the episode to get the date from
   * @return the date to compare, or null if no date is available
   */
  protected abstract Date getDateFromEpisode(TvShowEpisode episode);

  @Override
  protected boolean accept(TvShow tvShow, List<TvShowEpisode> episodes, boolean invert) {
    Date dateLow = datePickerLow.getDate();
    Date dateHigh = datePickerHigh.getDate();

    if (dateLow == null && dateHigh == null) {
      return true;
    }

    try {
      for (TvShowEpisode episode : episodes) {
        Date episodeDate = getDateFromEpisode(episode);
        boolean foundEpisode = matchDate(episodeDate);

        if (invert && !foundEpisode) {
          return true;
        }
        else if (!invert && foundEpisode) {
          return true;
        }
      }
    }
    catch (Exception e) {
      return true;
    }

    return false;
  }

  /**
   * Matches a date against the filter criteria based on the selected filter option.
   * <p>
   * Compares dates at the day level (ignoring time) and returns true if the date matches the filter criteria.
   * </p>
   *
   * @param date
   *          the date to match
   * @return true if the date matches the filter criteria, false otherwise
   */
  protected boolean matchDate(Date date) {
    if (date == null) {
      return false;
    }

    Date dateLow = datePickerLow.getDate();
    Date dateHigh = datePickerHigh.getDate();

    if (dateLow == null && dateHigh == null) {
      return true;
    }

    FilterOption filterOption = getFilterOption();

    // normalize dates to compare only the day (remove time component)
    Calendar calDate = Calendar.getInstance();
    calDate.setTime(date);
    calDate = DateUtils.truncate(calDate, Calendar.DAY_OF_MONTH);

    if (dateLow != null) {
      Calendar calLow = Calendar.getInstance();
      calLow.setTime(dateLow);
      calLow = DateUtils.truncate(calLow, Calendar.DAY_OF_MONTH);

      if (filterOption == FilterOption.EQ && DateUtils.isSameDay(calDate, calLow)) {
        return true;
      }
      else if (filterOption == FilterOption.LT && calDate.before(calLow)) {
        return true;
      }
      else if (filterOption == FilterOption.LE && (DateUtils.isSameDay(calDate, calLow) || calDate.before(calLow))) {
        return true;
      }
      else if (filterOption == FilterOption.GE && (DateUtils.isSameDay(calDate, calLow) || calDate.after(calLow))) {
        return true;
      }
      else if (filterOption == FilterOption.GT && calDate.after(calLow)) {
        return true;
      }
      else if (filterOption == FilterOption.BT && dateHigh != null) {
        Calendar calHigh = Calendar.getInstance();
        calHigh.setTime(dateHigh);
        calHigh = DateUtils.truncate(calHigh, Calendar.DAY_OF_MONTH);

        boolean afterOrEqualLow = DateUtils.isSameDay(calDate, calLow) || calDate.after(calLow);
        boolean beforeOrEqualHigh = DateUtils.isSameDay(calDate, calHigh) || calDate.before(calHigh);

        return afterOrEqualLow && beforeOrEqualHigh;
      }
    }

    return false;
  }
}
