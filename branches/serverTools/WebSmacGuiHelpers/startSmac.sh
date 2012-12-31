#!/bin/bash

# first argument : the name of user dir
tex_dir=$1
user_dir=$2

mkdir $user_dir
mkdir $user_dir/flash
mkdir $user_dir/tmp



java -jar WebSmac.jar  $tex_dir $user_dir

echo --- Done creating dir  
