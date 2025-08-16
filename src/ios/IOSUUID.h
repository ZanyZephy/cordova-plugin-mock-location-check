#import <Cordova/CDV.h>

@interface IOSUUID : CDVPlugin

// 对外接口
- (void)getUUID:(CDVInvokedUrlCommand*)command;

@end
