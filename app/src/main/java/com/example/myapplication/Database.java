package com.example.myapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Pair;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Database extends SQLiteOpenHelper {
    private final static String DB_NAME = "StepN";
    private final static int DB_VERSION = 1;
    private static Database instance;
    private static final AtomicInteger openCounter = new AtomicInteger();
    private Database(final Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }
    public static synchronized Database getInstance(final Context c) {
        if (instance == null) {
            instance = new Database(c.getApplicationContext());
        }
        openCounter.incrementAndGet();
        return instance;
    }
    @Override
    public void close() {
        if (openCounter.decrementAndGet() == 0) {
            super.close();
        }
    }
    @Override
    public void onCreate(final SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + DB_NAME + " (date INTEGER, steps INTEGER)");
    }
    @Override
    public void onUpgrade(final SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion == 1) {
            db.execSQL("CREATE TABLE " + DB_NAME + "2 (date INTEGER, steps INTEGER)");
            db.execSQL("INSERT INTO " + DB_NAME + "2 (date, steps) SELECT date, steps FROM " +
                    DB_NAME);
            db.execSQL("DROP TABLE " + DB_NAME);
            db.execSQL("ALTER TABLE " + DB_NAME + "2 RENAME TO " + DB_NAME + "");
        }
    }
    public void insertNewDay(long date, int steps) {
        if (steps >= 0) {
            getWritableDatabase().beginTransaction();
            try {
                Cursor c = getReadableDatabase().query(DB_NAME, new String[]{"date"}, "date = ?",
                        new String[]{String.valueOf(date)}, null, null, null);
                if (c.getCount() == 0 && steps >= 0) {
                    addToLastEntry(steps);
                    ContentValues values = new ContentValues();
                    values.put("date", date);
                    values.put("steps", -steps);
                    getWritableDatabase().insert(DB_NAME, null, values);
                }
                c.close();
                getWritableDatabase().setTransactionSuccessful();
            } finally {
                getWritableDatabase().endTransaction();
            }
        }
    }
    public void addToLastEntry(int steps) {
        getWritableDatabase().execSQL("UPDATE " + DB_NAME + " SET steps = steps + " + steps +
                " WHERE date = (SELECT MAX(date) FROM " + DB_NAME + ")");
    }

    public int getTotalWithoutToday() {
        Cursor c = getReadableDatabase()
                .query(DB_NAME, new String[]{"SUM(steps)"}, "steps > 0 AND date > 0 AND date < ?",
                        new String[]{String.valueOf(Util.getToday())}, null, null, null);
        c.moveToFirst();
        int re = c.getInt(0);
        c.close();
        return re;
    }

    public Pair<Date, Integer> getRecordData() {
        Cursor c = getReadableDatabase()
                .query(DB_NAME, new String[]{"date, steps"}, "date > 0", null, null, null,
                        "steps DESC", "1");
        c.moveToFirst();
        Pair<Date, Integer> p = new Pair<>(new Date(c.getLong(0)), c.getInt(1));
        c.close();
        return p;
    }

    public int getSteps(final long date) {
        Cursor c = getReadableDatabase().query(DB_NAME, new String[]{"steps"}, "date = ?",
                new String[]{String.valueOf(date)}, null, null, null);
        c.moveToFirst();
        int re;
        if (c.getCount() == 0) re = Integer.MIN_VALUE;
        else re = c.getInt(0);
        c.close();
        return re;
    }

    public List<Pair<Long, Integer>> getLastEntries(int num) {
        Cursor c = getReadableDatabase().query(DB_NAME, new String[]{"date", "steps"}, "date > 0", null, null, null, "date DESC", String.valueOf(num));
        int max = c.getCount();
        List<Pair<Long, Integer>> result = new ArrayList<>(max);
        if (c.moveToFirst()) {
            do {
                result.add(new Pair<>(c.getLong(0), c.getInt(1)));
            } while (c.moveToNext());
        }
        c.close();
        return result;
    }

    public int getSteps(final long start, final long end) {
        Cursor c = getReadableDatabase()
                .query(DB_NAME, new String[]{"SUM(steps)"}, "date >= ? AND date <= ?",
                        new String[]{String.valueOf(start), String.valueOf(end)}, null, null, null);
        int re;
        if (c.getCount() == 0) {
            re = 0;
        } else {
            c.moveToFirst();
            re = c.getInt(0);
        }
        c.close();
        return re;
    }
    public void removeNegativeEntries() {
        getWritableDatabase().delete(DB_NAME, "steps < ?", new String[]{"0"});
    }
    public int getDaysWithoutToday() {
        Cursor c = getReadableDatabase()
                .query(DB_NAME, new String[]{"COUNT(*)"}, "steps > ? AND date < ? AND date > 0",
                        new String[]{String.valueOf(0), String.valueOf(Util.getToday())}, null,
                        null, null);
        c.moveToFirst();
        int re = c.getInt(0);
        c.close();
        return Math.max(re, 0);
    }
    public int getDays() {
        return this.getDaysWithoutToday() + 1;
    }
    public void saveCurrentSteps(int steps) {
        ContentValues values = new ContentValues();
        values.put("steps", steps);
        if (getWritableDatabase().update(DB_NAME, values, "date = -1", null) == 0) {
            values.put("date", -1);
            getWritableDatabase().insert(DB_NAME, null, values);
        }
    }
    public int getCurrentSteps() {
        int re = getSteps(-1);
        return re == Integer.MIN_VALUE ? 0 : re;
    }
}