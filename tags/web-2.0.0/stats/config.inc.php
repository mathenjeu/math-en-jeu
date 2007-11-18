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

/**
 * MySQL server configuration
 */
$config["ddDBHost"]     = "localhost";  // Host name or IP address for MySQL
                                        // server
$config["ddDBUser"]     = "mathenjeu";  // User name in the server
$config["ddDBPassword"] = "smac/2pi";   // User password
$config["ddDBName"]     = "smac";       // Database name
$config["ddDBMode"]     = "mysqli";     // Database connection mode ("mysql" or
                                        // "mysqli" (PHP >5)
?>
