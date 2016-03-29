package com.young.share;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Message;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bigkoo.svprogresshud.SVProgressHUD;
import com.young.share.annotation.InjectView;
import com.young.share.base.BaseAppCompatActivity;
import com.young.share.config.Contants;
import com.young.share.model.MyUser;
import com.young.share.utils.BDLBSUtils;
import com.young.share.utils.LogUtils;
import com.young.share.utils.StringUtils;
import com.young.share.views.IdentifyCodeDialog;

import cn.bmob.sms.BmobSMS;
import cn.bmob.sms.exception.BmobException;
import cn.bmob.sms.listener.RequestSMSCodeListener;
import cn.bmob.v3.listener.SaveListener;

/**
 * 用户注册界面
 * <p>
 * Created by Nearby Yang on 2015-10-20.
 */
public class RegisterActivity extends BaseAppCompatActivity implements View.OnClickListener {

    @InjectView(R.id.et_reg_phone)
    private EditText registPhone;
    @InjectView(R.id.et_register_pwd)
    private EditText registPwd;
    @InjectView(R.id.et_register_config_pwd)
    private EditText registConfigPwd;
    @InjectView(R.id.tv_go_login)
    private TextView gotoLogin;
    @InjectView(R.id.tv_register_btn)
    private TextView registerBtn;
    @InjectView(R.id.im_reg_count_state)
    private ImageView phoneState;
    @InjectView(R.id.im_reg_pwd_state)
    private ImageView pwdState;
    @InjectView(R.id.im_reg_con_pwd_state)
    private ImageView confPwdState;
    @InjectView(R.id.txt_reg_get_identify_code)
    private TextView identifyCodeTx;

    private IdentifyCodeDialog identifyCodeDialog;
    private BDLBSUtils bdlbsUtils;
    private boolean phoneNumberVaild;//手机号验证错误
    private boolean pwdVaild;//密码无效
    private boolean comfPwdVaild;//确认密码无效
    private boolean phoneVerific = false;//手机号验证结果

    private String province = "广东省";
    private String city = "惠州市";
    private String district;
    private String street;
    private String streetNumber;

    private static final int MESSAGE_LOCATION = 0x01;//定位

    @Override
    public int getLayoutId() {
        return R.layout.activity_register;
    }

    @Override
    public void initData() {
        initialiToolbar();
        setTitle(R.string.regist);
        bdlbsUtils = new BDLBSUtils(this, new locationListener());
        bdlbsUtils.startLocation();

          /*初始化信息服务*/
        BmobSMS.initialize(this, Contants.BMOB_APP_KEY);
//        saveFile2SDCard();
    }

    @Override
    public void findviewbyid() {
        gotoLogin.setOnClickListener(this);
        registerBtn.setOnClickListener(this);
        identifyCodeDialog = new IdentifyCodeDialog(this);
    }

    @Override
    public void bindData() {
        /*输入监听*/
        textChangeListener();
/*设置dialog的dismissListener*/
        dialogSetDismiss();
/*注册按钮，初始状态，不可点击*/
        registerBtn.setEnabled(false);
        identifyCodeTx.setOnClickListener(this);

    }

    /**
     * dialog dismiss listener
     * 取得
     */
    private void dialogSetDismiss() {
        identifyCodeDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
/**
 * 验证通过，EditText进入不可编辑状态，反之
 */
                registPhone.setEnabled(phoneVerific = identifyCodeDialog.isMobilePhoneVerified());
                identifyCodeTx.setEnabled(phoneVerific);
                if (phoneVerific) {
                    registPhone.setTextColor(getResources().getColor(R.color.gray));
                }
            }
        });
    }


    /**
     * 输入框的内容判断
     * 减少发送时候的逻辑处理逻辑
     */
    private void textChangeListener() {
        /*文字输入监听,在完成之后进行显示状态*/
        registPhone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                phoneNumberVaild = StringUtils.phoneNumberValid(registPhone.getText().toString().trim());
                if (phoneNumberVaild) {
                    phoneState.setVisibility(View.VISIBLE);
                    phoneState.setImageResource(R.drawable.icon_checked);
                } else {
                    registPhone.setError(Html.fromHtml("<font color='white'>手机号码格式不对</font>"));
                }
                /*更新注册按钮的状态*/
                registerBtn.setEnabled(phoneNumberVaild && pwdVaild && comfPwdVaild);
            }
        });
/*第一次输入密码*/
        registPwd.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                boolean minlangth = pwdVaild = !TextUtils.isEmpty(registPwd.getText().toString().trim())
                        && registPwd.getText().toString().trim().length() >= 6;
                /*密码长度*/
                if (!pwdVaild) {
                    registPwd.setError(Html.fromHtml("<font color='white'>密码长度不少于6位</font>"));
                } else {
                    pwdState.setVisibility(View.VISIBLE);
                    pwdState.setImageResource(R.drawable.icon_checked);

                }
                /*两次密码相同否*/
                boolean pwdEqual = TextUtils.isEmpty(registConfigPwd.getText().toString().trim());

                if (!pwdEqual) {
                    pwdEqual = registPwd.getText().toString().trim()
                            .equals(registConfigPwd.getText().toString().trim());
                    if (!pwdEqual) {

                        registPwd.setError(Html.fromHtml("<font color='white'>两次输入密码不相符</font>"));
                    } else {
                        pwdState.setVisibility(View.VISIBLE);
                        pwdState.setImageResource(R.drawable.icon_checked);

                    }
                }


                pwdVaild = pwdVaild && pwdEqual;
                /*更新注册按钮的状态*/
                registerBtn.setEnabled(phoneNumberVaild && pwdVaild && comfPwdVaild);
            }
        });
/*确认密码*/
        registConfigPwd.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                comfPwdVaild = !TextUtils.isEmpty(registConfigPwd.getText().toString().trim())
                        && registConfigPwd.getText().toString().trim().length() >= 6;

                confPwdState.setVisibility(View.VISIBLE);

                boolean pwdEqual = registPwd.getText().toString().trim()
                        .equals(registConfigPwd.getText().toString().trim());
                if (!pwdEqual || !comfPwdVaild) {

                    registConfigPwd.setError(Html.fromHtml("<font color='white'>两次输入密码不相符</font>"));
                } else {
                    confPwdState.setImageResource(R.drawable.icon_checked);
                    pwdVaild = true;
                }

                registerBtn.setEnabled(phoneNumberVaild && pwdVaild && comfPwdVaild);
            }
        });
    }

    /**
     * 初始化toolbar
     */
    private void initialiToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.tb_register);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.icon_menu_back);
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mActivity.finish();
            }
        });

    }

    @Override
    public void handerMessage(Message msg) {
        switch (msg.what) {
            case MESSAGE_LOCATION:

                // 停止定位服务并且回到登陆界面
                gotoLoginAndStopLocationServices();
                break;
        }
    }

    @Override
    public void mBack() {
        mBackStartActivity(LoginActivity.class);
    }

    @Override
    public void onClick(View v) {


        switch (v.getId()) {
            case R.id.tv_register_btn:

                SVProgressHUD.show(this);
                /*注册*/
                regiter();

                break;

            case R.id.tv_go_login:

                gotoLoginAndStopLocationServices();
                break;

            case R.id.txt_reg_get_identify_code://获取验证码
/*请求发送验证码*/
                String phone = registPhone.getText().toString().trim();
                if (!TextUtils.isEmpty(phone)) {//手机号不为空
                    if (StringUtils.phoneNumberValid(phone)) {//手机号格式验证

                        requestIdentifyCode(phone);
                        identifyCodeDialog.setPhoneNumber(phone);
                /*输入验证码*/
                        identifyCodeDialog.show();
                    } else {
                        Toast.makeText(this, R.string.toast_phone_number_format_not_correct, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, R.string.toast_phone_number_empty, Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    /**
     * 请求发送验证码，请求按钮不可点击
     */
    private void requestIdentifyCode(String phone) {


//        identifyCodeTx.setEnabled(false);

        BmobSMS.requestSMSCode(this, phone, Contants.BMOB_MESSAGE_TEMPLE, new RequestSMSCodeListener() {

            @Override
            public void done(Integer smsId, BmobException ex) {
                if (ex == null) {//验证码发送成功
                    Toast.makeText(mActivity, R.string.toast_send_idnetify_code_success, Toast.LENGTH_SHORT).show();
                    LogUtils.i("bmob 短信id：" + smsId);//用于查询本次短信发送详情

                }
            }
        });


    }

    /**
     * 用户注册
     */
    private void regiter() {

        MyUser myUserRegister = new MyUser();
        String phone = registPhone.getText().toString().trim();
        String pwd = registPwd.getText().toString().trim();

/*有效*/
        if (phoneNumberVaild && pwdVaild) {


            SVProgressHUD.show(this);
            //随机字符串
            myUserRegister.setNickName(StringUtils.getRanDom());
            myUserRegister.setMobilePhoneNumber(phone);
            myUserRegister.setPassword(pwd);
            myUserRegister.setAddress(province + city + district);
            myUserRegister.setUsername(phone);

            myUserRegister.signUp(RegisterActivity.this, new SaveListener() {
                @Override
                public void onSuccess() {

                    SVProgressHUD.showSuccessWithStatus(RegisterActivity.this, getString(R.string.register_success));
                    mHandler.sendEmptyMessageDelayed(MESSAGE_LOCATION, Contants.ONE_SECOND);

                }

                @Override
                public void onFailure(int i, String s) {
/*j*/
                   if(i==209||i == 202){
                        SVProgressHUD.showInfoWithStatus(RegisterActivity.this, getString(R.string.user_had_register));
                    }
                    LogUtils.e(getClass().getName(), "注册失败  code = " + i + " message = " + s);
                }
            });


        } else {

            SVProgressHUD.showInfoWithStatus(this, getString(R.string.pwd_not_equals), SVProgressHUD.SVProgressHUDMaskType.Gradient);

        }


    }

    /**
     * 停止定位，回到登陆界面
     */
    private void gotoLoginAndStopLocationServices() {

        intents.setClass(this, LoginActivity.class);

        bdlbsUtils.stopLocation();


        startActivity(intents);

        this.finish();
    }

    private class locationListener implements BDLBSUtils.LocationInfoListener {

        @Override
        public void LocationInfo(double latitude, double longitude,
                                 String Province, String City,
                                 String District, String Street,
                                 String StreetNumber) {

            province = Province;
            city = City;
            district = District;
            street = Street;
            streetNumber = StreetNumber;
        }
    }
}
