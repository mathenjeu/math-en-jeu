<?php
/*******************************************************************************
Fichier : demo.php
Auteur : Maxime Bégin
Description : affiche le video de démo de mathenjeu
********************************************************************************
29-03-2007 Maxime Bégin - Version initiale
*******************************************************************************/

require_once("lib/ini.php");


main();

function main()
{
  try
  {
	$smarty = new MonSmarty($_SESSION['langage']);;
	global $lang;
	
	if(isset($_SESSION["joueur"]))
	{
		$smarty->assign('connecter',1); 
		$smarty->assign('alias',$_SESSION["joueur"]->reqAlias());
		$smarty->assign('motDePasse',$_SESSION["joueur"]->reqMotDePasse());
		$smarty->assign('acces',$_SESSION["joueur"]->reqCategorie());
	}
	
	$smarty->assign('titre',$lang['titre_index']);
	
	$smarty->cache_lifetime = 0;
	$smarty->display('header.tpl');

	
	$smarty->cache_lifetime = 0;
	$smarty->display('menu.tpl');
	$smarty->display('demo.tpl');

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


