<?php
/*******************************************************************************
Fichier : courriel.class.php
Auteur : Maxime B�gin
Description : classe courriel
********************************************************************************
10-11-2006 Maxime B�gin - Modification pour utiliser la classe PHPMailer
21-06-2006 Maxime B�gin - Ajout de commentaires.
10-06-2006 Maxime B�gin - Version initiale
*******************************************************************************/

require_once("ini.php");


class Courriel extends PHPMailer
{
    var $From     = ADRESSE_COURRIEL;	//l'adresse courriel utiliser
    var $FromName = NOM_COURRIEL;		//idem
    var $Host     = SERVEUR_SMTP;		//l'adresse du serveur SMTP
    var $Port 	  = PORT_SMTP;			//le port de connexion au serveur SMTP
    var $Username = USER_SMTP;			//l'utilisateur pour le serveur SMTP
    var $Password = PASS_SMTP;			//le mot de passe pour le serveur SMTP
    
    var $Mailer   = "smtp";                         
    //var $WordWrap = 75;
    
    private $adresseDestination;

    //**************************************************************************
    // Sommaire:    Constructeur
    // Entr�e:      $sujet
    //              $message
    //              $adresseDestination
    //              $adresseSource
    // Sortie:
    // Note:
    //**************************************************************************
    function Courriel($sujet,$message,$adresseDestination)
    {
      $this->asgSujet($sujet);
      $this->asgMessage($message);
      $this->asgAdresseDestination($adresseDestination);
      $this->IsHTML(true);
      $this->INVARIANTS();
    }

    //**************************************************************************
    // Sommaire:    V�rifier les invariants de la classe
    // Entr�e:      
    // Sortie:
    // Note:        
    //**************************************************************************
    function INVARIANTS()
    {
      INVARIANT(strlen($this->Subject)>0);
      INVARIANT(strlen($this->Body)>0);
      INVARIANT($this->validerCourriel($this->From));
      INVARIANT($this->validerCourriel($this->adresseDestination));
    }

    //**************************************************************************
    // Sommaire:    Assigner un sujet
    // Entr�e:      $sujet
    // Sortie:
    // Note:        le sujet ne doit pas �tre vide
    //**************************************************************************
    function asgSujet($sujet)
    {
      PRECONDITION(strlen($sujet)>0);
      $this->Subject=$sujet;
      POSTCONDITION($this->reqSujet()==$sujet);
    }
    
    //**************************************************************************
    // Sommaire:    Assigner un messager
    // Entr�e:      $message
    // Sortie:
    // Note:        le message ne doit pas �tre vide
    //**************************************************************************
    function asgMessage($message)
    {
      PRECONDITION(strlen($message)>0);
      $this->Body=$message;
      POSTCONDITION($this->reqMessage()==$message);
    }

    //**************************************************************************
    // Sommaire:    Assigner un ent�te
    // Entr�e:      $header
    // Sortie:
    // Note:        l'ent�te ne doit pas �tre vide
    //**************************************************************************
/*
    function asgHeader($header)
    {
      PRECONDITION(strlen($header)>0);
      $this->header=$header;
      POSTCONDITION($this->reqHeader()==$header);
    }
*/

    //**************************************************************************
    // Sommaire:    Assigner une adresse de destination
    // Entr�e:      $adresse
    // Sortie:
    // Note:        l'adresse doit �tre valide
    //**************************************************************************
    function asgAdresseDestination($adresse)
    {
      PRECONDITION($this->validerCourriel($adresse));
      $this->AddAddress($adresse);
      $this->adresseDestination = $adresse;
      POSTCONDITION($this->reqAdresseDestination()==$adresse);
    }

    //**************************************************************************
    // Sommaire:    Assigner une adresse source
    // Entr�e:      $adresse
    // Sortie:
    // Note:        l'adresse doit �tre valide
    //**************************************************************************
    function asgAdresseSource($adresse)
    {
      PRECONDITION($this->validerCourriel($adresse));
      $this->From=$adresse;
      POSTCONDITION($this->reqAdresseSource()==$adresse);
    }
    
    //**************************************************************************
    // Sommaire:    Envoie le courriel apr�s avoir v�rifier les invariants
    // Entr�e:      
    // Sortie:
    // Note:        retourne vrai si le courriel a pu �tre envoy�, faux sinon
    //**************************************************************************
    function envoyerCourriel()
    {
      $this->INVARIANTS();
      if(!$this->Send())
        return false;
      else
        return true;

    }
    
    //**************************************************************************
    // Sommaire:        Valide une adresse courriel
    // Entr�e:          $courriel
    // Sortie:          retourne vrai si l'adresse est valide, faux sinon
    // Note:
    //**************************************************************************
    function validerCourriel($courriel)
    {
        $user = "[-a-z0-9_]";
        $domaine = '([a-z0-9]([-a-z0-9]*[a-z0-9]+)?)';

        $valide = '^' .$user . '+' .      // un ou plusieurs caract�re de user
        '(\.' . $user . '+)*'.            // suivi de 0 ou plusieur set de caract�re s�par� de points
        '@'.                              // suivi du @
        '(' . $domaine . '{1,63}\.)+'.    // suivi de 1 ou maximum 63 nom de domaine s�par� par des point
        $domaine . '{2,63}'.              // suivi d'un dernier domaine de 2 � 63 caract�re'
        '$';

        $result = eregi($valide,$courriel);
        if ($courriel == "") {
          $result = 1;
        }
        return $result;
    }


    //*******************
    //fonctions de retour
    //*******************
    function reqSujet()
    {
      return $this->Subject;
    }
    
    function reqMessage()
    {
      return $this->Body;
    }
    
    function reqAdresseSource()
    {
      return $this->From;
    }
    
    function reqAdresseDestination()
    {
      return $this->adresseDestination;
    }

}

