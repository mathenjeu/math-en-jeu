<?php
/*******************************************************************************
Fichier : faq.php
Auteur : Maxime B�gin
Description : fichier pour afficher la foire au questions (FAQ)
********************************************************************************
24-11-2006 Maxime B�gin - Version initiale
*******************************************************************************/


require_once("lib/ini.php");

main();

function main()
{
 	try
  	{
  	 	$smarty = new MonSmarty($_SESSION['langage']);
		global $lang;
	
		$smarty->assign('titre',$lang['titre_faq']);
		$smarty->cache_lifetime = 0;
		$smarty->display('header.tpl');
  	 	
  	 	if(isset($_SESSION['css']))
  	 	{
			$smarty->assign('css',$_SESSION['css']);
		}
	
		if(isset($_SESSION["joueur"]))
		{
	 		$smarty->assign('connecter',1);
	 		$smarty->assign('alias',$_SESSION["joueur"]->reqAlias());
	 		$smarty->assign('motDePasse',$_SESSION["joueur"]->reqMotDePasse());
	 		$smarty->assign('acces',$_SESSION["joueur"]->reqCategorie());
	 	}
	 	$smarty->cache_lifetime = 0;
	 	$smarty->display('menu.tpl');
	 	
	 	afficherFaq();
	 	
	 	$smarty->cache_lifetime = -1;
	 	$smarty->display('footer.tpl');
		   	 
  	}
  	catch(MyException $e)
  	{
  		echo $e->exception_dump();  
  	}
  	 
}


/*******************************************************************************
Fonction : afficherFaq()
Param�tre :
Description :
    - on charge les informations � propos de la FAQ et on l'affiche
*******************************************************************************/
function afficherFaq()
{
  	$smarty = new MonSmarty($_SESSION['langage']);
  
  	//on v�rifie si on a une copie valide dans la cache
	//sinon on va chercher les informations dans la base de donn�es
	if(!$smarty->is_cached("faq.tpl"))
	{
  		$faqs = new FAQs($_SESSION['mysqli']);
  		$faqs->chargerMySQL(0,getCleLangue($_SESSION['langage']));
	  	$nb = $faqs->reqNbFaq();
	  	for($i=0;$i<$nb;$i++)
	  	{
	    	$faq = $faqs->reqFaq($i+1);
	    	$arr[$i]['question'] = stripslashes($faq->reqQuestion());
	    	$arr[$i]['reponse'] = stripslashes($faq->reqReponse());    
	 	}
	  	if($nb>0){
	  		$smarty->assign('faqs',$arr);
	  	}
	}
  	$smarty->cache_lifetime = -1;
  	$smarty->display('faq.tpl');  
}


?>