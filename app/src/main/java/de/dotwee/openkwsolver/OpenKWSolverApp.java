/*
 *             DO WHAT THE FUCK YOU WANT TO PUBLIC LICENSE
 *                     Version 2, December 2004
 *
 *  Copyright (C) 2015 Lukas "dotwee" Wolfsteiner <lukas@wolfsteiner.de>
 *
 *  Everyone is permitted to copy and distribute verbatim or modified
 *  copies of this license document, and changing it is allowed as long
 *  as the name is changed.
 *
 *             DO WHAT THE FUCK YOU WANT TO PUBLIC LICENSE
 *    TERMS AND CONDITIONS FOR COPYING, DISTRIBUTION AND MODIFICATION
 *
 *   0. You just DO WHAT THE FUCK YOU WANT TO.
 *
 */

package de.dotwee.openkwsolver;

import android.app.Application;
import android.content.Context;
import android.view.ContextThemeWrapper;
import de.dotwee.openkwsolver.Tools.StaticHelpers;

/**
 * Created by Lukas on 05.05.2015
 * for project OpenKWSolver.
 */
public class OpenKWSolverApp extends Application {
	private static Context wrapTheme(Context context) {
		if (StaticHelpers.isDarkThemeEnabled(context)) {
			return new ContextThemeWrapper(context, R.style.AppThemeDark);
		} else {
			return new ContextThemeWrapper(context, R.style.AppTheme);
		}
	}

	@Override
	public Context getBaseContext() {
		return wrapTheme(super.getBaseContext());
	}

	@Override
	public Context getApplicationContext() {
		return wrapTheme(super.getApplicationContext());
	}
}
