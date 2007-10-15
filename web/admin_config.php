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
     	config("");
    }
    else
    {
     	if($_GET['action']=="doConfig")
     	{
	  		doConfig();
	  		redirection("admin_config.php",0);
	  		return;
	  	}
	  	elseif($_GET['action']=="templates")
	  	{
	  		$_POST['css']="";
	  		config("");
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
  $smarty = new MonSmarty($_SESSION['langage']);
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
	$smarty = new MonSmarty($_SESSION['langage']);
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

?>