<?php
/*******************************************************************************
Fichier : detail-sondage.php
Auteur : Maxime Bégin
Description : afficher le détail d'un sondage sous forme d'un graphie en forme
    de tarte
********************************************************************************
16-06-2006 Maxime Bégin - Version initiale
*******************************************************************************/

require_once("lib/ini.php");

$smarty = new MonSmarty($_SESSION['langage']);
$smarty->assign('cle',$_GET['cle']);
$smarty->cache_lifetime = 0;
$smarty->display('detail_sondage.tpl');



