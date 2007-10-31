<?php
/*******************************************************************************
Fichier : joueur.class.php
Auteur : Maxime B�gin
Description :
    classe qui h�rite de la classe utilisateur. Elle g�re les joueurs.
********************************************************************************
09-11-2006 Maxime B�gin - s'assurer que le nombre de caract�res de l'alias 
	sugg�r� ne d�passe pas le nombre maximal de caract�res autoris�s.
14-08-2006 Maxime B�gin - Ajout du cryptage du mot de passe. Utilisation de la 
	fonction password() de MySQL.
11-07-2006 Maxime B�gin - modification des courriels pour qu'il fonctionne avec 
	le fichier de configuration.
21-06-2006 Maxime B�gin - ajout de quelques commentaires et petit changement
25-05-2006 Maxime B�gin - Version initiale
*******************************************************************************/

require_once("utilisateur.class.php");
require_once("courriel.class.php");
require_once("exception.class.php");
require_once("mon_mysqli.class.php");


class Joueur extends Utilisateur
{

    private $ville;
    private $sexe;
    private $pays;
    private $province;
    private $dateInscription;
    private $dateDernierAccess;
    private $aliasAdministrateur;
    private $cleAdministrateur;
    private $cleGroupe;
    private $cleLangue;
    //var $peutCreerSalle;
    
    private $partiesCompletes;
    private $tempsPartie;
    private $nbVictoire;
    private $totalPoints;
    
    private $aimeMaths;
    private $mathConsidere;
    private $mathEtudie;
    private $mathDecouvert;


    //**************************************************************************
    // Sommaire:        Constructeur, appel du constructeur parent
    // Entr�e:          $un object mon_mysqli
    // Sortie:
    // Note:
    //**************************************************************************
    function Joueur($mysqli)
    {
        PRECONDITION(get_class($mysqli)=="mon_mysqli");
      	$this->aimeMaths = 3;
        $this->mathConsidere = 3;
        $this->mathEtudie = 3;
        $this->mathDecouvert = 5;
        $this->cleGroupe=0;
        $this->cleAdministrateur=0;
        parent::Utilisateur($mysqli);


    }

    //**************************************************************************
    // Sommaire:    V�rifier les invariants de la classe
    // Entr�e:
    // Sortie:
    // Note:
    //**************************************************************************
    private function INVARIANTS()
    {

      INVARIANT(strlen($this->ville)>0);
      INVARIANT(strlen($this->pays)>0);
      INVARIANT(strlen($this->province)>0);
      
      $dateEx = explode("-",$this->dateInscription);
      INVARIANT(checkdate($dateEx[1],$dateEx[2],$dateEx[0]));
      INVARIANT($this->cleGroupe>=0);

    }
    
    //**************************************************************************
    // Sommaire:        Assigner une ville au joueur
    // Entr�e:          $ville ne doit pas �tre vide
    // Sortie:
    // Note:
    //**************************************************************************
    function asgVille($ville)
    {
      PRECONDITION(strlen($ville)>0);
      $this->ville=$ville;
      POSTCONDITION($this->reqVille()==$ville);
    }

    //**************************************************************************
    // Sommaire:        Assigner un sexe au joueur
    // Entr�e:          $sexe doit �tre �gal � 0 ou 1
    // Sortie:
    // Note:
    //**************************************************************************
    function asgSexe($sexe)
    {
		PRECONDITION($sexe==0 || $sexe==1 || $sexe==null);
		$this->sexe=$sexe;
		POSTCONDITION($this->reqSexe()==$sexe);
		
	}
    
    //**************************************************************************
    // Sommaire:        Assigner un pays au joueur
    // Entr�e:          $pays ne doit pas �tre vide
    // Sortie:
    // Note:
    //**************************************************************************
    function asgPays($pays)
    {
      PRECONDITION(strlen($pays)>0);
      $this->pays=$pays;
      POSTCONDITION($this->reqPays()==$pays);
    }
    
    //**************************************************************************
    // Sommaire:        Assigner une province au joueur
    // Entr�e:          $province ne doit pas �tre vide
    // Sortie:
    // Note:
    //**************************************************************************
    function asgProvince($province)
    {
      PRECONDITION(strlen($province)>0);
      $this->province=$province;
      POSTCONDITION($this->reqProvince()==$province);
    }
    

    //**************************************************************************
    // Sommaire:        Assigner une date d'inscription au joueur
    // Entr�e:          $dateInscription au format aaaa-mm-jj
    // Sortie:
    // Note:            
    //**************************************************************************
    function asgDateInscription($dateInscription)
    {
      $dateEx = explode("-",$dateInscription);
      PRECONDITION(checkdate($dateEx[1],$dateEx[2],$dateEx[0]));
      $this->dateInscription = $dateInscription;
      POSTCONDITION($this->reqDateInscription()==$dateInscription);
    }
    
    //**************************************************************************
    // Sommaire:        Assigner une cl� de groupe au joueur
    // Entr�e:          $cleGroupe doit �tre positive
    // Sortie:
    // Note:
    //**************************************************************************
    function asgCleGroupe($cleGroupe)
    {
      PRECONDITION($cleGroupe>=0);
      $this->cleGroupe=$cleGroupe;
      POSTCONDITION($this->reqCleGroupe()==$cleGroupe);
    }
    
    
    //fonction d'assignation pour le petit sondage � l'inscription
    function asgAimeMaths($no)
    {
        $this->aimeMaths=$no;
    }
    function asgMathConsidere($no)
    {
        $this->mathConsidere=$no;
    }
    function asgMathEtudie($no)
    {
        $this->mathEtudie=$no;
    }
    function asgMathDecouvert($no)
    {
        $this->mathDecouvert=$no;
    }
    
    function asgCleLangue($cleLangue) {
      PRECONDITION($cleLangue == 0 || $cleLangue == 1);
      $this->cleLangue = $cleLangue;
    }
    
    //**************************************************************************
    // Sommaire:        Assigner un administrateur au joueur courant
    // Entr�e:          $alias : l'alias du professeur associ�
    // Sortie:          retourne faux si l'administrateur n'existe pas, vrai sinon
    // Note:            l'alias doit �tre dans la table administrateur
    //**************************************************************************
    function asgAdministrateur($alias)
    {
      if($alias!="")
      {
        $sql="select cleAdministrateur from administrateur where alias='" . $alias ."'";
        $result=$this->mysqli->query($sql);
            
        if($result->num_rows==0)
            return false;

        $row=$result->fetch_object();
        $this->aliasAdministrateur=$alias;
        $this->cleAdministrateur=$row->cleAdministrateur;
      }
      else
      {
        $this->aliasAdministrateur="";
        $this->cleAdministrateur=0;
      }
      return true;
      
    }

    //**************************************************************************
    // Sommaire:        Assigner un administrateur au joueur courant avec la cl�
    //                  de l'administrateur
    // Entr�e:          $cle : la cl� de l'administrateur
    // Sortie:          
    // Note:            
    //**************************************************************************
    function asgAdministrateurCle($cle)
    {

        $sql="select alias from administrateur where cleAdministrateur=" . $cle;
        $result=$this->mysqli->query($sql);
        if($result->num_rows>0)
        {
            $row=$result->fetch_object();
            $this->aliasAdministrateur=$row->alias;
            $this->cleAdministrateur=$cle;
        }
        else
        {
            $this->aliasAdministrateur="";
            $this->cleAdministrateur=0;
        }

    }
    
    //**************************************************************************
    // Sommaire:        assigner toutes les informations au joueur
    // Entr�e:          $nom
    //                  $prenom
    //                  $alias
    //                  $motDePasse
    //                  $courriel
    //                  $estConfirmer   : 0 ou 1 pour dire si le courriel du
    //                                      joueur est confir�
    //                  $etablissement  : # de l'�tablissement choisie par le joueur
    //                  $niveau         : niveau scolaire entre 1 et 14
    //                  $ville
    //                  $pays
    //                  $province
    //                  $dateInscription : date d'inscription au format aaaa-mm-jj
    //					$categorie 		: la cl� de la cat�gorie du joueur
    //                  $sondageQ1      : la r�ponse 1 au sondage lors de l'inscription
    //                  $sondageQ2      : la r�ponse 2 au sondage lors de l'inscription
    //                  $sondageQ3      : la r�ponse 3 au sondage lors de l'inscription
    //                  $sondageQ4      : la r�ponse 4 au sondage lors de l'inscription
    // Sortie:
    // Note:            on apelle la focntion parente pour une partie des donn�es
    //**************************************************************************
    function asgJoueur($nom,$prenom,$alias,$motDePasse,$courriel,
        $estConfirmer,$etablissement,$niveau,$sexe,$ville,$pays,$province,
        $dateInscription,$categorie,$aimeMaths,$mathConsidere,$mathEtudie,$mathDecouvert)
    {
      
        parent::asgUtilisateur($nom,$prenom,$alias,$motDePasse,$courriel,
            $estConfirmer,$etablissement,$niveau,$categorie);
		
		$this->asgSexe($sexe);
        $this->asgVille($ville);
        $this->asgPays($pays);
        $this->asgProvince($province);
        $this->asgDateInscription($dateInscription);
        
        $this->aimeMaths = $aimeMaths;
        $this->mathConsidere=$mathConsidere;
        $this->mathEtudie=$mathEtudie;
        $this->mathDecouvert=$mathDecouvert;
        
        $this->INVARIANTS();
    }

    /*******************************************************************************
    Fonction : suggestionAlias($alias)
    Param�tre :
        - $alias : l'alias voulu par le joueur pour lequel
          il faut trouver une suggestion
    Description :
        - trouver un alias non utilis� � partir d'un alias de base, il faut
          v�rifier dans la base de donn�es pour s'assurer
          que l'alias est effectivement libre
    *******************************************************************************/
    function suggestionAlias($alias)
    {
      settype($i,"integer");
      $i=1;
      $nouvelAlias="";
      do
      {
        $nouvelAlias = $alias . $i;
        //on v�rifie si le nouveau nom d'utilisateur est valide, 
		//ie : il ne d�passe pas la longueur maximal pour un alias
        if(!Utilisateur::validerAlias($nouvelAlias))
        {
         $nouvelAlias = substr($nouvelAlias,0,strlen($nouvelAlias)-strlen($i)-1).$i;
		}
        $sql = "select alias from joueur where alias='" . $nouvelAlias . "'";
        $result = $this->mysqli->query($sql);
        $i = $i+1;
      }while($result->num_rows>0);

      return $nouvelAlias;

    }
    
    //**************************************************************************
    // Sommaire:        sert � valider un joueur nouvellement inscrit
    //                  avec sa cl� de confirmation
    // Entr�e:          $cleConfirmation
    // Sortie:          retourne faux si le joueur n'est pas trouv�,
    //                  vrai si tout va bien
    // Note:
    //**************************************************************************
    function validerConfirmation($cleConfirmation)
    {
      $sql="select cleJoueur from joueur where cleConfirmation='" . $cleConfirmation . "'";
      $result = $this->mysqli->query($sql);
      if($result->num_rows==0)
        return false;
      else
      {
        $row=$result->fetch_array();
        $this->chargerMySQLCle($row[0]);
        $this->asgEstConfirmer(1);
        $this->cleConfirmation="";
        $this->miseAJourMySQL();
        return true;
      }
    }

    //**************************************************************************
    // Sommaire:        calculer le nombre de partie, le nombre de victoire
    //                  et le temps jou�s par ce joueur
    // Entr�e:          
    // Sortie:          
    // Note:
    //**************************************************************************
    function calculNbPartieTempsJouee()
    {

        $sql="SELECT sum( partie.dureePartie ) AS temps,
                count( partie.clePartie ) AS nbPartie,
                sum( partiejoueur.gagner ) AS victoire,
                sum( partiejoueur.pointage) AS totalPoints
              FROM partiejoueur, partie
              WHERE partiejoueur.clePartie = partie.clePartie
              AND cleJoueur=" . $this->reqCle();
              
        $result=$this->mysqli->query($sql);
        $row=$result->fetch_object();
        $this->nbVictoire=$row->victoire;
        $this->partiesCompletes=$row->nbPartie;
        $this->tempsPartie=$row->temps;
        $this->totalPoints=$row->totalPoints;

    }

    //**************************************************************************
    // Sommaire:        charger un joueur de la base de donn�e MySQL correspondant
    //                  aux alias et mot de passe en entr�e
    // Entr�e:          $alias
    //                  $motDePasse
    // Sortie:          retourne faux si le joueur n'est pas trouv�,
    //                  vrai si tout va bien
    // Note:            
    //**************************************************************************
    function chargerMySQL($alias,$motDePasse)
    {

        $sql="select cleJoueur,motDePasse from joueur where alias='" . addslashes($alias) . "' and motDePasse=password('" . addslashes($motDePasse) . "')";
        $result=$this->mysqli->query($sql);

        if($result->num_rows==0)
        {
            return false;
        }
        else
        {
		  $row=$result->fetch_object();
		  $this->chargerMySQLCle($row->cleJoueur);
		  return true;
		}
    }

    //**************************************************************************
    // Sommaire:        charger un joueur de la base de donn�e MySQL correspondant
    //                  � la cl�
    // Entr�e:          $cle
    // Sortie:          retourne faux si le joueur n'est pas trouv�,vrai s'il l'est
    // Note:            
    //**************************************************************************
    function chargerMySQLCle($cle)
    {

        $sql="select * from joueur where cleJoueur=" . $cle;
        $result=$this->mysqli->query($sql);

        if($result->num_rows==0)
            return false;

        $row=$result->fetch_object();
        $this->asgJoueur(
			stripcslashes($row->nom),
			stripcslashes($row->prenom),
			$row->alias,
			stripcslashes($row->motDePasse),
	        $row->adresseCourriel,
			$row->estConfirme,
			$row->cleEtablissement,
			$row->cleNiveau,
			$row->sexe,
	        stripcslashes($row->ville),
			stripcslashes($row->pays),
			stripcslashes($row->province),
			$row->dateInscription,
			$row->cleCategorie,
	        $row->sondageQ1,
			$row->sondageQ2,
			$row->sondageQ3,
			$row->sondageQ4);
        
        $this->asgCle($row->cleJoueur);
        $this->asgAdministrateurCle($row->cleAdministrateur);
        $this->cleGroupe=$row->cleGroupe;
        $this->calculNbPartieTempsJouee();
        $this->cleConfirmation=$row->cleConfirmation;

	    $this->cleLangue=$row->cleLangue;
	

        return true;

    }
    
    //**************************************************************************
    // Sommaire:        G�n�rer une cl� unique de confirmation pour le courriel
    // Entr�e:          
    // Sortie:
    // Note:            On s'assure que la c� g�n�r�e est unique
    //                  dans la table joueur avant de l'assign�
    //**************************************************************************
    function genererCleConfirmation()
    {

        //on s'assure que la cle qui servira � la confirmation
        //du courriel soit unique
        do
        {
            $cle = $this->genererChaineAleatoire(30);
            $sql = "select cleConfirmation from joueur where cleConfirmation='" . $cle . "'";
            $result=$this->mysqli->query($sql);
        }
        while($result->num_rows!=0);
        $this->cleConfirmation = $cle;

    }

    //**************************************************************************
    // Sommaire:    Ins�rer le joueur nouvellement inscrit dans la base de donn�es
    // Entr�e:
    // Sortie:      retourne faux si l'alias ou le mot de pass ne sont pas unique
    // Note:        On doit v�rifier que l'alias est unique et
    //              que le courriel est unique
    //**************************************************************************
    function insertionMySQL()
    {
      $this->genererCleConfirmation();
      $this->asgDateInscription(date("Y-m-d"));
      
      $this->INVARIANTS();

	  //echo "test";
      //v�rifier la possibilit� de doublon
      if($this->validerAliasUnique($this->reqAlias())==false
            || $this->validerCourrielUnique($this->reqCourriel())==false)
        return false;


      $sql = "INSERT INTO joueur (prenom, nom, alias,
            motDePasse, adresseCourriel, ville, province, pays, cleNiveau, sexe,
            sondageQ1, sondageQ2, sondageQ3, sondageQ4, dateInscription,
            cleConfirmation,estConfirme,cleEtablissement,cleGroupe,cleAdministrateur,cleLangue) VALUES(";
      
      $sql .= "'"
            .addslashes($this->reqPrenom())."', '"
            .addslashes($this->reqNom())."', '"
            .$this->reqAlias()."',password('"
            .addslashes($this->reqMotdepasse())."'), '"
            .$this->reqCourriel()."', '"
            .addslashes($this->reqVille())."','"
            .addslashes($this->reqProvince())."', '"
            .addslashes($this->reqPays())."', "
            .$this->reqNiveau().","
            .$this->reqSexe().","
            .$this->reqAimeMaths().", "
            .$this->reqMathConsidere().", "
            .$this->reqMathEtudie().", "
            .$this->reqMathDecouvert().",'"
            .$this->reqDateInscription()."','"
            .$this->reqCleConfirmation() . "',0,"
            .addslashes($this->reqEtablissement()) . ","
            .$this->reqCleGroupe() . ","
            .$this->reqCleAdministrateur() . ","
            .$this->reqCleLangue() . ")";

        $result=$this->mysqli->query($sql);
        $this->asgCle($this->mysqli->insert_id);
        $this->asgEstConfirmer(0);
        //$this->envoyerCourrielConfirmation();

      
      $this->INVARIANTS();
      return true;
    }
    


    //**************************************************************************
    // Sommaire:    v�rifier que personne n'a d�j� choisie cet alias
    // Entr�e:      $alias
    // Sortie:      retourne vrai si l'alias est libre faux sinon
    // Note:        
    //**************************************************************************
    function validerAliasUnique($alias)
    {

        $sql="select * from joueur where alias='" . addslashes(strtolower($alias)) . "'";
        $result=$this->mysqli->query($sql);

        if($result->num_rows!=0)
            return false;
        else
            return true;

    }
    
    //**************************************************************************
    // Sommaire:    v�rifier que personne n'a d�j� choisie ce courriel
    // Entr�e:      $courriel
    // Sortie:      retourne vrai si le courriel est libre, faux sinon
    // Note:        
    //**************************************************************************
    function validerCourrielUnique($courriel)
    {
      if ($courriel != "") {
        $sql="select * from joueur where adresseCourriel='" . strtolower($courriel) . "'";
        $result=$this->mysqli->query($sql);
        if($result->num_rows!=0)
            return false;
        else
            return true;
      } else {
        return true;
      }

    }

    //**************************************************************************
    // Sommaire:    envoyer un courriel de confirmation
    //              pour valider l'adresse courriel
    // Entr�e:
    // Sortie:		retourne vrai si tout va bien,faux sinon
    // Note:        
    //**************************************************************************
    function envoyerCourrielConfirmation()
    {
        $sujet = SUJET_COURRIEL_INSCRIPTION;
        
        $a_chercher = array('[ALIAS]','[MOT_DE_PASSE]','[NOM]','[PRENOM]','[CLE_CONFIRMATION]','[ADRESSE_SITE_WEB]');
        $remplacement = array($this->reqAlias(),$this->reqMotDePasse(),$this->reqNom(),$this->reqPrenom(),$this->reqCleConfirmation(),ADRESSE_SITE_WEB);
        $message = str_replace($a_chercher,$remplacement,COURRIEL_INSCRIPTION);

        $courriel = new Courriel($sujet,$message,$this->reqCourriel());
        return $courriel->envoyerCourriel();
    }
    
    //**************************************************************************
    // Sommaire:    envoyer un courriel avec les information de connexion
    //				un nouveau mot de passe est g�n�r�
    // Entr�e:
    // Sortie:      on retourne faux si l'adresse courriel n'existe pas
    // Note:
    //**************************************************************************
    function envoyerCourrielInfoPerdu($courriel)
    {
      	if($this->validerCourrielUnique($courriel)==true)
        	return false;

        $sql="select * from joueur where adresseCourriel='" . $courriel . "'";
        $result=$this->mysqli->query($sql);
        $row = $result->fetch_object();
        
        $this->chargerMySQLCle($row->cleJoueur);
        
        $sujet = SUJET_COURRIEL_PASS_PERDU;
        $nouveauPass = $this->genererChaineAleatoire(10);
        $this->miseAJourMotDePasseMySQL($nouveauPass);
                
        $a_chercher = array('[ALIAS]','[MOT_DE_PASSE]','[NOM]','[PRENOM]','[CLE_CONFIRMATION]','[ADRESSE_SITE_WEB]');
        $remplacement = array($row->alias,$nouveauPass,$this->reqNom(),$this->reqPrenom(),$this->reqCleConfirmation(),ADRESSE_SITE_WEB);
        $message = str_replace($a_chercher,$remplacement,COURRIEL_PASS_PERDU);
		
        $courriel = new Courriel($sujet,$message,$courriel);
        $courriel->envoyerCourriel();
        return true;
    }

    //**************************************************************************
    // Sommaire:    Mettre � jour les donn�es du joueur courant.
    // Entr�e:
    // Sortie:
    // Note:        la cl� doit �tre valide
    //**************************************************************************
    function miseAJourMySQL()
    {
      $this->INVARIANTS();
      PRECONDITION($this->reqCle()>=0);

      $sql ="update joueur set nom='" . addslashes($this->reqNom()) .
            "',prenom='" . addslashes($this->reqPrenom()) .
            "',ville='" . addslashes($this->reqVille()) .
            "',pays='" .addslashes($this->reqPays()) .
            "',province='" . addslashes($this->reqProvince()) .
            "',cleNiveau=" . $this->reqNiveau() .
            ",motDePasse='" . addslashes($this->reqMotDePasse()) .
            "',cleEtablissement=" . addslashes($this->reqEtablissement()) .
            ",adresseCourriel='" . $this->reqCourriel() .
            "',cleConfirmation='" . $this->reqCleConfirmation() .
            "',estConfirme=" . $this->reqEstConfirmer() .
            ",alias='" . $this->reqAlias() .
            "',sexe=" . $this->reqSexe() .
            ",cleLangue=" . $this->reqCleLangue() .
            ",cleAdministrateur=" . $this->reqCleAdministrateur() .
            ",cleGroupe=" . $this->reqCleGroupe() .
            ",sondageQ1=" . $this->reqAimeMaths() .
            ",sondageQ2=" . $this->reqMathConsidere() .
            ",sondageQ3=" . $this->reqMathEtudie() .
            ",sondageQ4=" . $this->reqMathDecouvert() .
            " where cleJoueur=" . $this->reqCle();

        $result=$this->mysqli->query($sql);

      
      $this->INVARIANTS();

    }
    
    //**************************************************************************
    // Sommaire:    Mettre � jour le mot de passe avec la fonction password() de MySQL
    // Entr�e:
    // Sortie:
    // Note:        
    //**************************************************************************
    function miseAJourMotDePasseMySQL($motDePasse)
    {
      $sql ="update joueur set motDePasse=password('" . addslashes($motDePasse) . "') where cleJoueur=" . $this->reqCle();
      $result=$this->mysqli->query($sql);
      $this->chargerMySQLCle($this->reqCle());
	}
	

    //**************************************************************************
    // Sommaire:    Supprimer le joueur courant
    // Entr�e:
    // Sortie:
    // Note:        la cl� doit �tre valide
    //**************************************************************************
    function deleteMySQL()
    {
      PRECONDITION($this->reqCle()>=0);

        $sql="delete from joueur where cleJoueur=" . $this->reqCle();
        $result=$this->mysqli->query($sql);


    }

    //************************
    //les fonctions de retour.
    //************************
    function reqCleConfirmation()
    {
      return $this->cleConfirmation;
    }
    function reqSexe()
    {
		return $this->sexe;
	}
    function reqVille()
    {
      return $this->ville;
    }
    function reqProvince()
    {
      return $this->province;
    }
    function reqPays()
    {
      return $this->pays;
    }
    function reqDateInscription()
    {
      return $this->dateInscription;
    }
    function reqDateDernierAccess()
    {
      return $this->dateDernierAccess;
    }
    function reqAimeMaths()
    {
      return $this->aimeMaths;
    }
    function reqMathConsidere()
    {
      return $this->mathConsidere;
    }
    function reqMathEtudie()
    {
      return $this->mathEtudie;
    }
    function reqMathDecouvert()
    {
      return $this->mathDecouvert;
    }
    function reqAliasAdministrateur()
    {
      return $this->aliasAdministrateur;
    }
    function reqCleAdministrateur()
    {
      return $this->cleAdministrateur;
    }
    function reqCleLangue()
    {
      return $this->cleLangue;
    }
    function reqCleGroupe()
    {
      return $this->cleGroupe;
    }
    function reqPartiesCompletes()
    {
        return $this->partiesCompletes;
    }
    function reqTempsPartie()
    {
        return $this->tempsPartie;
    }
    function reqNbVictoire()
    {
      return $this->nbVictoire;
    }
    function reqTotalPoints()
    {
	  return $this->totalPoints;
	}

}

