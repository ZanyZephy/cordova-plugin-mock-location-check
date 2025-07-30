package cordova.plugin.mocklocationcheck;

import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;

public class MockLocationCheck extends CordovaPlugin {

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("isMockLocationEnabled")) {
            boolean isEnabled = isMockLocationEnabled(cordova.getContext());
            callbackContext.success(isEnabled ? "true" : "false");
            return true;
        }
        // 删除获取模拟位置应用的方法
        return false;
    }

    // 检查是否允许模拟位置（支持 Android 6.0 及以上版本）
    public boolean isMockLocationEnabled(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android 6.0 (API 23) 及以上版本
            return Settings.Secure.getInt(context.getContentResolver(),
                    Settings.Secure.ALLOW_MOCK_LOCATION, 0) != 0;
        } else {
            // 旧版 Android
            return !"0".equals(Settings.Secure.getString(context.getContentResolver(),
                    Settings.Secure.ALLOW_MOCK_LOCATION));
        }
    }
}
