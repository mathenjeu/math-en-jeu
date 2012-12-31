#!/bin/bash

# first argument : the tex file (must end in .tex)
# second argument : tmp dir (must be writeable)
# third argument : the directory to store the swf (cannot be the same as tmp dir)
# forth argument : the root of the subtree with all the graphic files

filename="${1}"
tmp_dir=`echo "${2}" | sed 's#/*$##'`
flash_dir=`echo "${3}" | sed 's#/*$##'`
texinputs_dir=`echo "${4}" | sed 's#/*$##'`

#actual_filename = everything strictly after the last '/' in filename
actual_filename=${filename##*/}

#set up a file name prefix.  All intermeditade files produced by the various 
#conversion commands have the form:
#   filename_prefix.xxx
filename_prefix=${tmp_dir}/${actual_filename%.tex}
logfile=${tmp_dir}/mej_convert.log
touch $logfile

echo --- Processing file : $1 --- >> $logfile
#echo 1=$1 2=$2 3=$3 4=$4 PWD=`pwd` >>$logfile 2>&1  #Uncomment when debugging

#convert the file swf, the conversion goes tex--(latex)-->dvi--(dvips)-->eps--(epstopdf)-->pdf--(pdfcrop)-->pdfcrop--(pdf2swf)-->swf
#TEXINPUTS: The double slash (//) means _and all subdirectory_, the trailing colon (:) means also look in the standard search path
TEXINPUTS=${texinputs_dir}//: latex -interaction=batchmode -output-directory=${tmp_dir}/ ${1%.tex} >> $logfile 2>&1
TEXINPUTS=${texinputs_dir}//: dvips -q ${filename_prefix}.dvi -o ${filename_prefix}.eps >> $logfile 2>&1
epstopdf --outfile=${filename_prefix}.pdf ${filename_prefix}.eps >> $logfile 2>&1
pdfcrop ${filename_prefix}.pdf ${filename_prefix}.pdfcrop.pdf >> $logfile 2>&1
#pdf2swf -q -o ${filename_prefix}.swf ${filename_prefix}.pdfcrop.pdf >> $logfile 2>&1
/usr/local/bin/pdf2swf -q -o ${filename_prefix}.swf ${filename_prefix}.pdfcrop.pdf >> $logfile 2>&1

#move the .swf to flash_dir and delete all remaining temporary files
mv -f ${filename_prefix}.swf ${flash_dir}/${actual_filename%.tex}.swf >> $logfile 2>&1
rm -f ${filename_prefix}.* >> $logfile 2>&1

echo --- Done creating swf for file : $1 --- >> $logfile
