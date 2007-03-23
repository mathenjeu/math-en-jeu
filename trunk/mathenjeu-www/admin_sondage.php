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
     	formSondage();
    }
	else
	{
	 	$action = $_GET["action"];
        switch($action)
        {
			case "ajoutSondage":
                if(!isset($_GET["nbChoix"]))
                    formAjoutSondage(4,"");
                else
                    formAjoutSondage($_GET["nbChoix"],"");
                break;
            case "modificationSondage":
             	 formModifierSondage($_GET['cleSondage'],"");
             	 break;
            case "doModificationSondage":
             	modificationSondage();
             	break;
            case "insertSondage":
                insertSondage($_GET["nbChoix"]);
                break;
            case "deleteSondage":
                supprimerSondage($_GET["cleSondage"]);
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
Fonction : formSondage()
Paramètre : aucun
Description : afficher tous les sondages avec le choix de supprimer celui-ci
*******************************************************************************/
function formSondage()
{
  	 global $lang;
    $smarty=new MonSmarty();
    //on affiche les sondages
    $sql = "select cleSondage from sondage order by cleSondage desc";
    $resultSondage = $_SESSION["mysqli"]->query($sql);
    $nb = $resultSondage->num_rows;
    
    for($i=0;$i<$nb;$i++)
    {
        $row=$resultSondage->fetch_object();

        $sondage = new Sondage($_SESSION["mysqli"]);
        $sondage->chargerSondageMySQL($row->cleSondage);
        $arr[$i]['date'] = $sondage->reqDate();
        $arr[$i]['titre'] = $sondage->reqTitre();
        $arr[$i]['total'] = $sondage->reqTotal();
        $arr[$i]['cle'] = $sondage->reqCleSondage();
        $arr[$i]['destinataire']= $lang['type_destinataire'][$sondage->reqDestinataire()];
    }
    
    if($nb>0)
	{
    	$smarty->assign('sondages',$arr);
    }
    $smarty->cache_lifetime = 0;
    $smarty->display('liste_sondage.tpl');
    
}

/*******************************************************************************
Fonction : formAjoutsondage($nbChoix)
Paramètre :
    $nbChoix : le nombre de choix pour le sondage
    $erreur : un message d'erreur s'il y en a
Description : afficher le formulaire pour ajouter un sondage
*******************************************************************************/
function formAjoutsondage($nbChoix,$erreur)
{

    if($nbChoix<2){
        $nbChoix=2;
    }
    elseif($nbChoix>5){
        $nbChoix=5;
    }
        
    $smarty=new MonSmarty();
    $smarty->assign('erreur',$erreur);
    $smarty->assign('action','ajout');
    $smarty->assign('nbChoix',$nbChoix);
    $smarty->assign('dateLongue',convertirDateEnString(date("Y-m-d")));
    
    if(isset($_POST['sondage']))
    {
        $smarty->assign('sondage',stripslashes($_POST['sondage']));
        $smarty->assign('selected' . $_POST['destinataire'],'selected');
    }
    
    for($i=0;$i<$nbChoix;$i++)
    {
      if(!isset($_POST["choix" . ($i+1)])){
        $arr[$i]= "";
      }
      else
      {
        $arr[$i]=stripslashes($_POST["choix" . ($i+1)]);
      }
    }
    
    $smarty->assign("reponse",$arr);
    $smarty->cache_lifetime = 0;
    $smarty->display("ajout_mod_sondage.tpl");

}



/*******************************************************************************
Fonction : insertSondage($nbChoix)
Paramètre :
    - $nbChoix : le nombre de choix que comporte le sondage
Description : ajoute un sondage à la base de données
*******************************************************************************/
function insertSondage($nbChoix)
{
    global $lang;
    $sondage=new Sondage($_SESSION["mysqli"]);
    if($_POST["sondage"]=="")
    {
        formAjoutSondage($nbChoix,$lang['sondage_titre_invalide']);
    }
    else
    {
        $sondage->asgTitre(addslashes($_POST["sondage"]));
        $sondage->asgDate(date("Y-m-d"));
        $sondage->asgDestinataire($_POST['destinataire']);

        for($i=1;$i<=$nbChoix;$i++)
        {
            $reponse=new ReponseSondage($_SESSION["mysqli"]);
            if(($_POST["choix".$i])=="")
            {
                formAjoutSondage($nbChoix,$lang['sondage_texte_reponse_invalide']);
                return;
            }
            $reponse->asgReponse(addslashes($_POST["choix".$i]));
            $sondage->ajoutReponse($reponse);
        }
        $sondage->insertionSondageMySQL();
        formSondage();
    }
}

/*******************************************************************************
Fonction : modificationSondage
Paramètre :
Description : enregistrer les informations mise à jour à propos du sondage
*******************************************************************************/
function modificationSondage()
{
     
 	  global $lang;
 	  //on vérifie que les données sont valide
     if($_POST["sondage"]=="")
     {
        formModifierSondage($_GET['cleSondage'],$lang['sondage_titre_invalide']);
        return;
     }
 	  for($i=1;$i<=$_GET['nbChoix'];$i++)
 	  {
 	      //si un des choix est vide on affiche un message d'erreur
 	      if(($_POST["choix".$i])=="")
         {
             formModifierSondage($_GET['cleSondage'],$lang['sondage_texte_reponse_invalide']);
             return;
         }
 	  }
 	  
 	  $sondage=new Sondage($_SESSION["mysqli"]);
 	  $sondage->chargerSondageMySQL($_GET['cleSondage']);
     $sondage->asgTitre(addslashes($_POST["sondage"]));
     $sondage->asgDestinataire(addslashes($_POST['destinataire']));
	  	  
	  //on boucle pour le nombre de choix choisi par l'utilisateur
     for($i=1;$i<=$_GET['nbChoix'];$i++)
     {
         //si l'utilisateur a choisi d'ajouter des choix il faut les ajouter dans la table
         if($i>$sondage->reqNbReponse())
         {
         	$reponse=new ReponseSondage($_SESSION["mysqli"]);
         	$reponse->asgReponse(addslashes($_POST["choix".$i]));
         	$reponse->insertionMySQL($_GET['cleSondage']);
         	$sondage->ajoutReponse($reponse);
         }
         //on modifie seulement les réponses
         else{
         	$sondage->reqReponse($i)->asgReponse(addslashes($_POST["choix".$i]));
         }
     }
     
     //on doit maintenant vérifier si l'utilisateur a choisi d'enlever des choix
     if($sondage->reqNbReponse()>$_GET['nbChoix'])
     {        		
     		for($i=$sondage->reqNbReponse();$i>$_GET['nbChoix'];$i--)
     		{
     			$sondage->reqReponse($i)->deleteMySQL();
     			$sondage->enleverReponse();
     		}
     }
     $sondage->miseAJourMySQL();
     formSondage();
    
}

/*******************************************************************************
Fonction :  
Paramètre :
Description : préparé et afficher le formulaire pour la modification d'un sondsge
*******************************************************************************/
function formModifierSondage($cle,$erreur)
{
	$smarty=new MonSmarty();
	$smarty->assign('erreur',$erreur);
  	$smarty->assign('action','modifier');
	
	//on vérifie si on veut seulement ajouter un choix
	if(isset($_POST['sondage']))
	{
		//on s'assure que le nombre de choix est valide
		if($_GET['nbChoix']<2)
		{
	   		$nbChoix=2;
	   	}
	   	elseif($_GET['nbChoix']>5)
		{
	      	$nbChoix=5;
	   	}
	   	else
		{
	   		$nbChoix=$_GET['nbChoix'];
	   	}
	   
	   	$smarty->assign('cle', $cle);
	  	$smarty->assign('sondage',$_POST['sondage']);
	  	$smarty->assign('dateLongue',$_POST['date']);
	  	$smarty->assign('selected' . $_POST['destinataire'],'selected');
	  	$smarty->assign('nbChoix',$_GET['nbChoix']);
	   
	   	//on parcours les réponses
	   	for($i=0;$i<$_GET['nbChoix'];$i++)
	   	{
		   	if(!isset($_POST["choix" . ($i+1)]))
		   	{
	        	$arr[$i]= "";
	    	}
	      	else
	      	{
	        	$arr[$i]=stripslashes($_POST["choix" . ($i+1)]);
	      	}
	   	}
	   	$smarty->assign("reponse",$arr);
	   
	}
	else //l'utilisateur vien de choisir de modifier ce sondage
	{
	  	$sondage = new Sondage($_SESSION["mysqli"]);
	  	if(!$sondage->chargerSondageMySQL($cle))
		{
	    	return;
	  	}
	  
	  	$smarty->assign('cle', $sondage->reqCleSondage());
	  	$smarty->assign('sondage',$sondage->reqTitre());
	  	$smarty->assign('dateLongue',convertirDateEnString($sondage->reqDate()));
	  	$smarty->assign('selected' . $sondage->reqDestinataire(),'selected');
	   	$nbChoix = $sondage->reqNbReponse();
	   	$smarty->assign('nbChoix',$nbChoix);
	   	for($i=0;$i<$nbChoix;$i++)
	   	{
	   		$arr[$i]=$sondage->reqReponse($i+1)->reqReponse();
	   	}
	    
	   	$smarty->assign("reponse",$arr);
	}
   	$smarty->cache_lifetime = 0;
   	$smarty->display('ajout_mod_sondage.tpl');


}

/*******************************************************************************
Fonction : supprimerSondage($cleSondage)
Paramètre :
    - $cleSondage : la clé du sondage à supprimer
Description : on supprime toutes les données en relation avec ce sondage
    dans la base de données
*******************************************************************************/
function supprimerSondage($cleSondage)
{
    $sondage= new Sondage($_SESSION["mysqli"]);
    $sondage->chargerSondageMySQL($cleSondage);
    $sondage->deleteSondageMySQL();
    formSondage();
}

?>
