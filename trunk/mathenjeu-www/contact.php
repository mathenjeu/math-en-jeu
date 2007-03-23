<?php 
/*******************************************************************************
Fichier : contact.php
Auteur : Maxime Bégin
Description : pour afficher la page qui permet aux joueurs de nous contacter
********************************************************************************
11-24-2006 Maxime Bégin - Version initiale
*******************************************************************************/

require_once("lib/ini.php");

main();

function main()
{
  try
  {
	$smarty = new MonSmarty;
	global $lang;
	
	$smarty->assign('titre',$lang['titre_contact']);
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
	 	$smarty->assign('nom',$_SESSION["joueur"]->reqPrenom() . " " . $_SESSION["joueur"]->reqNom());
	 	$smarty->assign('courriel',$_SESSION["joueur"]->reqCourriel());
	 	$smarty->assign('acces',$_SESSION["joueur"]->reqCategorie());
	}
	
	$smarty->cache_lifetime = 0;
	$smarty->display('menu.tpl');

	
	//on vérifie si un action est en cour ou non
	//sinon on affiche le formulaire pour le contact
	if(!isset($_GET['action']))
	{
	 	$smarty->assign('status',0);
	 	$smarty->cache_lifetime = 0;
		$smarty->display('contact.tpl');
	}
	elseif($_GET['action']=="envoyer")
	{
	 	$erreur="";
	 	//on vérifie que le sujet et le message ne sont pas vide
	 	if($_POST['sujet']=="")
	 	{
	 	 	$erreur = $lang['erreur_sujet_vide'];
		}
		elseif($_POST['message']=="")
		{
			$erreur = $lang['erreur_message_vide'];
		}
		elseif($_POST['nom']=="")
		{
		 	$erreur = $lang['erreur_nom'];
			
		}
		elseif(!Courriel::validerCourriel($_POST['courriel']))
		{
		 	$erreur = $lang['erreur_courriel'];
		}
		
		if($erreur!="")
		{
		 	$smarty->assign("erreur",$erreur);
		 	$smarty->assign('message',$_POST['message']);
		 	$smarty->assign('sujet',$_POST['sujet']);
		 	$smarty->assign('nom',$_POST['nom']);
		 	$smarty->assign('courriel',$_POST['courriel']);
		}
		else
		{
			//on tente maintenant d'envoyer le courriel
		 	//on affiche un message selon que le message a bien été envoyé
		 	//ou bien qu'il y a eu un problème
			$mail = new Courriel($_POST['sujet'],$_POST['message'],ADRESSE_COURRIEL);
			$mail->FromName = $_POST['nom'];
			$mail->From = $_POST['courriel'];
			if($mail->send())
			{
				$smarty->assign('status',1);
			}
			else
			{
				$smarty->assign('status',2);
			}
		}
		$smarty->cache_lifetime = 0;
		$smarty->display('contact.tpl');
	}
	
	$smarty->cache_lifetime = -1;
	$smarty->display('footer.tpl');
  }
  catch(MyException $e)
  {
  	echo $e->exception_dump();  
  }
}

?>