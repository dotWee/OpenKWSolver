package de.dotwee.openkwsolver.Tools;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import de.dotwee.openkwsolver.R;

/**
 * Created by Lukas on 10.05.2015.
 */
public class ThemeChanger {
    public final static int THEME_DEFAULT = R.style.AppTheme, THEME_DARK = R.style.AppThemeDark;
    private static int setTheme = 1;

    public static void changeTheme(Activity activity) {

        activity.finish();
        activity.startActivity(new Intent(activity, activity.getClass()));

        if (isDarkThemeEnabled(activity)) activity.setTheme(R.style.AppThemeDark);
        else activity.setTheme(R.style.AppTheme);
    }

    public static void onCreateChangeTheme(AppCompatActivity activity) {
        if (isDarkThemeEnabled(activity)) {
            activity.setTheme(THEME_DARK);
            activity.getWindow().getDecorView().findViewById(android.R.id.content).setBackgroundColor(activity.getResources().getColor(R.color.background_material_dark));

            if (Build.VERSION.SDK_INT >= 21) {
                activity.getWindow().setNavigationBarColor(activity.getResources().getColor(R.color.dark_primary));
                activity.getWindow().setStatusBarColor(activity.getResources().getColor(R.color.dark_primary_dark));
            }
        } else {
            activity.setTheme(THEME_DEFAULT);
            activity.getWindow().getDecorView().findViewById(android.R.id.content).setBackgroundColor(activity.getResources().getColor(R.color.background_material_light));

            if (Build.VERSION.SDK_INT >= 21) {
                activity.getWindow().setNavigationBarColor(activity.getResources().getColor(R.color.primary));
                activity.getWindow().setStatusBarColor(activity.getResources().getColor(R.color.primary_dark));
            }
        }
    }

    public static boolean isDarkThemeEnabled(Context context) {
        return StaticHelpers.getPreferencesBoolean(context, "pref_layout_darkui", false);
    }

    public static int getDefaultIcon(Context context) {
        if (isDarkThemeEnabled(context)) return R.mipmap.ic_launcher_dark;
        else return R.mipmap.ic_launcher;
    }
}
