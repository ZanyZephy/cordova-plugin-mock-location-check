
package cn.one.pdd.plugin;
import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

/**
 * This class echoes a string called from JavaScript.
 */
public class SystemCheckMockLocationPlugin extends CordovaPlugin {

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("isDeveloperModeEnabled")) {
            this.isDeveloperModeEnabled(callbackContext);
            return true;
        }
        else if (action.equals("isMockLocationEnabled")) {
            String message = args.getString(0);
            //this.isMockLocationEnabled(callbackContext);
            return true;
        }
        else if (action.equals("getUUID")) {
            this.getUUID(callbackContext);
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


    private static final String TAG = "DeviceIdManager";
    private static final String PREFS_NAME = "device_identifier";
    private static final String PREFS_DEVICE_ID = "device_id";
    private static final String EXTERNAL_STORAGE_FILE = ".device_id.abc";
    private static String deviceId;


    /**
     * 获取设备唯一标识
     * 优先从内存缓存获取，其次从SharedPreferences，再从外部存储，最后生成新的UUID
     */
    public void getUUID(CallbackContext callbackContext) {
        Context context = cordova.getActivity().getApplicationContext();
        try {
            if (deviceId == null) {
                // 1. 尝试从SharedPreferences获取
                SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                deviceId = prefs.getString(PREFS_DEVICE_ID, null);

                if (deviceId == null) {
                    // 2. 尝试从外部存储获取
                    deviceId = readFromExternalStorage();
                    if (deviceId != null) {
                        // 同步到SharedPreferences
                        prefs.edit().putString(PREFS_DEVICE_ID, deviceId).apply();
                    }else {
                        // 3. 生成新的UUID
                        deviceId = generateUUID(getAndroidId());

                        // 4. 保存到SharedPreferences
                        prefs.edit().putString(PREFS_DEVICE_ID, deviceId).apply();

                        // 5. 尝试保存到外部存储
                        saveToExternalStorage(deviceId);
                    }
                }
            }

            // 构造 JSONObject 传递结果
            JSONObject result = new JSONObject();
            result.put("data", deviceId);
            callbackContext.success(result);
        } catch (JSONException e) {
            callbackContext.error("构造返回数据失败：" + e.getMessage());
        }
    }

    /**
     * 生成UUID
     */
    private static String generateUUID(String prefix) {
        if (!prefix.isEmpty()) {
            String uuidmd5 = stringToMD5(prefix);
            return uuidmd5 != null ? uuidmd5 : prefix;
        }
        else {
            String uuid = UUID.randomUUID().toString().replace("-", "");
            String uuid2 = prefix + uuid + Build.BOARD + Build.MODEL;
            String uuidmd5 = stringToMD5(uuid2);
            return uuidmd5 != null ? uuidmd5 : uuid2;
        }
    }

    /**
     * 从外部存储读取设备ID
     */
    private static String readFromExternalStorage() {
        try {
            if (isExternalStorageWritable()) {
                File file = new File(getExternalStorageDir(), EXTERNAL_STORAGE_FILE);
                if (file.exists() && file.length() > 0) {
                    FileInputStream fis = new FileInputStream(file);
                    byte[] buffer = new byte[(int) file.length()];
                    fis.read(buffer);
                    fis.close();
                    return new String(buffer);
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Failed to read device ID from external storage", e);
        }
        return null;
    }

    /**
     * 保存设备ID到外部存储
     */
    private static void saveToExternalStorage(String deviceId) {
        try {
            if (isExternalStorageWritable()) {
                File file = new File(getExternalStorageDir(), EXTERNAL_STORAGE_FILE);
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(deviceId.getBytes());
                fos.close();
            }
        } catch (IOException e) {
            Log.e(TAG, "Failed to save device ID to external storage", e);
        }
    }

    /**
     * 检查外部存储是否可写
     */
    private static boolean isExternalStorageWritable() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    // 检查存储权限
    private void checkStoragePermission(Context context) {
        // 仅 Android 9 及以下需要申请 WRITE_EXTERNAL_STORAGE
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            if (context.getPackageManager().checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, context.getPackageName())
                    != PackageManager.PERMISSION_GRANTED) {
                // 未授权，发起申请
            } else {
                // 已授权，执行文件存储逻辑
            }
        } else {
            // Android 10+ 无需 WRITE_EXTERNAL_STORAGE，直接执行存储逻辑
        }
    }

    /**
     * 获取外部存储目录
     */
    private static File getExternalStorageDir() {
        File downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        File appDir = new File(downloadDir, "."+BuildConfig.APPLICATION_ID);
        if (!appDir.exists()) {
            if (!appDir.mkdirs()) {
                return downloadDir;
            }
        }
        return appDir;
    }

    /**
     * 清除存储的设备ID（用于测试）
     */
    private static void clearDeviceId(Context context) {
        // 清除内存缓存
        deviceId = null;

        // 清除SharedPreferences
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().remove(PREFS_DEVICE_ID).apply();

        // 清除外部存储文件
        if (isExternalStorageWritable()) {
            File file = new File(getExternalStorageDir(), EXTERNAL_STORAGE_FILE);
            if (file.exists()) {
                file.delete();
            }
        }
    }


    /**
     * 对字符串进行MD5加密
     * @param string 需要加密的字符串
     * @return 加密后的字符串（32位小写），如果发生异常则返回null
     */
    private static String stringToMD5(String string) {
        if (string == null || string.isEmpty()) {
            return null;
        }

        MessageDigest md5 = null;
        try {
            // 获取MD5实例
            md5 = MessageDigest.getInstance("MD5");

            // 将字符串转换为字节数组
            byte[] bytes = string.getBytes();

            // 计算MD5哈希值
            byte[] digest = md5.digest(bytes);

            // 将字节数组转换为十六进制字符串
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                // 转换为十六进制
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    sb.append('0');
                }
                sb.append(hex);
            }
            return sb.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }
}
