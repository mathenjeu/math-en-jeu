<?php



require_once("lib/ini.php");

main();

function main()
{
  try
  {
	$smarty = new MonSmarty;
	global $lang;
	
	$smarty->assign('titre',$lang['titre_nouvelle']);
	$smarty->cache_lifetime = 0;
	$smarty->display('header.tpl');

	if(isset($_SESSION['css']))
		$smarty->assign('css',$_SESSION['css']);
	
	if(isset($_SESSION["joueur"]))
	{
	 	$smarty->assign('connecter',1);
	 	$smarty->assign('alias',$_SESSION["joueur"]->reqAlias());
	 	$smarty->assign('motDePasse',$_SESSION["joueur"]->reqMotDePasse());
	 	$smarty->assign('acces',$_SESSION["joueur"]->reqCategorie());
	}
	
	$smarty->cache_lifetime = 0;
	$smarty->display('menu.tpl');

	
	//on v�rifie si on a une copie valide dans la cache
	//sinon on va chercher les informations dans la base de donn�es
	if(!$smarty->is_cached("nouvelle.tpl"))
	{
		//on charge les nouvelles
	    $nouvelles = new Nouvelles($_SESSION["mysqli"]);
	    $nouvelles->chargerMySQL(MAX_NB_NOUVELLES,array(0,1),getCleLangue($_SESSION['langage']));
	    
	    for($i=0;$i<$nouvelles->reqNbNouvelle();$i++)
	    {
	      $nouvelle = $nouvelles->reqNouvelle($i+1);
	      $arr[$i]['titre'] = $nouvelle->reqTitre();
	      $arr[$i]['date'] = convertirDateEnString($nouvelle->reqDate());
	      $arr[$i]['nouvelle'] = $nouvelle->reqNouvelle();
	      $arr[$i]['image'] = $nouvelle->reqImage();
	    }
		if($nouvelles->reqNbNouvelle()>0)
		{
	    	$smarty->assign('nouvelle',$arr);
	    }
	}
	
    $smarty->cache_lifetime = -1;
    $smarty->display('nouvelles.tpl');
    //on inclus ici le fichier pemettant d'afficher les sondages
    include("sondage.php");
    
    $smarty->cache_lifetime = -1;
    $smarty->display('footer.tpl');
        
  }
  catch(MyException $e)
  {
  	echo $e->exception_dump();  
  }
}

/*******************************************************************************
Fonction : afficherNouvelles
Param�tre : $nouvelles : les nouvelles � afficher
Description : affiche les nouvelles contenues dans l'objet $nouvelles
*******************************************************************************/
/*
function afficherNouvelles($nouvelles)
{

    $smarty = new MonSmarty;
    for($i=0;$i<$nouvelles->reqNbNouvelle();$i++)
    {
      $nouvelle = $nouvelles->reqNouvelle($i+1);
      $arr[$i]['titre'] = $nouvelle->reqTitre();
      $arr[$i]['date'] = convertirDateEnString($nouvelle->reqDate());
      $arr[$i]['nouvelle'] = $nouvelle->reqNouvelle();
      $arr[$i]['image'] = $nouvelle->reqImage();
    }
	 if($nouvelles->reqNbNouvelle()>0){
    	$smarty->assign('nouvelle',$arr);
    }
    $smarty->display('nouvelles.tpl');

}
*/

?>