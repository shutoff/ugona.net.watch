/*
Copyright (c) 2011, Sony Ericsson Mobile Communications AB
Copyright (c) 2011-2013, Sony Mobile Communications AB

 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:

 * Redistributions of source code must retain the above copyright notice, this
 list of conditions and the following disclaimer.

 * Redistributions in binary form must reproduce the above copyright notice,
 this list of conditions and the following disclaimer in the documentation
 and/or other materials provided with the distribution.

 * Neither the name of the Sony Ericsson Mobile Communications AB / Sony Mobile
 Communications AB nor the names of its contributors may be used to endorse or promote
 products derived from this software without specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package net.ugona.plus.watch;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import com.sonyericsson.extras.liveware.aef.control.Control;
import com.sonyericsson.extras.liveware.extension.util.ExtensionUtils;
import com.sonyericsson.extras.liveware.extension.util.control.ControlExtension;
import com.sonyericsson.extras.liveware.extension.util.control.ControlObjectClickEvent;
import com.sonyericsson.extras.liveware.extension.util.control.ControlViewGroup;

/**
 * The sample control for SmartWatch handles the control on the accessory. This
 * class exists in one instance for every supported host application that we
 * have registered to
 */
class ControlSmartWatch2 extends ControlExtension {

    static final String[] fields = new String[]{
            Names.VOLTAGE_MAIN,
            Names.VOLTAGE_RESERVED,
            Names.BALANCE,
            Names.TEMPERATURE,
            Names.TEMP_SIFT,
            Names.EVENT_TIME,
            Names.GUARD,
            Names.GUARD0,
            Names.GUARD1,
            Names.ZONE_ACCESSORY,
            Names.ZONE_DOOR,
            Names.ZONE_HOOD,
            Names.ZONE_TRUNK,
            Names.ZONE_IGNITION,
            Names.INPUT1,
            Names.INPUT2,
            Names.INPUT3,
            Names.INPUT4,
            Names.AZ
    };
    private static final int MENU_ITEM_0 = 0;
    Bundle[] mMenuItemsText = new Bundle[1];
    Bundle[] mMenuItemsIcons = new Bundle[1];
    CarDrawable carDrawable;
    private Handler mHandler;
    private ControlViewGroup mLayout = null;
    private boolean mTextMenu = false;

    /**
     * Create sample control.
     *
     * @param hostAppPackageName Package name of host application.
     * @param context            The context.
     * @param handler            The handler to use
     */
    ControlSmartWatch2(final String hostAppPackageName, final Context context,
                       Handler handler) {
        super(context, hostAppPackageName);
        if (handler == null) {
            throw new IllegalArgumentException("handler == null");
        }
        mHandler = handler;
        setupClickables(context);
        initializeMenus();
        carDrawable = new CarDrawable();
    }

    /**
     * Get supported control width.
     *
     * @param context The context.
     * @return the width.
     */
    public static int getSupportedControlWidth(Context context) {
        return context.getResources().getDimensionPixelSize(R.dimen.smart_watch_2_control_width);
    }

    /**
     * Get supported control height.
     *
     * @param context The context.
     * @return the height.
     */
    public static int getSupportedControlHeight(Context context) {
        return context.getResources().getDimensionPixelSize(R.dimen.smart_watch_2_control_height);
    }

    private void initializeMenus() {
        mMenuItemsText[0] = new Bundle();
        mMenuItemsText[0].putInt(Control.Intents.EXTRA_MENU_ITEM_ID, MENU_ITEM_0);
        mMenuItemsText[0].putString(Control.Intents.EXTRA_MENU_ITEM_TEXT, "Item 1");

        mMenuItemsIcons[0] = new Bundle();
        mMenuItemsIcons[0].putInt(Control.Intents.EXTRA_MENU_ITEM_ID, MENU_ITEM_0);
        mMenuItemsIcons[0].putString(Control.Intents.EXTRA_MENU_ITEM_ICON,
                ExtensionUtils.getUriString(mContext, R.drawable.actions));
    }

    ;

    @Override
    public void onDestroy() {
        Log.d(ExtensionService.LOG_TAG, "ControlSmartWatch onDestroy");
        mHandler = null;
    }

    @Override
    public void onStart() {
        // Nothing to do. Animation is handled in onResume.
    }

    @Override
    public void onStop() {
        // Nothing to do. Animation is handled in onPause.
    }

    @Override
    public void onResume() {
        Log.d(ExtensionService.LOG_TAG, "Starting animation");

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        String car_id = preferences.getString(State.ID, "");

        Bundle[] data = null;
        Bitmap car = null;

        Uri uri = Uri.parse("content://net.ugona.plus/car#" + car_id);
        Cursor c = mContext.getContentResolver().query(uri, fields, null, null, null);
        if (c != null) {
            c.moveToFirst();
            car = carDrawable.getBitmap(mContext, c, 126, 176);

            Bundle b1 = new Bundle();
            b1.putInt(Control.Intents.EXTRA_LAYOUT_REFERENCE, R.id.voltage);
            b1.putString(Control.Intents.EXTRA_TEXT, CarDrawable.getString(c, Names.VOLTAGE_MAIN) + " V");

            Bundle b2 = new Bundle();
            b2.putInt(Control.Intents.EXTRA_LAYOUT_REFERENCE, R.id.temperature);
            String temp = "--";
            try {
                String[] temp_data = CarDrawable.getString(c, Names.TEMPERATURE).split(";");
                int temp_value = Integer.parseInt(temp_data[0].split(":")[0]);
                temp = (temp_value + CarDrawable.getLong(c, Names.TEMP_SIFT)) + "";
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            b2.putString(Control.Intents.EXTRA_TEXT, temp + " \u00B0C");

            Bundle b3 = new Bundle();
            b3.putInt(Control.Intents.EXTRA_LAYOUT_REFERENCE, R.id.balance);
            b3.putString(Control.Intents.EXTRA_TEXT, CarDrawable.getString(c, Names.BALANCE));

            data = new Bundle[3];

            data[0] = b1;
            data[1] = b2;
            data[2] = b3;

            c.close();
        }

        showLayout(R.layout.control_2, data);
        if (car != null)
            sendImage(R.id.car, car);
    }

    @Override
    public void onObjectClick(final ControlObjectClickEvent event) {
        Log.d(ExtensionService.LOG_TAG, "onObjectClick() " + event.getClickType());
        if (event.getLayoutReference() != -1) {
            mLayout.onClick(event.getLayoutReference());
        }
    }

    @Override
    public void onKey(final int action, final int keyCode, final long timeStamp) {
        Log.d(ExtensionService.LOG_TAG, "onKey()");
        if (action == Control.Intents.KEY_ACTION_RELEASE
                && keyCode == Control.KeyCodes.KEYCODE_OPTIONS) {
            toggleMenu();
        } else if (action == Control.Intents.KEY_ACTION_RELEASE
                && keyCode == Control.KeyCodes.KEYCODE_BACK) {
            Log.d(ExtensionService.LOG_TAG, "onKey() - back button intercepted.");
        }
    }

    @Override
    public void onMenuItemSelected(final int menuItem) {
        Log.d(ExtensionService.LOG_TAG, "onMenuItemSelected() - menu item " + menuItem);
        if (menuItem == MENU_ITEM_0) {
            clearDisplay();
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    onResume();
                }
            }, 1000);
        }
    }

    private void toggleMenu() {
        if (mTextMenu) {
            showMenu(mMenuItemsIcons);
        } else {
            showMenu(mMenuItemsText);
        }
        mTextMenu = !mTextMenu;
    }

    private void setupClickables(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService
                (Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.control_2
                , null);
        mLayout = (ControlViewGroup) parseLayout(layout);

    }


}
