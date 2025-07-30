package cordova.plugin.mocklocationcheck;

import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MockLocationCheck extends CordovaPlugin {
    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("isMockLocationEnabled")) {
            boolean isEnabled = isMockLocationEnabled(cordova.getContext());
            callbackContext.success(isEnabled ? "true" : "false");
            return true;
        } else if (action.equals("getMockLocationApp")) {
            String app = getMockLocationApp(cordova.getContext());
            callbackContext.success(app != null ? app : "No app found");
            return true;
        }
        return false;
    }

    // 检查是否允许模拟位置
    public boolean isMockLocationEnabled(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Settings.Secure.getInt(context.getContentResolver(),
                    Settings.Secure.ALLOW_MOCK_LOCATION, 0) != 0;
        } else {
            return !"0".equals(Settings.Secure.getString(context.getContentResolver(),
                    Settings.Secure.ALLOW_MOCK_LOCATION));
        }
    }

    // 获取当前使用的模拟位置应用
    public String getMockLocationApp(Context context) {
        return Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.MOCK_LOCATION_APP);
    }
}
