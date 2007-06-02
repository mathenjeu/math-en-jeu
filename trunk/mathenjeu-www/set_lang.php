<?php
require_once("lib/ini.php");
$supported_lang = array('francais','english');

if (isset($_GET['lang']) && in_array($_GET['lang'],$supported_lang)) {
  $_SESSION['langage'] = $_GET['lang'];
}

redirection("index.php",0);
?>