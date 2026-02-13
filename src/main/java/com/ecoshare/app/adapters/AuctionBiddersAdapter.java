package com.ecoshare.app.adapters;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ecoshare.app.R;
import com.ecoshare.app.models.Auction;

import java.util.Collections;
import java.util.List;

public class AuctionBiddersAdapter extends RecyclerView.Adapter<AuctionBiddersAdapter.BidderViewHolder> {

    private Context context;
    private List<Auction.BidderInfo> bidders;

    public AuctionBiddersAdapter(Context context, List<Auction.BidderInfo> bidders) {
        this.context = context;
        this.bidders = bidders;
        sortBidders();
    }

    @NonNull
    @Override
    public BidderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_bidder, parent, false);
        return new BidderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BidderViewHolder holder, int position) {
        Auction.BidderInfo bidder = bidders.get(position);
        holder.bind(bidder);
    }

    @Override
    public int getItemCount() {
        return bidders.size();
    }

    public void updateBidders(List<Auction.BidderInfo> newBidders) {
        this.bidders.clear();
        this.bidders.addAll(newBidders);
        sortBidders();
        notifyDataSetChanged();
    }

    private void sortBidders() {
        Collections.sort(bidders, (b1, b2) -> Double.compare(b2.getBidAmount(), b1.getBidAmount()));
    }

    class BidderViewHolder extends RecyclerView.ViewHolder {

        TextView bidderNameTextView;
        TextView bidTimeTextView;
        TextView bidAmountTextView;

        public BidderViewHolder(@NonNull View itemView) {
            super(itemView);
            bidderNameTextView = itemView.findViewById(R.id.bidderNameTextView);
            bidTimeTextView = itemView.findViewById(R.id.bidTimeTextView);
            bidAmountTextView = itemView.findViewById(R.id.bidAmountTextView);
        }

        public void bind(Auction.BidderInfo bidder) {
            bidderNameTextView.setText(bidder.getName());
            bidAmountTextView.setText(String.format("$%.2f", bidder.getBidAmount()));

            CharSequence timeAgo = DateUtils.getRelativeTimeSpanString(
                    bidder.getTimestamp(),
                    System.currentTimeMillis(),
                    DateUtils.MINUTE_IN_MILLIS,
                    DateUtils.FORMAT_ABBREV_RELATIVE
            );
            bidTimeTextView.setText(timeAgo);
        }
    }
}
