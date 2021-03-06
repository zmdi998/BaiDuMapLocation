package com.example.json.dingwei;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.example.json.dingwei.base.BaseActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.bmob.v3.Bmob;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;


public class MainActivity extends BaseActivity implements View.OnClickListener {

    @BindView(R.id.tvMainAddress) TextView tvMainAddress;
    @BindView(R.id.ivMainActivityMenu) ImageView ivMainActivityMenu;
    @BindView(R.id.flMainActivityMenu) FrameLayout flMainActivityMenu;
    @BindView(R.id.title) RelativeLayout title;
    @BindView(R.id.barTitle) Toolbar barTitle;
    @BindView(R.id.ivMainActivityUserID) TextView ivMainActivityUserID;

    private View headView;

    public LocationClient mLocationClient;
    public MyLocationListener myListener;
    @BindView(R.id.nvMainActivity) NavigationView nvMainActivity;
    @BindView(R.id.dlMain) DrawerLayout dlMain;

    private WeiZhiBean weiZhiBeanMain;
    private long firstBack = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_drawer_layout);
        ButterKnife.bind(this);
        initHeadView();
        //第一：默认初始化
        Bmob.initialize(this, "19fb1a7ca34b632c3d283aebc5e14864");
        // requestWindowFeature(Window.FEATURE_NO_TITLE);
        EventBus.getDefault().register(this);
        myListener = new MyLocationListener();
        mLocationClient = new LocationClient(getApplicationContext());
        //声明LocationClient类
        mLocationClient.registerLocationListener(myListener);
        //注册监听函数
        initLocation();
    }

    public void initHeadView() {
        headView = nvMainActivity.inflateHeaderView(R.layout.main_activity_drawerlayout_header_layout);
        flMainActivityMenu.setOnClickListener(this);

        headView.findViewById(R.id.llMainDrawerShareMyLocation).setOnClickListener(this);
        headView.findViewById(R.id.llMainDrawerSeeOthersLocation).setOnClickListener(this);
        headView.findViewById(R.id.llMainDrawerUpdateVersion).setOnClickListener(this);
        headView.findViewById(R.id.llMainDrawerConnectUs).setOnClickListener(this);
        ivMainActivityUserID.setText("用户编号为：" + getNativePhoneNumber());

    }

    private void initLocation() {
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        //可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setCoorType("bd09ll");
        //可选，默认gcj02，设置返回的定位结果坐标系
        int span = 2000;
        option.setScanSpan(span);
        //可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setIsNeedAddress(true);
        //可选，设置是否需要地址信息，默认不需要
        option.setOpenGps(true);
        //可选，默认false,设置是否使用gps
        option.setLocationNotify(true);
        //可选，默认false，设置是否当GPS有效时按照1S/1次频率输出GPS结果
        option.setIsNeedLocationDescribe(true);
        //可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        option.setIsNeedLocationPoiList(true);
        //可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        option.setIgnoreKillProcess(false);
        //可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
        option.SetIgnoreCacheException(false);
        //可选，默认false，设置是否收集CRASH信息，默认收集
        option.setEnableSimulateGps(false);
        //可选，默认false，设置是否需要过滤GPS仿真结果，默认需要
        mLocationClient.setLocOption(option);
    }

    @Override
    protected void onStart() {
        super.onStart();
        //开启定位
        if (!mLocationClient.isStarted()) {
            mLocationClient.start();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mLocationClient.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void getEventBusData(WeiZhiBean weiZhiBean) {
        weiZhiBeanMain = weiZhiBean;
        tvMainAddress.setText(weiZhiBean.getNetKind() + "\n\n经度为：" + weiZhiBean.getLongitude() + "\n\n纬度为：" + weiZhiBean.getLatitude() + "\n\n定位时间：" + weiZhiBean.getTime() + "\n\n当前位置为：" + weiZhiBean.getAddress() + "\n\n 附近标志物：" + weiZhiBean.getLocationDescribe().toString());
        Local local = new Local();
        local.setUserID(getNativePhoneNumber());
        local.setNetKind(weiZhiBean.getNetKind());
        local.setTime(weiZhiBean.getTime());
        local.setAddress(weiZhiBean.getAddress());
        local.setLocationDescribe(weiZhiBean.getLocationDescribe());
        local.setLongitude(weiZhiBean.getLongitude());
        local.setLatitude(weiZhiBean.getLatitude());

        local.save(new SaveListener<String>() {
            @Override
            public void done(String objectId, BmobException e) {
                if (e == null) {
                } else {
                }
            }
        });

    }

    @Override
    public void onBackPressed() {

        if (System.currentTimeMillis() - firstBack < 2000) {
            super.onBackPressed();
        } else {
            firstBack = System.currentTimeMillis();
            Toast.makeText(MainActivity.this, "亲,再按一次就退出定位系统了！！！", Toast.LENGTH_SHORT).show();
        }
    }

    private void openDrawer() {
        //打开抽屉
        if (!isDrawerOpen()) {
            dlMain.openDrawer(Gravity.LEFT);
        }
    }

    private void closeDrawer() {
        doInUI(new Runnable() {
            @Override
            public void run() {
                //关闭抽屉
                if (isDrawerOpen()) {
                    dlMain.closeDrawer(Gravity.LEFT);
                    dlMain.closeDrawers();
                }
            }
        }, 500);
    }

    private boolean isDrawerOpen() {
        return dlMain.isDrawerOpen(Gravity.LEFT);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //点击左上角menu 打开抽屉
            case R.id.flMainActivityMenu:
                openDrawer();
                break;
            //分享当前位置
            case R.id.llMainDrawerShareMyLocation:
                shareMyLocation();
                closeDrawer();
                break;
            //查看其他用户的地址
            case R.id.llMainDrawerSeeOthersLocation:
                toActivity(SeeOthersLocationActivity.class);
                closeDrawer();
                break;
            //版本升级
            case R.id.llMainDrawerUpdateVersion:

                break;
            //联系我们
            case R.id.llMainDrawerConnectUs:
                toActivity(AboutOurActivity.class);
                closeDrawer();
                break;

        }
    }

    public void shareMyLocation() {
        if (weiZhiBeanMain != null) {
            AndroidShare as = new AndroidShare(MainActivity.this, "定位方式" + weiZhiBeanMain.getNetKind() + "\n\n经度为：" + weiZhiBeanMain.getLongitude() + "\n\n纬度为：" + weiZhiBeanMain.getLatitude() + "\n\n定位时间：" + weiZhiBeanMain.getTime() + "\n\n当前位置为：" + weiZhiBeanMain.getAddress() + "\n\n 附近标志物：" + weiZhiBeanMain.getLocationDescribe().toString(), "");
            as.show();
        }
    }

    /**
     * 获取电话号码
     */
    public String getNativePhoneNumber() {
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        return telephonyManager.getLine1Number();
    }
}
