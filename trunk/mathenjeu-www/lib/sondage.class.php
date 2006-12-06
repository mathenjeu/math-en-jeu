<?php
/*******************************************************************************
Fichier : sondage.class.php
Auteur : Maxime Bégin
Description :
    classes servant à la gestion des sondage
    2 classes : ReponseSondage et Sondage
********************************************************************************
27-07-2006 Maxime Bégin - ajout des fonctions de mise à jour d'un sondage,
	et d'enlèvement d'une réponse.
22-06-2006 Maxime Bégin - Ajout de fonctionnalité pour pouvoir afficher des
    sondage spécifique aux joueurs ou bien aux administrateurs.
21-06-2006 Maxime Bégin - ajout de quelques commentaires
25-05-2006 Maxime Bégin - Version initiale
*******************************************************************************/

//classe permmetant de gérer une réponse à un sondage
class ReponseSondage
{
    private $cleReponse;        //la clé unique de la réponse
    private $reponse;           //une réponse
    private $compteur;          //nombre joueur ayant choisie ce choix
    private $mysqli;            //objet monmysqli


    //**************************************************************************
    // Sommaire:    Constructeur de la classe ReponseSondage
    // Entrée:
    // Sortie:
    // Note:        
    //**************************************************************************
    function ReponseSondage($mysqli)
    {
      PRECONDITION(get_class($mysqli)=="mon_mysqli");
      $this->cleReponse=0;
      $this->reponse="";
      $this->compteur=0;
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
      INVARIANT($this->reponse!="");
      INVARIANT($this->compteur>=0);
      INVARIANT($this->cleReponse>=0);
    }
    
    //**************************************************************************
    // Sommaire:    assigné un texte à la réponse
    // Entrée:      $réponse : la réponse
    // Sortie:
    // Note:        la réponse ne doit pas être vide
    //**************************************************************************
    function asgReponse($reponse)
    {
      PRECONDITION(strlen($reponse)>0);
      $this->reponse=$reponse;
      POSTCONDITION($this->reqReponse()==$reponse);
    }

    //**************************************************************************
    // Sommaire:    assigné une clé à la réponse
    // Entrée:      $cle : la clé unique
    // Sortie:
    // Note:        la clé doit être plus grande que 0
    //**************************************************************************
    function asgCleReponse($cle)
    {
      PRECONDITION($cle>0);
      $this->cleReponse=$cle;
      POSTCONDITION($this->reqCleReponse()==$cle);
    }
    
    //**************************************************************************
    // Sommaire:    assigné une valeur au compteur
    // Entrée:      $compteur
    // Sortie:
    // Note:        le compteur doit être positif
    //**************************************************************************
    function asgCompteur($compteur)
    {
      PRECONDITION($compteur>=0);
      $this->compteur=$compteur;
      POSTCONDITION($this->reqCompteur()==$compteur);
    }

    //**************************************************************************
    // Sommaire:    charger une réponse à partir d'une cle Reponse
    // Entrée:      $cleReponse
    // Sortie:      retourne vrai si la réponse est trouvé, faux sinon
    // Note:        génère une exception si aucune réponse est trouvée
    //**************************************************************************
    function chargerMySQL($cleReponse)
    {
      PRECONDITION($cleReponse>0);
      $sql="select * from reponsesondage where cleReponse=" . $cleReponse;
      $result = $this->mysqli->query($sql);
      
      if($result->num_rows==0)
        return false;
        
      $row=$result->fetch_object();
      
      $this->asgReponse($row->reponse);
      $this->asgCompteur($row->compteur);
      $this->asgCleReponse($cleReponse);

    }
    
    //**************************************************************************
    // Sommaire:    ajouter cette réponse dans la table
    // Entrée:      $cleSondage
    // Sortie:
    // Note:        la clé du sondage doit être > 0 ,
    //              la clé de la péponse doit être = 0
    //              la réponse doit être valide
    //              le compteur doit être valide
    //**************************************************************************
    function insertionMySQL($cleSondage)
    {
      PRECONDITION($cleSondage>0);
      $this->INVARIANTS();

      $sql="insert into reponsesondage(cleSondage,reponse,compteur)
            values($cleSondage,'" . $this->reqReponse() . "'," . $this->compteur . ")";
      $result = $this->mysqli->query($sql);
      $this->asgCleReponse($this->mysqli->insert_id);

    }

    
    //**************************************************************************
    // Sommaire:    mettre à jour cette réponse dans la base de données
    // Entrée:
    // Sortie:
    // Note:        la clé de la péponse ne doit pas être = 0
    //**************************************************************************
    function miseAJourMySQL()
    {
      PRECONDITION($this->cleReponse!=0);

      $sql="update reponsesondage set reponse='" . $this->reponse .
        "',compteur=" .  $this->compteur . " where cleReponse=" .
      $this->cleReponse;
      $result = $this->mysqli->query($sql);

    }
    
    //**************************************************************************
    // Sommaire:    supprimer cette réponse de la base de données
    // Entrée:
    // Sortie:
    // Note:        
    //**************************************************************************
    function deleteMySQL()
    {
      $sql="delete from reponsesondage where cleReponse=" . $this->cleReponse;
      $result = $this->mysqli->query($sql);
    }
    
    //**************************************************************************
    // Sommaire:    ajout du choix dde l'utilisateur dans la table
    // Entrée:      $table : la table dans laquelle il faut ajouter le choix
    //              selon que c'est joueur ou un administrateur
    // Sortie:
    // Note:        on incrémente le compteur de cette réponse
    //**************************************************************************
    function ajoutReponseUtilisateurMySQL($cleUtilisateur,$cleSondage,$table)
    {
        $this->compteur++;
        $sql="update reponsesondage set compteur=" . $this->compteur .
            " where cleReponse=" . $this->cleReponse;
        
        $result = $this->mysqli->query($sql);
        
        $sql="insert into $table(cleUtilisateur,cleSondage,cleReponse)
            values($cleUtilisateur,$cleSondage," . $this->cleReponse . ")";
        $result = $this->mysqli->query($sql);
    }

    //*******************
    //fonctions de retour
    //*******************
    function reqReponse()
    {
      return $this->reponse;
    }
    function reqCleReponse()
    {
      return $this->cleReponse;
    }
    function reqCompteur()
    {
      return $this->compteur;
    }
}

//**************************************************************************
// Sommaire:    Pour gérer un sondage
// Note:        contient un tableau d'objet de type reponseSondage
//**************************************************************************
class Sondage
{
    private $cleSondage;        //la clé unique du sondage
    private $titre;             //le texte du sondage
    private $date;              //la date du sondage
    private $destinataire;      //le destinataire du sondage
    private $reponse;           //tableau d'objet reponseSondage
    private $nbReponse;         //nombre total de choix de réponse
    private $total;             //nombre total de joueur ayaynt répondu au sondage
    private $mysqli;            //objet monmysqli
    
    
    //**************************************************************************
    // Sommaire:    constructeur de la classe Sondage
    // Entrée:
    // Sortie:
    // Note:
    //**************************************************************************
    function Sondage($mysqli)
    {
      PRECONDITION(get_class($mysqli)=="mon_mysqli");
      $this->cleSondage=0;
      $this->titre="";
      $this->date=date("Y-m-d");
      $this->nbReponse=0;
      $this->total=0;
      $this->mysqli=$mysqli;
    }
    
    //**************************************************************************
    // Sommaire:    assigner une clé unique au sondage courant
    // Entrée:      $cle :
    // Sortie:
    // Note:        la clé doit être > 0
    //**************************************************************************
    function asgCleSondage($cle)
    {
        PRECONDITION($cle>0);
        $this->cleSondage=$cle;
        POSTCONDITION($this->reqCleSondage()==$cle);
        
    }
    
    //**************************************************************************
    // Sommaire:    assigner un titre au sondage
    // Entrée:      $titre
    // Sortie:
    // Note:        le titre ne doit pas être vide
    //**************************************************************************
    function asgTitre($titre)
    {
      PRECONDITION($titre!="");
      $this->titre=$titre;
      POSTCONDITION($this->reqTitre()==$titre);
    }

    //**************************************************************************
    // Sommaire:    assigner une date
    // Entrée:      $date
    // Sortie:
    // Note:        la date doit être valide
    //**************************************************************************
    function asgDate($date)
    {
      $dateEx = explode("-",$date);
      PRECONDITION(checkdate($dateEx[1],$dateEx[2],$dateEx[0]));
      $this->date=$date;
      POSTCONDITION($this->reqDate()==$date);
    }
    
    function asgDestinataire($destinataire)
    {
      PRECONDITION($destinataire==0 || $destinataire==1 || $destinataire==2);
      $this->destinataire=$destinataire;
      POSTCONDITION($this->reqDestinataire()==$destinataire);
    }

    //**************************************************************************
    // Sommaire:    ajouter une réponse au sondage courant
    // Entrée:      $reponse : un objet de type classe Reponse
    // Sortie:
    // Note:        
    //**************************************************************************
    function ajoutReponse($reponse)
    {
      PRECONDITION(get_class($reponse)=="ReponseSondage");
      $this->nbReponse++;
      $this->reponse[] = $reponse;
      POSTCONDITION(count($this->reponse)==$this->nbReponse);
    }
    
    //**************************************************************************
    // Sommaire:    enlever la dernière réponse
    // Entrée:      
    // Sortie:
    // Note:        
    //**************************************************************************
    function enleverReponse()
    {
    	PRECONDITION($this->nbReponse>0);
    	unset($this->reponse[$this->nbReponse-1]);
      $this->nbReponse--;
      POSTCONDITION(count($this->reponse)==$this->nbReponse);
    }

    //**************************************************************************
    // Sommaire:    ajouter le sondage dans la table
    // Entrée:      
    // Sortie:
    // Note:        le nombre de réponses >= 2
    //**************************************************************************
    function insertionSondageMySQL()
    {
      PRECONDITION($this->nbReponse>=2);
      $sql="insert into sondage(sondage,dateSondage,nbChoix,destinataire) values('"
            . $this->titre . "','" . $this->date . "'," . $this->nbReponse . ","
            . $this->destinataire . ")";
      $result = $this->mysqli->query($sql);
        
      //on obtient la cle du dernier sondage ajouté
      $this->cleSondage=$this->mysqli->insert_id;
      
      for($i=0;$i<$this->nbReponse;$i++)
      {
        $this->reponse[$i]->insertionMySQL($this->cleSondage);
      }
    }
    
    //**************************************************************************
    // Sommaire:    mettre à jour le sondage
    // Entrée:      
    // Sortie:
    // Note:        
    //**************************************************************************
    function miseAJourMySQL()
    {
    	$sql="UPDATE sondage set sondage='" . $this->titre . "',dateSondage='" . 
    		$this->date . "',nbChoix=" . $this->nbReponse . ",destinataire=" . $this->destinataire .
			" where cleSondage=" . $this->cleSondage;
    	
    	$result = $this->mysqli->query($sql);

      for($i=0;$i<$this->nbReponse;$i++)
      {
        $this->reponse[$i]->miseAJourMySQL();
      }
    
    }

    //**************************************************************************
    // Sommaire:    charger un sondage à partir de la clé
    // Entrée:      $clé : la clé unique du sondage
    // Sortie:
    // Note:
    //**************************************************************************
    function chargerSondageMySQL($cleSondage)
    {
        PRECONDITION($cleSondage>0);
        $sql="select * from sondage where cleSondage=" . $cleSondage;
        $result = $this->mysqli->query($sql);

        if($result->num_rows==0)
            return false;
            
        $row=$result->fetch_object();
      
        //assigne les informations au sondage
        $this->asgTitre($row->sondage);
        $this->asgCleSondage($row->cleSondage);
        $this->asgDate($row->dateSondage);
        $this->asgDestinataire($row->destinataire);

        //on va chercher les différents choix de réponse pour ce sondage
        $sql="SELECT cleReponse from reponsesondage where cleSondage=" . $this->cleSondage 
        	. " ORDER BY cleReponse";
        $result = $this->mysqli->query($sql);
      
        //on obtient le nombre de réponse
        $nb=$result->num_rows;
        //on boucle pour chaque réponse et on les ajoutes au tableau de réponse
        for($i=0;$i<$nb;$i++)
        {
            $row=$result->fetch_object();
            $reponse=new ReponseSondage($this->mysqli);
        
            $reponse->chargerMySQL($row->cleReponse);

            $this->ajoutReponse($reponse);
            $this->total+=$reponse->reqCompteur();
        }
        return true;
    }

    //**************************************************************************
    // Sommaire:    charger le sondage le plus récent
    // Entrée:      $destinataire : un tableau qui peut contenir une ou plusieurs
    //                  valeur 0 - pour tous , 1 - joueurs, 2 - administrateurs
    // Sortie:      retourne faux si aucun sondage dans la table
    // Note:        appel de la fonction chargerSondageMySQL
    //**************************************************************************
    function chargerPlusRecentSondageMySQL($destinataire)
    {
        $sql="select cleSondage from sondage where ";
        $nb=count($destinataire);
        for($i=0;$i<$nb;$i++)
        {
          $sql.="destinataire=" . $destinataire[$i];
          if($i+1<$nb)
            $sql.=" or ";
        }
        $sql.=" order by dateSondage desc, cleSondage desc limit 1";
        
        $result = $this->mysqli->query($sql);
        if($result->num_rows==0)
            return false;
        $row=$result->fetch_object();
        $this->chargerSondageMySQL($row->cleSondage);
    }
    
    //**************************************************************************
    // Sommaire:    supprimer le sondage en cours
    // Entrée:
    // Sortie:
    // Note:        on supprime dans les 4 tables, on appel de constructeur à la fin
    //**************************************************************************
    function deleteSondageMySQL()
    {
        $sql="delete from choixjoueursondage where cleSondage=" . $this->reqCleSondage();
        $this->mysqli->query($sql);
        
        $sql="delete from choixadminsondage where cleSondage=" . $this->reqCleSondage();
        $this->mysqli->query($sql);
        
        $sql="delete from reponsesondage where cleSondage=" . $this->reqCleSondage();
        $this->mysqli->query($sql);
      
        $sql="delete from sondage where cleSondage=" . $this->reqCleSondage();
        $this->mysqli->query($sql);
      
        $this->Sondage($this->mysqli);
    }

    //**************************************************************************
    // Sommaire:    ajouter le choix d'un joueur
    // Entrée:      $cleJoueur : la clé unique du joueur
    //              $noReponse : le # de réponse choisi
    //                    ( entre 1 et le nombre total de réponse)
    // Sortie:      faux si le choix n'a pas été ajouté
    // Note:        on vérifie
    //**************************************************************************
    function ajoutChoixJoueur($cleJoueur,$noReponse)
    {
        if($this->joueurDejaRepondu($cleJoueur)==false)
        {
            $reponse=$this->reponse[$noReponse-1];
            $reponse->ajoutReponseUtilisateurMySQL(
                $cleJoueur,$this->reqCleSondage(),'choixjoueursondage');
            return true;
        }
        else
            return false;
    }
    
    //**************************************************************************
    // Sommaire:    ajouter le choix d'un joueur
    // Entrée:      $cleJoueur : la clé unique du joueur
    //              $noReponse : le # de réponse choisi
    //                    ( entre 1 et le nombre total de réponse)
    // Sortie:      faux si le choix n'a pas été ajouté
    // Note:        on vérifie
    //**************************************************************************
    function ajoutChoixAdministrateur($cleAdministrateur,$noReponse)
    {
        if($this->adminDejaRepondu($cleAdministrateur)==false)
        {
            $reponse=$this->reponse[$noReponse-1];
            $reponse->ajoutReponseUtilisateurMySQL(
                $cleAdministrateur,$this->reqCleSondage(),'choixadminsondage');
            return true;
        }
        else
            return false;
    }

    //**************************************************************************
    // Sommaire:    vérifier si un joueur a déjà répondu à ce sondage
    // Entrée:
    // Sortie:      retourne faux si le joueur n'a pas encore répondu
    //              vrai dans le cas contraire
    // Note:        
    //**************************************************************************
    function joueurDejaRepondu($cleJoueur)
    {
        $sql="select * from choixjoueursondage where cleSondage=" . $this->cleSondage
            . " and cleUtilisateur=" . $cleJoueur;
        $result = $this->mysqli->query($sql);
        if($result->num_rows==0)
            return false;
        else
            return true;
    }
    
    //**************************************************************************
    // Sommaire:    vérifier si un joueur a déjà répondu à ce sondage
    // Entrée:
    // Sortie:      retourne faux si le joueur n'a pas encore répondu
    //              vrai dans le cas contraire
    // Note:
    //**************************************************************************
    function adminDejaRepondu($cleAdmin)
    {
        $sql="select * from choixadminsondage where cleSondage=" . $this->cleSondage
            . " and cleUtilisateur=" . $cleAdmin;
        $result = $this->mysqli->query($sql);
        if($result->num_rows==0)
            return false;
        else
            return true;
    }


    //*******************
    //fonctions de retour
    //*******************

    //**************************************************************************
    // Sommaire:    retourne la réponse # $noReponse
    // Entrée:      $noReponse : doit être entre 1 et $nbReponse
    // Sortie:
    // Note:        $noReponse-1 est le numéro de la réponse dans le tableau
    //**************************************************************************
    function reqReponse($noReponse)
    {
      PRECONDITION($noReponse>=1 && $noReponse<=$this->nbReponse);
      return $this->reponse[$noReponse-1];
    }
    
    function reqCleSondage()
    {
      return $this->cleSondage;
    }
    function reqDate()
    {
      return $this->date;
    }
    function reqTitre()
    {
      return $this->titre;
    }
    function reqNbReponse()
    {
      return $this->nbReponse;
    }
    function reqTotal()
    {
      return $this->total;
    }
    function reqDestinataire()
    {
      return $this->destinataire;
    }

}

