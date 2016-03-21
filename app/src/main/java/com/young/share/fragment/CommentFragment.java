package com.young.share.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Message;
import android.widget.Toast;

import com.bigkoo.svprogresshud.SVProgressHUD;
import com.young.share.R;
import com.young.share.base.BaseFragment;
import com.young.share.config.Contants;
import com.young.share.interfaces.AsyncListener;
import com.young.share.model.CommentList;
import com.young.share.model.Comment_HZ;
import com.young.share.network.BmobApi;
import com.young.share.utils.LogUtils;
import com.young.share.views.CommentListView.CommentAdapter;
import com.young.share.views.CommentListView.CommentListView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 评论区显示的内容
 * Created by Nearby Yang on 2016-03-19.
 */

public class CommentFragment extends BaseFragment {

    private String shareMessageId;
    private CommentListView commentListView;
    private CommentAdapter commentAdapter;
    private List<Comment_HZ> dataList = new ArrayList<>();//数据

    private static final int GET_MESSAGE = 0x01;//格式化数据

    public CommentFragment() {
    }

    @SuppressLint("ValidFragment")
    public CommentFragment(Context context, String shareMessageId) {
        super(context);
        this.shareMessageId = shareMessageId;
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_comment;
    }

    @Override
    public void initData() {
        // TODO: 2016-03-20 建立缓存

        //提示
        SVProgressHUD.showWithStatus(context, getString(R.string.tips_loading));
/*获取评论内容*/
        getComment(shareMessageId);

    }

    @Override
    public void initView() {
        commentListView = $(R.id.clv_frament_com_list);
        commentAdapter = new CommentAdapter(context);

    }

    @Override
    public void bindData() {

        commentAdapter.bindListView(commentListView);
    }

    @Override
    public void handler(Message msg) {

        switch (msg.what) {
            case GET_MESSAGE:
                /*评论数据，显示到linearLayout*/

                // TODO: 2016-03-20 展示数据 ，评论、回复，向上滚动

                commentAdapter.setDatas(dataList);
                commentAdapter.notifyDataSetChanged();

                break;

        }
    }


    /**
     * 获取评论数据
     *
     * @param messageId
     */
    private void getComment(String messageId) {

        JSONObject params = new JSONObject();

        try {
            params.put(Contants.PARAMS_MESSAGE_ID, messageId);
        } catch (JSONException e) {
            LogUtils.d("get comment add params failure" + e.toString());
        }

        BmobApi.AsyncFunction(context, params, BmobApi.GET_MESSAGE_COMMENTS, CommentList.class, new AsyncListener() {
            @Override
            public void onSuccess(Object object) {
                CommentList commentList = (CommentList) object;

                if (commentList.getCommentList() != null && commentList.getCommentList().size() > 0) {

                    if (dataList != null && dataList.size() > 0) {
                        dataList.clear();
                    }else {
                        dataList = new ArrayList<Comment_HZ>();
                    }

                    dataList.addAll(commentList.getCommentList());
                }

//刷新评论列表
                mhandler.sendEmptyMessage(GET_MESSAGE);
                //关闭显示的框
                processDialogDismisson();

            }

            @Override
            public void onFailure(int code, String msg) {
                processDialogDismisson();

                Toast.makeText(context, R.string.tips_loading_faile, Toast.LENGTH_SHORT).show();
                LogUtils.d("get comment add params failure.  code = " + code + " message =  " + msg);
            }
        });
    }

    /**
     * 关闭进度条提示
     */
    private void processDialogDismisson() {
//        LogUtils.d(" isshow" + SVProgressHUD.isShowing(mActivity));
        if (SVProgressHUD.isShowing(context)) {

            //提示
            SVProgressHUD.dismiss(context);
        }
    }
}
