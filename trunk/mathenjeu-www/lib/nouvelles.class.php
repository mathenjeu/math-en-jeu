<?php
/*******************************************************************************
Fichier : nouvelles.class.php
Auteur : Maxime Bégin
Description :
    classes servant à la gestion des nouvelles
    2 classes : uneNouvelle et Nouvelles
********************************************************************************
23-06-2006 Maxime Bégin - Ajout d'une image associé aux nouvelles.
22-06-2006 Maxime Bégin - ajout de destinataire pour les nouvelles :
    tout le monde(0),seulement les joueurs(1) ou bien seulement les professeurs(2).
    Lorsque l'on veut charger les nouvelles on peut par exemple passé en paramètre
    un tableau contenant 0,1,2 ce qui signifie qu'on charger TOUTES les nouvelles
21-06-2006 Maxime Bégin - ajout de quelques commentaires
05-06-2006 Maxime Bégin - Version initiale
*******************************************************************************/

require_once("exception.class.php");
require_once("mon_mysqli.class.php");

class UneNouvelle
{

    private $cleNouvelle;       //clé de la nouvelle
    private $nouvelle;          //la nouvelle elle-même
    private $date;              //date de la nouvelle
    private $titre;             //titre de la nouvelle
    private $image;             //chemin de l'image
    private $destinataire;      //à qui s'adresse la nouvelle
    
    private $mysqli;            //objet monmysqli


    //**************************************************************************
    // Sommaire:    Constructeur de la classe uneNouvelle
    // Entrée:
    // Sortie:
    // Note:        met la clé de la nouvelle à -1
    //**************************************************************************
    function UneNouvelle($mysqli)
    {
      PRECONDITION(get_class($mysqli)=="mon_mysqli");
      $this->cleNouvelle=0;
      $this->mysqli=$mysqli;
    }

    //**************************************************************************
    // Sommaire:    Vérifier les invariants de la classe
    // Entrée:
    // Sortie:
    // Note:        
    //**************************************************************************
    private function INVARIANTS()
    {
      INVARIANT(strlen($this->nouvelle)>0);
      INVARIANT(strlen($this->titre)>0);
      $dateEx = explode("-",$this->date);
      INVARIANT(checkdate($dateEx[1],$dateEx[2],$dateEx[0]));
      INVARIANT($this->cleNouvelle>=0);
      INVARIANT($this->destinataire==0 || $this->destinataire==1 || $this->destinataire==2);
    }

    //**************************************************************************
    // Sommaire:    construire une nouvelle
    // Entrée:      $nouvelle : la nouvelle
    //              $date : la date de la nouvelle
    //              $titre le titre de la nouvelle
    // Sortie:
    // Note:
    //**************************************************************************
    function asgUneNouvelle($nouvelle,$date,$titre,$destinataire,$image)
    {
      $this->asgNouvelle($nouvelle);
      $this->asgTitre($titre);
      $this->asgDate($date);
      $this->asgDestinataire($destinataire);
      $this->asgImage($image);
      $this->INVARIANTS();
    }

    //**************************************************************************
    // Sommaire:    assigné le texte principale
    // Entrée:      $nouvelle : la nouvelle
    // Sortie:
    // Note:
    //**************************************************************************
    function asgNouvelle($texte)
    {
      PRECONDITION(strlen($texte)>0);
      $this->nouvelle=$texte;
      POSTCONDITION($this->reqNouvelle()==$texte);
    }

    //**************************************************************************
    // Sommaire:    assigné le titre de la nouvelle
    // Entrée:      $titre : le titre de la nouvelle
    // Sortie:
    // Note:
    //**************************************************************************
    function asgTitre($titre)
    {
      PRECONDITION(strlen($titre)>0);
      $this->titre=$titre;
      POSTCONDITION($this->reqTitre()==$titre);
    }

    //**************************************************************************
    // Sommaire:    assigné le destinataire de la nouvelles
    // Entrée:      $destinataire : le destinataire de la nouvelle
    // Sortie:
    // Note:
    //**************************************************************************
    function asgDestinataire($destinataire)
    {
      PRECONDITION($destinataire==0 || $destinataire==1 || $destinataire==2);
      $this->destinataire=$destinataire;
      POSTCONDITION($this->reqDestinataire()==$destinataire);
    }

    //**************************************************************************
    // Sommaire:    assigné la date de la nouvelle
    // Entrée:      $date : le date de la nouvelle
    // Sortie:
    // Note:        date au format aaaa-mm-jj
    //**************************************************************************
    function asgDate($date)
    {
      $dateEx = explode("-",$date);
      PRECONDITION(checkdate($dateEx[1],$dateEx[2],$dateEx[0]));
      $this->date=$date;
      POSTCONDITION($this->reqDate()==$date);
    }

    //**************************************************************************
    // Sommaire:    assigné une image à la nouvelle
    // Entrée:      $img : le chemin de l'image
    // Sortie:
    // Note:        
    //**************************************************************************
    function asgImage($img)
    {
      $this->image=$img;
      POSTCONDITION($this->reqImage()==$img);
    }

    //**************************************************************************
    // Sommaire:    assigné la clé de la nouvelle
    // Entrée:      $cle : la clé unique de la nouvelle
    // Sortie:
    // Note:
    //**************************************************************************
    function asgCleNouvelle($cle)
    {
      PRECONDITION($cle>=0);
      $this->cleNouvelle=$cle;
      POSTCONDITION($this->reqCle()==$cle);
    }

    //**************************************************************************
    // Sommaire:    insérer la nouvelle courante dans la table
    // Entrée:
    // Sortie:
    // Note:        la clé doit être égale à -1
    //**************************************************************************
    function insertionMySQL()
    {
      $this->date=date("Y-m-d");
      $this->INVARIANTS();
      $sql="insert into nouvelle(titre,dateNouvelle,nouvelle,destinataire,image) values('" .
            $this->titre . "','" . $this->date . "','" .
            $this->nouvelle . "'," . $this->destinataire . ",'" . $this->image . "')";
      $result = $this->mysqli->query($sql);
      $this->asgCleNouvelle($this->mysqli->insert_id);

    }

    //**************************************************************************
    // Sommaire:    charger une nouvelle à partir du numéro de clé
    // Entrée:
    // Sortie:      faux si aucune nouvelle, vrai sinon
    // Note:        si aucun résultat on génère une exception
    //**************************************************************************
    function chargerMySQL($cleNouvelle)
    {

      $sql="select * from nouvelle where cleNouvelle=" . $cleNouvelle;
      $result = $this->mysqli->query($sql);
      
      if($result->num_rows==0)
        return false;

      $row=$result->fetch_object();

      $this->asgNouvelle($row->nouvelle);
      $this->asgTitre($row->titre);
      $this->asgDate($row->dateNouvelle);
      $this->asgCleNouvelle($row->cleNouvelle);
      $this->asgDestinataire($row->destinataire);
      $this->asgImage($row->image);
      $this->INVARIANTS();
      return true;
    }

    //**************************************************************************
    // Sommaire:    mettre à jour la nouvelle dans la table
    // Entrée:
    // Sortie:
    // Note:        
    //**************************************************************************
    function miseAJourMySQL()
    {
      $this->INVARIANTS();
      $sql="update nouvelle set titre='" . $this->titre .
            "',dateNouvelle='" . $this->date .
            "',nouvelle='" . $this->nouvelle .
            "',image='" . $this->image .
            "',destinataire=" . $this->destinataire .
            " where cleNouvelle=" . $this->cleNouvelle;

      $this->mysqli->query($sql);
      return true;

    }

    //**************************************************************************
    // Sommaire:    supprimer la nouvelle de la table
    // Entrée:
    // Sortie:      retourne le nombre de ligne affecté
    // Note:        
    //**************************************************************************
    function deleteMySQL()
    {
        $sql = "delete from nouvelle where cleNouvelle=" . $this->cleNouvelle;
        $this->mysqli->query($sql);
        return($this->mysqli->affected_rows);
    }

    //***********************
    //les fonctions de retour
    //***********************
    function reqNouvelle()
    {
      return $this->nouvelle;
    }
    function reqDate()
    {
      return $this->date;
    }
    function reqTitre()
    {
      return $this->titre;
    }
    function reqCle()
    {
      return $this->cleNouvelle;
    }
    function reqDestinataire()
    {
      return $this->destinataire;
    }
    function reqImage()
    {
      return $this->image;
    }
}

//**************************************************************************
// Sommaire:    Classe Nouvelles
// Note:        la classe Nouvelles contient un tableau de nouvelle
//**************************************************************************
class Nouvelles
{

    private $nbNouvelle;    //le nombre de nouvelles
    private $nouvelles;     //tableau contenant les nouvelles
    private $mysqli;
    
    //**************************************************************************
    // Sommaire:    Constructeur de la classe Nouvelles
    // Entrée:
    // Sortie:
    // Note:        met le nombre de nouvelle à 0
    //**************************************************************************
    function Nouvelles($mysqli)
    {
      PRECONDITION(get_class($mysqli)=="mon_mysqli");
      $this->nbNouvelle=0;
      $this->mysqli=$mysqli;
    }
    

    //**************************************************************************
    // Sommaire:    Ajouter une nouvelle dans le tableau
    // Entrée:      $nouvelle un object de type UneNouvelle
    // Sortie:
    // Note:        ajouter une nouvelle de type uneNouvelleau tableau de nouvelles
    //**************************************************************************
    function ajoutNouvelle($nouvelle)
    {
      PRECONDITION(get_class($nouvelle)=="UneNouvelle");
      $this->nouvelles[]=$nouvelle;
      $this->nbNouvelle++;
      POSTCONDITION(count($this->nouvelles)==$this->nbNouvelle);
    }
    

    //**************************************************************************
    // Sommaire:    on charge dans le tableau les nouvelles de la table
    // Entrée:      $nbNouvelle : le nombre de nouvelle à charger
    //              $destinataire : un tableau qui peut contenir une ou plusieurs
    //                  valeur 0 - pour tous , 1 - joueurs, 2 - administrateurs
    // Sortie:
    // Note:        si $nbNouvelle = -1 on charge toutes les nouvelles
    //
    //**************************************************************************
    function chargerMySQL($nbNouvelle,$destinataire)
    {

      $sql = "select * from nouvelle where ";
      $nb=count($destinataire);
      for($i=0;$i<$nb;$i++)
      {
        $sql.= "destinataire=" . $destinataire[$i];
        if($i+1<$nb)
            $sql.= " or ";
      }

      $sql.= " order by dateNouvelle desc, cleNouvelle desc";
      
      if($nbNouvelle!=-1)
        $sql.= " limit " . $nbNouvelle;

      $result = $this->mysqli->query($sql);
      $nbNouvelle = $result->num_rows;
      
      for($i=0;$i<$nbNouvelle;$i++)
      {
        $row=$result->fetch_object();
        $uneNouvelle=new uneNouvelle($this->mysqli);
        $uneNouvelle->asgUneNouvelle($row->nouvelle,$row->dateNouvelle,
            $row->titre,$row->destinataire,$row->image);
        $uneNouvelle->asgCleNouvelle($row->cleNouvelle);
        
        $this->ajoutNouvelle($uneNouvelle);
      }

    }

    //*******************
    //fonctions de retour
    //*******************
    
    //**************************************************************************
    // Sommaire:    retourne la nouvelle # $noNouvelle
    // Entrée:      $noNouvelle : doit être entre 1 et $nbNouvelle
    // Sortie:
    // Note:        $noNouvelle-1 est le numéro de la nouvelle dans le tableau
    //**************************************************************************
    function reqNouvelle($noNouvelle)
    {
      PRECONDITION($noNouvelle>=1 && $noNouvelle<=$this->nbNouvelle);
      return $this->nouvelles[$noNouvelle-1];
    }
    function reqNbNouvelle()
    {
      return $this->nbNouvelle;
    }
    
}

