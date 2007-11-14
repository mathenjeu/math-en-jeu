<?php
require_once("lib/ini.php");

global $lang;

$_SESSION['language_choose'] = 1;
$smarty = new MonSmarty('fr');

$smarty->cache_lifetime = 0;
$smarty->display('header.tpl');

$smarty->cache_lifetime = -1;
$smarty->display('choose_lang.tpl');

$smarty->cache_lifetime = -1;
$smarty->display('footer.tpl');

?>