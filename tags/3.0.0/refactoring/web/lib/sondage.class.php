<?php
/*******************************************************************************
Fichier : sondage.class.php
Auteur : Maxime B�gin
Description :
    classes servant � la gestion des sondage
    2 classes : ReponseSondage et Sondage
********************************************************************************
27-07-2006 Maxime B�gin - ajout des fonctions de mise � jour d'un sondage,
	et d'enl�vement d'une r�ponse.
22-06-2006 Maxime B�gin - Ajout de fonctionnalit� pour pouvoir afficher des
    sondage sp�cifique aux joueurs ou bien aux administrateurs.
21-06-2006 Maxime B�gin - ajout de quelques commentaires
25-05-2006 Maxime B�gin - Version initiale
*******************************************************************************/

//classe permmetant de g�rer une r�ponse � un sondage
class ReponseSondage
{
    private $cleReponse;        //la cl� unique de la r�ponse
    private $reponse;           //une r�ponse
    private $compteur;          //nombre joueur ayant choisie ce choix
    private $mysqli;            //objet monmysqli


    //**************************************************************************
    // Sommaire:    Constructeur de la classe ReponseSondage
    // Entr�e:
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
    // Sommaire:    V�rifier les invariants de la classe
    // Entr�e:
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
    // Sommaire:    assign� un texte � la r�ponse
    // Entr�e:      $r�ponse : la r�ponse
    // Sortie:
    // Note:        la r�ponse ne doit pas �tre vide
    //**************************************************************************
    function asgReponse($reponse)
    {
      PRECONDITION(strlen($reponse)>0);
      $this->reponse=$reponse;
      POSTCONDITION($this->reqReponse()==$reponse);
    }

    //**************************************************************************
    // Sommaire:    assign� une cl� � la r�ponse
    // Entr�e:      $cle : la cl� unique
    // Sortie:
    // Note:        la cl� doit �tre plus grande que 0
    //**************************************************************************
    function asgCleReponse($cle)
    {
      PRECONDITION($cle>0);
      $this->cleReponse=$cle;
      POSTCONDITION($this->reqCleReponse()==$cle);
    }
    
    //**************************************************************************
    // Sommaire:    assign� une valeur au compteur
    // Entr�e:      $compteur
    // Sortie:
    // Note:        le compteur doit �tre positif
    //**************************************************************************
    function asgCompteur($compteur)
    {
      PRECONDITION($compteur>=0);
      $this->compteur=$compteur;
      POSTCONDITION($this->reqCompteur()==$compteur);
    }

    //**************************************************************************
    // Sommaire:    charger une r�ponse � partir d'une cle Reponse
    // Entr�e:      $cleReponse
    // Sortie:      retourne vrai si la r�ponse est trouv�, faux sinon
    // Note:        g�n�re une exception si aucune r�ponse est trouv�e
    //**************************************************************************
    function chargerMySQL($cleReponse)
    {
      PRECONDITION($cleReponse>0);
      $sql="select * from pool_awnser where pool_awnser_id=" . $cleReponse;
      $result = $this->mysqli->query($sql);
      
      if($result->num_rows==0)
        return false;
        
      $row=$result->fetch_object();
      
      $this->asgReponse($row->reponse);
      $this->asgCompteur($row->compteur);
      $this->asgCleReponse($cleReponse);

    }
    
    //**************************************************************************
    // Sommaire:    ajouter cette r�ponse dans la table
    // Entr�e:      $cleSondage
    // Sortie:
    // Note:        la cl� du sondage doit �tre > 0 ,
    //              la cl� de la p�ponse doit �tre = 0
    //              la r�ponse doit �tre valide
    //              le compteur doit �tre valide
    //**************************************************************************
    function insertionMySQL($cleSondage)
    {
      PRECONDITION($cleSondage>0);
      $this->INVARIANTS();

      $sql="insert into pool_awnser(pool_id,awnser,count)
            values($cleSondage,'" . $this->reqReponse() . "'," . $this->compteur . ")";
      $result = $this->mysqli->query($sql);
      $this->asgCleReponse($this->mysqli->insert_id);

    }

    
    //**************************************************************************
    // Sommaire:    mettre � jour cette r�ponse dans la base de donn�es
    // Entr�e:
    // Sortie:
    // Note:        la cl� de la p�ponse ne doit pas �tre = 0
    //**************************************************************************
    function miseAJourMySQL()
    {
      PRECONDITION($this->cleReponse!=0);

      $sql="update pool_awnser set awnser='" . $this->reponse .
        "',count=" .  $this->compteur . " where awnser_id=" .
      $this->cleReponse;
      $result = $this->mysqli->query($sql);

    }
    
    //**************************************************************************
    // Sommaire:    supprimer cette r�ponse de la base de donn�es
    // Entr�e:
    // Sortie:
    // Note:        
    //**************************************************************************
    function deleteMySQL()
    {
      $sql="delete from pool_awnser where awnser_id=" . $this->cleReponse;
      $result = $this->mysqli->query($sql);
    }
    
    //**************************************************************************
    // Sommaire:    ajout du choix dde l'utilisateur dans la table
    // Entr�e:      $table : la table dans laquelle il faut ajouter le choix
    //              selon que c'est joueur ou un administrateur
    // Sortie:
    // Note:        on incr�mente le compteur de cette r�ponse
    //**************************************************************************
    function ajoutReponseUtilisateurMySQL($cleUtilisateur,$cleSondage,$table)
    {
        $this->compteur++;
        $sql="update pool_awnser set count=" . $this->compteur .
            " where awnser_id=" . $this->cleReponse;
        
        $result = $this->mysqli->query($sql);
        
        $sql="insert into user_pool_choice(user_id, pool_id) " .
            "values($cleUtilisateur,$cleSondage)";
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
// Sommaire:    Pour g�rer un sondage
// Note:        contient un tableau d'objet de type reponseSondage
//**************************************************************************
class Sondage
{
    private $cleSondage;        //la cl� unique du sondage
    private $titre;             //le texte du sondage
    private $date;              //la date du sondage
    private $destinataire;      //le destinataire du sondage
    private $reponse;           //tableau d'objet reponseSondage
    private $nbReponse;         //nombre total de choix de r�ponse
    private $total;             //nombre total de joueur ayaynt r�pondu au sondage
    private $cleLangue;
    private $mysqli;            //objet monmysqli
    
    
    //**************************************************************************
    // Sommaire:    constructeur de la classe Sondage
    // Entr�e:
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
    // Sommaire:    assigner une cl� unique au sondage courant
    // Entr�e:      $cle :
    // Sortie:
    // Note:        la cl� doit �tre > 0
    //**************************************************************************
    function asgCleSondage($cle)
    {
        PRECONDITION($cle>0);
        $this->cleSondage=$cle;
        POSTCONDITION($this->reqCleSondage()==$cle);
        
    }
    
    //**************************************************************************
    // Sommaire:    assigner un titre au sondage
    // Entr�e:      $titre
    // Sortie:
    // Note:        le titre ne doit pas �tre vide
    //**************************************************************************
    function asgTitre($titre)
    {
      PRECONDITION($titre!="");
      $this->titre=$titre;
      POSTCONDITION($this->reqTitre()==$titre);
    }

    //**************************************************************************
    // Sommaire:    assigner une date
    // Entr�e:      $date
    // Sortie:
    // Note:        la date doit �tre valide
    //**************************************************************************
    function asgDate($date)
    {
      $dateEx = explode("-",$date);
      PRECONDITION(checkdate($dateEx[1],$dateEx[2],$dateEx[0]));
      $this->date=$date;
      POSTCONDITION($this->reqDate()==$date);
    }
    
    function asgCleLangue($cleLangue) {
      $this->cleLangue = $cleLangue;  
    }
    
    function asgDestinataire($destinataire)
    {
      PRECONDITION($destinataire==0 || $destinataire==1 || $destinataire==2);
      $this->destinataire=$destinataire;
      POSTCONDITION($this->reqDestinataire()==$destinataire);
    }

    //**************************************************************************
    // Sommaire:    ajouter une r�ponse au sondage courant
    // Entr�e:      $reponse : un objet de type classe Reponse
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
    // Sommaire:    enlever la derni�re r�ponse
    // Entr�e:      
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
    // Entr�e:      
    // Sortie:
    // Note:        le nombre de r�ponses >= 2
    //**************************************************************************
    function insertionSondageMySQL()
    {
      PRECONDITION($this->nbReponse>=2);
      $sql="insert into pool(question,date,number_of_choice,language_id) values('"
            . $this->titre . "','" . $this->date . "'," . $this->nbReponse 
            . "," . $this->cleLangue . ")";
      $result = $this->mysqli->query($sql);
        
      //on obtient la cle du dernier sondage ajout�
      $this->cleSondage=$this->mysqli->insert_id;
      
      for($i=0;$i<$this->nbReponse;$i++)
      {
        $this->reponse[$i]->insertionMySQL($this->cleSondage);
      }
    }
    
    //**************************************************************************
    // Sommaire:    mettre � jour le sondage
    // Entr�e:      
    // Sortie:
    // Note:        
    //**************************************************************************
    function miseAJourMySQL()
    {
    	$sql="UPDATE pool set question='" . $this->titre . "',date='" . 
    		$this->date . "',number_of_choice=" . $this->nbReponse .
    		",language_id=" . $this->cleLangue .
			" where pool_id=" . $this->cleSondage;
    	
    	$result = $this->mysqli->query($sql);

      for($i=0;$i<$this->nbReponse;$i++)
      {
        $this->reponse[$i]->miseAJourMySQL();
      }
    
    }

    //**************************************************************************
    // Sommaire:    charger un sondage � partir de la cl�
    // Entr�e:      $cl� : la cl� unique du sondage
    // Sortie:
    // Note:
    //**************************************************************************
    function chargerSondageMySQL($cleSondage)
    {
        PRECONDITION($cleSondage>0);
        $sql="select * from pool where pool_id=" . $cleSondage;
        $result = $this->mysqli->query($sql);

        if($result->num_rows==0)
            return false;
            
        $row=$result->fetch_object();
      
        //assigne les informations au sondage
        $this->asgTitre($row->sondage);
        $this->asgCleSondage($row->cleSondage);
        $this->asgDate($row->dateSondage);
        $this->asgDestinataire($row->destinataire);

        //on va chercher les diff�rents choix de r�ponse pour ce sondage
        $sql="SELECT pool_awnser_id from pool_awnser where pool_id=" . $this->cleSondage 
        	. " ORDER BY pool_id";
        $result = $this->mysqli->query($sql);
      
        //on obtient le nombre de r�ponse
        $nb=$result->num_rows;
        //on boucle pour chaque r�ponse et on les ajoutes au tableau de r�ponse
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
    // Sommaire:    charger le sondage le plus r�cent
    // Entr�e:      $destinataire : un tableau qui peut contenir une ou plusieurs
    //                  valeur 0 - pour tous , 1 - joueurs, 2 - administrateurs
    // Sortie:      retourne faux si aucun sondage dans la table
    // Note:        appel de la fonction chargerSondageMySQL
    //**************************************************************************
    function chargerPlusRecentSondageMySQL($destinataire,$language)
    {
        $sql="select pool_id from pool p, language l " .
          " where p.language_id=l.language_id " . 
          " and l.short_name='" . $language . "'" .
          " order by `date` desc, pool_id desc limit 1";
        
        $result = $this->mysqli->query($sql);
        if($result->num_rows==0) {
            return false;
        }
        $row=$result->fetch_object();
        return $this->chargerSondageMySQL($row->cleSondage);
    }
    
    //**************************************************************************
    // Sommaire:    supprimer le sondage en cours
    // Entr�e:
    // Sortie:
    // Note:        on supprime dans les 4 tables, on appel de constructeur � la fin
    //**************************************************************************
    function deleteSondageMySQL()
    {
        $sql="delete from user_pool_choice where pool_id=" . $this->reqCleSondage();
        $this->mysqli->query($sql);
        
        //$sql="delete from choixadminsondage where cleSondage=" . $this->reqCleSondage();
        //$this->mysqli->query($sql);
        
        $sql="delete from pool_awnser where pool_id=" . $this->reqCleSondage();
        $this->mysqli->query($sql);
      
        $sql="delete from pool where pool_id=" . $this->reqCleSondage();
        $this->mysqli->query($sql);
      
        $this->Sondage($this->mysqli);
    }

    //**************************************************************************
    // Sommaire:    ajouter le choix d'un joueur
    // Entr�e:      $cleJoueur : la cl� unique du joueur
    //              $noReponse : le # de r�ponse choisi
    //                    ( entre 1 et le nombre total de r�ponse)
    // Sortie:      faux si le choix n'a pas �t� ajout�
    // Note:        on v�rifie
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
    // Entr�e:      $cleJoueur : la cl� unique du joueur
    //              $noReponse : le # de r�ponse choisi
    //                    ( entre 1 et le nombre total de r�ponse)
    // Sortie:      faux si le choix n'a pas �t� ajout�
    // Note:        on v�rifie
    // @deprecated
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
    // Sommaire:    v�rifier si un joueur a d�j� r�pondu � ce sondage
    // Entr�e:
    // Sortie:      retourne faux si le joueur n'a pas encore r�pondu
    //              vrai dans le cas contraire
    // Note:        
    //**************************************************************************
    function joueurDejaRepondu($cleJoueur)
    {
        $sql="select * from user_pool_choice where pool_id=" . $this->cleSondage
            . " and user_id=" . $cleJoueur;
        $result = $this->mysqli->query($sql);
        if($result->num_rows==0)
            return false;
        else
            return true;
    }
    
    //**************************************************************************
    // Sommaire:    v�rifier si un joueur a d�j� r�pondu � ce sondage
    // Entr�e:
    // Sortie:      retourne faux si le joueur n'a pas encore r�pondu
    //              vrai dans le cas contraire
    // Note:
    // @deprecated
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
    // Sommaire:    retourne la r�ponse # $noReponse
    // Entr�e:      $noReponse : doit �tre entre 1 et $nbReponse
    // Sortie:
    // Note:        $noReponse-1 est le num�ro de la r�ponse dans le tableau
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
    
    function reqCleLangue() {
      return $this->cleLangue;
    }

}

