#!/bin/bash


#first argument : pass the full path of the tex file
#second argument : the directory to store the swf
#third argument : pdf for pdf convert, jpeg for jpeg conversion

#move the file to the temp directory
cp -f $1 /tmp
cd /tmp

#rm -rf convert.log

#touch ./convert.log

echo --- Processing file : $1 ---

ffilename=$1
filename=${ffilename%.tex}


#convert the tex file to dvi
latex -interaction=batchmode ${ffilename}

#convert the file to eps
dvips -q -E ${filename}.dvi -o ${filename}.eps

#convert the file to pdf
if [ "$3" == "pdf" ]
then
	#convert the file to pdf then to swf
	epstopdf ${filename}.eps
	pdf2swf -q ${filename}.pdf
else
	#convert the eps to a jpeg file, then convert to swf
	convert -density 300x300 -resize 150% ${filename}.eps ${filename}.jpeg
	jpeg2swf ${filename}.jpeg -o ${filename}.swf
fi

mv ${filename}.swf $2

echo --- Done creating swf for file : ${ffilename} ---


