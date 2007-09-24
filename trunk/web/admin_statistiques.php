<?php
require_once("lib/ini.php");

main();

/*******************************************************************************
Fonction : main()
Paramètre : -
Description : permet de gérer les différentes actions à effectuer
*******************************************************************************/
function main()
{
  $smarty = new MonSmarty($_SESSION['langage']);
  global $lang;
  try
  {

	if(isset($_SESSION["joueur"]))
	{
	 	//vérifie que l'utilisateur peut être ici
	 	if($_SESSION["joueur"]->reqCategorie()<5)
	 	{
			redirection('index.php',0);
			return;
		}
		$smarty->assign('connecter',1); 
		$smarty->assign('alias',$_SESSION["joueur"]->reqAlias());
		$smarty->assign('motDePasse',$_SESSION["joueur"]->reqMotDePasse());
		$smarty->assign('acces',$_SESSION["joueur"]->reqCategorie());
	}
	else
	{
		redirection('index.php',0);
		return;
	}
	
	$smarty->assign('titre',$lang['titre_admin']);
    $smarty->cache_lifetime = 0;
    $smarty->display('header.tpl');
    if(isset($_SESSION['css']))
    {
    	$smarty->assign('css',$_SESSION['css']);
    }
	 
	$smarty->cache_lifetime = 0;
	$smarty->display('menu.tpl');
        
    if(!isset($_GET["action"]))
    {
     	formStatistique();
    }
    else
    {
     	if($_GET['action']=="showStatistique")
     	{
	  		formStatistique();
            showStatistique();
	  	}
    }

    $smarty->cache_lifetime = -1;
    $smarty->display("footer.tpl");
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


/*******************************************************************************
Fonction : formStatistique
Paramètre :
Description : afficher les statistiques
*******************************************************************************/
function formStatistique()
{
  $smarty = new MonSmarty($_SESSION['langage']);
  $smarty->cache_lifetime = 0;
  $smarty->display('statistiques.tpl');
}

?>