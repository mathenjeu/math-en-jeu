<?php 
/*******************************************************************************
Fichier : contact.php
Auteur : Maxime B�gin
Description : pour afficher la page qui permet aux joueurs de nous contacter
********************************************************************************
11-24-2006 Maxime B�gin - Version initiale
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

	
	//on v�rifie si un action est en cour ou non
	//sinon on affiche le formulaire pour le contact
	if(!isset($_GET['action']))
	{
	 	$smarty->assign('status',0);
	 	$smarty->cache_lifetime = 0;
	 	if (isset($_GET['sujet'])) {
	 	  $smarty->assign('sujet',$_GET['sujet']);
	 	}
		$smarty->display('contact.tpl');
	}
	elseif($_GET['action']=="envoyer")
	{
	 	$erreur="";
	 	//on v�rifie que le sujet et le message ne sont pas vide
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
		 	//on affiche un message selon que le message a bien �t� envoy�
		 	//ou bien qu'il y a eu un probl�me
			$mail = new Courriel($_POST['sujet'],$_POST['message'],ADRESSE_COURRIEL);
			$mail->FromName = $_POST['nom'];
			$mail->From = $_POST['courriel'];
			if($mail->send())
			{
				$smarty->assign('status',1);
				//si le sujet du courriel est beta on réenvoit directement un nouveau courriel

				if ($_POST['sujet'] == "Version 2") {
				  $mail2 = new Courriel("Participation au béta",
				    $lang['message_bienvenue_beta'],$_POST['courriel']);
				  $mail2->FromName = "Maxime Bégin";
			      $mail2->From = "maxime.begin@smac.ulaval.ca";
				  if (!$mail2->send()) {
				    echo "erreur";
				  }
				}
          
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