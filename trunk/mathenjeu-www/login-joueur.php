<?php
/*******************************************************************************
Fichier : joueur-login.php
Auteur : Maxime Bégin
Description : Afficher la page de connexions des joueurs et valider cette connexion
********************************************************************************
21-06-2006 Maxime Bégin - Ajout de commentaires.
10-06-2006 Maxime Bégin - Version initiale
*******************************************************************************/

require_once("lib/ini.php");

main();

function main()
{
  $smarty = new MonSmarty();
  global $lang;

  try
  {
    /*
   	if(!isset($_COOKIE['test']))
   	{
		redirection("cookie.php",0);
		exit;
	}
	*/
    //si le joueur est déjà connecté,
    //il est redirigé à la page portail-joueur
    if(isset($_SESSION["joueur"]))
    {
        redirection("nouvelles.php",0);
    }
    else
    {

      $smarty->assign('titre',$lang['titre_connexion']);
      
      if(isset($_SESSION['css']))
      {
        $smarty->assign('css',$_SESSION['css']);
      }
     
      if(isset($_GET["action"]))
      {
       //on valide les informations de connexion
       	if($_GET["action"]=="valider")
      	{
        	$joueur=new Joueur($_SESSION["mysqli"]);
        	if($joueur->chargerMySQL($_POST["alias"],$_POST["motDePasse"]))
        	{
            	$_SESSION["joueur"] = $joueur;
            	
            	//on vérifie si le HTTP_REFERER existe si oui
            	//on redirige le client vers la page où il s'est 
            	//connecté
            	if(isset($_SERVER['HTTP_REFERER']))
            	{
            	 	redirection($_SERVER['HTTP_REFERER'],0);
				}
				else
				{
            		redirection("nouvelles.php",0);
            	}
        	}
        	else
        	{
        	 	//une erreur est survenue dans la connexion
        	 	$smarty->cache_lifetime = 0;
        	 	$smarty->display('header.tpl');
	    		$smarty->display('menu.tpl');
            	$smarty->assign('erreur',$lang['login_impossible']);
            	$smarty->display('login_joueur.tpl');
            	$smarty->cache_lifetime = -1;
            	$smarty->display('footer.tpl');
        	}
        }
        else
        {
         	//si l'action n'est pas de valider le login d'un joueur
         	$smarty->cache_lifetime = 0;
         	$smarty->display('header.tpl');
	    	$smarty->display('menu.tpl');
			if($_GET["action"]=="pass_perdu")
		    {
		     	$smarty->cache_lifetime = 0;
		        $smarty->display('login_joueur_pass_perdu.tpl');
		    }
		    elseif($_GET["action"]=="envoi_info")
		    {
		        $joueur=new Joueur($_SESSION["mysqli"]);
		        if(!$joueur->envoyerCourrielInfoPerdu($_POST["courriel"]))
		        {
		          $smarty->assign('erreur',$lang['courriel_introuvable']);
		          $smarty->cache_lifetime = 0;
		          $smarty->display('login_joueur_pass_perdu.tpl');
		        }
		        else
		        {
		          $smarty->cache_lifetime = 0;
		          $smarty->display('login_joueur_pass_perdu_ok.tpl');
		          redirection("index.php",10);
		        }
		    }
		    $smarty->cache_lifetime = -1;
		    $smarty->display('footer.tpl');
		}
		
	  }
	  //si aucune action on affiche la page de login
	  else
	  {
	   	$smarty->cache_lifetime = 0;
	    $smarty->display('header.tpl');
	    $smarty->display('menu.tpl');
		$smarty->display('login_joueur.tpl');
		$smarty->cache_lifetime = -1;
		$smarty->display('footer.tpl');
	  }
    }
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

