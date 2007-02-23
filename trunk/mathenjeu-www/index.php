<?php
/*******************************************************************************
Fichier : index.php
Auteur : Maxime Bégin
Description : affiche l'index
********************************************************************************
10-06-2006 Maxime Bégin - Version initiale
*******************************************************************************/

require_once("lib/ini.php");

$fp=fopen("compteur.txt","a+"); //OUVRE LE FICHIER compteur.txt
$num=fgets($fp,4096); // RECUPERE LE CONTENUE DU COMPTEUR
fclose($fp); // FERME LE FICHIER
$hits=$num - -1;  // TRAITEMENT
$fp=fopen("compteur.txt","w");  // OUVRE DE NOUVEAU LE FICHIER
fputs($fp,$hits); // MET LA NOUVELLE VALEUR
fclose($fp);  // FERME LE FICHIER


main();

function main()
{
  try
  {
	$smarty = new MonSmarty;
	global $lang;
	
	if(isset($_SESSION["joueur"]))
	{
		$smarty->assign('connecter',1); 
		$smarty->assign('alias',$_SESSION["joueur"]->reqAlias());
		$smarty->assign('motDePasse',$_SESSION["joueur"]->reqMotDePasse());
		$smarty->assign('acces',$_SESSION["joueur"]->reqAcces());
	}
	
	$smarty->assign('titre',$lang['titre_index']);
	
	$smarty->cache_lifetime = 0;
	$smarty->display('header.tpl');

	if(isset($_SESSION['css']))
		$smarty->assign('css',$_SESSION['css']);
	
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


