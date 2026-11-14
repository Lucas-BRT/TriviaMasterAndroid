package com.trivia.masterandroid.util;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.InsetDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import com.trivia.masterandroid.R;

/** Confirmation dialog that follows the app look (ink outline, hard shadow). */
public final class Dialogs {

    private Dialogs() {
    }

    /** titleRes may be 0 for a message-only dialog. */
    public static void confirm(Activity activity, int titleRes, int messageRes,
                               int positiveRes, Runnable onPositive, int negativeRes) {
        if (activity.isFinishing() || activity.isDestroyed()) {
            return;
        }
        Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        View view = activity.getLayoutInflater().inflate(R.layout.dialog_confirm, null);
        dialog.setContentView(view);

        TextView title = view.findViewById(R.id.dialog_title);
        if (titleRes != 0) {
            title.setText(titleRes);
            title.setVisibility(View.VISIBLE);
        }
        ((TextView) view.findViewById(R.id.dialog_message)).setText(messageRes);

        TextView positive = view.findViewById(R.id.dialog_positive);
        positive.setText(positiveRes);
        positive.setOnClickListener(v -> {
            dialog.dismiss();
            if (onPositive != null) {
                onPositive.run();
            }
        });
        TextView negative = view.findViewById(R.id.dialog_negative);
        negative.setText(negativeRes);
        negative.setOnClickListener(v -> dialog.dismiss());

        Window window = dialog.getWindow();
        if (window != null) {
            int margin = (int) (24 * activity.getResources().getDisplayMetrics().density);
            window.setBackgroundDrawable(new InsetDrawable(new ColorDrawable(Color.TRANSPARENT), margin));
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            window.setDimAmount(0.55f);
        }
        dialog.show();
    }
}
