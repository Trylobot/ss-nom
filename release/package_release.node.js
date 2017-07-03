var mod_info = require('./mod_info.json');
var version = mod_info['version'];
var child_process = require('child_process');
var sys = require('sys');
function puts(error, stdout, stderr) { sys.puts(stdout); }

child_process.exec( "RMDIR /S /Q ss-nom", puts ).on('exit', function() {
child_process.exec( "MKDIR ss-nom", puts ) }).on('exit', function() {;
child_process.exec( "XCOPY ../data ss-nom", puts ) }).on('exit', function() {;
child_process.exec( "7z a ss-nom-"+version+".zip ss-nom", puts ) });

