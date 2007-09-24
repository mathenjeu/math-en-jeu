<?php
/*******************************************************************************
Fichier : groupe.class.php
Auteur : Maxime Bégin
Description : classes pour la gestion des groupes

TODO :  - décider de la durée maximale des parties pour les groupes
        - les banques de questions
********************************************************************************
21-06-2006 Maxime Bégin - Ajout de commentaires.
30-05-2006 Maxime Bégin - Version initiale
*******************************************************************************/

require_once("joueur.class.php");
require_once("exception.class.php");
require_once("mon_mysqli.class.php");

//classe UnGroupe
class UnGroupe
{
  private $mysqli;              //objet monmysqli
  private $listeJoueur;         //liste de joueur faisant partie du groupe
  private $nbJoueur;            //nombre de joueur dans le groupe
  
  private $cle;                 //numéro unique du groupe
  private $dureePartie;         //durée des parties pour ce groupe
  private $clavardage;          //clavardage permis ou non
  private $nom;                 //le nom du groupe
  private $banqueQuestions;     //le numéro de banque de questions
  private $cleAdministrateur;   //la clé de l'administrateur
  
  
  //****************************************************************************
  // Sommaire:    Constructeur de la classe UnGroupe
  // Entrée:
  // Sortie:
  // Note:
  //****************************************************************************
  function UnGroupe($mysqli)
  {
    PRECONDITION(get_class($mysqli)=="mon_mysqli");
    $this->mysqli=$mysqli;
    $this->nbJoueur=0;
    $this->cle=0;
    $this->dureePartie=20;
    $this->clavardage=true;
    $this->nom="AUCUN";
    $this->banqueQuestions=0;
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
    INVARIANT($this->cle>=0);
    INVARIANT($this->dureePartie>0);
    INVARIANT($this->clavardage==0 || $this->clavardage==1);
    INVARIANT($this->nom!="");
    INVARIANT($this->cleAdministrateur>=0);
  }
  
  //****************************************************************************
  // Sommaire:      Assigne une clé au groupe courant
  // Entrée:        $cle :  la clé doit être positive
  // Sortie:
  // Note:
  //****************************************************************************
  function asgCle($cle)
  {
    PRECONDITION($cle>=0);
    $this->cle=$cle;
    POSTCONDITION($this->reqCle()==$cle);
  }

  //****************************************************************************
  // Sommaire:      Assigne une durée au groupe courant
  // Entrée:        $durée : doit être > 0 et inférieur à 120 ???
  // Sortie:
  // Note:
  //****************************************************************************
  function asgDureePartie($duree)
  {
    PRECONDITION($duree>0);
    //PRECONDITION($duree<120);
    $this->dureePartie=$duree;
    POSTCONDITION($this->reqDureePartie()==$duree);
  }

  //****************************************************************************
  // Sommaire:      Définir si le clavardage est permis ou non
  // Entrée:        $clavardage : est = à 1 ou 0
  // Sortie:
  // Note:
  //****************************************************************************
  function asgClavardage($bool)
  {
    PRECONDITION($bool==0 || $bool==1);
    $this->clavardage=$bool;
    POSTCONDITION($this->reqClavardage()==$bool);
  }

  //****************************************************************************
  // Sommaire:      Assigne un nom au groupe courant
  // Entrée:        $nom :  ne doit pas être vide
  // Sortie:
  // Note:
  //****************************************************************************
  function asgNom($nom)
  {
    PRECONDITION($nom!="");
    $this->nom=$nom;
    POSTCONDITION($this->reqNom()==$nom);
  }

  //****************************************************************************
  // Sommaire:      Assigne une banque de questions au groupe courant
  // Entrée:        $banque : un numéro de banque de questions
  // Sortie:
  // Note:
  //****************************************************************************
  function asgBanqueQuestions($banque)
  {
    //à revoir
    $this->banqueQuestions=$banque;
  }
  
  //****************************************************************************
  // Sommaire:      Assigne une cle d'administrateur au groupe
  // Entrée:        $cle : la clé de l'administrateur
  // Sortie:
  // Note:
  //****************************************************************************
  function asgCleAdministrateur($cle)
  {
    PRECONDITION($cle>=0);
    $this->cleAdministrateur=$cle;
    POSTCONDITION($this->reqCleAdministrateur()==$cle);
  }

  //****************************************************************************
  // Sommaire:      Assigne toutes les informations au groupe
  // Entrée:        $nom
  //                $clavardage
  //                $dureePartie
  //                $banqueQuestions
  //                $cleGroupe
  //                $cleAdministrateur
  // Sortie:
  // Note:          on appel les fonctions d'assignation simple
  //****************************************************************************
  function asgGroupe($nom,$clavardage,$dureePartie,$banqueQuestions,$cleGroupe,$cleAdministrateur)
  {
    $this->asgNom($nom);
    $this->asgClavardage($clavardage);
    $this->asgDureePartie($dureePartie);
    $this->asgBanqueQuestions($banqueQuestions);
    $this->asgCle($cleGroupe);
    $this->asgCleAdministrateur($cleAdministrateur);
    $this->INVARIANTS();
  }

  //****************************************************************************
  // Sommaire:      ajouter un joueur à la liste de joueur de ce groupe
  // Entrée:        $joueur :  doit être un objet de type Joueur
  // Sortie:
  // Note:          $nbJoueur est invrémenté de 1
  //****************************************************************************
  function ajouterJoueur($joueur)
  {
    PRECONDITION(get_class($joueur)=="Joueur");
    $this->listeJoueur[]=$joueur;
    $this->nbJoueur++;
    POSTCONDITION(count($this->listeJoueur)==$this->nbJoueur);
  }

  //****************************************************************************
  // Sommaire:      enlever un joueur de la liste des joueurs
  // Entrée:        $no : entre 1 et $nbJoueur
  // Sortie:
  // Note:          $nbJoueur est décrémenté de 1
  //****************************************************************************
  function enleverJoueur($no)
  {
    PRECONDITION($no>=1 && $no <=$this->nbJoueur);
    unset($this->listeJoueur[$no-1]);
    $this->nbJoueur--;
    POSTCONDITION(count($this->listeJoueur)==$this->nbJoueur);
  }
  
  //****************************************************************************
  // Sommaire:      charger un groupe à partie de sa clé
  // Entrée:        $cle : la clé unique du groupe
  // Sortie:        retourne vrai si le groupe existe,faux sinon
  // Note:          
  //****************************************************************************
  function chargerMySQL($cle)
  {
    PRECONDITION($cle>0);
    
    $sql="select * from groupe where cleGroupe=" . $cle;
    $result = $this->mysqli->query($sql);
    if($result->num_rows==0)
        return false;
            
    $row=$result->fetch_object();

    $this->asgGroupe($row->nom,
        intval($row->cfgClavardagePermis),
        $row->cfgDureePartie,
        $row->cfgBanqueQuestions,
        $row->cleGroupe,
        $row->cleAdministrateur);

    $this->chargerJoueurMySQL();
    $this->INVARIANTS();
    return true;
  }

  //****************************************************************************
  // Sommaire:      charger les joueurs qui sont associé à ce groupe
  // Entrée:        
  // Sortie:
  // Note:
  //****************************************************************************
  function chargerJoueurMySQL()
  {
    //on charge les joueurs pour les ajouter à la liste des groupes
    $sql="select cleJoueur from joueur where cleGroupe=" . $this->cle;
    $result = $this->mysqli->query($sql);

    $nb = $result->num_rows;
    for($i=0;$i<$nb;$i++)
    {
        $row=$result->fetch_object();
        $joueur = new Joueur($this->mysqli);
        $joueur->chargerMySQLCle($row->cleJoueur);
        $this->ajouterJoueur($joueur);
    }
  }

  //****************************************************************************
  // Sommaire:      insérer un nouveau groupe dans la table
  // Entrée:        $cleAdministrateur : la clé de l'administrateur
  //                    à qui apartient ce groupe
  // Sortie:
  // Note:         
  //****************************************************************************
  function insertionMySQL()
  {
    PRECONDITION($this->cle==0 && $this->cleAdministrateur>0);

    $sql="insert into groupe(nom,cleAdministrateur,cfgDureePartie,
        cfgClavardagePermis,cfgBanqueQuestions)
        values('" .
        $this->nom . "'," .
        $this->cleAdministrateur . "," .
        $this->dureePartie . "," .
        $this->clavardage . "," .
        $this->banqueQuestions . ")";
        
    $result = $this->mysqli->query($sql);
    $this->asgCle($this->mysqli->insert_id);
    //$this->miseAJourJoueurMySQL();

  }
  
  //****************************************************************************
  // Sommaire:      mettre a jour la cle de groupe dans la table des joueurs
  // Entrée:        
  // Sortie:
  // Note:
  //****************************************************************************
  function miseAJourJoueurMySQL()
  {
    for($i=0;$i<$this->nbJoueur;$i++)
    {
        //echo $this->listeJoueur[$i]->reqCle()."<br>";
        $this->listeJoueur[$i]->asgCleGroupe($this->cle);
        $this->listeJoueur[$i]->miseAJourMySQL();
    }
  }
  
  //****************************************************************************
  // Sommaire:      mettre a jour le groupe et les joueurs
  // Entrée:
  // Sortie:
  // Note:
  //****************************************************************************
  function miseAJourMySQL()
  {
    PRECONDITION($this->cle>0);
    $sql="update groupe set
            nom='" . $this->nom . "',cfgDureePartie=" . $this->dureePartie .
            ",cfgClavardagePermis=" . $this->clavardage .
            ",cfgBanqueQuestions=" . $this->banqueQuestions .
            " where cleGroupe=" . $this->cle;

    $this->mysqli->query($sql);
    $this->miseAJourJoueurMySQL();

  }

  //****************************************************************************
  // Sommaire:      supprimer le groupe
  // Entrée:
  // Sortie:
  // Note:          met à jour le groupe dans la table des joueurs
  //****************************************************************************
  function deleteMySQL()
  {
    PRECONDITION($this->cle>0);
    $sql="update joueur set cleGroupe=0 where cleGroupe=" . $this->cle;
    $this->mysqli->query($sql);
    
    $sql="delete from groupe where cleGroupe=" . $this->cle;
    $this->mysqli->query($sql);
  }


  //****************************************************************************
  // Sommaire:      retourner un joueur du tableau des joueurs
  // Entrée:        $no : entre 1 et le nombre de joueur
  // Sortie:
  // Note:
  //****************************************************************************
  function reqJoueur($no)
  {
    PRECONDITION($no>=1 && $no<=$this->nbJoueur);
    return $this->listeJoueur[$no-1];
  }
  
  
  //fonctions de retour
  function reqNom()
  {
    return $this->nom;
  }
  function reqNbJoueur()
  {
    return $this->nbJoueur;
  }
  function reqDureePartie()
  {
    return $this->dureePartie;
  }
  function reqClavardage()
  {
    return $this->clavardage;
  }
  function reqCle()
  {
    return $this->cle;
  }
  function reqCleAdministrateur()
  {
    return $this->cleAdministrateur;
  }
  
}

class Groupes
{
  private $listeGroupes;        //liste des groupes pour l'administrateur
  private $nbGroupes;           //le nombre de groupes
  private $cleAdministrateur;   //la clé de l'administrateur à qui apartien le groupe
  
  private $mysqli;              //objet monmysqli

  //****************************************************************************
  // Sommaire:      constructeur de la classe Groupes
  // Entrée:
  // Sortie:
  // Note:
  //****************************************************************************
  function Groupes($mysqli)
  {
    PRECONDITION(get_class($mysqli)=="mon_mysqli");
    $this->nbGroupes=0;
    $this->cleAdministrateur=0;
    $this->mysqli=$mysqli;
  }

  //****************************************************************************
  // Sommaire:      ajouter un groupe à la liste de Groupe
  // Entrée:        $groupe : le groupe à ajouter
  // Sortie:
  // Note:
  //****************************************************************************
  function ajoutGroupe($groupe)
  {
    PRECONDITION(get_class($groupe)=="UnGroupe");
    $this->listeGroupes[]=$groupe;
    $this->nbGroupes++;
    POSTCONDITION(count($this->listeGroupes) == $this->nbGroupes);
  }

  //****************************************************************************
  // Sommaire:      enlever un groupe de la liste
  // Entrée:        $no : le numéro de groupe à enlever
  // Sortie:
  // Note:
  //****************************************************************************
  function enleverGroupe($no)
  {
    PRECONDITION($no>=1 && $no <=$this->nbGroupes);
    unset($this->listeGroupes[$no-1]);
    $this->nbGroupes--;
    POSTCONDITION(count($this->listeGroupes) == $this->nbGroupes);
  }

  //****************************************************************************
  // Sommaire:      assigner une clé d'aministrateur aux groupes
  // Entrée:        $cle : la clé de l'administrateur
  // Sortie:
  // Note:
  //****************************************************************************
  function asgCleAdministrateur($cle)
  {
    PRECONDITION($cle>=0);
    $this->cleAdministrateur=$cle;
    POSTCONDITION($this->reqCleAdministrateur()==$cle);
  }

  //****************************************************************************
  // Sommaire:      charger tous les groupe appartenant à cet administrateur
  // Entrée:        
  // Sortie:
  // Note:
  //****************************************************************************
  function chargerMySQL()
  {
    $sql="select cleGroupe from groupe where cleAdministrateur=" . $this->cleAdministrateur;
    $result = $this->mysqli->query($sql);
    $nb=$result->num_rows;
    for($i=0;$i<$nb;$i++)
    {
        $row=$result->fetch_object();
        $groupe=new UnGroupe($this->mysqli);
        $groupe->chargerMySQL($row->cleGroupe);
        $this->ajoutGroupe($groupe);
    }
  }
  
  //fonction de retour
  function reqGroupe($no)
  {
    PRECONDITION($no >=1 && $no<=$this->nbGroupes);
    return $this->listeGroupes[$no-1];
  }
  function reqNbGroupe()
  {
    return $this->nbGroupes;
  }
  function reqCleAdministrateur()
  {
    return $this->cleAdministrateur;
  }

  
}
