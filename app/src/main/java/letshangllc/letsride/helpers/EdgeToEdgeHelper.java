package letshangllc.letsride.helpers;

import android.graphics.Color;
import android.os.Build;
import android.view.View;
import android.view.Window;

import androidx.activity.ComponentActivity;
import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

/**
 * Applies edge-to-edge display with system-bar padding so existing layouts remain usable on API 35+.
 */
public final class EdgeToEdgeHelper {
    private EdgeToEdgeHelper() {
    }

    public static void apply(ComponentActivity activity) {
        EdgeToEdge.enable(activity);
        final View content = activity.findViewById(android.R.id.content);
        if (content == null) {
            return;
        }
        ViewCompat.setOnApplyWindowInsetsListener(content, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(insets.left, insets.top, insets.right, insets.bottom);
            return WindowInsetsCompat.CONSUMED;
        });

        Window window = activity.getWindow();
        WindowCompat.setDecorFitsSystemWindows(window, false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(Color.TRANSPARENT);
            window.setNavigationBarColor(Color.TRANSPARENT);
        }
        WindowInsetsControllerCompat controller =
                WindowCompat.getInsetsController(window, window.getDecorView());
        if (controller != null) {
            controller.setAppearanceLightStatusBars(false);
        }
    }
}
