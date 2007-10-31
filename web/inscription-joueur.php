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
	
    $smarty = new MonSmarty($_SESSION['langage']);
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

    if(!isset($_GET["action"]))
    {
      etape1("");
    } elseif($_GET["action"]=="soumettre") {
      $erreur = validerEtape1();
      if ($erreur != "") {
        etape1($erreur);
      } else {
        $smarty->assign('etape',4);
        $smarty->cache_lifetime = 0;
        $smarty->display('inscription_joueur.tpl');
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
function validerEtape1()
{
    global $lang;
    $error = "";
    $joueur = new Joueur($_SESSION["mysqli"]);
    
    if($_POST["prenom"]=="") {
      $error = $lang['erreur_prenom'];
    } elseif($_POST["nom"]=="") {
      $error = $lang['erreur_nom'];
    } elseif($_POST["ville"]=="") {
      $error = $lang['erreur_ville'];
    } elseif($_POST["province"]=="") {
      $error = $lang['erreur_province'];
    } elseif($_POST["pays"]=="") {
      $error = $lang['erreur_pays'];
    } elseif(!Courriel::validerCourriel($_POST["courriel"])) {
      $error = $lang['erreur_courriel'];
    } elseif($joueur->validerCourrielUnique($_POST["courriel"])==false) {
      $error = $lang['doublon_courriel'];
    } elseif($_POST["courriel"]!=$_POST["courriel2"]) {
      $error = $lang['erreur_resaisie_courriel'];
    } elseif(!$joueur->validerAliasUnique($_POST["alias"])) {
      $error = $lang['doublon_alias'];
    } elseif(!Joueur::validerAlias($_POST["alias"])) {
      $error = $lang['erreur_alias'];
    } elseif(!Joueur::validerMotDePasse($_POST["motDePasse"])) {
      $error = $lang['erreur_mot_passe'];
    } elseif($_POST['motDePasse']!=$_POST['motDePasse2']) {
  	  $error = $lang['erreur_resaisie_mot_passe'];
    }

     
    if ($error == "") {
        $joueur->asgNom($_POST["nom"]);
        $joueur->asgPrenom($_POST["prenom"]);
        $joueur->asgVille($_POST["ville"]);
        $joueur->asgProvince($_POST["province"]);
        $joueur->asgPays($_POST["pays"]);
        $joueur->asgSexe($_POST['sexe']);
        $joueur->validerCourrielUnique($_POST["courriel"]);
        $joueur->asgCourriel($_POST["courriel"]);
        
        $joueur->setLanguage($_SESSION['langage']);
        
        $joueur->asgAlias($_POST["alias"]);
      	$joueur->asgMotDePasse($_POST["motDePasse"]);
      	$joueur->asgEtablissement(0);
      	
      	/*
      	$niveau = array();
      	foreach($_POST['subject'] as $k => $v) {
      	  $niveau[$k] = $v;
      	}
*/
      	
      	$joueur->asgNiveau($_POST['subject']);
      	
      	//echo $_POST['subject'];

        if($joueur->insertionMySQL()) {
          if ($joueur->reqCourriel() != "") {
      		  $joueur->envoyerCourrielConfirmation(); 
          }
      		$_SESSION["joueurInscription"] = $joueur;
      	}

        return "";
        
    } else {
      return $error;
    }
}

/*******************************************************************************
Fonction : etape1($erreur)
Param�tre :
    - $erreur : message d'erreur s'il y en a un
Description : afficher le formulaire d'inscription pour l'�tape 1
*******************************************************************************/
function etape1($erreur)
{
    $smarty = new MonSmarty($_SESSION['langage']);
    if($erreur != "")
    {
      foreach($_POST as $cle => $valeur)
      {
        if (!is_array($valeur)) {
	        $smarty->assign($cle,stripslashes($valeur));
        } 
	    }
    }
    
    $dao = new Dao($_SESSION["mysqli"]);
    $result = $dao->getSubjectsLevels($_SESSION['langage']);
    
    
    if ($erreur != "") {
      foreach($_POST['subject'] as $subject_id => $level_id)
      {
        $result[$subject_id]['selected'] = $level_id;
      }
    }
    
    $smarty->assign('subjects', $result);
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
    $smarty = new MonSmarty($_SESSION['langage']);
    global $lang;
    
    //$niveau=7;
    if(isset($_POST["school_type"]))
    {
      $selected_school_type_id=$_POST["school_type"];
      $smarty->assign('selected_school_type_id',$selected_school_type_id);
      $smarty->assign('alias',$_POST["alias"]);
      $smarty->assign('motDePasse',$_POST["motDePasse"]);
      $smarty->assign('motDePasse2',$_POST["motDePasse2"]);

    }
    else
    {
        $smarty->assign('selected_school_type_id',0);
    }

    $smarty->assign('erreur',$erreur);
    
    if($erreur==$lang['doublon_alias'])
    {
        $smarty->assign('suggestion_alias',$_SESSION["joueurInscription"]->suggestionAlias($_POST["alias"]));
    }

    $arrET = getSchoolType();
    $smarty->assign('school_type_id',array_keys($arrET[0]));
    $smarty->assign('school_type_name',$arrET[1]);
    
    
    //
    // on g�n�re la liste des niveau scolaire
    // on enleve les niveau primaire,coll�gial,universitaire,grand public temporairement
    /*
    for($i=7;$i<=11;$i++)
    {
        $niveauTexte[$i] = $lang["niveau_$i"];
    }
    $smarty->assign('niveauID',array_keys($niveauTexte));
    $smarty->assign('niveauTexte',$niveauTexte);
    */
    
    //
    // g�n�rer la liste d'�tablissement
    //
    
    /*
    $arrE = genererListeEtablissement($school_type_id);
    $smarty->assign('etablissementID',$arrE[0]);
    $smarty->assign('etablissementTexte',$arrE[1]);
    */
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
    $smarty = new MonSmarty($_SESSION['langage']);
    $smarty->assign('etape',3);
    $smarty->cache_lifetime = 0;
    $smarty->display('inscription_joueur.tpl');

}

