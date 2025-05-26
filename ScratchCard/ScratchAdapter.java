package com.solution.easypay.xyz.ScratchCard;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.solution.easypay.xyz.ApiHits.CouponList;
import com.solution.easypay.xyz.R;
import com.solution.easypay.xyz.Util.Utility;

import java.util.ArrayList;

public class ScratchAdapter extends RecyclerView.Adapter<ScratchAdapter.MyViewHolder> {
    int layout;
    private Activity mContext;
    ArrayList<CouponList> couponList;


    public ScratchAdapter(Activity mContext, ArrayList<CouponList> couponList, int layout) {
        this.layout = layout;
        this.mContext = mContext;
        this.couponList = couponList;

    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        final CouponList myCouponList = couponList.get(position);
        if (myCouponList != null) {
            if (myCouponList.isRedeemed()) {
                holder.layoutDetails.setVisibility(View.VISIBLE);
                holder.imageScratch.setVisibility(View.GONE);
            }else {
                holder.layoutDetails.setVisibility(View.VISIBLE);
                holder.imageScratch.setVisibility(View.VISIBLE);
                holder.itemView.setOnClickListener(v -> {
                    Intent intent = new Intent(mContext, ScratchViewActivity.class);
                    intent.putExtra("redeemAmount",myCouponList.getRedeemAmount());
                    intent.putExtra("Id",myCouponList.getTid());
                    intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    mContext.startActivity(intent);
                });
            }
        }
        holder.txtRedeemAmount.setText(Utility.INSTANCE.formatedAmountWithRupees(String.valueOf(myCouponList.getRedeemAmount())));

    }

    @Override
    public int getItemCount() {
        return couponList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageScratch;
        private TextView txtRedeemAmount;
        private View layoutDetails;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            imageScratch = itemView.findViewById(R.id.imageScratch);
            txtRedeemAmount = itemView.findViewById(R.id.txtRedeemAmount);
            layoutDetails = itemView.findViewById(R.id.layoutDetails);
        }
    }
}
