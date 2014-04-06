package net.ugona.plus.watch;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;

import java.util.Date;

public class CarDrawable {

    static final String PKG_NAME = "net.ugona.plus";
    static final String[] drawables = {
            "car_black",           // 1
            "car_white",           // 2
            "car_blue",            // 3
            "car_red",             // 4
            "doors_white",         // 5
            "doors_blue",          // 6
            "doors_red",           // 7
            "doors_white_open",    // 8
            "doors_blue_open",     // 9
            "doors_red_open",      // 10
            "hood_white",          // 11
            "hood_blue",           // 12
            "hood_red",            // 13
            "hood_white_open",     // 14
            "hood_blue_open",      // 15
            "hood_red_open",       // 16
            "trunk_white",         // 17
            "trunk_blue",          // 18
            "trunk_red",           // 19
            "trunk_white_open",    // 20
            "trunk_blue_open",     // 21
            "trunk_red_open",      // 22
            "engine1",             // 23
            "engine1_blue",        // 24
            "ignition",            // 25
            "ignition_red",        // 26
            "lock_white",          // 27
            "lock_white_widget",   // 28
            "lock_blue",           // 29
            "lock_blue_widget",    // 30
            "valet",               // 31
            "block",               // 32
            "heater",              // 33
            "heater_blue",         // 34
    };
    static Bitmap bitmap;
    Resources resources;
    int[] parts_id;

    CarDrawable() {
        parts_id = new int[7];

        parts_id[0] = 0;
        parts_id[1] = 0;
        parts_id[2] = 0;
        parts_id[3] = 0;
        parts_id[4] = 0;
        parts_id[5] = 0;
        parts_id[6] = 0;
    }

    static long getLong(Cursor c, String name) {
        if (c == null)
            return 0;
        int idx = c.getColumnIndex(name);
        return c.getLong(idx);
    }

    static boolean getBoolean(Cursor c, String name) {
        int idx = c.getColumnIndex(name);
        return c.getString(idx).equals("true");
    }

    static String getString(Cursor c, String name) {
        int idx = c.getColumnIndex(name);
        return c.getString(idx);
    }

    private boolean update(Cursor c) {
        long last = getLong(c, Names.EVENT_TIME);
        Date now = new Date();
        boolean upd = false;
        if (last < now.getTime() - 24 * 60 * 60 * 1000) {
            upd = setLayer(0, 1);
            upd |= setLayer(1, 0);
            upd |= setLayer(2, 0);
            upd |= setLayer(3, 0);
            upd |= setLayer(4, 0);
            upd |= setLayer(5, 0);
            upd |= setLayer(6, 0);
        } else {

            boolean guard = getBoolean(c, Names.GUARD);
            boolean guard0 = getBoolean(c, Names.GUARD0);
            boolean guard1 = getBoolean(c, Names.GUARD1);

            boolean white = !guard || (guard0 && guard1);

            upd = setModeCar(!white, getBoolean(c, Names.ZONE_ACCESSORY));

            boolean doors_open = getBoolean(c, Names.INPUT1);
            boolean doors_alarm = getBoolean(c, Names.ZONE_DOOR);
            if (white && doors_alarm) {
                doors_alarm = false;
                doors_open = true;
            }
            upd |= setModeOpen(0, !white, doors_open, doors_alarm);

            boolean hood_open = getBoolean(c, Names.INPUT4);
            boolean hood_alarm = getBoolean(c, Names.ZONE_HOOD);
            if (white && hood_alarm) {
                hood_alarm = false;
                hood_open = true;
            }
            upd |= setModeOpen(1, !white, hood_open, hood_alarm);

            boolean trunk_open = getBoolean(c, Names.INPUT2);
            boolean trunk_alarm = getBoolean(c, Names.ZONE_TRUNK);
            if (white && trunk_alarm) {
                trunk_alarm = false;
                trunk_open = true;
            }
            upd |= setModeOpen(2, !white, trunk_open, trunk_alarm);

            boolean az = getBoolean(c, Names.AZ);
            if (az) {
                upd |= setLayer(4, white ? 24 : 23);
            } else {
                upd |= setLayer(4, 0);
            }

            int ignition_id = 0;
            if (!az && (getBoolean(c, Names.INPUT3) || getBoolean(c, Names.ZONE_IGNITION)))
                ignition_id = guard ? 26 : 25;
            upd |= setLayer(5, ignition_id);

            int state = 0;
            if (guard) {
                state = white ? 30 : 28;
            }
            if (guard0 && !guard1)
                state = 31;
            if (!guard0 && guard1)
                state = 32;
            upd |= setLayer(6, state);
        }
        return upd;
    }

    Bitmap getBitmap(Context context, Cursor c, int width, int height) {
        try {
            PackageManager manager = context.getPackageManager();
            resources = manager.getResourcesForApplication(PKG_NAME);
        } catch (Exception ex) {
            // ignore
        }

        if ((resources == null) || (!update(c) && (bitmap != null)))
            return bitmap;

        if (bitmap == null) {
            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        } else {
            bitmap.eraseColor(Color.TRANSPARENT);
        }
        Canvas canvas = new Canvas(bitmap);
        for (int part : parts_id) {
            if (part == 0)
                continue;
            int resID = resources.getIdentifier(drawables[part - 1], "drawable", PKG_NAME);
            Drawable d = resources.getDrawable(resID);
            d.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            d.draw(canvas);
        }
        return bitmap;
    }

    boolean setLayer(int n, int id) {
        if (parts_id[n] == id)
            return false;
        parts_id[n] = id;
        return true;
    }

    boolean setModeCar(boolean guard, boolean alarm) {
        int pos = guard ? 1 : 0;
        if (alarm)
            pos = 2;
        return setLayer(0, pos + 2);
    }

    boolean setModeOpen(int group, boolean guard, boolean open, boolean alarm) {
        int pos = guard ? 1 : 0;
        if (alarm)
            pos = 2;
        if (open)
            pos += 3;
        return setLayer(group + 1, group * 6 + pos + 5);
    }

}
