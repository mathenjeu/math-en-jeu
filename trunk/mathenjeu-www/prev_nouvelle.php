<?php
/*******************************************************************************
Fichier : prev_nouvelle.php
Auteur : Maxime Bégin
Description : fenêtre pop-up javacript qui permet de visualisé une nouvelle
    avant de la poster.
********************************************************************************
23-06-2006 Maxime Bégin - ajout d'image pour les nouvelles
22-06-2006 Maxime Bégin - Version initiale
*******************************************************************************/

require_once("lib/ini.php");

$smarty = new MonSmarty($_SESSION['langage']);
$arr[0]['titre'] = stripslashes($_GET['titre']);
$arr[0]['date'] = $_GET['date'];
echo $_GET['nouvelle'];
$arr[0]['nouvelle'] = wordwrap(stripslashes($_GET['nouvelle']),70,"<br>");
$arr[0]['image'] = $_GET['image'];

$smarty->assign('nouvelle',$arr);
echo '<html><link rel="stylesheet" type="text/css" href="' . TEMPLATE . 'mathenjeu.css' . '"><body>';
$smarty->display('nouvelles.tpl');
echo '</body></html>';

