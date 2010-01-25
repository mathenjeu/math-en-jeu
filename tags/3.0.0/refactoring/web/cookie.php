<?php
/*******************************************************************************
Fichier : cookie.php
Auteur : Maxime Bégin
Description : affiche le message qui indique que les cookies doivent être activées
********************************************************************************
10-06-2006 Maxime Bégin - Version initiale
*******************************************************************************/

require_once("lib/ini.php");


main();

function main()
{
  try
  {
	$smarty = new MonSmarty($_SESSION['langage']);;
	global $lang;
	$smarty->assign('titre',$lang['titre_index']);
	
	$smarty->cache_lifetime = 0;
	$smarty->display('header.tpl');

	if(isset($_SESSION['css']))
		$smarty->assign('css',$_SESSION['css']);
	
	$smarty->cache_lifetime = 0;
	$smarty->display('menu.tpl');
	
	$smarty->cache_lifetime = -1;
	$smarty->display('cookie.tpl');
	
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