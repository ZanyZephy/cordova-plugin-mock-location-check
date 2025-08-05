var exec = require('cordova/exec');


exports.isDeveloperModeEnabled = function (success, error) {
    exec(success, error, 'SystemCheckMockLocationPlugin', 'isDeveloperModeEnabled', []);
};

// exports.isMockLocationEnabled = function (success, error) {
//     exec(success, error, 'SystemCheckMockLocationPlugin', 'isMockLocationEnabled', []);
// };
