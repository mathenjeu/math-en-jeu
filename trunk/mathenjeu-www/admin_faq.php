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
  $smarty=new MonSmarty();
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
     	formFaq();
    }
    else
    {
        $action = $_GET["action"];
        switch($action)
        {
	  		case "faqMove":
	  			faqMove($_GET['numero']);
	  			formFaq();
	  			break;
	  		case "ajoutFaq":
	  			$smarty->assign('action',"ajout");
	  			$smarty->display('ajout_mod_faq.tpl');
	  			break;
	  		case "insertFaq":
	  			insertFaq();
	  			break;
	  		case "deleteFaq":
	  			deleteFaq($_GET['cleFaq']);
	  			break;
	  		case "detailFaq":
				formModfierFaq($_GET['cleFaq']);
	  			break;
	  		case "doUpdateFaq":
	  			doModifierFaq($_GET['cleFaq']);
	  			break;
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
Fonction : formModfierFaq
Paramètre : $cle : la clé de la faq à modifier
Description : on affiche le formulaire de modification avec les données de la 
	faq corespondant à la clé
*******************************************************************************/
function formModfierFaq($cle)
{
  $smarty = new MonSmarty();
  $faq=new FAQ($_SESSION['mysqli']);
  if(!$faq->chargerMySQL($cle))
  {
  	formFaq();
  	return;
  }
  $smarty->assign('cle',$faq->reqCle());
  $smarty->assign('question',$faq->reqQuestion());
  $smarty->assign('reponse',$faq->reqReponse());
  $smarty->assign('action',"modifier");
  $smarty->cache_lifetime = 0;
  $smarty->display('ajout_mod_faq.tpl');
}

/*******************************************************************************
Fonction : doModifierFaq
Paramètre : $cle : la clé de la faq à modifier
Description : on modfie la faq corespondant à la clé, si une des données 
	est invalide; on affiche le formulaire avec un message d'erreur
*******************************************************************************/
function doModifierFaq($cle)
{
  $smarty = new MonSmarty();
  if($_POST['question']=="")
  {
  	$smarty->assign('erreur',$lang['erreur_faq_question']);
  	$smarty->cache_lifetime = 0;
  	$smarty->display('ajout_mod_faq.tpl');
  }
  elseif($_POST['reponse']=="")
  {
  	$smarty->assign('erreur',$lang['erreur_faq_reponse']);
  	$smarty->cache_lifetime = 0;
  	$smarty->display('ajout_mod_faq.tpl');
  }
  else
  {
    $faq=new FAQ($_SESSION['mysqli']);
    $faq->chargerMySQL($cle);
    $faq->asgQuestion(addslashes($_POST['question']));
    $faq->asgReponse(addslashes($_POST['reponse']));
    $faq->miseAJourMySQL();
    formFaq();
  }
}

/*******************************************************************************
Fonction : deleteFaq
Paramètre : $cle : la clé de la faq à supprimer
Description : on supprimer la faq corespondant à la clé
*******************************************************************************/
function deleteFaq($cle)
{
  $faq = new FAQ($_SESSION['mysqli']);
  $faq->chargerMySQL($cle);
  $faq->deleteMySQL();
  formFaq();
}

/*******************************************************************************
Fonction : insertFaq
Paramètre : 
Description : insérer une nouvelle faq dans la table, si une des données 
	est invalide; on affiche le formulaire avec un message d'erreur
*******************************************************************************/
function insertFaq()
{
  $smarty = new MonSmarty();
  if($_POST['question']=="")
  {
  	$smarty->assign('erreur',$lang['erreur_faq_question']);
  	$smarty->display('ajout_mod_faq.tpl');
  }
  elseif($_POST['reponse']=="")
  {
  	$smarty->assign('erreur',$lang['erreur_faq_reponse']);
  	$smarty->display('ajout_mod_faq.tpl');
  }
  else
  {
    $faq = new FAQ($_SESSION['mysqli']);
    $faq->asgQuestion(addslashes($_POST['question']));
    $faq->asgReponse(addslashes($_POST['reponse']));
    $faq->insertionMySQL();
    formFaq();
  }
 
}

/*******************************************************************************
Fonction : faqMove
Paramètre : $numero(pas la clé) de la faq à monter ou descendre
Description : fonction qui change l'ordre d'une faq.
	si le numéro est négatif cela signifie que la faq descend dans la liste et
	on augmente son numéro de 1 et on diminue celui de la faq juste en dessous.
	On effectue l'opération inverse si le numéro est positif. 
*******************************************************************************/
function faqMove($numero)
{
  $faqs = new FAQs($_SESSION['mysqli']);
  if($numero<0)
  	$faqs->chargerMySQLNumero(array(abs($numero),abs($numero)+1));
  else
  	$faqs->chargerMySQLNumero(array($numero-1,$numero));
  
  if($faqs->reqNbFaq()<2)
  	return;
  	
  $faq=$faqs->reqFaq(1);
  $faq->asgNumero($faq->reqNumero()+1);
  $faq->miseAJourMySQL();
  $faq=$faqs->reqFaq(2);
  $faq->asgNumero($faq->reqNumero()-1);
  $faq->miseAJourMySQL();
  
  
}

/*******************************************************************************
Fonction : formFaq
Paramètre : 
Description : charger les faqs et afficher la liste avec possibilité de modifier,
	supprimer et de changer leur ordre.
*******************************************************************************/
function formFaq()
{
  $smarty = new MonSmarty();
  
  $faqs = new FAQs($_SESSION['mysqli']);
  $faqs->chargerMySQL(0);
  
  $nb=$faqs->reqNbFaq();
  for($i=0;$i<$nb;$i++)
  {
    $faq = $faqs->reqFaq($i+1);
    $arr[$i]['cle'] = $faq->reqCle();
    
    $arr[$i]['question'] = stripslashes($faq->reqQuestion());
    	
    $arr[$i]['reponse'] = htmlentities(substr(stripslashes($faq->reqReponse()),0,120));
    if(strlen($faq->reqReponse())>120)
    	$arr[$i]['reponse'] .= " ...";
    	
    $arr[$i]['numero'] = $faq->reqNumero();
  }
  $smarty->assign('nb_faq',$nb);
  if($nb>0)
  	$smarty->assign('faqs',$arr);
  $smarty->cache_lifetime = 0;
  $smarty->display('liste_faqs.tpl');
  
}


?>