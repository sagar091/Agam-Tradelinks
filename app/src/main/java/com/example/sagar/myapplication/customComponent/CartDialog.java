package com.example.sagar.myapplication.customComponent;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sagar.myapplication.R;
import com.example.sagar.myapplication.helper.DatabaseHandler;
import com.example.sagar.myapplication.helper.Functions;
import com.example.sagar.myapplication.model.Scheme;
import com.flyco.animation.FadeEnter.FadeEnter;
import com.flyco.dialog.widget.base.BaseDialog;
import com.rey.material.widget.Button;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import me.gujun.android.taggroup.TagGroup;

/**
 * Created by sagartahelyani on 18-09-2015.
 */
public class CartDialog extends BaseDialog {

    ArrayList<String> productDetails;
    ArrayList<String> cartProductDetails;
    TextView unitPrice, unitQty, unitTotalPrice;
    SeekBar seekbar;
    Button btnOK, btnCancel;
    private TagGroup mTagGroup;
    DatabaseHandler handler;
    StringBuilder sb;
    View customView;
    View parentView;
    List<Scheme> schemes;
    int qty;

    public void setOnCartAddListener(OnCartAddListener onCartAddListener) {
        this.onCartAddListener = onCartAddListener;
    }

    OnCartAddListener onCartAddListener;

    public CartDialog(Context context, ArrayList<String> productDetails, List<Scheme> schemes) {
        super(context);
        this.productDetails = productDetails;
        this.schemes = schemes;
    }

    @Override
    public View onCreateView() {
        widthScale(0.9f);
        showAnim(new FadeEnter());

        customView = View.inflate(context, R.layout.add_cart_dialog, null);
        init(customView);

        handler = new DatabaseHandler(context);

        customView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        if(mTagGroup.getInputTagText().contains("\n")){
            Log.e("key2", "enter");
            mTagGroup.submitTag();
        }

        mTagGroup.setOnTagClickListener(new TagGroup.OnTagClickListener() {
            @Override
            public void onTagClick(String tag) {
                if(tag.contains("\n")){
                    Log.e("key1", "enter");
                    mTagGroup.submitTag();
                }
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cartProductDetails = new ArrayList<String>();

                if (seekbar.getProgress() == 0) {
                    Functions.showSnack(customView, "Invalid Quantity");
                } else if (mTagGroup.getTags().length == 0) {
                    Functions.showSnack(customView, "Add atleast one color for model");
                } else {

                    sb = new StringBuilder();
                    for (int i = 0; i < mTagGroup.getTags().length; i++) {
                        Log.e("colors", mTagGroup.getTags()[i]);
                        sb.append(mTagGroup.getTags()[i] + ", ");
                    }

                    cartProductDetails.add(productDetails.get(0)); // id
                    cartProductDetails.add(productDetails.get(1)); // name
                    cartProductDetails.add(productDetails.get(2)); // price
                    if (qty == 0) {
                        qty = 1;
                    }
                    cartProductDetails.add(qty + ""); // qty

                    String colors = sb.toString().substring(0, sb.toString().length() - 2);
                    cartProductDetails.add(colors); // colors

                    try {
                        handler.openDataBase();
                        boolean save = handler.addCartProduct(cartProductDetails, schemes);
                        if (save) {
                            if (onCartAddListener != null) {
                                onCartAddListener.onOkClick();
                            }
                            dismiss();
                        } else {
//                            Functions.showSnack(parentView,"Something went wrong");
                            Toast.makeText(context, "Something went wrong", Toast.LENGTH_LONG).show();
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }

                }

            }
        });

        return customView;
    }

    private void init(View customView) {
        parentView = (View) findViewById(android.R.id.content);
        mTagGroup = (TagGroup) customView.findViewById(R.id.tag_group);
        btnOK = (Button) customView.findViewById(R.id.btnOK);
        btnCancel = (Button) customView.findViewById(R.id.btnCancel);
        unitPrice = (TextView) customView.findViewById(R.id.unitPrice);
        unitQty = (TextView) customView.findViewById(R.id.unitQty);
        unitTotalPrice = (TextView) customView.findViewById(R.id.unitTotalPrice);
        seekbar = (SeekBar) customView.findViewById(R.id.seekbar);

        unitPrice.setText(context.getResources().getString(R.string.Rs) + " " + productDetails.get(2));
        unitQty.setText("x 1 Qty");
        unitTotalPrice.setText("= " + context.getResources().getString(R.string.Rs) + " " + productDetails.get(2));
    }

    @Override
    public boolean setUiBeforShow() {
        seekbar.setProgress(1);
        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                qty = progress;
                unitQty.setText("x " + progress + " Qty");
                unitTotalPrice.setText("= " + context.getResources().getString(R.string.Rs) + " " + String.valueOf(Integer.parseInt(productDetails.get(2)) * progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        return false;
    }

    public interface OnCartAddListener {
        void onOkClick();
    }

}
