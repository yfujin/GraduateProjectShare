package com.young.share;

import android.os.Message;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.bigkoo.svprogresshud.SVProgressHUD;
import com.young.annotation.InjectView;
import com.young.base.ItemActBarActivity;
import com.young.config.Contants;
import com.young.utils.LogUtils;
import com.young.utils.XmlUtils;
import com.young.views.CitySelectPopupWin;
import com.young.views.PopupWinListView;

import java.util.HashMap;

import cn.bmob.v3.listener.UpdateListener;
import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;
import cn.smssdk.gui.RegisterPage;

/**
 * Created by Nearby Yang on 2015-11-13.
 */
public class EditPersonalInfoActivity extends ItemActBarActivity implements View.OnClickListener {

    @InjectView(R.id.et_edit_personal_nickname)
    private EditText nickname_et;
    @InjectView(R.id.et_edit_personal_qq)
    private EditText qq_et;
    @InjectView(R.id.et_edit_personal_email)
    private EditText email_et;
    @InjectView(R.id.et_edit_personal_mobile_phone_number)
    private EditText mobilePhone_et;
    @InjectView(R.id.popupwin_edit_personnal_info_gender)
    private TextView gender_tv;
    @InjectView(R.id.popupwin_edit_personnal_info_age)
    private TextView age_tv;
    @InjectView(R.id.popupwin_edit_personnal_info_hometown)
    private TextView hometown_tv;

    @InjectView(R.id.confirm_pwd_bt)
    private TextView save_tv;
//    @InjectView(R.id.cancel_pwd)
//    private TextView cancel_tv;

    private PopupWinListView gender_popupList;
    private PopupWinListView age_popupList;
    private CitySelectPopupWin hometown_popupList;

    private String hometown;
    private String gender;
    private String age;

    @Override
    public int getLayoutId() {
        return R.layout.activity_edit_personal_info;
    }

    @Override
    public void initData() {
        super.initData();

        setTvTitle(R.string.edit_personal_info);
        setBarItemVisible(true, false);
        setItemListener(new BarItemOnClick() {
            @Override
            public void leftClick(View v) {
                back2super();
            }

            @Override
            public void rightClivk(View v) {

            }
        });

        //手机短信验证
        SMSSDK.initSDK(this, Contants.SMS_APP_KEY, Contants.SMS_APP_SECRET);
    }

    @Override
    public void findviewbyid() {
        save_tv.setOnClickListener(this);
//        cancel_tv.setOnClickListener(this);
        gender_tv.setOnClickListener(this);
        age_tv.setOnClickListener(this);
        hometown_tv.setOnClickListener(this);
    }

    @Override
    public void bindData() {
        gender_popupList = new PopupWinListView(this, XmlUtils.getSelectGender(this), false);
        age_popupList = new PopupWinListView(this, XmlUtils.getSelectAge(this), false);
        hometown_popupList = new CitySelectPopupWin(this, XmlUtils.getSelectCities(this));

        gender_popupList.setItemClick(new PopupWinListView.onItemClick() {
            @Override
            public void onClick(View view, String str, int position, long id) {
//                LogUtils.logD("性别 = "+ str +" position = "+position);
                gender_tv.setText(str);
                gender = str;
            }
        });

        age_popupList.setItemClick(new PopupWinListView.onItemClick() {
            @Override
            public void onClick(View view, String str, int position, long id) {
//                LogUtils.logD("年龄 = "+ str +" position = "+position);
                age_tv.setText(str);
                age = str;
            }
        });

        hometown_popupList.setResultListener(new CitySelectPopupWin.ResultListener() {
            @Override
            public void result(String str) {
//                LogUtils.logD("城市 = "+ str);
                hometown_tv.setText(str);
                hometown = str;
            }
        });

        getUserDatas();

    }

    /**
     * 显示用户的信息
     */
    private void getUserDatas() {
        String name = mUser.getNickName();
        String qq = mUser.getQq();
        String gender = mUser.isGender() ? Contants.GENDER_MALE : Contants.GENDER_FEMALE;
        String age = String.valueOf(mUser.getAge());
        String email = mUser.getEmail();
        String mobilePhoneNumber = mUser.getMobilePhoneNumber();
        String hometown = TextUtils.isEmpty(mUser.getAddress()) ? getString(R.string.gd_hz) : mUser.getAddress();

        nickname_et.setText(name);
        qq_et.setText(qq);
        gender_tv.setText(gender);
        age_tv.setText(age);
        email_et.setText(email);
        mobilePhone_et.setText(mobilePhoneNumber);
        hometown_tv.setText(hometown);

    }

    @Override
    public void handerMessage(Message msg) {
        back2super();
    }

    private void back2super() {
        mBackStartActivity(PersonalCenterActivity.class);
        finish();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK
                && event.getAction() != KeyEvent.ACTION_UP) {
            mBackStartActivity(MainActivity.class);
        }


        return super.dispatchKeyEvent(event);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.confirm_pwd_bt://保存修改
                handlerUIDatas();
                break;

//            case R.id.cancel_pwd:
//                back2super();
//                break;

            case R.id.popupwin_edit_personnal_info_gender://性别
                gender_popupList.onShow(v);
                break;
            case R.id.popupwin_edit_personnal_info_age://年龄
                age_popupList.onShow(v);
                break;
            case R.id.popupwin_edit_personnal_info_hometown://城市
                hometown_popupList.setDatas(XmlUtils.getSelectCities(this));
                hometown_popupList.onShow(v);
                break;
        }
    }


    private void handlerUIDatas() {
        //1手机号，邮箱，昵称，qq，家乡，性别，年龄
        if (!TextUtils.isEmpty(nickname_et.getText().toString()) && nickname_et.getText().toString().length() >= Contants.NICKNAME_MIN_LENGHT) {
            if (nickname_et.getText().toString().length() <= Contants.NICKNAME_MAX_LENGHT) {
//User user = new User();
                if (!TextUtils.isEmpty(mobilePhone_et.getText().toString())) {
                    smsVerfied(mobilePhone_et.getText().toString());

                } else {
                    updateUserInfo();
                }


            } else {//昵称长度太长
                SVProgressHUD.showErrorWithStatus(this, String.format(getString(R.string.nickname_lenght_toolong), Contants.NICKNAME_MAX_LENGHT));
            }
        } else {//你曾长度太短
            SVProgressHUD.showErrorWithStatus(this, getString(R.string.nickname_lenght_short));
        }
    }

    private void updateUserInfo() {

        mUser.setNickName(nickname_et.getText().toString());
        mUser.setGender(Contants.GENDER_MALE.equals(gender_tv.getText().toString()));
        mUser.setAge(Integer.valueOf(age_tv.getText().toString()));
        mUser.setQq(qq_et.getText().toString());
        mUser.setEmail(email_et.getText().toString());
        mUser.setAddress(hometown_tv.getText().toString());

        mUser.update(this, new UpdateListener() {
            @Override
            public void onSuccess() {
                LogUtils.logD("更新信息成功");
                mHandler.sendEmptyMessageDelayed(101, Contants.ONE_SECOND);

                SVProgressHUD.showSuccessWithStatus(mActivity, getString(R.string.update_user_info_success));
            }

            @Override
            public void onFailure(int i, String s) {
                LogUtils.logD("更新失败 code = " + i + " message = " + s);
                SVProgressHUD.showErrorWithStatus(mActivity, getString(R.string.update_user_info_faile));
            }
        });
    }


    /**
     * 短信验证
     *
     * @param mobilePhoneNumber
     */
    private void smsVerfied(final String mobilePhoneNumber) {

        if (mUser.getEmailVerified()&&mobilePhoneNumber.equals(mUser.getMobilePhoneNumber())) {
            updateUserInfo();
            return;
        }

        //打开注册页面
        RegisterPage registerPage = new RegisterPage();

        registerPage.setRegisterCallback(new EventHandler() {
            public void afterEvent(int event, int result, Object data) {
// 解析注册结果
                if (result == SMSSDK.RESULT_COMPLETE) {
                    @SuppressWarnings("unchecked")
                    HashMap<String, Object> phoneMap = (HashMap<String, Object>) data;
//                    String country = (String) phoneMap.get("country");
                    String phone = (String) phoneMap.get("phone");

                    if (mobilePhoneNumber.equals(phone)) {//手机验证与预留手机号不相同

                        SVProgressHUD.showWithStatus(mActivity, getString(R.string.updating));

                        // 提交用户信息
                        mUser.setMobilePhoneNumber(mobilePhone_et.getText().toString());
                        mUser.setEmailVerified(true);

                        updateUserInfo();

                    } else {//手机验证与预留手机号不相同

                        SVProgressHUD.showErrorWithStatus(mActivity, getString(R.string.verify_mobilePhone_faile), SVProgressHUD.SVProgressHUDMaskType.GradientCancel);

                    }


                }
            }
        });

//显示验证窗口
        registerPage.show(this);
    }

}