package com.young.share;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.young.share.adapter.MainPagerAdapter;
import com.young.share.base.BaseAppCompatActivity;
import com.young.share.bmobPush.BmobPush;
import com.young.share.bmobPush.MessageNotification;
import com.young.share.config.Contants;
import com.young.share.fragment.DiscountFragment;
import com.young.share.fragment.DiscoverFragment;
import com.young.share.fragment.RankFragment;
import com.young.share.interfaces.MScrollListener;
import com.young.share.model.MyUser;
import com.young.share.utils.BDLBSUtils;
import com.young.share.utils.DialogUtils;
import com.young.share.utils.LogUtils;
import com.young.share.utils.StringUtils;
import com.young.share.utils.XmlUtils;
import com.young.share.views.ArcMenu;
import com.young.share.views.CustomViewPager;
import com.young.share.views.Dialog4Tips;
import com.young.share.views.actionProvider.MainActyProvider;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.push.PushConstants;
import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.update.BmobUpdateAgent;

public class MainActivity extends BaseAppCompatActivity implements MScrollListener {

    private ArcMenu mArcMenu;
    private BDLBSUtils bdlbsUtils;
    private MainPagerAdapter pagerAdapter;
    private MainActyProvider mainActyProvider;//ActionProvider
    private String currentCity;//当前用户所在城市
    private DiscountFragment discountFragment;
    private DiscoverFragment discoverFragment;

    private int times = 0;
    private static final int callbackTimes = 10;//回调10次
    private boolean isRegistBordcast = false;//是否注册了广播接收者
    private boolean isDiscount = false;//当前是否为商家优惠界面。true -->是

    private static final int MESSAGE_BAIDU_MAP_DELAY = 0x001;//百度地图
    private static final long DELAYED = 6000L;

    @Override
    public int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    public void initData() {
        //toolbar
        initialiToolbar();
        //初始化Bmob的消息推送
        configBmob();

        bdlbsUtils = BDLBSUtils.builder(this, new locationListener());
        bdlbsUtils.startLocation();
    }


    @Override
    public void findviewbyid() {

        List<Fragment> list = new ArrayList<>();
        CustomViewPager viewPager = $(R.id.vp_main);
        mArcMenu = $(R.id.id_menu);

        discountFragment = new DiscountFragment();
        discoverFragment = new DiscoverFragment();
        RankFragment rankFragment = new RankFragment();

        discoverFragment.setmScrollListener(this);
        discountFragment.setmScrollListener(this);
        list.add(discountFragment);
        list.add(discoverFragment);
        list.add(rankFragment);

        mArcMenu.setOnMenuItemClickListener(new onitmeListener());

        pagerAdapter = new MainPagerAdapter(list,
                getSupportFragmentManager(), viewPager,
                new pageChangeListener());

        viewPager.setCurrentItem(1);

    }


    /**
     * 初始化toolbar
     */
    private void initialiToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.tb_main_activity);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(Color.WHITE);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        MenuItem item = menu.findItem(R.id.menu_mian_city);
        mainActyProvider = (MainActyProvider) MenuItemCompat.getActionProvider(item);
        mainActyProvider.setOnPopupMenuitemListener(new MainActyProvider.OnPopupMenuitemListener() {
            @Override
            public void clickItem(int position, String city) {
                currentCity = city;
                updateCity(position);

            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * 初始化Bmob的消息推送
     */
    private void configBmob() {
        // 初始化BmobSDK
        Bmob.initialize(this, Contants.BMOB_APP_KEY);
        // 使用推送服务时的初始化操作
        savaUserWithInsId();
        //自动升级
        BmobUpdateAgent.update(this);
        // 启动推送服务
        cn.bmob.push.BmobPush.startWork(this);
        //注册信息接收者
        registerBoradcastReceiver();
    }

    @Override
    public void bindData() {

        setTitle(R.string.discover);


        if (cuser == null) {
            loginFunction();
        }


    }

    @Override
    public void handerMessage(Message msg) {
        switch (msg.what) {

            case MESSAGE_BAIDU_MAP_DELAY://启动百度地图定位
                bdlbsUtils.startLocation();
                break;
        }
    }

    /**
     * 更改城市，更改经纬度
     *
     * @param position
     */
    private void updateCity(int position) {
        List<String> cityList = XmlUtils.getSelectCitiesLongitude(this);

        app.getCacheInstance().put(Contants.ACAHE_KEY_LONGITUDE, cityList.get(position));

        //更新数据
//        if (isDiscount) {
////            discountFragment.getRemoteData();
//            discountFragment.getRemoteData();
//        } else {
//            discoverFragment.getDataFromRemote();
//        }
        updateDisFragmentData();

    }

    /**
     * 更新 discountFragmen、discoverFragment的数据
     */
    private void updateDisFragmentData() {
        //更新数据
        if (isDiscount) {
//            discountFragment.getRemoteData();
            discountFragment.getRemoteData();
        } else {
            discoverFragment.getDataFromRemote();
        }
    }

    @Override
    public void scrollStop() {
//        LogUtils.e("停止");

        Animation slidOutAnimation = AnimationUtils.loadAnimation(this, R.anim.button_slid_bottom_in);
//        slidOutAnimation.setInterpolator(new DecelerateInterpolator());
        slidOutAnimation.setFillEnabled(true);
        slidOutAnimation.setFillBefore(true);
        slidOutAnimation.setFillAfter(true);
        slidOutAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                mArcMenu.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mArcMenu.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mArcMenu.startAnimation(slidOutAnimation);

//        translateAnimation.start();
//        mArcMenu.setVisibility(View.VISIBLE);
//        LogUtils.e("停止");
    }

    @Override
    public void scrollStart() {
//        LogUtils.e("开始");

        if (mArcMenu.getVisibility() == View.VISIBLE) {
            Animation slidInAnimation = AnimationUtils.loadAnimation(this, R.anim.button_slid_bottom_out);
            slidInAnimation.setFillEnabled(true);
            slidInAnimation.setFillAfter(true);
            slidInAnimation.setFillBefore(true);
            slidInAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    mArcMenu.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            mArcMenu.startAnimation(slidInAnimation);
        }
//        mArcMenu.setVisibility(View.GONE);

    }

    /**
     * 页面监听
     */
    private class pageChangeListener implements MainPagerAdapter.OnPageSelected {

        int lastIndex = 1;

        @Override
        public void onselected(int position) {

            switch (position) {

                case 0:
                    setTitle(R.string.discount);
//                    mArcMenu.setVisibility(View.VISIBLE);
                    isDiscount = true;
                    lastIndex = 0;
                    break;

                case 1:

                    setTitle(R.string.discover);
//                    mArcMenu.setVisibility(View.VISIBLE);

                    isDiscount = false;
                    if (lastIndex == 2) {
                        scrollStop();
                    }

                    lastIndex = 1;
                    break;

                case 2:
                    setTitle(R.string.rank);
//                    mArcMenu.setVisibility(View.GONE);
                    scrollStart();
                    lastIndex = 2;
                    break;

            }

        }

    }

    //注册广播接收者。Bmob推送消息 更新UI
    public void registerBoradcastReceiver() {
//        myIntentFilter= new IntentFilter();
        myIntentFilter.addAction(Contants.BMOB_PUSH_MESSAGES);
        //注册广播
        registerReceiver(mBroadcastReceiver, myIntentFilter);
        isRegistBordcast = true;

    }

    //注册广播接收者。分享发现、优惠 更新UI
    public void registerBoradcastReceiverShare() {
        myIntentFilter = new IntentFilter();
        myIntentFilter.addAction(Contants.BORDCAST_SHARE);
        //注册广播
        registerReceiver(mBroadcastReceiver, myIntentFilter);
        isRegistBordcast = true;

    }


    /**
     * 登录过，才能进行登录
     * 没有登录过，则不进行其他操作
     */
    private void loginFunction() {
        Dialog4Tips.loginFunction(mActivity);

    }


    // 退出程序
    public boolean dispatchKeyEvent(KeyEvent event) {

        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK
                && event.getAction() != KeyEvent.ACTION_UP) {

            AlertDialog.Builder alertbBuilder = DialogUtils.exitDialog(mActivity);
            alertbBuilder.show();

            return true;

        } else {

            return super.dispatchKeyEvent(event);

        }
    }

    /**
     * 定位回调
     * 回调 10次 还是没有得到位置，那么就停止
     */
    private class locationListener implements BDLBSUtils.LocationInfoListener {

        @Override
        public void LocationInfo(double latitude, double longitude,
                                 String Province, String City,
                                 String District, String Street,
                                 String StreetNumber) {

            times++;

            if (Province != null) {

                if (mainActyProvider != null && mainActyProvider.getCityTx() != null) {
                    mainActyProvider.getCityTx().setText(String.format("%s%s", getString(R.string.txt_current_city), City));
                }

                Bundle bundle = new Bundle();
                bundle.putDouble(Contants.LATITUDE, latitude);
                bundle.putDouble(Contants.LONGITUDE, longitude);
                bundle.putString(Contants.PROVINCE, Province);
                bundle.putString(Contants.CITY, City);
                bundle.putString(Contants.DISTRICT, District);
                bundle.putString(Contants.STREET, Street);
                bundle.putString(Contants.STREETNUMBER, StreetNumber);

                intents.setAction(Contants.BORDCAST_LOCATIONINFO);
                intents.putExtra(BUNDLE_BROADCAST, bundle);
                sendBroadcast(intents);

//                LogUtils.i("定位成功 定位信息 发送广播 ");

                bdlbsUtils.stopLocation();
            } else {//没有定位成功
                bdlbsUtils.stopLocation();
                //如果不够十次的话继续定位
                if (times <= callbackTimes) {
                    mHandler.sendEmptyMessageDelayed(MESSAGE_BAIDU_MAP_DELAY, DELAYED);
                }
            }
        }
    }

    /**
     * 消息的广播接收者
     * 处理说到的广播信息
     */
    public BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {


        @Override
        public void onReceive(Context context, Intent intent) {

            LogUtils.d("Intent getAction = " + intent.getAction());
//
            switch (intent.getAction()) {
                case Contants.BMOB_PUSH_MESSAGES://"Bmob  信息
                    LogUtils.e("main acitivity bmob ：" + intent.getStringExtra(PushConstants.EXTRA_PUSH_MESSAGE_STRING));
//                    LogUtils.ts("mian activity" + intent.getStringExtra(PushConstants.EXTRA_PUSH_MESSAGE_STRING));
/*更新界面数据*/
                    if (!TextUtils.isEmpty(intent.getStringExtra(PushConstants.EXTRA_PUSH_MESSAGE_STRING))) {

//                        initMessagesIcon(true);
                        JSONObject jsonObject = null;
                        try {
                            jsonObject = new JSONObject(intent.getStringExtra(PushConstants.EXTRA_PUSH_MESSAGE_STRING));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        if (jsonObject != null) {
                            MessageNotification.showReceiveComment(mActivity, StringUtils.getEmotionContent(mActivity, jsonObject.optString("messageBody")));
                        }

                    }

                    break;
//                case Contants.BORDCAST_REQUEST_LOCATIONINFO://使用百度定位
//                    //开始 百度定位
//                    times = 0;
//                    startLocation();
//                    break;

                case Contants.BORDCAST_SHARE:
                    //分享后刷新
                    updateDisFragmentData();

                    break;


            }

        }

    };


    /**
     * 自定义按钮的点击事件
     */
    private class onitmeListener implements ArcMenu.OnMenuItemClickListener {
        private ImageView itemIm;


        @Override
        public void onClick(View view, int pos) {
//            LogUtils.i("view = "+view+" position = "+pos);
            itemIm = (ImageView) view;
            cuser = BmobUser.getCurrentUser(mActivity, MyUser.class);

            switch (pos) {
                case 1://分享信息
                    shareActivity();

                    break;
                case 2://消息中心
                    messageCenter();
                    break;
                case 3://个人中心

                    if (cuser != null) {

                        mStartActivity(PersonalCenterActivity.class);

                    } else {
                        loginFunction();
                    }
                    break;
            }
        }

        @Override
        public void onViewGroundClickListener(View view) {
            itemIm = (ImageView) view;
            itemIm.setImageResource(R.drawable.ic_more);
        }
    }

    /**
     * 进入分享信息界面
     */
    private void shareActivity() {
        if (cuser != null) {
            //注册广播接收者 ，分享后刷新ui
            registerBoradcastReceiverShare();
            Bundle bundle = new Bundle();
            bundle.putBoolean(Contants.BUNDLE_CURRENT_IS_DISCOUNT, isDiscount);
            //定位信息请求，注册广播接收者
//            registerBoradcastReceiverRequestLocation();
            intents = new Intent(this, ShareActivity.class);
            intents.putExtras(bundle);
            mActivity.startActivityForResult(intents, Contants.REQUSET_SHARE_DISCOVER);


        } else {

            loginFunction();
        }
    }

    /**
     * 进入消息中心
     */
    private void messageCenter() {

        if (cuser != null) {
            //注册广播接收者
            intents = new Intent(this, MessageCenterActivity.class);
            mActivity.startActivityForResult(intents, Contants.REQUSET_MESSAGE_CENTER);

        } else {
//用户还没登陆
            loginFunction();

        }
    }

    /**
     * 将installationId与user绑定
     */
    private void savaUserWithInsId() {
        if (cuser != null) {
//            MyBmobInstallation myBmobInstallation = new MyBmobInstallation(this);
//            myBmobInstallation.setMyUser(cuser);
//            myBmobInstallation.save(this);

            BmobPush.updateinstallationId(this, cuser);


        }
    }

    @Override
    protected void onResume() {
        super.onResume();
//        cuser = BmobUser.getCurrentUser(mActivity, MyUser.class);
//
//        savaUserWithInsId();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bdlbsUtils.stopLocation();
        if (isRegistBordcast) {
            unregisterReceiver(mBroadcastReceiver);
        }

    }
}
