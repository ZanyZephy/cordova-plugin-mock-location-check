var exec = require('cordova/exec');


exports.isDeveloperModeEnabled = function (success, error) {
    exec(success, error, 'SystemCheckMockLocationPlugin', 'isDeveloperModeEnabled', []);
};

exports.getUUID = function (success, error) {
    // exec(success, error, 'SystemCheckMockLocationPlugin', 'getUUID', []);
    var serviceName = device.platform === 'iOS' ? 'IOSUUID' : 'SystemCheckMockLocationPlugin';
    exec(success, error, serviceName, 'getUUID', []);
};
