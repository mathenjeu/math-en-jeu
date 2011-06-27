#!/bin/bash

#This script deletes the stats for the time period specified by the first
#argument:
#0 = week stats 
#1 = month stats 
#2 = since creation (you don't want to to this)
#anything else is illegal

#It only deletes the _summary_ stats, the _full_ stats aren't touched
#It needs to be executable, and should be run regularly by a cron job 
#(see crontab.example)

db=<database>
pwd=<password>
user=<mysql user>
host=<mysql host>
period=$1

command="use $db; delete from gamestats_summary_questions where time_period=$period; delete from gamestats_summary_users where time_period=$period;"

#Deleting anything but the weekly or monthly stats doesn't make sense
[ "$period" = "0" ] || [ "$period" = "1" ] || exit

mysql -u$user -h$host -p$pwd -e"$command"
