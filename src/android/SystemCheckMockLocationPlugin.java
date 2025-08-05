package cn.one.pdd.plugin;

import android.content.Context;
import android.provider.Settings;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This class echoes a string called from JavaScript.
 */
public class SystemCheckMockLocationPlugin extends CordovaPlugin {

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("isDeveloperModeEnabled")) {
            this.isDeveloperModeEnabled(callbackContext);
            return true;
        } else if (action.equals("isMockLocationEnabled")) {
            // String message = args.getString(0);
            //this.isMockLocationEnabled(callbackContext);
            return true;
        }
        return false;
    }

    public void isDeveloperModeEnabled(CallbackContext callbackContext) {
        Context context = cordova.getActivity().getApplicationContext();
        int enabled =
            Settings.Global.getInt(
                context.getContentResolver(),
                Settings.Global.DEVELOPMENT_SETTINGS_ENABLED,
                0
            );
        // 构造 JSONObject 传递结果
        JSONObject result = new JSONObject();
        try {
            result.put("enabled", enabled);
            callbackContext.success(result);
        } catch (JSONException e) {
            callbackContext.error("构造返回数据失败：" + e.getMessage());
        }
    }

//    public void isMockLocationEnabled(CallbackContext callbackContext) {
//        Context context = cordova.getActivity().getApplicationContext();
//
//        try {
//            boolean mHasAddTestProvider = false;
//            // 1. 检查全局开关是否开启
//            boolean mCanMockPosition =
//                    (Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.ALLOW_MOCK_LOCATION, 0) != 0)
//                            || Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
//            if (mCanMockPosition) {
//                try {
//                    LocationManager mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
//                    // gps 提供
//                    String providerStr = LocationManager.GPS_PROVIDER;
//                    // 位置提供者
//                    LocationProvider provider = mLocationManager.getProvider(providerStr);
//                    String mProviderName = providerStr;
//                    if (null != provider) {
//                        mProviderName = provider.getName();
//                        // 添加测试位置提供
//                        mLocationManager.addTestProvider(
//                                mProviderName,
//                                provider.requiresNetwork(),
//                                provider.requiresSatellite(),
//                                provider.requiresCell(),
//                                provider.hasMonetaryCost(),
//                                provider.supportsAltitude(),
//                                provider.supportsSpeed(),
//                                provider.supportsBearing(),
//                                provider.getPowerRequirement(),
//                                provider.getAccuracy()
//                        );
//                    }
//                    else {
//                        mLocationManager.addTestProvider(
//                                providerStr,
//                                true,
//                                true,
//                                false,
//                                false,
//                                true,
//                                true,
//                                true,
//                                3,
//                                1
//                        );
//                    }
//
//                    // 设置测试提供可用
//                    mLocationManager.setTestProviderEnabled(mProviderName, true);
//                    // 设置测试提供状态
//                    mLocationManager.setTestProviderStatus(
//                            mProviderName,
//                            LocationProvider.AVAILABLE,
//                            null,
//                            System.currentTimeMillis()
//                    );
//                    // 模拟位置可用
//                    mHasAddTestProvider = true;
//                    mCanMockPosition = true;
//                    //释放资源
//                    mLocationManager.setTestProviderEnabled(
//                            mProviderName,
//                            false
//                    );
//                    mLocationManager.removeTestProvider(mProviderName);
//                } catch (Exception e) {
//                    mCanMockPosition = false;
//                }
//            }
//            int isEnabled = (mCanMockPosition && mHasAddTestProvider) ? 1 : 0;
//
//            JSONObject result = new JSONObject();
//            result.put("enabled", isEnabled);
//            callbackContext.success(result);
//        } catch (Exception e) {
//            callbackContext.error("Error checking mock location: " + e.getMessage());
//        }
//    }

}
