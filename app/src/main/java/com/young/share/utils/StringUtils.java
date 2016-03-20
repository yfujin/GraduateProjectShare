package com.young.share.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.text.style.ImageSpan;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.young.share.R;
import com.young.share.config.Contants;

import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 字符串处理
 */
public class StringUtils {

    /**
     * 匹配字符串中特定的序列，使用正则表达式
     * 匹配这样的字符串[**]，匹配之后再对比
     *
     * @param context
     * @param tv
     * @param source
     * @return
     */
    public static SpannableString getEmotionContent(final Context context, final TextView tv, String source) {
        SpannableString spannableString = new SpannableString(source);
        Resources res = context.getResources();

        String regexEmotion = "\\[([\u4e00-\u9fa5\\w])+\\]";
        Pattern patternEmotion = Pattern.compile(regexEmotion);
        Matcher matcherEmotion = patternEmotion.matcher(spannableString);

        while (matcherEmotion.find()) {
            // 获取匹配到的具体字符
            String key = matcherEmotion.group();
            // 匹配字符串的开始位置
            int start = matcherEmotion.start();
            // 利用表情名字获取到对应的图片
            Integer imgRes = EmotionUtils.getImgByName(key);
            if (imgRes != 0) {
                // 压缩表情图片
                int size = (int) tv.getTextSize();
                Bitmap bitmap = BitmapFactory.decodeResource(res, imgRes);
                Bitmap scaleBitmap = Bitmap.createScaledBitmap(bitmap, size, size, true);

                ImageSpan span = new ImageSpan(context, scaleBitmap);
                spannableString.setSpan(span, start, start + key.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
        return spannableString;
    }


    /**
     * 随机字符串，长度是4
     *
     * @return
     */
    public static String getRanDom() {
        String val = "";

        Random random = new Random();
        for (int i = 0; i < Contants.NICKNAME_MIN_LENGHT; i++) {
            String charOrNum = random.nextInt(2) % 2 == 0 ? "char" : "num"; // 输出字母还是数字

            if ("char".equalsIgnoreCase(charOrNum)) {// 字符串

                int choice = random.nextInt(2) % 2 == 0 ? 65 : 97; // 取得大写字母还是小写字母
                val += (char) (choice + random.nextInt(26));
            } else if ("num".equalsIgnoreCase(charOrNum)) { // 数字

                val += String.valueOf(random.nextInt(10));
            }
        }

        return val;
    }

    /**
     * 将全部的用户id对应的头像都添加在string中
     *
     * @param context
     * @param userIds
     * @param avatarList
     * @return
     */
    public static SpannableStringBuilder idConver2Bitmap(final Context context,
                                                         List<String> userIds,
                                                         List<Bitmap> avatarList) {

        String text = "";
        for (String id : userIds) {
            text = text + id + "  ";
        }

        SpannableStringBuilder ssb = new SpannableStringBuilder(text);

        ClickableSpan click_span = new ClickableSpan() {

            @Override
            public void onClick(View widget) {

                Toast.makeText(context,
                        "Image Clicked " + widget.getId() + " tag " + widget.getTag(),
                        Toast.LENGTH_SHORT).show();

            }

        };

        for (int i = 0; i < userIds.size(); i++) {
            Pattern pattern = Pattern.compile(userIds.get(i));
            Matcher matcher = pattern.matcher(text);
            ImageSpan imageSpan = new ImageSpan(context, avatarList.get(i));

            while (matcher.find()) {
                ssb.setSpan(imageSpan
                        , matcher.start(), matcher
                        .end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            
            int start = ssb.getSpanStart(imageSpan);
            int end = ssb.getSpanEnd(imageSpan);
/*设置监听*/
            ssb.setSpan(click_span, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        }

        return ssb;
    }

    /**
     * 评论与回复，用户名点击
     *
     * @param context
     * @param str
     * @param textLink
     * @return
     */
    public static SpannableStringBuilder clickUsername(final Context context, final String str, final TextLink textLink) {
        SpannableString spanStr = new SpannableString(str);

        spanStr.setSpan(null, 0, 1, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        SpannableStringBuilder ssb = new SpannableStringBuilder(spanStr);

        ssb.setSpan(new ClickableSpan() {

            @Override
            public void onClick(View widget) {

                if (textLink != null) {
                    textLink.onclick(str);
                }
                LogUtils.d("text click " + str);
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(context.getResources().getColor(R.color.color_name)); // 设置文本颜色
                // 去掉下划线
                ds.setUnderlineText(false);
            }

        }, 0, str.length(), 0);
//            }
//        }

        return ssb;
    }


    public interface TextLink {
        void onclick(String str);
    }
}
