<?php
/*******************************************************************************
Fichier : inscription-joueur.php
Auteur : Maxime B�gin
Description : Inscription des joueurs en 3 �tapes.
********************************************************************************
14-07-2006 maxime B�gin - Ajout du choix de professeur dans l'inscription. 
	Modification pour avoir seulement un fichier inscription_joueur.tpl 
	au lieu de plusieurs.
21-06-2006 Maxime B�gin - Ajout de commentaires.
10-06-2006 Maxime B�gin - Version initiale
*******************************************************************************/

require_once("lib/ini.php");
  
main();

/*******************************************************************************
Fonction : main()
Param�tre : -
Description : permet de g�rer les diff�rentes actions � effectuer
*******************************************************************************/
function main()
{
  try
  {
	
    $smarty = new MonSmarty();
    global $lang;
    $smarty->assign('titre',$lang['titre_inscription_joueur']);
    
    $smarty->cache_lifetime = 0;
    $smarty->display('header.tpl');
    
    if(isset($_SESSION['css']))
    {
    	$smarty->assign('css',$_SESSION['css']);
    }
    
    $smarty->cache_lifetime = 0;
    $smarty->display('menu.tpl');
    
    //v�rifie s'il y a un param�tre, sinon on passe � l'�tape 1
    if(!isset($_GET["action"]))
    {
      etape1("");
      $joueur=new Joueur($_SESSION["mysqli"]);
      $_SESSION["joueurInscription"]=$joueur;
    }
    else
    {
      //on v�rifie si le joueur est d�j� dans session
      if(!isset($_SESSION["joueurInscription"]))
      {
        $joueur=new Joueur($_SESSION["mysqli"]);
        redirection("cookie.php",0);
        exit(0);
      }
      else
      {
        $joueur=$_SESSION["joueurInscription"];
      }
		
	  //changement de niveau scolaire, il faut rafr�chir la liste des �tablissements
      if($_GET["action"]=="etablissement")
      {
        etape2("");
      }
      elseif($_GET["action"]=="etape2")
      {
      	(($erreur = validerEtape1($joueur)))!="" ? etape1($erreur) : etape2("");
      }
      elseif($_GET["action"]=="etape3")
      {
        if(!isset($_SESSION["joueurInscription"]))
        {
            redirection("cookie.php",0);
        }
        else
        {
            (($erreur = validerEtape2($joueur)))!="" ? etape2($erreur) : etape3("");
        }
      }
      elseif($_GET["action"]=="soumettre")
      {
        if(!isset($_SESSION["joueurInscription"]))
            redirection("index.php",0);
        else
        {
            validerEtape3($_SESSION["joueurInscription"]);
            $smarty->assign('etape',4);
            $smarty->cache_lifetime = 0;
            $smarty->display('inscription_joueur.tpl');
        }
      }
    }
    
    $smarty->cache_lifetime = -1;
    $smarty->display('footer.tpl');
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
Fonction : validerEtape1
Param�tre : $joueur : le joueur qui est en train de s'inscrire
Description : valider et assign� les informations de l'�tape 1
*******************************************************************************/
function validerEtape1($joueur)
{
    global $lang;

    if($_POST["prenom"]=="")
        return $lang['erreur_prenom'];
    elseif($_POST["nom"]=="")
        return $lang['erreur_nom'];
    elseif($_POST["ville"]=="")
        return $lang['erreur_ville'];
    elseif($_POST["province"]=="")
        return $lang['erreur_province'];
    elseif($_POST["pays"]=="")
        return $lang['erreur_pays'];
    elseif(!Courriel::validerCourriel($_POST["courriel"]))
        return $lang['erreur_courriel'];
    elseif($joueur->validerCourrielUnique($_POST["courriel"])==false)
        return $lang['doublon_courriel'];
    elseif($_POST["courriel"]!=$_POST["courriel2"])
        return $lang['erreur_resaisie_courriel'];
    else
    {
        $joueur->asgNom($_POST["nom"]);
        $joueur->asgPrenom($_POST["prenom"]);
        $joueur->asgVille($_POST["ville"]);
        $joueur->asgProvince($_POST["province"]);
        $joueur->asgPays($_POST["pays"]);
        $joueur->asgSexe($_POST['sexe']);
        $joueur->validerCourrielUnique($_POST["courriel"]);
        $joueur->asgCourriel($_POST["courriel"]);
        
        $joueur->asgCleLangue(LANG_FRENCH);
        if(isset($_SESSION['langage'])) {
          if ($_SESSION['langage'] == "english") {
            $joueur->asgCleLangue(LANG_ENGLISH);
          }
        }
        $_SESSION["joueurInscription"] = $joueur;
        return "";
    }
}
/*******************************************************************************
Fonction : validerEtape2
Param�tre : $joueur : le joueur qui est en train de s'inscrire
Description : valider et assign� les informations de l'�tape 2
*******************************************************************************/
function validerEtape2($joueur)
{
  global $lang;

  if(!$joueur->validerAliasUnique($_POST["alias"]))
    return $lang['doublon_alias'];
  elseif(!Joueur::validerAlias($_POST["alias"]))
    return $lang['erreur_alias'];
  elseif(!Joueur::validerMotDePasse($_POST["motDePasse"]))
    return $lang['erreur_mot_passe'];
  elseif($_POST['motDePasse']!=$_POST['motDePasse2'])
  	 return $lang['erreur_resaisie_mot_passe'];
  else
  {

   	if(isset($GLOBALS[$_POST["alias"]]))
   	{
		return $lang['doublon_alias'];
	}
    if(isset($_POST["aliasProf"]))
    {
    	if(!$joueur->asgAdministrateur($_POST["aliasProf"]))
    	{
	  		return $lang['alias_prof_introuvable'];
	 	}
	}
	$joueur->asgAlias($_POST["alias"]);
	$joueur->asgMotDePasse($_POST["motDePasse"]);
	
	if(!isset($_POST["etablissement"]))
	{
	    $joueur->asgEtablissement(0);
	}
	else
	{
	    $joueur->asgEtablissement($_POST["etablissement"]);
	}
	
	$joueur->asgNiveau($_POST["niveau"]);
	
	
	
	//on inscrit le joueur � cet �tape pour s'assurer que le nom d'utlisateur
	//est r�server pour ce joueur, au cas ou 2 joueurs s'inscrive en m�me temps avec
	//le m�me nom d'utlisateur
	$joueur->asgAimeMaths(3);
  $joueur->asgMathConsidere(3);
  $joueur->asgMathEtudie(3);
  $joueur->asgMathDecouvert(3);
  if($joueur->insertionMySQL()) {
    if ($joueur->reqCourriel() != "") {
		  $joueur->envoyerCourrielConfirmation(); 
    }
		$_SESSION["joueurInscription"] = $joueur;
	}
    
	return "";
	 
  }

}

/*******************************************************************************
Fonction : validerEtape3
Param�tre : $joueur : le joueur qui est en train de s'inscrire
Description : valider et assign� les informations de l'�tape 3
*******************************************************************************/
function validerEtape3($joueur)
{
 	$joueur->chargerMySQLCle($joueur->reqCle());
 	if($joueur->reqCle()==0)
 	{
		$log = new clog(LOG_FILE);
		$log->ecrire("Probl�me avec les sessions");
	}
	else
	{
	 	if(isset($_POST["aimeMaths"]))
	 	{
	    	$joueur->asgAimeMaths($_POST["aimeMaths"]);
	    	$joueur->asgMathConsidere($_POST["mathConsidere"]);
	    	$joueur->asgMathEtudie($_POST["mathEtudie"]);
	    	$joueur->asgMathDecouvert($_POST["mathDecouvert"]);
	    }
		$joueur->miseAJourMySQL();
    }
    return "";
}

/*******************************************************************************
Fonction : etape1($erreur)
Param�tre :
    - $erreur : message d'erreur s'il y en a un
Description : afficher le formulaire d'inscription pour l'�tape 1
*******************************************************************************/
function etape1($erreur)
{
    $smarty = new MonSmarty;
    if(isset($_POST["nom"]))
    {
      foreach($_POST as $cle => $valeur)
      {
	    $smarty->assign($cle,stripslashes($valeur)); 
	   }
	  /*
          $smarty->assign('nom',stripslashes($_POST["nom"]));
          $smarty->assign('prenom',stripslashes($_POST["prenom"]));
          $smarty->assign('ville',stripslashes($_POST["ville"]));
          $smarty->assign('province',stripslashes($_POST["province"]));
          $smarty->assign('pays',stripslashes($_POST["pays"]));
          $smarty->assign('courriel',stripslashes($_POST["courriel"]));
          $smarty->assign('courriel2',stripslashes($_POST["courriel2"]));
        */
    }
    $smarty->assign('erreur',$erreur);
	$smarty->assign('etape',1);
	$smarty->cache_lifetime = 0;
	$smarty->display('inscription_joueur.tpl');
}

/*******************************************************************************
Fonction : etape2($erreur)
Param�tre :
    - $erreur : message d'erreur s'il y en a un
Description : afficher le formulaire d'inscription pour l'�tape 2
*******************************************************************************/
function etape2($erreur)
{
    $smarty = new MonSmarty;
    global $lang;
    
    $niveau=7;
    if(isset($_POST["niveau"]))
    {
      $niveau=$_POST["niveau"];
      $smarty->assign('niveau',$niveau);
      $smarty->assign('alias',$_POST["alias"]);
      $smarty->assign('motDePasse',$_POST["motDePasse"]);
      $smarty->assign('motDePasse2',$_POST["motDePasse2"]);
      if(isset($_POST['aliasProf']))
      {
	  		$smarty->assign('aliasProf',$_POST["aliasProf"]);
	  }
    }
    else
    {
        $smarty->assign('niveau',$niveau);
    }

    $smarty->assign('erreur',$erreur);
    
    if($erreur==$lang['doublon_alias'])
    {
        $smarty->assign('suggestion_alias',$_SESSION["joueurInscription"]->suggestionAlias($_POST["alias"]));
    }

    //
    // on g�n�re la liste des niveau scolaire
    // on enleve les niveau primaire,coll�gial,universitaire,grand public temporairement
    for($i=7;$i<=11;$i++)
    {
        $niveauTexte[$i] = $lang["niveau_$i"];
    }
    $smarty->assign('niveauID',array_keys($niveauTexte));
    $smarty->assign('niveauTexte',$niveauTexte);
    
    //
    // g�n�rer la liste d'�tablissement
    //
    $arrE = genererListeEtablissement($niveau);
    $smarty->assign('etablissementID',$arrE[0]);
    $smarty->assign('etablissementTexte',$arrE[1]);
    
    if(isset($_POST["etablissement"]))
    {
        $smarty->assign('etablissement',$_POST["etablissement"]);
    }

	 $smarty->assign('etape',2);
	 $smarty->cache_lifetime = 0;
	 $smarty->display('inscription_joueur.tpl');
    
}

/*******************************************************************************
Fonction : etape3
Param�tre :
Description : afficher le formulaire d'inscription pour l'�tape 3
*******************************************************************************/
function etape3()
{
    $smarty = new MonSmarty;
    $smarty->assign('etape',3);
    $smarty->cache_lifetime = 0;
    $smarty->display('inscription_joueur.tpl');

}

