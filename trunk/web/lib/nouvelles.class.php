<?php
/*******************************************************************************
Fichier : nouvelles.class.php
Auteur : Maxime B�gin
Description :
    classes servant � la gestion des nouvelles
    2 classes : uneNouvelle et Nouvelles
********************************************************************************
23-06-2006 Maxime B�gin - Ajout d'une image associ� aux nouvelles.
22-06-2006 Maxime B�gin - ajout de destinataire pour les nouvelles :
    tout le monde(0),seulement les joueurs(1) ou bien seulement les professeurs(2).
    Lorsque l'on veut charger les nouvelles on peut par exemple pass� en param�tre
    un tableau contenant 0,1,2 ce qui signifie qu'on charger TOUTES les nouvelles
21-06-2006 Maxime B�gin - ajout de quelques commentaires
05-06-2006 Maxime B�gin - Version initiale
*******************************************************************************/

require_once("exception.class.php");
require_once("mon_mysqli.class.php");

class UneNouvelle
{

    private $cleNouvelle;       //cl� de la nouvelle
    private $nouvelle;          //la nouvelle elle-m�me
    private $date;              //date de la nouvelle
    private $titre;             //titre de la nouvelle
    private $image;             //chemin de l'image
    private $destinataire;      //qui s'adresse la nouvelle
    private $cleLangue;
    private $mysqli;            //objet monmysqli


    //**************************************************************************
    // Sommaire:    Constructeur de la classe uneNouvelle
    // Entr�e:
    // Sortie:
    // Note:        met la cl� de la nouvelle � -1
    //**************************************************************************
    function UneNouvelle($mysqli)
    {
      PRECONDITION(get_class($mysqli)=="mon_mysqli");
      $this->cleNouvelle=0;
      $this->mysqli=$mysqli;
    }

    //**************************************************************************
    // Sommaire:    V�rifier les invariants de la classe
    // Entr�e:
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
    // Entr�e:      $nouvelle : la nouvelle
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
    // Sommaire:    assign� le texte principale
    // Entr�e:      $nouvelle : la nouvelle
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
    // Sommaire:    assign� le titre de la nouvelle
    // Entr�e:      $titre : le titre de la nouvelle
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
    // Sommaire:    assign� le destinataire de la nouvelles
    // Entr�e:      $destinataire : le destinataire de la nouvelle
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
    // Sommaire:    assign� la date de la nouvelle
    // Entr�e:      $date : le date de la nouvelle
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
    // Sommaire:    assign� une image � la nouvelle
    // Entr�e:      $img : le chemin de l'image
    // Sortie:
    // Note:        
    //**************************************************************************
    function asgImage($img)
    {
      $this->image=$img;
      POSTCONDITION($this->reqImage()==$img);
    }

    //**************************************************************************
    // Sommaire:    assign� la cl� de la nouvelle
    // Entr�e:      $cle : la cl� unique de la nouvelle
    // Sortie:
    // Note:
    //**************************************************************************
    function asgCleNouvelle($cle)
    {
      PRECONDITION($cle>=0);
      $this->cleNouvelle=$cle;
      POSTCONDITION($this->reqCle()==$cle);
    }
    
    function asgCleLangue($cleLangue) {
      $this->cleLangue = $cleLangue;
    }

    //**************************************************************************
    // Sommaire:    ins�rer la nouvelle courante dans la table
    // Entr�e:
    // Sortie:
    // Note:        la cl� doit �tre �gale � -1
    //**************************************************************************
    function insertionMySQL()
    {
      $this->date=date("Y-m-d");
      $this->INVARIANTS();
      $sql="insert into nouvelle(titre,dateNouvelle,nouvelle,destinataire,image,cleLangue) values('" .
            $this->titre . "','" 
			. $this->date . "','" 
            . $this->nouvelle . "'," 
			. $this->destinataire . ",'" 
			. $this->image . "'," . $this->cleLangue . ")";
      $result = $this->mysqli->query($sql);
      $this->asgCleNouvelle($this->mysqli->insert_id);

    }

    //**************************************************************************
    // Sommaire:    charger une nouvelle � partir du num�ro de cl�
    // Entr�e:
    // Sortie:      faux si aucune nouvelle, vrai sinon
    // Note:        si aucun r�sultat on g�n�re une exception
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
      $this->asgCleLangue($row->cleLangue);
      $this->INVARIANTS();
      return true;
    }

    //**************************************************************************
    // Sommaire:    mettre � jour la nouvelle dans la table
    // Entr�e:
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
            ",cleLangue=" . $this->reqCleLangue() .
            " where cleNouvelle=" . $this->cleNouvelle;

      $this->mysqli->query($sql);
      return true;

    }

    //**************************************************************************
    // Sommaire:    supprimer la nouvelle de la table
    // Entr�e:
    // Sortie:      retourne le nombre de ligne affect�
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
    function reqCleLangue() {
      return $this->cleLangue;
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
    // Entr�e:
    // Sortie:
    // Note:        met le nombre de nouvelle � 0
    //**************************************************************************
    function Nouvelles($mysqli)
    {
      PRECONDITION(get_class($mysqli)=="mon_mysqli");
      $this->nbNouvelle=0;
      $this->mysqli=$mysqli;
    }
    

    //**************************************************************************
    // Sommaire:    Ajouter une nouvelle dans le tableau
    // Entr�e:      $nouvelle un object de type UneNouvelle
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
    // Entr�e:      $nbNouvelle : le nombre de nouvelle � charger
    //              $destinataire : un tableau qui peut contenir une ou plusieurs
    //                  valeur 0 - pour tous , 1 - joueurs, 2 - administrateurs
    // Sortie:
    // Note:        si $nbNouvelle = -1 on charge toutes les nouvelles
    //
    //**************************************************************************
    function chargerMySQL($nbNouvelle,$destinataire, $cleLangue)
    {

      $sql = "select * from nouvelle where (";
      $nb=count($destinataire);
      for($i=0;$i<$nb;$i++)
      {
        $sql.= "destinataire=" . $destinataire[$i];
        if($i+1<$nb)
            $sql.= " or ";
      }
      
      $sql.= ") and cleLangue=" . $cleLangue;

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
        $uneNouvelle->asgCleLangue($row->cleLangue);
        $this->ajoutNouvelle($uneNouvelle);
      }

    }
    
    function chargerTouteMySQL() {
      $sql = "select * from nouvelle order by dateNouvelle desc, cleNouvelle desc";
      
      $result = $this->mysqli->query($sql);
      $nbNouvelle = $result->num_rows;
      
      for($i=0;$i<$nbNouvelle;$i++)
      {
        $row=$result->fetch_object();
        $uneNouvelle=new uneNouvelle($this->mysqli);
        $uneNouvelle->asgUneNouvelle($row->nouvelle,$row->dateNouvelle,
            $row->titre,$row->destinataire,$row->image);
        $uneNouvelle->asgCleNouvelle($row->cleNouvelle);
        $uneNouvelle->asgCleLangue($row->cleLangue);
        $this->ajoutNouvelle($uneNouvelle);
      }
      
    }

    //*******************
    //fonctions de retour
    //*******************
    
    //**************************************************************************
    // Sommaire:    retourne la nouvelle # $noNouvelle
    // Entr�e:      $noNouvelle : doit �tre entre 1 et $nbNouvelle
    // Sortie:
    // Note:        $noNouvelle-1 est le num�ro de la nouvelle dans le tableau
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

