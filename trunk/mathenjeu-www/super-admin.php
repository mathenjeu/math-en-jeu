<?php
/*******************************************************************************
Fichier : super-admin.php
Auteur : Maxime Bégin
Description : Permet de gérer les différente action qu'un super-admin
    peut effectuer. Par exemple : l'ajout, la modification de nouvelles et de
    sondage. Permet aussi de visualiser sous forme graphiques des statistiques
    et les sondages.
    TODO :  la gestion des questions qui seront éventuellement soumises
            par les professeurs.
********************************************************************************
10-11-2006 Maxime Bégin - Modification pour inclure les informations de connexion
	SMTP dans le fichier de configuration
22-09-3006 Maxime Bégin - Modification de la longueur du wordwrap du texte 
	de la nouvelle( de 80 à 50)
26-07-2006 Maxime Bégin - ajout de la modification des sondages.
06-07-2006 Maxime Bégin - ajout de gestion de la faq.
05-07-2006 Maxime Bégin - ajout du choix de langue dans la configuration
04-07-2006 Maxime Bégin - ajout de la section configuration, 
	et d'option pour changer la couleur d'un skin
22-06-2006 Maxime Bégin - ajout de destinataire pour les
    nouvelles et sondage.
21-06-2006 Maxime Bégin - Ajout de commentaires.
10-06-2006 Maxime Bégin - Version initiale
*******************************************************************************/

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
    $smarty->assign('titre',$lang['titre_admin']);
    $smarty->cache_lifetime = 0;
    $smarty->display('header.tpl');


    //si le super-admin n'est pas connecté ; on affiche la page de login
    if(!isset($_SESSION["superadmin"]))
    {
      if(isset($_GET["action"]))
      {
        if($_GET["action"]=="doLogin")
            doLogin();
        else
            login("");
      }
      else
        login("");
    }
    else
    {
        if(isset($_SESSION['css']))
        {
    		$smarty->assign('css',$_SESSION['css']);
    	}
        //le super-admin est connecté on affiche donc le menu   
		$smarty->cache_lifetime = -1;    	
        $smarty->display('menu_superadmin.tpl');
        
        if(isset($_GET["action"]))
        {
        	$action = $_GET["action"];
            switch($action)
            {
            case "nouvelle":
                formNouvelle();
                break;
            case "ajoutNouvelle":
                formAjoutNouvelle("");
                break;
            case "insertNouvelle":
                insertNouvelle($_POST["titre"],$_POST["nouvelle"],$_POST["destinataire"],$_POST['image']);
                break;
            case "updateNouvelle":
                formModifierNouvelle($_GET["cleNouvelle"],"");
                break;
            case "doUpdateNouvelle":
                updateNouvelle($_GET["cleNouvelle"],$_POST["titre"],
                    $_POST["nouvelle"],$_POST["destinataire"],$_POST['image']);
                break;
            case "deleteNouvelle":
                supprimerNouvelle($_GET["cleNouvelle"]);
                break;
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
            case "sondage":
                formSondage();
                break;
            case "deleteSondage":
                supprimerSondage($_GET["cleSondage"]);
                break;
            case "question":
                afficherQuestion();
                break;
            case "logout":
                unset($_SESSION["superadmin"]);
                redirection("index.php",0);
                break;
            case "statistique":
                formStatistique();
                break;
            case "showStatistique":
                formStatistique();
                showStatistique();
                break;
            case "couleur":
				$_SESSION['css'] = $_POST['css'];
				redirection("super-admin.php",0);
				return;
				break;
			case "config":
				config("");
	  			break;
	  		case "templates":
	  			$_POST['css']="";
	  			config("");
	  			break;
	  		case "doConfig":
	  			doConfig();
	  			redirection("super-admin.php?action=config",0);
	  			break;
	  		case "faq":
	  			formFaq();
	  			break;
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
	  		default:
	  			config("");
	  			break;
             }
        }

    }
    $smarty->cache_lifetime = -1;
    $smarty->display("footer.tpl");
    $smarty->clear_cache();
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

/*******************************************************************************
Fonction : doConfig
Paramètre : 
Description : enregistrer les configurations du serveur dans un fichier xml
*******************************************************************************/
function doConfig()
{
  $fichier = fopen(CONFIG_FILE,'w');
  $contenu = "<?xml version ='1.0' encoding='UTF-8' ?>
<config>
	<langue>" . $_POST['langue'] . "</langue>
	<adresseWeb>" . $_POST['adresseWeb'] . "</adresseWeb>
	<nbNouvelles>" . $_POST['nbNouvelles'] . "</nbNouvelles>
	<nbJoueurs>" . $_POST['nbJoueurs'] . "</nbJoueurs>
	<minParties>" . $_POST['minParties'] . "</minParties>
	<nbJours>" . $_POST['nbJours'] . "</nbJours>
	<template>" . $_POST['template'] . "</template>
	<css>" . $_POST['css'] . "</css>
	<nomCourriel>" . utf8_encode(stripslashes($_POST['nomCourriel'])) . "</nomCourriel>
	<courriel>" . $_POST['courriel'] . "</courriel>
	<serveurSMTP>" . $_POST['serveurSMTP'] . "</serveurSMTP>
	<portSMTP>" . $_POST['portSMTP'] . "</portSMTP>
	<utilisateurSMTP>" . $_POST['utilisateurSMTP'] . "</utilisateurSMTP>
	<motDePasseSMTP>" . $_POST['motDePasseSMTP'] . "</motDePasseSMTP>
	<sujet_courriel_inscription><![CDATA[" . utf8_encode(stripslashes($_POST['sujet_courriel_inscription'])) . "]]></sujet_courriel_inscription>
	<courriel_inscription><![CDATA[" . utf8_encode(stripslashes($_POST['courriel_inscription'])) . "]]></courriel_inscription>
	<sujet_courriel_pass_perdu><![CDATA[" . utf8_encode(stripslashes($_POST['sujet_courriel_pass_perdu'])) . "]]></sujet_courriel_pass_perdu>
	<courriel_pass_perdu><![CDATA[" . utf8_encode(stripslashes($_POST['courriel_pass_perdu'])) . "]]></courriel_pass_perdu>
	<dbHote>" . $_POST['dbHote'] . "</dbHote>
	<dbUtilisateur>" . $_POST['dbUtilisateur'] . "</dbUtilisateur>
	<dbMotDePasse>" . $_POST['dbMotDePasse'] . "</dbMotDePasse>
	<dbSchema>" . $_POST['dbSchema'] . "</dbSchema>
</config>";

  fwrite($fichier,$contenu);  
  fclose($fichier);
  $smarty=new MonSmarty();
  $smarty->clear_compiled_tpl();
  $smarty->clear_cache();
  
  $sa = $_SESSION["superadmin"];
  session_destroy();
  error_reporting(0);
  session_start();
  error_reporting(E_ALL);
  $_SESSION["superadmin"]=$sa;

  $_SESSION['css']= $_POST['css'];
}


/*******************************************************************************
Fonction : config
Paramètre : 
	- erreur : un message d'erreur s'il y en a
Description : afficher le formulaire pour changer les configurations
*******************************************************************************/
function config($erreur)
{
	$smarty=new MonSmarty();
	$smarty->assign("erreur",$erreur);
	if(!isset($_POST['nbNouvelles']))
	{
	  $config = simplexml_load_file(CONFIG_FILE);
	  $smarty->assign('adresseWeb',$config->adresseWeb);
	  
	  $smarty->assign('nbNouvelles',$config->nbNouvelles);
	  $smarty->assign('nbJoueurs',$config->nbJoueurs);
	  $smarty->assign('minParties',$config->minParties);
	  $smarty->assign('nbJours',$config->nbJours);
	  
	  $smarty->assign('nomCourriel',utf8_decode($config->nomCourriel));
	  $smarty->assign('courriel',$config->courriel);
	  $smarty->assign('serveurSMTP',$config->serveurSMTP);
	  $smarty->assign('portSMTP',$config->portSMTP);
	  $smarty->assign('utilisateurSMTP',$config->utilisateurSMTP);
	  $smarty->assign('motDePasseSMTP',$config->motDePasseSMTP);
	  
	  $smarty->assign('sujet_courriel_inscription',utf8_decode($config->sujet_courriel_inscription));
	  $smarty->assign('courriel_inscription',utf8_decode($config->courriel_inscription));
	  $smarty->assign('sujet_courriel_pass_perdu',utf8_decode($config->sujet_courriel_pass_perdu));
	  $smarty->assign('courriel_pass_perdu',utf8_decode($config->courriel_pass_perdu));
	  
	  $smarty->assign('dbHote',$config->dbHote);
	  $smarty->assign('dbUtilisateur',$config->dbUtilisateur);
	  $smarty->assign('dbMotDePasse',$config->dbMotDePasse);
	  $smarty->assign('dbSchema',$config->dbSchema);
	  
	  $smarty->assign('sTemplate',preg_replace('/\s+/','',$config->template));
	  $smarty->assign('scss',preg_replace('/\s+/','',$config->css));
	  $smarty->assign('sLangue',preg_replace('/\s+/','',$config->langue));
	  
	  templates_dir($smarty,TEMPLATES_DIR);
	  css_file($smarty,TEMPLATES_DIR . $config->template);
	  langage_dir($smarty,LANGAGE_DIR);
	  
	}
	else //on passe ici lors du choix de template
	{
	  $smarty->assign('adresseWeb',$_POST['adresseWeb']);
	  $smarty->assign('nbNouvelles',$_POST['nbNouvelles']);
	  $smarty->assign('nbJoueurs',$_POST['nbJoueurs']);
	  $smarty->assign('minParties',$_POST['minParties']);
	  $smarty->assign('nbJours',$_POST['nbJours']);
	  
	  $smarty->assign('nomCourriel',stripslashes($_POST['nomCourriel']));
	  $smarty->assign('courriel',$_POST['courriel']);
	  $smarty->assign('serveurSMTP',$_POST['serveurSMTP']);
	  $smarty->assign('portSMTP',$_POST['portSMTP']);
	  $smarty->assign('utilisateurSMTP',$_POST['utilisateurSMTP']);
	  $smarty->assign('motDePasseSMTP',$_POST['motDePasseSMTP']);
	  
	  $smarty->assign('sujet_courriel_inscription',stripslashes($_POST['sujet_courriel_inscription']));
	  $smarty->assign('courriel_inscription',stripslashes($_POST['courriel_inscription']));
	  $smarty->assign('sujet_courriel_pass_perdu',stripslashes($_POST['sujet_courriel_pass_perdu']));
	  $smarty->assign('courriel_pass_perdu',stripslashes($_POST['courriel_pass_perdu']));
	  
	  $smarty->assign('dbHote',$_POST['dbHote']);
	  $smarty->assign('dbUtilisateur',$_POST['dbUtilisateur']);
	  $smarty->assign('dbMotDePasse',$_POST['dbMotDePasse']);
	  $smarty->assign('dbSchema',$_POST['dbSchema']);
	  	  
	  $smarty->assign('sTemplate',$_POST['template']);
	  $smarty->assign('sLangue',$_POST['langue']);
	  
	  templates_dir($smarty,TEMPLATES_DIR);
	  langage_dir($smarty,LANGAGE_DIR);
	  
	  if($_POST['css']=="")
	  	css_file($smarty,TEMPLATES_DIR . $_POST['template']);
	  else
	  	$smarty->assign('scss',$_POST['css']);
	}
	$smarty->cache_lifetime = 0;
  	$smarty->display('configuration.tpl');
}



/*******************************************************************************
Fonction : formStatistique
Paramètre :
Description : afficher les statistiques
*******************************************************************************/
function formStatistique()
{
  $smarty=new MonSmarty();
  $smarty->cache_lifetime = 0;
  $smarty->display('statistiques.tpl');
}

/*******************************************************************************
Fonction : doLogin()
Paramètre :
Description : vérifier les données entrées par l'utilisateur,
    et faire le login si elles sont valide
*******************************************************************************/
function doLogin()
{
    global $lang;
    $smarty=new MonSmarty();
    $sAdmin = new SuperAdmin($_SESSION["mysqli"]);
    
    if(!$sAdmin->chargerMySQL($_POST["courriel"],$_POST["motDePasse"]))
    {
        login($lang['login_impossible']);
    }
    else
    {
        $_SESSION["superadmin"]=$sAdmin;
        if(isset($_SESSION['css']))
        {
    		$smarty->assign('css',$_SESSION['css']);
    	}
    	$smarty->cache_lifetime = -1;
        $smarty->display('menu_superadmin.tpl');
  		config("");
    }
}

/*******************************************************************************
Fonction : login()
Paramètre :
Description : afficher un formulaire de login pour les super-administrateur
*******************************************************************************/
function login($erreur)
{
    $smarty=new MonSmarty();
    if(isset($_SESSION['css']))
    	$smarty->assign('css',$_SESSION['css']);
    
    $smarty->cache_lifetime = 0;
    $smarty->display('menu.tpl');
    
    $smarty->assign('erreur',$erreur);
    $smarty->cache_lifetime = 0;
    $smarty->display('login_superadmin.tpl');
}

/*******************************************************************************
Fonction : afficherQuestion()
Paramètre :
Description : afficher les question qui sont en attente de validation
*******************************************************************************/
function afficherQuestion()
{
  $smarty=new MonSmarty();
  
    $mysqli=($_SESSION["mysqli"]);
  
    $sql = "select * from questiontmp order by cleQuestion desc";
    $result = $mysqli->query($sql);
    $nb = $result->num_rows;
    
    for($i=0;$i<$nb;$i++)
    {
      $row[]=$result->fetch_assoc();
    }
    
    $smarty->assign('questions',$row);
    
    $smarty->cache_lifetime = 0;
    $smarty->display("liste_question.tpl");

}


/*******************************************************************************
Fonction : updateNouvelle($cle,$titre,$nouvelle)
Paramètre :
    - $cle : la clé unique de la nouvelle
    - $titre : le titre de la nouvelle
    - $nouvelle : la nouvelle
Description : met à jour la nouvelle
*******************************************************************************/
function updateNouvelle($cle,$titre,$texte,$destinataire,$image)
{
  global $lang;
  if($titre=="")
    formModifierNouvelle($cle,$lang['nouvelle_titre_vide']);
  elseif($texte=="")
    formModifierNouvelle($cle,$lang['nouvelle_nouvelle_vide']);
  else
  {
      $nouvelle=new UneNouvelle($_SESSION["mysqli"]);
      $nouvelle->chargerMySQL($cle);
      $nouvelle->asgTitre(addslashes($titre));
      $nouvelle->asgNouvelle(addslashes(wordwrap($texte,50,"\r\n")));
      $nouvelle->asgImage($image);
      $nouvelle->asgDestinataire($destinataire);
      $nouvelle->miseAJourMySQL();
      formNouvelle();
  }

}

/*******************************************************************************
Fonction : supprimerNouvelle($cle)
Paramètre :
    - $cle : la clé de la nouvelle à supprimer
Description : supprime la nouvelle correspondant à la clé passé en paramètre
*******************************************************************************/
function supprimerNouvelle($cle)
{
  if($cle>=0)
  {
    $nouvelle=new UneNouvelle($_SESSION["mysqli"]);
    $nouvelle->chargerMySQL($cle);
    $nouvelle->deleteMySQL();
  }
  formNouvelle();
}

/*******************************************************************************
Fonction : insertNouvelle($titre,$nouvelle)
Paramètre :
    - $titre : le titre de la nouvelle
    - $nouvelle : la nouvelle
Description : ajoute une nouvelle à la table
*******************************************************************************/
function insertNouvelle($titre,$texte,$destinataire,$image)
{
  global $lang;
  if($titre=="")
  {
    formAjoutNouvelle($lang['nouvelle_titre_vide']);
  }
  elseif($texte=="")
  {
    formAjoutNouvelle($lang['nouvelle_nouvelle_vide']);
  }
  else
  {
    $nouvelle=new UneNouvelle($_SESSION["mysqli"]);
    $nouvelle->asgTitre(addslashes($titre));
    $nouvelle->asgNouvelle(addslashes(wordwrap($texte,50,"\r\n")));
    $nouvelle->asgImage($image);
    $nouvelle->asgDestinataire($destinataire);
    $nouvelle->insertionMySQL();
    formNouvelle();
  }
}

/*******************************************************************************
Fonction : formNouvelle()
Paramètre :
Description : affiche les nouvelles avec des liens vers la modification
    et la suppression
*******************************************************************************/
function formNouvelle()
{
  	global $lang;
  	$smarty=new MonSmarty();
  
    $nouvelles = new Nouvelles($_SESSION["mysqli"]);
    //on charge toutes les nouvelles
    $nouvelles->chargerMySQL(-1,array(0,1,2));
    for($i=0;$i<$nouvelles->reqNbNouvelle();$i++)
    {
        $nouvelle=$nouvelles->reqNouvelle($i+1);
        $arr[$i]['cle'] = $nouvelle->reqCle();
        $arr[$i]['date'] = convertirDateEnString($nouvelle->reqDate());
        $arr[$i]['titre'] = $nouvelle->reqTitre();
        $arr[$i]['nouvelle'] = htmlentities(substr($nouvelle->reqNouvelle(),0,120));
        if(strlen($nouvelle->reqNouvelle())>120)
        {
        	$arr[$i]['nouvelle'] .= " ...";
        }
        $arr[$i]['destinataire'] = $lang['type_destinataire'][$nouvelle->reqDestinataire()];
    }
    
    if($nouvelles->reqNbNouvelle() > 0)
    {
    	$smarty->assign('nouvelles',$arr);
    }
    	
    $smarty->cache_lifetime = 0;
    $smarty->display('liste_nouvelle.tpl');
}


/*******************************************************************************
Fonction : formModifierNouvelle($cle)
Paramètre :
    $cle : le numéro de la nouvelle à modifier
    $erreur : un message d'erreur s'il y en a
Description : afficher le formulaire pour la modification de la nouvelle
    qui correspond à $cle
*******************************************************************************/
function formModifierNouvelle($cle,$erreur)
{
  $smarty=new MonSmarty();
  $smarty->assign('erreur',$erreur);
  $smarty->assign('action','modifier');

  $nouvelle = new UneNouvelle($_SESSION["mysqli"]);
  if(!$nouvelle->chargerMySQL($cle))
    return;
  
  $smarty->assign('cle', $nouvelle->reqCle());
  $smarty->assign('titre',$nouvelle->reqTitre());
  $smarty->assign('nouvelle',$nouvelle->reqNouvelle());
  $smarty->assign('dateLongue',convertirDateEnString($nouvelle->reqDate()));
  $smarty->assign('selected' . $nouvelle->reqDestinataire(),'selected');

  imageDir($smarty,$nouvelle->reqImage());
  $smarty->cache_lifetime = 0;
  $smarty->display('ajout_mod_nouvelle.tpl');

}

/*******************************************************************************
Fonction : imageDir
Paramètre :
    - $smarty : l'adresse de l'objet smarty
    - $img : l'image à afficher(modification de nouvelle)
            ou bien vide si on ajoute une nouvelle
Description : parcourir le dossier des images relié au nouvelle et
    assigner ces informations à smarty
*******************************************************************************/
function imageDir(&$smarty,$img)
{
  $dir = IMAGE_DIR;
  $i=0;

  if($img!="")
  {
    $smarty->assign('image',$img);
  }

  $arr[' '] = " ";
  //on parcour le dossier qui contient les images des nouvelles
  if (is_dir($dir))
  {
   if ($dh = opendir($dir))
   {
       while (($file = readdir($dh)) !== false)
       {
         if(!is_dir($file))
         {
            $arr['img/sujet/' . $file] = $file;
            if($i==0 && $img=="")
                $smarty->assign('image','img/sujet/' . $file);
            $i++;
         }

       }
       closedir($dh);
   }
  }
  $smarty->assign('img',$arr);

}

/*******************************************************************************
Fonction : formAjoutNouvelle()
Paramètre : $erreur : un message d'erreur s'il y en a
Description : afficher le formulaire pour l'ajout d'une nouvelle
*******************************************************************************/
function formAjoutNouvelle($erreur)
{
  $smarty=new MonSmarty();
  $smarty->assign('erreur',$erreur);
  $smarty->assign('action','ajout');
  if(isset($_POST["titre"]))
  {
    $smarty->assign('titre',$_POST["titre"]);
    $smarty->assign('nouvelle',$_POST["nouvelle"]);
    $smarty->assign('selected' . $_POST['destinataire'],"selected");
  }
  $smarty->assign('dateLongue',convertirDateEnString(date("Y-m-d")));

  imageDir($smarty,"");
  
  $smarty->cache_lifetime = 0;
  $smarty->display('ajout_mod_nouvelle.tpl');

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
