package com.example.sqlitedb;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.camera2.params.LensShadingMap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private String TAG = MainActivity.class.getName();
    private RentalDBHelper helper = null;
    private SQLiteDatabase db = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize the database, potentially creating it
        helper = new RentalDBHelper(this);
        db = helper.getReadableDatabase();

        // Only populate the db if it is empty
        Cursor c = db.rawQuery("SELECT count(*) FROM issues", null);
        c.moveToFirst();
        if (c.getInt(0) == 0) {
            Thread t = new Thread() {
                @Override
                public void run() {
                    // We have to init the data in a separate thread because of networking
                    helper.initData();

                    // We are now ready to initialize the view on the UI thread
                    runOnUiThread(() -> {
                        initView();
                    });
                }
            };
            t.start();
        } else {
            // We are already inside the UI thread
            initView();
        }
        c.close();
    }

    private void initView() {
        Spinner tv = findViewById(R.id.spinnerTextView);
        ArrayList<String> areas = new ArrayList<>();
        Cursor cursor = db.rawQuery("SELECT DISTINCT(area) FROM issues", null);
        while (cursor.moveToNext()) {
            String area = cursor.getString(0);
            areas.add(area);
        }
        cursor.close();

        ArrayAdapter<String> areaAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, areas);

        tv.setAdapter(areaAdapter);

        ListView listView = (ListView)findViewById(R.id.listView);
        ArrayList<String>Operators = new ArrayList<>();
        ArrayAdapter<String>arrayAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,Operators);

        tv.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedArea = ((TextView) view).getText().toString();
                Cursor cursor1 = db.rawQuery("SELECT operator FROM issues WHERE area = ?", new String[]{selectedArea});
                Operators.clear();
                while (cursor1.moveToNext()) {
                    String operator = cursor1.getString(0);
                    Operators.add(operator);
                    Log.d(TAG, operator);
                }listView.setAdapter(arrayAdapter);

               // TextView textView = findViewById(R.id.textView);
               // textView.setText(operators.stream().reduce("", (s, s2) -> s + "\n" + s2));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                String selectedFromList = (String) listView.getItemAtPosition(position);
                Cursor cursor1 = db.rawQuery("SELECT businessUrl FROM issues WHERE operator = ?", new String[]{selectedFromList});
                while (cursor1.moveToNext()) {
                    String Link = cursor1.getString(0);
                    String Url = Link.substring(4);
                    Log.d("The Url is", Url);

                    Intent i = new Intent(MainActivity.this, webView.class);
                    i.putExtra("Link", Url);
                    startActivity(i);
                }
                cursor.close();
            }
        });
    }
}
