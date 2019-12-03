package com.example.jtuckkjarocki.shoppinghelper.Adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;

import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jtuckkjarocki.shoppinghelper.Activities.ItemAddActivity;
import com.example.jtuckkjarocki.shoppinghelper.barcode.R;

import java.util.ArrayList;


public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {
    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    //String[] SubjectValues;

    ArrayList<String> SubjectValues;
    OnProductListener mOnProductListener;
    Context context;
    View view1;
    ViewHolder viewHolder1;
    TextView textView;

    public ProductAdapter(Context context1, ArrayList<String> SubjectValues1, OnProductListener onProductListener) {

        SubjectValues = SubjectValues1;
        context = context1;
        mOnProductListener = onProductListener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextView textView;
        OnProductListener onProductListener;

        public ViewHolder(@NonNull View v, OnProductListener onProductListener) {

            super(v);
            textView = (TextView) v.findViewById(R.id.textViewName);
            this.onProductListener = onProductListener;

            // Attach OnClickListener to View
            v.setOnClickListener(this);

        }
        // Viewholder Onclick method
        @Override
        public void onClick(View v) {
            onProductListener.onProductClick(getAdapterPosition());
        }
    }

    @Override
    public ProductAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        view1 = LayoutInflater.from(context).inflate(R.layout.recyclerview_layout, parent, false);
        viewHolder1 = new ViewHolder(view1, mOnProductListener);

        return viewHolder1;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String currentProduct = SubjectValues.get(position);
        Log.i("Product Name: ", currentProduct);
        holder.textView.setText(SubjectValues.get(position));

//        holder.textView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // Start a new activity to edit the existing item in the recyclerview when the view is clicked
//                Intent itemEditIntent = new Intent(v.getContext(), ItemAddActivity.class);
//                itemEditIntent.putExtra("productname", SubjectValues.get(position));
//                v.getContext().startActivity(itemEditIntent);
//            }
//        });
    }

    // View Click Listener Interface
    public interface OnProductListener{
        void onProductClick(int position);
    }

    @Override
    public int getItemCount() {

        return SubjectValues.size();
    }

}