package com.trivia.masterandroid.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;

import com.trivia.masterandroid.service.QuestionPrefetchService;

/** Registered at runtime by screens that care about connectivity. */
public class NetworkReceiver extends BroadcastReceiver {

    public interface Listener {
        void onConnectionChanged(boolean online);
    }

    private final Listener listener;

    public NetworkReceiver(Listener listener) {
        this.listener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        boolean online = isOnline(context);
        listener.onConnectionChanged(online);
        if (online) {
            try {
                context.startService(new Intent(context, QuestionPrefetchService.class));
            } catch (IllegalStateException ignored) {
                // Started from background: skip.
            }
        }
    }

    public static boolean isOnline(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) {
            return false;
        }
        Network network = cm.getActiveNetwork();
        NetworkCapabilities caps = network == null ? null : cm.getNetworkCapabilities(network);
        return caps != null && caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                && caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
    }
}
