# TmmToast - Toast Notification System

## Overview

The `TmmToast` system provides elegant, non-intrusive toast notifications for TmmDialog instances. Toast messages appear as semi-transparent overlays on the right side of dialogs and automatically fade in and out.

## Features

- **Glass Pane Based**: Uses the dialog's glass pane as a topmost overlay layer
- **Multiple Toasts**: Supports stacking multiple toast messages vertically
- **Smooth Animations**: Fade-in, slide-in, and fade-out animations at 60 FPS with easing
- **Type-based Styling**: Four toast types (INFO, SUCCESS, WARNING, ERROR) with distinct colors
- **Text Wrapping**: Automatically wraps long messages to multiple lines
- **Optional Title**: Render an optional bold title above the message
- **Theme Aware**: Adapts INFO toast colors based on the current UI theme (light/dark)
- **Optimized Performance**: Timer only runs when toasts are active
- **Automatic Cleanup**: Automatically uninstalls when TmmDialog is disposed to prevent memory leaks

## Architecture

### TmmToast Class

The main class that provides the toast functionality. It extends `JComponent` and acts as a glass pane overlay.

**Key Features:**
- Singleton per window (using WeakHashMap to prevent memory leaks)
- Timer-based animation system (60 FPS)
- Supports multiple concurrent toasts with vertical stacking
- Automatic lifecycle management (fade-in → display → fade-out → removal)

### ToastType Enum

Defines the visual appearance of toasts:

- **INFO**: Neutral color (adapts to theme)
- **SUCCESS**: Green
- **WARNING**: Amber/Orange
- **ERROR**: Red

## Usage

### Basic Usage in TmmDialog

All `TmmDialog` subclasses automatically have access to toast methods:

```java
public class MyDialog extends TmmDialog {
  
  public MyDialog() {
    super("My Dialog", "myDialog");
    
    // Show a simple info toast
    showToast("Operation completed");
    
    // Show a success toast
    showSuccessToast("File saved successfully");
    
    // Show a warning toast
    showWarningToast("Connection is slow");
    
    // Show an error toast
    showErrorToast("Failed to load data");
    
    // Show a custom toast with specific duration
    showToast("Custom message", TmmToast.ToastType.INFO, 5000);
  }
}
```

Note: Title support is available via `TmmToastManager` or direct `TmmToast` usage.

### Usage from Embedded Panels (TmmToastManager)

When working with panels embedded in dialogs that don't have direct access to the parent dialog, use `TmmToastManager`:

```java
import org.tinymediamanager.ui.components.toast.TmmToastManager;

public class MySettingsPanel extends JPanel {
  
  private void onSaveButtonClick() {
    // Save logic...
    
    // Show toast using TmmToastManager - it automatically finds the parent window
    TmmToastManager.showSuccessToast(this, "Settings saved successfully");

    // Show toast with a bold title above the message
    TmmToastManager.showSuccessToast(this, "Operation complete", "Settings saved successfully");
  }
  
  private void onError() {
    TmmToastManager.showErrorToast(this, "Validation failed", "Failed to validate input");
  }
  
  private void onWarning() {
    TmmToastManager.showWarningToast(this, "Be careful", "Some values were adjusted");
  }
  
  private void onInfo() {
    TmmToastManager.showToast(this, "Processing...");
  }
}
```

**How it works:** `TmmToastManager` uses `SwingUtilities.getWindowAncestor(component)` to walk up the component hierarchy and find the parent `Window`, then installs and uses the toast system on that window.

### Available Methods

#### In TmmDialog (for dialog subclasses)

- `showToast(String message)` - Shows an INFO toast with 3 second duration.
- `showSuccessToast(String message)` - Shows a SUCCESS toast with 3 second duration.
- `showWarningToast(String message)` - Shows a WARNING toast with 4 second duration.
- `showErrorToast(String message)` - Shows an ERROR toast with 5 second duration.
- `showToast(String message, TmmToast.ToastType type, int durationMs)` - Shows a toast with custom type and duration.

#### In TmmToast (instance methods)

- `showToast(String message)` - INFO toast, 3s duration.
- `showToast(String message, ToastType type, int durationMs)` - Custom type and duration.
- `showToast(String title, String message)` - INFO toast with a bold title above the message, 3s duration.
- `showToast(String title, String message, ToastType type, int durationMs)` - Custom type/duration with bold title.

#### In TmmToastManager (for embedded panels and components)

- `TmmToastManager.showToast(Component, String message)` - INFO toast, 3s duration.
- `TmmToastManager.showToast(Component, String title, String message)` - INFO toast with bold title, 3s duration.
- `TmmToastManager.showToast(Component, String message, ToastType type, int durationMs)` - Custom type/duration.
- `TmmToastManager.showToast(Component, String title, String message, ToastType type, int durationMs)` - Custom
  type/duration with bold title.
- `TmmToastManager.showSuccessToast(Component, String message)` - SUCCESS toast, 3s duration.
- `TmmToastManager.showSuccessToast(Component, String title, String message)` - SUCCESS toast with bold title, 3s
  duration.
- `TmmToastManager.showWarningToast(Component, String message)` - WARNING toast, 4s duration.
- `TmmToastManager.showWarningToast(Component, String title, String message)` - WARNING toast with bold title, 4s
  duration.
- `TmmToastManager.showErrorToast(Component, String message)` - ERROR toast, 5s duration.
- `TmmToastManager.showErrorToast(Component, String title, String message)` - ERROR toast with bold title, 5s duration.
- `TmmToastManager.getToast(Component)` - Gets the TmmToast instance for advanced usage.

### Direct TmmToast Usage (Advanced)

For non-TmmDialog windows, you can use TmmToast directly:

```java
Window myWindow = ...;
TmmToast toast = TmmToast.install(myWindow);

toast.showToast("Hello", TmmToast.ToastType.INFO, 3000);
// With title:
toast.

showToast("Operation complete","All items processed successfully",TmmToast.ToastType.SUCCESS, 3000);

// When done, uninstall to restore previous glass pane
TmmToast.uninstall(myWindow);
```

## Memory Management

The toast system is designed to prevent memory leaks:

### Automatic Cleanup in TmmDialog

**TmmDialog automatically uninstalls the toast when `dispose()` is called**, so you don't need to do anything manually:

```java
public class MyDialog extends TmmDialog {
  public MyDialog() {
    super("My Dialog", "myDialog");
    showToast("Dialog opened");
  }
  // No cleanup needed - automatically handled in dispose()
}
```

### Manual Cleanup (When Needed)

If you're using toasts outside of TmmDialog or need explicit cleanup:

```java
// From a window
TmmToast.uninstall(myWindow);
```

### What Uninstall Does

When uninstalling, the toast system:
1. Stops the animation timer
2. Clears all pending toast messages
3. Removes component listeners from the window
4. Restores the previous glass pane (if any)
5. Removes the toast instance from memory

### WeakHashMap Protection

The toast system uses `WeakHashMap` to store window-to-toast mappings, which means:
- If a window is garbage collected, its toast entry is automatically removed
- No strong references are held that would prevent garbage collection
- Even if you forget to uninstall, the memory will eventually be reclaimed

## Configuration

### Animation Timing
- **Frame Rate**: 60 FPS for ultra-smooth animations
- **Fade In**: 250ms with cubic easing
- **Fade Out**: 600ms for smooth disappearance
- **Slide Animation**: 30px with ease-out cubic easing
- **Default Duration**:
  - INFO: 3000ms (3 seconds)
  - SUCCESS: 3000ms (3 seconds)
  - WARNING: 4000ms (4 seconds)
  - ERROR: 5000ms (5 seconds)

### Layout Constants
- **Margin from edge**: 16px
- **Gap between toasts**: 12px
- **Max toast width**: 400px
- **Padding (horizontal)**: 16px
- **Padding (vertical)**: 12px
- **Border radius**: 8px
- **Title/message gap**: 4px

## Testing

A test dialog is provided for demonstration and testing purposes:

```java
ToastTestDialog dialog = new ToastTestDialog();
dialog.setVisible(true);
```

The test dialog includes buttons to:
- Show each toast type
- Show multiple toasts simultaneously
- Show a long message with text wrapping
- Show titled toasts

## Implementation Details

### Glass Pane Integration

The toast system uses the dialog's glass pane to ensure toasts appear above all other components. When a toast is shown for the first time, it automatically:

1. Installs itself as the glass pane of the window
2. Makes the glass pane visible
3. Sets up a component listener for window resizing

### Animation System

A Swing `Timer` runs at 60 FPS to update toast animations smoothly:

1. **Fade In** (0% → 100% alpha over 250ms with ease-out cubic)
2. **Display** (100% alpha for specified duration)
3. **Fade Out** (100% → 0% alpha over 600ms)
4. **Removal** (toast removed from list when alpha reaches ~0%)

The timer automatically starts when a toast is shown and stops when no toasts are active, optimizing CPU usage.

### Thread Safety

All toast operations are executed on the Event Dispatch Thread (EDT) using `SwingUtilities.invokeLater()`.

### Memory Management

- Uses `WeakHashMap` to store toast instances per window
- Automatic cleanup when TmmDialog is disposed
- Manual uninstall available via `TmmToast.uninstall(window)`
- Restores previous glass pane on uninstall
- Removes all listeners and stops timers to prevent memory leaks
- Weak references allow garbage collection of closed windows

## Best Practices

1. **Use Appropriate Types**: Choose the toast type that matches the message severity
2. **Keep Messages Concise**: While wrapping is supported, short messages work best
3. **Avoid Spam**: Don't show too many toasts in rapid succession
4. **Duration Guidelines**: 
   - Short messages: 2-3 seconds
   - Normal messages: 3-4 seconds
   - Important messages: 5-6 seconds
5. **User Actions**: Use toasts for confirmations after user actions (e.g., "Saved", "Deleted")

## Future Enhancements

Potential improvements for future versions:

- Custom icons per toast type
- Click-to-dismiss functionality
- Progress bar support for long operations
- Action buttons (e.g., "Undo")
- Sound notifications
- Position customization (left/right/top/bottom)
- Accessibility features (screen reader support)

## Examples

### From TmmDialog

```java
private void saveData() {
  try {
    // Save logic here
    showSuccessToast("Data saved successfully");
  } catch (Exception e) {
    showErrorToast("Failed to save: " + e.getMessage());
  }
}
```

### From Embedded Panel

```java
public class MySettingsPanel extends JPanel {
  
  private void onApplySettings() {
    if (validateInput()) {
      applySettings();
      TmmToastManager.showSuccessToast(this, "All done", "Settings applied successfully");
    } else {
      TmmToastManager.showWarningToast(this, "Please review", "Some validation issues found");
    }
  }
}
```

### Progress Notification

```java
private void processItems() {
  int total = items.size();
  int processed = 0;
  
  for (Item item : items) {
    // Process item
    processed++;
    if (processed % 10 == 0) {
      TmmToastManager.showToast(this, "Progress", "Processed " + processed + " of " + total);
    }
  }

  TmmToastManager.showSuccessToast(this, "Completed", "All items processed!");
}
```

### Validation Warning

```java
private void validateInput() {
  if (inputField.getText().isEmpty()) {
    TmmToastManager.showWarningToast(this, "Missing value", "Please enter a value");
    return;
  }
  // Continue processing
}
```
