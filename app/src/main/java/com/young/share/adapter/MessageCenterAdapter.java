package com.young.share.adapter;

import android.content.Context;
import android.widget.TextView;

import com.young.share.adapter.baseAdapter.CommAdapter;
import com.young.share.adapter.baseAdapter.ViewHolder;
import com.young.share.model.RemoteModel;
import com.young.share.R;
import com.young.share.utils.StringUtils;

/**
 * 消息中心列表
 * <p/>
 * Created by Nearby Yang on 2015-12-06.
 */
public class MessageCenterAdapter extends CommAdapter<RemoteModel> {

    public MessageCenterAdapter(Context context) {
        super(context);
    }

    @Override
    public void convert(ViewHolder holder, RemoteModel remoteModel, int position) {
        ((TextView) holder.getView(R.id.tv_record_comm_nickname)).setText(remoteModel.getSender().getNickName());
        TextView content = holder.getView(R.id.tv_record_comm_content);
        int colorId = remoteModel.isRead() ? R.color.black : R.color.theme_puple;
        //未读为紫色
        content.setTextColor(ctx.getResources().getColor(colorId));
        content.setText(StringUtils.getEmotionContent(ctx, content, remoteModel.getContent()));

        ((TextView) holder.getView(R.id.tv_record_comm_created)).setText(remoteModel.getCreatedAt());

    }


    @Override
    public int getlayoutid(int position) {
        return R.layout.item_record_comm;
    }


}
