package com.example.sagar.myapplication.ui;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.sagar.myapplication.R;
import com.example.sagar.myapplication.customComponent.SettingDialog;
import com.example.sagar.myapplication.customComponent.TouchImageView;
import com.example.sagar.myapplication.helper.ComplexPreferences;
import com.example.sagar.myapplication.helper.Constants;
import com.example.sagar.myapplication.helper.Functions;
import com.example.sagar.myapplication.helper.HttpRequest;
import com.example.sagar.myapplication.model.CompanyData;
import com.example.sagar.myapplication.model.MainProductClass;
import com.example.sagar.myapplication.model.ModelData;
import com.flyco.dialog.listener.OnOperItemClickL;
import com.flyco.dialog.widget.ActionSheetDialog;
import com.google.gson.GsonBuilder;
import com.rey.material.app.Dialog;
import com.rey.material.widget.Button;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    ProgressDialog pd;
    private String selectCompanyId, selectModelId;
    private Toolbar toolbar;
    private ImageView imgAccount, imgProduct;
    private EditText edtCompany, edtModel;
    private Button btnGo;
    CompanyData companyData;
    ModelData modelData;
    int modelError;
    private ComplexPreferences complexPreferences;
    MainProductClass mainProduct;

    LinearLayout displayLayout, osLayout, camLayout, ramLayout, expLayout, fmLayout, audioLayout, processorLayout, batteryLayout, btLayout,
            flashLayout, colorLayout, specialLayout, scroll;

    TextView txtBrand, txtName, txtPrice, txtDisplay, txtOS, txtCamera, txtRAM, txtExpandable, txtFM, txtAudio, txtProcessor, txtBattery,
            txtColor, txtBluetooth, txtFlash, txtSpecial;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();

        if (Functions.isConnecting(this)) {
            new GetCompany().execute();
        }

        edtCompany.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (companyData.company.size() > 0) {
                    setCompanyDialog();
                } else {
                    Snackbar.make(edtCompany, "No data for company", Snackbar.LENGTH_SHORT).show();
                }

            }
        });

        edtModel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (selectCompanyId == null) {
                    Snackbar.make(edtModel, "First Select company", Snackbar.LENGTH_SHORT).show();
                } else {
                    setModelDialog();
                }

            }
        });

        btnGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Snackbar.make(v, selectModelId, Snackbar.LENGTH_SHORT);
                if (selectCompanyId == null || selectModelId == null) {
                    Snackbar.make(edtModel, "Select company and model both", Snackbar.LENGTH_SHORT).show();

                } else {
                    new GetProductDetails().execute(selectModelId);
                }

            }
        });

        imgProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String imageURL = mainProduct.product.product_image;

                final Dialog dialog = new Dialog(MainActivity.this, android.R.style.Theme_Translucent_NoTitleBar);
                dialog.setContentView(R.layout.image_dialog);
                dialog.getWindow().setLayout(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT);
                TouchImageView bigImage = (TouchImageView) dialog.findViewById(R.id.bigImage);
                Glide.with(MainActivity.this).load(imageURL).thumbnail(0.1f).placeholder(R.drawable.loading).into(bigImage);

                dialog.show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!Functions.isConnecting(this)) {
            SettingDialog dialog = new SettingDialog(this, "You don't seem to have an active internet connection. Please check your internet connectivity and come again.", android.provider.Settings.ACTION_SETTINGS);
            dialog.setOnExitListener(new SettingDialog.OnExitListener() {
                @Override
                public void exit() {
                    Log.e("FINISH", "YES");
                    finish();
                }
            });
            dialog.show();
        }

    }

    private void setModelDialog() {
        if (modelError == 0) {
            ArrayList<String> models = new ArrayList<String>();
            for (int i = 0; i < modelData.model.size(); i++) {
                models.add(modelData.model.get(i).name);
            }

            String[] stringItems = new String[models.size()];
            stringItems = models.toArray(stringItems);

            final ActionSheetDialog dialog = new ActionSheetDialog(MainActivity.this, stringItems, edtModel);
            dialog.isTitleShow(true).show();
            dialog.title("Select Model").titleTextSize_SP(20);

            dialog.setOnOperItemClickL(new OnOperItemClickL() {
                @Override
                public void onOperItemClick(AdapterView<?> parent, View view, int position, long id) {
                    selectModelId = modelData.model.get(position).id;
                    edtModel.setText(modelData.model.get(position).name);
                    dialog.dismiss();
                }
            });

        } else {
            Snackbar.make(edtModel, "No model for selected company", Snackbar.LENGTH_SHORT).show();
        }
    }

    private void setCompanyDialog() {

        ArrayList<String> cats = new ArrayList<String>();
        for (int i = 0; i < companyData.company.size(); i++) {
            cats.add(companyData.company.get(i).cat_name);
        }

        String[] stringItems = new String[cats.size()];
        stringItems = cats.toArray(stringItems);

        final ActionSheetDialog dialog = new ActionSheetDialog(MainActivity.this, stringItems, edtCompany);
        dialog.isTitleShow(true).show();
        dialog.title("Select Company").titleTextSize_SP(20);

        dialog.setOnOperItemClickL(new OnOperItemClickL() {
            @Override
            public void onOperItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectCompanyId = companyData.company.get(position).cat_id;
                edtCompany.setText(companyData.company.get(position).cat_name);
                dialog.dismiss();
                edtModel.setText("");
                selectModelId = null;
                new CountDownTimer(900, 100) {
                    @Override
                    public void onTick(long l) {

                    }

                    @Override
                    public void onFinish() {
                        new GetModel().execute(selectCompanyId);
                    }
                }.start();
            }
        });
    }

    private void init() {

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        imgProduct = (ImageView) findViewById(R.id.imgProduct);
        imgAccount = (ImageView) findViewById(R.id.imgAccount);

        imgAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Functions.fireIntent(MainActivity.this, LoginActivity.class);
            }
        });

        edtCompany = (EditText) findViewById(R.id.edtCompany);
        edtModel = (EditText) findViewById(R.id.edtModel);
        btnGo = (com.rey.material.widget.Button) findViewById(R.id.btnGo);

        scroll = (LinearLayout) findViewById(R.id.scroll);
        displayLayout = (LinearLayout) findViewById(R.id.displayLayout);
        osLayout = (LinearLayout) findViewById(R.id.osLayout);
        camLayout = (LinearLayout) findViewById(R.id.pcamLayout);
        ramLayout = (LinearLayout) findViewById(R.id.ramLayout);
        expLayout = (LinearLayout) findViewById(R.id.expLayout);
        fmLayout = (LinearLayout) findViewById(R.id.fmLayout);
        audioLayout = (LinearLayout) findViewById(R.id.audioLayout);
        processorLayout = (LinearLayout) findViewById(R.id.processorLayout);
        batteryLayout = (LinearLayout) findViewById(R.id.batteryLayout);
        btLayout = (LinearLayout) findViewById(R.id.btLayout);
        flashLayout = (LinearLayout) findViewById(R.id.flashLayout);
        colorLayout = (LinearLayout) findViewById(R.id.colorLayout);
        specialLayout = (LinearLayout) findViewById(R.id.specialLayout);

        txtBrand = (TextView) findViewById(R.id.txtBrand);
        txtName = (TextView) findViewById(R.id.txtName);
        txtPrice = (TextView) findViewById(R.id.txtPrice);
        txtDisplay = (TextView) findViewById(R.id.txtDisplay);
        txtOS = (TextView) findViewById(R.id.txtOS);
        txtCamera = (TextView) findViewById(R.id.txtCamera);
        txtRAM = (TextView) findViewById(R.id.txtRAM);
        txtExpandable = (TextView) findViewById(R.id.txtExpandable);
        txtFM = (TextView) findViewById(R.id.txtFM);
        txtAudio = (TextView) findViewById(R.id.txtAudio);
        txtProcessor = (TextView) findViewById(R.id.txtProcessor);
        txtBattery = (TextView) findViewById(R.id.txtBattery);
        txtColor = (TextView) findViewById(R.id.txtColor);
        txtBluetooth = (TextView) findViewById(R.id.txtBluetooth);
        txtFlash = (TextView) findViewById(R.id.txtFlash);
        txtSpecial = (TextView) findViewById(R.id.txtSpecial);
    }

    private class GetCompany extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd = ProgressDialog.show(MainActivity.this, "Loading", "Please wait", false);
        }

        @Override
        protected Void doInBackground(Void... params) {
            HashMap<String, String> map = new HashMap<>();
            map.put("form_type", "subcat_list");
            try {
                HttpRequest req = new HttpRequest(Constants.BASE_URL);
                JSONObject obj = req.preparePost().withData(map).sendAndReadJSON();
//                Log.e("company_response", obj.toString());
                companyData = new GsonBuilder().create().fromJson(obj.toString(), CompanyData.class);
                if (companyData.company.size() > 0) {
                    complexPreferences = ComplexPreferences.getComplexPreferences(MainActivity.this, "user_pref", 0);
                    complexPreferences.putObject("mobile_companies", companyData);
                    complexPreferences.commit();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            pd.dismiss();
        }
    }

    private class GetModel extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd = ProgressDialog.show(MainActivity.this, "Loading", "Please wait", false);
        }

        @Override
        protected String doInBackground(String... params) {
            HashMap<String, String> map = new HashMap<>();
            map.put("form_type", "product_list");
            map.put("cat_id", params[0]);
            try {
                HttpRequest req = new HttpRequest(Constants.BASE_URL);
                JSONObject obj = req.preparePost().withData(map).sendAndReadJSON();
                // Log.e("model_response", obj.toString());
                modelError = obj.getInt("error");
                if (modelError == 0) {
                    modelData = new GsonBuilder().create().fromJson(obj.toString(), ModelData.class);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String string) {
            super.onPostExecute(string);
            pd.dismiss();
        }
    }

    private class GetProductDetails extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd = ProgressDialog.show(MainActivity.this, "Loading", "Please wait", false);
        }

        @Override
        protected String doInBackground(String... params) {
            HashMap<String, String> map = new HashMap<>();
            map.put("form_type", "product_details");
            map.put("product_id", params[0]);
            Log.e("product_details req", map.toString());
            try {
                HttpRequest req = new HttpRequest(Constants.BASE_URL);
                JSONObject obj = req.preparePost().withData(map).sendAndReadJSON();
                Log.e("product_details res", obj.toString());

                mainProduct = new GsonBuilder().create().fromJson(obj.toString(), MainProductClass.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            pd.dismiss();

            setDetails();
        }
    }

    private void setDetails() {

        txtBrand.setText(Functions.getText(edtCompany));
        txtName.setText(mainProduct.product.name);
        Glide.with(this).load(mainProduct.product.product_image).thumbnail(0.1f).placeholder(R.drawable.loading).into(imgProduct);

        if (mainProduct.product.featuredData.battery.equals("")) {
            batteryLayout.setVisibility(View.GONE);
        } else {
            batteryLayout.setVisibility(View.VISIBLE);
            txtBattery.setText(mainProduct.product.featuredData.battery);
        }

        if (mainProduct.product.featuredData.processor.equals("")) {
            processorLayout.setVisibility(View.GONE);
        } else {
            processorLayout.setVisibility(View.VISIBLE);
            txtProcessor.setText(mainProduct.product.featuredData.processor);
        }

        if (mainProduct.product.featuredData.display.equals("")) {
            displayLayout.setVisibility(View.GONE);
        } else {
            displayLayout.setVisibility(View.VISIBLE);
            txtDisplay.setText(mainProduct.product.featuredData.display);
        }

        if (mainProduct.product.featuredData.ram_rom.equals("")) {
            ramLayout.setVisibility(View.GONE);
        } else {
            ramLayout.setVisibility(View.VISIBLE);
            txtRAM.setText(mainProduct.product.featuredData.ram_rom);
        }

        if (mainProduct.product.featuredData.expandable_slot.equals("")) {
            expLayout.setVisibility(View.GONE);
        } else {
            expLayout.setVisibility(View.VISIBLE);
            txtExpandable.setText(mainProduct.product.featuredData.expandable_slot);
        }

        txtPrice.setText(getResources().getString(R.string.Rs) + " " + mainProduct.product.featuredData.mrp);

        if (mainProduct.product.featuredData.os.equals("")) {
            osLayout.setVisibility(View.GONE);
        } else {
            osLayout.setVisibility(View.VISIBLE);
            txtOS.setText(mainProduct.product.featuredData.os);
        }

        if (mainProduct.product.featuredData.camera.equals("")) {
            camLayout.setVisibility(View.GONE);
        } else {
            camLayout.setVisibility(View.VISIBLE);
            txtCamera.setText(mainProduct.product.featuredData.camera);
        }

        if (mainProduct.product.featuredData.flash.equals("")) {
            flashLayout.setVisibility(View.GONE);
        } else {
            flashLayout.setVisibility(View.VISIBLE);
            txtFlash.setText(mainProduct.product.featuredData.flash);
        }

        if (mainProduct.product.featuredData.color.equals("")) {
            colorLayout.setVisibility(View.GONE);
        } else {
            colorLayout.setVisibility(View.VISIBLE);
            txtColor.setText(mainProduct.product.featuredData.color);
        }

        if (mainProduct.product.featuredData.bt.equals("")) {
            btLayout.setVisibility(View.GONE);
        } else {
            btLayout.setVisibility(View.VISIBLE);
            txtBluetooth.setText(mainProduct.product.featuredData.bt);
        }

        if (mainProduct.product.featuredData.audio_video.equals("")) {
            audioLayout.setVisibility(View.GONE);
        } else {
            audioLayout.setVisibility(View.VISIBLE);
            txtAudio.setText(mainProduct.product.featuredData.audio_video);
        }

        if (mainProduct.product.featuredData.fm.equals("")) {
            fmLayout.setVisibility(View.GONE);
        } else {
            fmLayout.setVisibility(View.VISIBLE);
            txtFM.setText(mainProduct.product.featuredData.fm);
        }

        if (mainProduct.product.featuredData.special_feature.equals("")) {
            specialLayout.setVisibility(View.GONE);
        } else {
            specialLayout.setVisibility(View.VISIBLE);
            txtSpecial.setText(mainProduct.product.featuredData.special_feature);
        }

        scroll.setVisibility(View.VISIBLE);

    }
}
