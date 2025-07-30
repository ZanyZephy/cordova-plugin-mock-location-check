var exec = require('cordova/exec');

var MockLocationCheck = {
    // 检查是否启用了模拟位置
    isMockLocationEnabled: function(success, error) {
        exec(success, error, 'MockLocationCheck', 'isMockLocationEnabled', []);
    },
    
    // 获取当前使用的模拟位置应用
    getMockLocationApp: function(success, error) {
        exec(success, error, 'MockLocationCheck', 'getMockLocationApp', []);
    }
};

module.exports = MockLocationCheck;
