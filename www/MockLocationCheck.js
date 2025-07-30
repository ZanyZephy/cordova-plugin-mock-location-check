var exec = require('cordova/exec');

var MockLocationCheck = {
    // 检查是否启用了模拟位置
    isMockLocationEnabled: function(success, error) {
        exec(success, error, 'MockLocationCheck', 'isMockLocationEnabled', []);
    }
};

module.exports = MockLocationCheck;
