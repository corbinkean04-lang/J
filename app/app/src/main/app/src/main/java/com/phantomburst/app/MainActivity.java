package com.phantomburst.app;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.*;
import java.util.Locale;

public class MainActivity extends Activity {
    private EditText ip, pairingPort, pairingCode, connectPort;
    private TextView status;
    private boolean activated = false;

    // The command is intentionally kept as a predefined payload.
    private static final String PHANTOM_COMMAND =
        "echo \"Phantom Speed Burst...\"\\n" +
        "setprop debug.oculus.headlock 1\\n" +
        "setprop debug.force-opengl 1\\n" +
        "setprop debug.hwc.force_gpu_vsync 1\\n" +
        "setprop debug.performance.profile 1\\n" +
        "settings put global window_animation_scale 0.0\\n" +
        "settings put global transition_animation_scale 0.0\\n" +
        "settings put global animator_duration_scale 0.0\\n" +
        "i=1\\n" +
        "while [ $i -lt 18 ]; do\\n" +
        "  setprop debug.oculus.headlock.translation.z \"$(awk -v i=$i 'BEGIN {print i * -2.95}')\"\\n" +
        "  i=$((i + 1))\\n" +
        "done\\n" +
        "setprop debug.oculus.headlock.translation.z 0\\n" +
        "setprop debug.oculus.headlock.translation.y 0\\n" +
        "setprop debug.oculus.headlock.translation.x 0\\n" +
        "setprop debug.oculus.headlock 0\\n" +
        "cmd power set-fixed-performance-mode-enabled true\\n" +
        "settings put system peak_refresh_rate 90.0\\n" +
        "settings put system min_refresh_rate 90.0\\n" +
        "echo \"Phantom Burst complete.\"";

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        buildUi();
    }

    private TextView label(String s) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextColor(Color.WHITE);
        t.setTextSize(15);
        t.setPadding(0, 10, 0, 4);
        return t;
    }

    private EditText field(String hint) {
        EditText e = new EditText(this);
        e.setHint(hint);
        e.setHintTextColor(Color.rgb(160,160,160));
        e.setTextColor(Color.WHITE);
        e.setSingleLine(true);
        e.setPadding(16, 8, 16, 8);
        return e;
    }

    private Button button(String text) {
        Button b = new Button(this);
        b.setText(text);
        b.setTextSize(16);
        return b;
    }

    private void buildUi() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(35, 25, 35, 25);
        root.setBackgroundColor(Color.rgb(5,5,5));

        TextView title = new TextView(this);
        title.setText("PHANTOM BURST");
        title.setTextColor(Color.rgb(255,32,32));
        title.setTextSize(28);
        title.setGravity(Gravity.CENTER);
        root.addView(title, new LinearLayout.LayoutParams(-1, 60));

        root.addView(label("Quest IP address"));
        ip = field("Example: 192.168.1.20");
        root.addView(ip);

        LinearLayout ports = new LinearLayout(this);
        ports.setOrientation(LinearLayout.HORIZONTAL);

        pairingPort = field("Pairing port");
        connectPort = field("ADB connection port");
        ports.addView(pairingPort, new LinearLayout.LayoutParams(0, -2, 1));
        ports.addView(connectPort, new LinearLayout.LayoutParams(0, -2, 1));
        root.addView(ports);

        root.addView(label("Wireless Debugging pairing code"));
        pairingCode = field("6-digit code");
        root.addView(pairingCode);

        Button pair = button("PAIR");
        Button connect = button("CONNECT");
        Button activate = button("ACTIVATE PHANTOM BURST");
        Button disconnect = button("DISCONNECT");

        root.addView(pair);
        root.addView(connect);
        root.addView(activate);
        root.addView(disconnect);

        status = label("Status: Not connected");
        status.setTextSize(16);
        root.addView(status);

        TextView hint = label("When enabled, Left X is reserved for the Phantom Burst trigger.");
        hint.setTextColor(Color.LTGRAY);
        root.addView(hint);

        pair.setOnClickListener(v -> {
            status.setText("Status: Pairing requested — ADB pairing transport still needs to be implemented.");
        });

        connect.setOnClickListener(v -> {
            if (ip.getText().toString().trim().isEmpty() ||
                connectPort.getText().toString().trim().isEmpty()) {
                status.setText("Status: Enter IP and ADB connection port.");
                return;
            }
            status.setText("Status: Connection requested for " +
                ip.getText().toString().trim() + ":" +
                connectPort.getText().toString().trim());
        });

        activate.setOnClickListener(v -> {
            activated = !activated;
            status.setText(activated
                ? "Status: Phantom Burst ENABLED — Left X trigger armed."
                : "Status: Phantom Burst disabled.");
        });

        disconnect.setOnClickListener(v -> {
            activated = false;
            status.setText("Status: Disconnected.");
        });

        setContentView(root);
    }

    @Override public boolean dispatchKeyEvent(KeyEvent event) {
        // Catches Android key events when the Quest maps the X button to a key event.
        // The exact Quest controller mapping must be verified on-device.
        if (event.getAction() == KeyEvent.ACTION_DOWN &&
            activated && isLeftXCandidate(event)) {
            status.setText("Left X detected — Phantom Burst requested.");
            // The actual ADB transport call will be wired here.
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    private boolean isLeftXCandidate(KeyEvent e) {
        return e.getKeyCode() == KeyEvent.KEYCODE_BUTTON_X;
    }

    // Exposed for the future ADB transport layer.
    private String getPhantomCommand() {
        return PHANTOM_COMMAND;
    }
}
