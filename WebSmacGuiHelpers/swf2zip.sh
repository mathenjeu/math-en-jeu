#!/bin/bash

# first argument : the name of zip file
# second argument : tmp dir (must be writeable)
# third argument : the directory to store the swf (cannot be the same as tmp dir)
filename="${1}"
tmp_dir="${2}"
flash_dir="${3}"

cp smac.log ./$flash_dir

logfile=${tmp_dir}/mej_convert.log
touch $logfile
echo --- zipping directory : $1 --- >> $logfile
zip -r ${filename} ${flash_dir}
echo --- Done creating zip ${filename}: $1 --- >> $logfile