package com.example.sagar.myapplication.marketing.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.sagar.myapplication.R;
import com.example.sagar.myapplication.customComponent.FilterDialog;
import com.example.sagar.myapplication.helper.ComplexPreferences;
import com.example.sagar.myapplication.helper.Constants;
import com.example.sagar.myapplication.helper.Functions;
import com.example.sagar.myapplication.helper.HttpRequest;
import com.example.sagar.myapplication.model.OrderMarketingData;
import com.example.sagar.myapplication.model.OrderModel;
import com.example.sagar.myapplication.model.UserProfile;
import com.google.gson.GsonBuilder;
import com.rey.material.widget.CheckBox;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

public class RetailerOrdersActivity extends AppCompatActivity {

    private String selectRetailerId, selectRetailerName;
    private TextView noData;
    String orderError;
    private Toolbar toolbar;
    private ImageView imgCart, imgFilter;
    View parentView;
    private String userId, filter = "0";
    ComplexPreferences complexPreferences;
    ProgressDialog pd;
    OrderMarketingData orderData;
    OrdersAdapter adapter;
    ListView listview;
    String orderStatus, orderId;

    private TextView txtClear;
    TextView txtFilter;
    private RelativeLayout filterLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_retailer_orders);

        selectRetailerId = getIntent().getStringExtra("selectRetailerId");
        selectRetailerName = getIntent().getStringExtra("selectRetailerName");

        init();

        UserProfile userProfile = new UserProfile();
        complexPreferences = ComplexPreferences.getComplexPreferences(this, "user_pref", 0);
        userProfile = complexPreferences.getObject("current-user", UserProfile.class);
        userId = userProfile.user_id;

        new GetOrders().execute();

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                orderId = orderData.orders.get(position).order.order_id;
                orderStatus = orderData.orders.get(position).order.dilivery_pending;
//                Functions.showSnack(parentView, orderId + " -- " + orderStatus);

                Intent i = new Intent(RetailerOrdersActivity.this, OrderDetailsActivity.class);
                i.putExtra("orderId", orderId);
                i.putExtra("orderStatus", orderStatus);
                startActivity(i);

            }
        });

        imgFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final FilterDialog dialog = new FilterDialog(RetailerOrdersActivity.this, filter);
                dialog.setOnFilterChangeListener(new FilterDialog.FilterChangeListener() {
                    @Override
                    public void onFilter(String filterType) {
                        filter = filterType;
                        dialog.dismiss();
                        new GetOrders().execute();
                    }
                });
                dialog.show();
            }
        });

        txtClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filter = "0";
                new GetOrders().execute();
            }
        });

    }

    private void init() {
        filterLayout = (RelativeLayout) findViewById(R.id.filterLayout);
        txtClear = (TextView) findViewById(R.id.txtClear);
        txtFilter = (TextView) findViewById(R.id.txtFilter);

        listview = (ListView) findViewById(R.id.listview);
        parentView = findViewById(android.R.id.content);
        noData = (TextView) findViewById(R.id.noData);
        toolbar = (Toolbar) findViewById(R.id.toolbar2);
        toolbar.setTitle("Orders");
        toolbar.setSubtitle("Retailer: " + selectRetailerName);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        imgCart = (ImageView) findViewById(R.id.imgCart);
        imgFilter = (ImageView) findViewById(R.id.imgFilter);
        imgFilter.setVisibility(View.VISIBLE);

        imgCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Functions.fireIntent(RetailerOrdersActivity.this, CartActivity.class);
            }
        });
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    private void setFilterLayout() {
        if (filter.equals("0")) {
            filterLayout.setVisibility(View.GONE);
        } else {
            filterLayout.setVisibility(View.VISIBLE);
            txtFilter.setText("Orders from: " + getFilterText(filter));
        }
    }

    private String getFilterText(String filter) {
        if (filter.equals("1")) {
            return "1 Week";
        } else if (filter.equals("2")) {
            return "2 Week";
        } else {
            return "3 Week";
        }
    }


    private class GetOrders extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            setFilterLayout();
            pd = ProgressDialog.show(RetailerOrdersActivity.this, "Loading", "Please wait..", false);
        }

        @Override
        protected String doInBackground(String... params) {
            HashMap<String, String> map = new HashMap<>();
            map.put("form_type", "retailor_order");
            map.put("user_id", userId);
            map.put("filter", filter);
            map.put("retailor_id", selectRetailerId);
            try {
                HttpRequest request = new HttpRequest(Constants.BASE_URL);
                JSONObject obj = request.preparePost().withData(map).sendAndReadJSON();
                Log.e("order_response", obj.toString());
                orderError = obj.getString("error");
                if (orderError.equals("0")) {
                    orderData = new GsonBuilder().create().fromJson(obj.toString(), OrderMarketingData.class);
                }
            } catch (Exception e) {
                Functions.showSnack(parentView, e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            pd.dismiss();
            if (orderError.equals("0")) {
                adapter = new OrdersAdapter(RetailerOrdersActivity.this, orderData.orders);
                listview.setAdapter(adapter);

            } else {
                noData.setVisibility(View.VISIBLE);
            }
        }
    }

    private class OrdersAdapter extends BaseAdapter {

        Context context;
        LayoutInflater mInflater;
        List<OrderModel> orders;

        public OrdersAdapter(Context context, List<OrderModel> orders) {
            this.context = context;
            this.orders = orders;
        }

        @Override
        public int getCount() {
            return orders.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            // TODO Auto-generated method stub
            final ViewHolder mHolder;
            if (convertView == null) {
                mInflater = (LayoutInflater) context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = mInflater.inflate(R.layout.ret_order_row,
                        parent, false);
                mHolder = new ViewHolder();
                mHolder.fullLayout = (LinearLayout) convertView.findViewById(R.id.fullLayout);
                mHolder.txtOrderId = (TextView) convertView
                        .findViewById(R.id.txtOrderId);
                mHolder.txtOrderTotal = (TextView) convertView
                        .findViewById(R.id.txtOrderTotal);
                mHolder.checkbox = (CheckBox) convertView.findViewById(R.id.checkbox);
                mHolder.checkbox.setVisibility(View.GONE);
                convertView.setTag(mHolder);
            } else {
                mHolder = (ViewHolder) convertView.getTag();
            }

            mHolder.txtOrderId.setText(orders.get(position).order.order_id);
            mHolder.txtOrderTotal.setText(orders.get(position).order.order_total);


            if (orders.get(position).order.dilivery_pending.equals("1")) {  // Pending Order
                mHolder.fullLayout.setBackgroundResource(R.color.quad_blue);

            } else {// Completed Order
                mHolder.fullLayout.setBackgroundResource(R.color.quad_green);
            }

            return convertView;
        }

        private class ViewHolder {
            TextView txtOrderTotal, txtOrderId;
            LinearLayout fullLayout;
            CheckBox checkbox;
        }
    }
}
