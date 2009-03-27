#!/bin/bash

# first argument : pass the full path of the tex file
# second argument : the directory to store the swf
# third argument : pdf for pdf convert, jpeg for jpeg conversion

olddir=`pwd`

file_full_path=$1
tmp_dir=${file_full_path%/*}/
if [ ${file_full_path}/ == ${tmp_dir} ] ; then 
    tmp_dir=./
fi

filename=${file_full_path##/*/}
filename_prefix=${filename%.tex}

cd $tmp_dir

rm -f mej_convert.log
touch mej_convert.log

echo Changed dir from $olddir to `pwd` >> mej_convert.log
echo --- Processing file : $1 --- >> mej_convert.log

#convert the tex file to dvi
#latex doesn't care if the trailing .tex is there or not.
latex -interaction=batchmode ${filename_prefix} 2>> mej_convert.log

#convert the file to eps
dvips -q -E ${filename_prefix}.dvi -o ${filename_prefix}.eps 2>> mej_convert.log

#convert the file to swf
if [ "$3" == "pdf" ]
then
    #conversion goes eps->pdf->swf
    epstopdf ${filename_prefix}.eps 2>> mej_convert.log
    pdf2swf -q ${filename_prefix}.pdf 2>>mej_convert.log
else
    #convertion goes eps->jpg->swf
    convert -density 300x300 -resize 150\% ${filename_prefix}.eps ${filename_prefix}.jpeg
    jpeg2swf ${filename_prefix}.jpeg -o ${filename_prefix}.swf
fi

mv ${filename_prefix}.swf $2
rm -f ${filename_prefix}.*

echo --- Done creating swf for file : $1 --- >> mej_convert.log
echo Changing back to $olddir >> mej_convert.log
cd $olddir
