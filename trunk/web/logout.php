<?php
/*******************************************************************************
Fichier : logout.php
Auteur : Maxime Bégin
Description : Déconnecte un joueur
********************************************************************************
26-11-2006 Maxime Bégin - Version initiale
*******************************************************************************/

require_once("lib/ini.php");

unset($_SESSION["joueur"]);
redirection("index.php",0);

?>