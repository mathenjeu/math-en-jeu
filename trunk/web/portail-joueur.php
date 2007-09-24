<?php
/*******************************************************************************
Fichier : portail-joueur.php
Auteur : Maxime B�gin
Description : Permet de g�rer les diff�rentes actions qu'un joueur
    peut effectuer. Par exemple : modifi� ses informations, afficher le palmar�s
    des meilleurs joueurs, r�pondre aux sondage, lire les nouvelles
    et lancer le jeu.
********************************************************************************
26-11-2006 Maxime B�gin - d�m�nagement de plusieur fonctionnalit� dans des pages
	ind�pendante.
23-06-2006 Maxime B�gin - Ajout d'image pour les nouvelles
22-06-2006 Maxime B�gin - Modification pour l'ajout de destinataire
    pour les nouvelles et les sondages
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
    	
      //on v�rifie su le joueur est connect� et a la permission d'acc�d� � cette page
      if(!isset($_SESSION["joueur"]))
	  {
        redirection("login-joueur.php",0);
      }
      else
      {
        $joueur = $_SESSION["joueur"];
        
        $smarty->assign('connecter',1);
        $smarty->assign('titre',$lang['titre_portail_joueur']);
        
        $smarty->cache_lifetime = 0;
        $smarty->display('header.tpl');
        
        $smarty->assign('alias',$joueur->reqAlias());
        $smarty->assign('motDePasse',$joueur->reqMotDePasse());
        $smarty->assign('acces',$_SESSION["joueur"]->reqCategorie());
        
        if(isset($_SESSION['css']))
		{
        	 $smarty->assign('css',$_SESSION['css']);
        }
        
        $smarty->cache_lifetime = 0;
        $smarty->display('menu.tpl');
        
        //si on a une action � �fectuer
        if(isset($_GET["action"]))
        {
            $action=$_GET["action"];
            if($action=="profil" || $action=="etablissement")
            {
                formulaireModification($joueur,"",0,"",0);
            }
            elseif($action=="doModificationScolaire")
            {
              doModificationScolaire($joueur);
            }
            elseif($action=="doModificationPass")
            {
              doModificationPass($joueur);
            }
            elseif($action=="doModificationPerso")
            {
              doModificationPerso($joueur);
            }
			elseif($action=="stat")
			{
			 	statJoueur();
			}
			
        }
        	
		$smarty->cache_lifetime = -1;
        $smarty->display('footer.tpl');

      }
  }
  catch(MyException $e)
  {
    echo $e->exception_dump();
  }
  catch(SQLException $e)
  {
    echo $e->exception_dump();
  }

}

function statJoueur()
{
  $smarty = new MonSmarty($_SESSION['langage']);
  $joueur=$_SESSION["joueur"];
  
  $joueur->calculNbPartieTempsJouee();

  if($joueur->reqPartiesCompletes() > 0)
  {
    $smarty->assign('nbParties',$joueur->reqPartiesCompletes());
    $smarty->assign('temps',$joueur->reqTempsPartie());
    $smarty->assign('nbVictoire',$joueur->reqNbVictoire());
    $smarty->assign('totalPoints',$joueur->reqTotalPoints());
  	$smarty->assign('moyPoint',round($joueur->reqTotalPoints()/$joueur->reqTempsPartie(),2));
  	$smarty->assign('moyVictoire',round(($joueur->reqNbVictoire()/$joueur->reqPartiesCompletes())*100),0);
  	$smarty->assign('moyTempsParties',round($joueur->reqTempsPartie()/$joueur->reqPartiesCompletes()));
  }
  else
  {
    $smarty->assign('nbParties',0);
    $smarty->assign('temps',0);
    $smarty->assign('nbVictoire',0);
    $smarty->assign('totalPoints',0);
  	$smarty->assign('moyPoint',0);
  	$smarty->assign('moyVictoire',0);
  	$smarty->assign('moyTempsParties',0);
  }

  $smarty->cache_lifetime = 0;
  $smarty->display('stat_joueur.tpl');

  
}

/*******************************************************************************
Fonction : doModificationPerso
Param�tre : $joueur : le joueur courant
Description : valider les nouvelles informations personnelles du joueur.
    Enregistrer les modifications si tout est correct, sinon afficher
    le formulaire de modification avec le message d'erreur appropri�
*******************************************************************************/
function doModificationPerso($joueur)
{
    global $lang;
    
    if($_POST["prenom"]==""){
        formulaireModification($joueur,$lang['erreur_prenom'],1,"",0);
    }
    elseif($_POST["nom"]==""){
        formulaireModification($joueur,$lang['erreur_nom'],1,"",0);
    }
    elseif($_POST["ville"]==""){
        formulaireModification($joueur,$lang['erreur_ville'],1,"",0);
    }
    elseif($_POST["province"]==""){
        formulaireModification($joueur,$lang['erreur_province'],1,"",0);
    }
    elseif($_POST["pays"]==""){
        formulaireModification($joueur,$lang['erreur_pays'],1,"",0);
    }
    elseif(!Courriel::validerCourriel($_POST["courriel"])){
        formulaireModification($joueur,$lang['erreur_courriel'],1,"",0);
    }
    elseif($joueur->reqCourriel()!=$_POST["courriel"] &&
        $joueur->validerCourrielUnique($_POST["courriel"])==false){
        formulaireModification($joueur,$lang['doublon_courriel'],1,"",0);
    }
    else
    {
    	  //modification des informations personnelles
        $joueur->asgPrenom($_POST["prenom"]);
        $joueur->asgNom($_POST["nom"]);
        $joueur->asgVille($_POST["ville"]);
        $joueur->asgProvince($_POST["province"]);
        $joueur->asgPays($_POST["pays"]);
        $joueur->asgCourriel($_POST["courriel"]);
        $oldLangue = $joueur->reqCleLangue();
        $joueur->asgCleLangue($_POST["langue"]);
        $joueur->miseAJourMySQL();
        $_SESSION["joueur"] = $joueur;
        setLangage($joueur->reqCleLangue());
        formulaireModification($joueur,"",0,$lang['mod_joueur_personel_succes'],1);

    }
}

/*******************************************************************************
Fonction : doModificationPass
Param�tre : $joueur : le joueur courant
Description : valider le nouveau mot de passe du joueur.
    Enregistrer les modifications si tout est correct, sinon afficher
    le formulaire de modification avec le message d'erreur appropri�
*******************************************************************************/
function doModificationPass($joueur)
{
    global $lang;
    if(!$joueur->validerPassCrypter($joueur->reqMotDePasse(),$_POST["oldpass"]))
	{
        formulaireModification($joueur,$lang['ancien_mot_passe_invalide'],2,"",0);
    }
    elseif($_POST["newpass"] != $_POST["newpass2"])
	{
        formulaireModification($joueur,$lang['erreur_resaisie_mot_passe'],2,"",0);
    }
    elseif(!Utilisateur::validerMotDePasse($_POST["newpass"]))
	{
        formulaireModification($joueur,$lang['erreur_mot_passe'],2,"",0);
    }
    else
	{
        $joueur->miseAJourMotDePasseMySQL($_POST["newpass"]);
        $_SESSION["motDePasseJoueur"] = $joueur->reqMotDePasse();
        formulaireModification($joueur,"",0,$lang['mod_joueur_pass_succes'],2);
    }
}

/*******************************************************************************
Fonction : doModificationScolaire
Param�tre : $joueur : le joueur courant
Description : valider les nouvelles informations scolaire du joueur.
    Enregistrer les modifications si tout est correct, sinon afficher
    le formulaire de modification avec le message d'erreur appropri�
*******************************************************************************/
function doModificationScolaire($joueur)
{
    global $lang;
    if(!isset($_POST["etablissement"]))
	{
        $etablissement=0;
    }
    else
	{
        $etablissement=$_POST["etablissement"];
    }
	
	if(isset($_POST["aliasProf"]))
	{
		if(!$joueur->asgAdministrateur($_POST["aliasProf"]))
		{
	  		formulaireModification($joueur,$lang['alias_prof_introuvable'],3,"",0);
	  		return;
	  	}	
	}
	
    //modification des informations scolaires
    $joueur->asgNiveau($_POST["niveau"]);
    $joueur->asgEtablissement($etablissement);
    $joueur->miseAJourMySQL();
    formulaireModification($joueur,"",0,$lang['mod_joueur_scolaire_succes'],3);

}

/*******************************************************************************
Fonction : formulaireModification
Param�tre :
    $joueur : le joueur courant
    $erreur : le message d'erreur s'il y en a un
    $erreur_id : le num�ro d'erreur ( un pour chacune des sections )
    $message : un message s'il y en a
    $message_id : le num�ro de message ( un pour chacune des sections )
Description : afficher le formulaire qui permet de modifier
    les informations du joueur.
*******************************************************************************/
function formulaireModification($joueur,$erreur,$erreur_id,$message,$message_id)
{
	global $lang;

    if(!isset($_POST["niveau"]))
	{
      $niveau=$joueur->reqNiveau();
    }
    else
	{
      $niveau=$_POST["niveau"];
    }

    $smarty = new MonSmarty($_SESSION['langage']);
    
    if(isset($_POST["etablissement"]))
	{
        $smarty->assign('etablissement',$_POST['etablissement']);
    }
    else
	{
        $smarty->assign('etablissement',$joueur->reqEtablissement());
    }
    
    $smarty->assign('alias',$joueur->reqAlias());
    $smarty->assign('prenom',$joueur->reqPrenom());
    $smarty->assign('nom',$joueur->reqNom());
    $smarty->assign('ville',$joueur->reqVille());
    $smarty->assign('province',$joueur->reqProvince());
    $smarty->assign('pays',$joueur->reqPays());
    $smarty->assign('courriel',$joueur->reqCourriel());
    $smarty->assign('langue',$joueur->reqCleLangue());
    $smarty->assign('aliasProf',$joueur->reqAliasAdministrateur());
    $smarty->assign('niveau',$niveau);
    $smarty->assign('etablissement',$joueur->reqEtablissement());
    $smarty->assign('erreur'.$erreur_id,$erreur);
    $smarty->assign('message'.$message_id,$message);

    //
    // on g�n�re la liste des niveau scolaire
    // on enleve les niveau primaire,coll�gial,universitaire temporairement
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
        $smarty->assign('etablissement',$joueur->reqEtablissement());
    }
	
	$smarty->cache_lifetime = 0;
    $smarty->display('modification_profil_joueur.tpl');

}


?>
