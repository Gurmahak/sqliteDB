package com.example.sqlitedb;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class RentalDBHelper extends SQLiteOpenHelper {

    private final String TAG = RentalDBHelper.class.getName();

    public RentalDBHelper(Context ctx) {
        super(ctx, "rental.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "Creating database");
        String sql = "CREATE TABLE issues ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "operator TEXT,"
                + "businessURL TEXT,"
                + "street_number TEXT,"
                + "street TEXT,"
                + "total_outstanding INTEGER,"
                + "total_units INTEGER,"
                + "geom TEXT,"
                + "area TEXT)";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS issues");
        onCreate(db);
    }

    /**
     * Initialize data from web service
     */
    public void initData() {


        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("https://learn.operatoroverload.com/rental/issues/1000")
                .build();

        SQLiteDatabase db = getWritableDatabase();
        try (Response response = client.newCall(request).execute()) {
            JSONArray data = new JSONArray(response.body().string());

            // Issue INSERT statements against database
            for (int i = 0; i < data.length(); i++) {
                JSONObject obj = data.getJSONObject(i);
                int id = obj.getInt("id");
                String operator = obj.getString("operator");
                String businessUrl = obj.getString("businessURL");
                String street_number = obj.getString("street_number");
                int total_outstanding = obj.getInt("total_outstanding");
                int total_units = obj.getInt("total_units");
                String geom = obj.getString("geom");
                String area = obj.getString("area");

                db.execSQL(" INSERT into issues "
                                + " (id, operator, businessURL, street_number, total_outstanding, total_units, geom, area) "
                                + " VALUES "
                                + " (?, ?, ?, ?, ?, ?, ?, ?) ",
                        new Object[] {id, operator, businessUrl, street_number, total_outstanding, total_units, geom, area});
            }


        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
}