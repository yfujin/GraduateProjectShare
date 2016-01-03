package com.young.share.config;

import android.content.Context;

import com.baidu.mapapi.SDKInitializer;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiskCache;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.UsingFreqLimitedMemoryCache;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;
import com.nostra13.universalimageloader.utils.StorageUtils;
import com.young.share.utils.ThreadUtils;

import org.litepal.LitePalApplication;

import java.io.File;


/**
 * 应有全局application
 * <p/>
 * Created by Nearby Yang on 2015-07-02.
 */
public class ApplicationConfig extends LitePalApplication {

    //是否使用调试
    public final static boolean isDebug = true;

    //单例模式
    private volatile static ApplicationConfig instance;

    public ThreadUtils threadUtils;

    @Override
    public void onCreate() {
        super.onCreate();
        initConfig();
    }

    /**
     * 应用的基本配置
     */
    private void initConfig() {
        LitePalApplication.initialize(this);
        //初始化imageloader
        initImageLoader(getApplicationContext());
        //百度基础地图定位
        SDKInitializer.initialize(getApplicationContext());


    }

    /**
     * 可回收线程池
     *
     * @return
     */
    public ThreadUtils getThreadInstance() {
        threadUtils = new ThreadUtils();
        return threadUtils;
    }


    private void initImageLoader(Context ctx) {

//        File cacheFile = new File(Environment.getExternalStorageState(), "uilCache");
        File cacheFile = StorageUtils.getOwnCacheDirectory(ctx, Contants.DOWNLOAD_PATH);

        ImageLoaderConfiguration imConfig = new ImageLoaderConfiguration.Builder(ctx)
                .memoryCacheExtraOptions(Contants.IAMGE_MAX_WIDTH, Contants.IAMGE_MAX_WIDTH) // max width, max height，即保存的每个缓存文件的最大长宽
                .threadPoolSize(3) //线程池内线程的数量
                .threadPriority(Thread.NORM_PRIORITY - 2)
                .denyCacheImageMultipleSizesInMemory()
                .diskCacheFileNameGenerator(new Md5FileNameGenerator()) //将保存的时候的URI名称用MD5 加密
                .memoryCache(new UsingFreqLimitedMemoryCache(5 * 1024 * 1024))
                .memoryCacheSize(5 * 1024 * 1024) // 内存缓存的最大值
                .diskCacheSize(50 * 1024 * 1024)  // SD卡缓存的最大值
                .tasksProcessingOrder(QueueProcessingType.LIFO)
                        // 由原先的discCache -> diskCache
                .diskCache(new UnlimitedDiskCache(cacheFile))//自定义缓存路径
                .imageDownloader(new BaseImageDownloader(ctx, 5 * 1000, 30 * 1000)) // connectTimeout (5 s), readTimeout (30 s)超时时间
//                .writeDebugLogs() // Remove for release app
                .build();

        ImageLoader.getInstance().init(imConfig);
    }


    /**
     * 单例的Application
     *
     * @return ApplicationConfig.this
     */
    public static ApplicationConfig getInstance() {
        if (instance == null) {
            synchronized (ApplicationConfig.class) {
                if (instance == null) {
                    instance = (ApplicationConfig) getContext();
                }
            }
        }
        return instance;
    }


}