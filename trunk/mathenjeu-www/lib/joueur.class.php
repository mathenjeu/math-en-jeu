<?php
/*******************************************************************************
Fichier : joueur.class.php
Auteur : Maxime Bégin
Description :
    classe qui hérite de la classe utilisateur. Elle gère les joueurs.
********************************************************************************
09-11-2006 Maxime Bégin - s'assurer que le nombre de caractères de l'alias 
	suggéré ne dépasse pas le nombre maximal de caractères autorisés.
14-08-2006 Maxime Bégin - Ajout du cryptage du mot de passe. Utilisation de la 
	fonction password() de MySQL.
11-07-2006 Maxime Bégin - modification des courriels pour qu'il fonctionne avec 
	le fichier de configuration.
21-06-2006 Maxime Bégin - ajout de quelques commentaires et petit changement
25-05-2006 Maxime Bégin - Version initiale
*******************************************************************************/

require_once("utilisateur.class.php");
require_once("courriel.class.php");
require_once("exception.class.php");
require_once("mon_mysqli.class.php");


class Joueur extends Utilisateur
{

    private $ville;
    private $pays;
    private $province;
    private $dateInscription;
    private $dateDernierAccess;
    private $aliasAdministrateur;
    private $cleAdministrateur;
    private $cleGroupe;
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
    // Entrée:          $un object mon_mysqli
    // Sortie:
    // Note:
    //**************************************************************************
    function Joueur($mysqli)
    {
      PRECONDITION(get_class($mysqli)=="mon_mysqli");
        parent::Utilisateur($mysqli);
        $this->cleGroupe=0;
        $this->cleAdministrateur=0;
    }

    //**************************************************************************
    // Sommaire:    Vérifier les invariants de la classe
    // Entrée:
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
    // Entrée:          $ville ne doit pas être vide
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
    // Sommaire:        Assigner un pays au joueur
    // Entrée:          $pays ne doit pas être vide
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
    // Entrée:          $province ne doit pas être vide
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
    // Entrée:          $dateInscription au format aaaa-mm-jj
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
    // Sommaire:        Assigner une clé de groupe au joueur
    // Entrée:          $cleGroupe doit être positive
    // Sortie:
    // Note:
    //**************************************************************************
    function asgCleGroupe($cleGroupe)
    {
      PRECONDITION($cleGroupe>=0);
      $this->cleGroupe=$cleGroupe;
      POSTCONDITION($this->reqCleGroupe()==$cleGroupe);
    }
    
    //fonction d'assignation pour le petit sondage à l'inscription
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
    
    //**************************************************************************
    // Sommaire:        Assigner un administrateur au joueur courant
    // Entrée:          $alias : l'alias du professeur associé
    // Sortie:          retourne faux si l'administrateur n'existe pas, vrai sinon
    // Note:            l'alias doit être dans la table administrateur
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
    // Sommaire:        Assigner un administrateur au joueur courant avec la clé
    //                  de l'administrateur
    // Entrée:          $cle : la clé de l'administrateur
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
    // Entrée:          $nom
    //                  $prenom
    //                  $alias
    //                  $motDePasse
    //                  $courriel
    //                  $estConfirmer   : 0 ou 1 pour dire si le courriel du
    //                                      joueur est confiré
    //                  $etablissement  : # de l'établissement choisie par le joueur
    //                  $niveau         : niveau scolaire entre 1 et 14
    //                  $ville
    //                  $pays
    //                  $province
    //                  $dateInscription : date d'inscription au format aaaa-mm-jj
    //                  $sondageQ1      : la réponse 1 au sondage lors de l'inscription
    //                  $sondageQ2      : la réponse 2 au sondage lors de l'inscription
    //                  $sondageQ3      : la réponse 3 au sondage lors de l'inscription
    //                  $sondageQ4      : la réponse 4 au sondage lors de l'inscription
    // Sortie:
    // Note:            on apelle la focntion parente pour une partie des données
    //**************************************************************************
    function asgJoueur($nom,$prenom,$alias,$motDePasse,$courriel,
        $estConfirmer,$etablissement,$niveau,$ville,$pays,$province,
        $dateInscription,$aimeMaths,$mathConsidere,$mathEtudie,$mathDecouvert)
    {
      
        parent::asgUtilisateur($nom,$prenom,$alias,$motDePasse,$courriel,
            $estConfirmer,$etablissement,$niveau);

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
    Paramètre :
        - $alias : l'alias voulu par le joueur pour lequel
          il faut trouver une suggestion
    Description :
        - trouver un alias non utilisé à partir d'un alias de base, il faut
          vérifier dans la base de données pour s'assurer
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
        //on vérifie si le nouveau nom d'utilisateur est valide, 
		//ie : il ne dépasse pas la longueur maximal pour un alias
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
    // Sommaire:        sert à valider un joueur nouvellement inscrit
    //                  avec sa clé de confirmation
    // Entrée:          $cleConfirmation
    // Sortie:          retourne faux si le joueur n'est pas trouvé,
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
    //                  et le temps joués par ce joueur
    // Entrée:          
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
    // Sommaire:        charger un joueur de la base de donnée MySQL correspondant
    //                  aux alias et mot de passe en entrée
    // Entrée:          $alias
    //                  $motDePasse
    // Sortie:          retourne faux si le joueur n'est pas trouvé,
    //                  vrai si tout va bien
    // Note:            
    //**************************************************************************
    function chargerMySQL($alias,$motDePasse)
    {

        $sql="select cleJoueur,motDePasse from joueur where alias='$alias' and motDePasse=password('" . $motDePasse . "')";
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
    // Sommaire:        charger un joueur de la base de donnée MySQL correspondant
    //                  à la clé
    // Entrée:          $cle
    // Sortie:          retourne faux si le joueur n'est pas trouvé,vrai s'il l'est
    // Note:            
    //**************************************************************************
    function chargerMySQLCle($cle)
    {

        $sql="select * from joueur where cleJoueur=" . $cle;
        $result=$this->mysqli->query($sql);

        if($result->num_rows==0)
            return false;

        $row=$result->fetch_object();
        $this->asgJoueur($row->nom,$row->prenom,$row->alias,$row->motDePasse,
        $row->adresseCourriel,$row->estConfirme,$row->cleEtablissement,$row->cleNiveau,
        $row->ville,$row->pays,$row->province,$row->dateInscription,
        $row->sondageQ1,$row->sondageQ2,$row->sondageQ3,$row->sondageQ4);
        
        $this->asgCle($row->cleJoueur);
        $this->asgAdministrateurCle($row->cleAdministrateur);
        $this->cleGroupe=$row->cleGroupe;
        $this->calculNbPartieTempsJouee();
        $this->cleConfirmation=$row->cleConfirmation;

        return true;

    }
    
    //**************************************************************************
    // Sommaire:        Générer une clé unique de confirmation pour le courriel
    // Entrée:          
    // Sortie:
    // Note:            On s'assure que la cé générée est unique
    //                  dans la table joueur avant de l'assigné
    //**************************************************************************
    function genererCleConfirmation()
    {

        //on s'assure que la cle qui servira à la confirmation
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
    // Sommaire:    Insérer le joueur nouvellement inscrit dans la base de données
    // Entrée:
    // Sortie:      retourne faux si l'alias ou le mot de pass ne sont pas unique
    // Note:        On doit vérifier que l'alias est unique et
    //              que le courriel est unique
    //**************************************************************************
    function insertionMySQL()
    {
      $this->genererCleConfirmation();
      $this->asgDateInscription(date("Y-m-d"));
      
      $this->INVARIANTS();

	  //echo "test";
      //vérifier la possibilité de doublon
      if($this->validerAliasUnique($this->reqAlias())==false
            || $this->validerCourrielUnique($this->reqCourriel())==false)
        return false;
	

      $sql = "INSERT INTO joueur (prenom, nom, alias,
            motDePasse, adresseCourriel, ville, province, pays, cleNiveau,
            sondageQ1, sondageQ2, sondageQ3, sondageQ4, dateInscription,
            cleConfirmation,cleEtablissement,cleGroupe,cleAdministrateur) VALUES(";
      
      $sql .= "'"
            .$this->reqPrenom()."', '"
            .$this->reqNom()."', '"
            .$this->reqAlias()."',password('"
            .$this->reqMotdepasse()."'), '"
            .$this->reqCourriel()."', '"
            .$this->reqVille()."','"
            .$this->reqProvince()."', '"
            .$this->reqPays()."', "
            .$this->reqNiveau().","
            .$this->reqAimeMaths().", "
            .$this->reqMathConsidere().", "
            .$this->reqMathEtudie().", "
            .$this->reqMathDecouvert().",'"
            .$this->reqDateInscription()."','"
            .$this->reqCleConfirmation() . "',"
            .$this->reqEtablissement() . ","
            .$this->reqCleGroupe() . ","
            .$this->reqCleAdministrateur() . ")";

        $result=$this->mysqli->query($sql);
        $this->asgCle($this->mysqli->insert_id);
        //$this->envoyerCourrielConfirmation();

      
      $this->INVARIANTS();
      return true;
    }
    


    //**************************************************************************
    // Sommaire:    vérifier que personne n'a déjà choisie cet alias
    // Entrée:      $alias
    // Sortie:      retourne vrai si l'alias est libre faux sinon
    // Note:        
    //**************************************************************************
    function validerAliasUnique($alias)
    {

        $sql="select * from joueur where alias='" . strtolower($alias) . "'";
        $result=$this->mysqli->query($sql);

        if($result->num_rows!=0)
            return false;
        else
            return true;

    }
    
    //**************************************************************************
    // Sommaire:    vérifier que personne n'a déjà choisie ce courriel
    // Entrée:      $courriel
    // Sortie:      retourne vrai si le courriel est libre, faux sinon
    // Note:        
    //**************************************************************************
    function validerCourrielUnique($courriel)
    {

        $sql="select * from joueur where adresseCourriel='" . strtolower($courriel) . "'";
        $result=$this->mysqli->query($sql);
        if($result->num_rows!=0)
            return false;
        else
            return true;

    }

    //**************************************************************************
    // Sommaire:    envoyer un courriel de confirmation
    //              pour valider l'adresse courriel
    // Entrée:
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
    //				un nouveau mot de passe est généré
    // Entrée:
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
    // Sommaire:    Mettre à jour les données du joueur courant.
    // Entrée:
    // Sortie:
    // Note:        la clé doit être valide
    //**************************************************************************
    function miseAJourMySQL()
    {
      $this->INVARIANTS();
      PRECONDITION($this->reqCle()>=0);

      $sql ="update joueur set nom='" . $this->reqNom() .
            "',prenom='" . $this->reqPrenom() .
            "',ville='" . $this->reqVille() .
            "',pays='" .$this->reqPays() .
            "',province='" . $this->reqProvince() .
            "',cleNiveau=" . $this->reqNiveau() .
            ",motDePasse='" . $this->reqMotDePasse() .
            "',cleEtablissement=" . $this->reqEtablissement() .
            ",adresseCourriel='" . $this->reqCourriel() .
            "',cleConfirmation='" . $this->reqCleConfirmation() .
            "',estConfirme=" . $this->reqEstConfirmer() .
            ",alias='" . $this->reqAlias() .
            "',cleAdministrateur=" . $this->reqCleAdministrateur() .
            ",cleGroupe=" . $this->reqCleGroupe() .
            ",cleConfirmation='" . $this->reqCleConfirmation() . "'" .
            " where cleJoueur=" . $this->reqCle();

        $result=$this->mysqli->query($sql);

      
      $this->INVARIANTS();

    }
    
    //**************************************************************************
    // Sommaire:    Mettre à jour le mot de passe avec la fonction password() de MySQL
    // Entrée:
    // Sortie:
    // Note:        
    //**************************************************************************
    function miseAJourMotDePasseMySQL($motDePasse)
    {
      $sql ="update joueur set motDePasse=password('" . $motDePasse . "') where cleJoueur=" . $this->reqCle();
      $result=$this->mysqli->query($sql);
      $this->chargerMySQLCle($this->reqCle());
	}
	

    //**************************************************************************
    // Sommaire:    Supprimer le joueur courant
    // Entrée:
    // Sortie:
    // Note:        la clé doit être valide
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

