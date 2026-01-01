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
package org.tinymediamanager.ui.components.toast;

import java.awt.Component;
import java.awt.Window;

import javax.swing.SwingUtilities;

/**
 * The Class {@link TmmToastManager} provides utility methods to show toast notifications from anywhere in the UI.
 * <p>
 * This is particularly useful for panels embedded in dialogs that need to show toast messages but don't have direct access to the parent dialog.
 * </p>
 *
 * @author Manuel Laggner
 */
public class TmmToastManager {

  private TmmToastManager() {
    throw new IllegalAccessError();
  }

  /**
   * Show a toast message on the window containing the given component.
   * <p>
   * This method walks up the component hierarchy to find the parent window and displays a toast on it.
   * </p>
   *
   * @param component
   *          the component (e.g., a panel) from which to show the toast
   * @param message
   *          the message text to display
   * @param type
   *          the toast type (INFO, SUCCESS, WARNING, ERROR)
   * @param durationMs
   *          the duration in milliseconds before the toast fades out
   */
  public static void showToast(Component component, String message, TmmToast.ToastType type, int durationMs) {
    if (component == null) {
      return;
    }
    Window window = SwingUtilities.getWindowAncestor(component);
    if (window != null) {
      TmmToast toast = TmmToast.install(window);
      toast.showToast(message, type, durationMs);
    }
  }

  /**
   * Show a toast message with an optional title on the window containing the given component.
   * <p>
   * The title will be rendered in bold above the message text if provided.
   * </p>
   *
   * @param component
   *          the component (e.g., a panel) from which to show the toast
   * @param title
   *          the optional title (rendered bold); may be null or empty
   * @param message
   *          the message text to display
   * @param type
   *          the toast type (INFO, SUCCESS, WARNING, ERROR)
   * @param durationMs
   *          the duration in milliseconds before the toast fades out
   */
  public static void showToast(Component component, String title, String message, TmmToast.ToastType type, int durationMs) {
    if (component == null) {
      return;
    }
    Window window = SwingUtilities.getWindowAncestor(component);
    if (window != null) {
      TmmToast toast = TmmToast.install(window);
      toast.showToast(title, message, type, durationMs);
    }
  }

  /**
   * Show an INFO toast message with default 3 second duration.
   *
   * @param component
   *          the component from which to show the toast
   * @param message
   *          the message text to display
   */
  public static void showToast(Component component, String message) {
    showToast(component, message, TmmToast.ToastType.INFO, 3000);
  }

  /**
   * Show an INFO toast message with an optional title and default 3 second duration.
   *
   * @param component
   *          the component from which to show the toast
   * @param title
   *          the optional title (rendered bold); may be null or empty
   * @param message
   *          the message text to display
   */
  public static void showToast(Component component, String title, String message) {
    showToast(component, title, message, TmmToast.ToastType.INFO, 3000);
  }

  /**
   * Show a SUCCESS toast message with default 3 second duration.
   *
   * @param component
   *          the component from which to show the toast
   * @param message
   *          the message text to display
   */
  public static void showSuccessToast(Component component, String message) {
    showToast(component, message, TmmToast.ToastType.SUCCESS, 3000);
  }

  /**
   * Show a SUCCESS toast message with an optional title and default 3 second duration.
   *
   * @param component
   *          the component from which to show the toast
   * @param title
   *          the optional title (rendered bold); may be null or empty
   * @param message
   *          the message text to display
   */
  public static void showSuccessToast(Component component, String title, String message) {
    showToast(component, title, message, TmmToast.ToastType.SUCCESS, 3000);
  }

  /**
   * Show a WARNING toast message with default 4 second duration.
   *
   * @param component
   *          the component from which to show the toast
   * @param message
   *          the message text to display
   */
  public static void showWarningToast(Component component, String message) {
    showToast(component, message, TmmToast.ToastType.WARNING, 4000);
  }

  /**
   * Show a WARNING toast message with an optional title and default 4 second duration.
   *
   * @param component
   *          the component from which to show the toast
   * @param title
   *          the optional title (rendered bold); may be null or empty
   * @param message
   *          the message text to display
   */
  public static void showWarningToast(Component component, String title, String message) {
    showToast(component, title, message, TmmToast.ToastType.WARNING, 4000);
  }

  /**
   * Show an ERROR toast message with default 5 second duration.
   *
   * @param component
   *          the component from which to show the toast
   * @param message
   *          the message text to display
   */
  public static void showErrorToast(Component component, String message) {
    showToast(component, message, TmmToast.ToastType.ERROR, 5000);
  }

  /**
   * Show an ERROR toast message with an optional title and default 5 second duration.
   *
   * @param component
   *          the component from which to show the toast
   * @param title
   *          the optional title (rendered bold); may be null or empty
   * @param message
   *          the message text to display
   */
  public static void showErrorToast(Component component, String title, String message) {
    showToast(component, title, message, TmmToast.ToastType.ERROR, 5000);
  }

  /**
   * Get the TmmToast instance for the window containing the given component.
   * <p>
   * This is useful if you need direct access to the toast instance for advanced usage.
   * </p>
   *
   * @param component
   *          the component from which to get the toast
   * @return the TmmToast instance, or null if no window ancestor is found
   */
  public static TmmToast getToast(Component component) {
    if (component == null) {
      return null;
    }
    Window window = SwingUtilities.getWindowAncestor(component);
    if (window != null) {
      return TmmToast.install(window);
    }
    return null;
  }
}
