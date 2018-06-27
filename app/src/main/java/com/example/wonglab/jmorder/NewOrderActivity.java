package com.example.wonglab.jmorder;

import android.app.Activity;
import android.app.ListActivity;
import android.content.ClipData;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.Editable;
import android.text.Selection;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;


public class NewOrderActivity extends AppCompatActivity implements OrderRecyclerItemTouchHelper.RecyclerItemTouchHelperListener {

    private RecyclerView orderListRecycler;
    private RecyclerView.Adapter orderListAdapter;
    private RecyclerView.LayoutManager layoutManager;

    CustomerAutoCompleteView myAutoComplete;
    ProductAutoCompleteView myAutoComplete1;

    // adapter for auto-complete
    ArrayAdapter<String> myAdapter;
    ArrayAdapter<String> myAdapter1;

    // for database operations
    CustomerDatabaseHandler databaseH;
    ProductDatabaseHandler databaseH1;

    // just to add some initial value
    String[] item = new String[] {"Please search..."};
    String[] item1 = new String[] {"Please search..."};

    EditText qty;
    Button add, save;

    List<String> itemInput = new ArrayList<>();
    List<String> qtyInput = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_new_order);
        getSupportActionBar().hide();

        try {
            copyDataBase1();
            copyDataBase2();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try{

            // instantiate database handler
            databaseH = new CustomerDatabaseHandler(NewOrderActivity.this);
            databaseH1 = new ProductDatabaseHandler(NewOrderActivity.this);

            // put sample data to database
            //insertSampleData();

            // autocompletetextview is in activity_main.xml
            myAutoComplete = (CustomerAutoCompleteView) findViewById(R.id.myautocomplete);
            myAutoComplete1 = (ProductAutoCompleteView) findViewById(R.id.myautocomplete1);

            // add the listener so it will tries to suggest while the user types
            myAutoComplete.addTextChangedListener(new CustomerAutoCompleteTextChangedListener(this));
            myAutoComplete1.addTextChangedListener(new ProductAutoCompleteTextChangedListener(this));

            // set our adapter
            myAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, item);
            myAutoComplete.setAdapter(myAdapter);

            myAdapter1 = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, item1);
            myAutoComplete1.setAdapter(myAdapter1);

        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        orderListRecycler = (RecyclerView) findViewById(R.id.orderListRecycler);
        add = (Button) findViewById(R.id.add);
        save = (Button) findViewById(R.id.save);
        qty = (EditText) findViewById(R.id.qty);
        orderListRecycler.setHasFixedSize(true);

        // use a linear layout manager
        layoutManager = new LinearLayoutManager(this);
        orderListRecycler.setLayoutManager(layoutManager);
        orderListAdapter = new OrderListRecyclerAdapter(itemInput, qtyInput);
        orderListRecycler.setAdapter(orderListAdapter);

        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new OrderRecyclerItemTouchHelper(0, ItemTouchHelper.LEFT, this);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(orderListRecycler);

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String custName = myAutoComplete.getText().toString();
                String itemName = myAutoComplete1.getText().toString();
                String quantity = qty.getText().toString();
                if(custName.length() == 0){
                    Toast.makeText(NewOrderActivity.this, "Enter Customer Name!", Toast.LENGTH_SHORT).show(); }
                else if(itemName.length() == 0){
                    Toast.makeText(NewOrderActivity.this, "Select Item!", Toast.LENGTH_SHORT).show(); }
                else if(quantity.length() == 0){
                    Toast.makeText(NewOrderActivity.this, "Enter Quantity!", Toast.LENGTH_SHORT).show(); }
                else{
                    itemInput.add(itemName);
                    qtyInput.add(quantity);
                    orderListAdapter.notifyDataSetChanged();
                    myAutoComplete1.setText("");
                    qty.setText("");
                    myAutoComplete1.requestFocus();
                }
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });

        AutoCompleteTextView custFrom = (AutoCompleteTextView) findViewById(R.id.myautocomplete);
        custFrom.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> p, View v, int pos, long id) {
                //TODO: set focus on next view
                AutoCompleteTextView itemTo = (AutoCompleteTextView) findViewById(R.id.myautocomplete1);
                itemTo.setFocusableInTouchMode(true);
                itemTo.requestFocus();
            }
        });

        AutoCompleteTextView itemFrom = (AutoCompleteTextView) findViewById(R.id.myautocomplete1);
        itemFrom.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> p, View v, int pos, long id) {
                //TODO: set focus on next view
                EditText qtyTo = (EditText) findViewById(R.id.qty);
                qtyTo.setFocusableInTouchMode(true);
                qtyTo.requestFocus();
            }
        });

    }

    // this function is used in CustomAutoCompleteTextChangedListener.java
    public String[] getItemsFromDb(String searchTerm){

        // add items on the array dynamically
        List<CustomerObject> products = databaseH.read(searchTerm);
        int rowCount = products.size();

        String[] item = new String[rowCount];
        int x = 0;

        for (CustomerObject record : products) {

            item[x] = record.objectName;
            x++;
        }

        return item;
    }

    public String[] getItemsFromDb1(String searchTerm){

        // add items on the array dynamically
        List<ProductObject> products = databaseH1.read(searchTerm);
        int rowCount = products.size();

        String[] item1 = new String[rowCount];
        int x = 0;

        for (ProductObject record : products) {

            item1[x] = record.objectName;
            x++;
        }

        return item1;
    }

    public void copyDataBase1() throws IOException {
        String package_name = getApplicationContext().getPackageName();
        String DB_PATH = "/data/data/"+package_name+"/databases/";
        String DB_NAME = "CustomerDatabase";
        try {
            InputStream myInput = getApplicationContext().getAssets().open(DB_NAME);

            File dbFile=new File(DB_PATH);
            dbFile.mkdirs();

            String outputFileName = DB_PATH + DB_NAME;
            OutputStream myOutput = new FileOutputStream(outputFileName);

            byte[] buffer = new byte[1024];
            int length;

            while((length = myInput.read(buffer))>0){
                myOutput.write(buffer, 0, length);
            }

            myOutput.flush();
            myOutput.close();
            myInput.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void copyDataBase2() throws IOException {
        String package_name = getApplicationContext().getPackageName();
        String DB_PATH = "/data/data/"+package_name+"/databases/";
        String DB_NAME = "ProductDatabase";
        try {
            InputStream myInput = getApplicationContext().getAssets().open(DB_NAME);

            File dbFile = new File(DB_PATH);
            dbFile.mkdirs();

            String outputFileName = DB_PATH + DB_NAME;
            OutputStream myOutput = new FileOutputStream(outputFileName);

            byte[] buffer = new byte[1024];
            int length;

            while((length = myInput.read(buffer))>0){
                myOutput.write(buffer, 0, length);
            }

            myOutput.flush();
            myOutput.close();
            myInput.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
        if (viewHolder instanceof OrderListRecyclerAdapter.ViewHolder) {
            // get the removed item name to display it in snack bar
            String name = itemInput.get(viewHolder.getAdapterPosition());

            // backup of removed item for undo purpose
            final String deletedItem = itemInput.get(viewHolder.getAdapterPosition());
            final int deletedIndex = viewHolder.getAdapterPosition();

            // remove the item from recycler view
            itemInput.remove(itemInput.get(viewHolder.getAdapterPosition()));
            qtyInput.remove(qtyInput.get(viewHolder.getAdapterPosition()));
            orderListAdapter.notifyDataSetChanged();
        }
    }
}




