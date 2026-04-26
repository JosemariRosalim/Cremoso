package com.example.creamoso;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "Cremoso.db";
    private static final int DATABASE_VERSION = 4; // Incremented for Orders table

    private static final String TABLE_USERS = "users";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_INPUT = "user_input";
    private static final String COLUMN_FIRST_NAME = "first_name";
    private static final String COLUMN_LAST_NAME = "last_name";
    private static final String COLUMN_MOBILE = "mobile";
    private static final String COLUMN_EMAIL = "email";
    private static final String COLUMN_BIRTHDAY = "birthday";

    // Cart Table
    private static final String TABLE_CART = "cart";
    private static final String COLUMN_CART_ID = "cart_id";
    private static final String COLUMN_USER_KEY = "user_key";
    private static final String COLUMN_PRODUCT_NAME = "product_name";
    private static final String COLUMN_PRODUCT_PRICE = "product_price";
    private static final String COLUMN_QUANTITY = "quantity";

    // Orders Table
    private static final String TABLE_ORDERS = "orders";
    private static final String COLUMN_ORDER_ID = "order_id";
    private static final String COLUMN_ORDER_USER = "order_user";
    private static final String COLUMN_ORDER_TOTAL = "order_total";
    private static final String COLUMN_ORDER_STATUS = "order_status";
    private static final String COLUMN_ORDER_DATE = "order_date";
    private static final String COLUMN_ORDER_ITEMS = "order_items_summary";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_USERS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_INPUT + " TEXT UNIQUE,"
                + COLUMN_FIRST_NAME + " TEXT,"
                + COLUMN_LAST_NAME + " TEXT,"
                + COLUMN_MOBILE + " TEXT,"
                + COLUMN_EMAIL + " TEXT,"
                + COLUMN_BIRTHDAY + " TEXT" + ")");

        db.execSQL("CREATE TABLE " + TABLE_CART + "("
                + COLUMN_CART_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_USER_KEY + " TEXT,"
                + COLUMN_PRODUCT_NAME + " TEXT,"
                + COLUMN_PRODUCT_PRICE + " REAL,"
                + COLUMN_QUANTITY + " INTEGER" + ")");

        db.execSQL("CREATE TABLE " + TABLE_ORDERS + "("
                + COLUMN_ORDER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_ORDER_USER + " TEXT,"
                + COLUMN_ORDER_TOTAL + " REAL,"
                + COLUMN_ORDER_STATUS + " TEXT,"
                + COLUMN_ORDER_DATE + " TEXT,"
                + COLUMN_ORDER_ITEMS + " TEXT" + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + COLUMN_FIRST_NAME + " TEXT");
            db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + COLUMN_LAST_NAME + " TEXT");
            db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + COLUMN_MOBILE + " TEXT");
            db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + COLUMN_EMAIL + " TEXT");
            db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + COLUMN_BIRTHDAY + " TEXT");
        }
        if (oldVersion < 3) {
            db.execSQL("CREATE TABLE " + TABLE_CART + "("
                    + COLUMN_CART_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_USER_KEY + " TEXT,"
                    + COLUMN_PRODUCT_NAME + " TEXT,"
                    + COLUMN_PRODUCT_PRICE + " REAL,"
                    + COLUMN_QUANTITY + " INTEGER" + ")");
        }
        if (oldVersion < 4) {
            db.execSQL("CREATE TABLE " + TABLE_ORDERS + "("
                    + COLUMN_ORDER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_ORDER_USER + " TEXT,"
                    + COLUMN_ORDER_TOTAL + " REAL,"
                    + COLUMN_ORDER_STATUS + " TEXT,"
                    + COLUMN_ORDER_DATE + " TEXT,"
                    + COLUMN_ORDER_ITEMS + " TEXT" + ")");
        }
    }

    public boolean addUser(String input) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_INPUT, input);
        if (input.contains("@")) {
            values.put(COLUMN_EMAIL, input);
        } else {
            values.put(COLUMN_MOBILE, input);
        }
        long result = db.insert(TABLE_USERS, null, values);
        return result != -1;
    }

    public boolean checkUser(String input) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[]{COLUMN_ID},
                COLUMN_INPUT + "=?", new String[]{input}, null, null, null);
        int count = cursor.getCount();
        cursor.close();
        return count > 0;
    }

    public Cursor getUserData(String input) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_USERS, null, COLUMN_INPUT + "=?", new String[]{input}, null, null, null);
    }

    public boolean updateProfile(String input, String firstName, String lastName, String birthday) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_FIRST_NAME, firstName);
        values.put(COLUMN_LAST_NAME, lastName);
        values.put(COLUMN_BIRTHDAY, birthday);
        int result = db.update(TABLE_USERS, values, COLUMN_INPUT + "=?", new String[]{input});
        return result > 0;
    }

    public boolean deleteUser(String input) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_USERS, COLUMN_INPUT + "=?", new String[]{input});
        return result > 0;
    }

    // Cart Operations
    public boolean addToCart(String userKey, String productName, double price, int quantity) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_KEY, userKey);
        values.put(COLUMN_PRODUCT_NAME, productName);
        values.put(COLUMN_PRODUCT_PRICE, price);
        values.put(COLUMN_QUANTITY, quantity);
        long result = db.insert(TABLE_CART, null, values);
        return result != -1;
    }

    public Cursor getCartItems(String userKey) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_CART, null, COLUMN_USER_KEY + "=?", new String[]{userKey}, null, null, null);
    }

    public void clearCart(String userKey) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_CART, COLUMN_USER_KEY + "=?", new String[]{userKey});
    }

    // Order Operations
    public boolean placeOrder(String userKey, double total, String itemsSummary) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ORDER_USER, userKey);
        values.put(COLUMN_ORDER_TOTAL, total);
        values.put(COLUMN_ORDER_STATUS, "PENDING");
        
        String date = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());
        values.put(COLUMN_ORDER_DATE, date);
        values.put(COLUMN_ORDER_ITEMS, itemsSummary);

        long result = db.insert(TABLE_ORDERS, null, values);
        return result != -1;
    }

    public Cursor getAllOrders() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_ORDERS, null, null, null, null, null, COLUMN_ORDER_ID + " DESC");
    }

    public boolean updateOrderStatus(int orderId, String status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ORDER_STATUS, status);
        return db.update(TABLE_ORDERS, values, COLUMN_ORDER_ID + "=?", new String[]{String.valueOf(orderId)}) > 0;
    }
}