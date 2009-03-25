#!/bin/bash

# first argument : pass the full path of the tex file
# second argument : the directory to store the swf
rm -rf convert.log

touch convert.log

echo --- Processing file : $1 --- >> convert.log

filefullpath=$1
filefullname=${filefullpath##/*/}
filename=${filefullname%.tex}

#convert the tex file to dvi
latex -interaction=batchmode ${filefullpath} 2>> convert.log
#latex  ${filefullpath} >> convert.log 2>&1

#convert the file to eps
dvips -q -E ${filename}.dvi -o ${filename}.eps 2>> convert.log

#convert the file to pdf
#epstopdf ${filename}.eps 2>> convert.log

#convert the file to swf
#pdf2swf -q ${filename}.pdf 2>>convert.log

#mv ${filename}.swf $2

echo --- Done creating swf for file : $1 --- >> convert.log
