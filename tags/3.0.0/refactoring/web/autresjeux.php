<?php
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
	
	if (!isset($_SESSION['langage'])) {
	  $_SESSION['langage'] = "francais";
	}
	$smarty->cache_lifetime = 0;
	$smarty->display('header.tpl');

	if(isset($_SESSION['css']))
		$smarty->assign('css',$_SESSION['css']);
	
	$smarty->cache_lifetime = 0;
	$smarty->display('menu.tpl');
	
    $smarty->cache_lifetime = -1;
	$smarty->display('autres_jeux.tpl');
	

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

?>