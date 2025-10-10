#import "IOSUUID.h"
#import <UIKit/UIKit.h>
#import <Security/Security.h>

@implementation IOSUUID

// Keychain中保存UUID的key
static NSString * const kKeychainUUIDKey = @"com.tycl.device.uuid";
// Keychain服务名称
static NSString * const kKeychainServiceName = @"com.tycl.keychain.service";

- (void)getUUID:(CDVInvokedUrlCommand*)command {
    CDVPluginResult* result = nil;
    
    // 获取设备的UUID
    NSString* uuid = [IOSUUID getDeviceUUID];
    
    if (uuid != nil) {
        result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:@{@"data": uuid}];
    } else {
        result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"Failed to get UUID"];
    }
    
    [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
}

#pragma mark - Public Methods

+ (NSString *)getDeviceUUID {
    // 首先尝试从Keychain中获取已保存的UUID
    NSString *existingUUID = [self getUUIDFromKeychain];
    
    if (existingUUID && existingUUID.length > 0) {
        NSLog(@"从Keychain中获取到已存在的UUID: %@", existingUUID);
        return existingUUID;
    }
    
    // 如果Keychain中没有UUID，则生成新的UUID
    NSString *newUUID = [self generateNewUUID];
    
    // 将新生成的UUID保存到Keychain
    BOOL saveSuccess = [self saveUUIDToKeychain:newUUID];
    
    if (saveSuccess) {
        NSLog(@"成功生成并保存新的UUID到Keychain: %@", newUUID);
        return newUUID;
    } else {
        NSLog(@"保存UUID到Keychain失败，返回临时UUID");
        // 如果保存失败，至少返回生成的UUID（但下次启动可能会不同）
        return newUUID;
    }
}

+ (BOOL)removeDeviceUUID {
    NSDictionary *query = @{
        (__bridge NSString *)kSecClass: (__bridge NSString *)kSecClassGenericPassword,
        (__bridge NSString *)kSecAttrService: kKeychainServiceName,
        (__bridge NSString *)kSecAttrAccount: kKeychainUUIDKey
    };
    
    OSStatus status = SecItemDelete((__bridge CFDictionaryRef)query);
    
    if (status == errSecSuccess) {
        NSLog(@"成功从Keychain中删除UUID");
        return YES;
    } else if (status == errSecItemNotFound) {
        NSLog(@"Keychain中未找到要删除的UUID");
        return YES; // 不存在也算删除成功
    } else {
        NSLog(@"从Keychain删除UUID失败，错误代码: %d", (int)status);
        return NO;
    }
}

+ (BOOL)isUUIDExistsInKeychain {
    NSString *uuid = [self getUUIDFromKeychain];
    return (uuid != nil && uuid.length > 0);
}

#pragma mark - Private Methods

+ (NSString *)generateNewUUID {
    // 首先尝试获取 identifierForVendor
    NSUUID *vendorUUID = [[UIDevice currentDevice] identifierForVendor];
    if (vendorUUID) {
        NSString *vendorUUIDString = [vendorUUID UUIDString];
        NSLog(@"使用 identifierForVendor: %@", vendorUUIDString);
        return vendorUUIDString;
    } else {
        // 如果 identifierForVendor 返回 nil（极少数情况），则生成随机UUID作为备用
        NSLog(@"identifierForVendor 返回 nil，生成随机UUID作为备用");
        NSUUID *randomUUID = [[NSUUID alloc] init];
        return [randomUUID UUIDString];
    }
}

+ (NSString *)getUUIDFromKeychain {
    NSDictionary *query = @{
        (__bridge NSString *)kSecClass: (__bridge NSString *)kSecClassGenericPassword,
        (__bridge NSString *)kSecAttrService: kKeychainServiceName,
        (__bridge NSString *)kSecAttrAccount: kKeychainUUIDKey,
        (__bridge NSString *)kSecReturnData: @YES,
        (__bridge NSString *)kSecMatchLimit: (__bridge NSString *)kSecMatchLimitOne
    };
    
    CFDataRef dataRef = NULL;
    OSStatus status = SecItemCopyMatching((__bridge CFDictionaryRef)query, (CFTypeRef *)&dataRef);
    
    if (status == errSecSuccess && dataRef) {
        NSData *data = (__bridge_transfer NSData *)dataRef;
        NSString *uuid = [[NSString alloc] initWithData:data encoding:NSUTF8StringEncoding];
        return uuid;
    } else if (status == errSecItemNotFound) {
        NSLog(@"Keychain中未找到UUID");
        return nil;
    } else {
        NSLog(@"从Keychain读取UUID失败，错误代码: %d", (int)status);
        return nil;
    }
}

+ (BOOL)saveUUIDToKeychain:(NSString *)uuid {
    if (!uuid || uuid.length == 0) {
        NSLog(@"UUID为空，无法保存到Keychain");
        return NO;
    }
    
    NSData *uuidData = [uuid dataUsingEncoding:NSUTF8StringEncoding];
    
    // 先尝试更新已存在的项
    NSDictionary *query = @{
        (__bridge NSString *)kSecClass: (__bridge NSString *)kSecClassGenericPassword,
        (__bridge NSString *)kSecAttrService: kKeychainServiceName,
        (__bridge NSString *)kSecAttrAccount: kKeychainUUIDKey
    };
    
    NSDictionary *updateAttributes = @{
        (__bridge NSString *)kSecValueData: uuidData
    };
    
    OSStatus updateStatus = SecItemUpdate((__bridge CFDictionaryRef)query, (__bridge CFDictionaryRef)updateAttributes);
    
    if (updateStatus == errSecSuccess) {
        NSLog(@"成功更新Keychain中的UUID");
        return YES;
    } else if (updateStatus == errSecItemNotFound) {
        // 如果项不存在，则添加新项
        NSDictionary *addAttributes = @{
            (__bridge NSString *)kSecClass: (__bridge NSString *)kSecClassGenericPassword,
            (__bridge NSString *)kSecAttrService: kKeychainServiceName,
            (__bridge NSString *)kSecAttrAccount: kKeychainUUIDKey,
            (__bridge NSString *)kSecValueData: uuidData,
            (__bridge NSString *)kSecAttrAccessible: (__bridge NSString *)kSecAttrAccessibleWhenUnlockedThisDeviceOnly
        };
        
        OSStatus addStatus = SecItemAdd((__bridge CFDictionaryRef)addAttributes, NULL);
        
        if (addStatus == errSecSuccess) {
            NSLog(@"成功添加UUID到Keychain");
            return YES;
        } else {
            NSLog(@"添加UUID到Keychain失败，错误代码: %d", (int)addStatus);
            return NO;
        }
    } else {
        NSLog(@"更新Keychain中的UUID失败，错误代码: %d", (int)updateStatus);
        return NO;
    }
}

@end

