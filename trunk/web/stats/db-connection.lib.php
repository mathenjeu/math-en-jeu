<?php
/******************************************************************************
  daDrill, an offline BibTeX library browser
  Copyright (C) 2005  Sylvain Hallé
  
  This program is free software; you can redistribute it and/or
  modify it under the terms of the GNU General Public License
  as published by the Free Software Foundation; either version 2
  of the License, or (at your option) any later version.
  
  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.
  
  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, 
  MA  02110-1301, USA.
******************************************************************************/

// Imports
require_once("config.inc.php");

/**
 * Opens connection to the database.  This is a wrapper for the two alternate
 * methods (mysql and mysqli) provided by PHP to connect to a MySQL database
 * @return A valid handle to the database, FALSE if failure
 */
function db_connect()
{
  global $config;
  $db = false;
  switch($config["ddDBMode"])
  {
    case "mysqli":
    {
      $db = mysqli_connect($config["ddDBHost"], $config["ddDBUser"],
        $config["ddDBPassword"], $config["ddDBName"]);
      break;
    }
    case "mysql":
    {
      $db = mysql_connect($config["ddDBHost"], $config["ddDBUser"],
        $config["ddDBPassword"]);
      if ($db)
        mysql_select_db($config["ddDBName"], $db);
    }
  }
  return $db;
}

/**
 * Performs a query on the database.  This is a wrapper for the two alternate
 * methods (mysql and mysqli) provided by PHP to connect to a MySQL database
 * @param $db Valid handle to the database
 * @param $query Query string to execute
 * @return A valid handle to the result set, FALSE if failure
 */
function db_query($db, $query)
{
  global $config;
  $result = false;
  switch($config["ddDBMode"])
  {
    case "mysqli":
    {
      $result = mysqli_query($db, $query);
      break;
    }
    case "mysql":
    {
      $result = mysql_query($query, $db);
      break;
    }
  }
  return $result;
}

/**
 * Shows the error message.  This is a wrapper for the two alternate
 * methods (mysql and mysqli) provided by PHP to connect to a MySQL database
 * @param $db Valid handle to the database
 * @return The error message of the last operation executed by the server
 */
function db_error($db)
{
  global $config;
  switch($config["ddDBMode"])
  {
    case "mysqli":
    {
      return mysqli_error($db);
      break;
    }
    case "mysql":
    {
      return mysql_error($db);
      break;
    }
  }
  return false;
}

/**
 * Counts the rows in a result set.  This is a wrapper for the two possible 
 * modes of database connection in PHP (mysqli and mysql).
 * @param $result Handle to a valid result set
 * @return The number of rows in the result set
 */
function db_num_rows($result)
{
  global $config;
  switch($config["ddDBMode"])
  {
    case "mysqli":
    {
      return mysqli_num_rows($result);
      break;
    }
    case "mysql":
    {
      return mysql_num_rows($result);
      break;
    }
  }
  return false;
}

/**
 * Fetches the next row of a result set and puts the result into an associative
 * array. This is a wrapper for the two possible modes of database connection in
 * PHP (mysqli and mysql).
 * @param $result Valid handle to a result set
 */
function db_fetch_assoc_array($result)
{
  global $config;
  $out = false;
  switch($config["ddDBMode"])
  {
    case "mysqli":
    {
      $out = mysqli_fetch_array($result, MYSQLI_ASSOC);
      break;
    }
    case "mysql":
    {
      $out = mysql_fetch_array($result, MYSQL_ASSOC);
      break;
    }
  }
  return $out;
}

/**
 * Close database connection. This is a wrapper for the two possible modes of 
 * database connection in PHP (mysqli and mysql).
 * @param $db Handle to valid opened database
 */
function db_close($db)
{
  global $config;
  switch($config["ddDBMode"])
  {
    case "mysqli":
    {
      return mysqli_close($db);
      break;
    }
    case "mysql":
    {
      return mysql_close($db);
      break;
    }
  }
  return false;
}
?>
