#!/bin/bash

#convert all the tex file in the current directory to swf file
#first argument is the directory to move the swf to

for i in $( ls | grep .tex);
do
	tex2swf.sh $i $1
done

