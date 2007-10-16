<?php
/*******************************************************************************
Fichier : index.php
Auteur : Maxime B�gin
Description : affiche l'index
********************************************************************************
10-06-2006 Maxime B�gin - Version initiale
*******************************************************************************/

require_once("lib/ini.php");


main();

function main()
{
  try
  {
	$smarty = new MonSmarty($_SESSION['langage']);
	global $lang;
	
	if(isset($_SESSION["joueur"]))
	{
		$smarty->assign('connecter',1); 
		$smarty->assign('alias',$_SESSION["joueur"]->reqAlias());
		$smarty->assign('motDePasse',$_SESSION["joueur"]->reqMotDePasse());
		$smarty->assign('acces',$_SESSION["joueur"]->reqCategorie());
	}
	
	$smarty->assign('titre',$lang['titre_index']);
	
	if (!isset($_SESSION['langage'])) {
	  $_SESSION['langage'] = "fr";
	}
	$smarty->cache_lifetime = 0;
	$smarty->display('header.tpl');

	if(isset($_SESSION['css'])) {
		$smarty->assign('css',$_SESSION['css']);
	}
	
	$smarty->cache_lifetime = 0;
	$smarty->display('menu.tpl');
	
	if(isset($_GET['action']))
	{
	    $action=$_GET['action'];
		if($action=="couleur")
		{
			$_SESSION['css']=$_POST['css'];
			redirection('index.php',0);
			return;
		}
	}
	else
	{
	  $smarty->cache_lifetime = -1;
	  $smarty->display('index.tpl');
	}
	//on inclus ici le fichier pemettant d'afficher les sondages
    include("sondage.php");
    $smarty->cache_lifetime = -1;
	$smarty->display('footer.tpl');

  }
  catch(SQLException $e)
  {
    echo $e->exception_dump();
  }
  catch(MyException $e)
  {
  	echo $e->exception_dump();  
  }
}


