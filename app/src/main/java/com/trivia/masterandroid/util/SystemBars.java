package com.trivia.masterandroid.util;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.view.Window;

import com.trivia.masterandroid.R;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

/**
 * Edge-to-edge: the window background is painted behind the status and navigation bars,
 * and screens add the bar sizes (and the keyboard) as padding so content is never covered.
 */
public final class SystemBars {

    private SystemBars() {
    }

    public static void enable(Activity activity) {
        Window window = activity.getWindow();
        WindowCompat.setDecorFitsSystemWindows(window, false);
        window.setStatusBarColor(Color.TRANSPARENT);
        window.setNavigationBarColor(Color.TRANSPARENT);
        if (Build.VERSION.SDK_INT >= 29) {
            window.setNavigationBarContrastEnforced(false);
            window.setStatusBarContrastEnforced(false);
        }
        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(window, window.getDecorView());
        controller.setAppearanceLightStatusBars(true);
        controller.setAppearanceLightNavigationBars(true);
        addStatusBarScrim(activity);
    }

    /** Opaque strip in the window color so scrolling content does not run under the clock/icons. */
    private static void addStatusBarScrim(Activity activity) {
        ViewGroup content = activity.findViewById(android.R.id.content);
        View scrim = new View(activity);
        scrim.setBackgroundColor(activity.getColor(R.color.paper));
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, Gravity.TOP);
        content.addView(scrim, lp);
        ViewCompat.setOnApplyWindowInsetsListener(scrim, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.statusBars()
                    | WindowInsetsCompat.Type.displayCutout());
            ViewGroup.LayoutParams params = v.getLayoutParams();
            if (params.height != bars.top) {
                params.height = bars.top;
                v.setLayoutParams(params);
            }
            return insets;
        });
        ViewCompat.requestApplyInsets(scrim);
    }

    /** Adds the system bar insets to the view's current padding (top and/or bottom). */
    public static void pad(View view, boolean top, boolean bottom) {
        final int baseTop = view.getPaddingTop();
        final int baseBottom = view.getPaddingBottom();
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars()
                    | WindowInsetsCompat.Type.displayCutout() | WindowInsetsCompat.Type.ime());
            v.setPadding(v.getPaddingLeft(), baseTop + (top ? bars.top : 0),
                    v.getPaddingRight(), baseBottom + (bottom ? bars.bottom : 0));
            return insets;
        });
        ViewCompat.requestApplyInsets(view);
    }
}
