#!/bin/bash


#first argument : pass the full path of the tex file
#second argument : the directory to store the swf

echo --- Processing file : $1 ---

ffilename=$1
filename=${ffilename%.tex}

#convert the tex file to dvi
latex -interaction=batchmode ${ffilename} >> latex.log

#convert the file to eps
dvips -q -E ${filename}.dvi -o ${filename}.eps >> dvips.log

#convert the file to pdf
epstopdf ${filename}.eps >> epstopdf.log

#convert the file to swf
pdf2swf -q ${filename}.pdf >> pdf2swf.log

echo --- Done creating swf for file : ${ffilename} ---

mv ${filename}.swf $2
