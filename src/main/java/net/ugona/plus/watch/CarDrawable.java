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

    static Bitmap bitmap;
    Resources resources;
    String[] parts_id;

    CarDrawable() {
        parts_id = new String[9];
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
        boolean doors4 = getBoolean(c, Names.DOORS_4);
        if ((last < now.getTime() - 24 * 60 * 60 * 1000)) {
            upd = setLayer(0, doors4 ? "car_black4" : "car_black");
            upd |= setLayer(1);
            upd |= setLayer(2);
            upd |= setLayer(3);
            upd |= setLayer(4);
            upd |= setLayer(5);
            upd |= setLayer(6);
            upd |= setLayer(7);
            upd |= setLayer(8);
        } else {

            boolean guard = getBoolean(c, Names.GUARD);
            boolean guard0 = getBoolean(c, Names.GUARD0);
            boolean guard1 = getBoolean(c, Names.GUARD1);
            boolean card = false;
            if (guard) {
                long guard_t = getLong(c, Names.GUARD_TIME);
                long card_t = getLong(c, Names.CARD);
                if ((guard_t > 0) && (card_t > 0) && (card_t < guard_t))
                    card = true;
            }

            boolean white = !guard || (guard0 && guard1) || card;

            upd = setModeCar(!white, getBoolean(c, Names.ZONE_ACCESSORY), doors4);

            if (doors4) {
                boolean fl = getBoolean(c, Names.DOOR_FL);
                upd |= setModeOpen(1, "door_fl", !white, fl, fl && guard, false);
                boolean fr = getBoolean(c, Names.DOOR_FR);
                upd |= setModeOpen(6, "door_fr", !white, fr, fr && guard, false);
                boolean bl = getBoolean(c, Names.DOOR_BL);
                upd |= setModeOpen(7, "door_bl", !white, bl, bl && guard, false);
                boolean br = getBoolean(c, Names.DOOR_BR);
                upd |= setModeOpen(8, "door_br", !white, br, br && guard, false);

            } else {
                boolean doors_open = getBoolean(c, Names.INPUT1);
                boolean doors_alarm = getBoolean(c, Names.ZONE_DOOR);
                if (white && doors_alarm) {
                    doors_alarm = false;
                    doors_open = true;
                }
                upd |= setModeOpen(1, "doors", !white, doors_open, doors_alarm, false);
                upd |= setLayer(6);
                upd |= setLayer(7);
                upd |= setLayer(8);
            }

            boolean hood_open = getBoolean(c, Names.INPUT4);
            boolean hood_alarm = getBoolean(c, Names.ZONE_HOOD);
            if (white && hood_alarm) {
                hood_alarm = false;
                hood_open = true;
            }
            upd |= setModeOpen(2, "hood", !white, hood_open, hood_alarm, doors4);

            boolean trunk_open = getBoolean(c, Names.INPUT2);
            boolean trunk_alarm = getBoolean(c, Names.ZONE_TRUNK);
            if (white && trunk_alarm) {
                trunk_alarm = false;
                trunk_open = true;
            }
            upd |= setModeOpen(3, "trunk", !white, trunk_open, trunk_alarm, doors4);

            boolean az = getBoolean(c, Names.AZ);
            if (az) {
                upd |= setLayer(4, "engine1", !white);
            } else {
                String ignition_id = null;
                if (!az && (getBoolean(c, Names.INPUT3) || getBoolean(c, Names.ZONE_IGNITION)))
                    ignition_id = guard ? "ignition_red" : (white ? "ignition_blue" : "ignition");
                upd |= setLayer(4, ignition_id);
            }

            String state = null;
            if (guard) {
                state = white ? "lock_blue" : "lock_white";
                if (card)
                    state = "lock_red";
            }
            if (guard0 && !guard1)
                state = "valet";
            if (!guard0 && guard1)
                state = "block";
            upd |= setLayer(5, state);
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
        for (String part : parts_id) {
            if (part == null)
                continue;
            ;
            int id = resources.getIdentifier(part, "drawable", PKG_NAME);
            if (id == 0)
                continue;
            Drawable d = resources.getDrawable(id);
            d.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            d.draw(canvas);
        }
        return bitmap;
    }

    boolean setLayer(int n) {
        if (parts_id[n] == null)
            return false;
        parts_id[n] = null;
        return true;
    }

    boolean setLayer(int n, String name) {
        if (name == null) {
            if (parts_id[n] == null)
                return false;
            parts_id[n] = null;
            return true;
        }
        if (parts_id[n] == null) {
            parts_id[n] = name;
            return true;
        }
        if (parts_id[n].equals(name))
            return false;
        parts_id[n] = name;
        return true;
    }

    boolean setLayer(int n, String name, boolean white) {
        if (!white)
            name += "_blue";
        return setLayer(n, name);
    }

    boolean setModeCar(boolean guard, boolean alarm, boolean doors4) {
        String pos = guard ? "car_blue" : "car_white";
        if (alarm)
            pos = "car_red";
        if (doors4)
            pos += "4";
        return setLayer(0, pos);
    }

    boolean setModeOpen(int pos, String group, boolean guard, boolean open, boolean alarm, boolean doors4) {
        if (alarm) {
            group += "_red";
        } else if (guard) {
            group += "_blue";
        } else {
            group += "_white";
        }
        if (open || alarm)
            group += "_open";
        if (doors4)
            group += "4";
        return setLayer(pos, group);
    }

}
