<?php
/*******************************************************************************
Fichier : utilisateur.class.php
Auteur : Maxime Bégin
Description :
    classe abstraire qui ne peut pas être utilisé directement
    elle contient les fonctions relative aux utilisateurs du site web de math enjeu
    c'est la classe de base pour les joueurs et les administrateurs
********************************************************************************
14-08-2006 Maxime Bégin - ajout du chiffrement de mot de passe
21-06-2006 Maxime Bégin - ajout de quelques commentaires
25-05-2006 Maxime Bégin - Version initiale
*******************************************************************************/

require_once("exception.class.php");
require_once("courriel.class.php");

abstract class Utilisateur
{
    protected $alias;
    protected $courriel;
    private $cle;
    private $motDePasse;
    private $motDePasseNonChiffrer;
    private $nom;
    private $prenom;
    private $estConfirmer;
    private $etablissement;
    private $niveau;
    private $acces;
    protected $cleConfirmation;
    protected $mysqli;


    //**************************************************************************
    //Somaire : met la cle à -1 ce qui signifie que le joueur n'est pas encore
    //          valide
    //Entrée:
    //Sortie:
    //Note: 
    //**************************************************************************
    function Utilisateur($mysqli)
    {
      PRECONDITION(get_class($mysqli)=="mon_mysqli");
      $this->mysqli=$mysqli;
      $this->asgCle(0);
    }
    
    //**************************************************************************
    // Sommaire:    Vérifier les invariants de la classe
    // Entrée:
    // Sortie:
    // Note:
    //**************************************************************************
    private function INVARIANTS()
    {
      
      //INVARIANT(strlen($this->motDePasse)>=5 && strlen($this->motDePasse)<=20);
      INVARIANT(strlen($this->nom)>0);
      INVARIANT(strlen($this->prenom)>0);
      INVARIANT($this->estConfirmer==0 || $this->estConfirmer==1);
      INVARIANT($this->etablissement >= 0);
      INVARIANT(($this->niveau > 0) && ($this->niveau < 15));
      INVARIANT(Courriel::validerCourriel($this->courriel));
      INVARIANT(eregi("^[a-zA-Z0-9]{4,8}$",$this->alias));

    }
    
    //**************************************************************************
    //Somaire : Valider un alias
    //Entrée: $alias : l'alias à valider
    //Sortie: retourne vrai si l'alias est valide faux sinon
    //Note:
    //**************************************************************************
    function validerAlias($alias)
    {
      return (eregi("^[a-zA-Z0-9_-]{4,8}$",stripcslashes($alias)));
    }

    //**************************************************************************
    //Somaire : Valider un mot de passe
    //Entrée: $alias : l'alias à valider
    //Sortie: retourne vrai si le mot de passe est valide faux sinon
    //Note: le mot de passe doit avoir entre 5 et caractères
    //**************************************************************************
    static function validerMotDePasse($pass)
    {
      return (strlen($pass)>=5 && strlen($pass)<=20);
      //return true;
    }
    
    //**************************************************************************
    // Sommaire:        Assigner l'alias à l'utilisateur
    // Entrée:          $alias
    // Sortie:          
    // Note:            une exception est lancé si l'alias est invalide
    //                  l'alias est convertie en minuscule
    //**************************************************************************
    function asgAlias($alias)
    {
      PRECONDITION($this->validerAlias($alias));
      $this->alias=strtolower($alias);
      POSTCONDITION($this->reqAlias()==strtolower($alias));
    }
    
    //**************************************************************************
    // Sommaire:        Assigner une adresse courriel au joueur
    // Entrée:          $courriel
    // Sortie:
    // Note:            le courriel est convertie en minuscule
    //**************************************************************************
    function asgCourriel($courriel)
    {
      PRECONDITION(Courriel::validerCourriel($courriel));
      $this->courriel=strtolower($courriel);
      POSTCONDITION($this->reqCourriel()==strtolower($courriel));
    }



    //**************************************************************************
    //Sommaire : Assigne les valeurs à l'utilisateur courant
    //Entrée:
    //    $nom
    //    $prenom
    //    $alias
    //    $motDePasse
    //    $courriel
    //    $estConfirmer       : si le courriel est confirmé ou non
    //    $etablissement      : clé de l'établissement
    //    $niveau             : le niveau scolaire
    //Sortie:
    //Note : on appel les fonctions d'assignations
    //**************************************************************************
    function asgUtilisateur($nom,$prenom,$alias,$motDePasse,$courriel,
        $estConfirmer,$etablissement,$niveau,$acces)
    {
      $this->asgNom($nom);
      $this->asgPrenom($prenom);
      $this->asgAlias($alias);
      $this->asgMotDePasse($motDePasse);
      $this->asgCourriel($courriel);
      $this->asgEstConfirmer($estConfirmer);
      $this->asgNiveau($niveau);
      $this->asgEtablissement($etablissement);
      $this->asgAcces($acces);
      
      $this->INVARIANTS();
    }
    

    //**************************************************************************
    // Sommaire:        Assigner le mot de passe à l'utilisateur
    // Entrée:          $motDePasse
    // Sortie:
    // Note:            une exception est lancé si le mot de passe est invalide
    //**************************************************************************
    function asgMotDePasse($motDePasse)
    {
      $this->motDePasse=$motDePasse;
      POSTCONDITION($this->reqMotDePasse()==$motDePasse);
    }
    //**************************************************************************
    // Sommaire:        Assigner la clé unique à l'utilisateur
    // Entrée:          $cle
    // Sortie:
    // Note:            la clé doit être > 0
    //**************************************************************************
    protected function asgCle($cle)
    {
      PRECONDITION($cle >= 0);
      $this->cle=$cle;
      POSTCONDITION($this->reqCle()==$cle);
    }

    //**************************************************************************
    // Sommaire:        Assigne le nom à l'utilisateur
    // Entrée:          $nom
    // Sortie:
    // Note:            la longueur du nom ne doit pas être nulle
    //**************************************************************************
    function asgNom($nom)
    {
      PRECONDITION(strlen($nom)>0);
      $this->nom = $nom;
      POSTCONDITION($this->reqNom()==$nom);
    }

    //**************************************************************************
    // Sommaire:        Assigne le prénom à l'utilisateur
    // Entrée:          $prenom
    // Sortie:
    // Note:            la longueur du prénom ne doit pas être nulle
    //**************************************************************************
    function asgPrenom($prenom)
    {
      PRECONDITION(strlen($prenom)>0);
      $this->prenom=$prenom;
      POSTCONDITION($this->reqPrenom()==$prenom);
    }

    //**************************************************************************
    // Sommaire:        asigne à l'utilisateur le status de confirmer ou non
    // Entrée:          $estConfirmer : 0 pour non confirmer, 1 pour confirmer
    // Sortie:          
    // Note:
    //**************************************************************************
    function asgEstConfirmer($estConfirmer)
    {
      PRECONDITION($estConfirmer==0 || $estConfirmer==1);
      $this->estConfirmer=$estConfirmer;
      POSTCONDITION($this->reqEstConfirmer()==$estConfirmer);
    }

    //**************************************************************************
    // Sommaire:        asigne à l'utilisateur le numéro d'établissement
    // Entrée:          $etablissement : le numéro d'établissement
    // Sortie:
    // Note:            le # d'établissement doit être positif
    //**************************************************************************
    function asgEtablissement($etablissement)
    {
      PRECONDITION($etablissement >= 0);
      $this->etablissement=$etablissement;
      POSTCONDITION($this->reqEtablissement()==$etablissement);
    }

    //**************************************************************************
    // Sommaire:        asigne à l'utilisateur le niveau scolaire
    // Entrée:          $niveau
    // Sortie:
    // Note:            le niveau doit être entre 1 et 15 inclusivement
    //**************************************************************************
    function asgNiveau($niveau)
    {
      PRECONDITION($niveau > 0 && $niveau <= 15);
      $this->niveau=$niveau;
      POSTCONDITION($this->reqNiveau()==$niveau);
    }
    
    //**************************************************************************
    // Sommaire:        asigne à l'utilisateur le niveau d'accès
    // Entrée:          $acces
    // Sortie:
    // Note:            le niveau d'accès doit être entre 0 et 5 inclusivement
    //**************************************************************************    
    function asgAcces($acces)
    {
     	PRECONDITION($acces>=0 && $acces<=5);
		$this->acces = $acces;
		POSTCONDITION($this->reqAcces() == $acces);
	}
    
    //**************************************************************************
    // Sommaire:        générer une chaîne aléatoire
    // Entrée:          $longueur : la longueur de la chaîne
    // Sortie:
    // Note:            
    //**************************************************************************
    function genererChaineAleatoire($longueur)
    {
        $patron = "1234567890ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        $chaineRnd="";
        for ($i = 0; $i < $longueur; $i++)
        {
            $c = rand(0, strlen($patron) - 1);
            $chaineRnd .= $patron[$c];
        }
        return $chaineRnd;
    }
    
    //**************************************************************************
    // Sommaire:        encrypter le mot de passe courant
    // Entrée:          
    // Sortie:
    // Note:            on conserve le mot de passe non chiffrer pour envoie 
	//					par mail
    //**************************************************************************
    function crypterMotDePasse()
    {
      $this->motDePasseNonChiffrer = $this->motDePasse;
	  $this->motDePasse=crypt($this->motDePasse);
	}
	
	//**************************************************************************
    // Sommaire:        vérifier si le mot de passe est valide
    // Entrée:          $passChiffrer = le mot de passe chiffre
	//					$pass : le mot de passe non-chiffrer qu'on doit vérifier
    // Sortie:			$vrai si le mot de passe est valide, faux sinon
    // Note:            on conserve le mot de passe non chiffrer pour l'envoie 
	//					par courriel.
    //**************************************************************************
	function validerPassCrypter($passChiffrer,$pass)
	{
	  $sql="select password('" . addslashes($pass) . "')";
	  $result = $this->mysqli->query($sql);
	  $row=$result->fetch_array();

	  return ($row[0]==$passChiffrer)?true:false;

	}


    //les fonctions à déveloper dans les classes filles

    abstract function genererCleConfirmation();
    abstract function validerAliasUnique($alias);
    abstract function validerCourrielUnique($courriel);
    abstract function envoyerCourrielConfirmation();
    abstract function envoyerCourrielInfoPerdu($courriel);
    abstract function miseAJourMotDePasseMySQL($motDePasse);

    abstract function chargerMySQL($alias,$motDePasse);
    abstract function insertionMySQL();
    abstract function miseAJourMySQL();
    abstract function deleteMySQL();

    
    //***********************
    //Les fonctions de retour
    //***********************
    function reqNom(){
      return $this->nom;
    }
    function reqCle(){
      return $this->cle;
    }
    function reqPrenom(){
      return $this->prenom;
    }
    function reqAlias(){
      return $this->alias;
    }
    function reqMotDePasse(){
      return $this->motDePasse;
    }
    function reqCourriel(){
      return $this->courriel;
    }
    function reqEstConfirmer(){
      return $this->estConfirmer;
    }
    function reqCleConfirmation(){
      return $this->cleConfirmation;
    }
    function reqNiveauScolaire(){
      return $this->niveau;
    }
    function reqEtablissement(){
      return $this->etablissement;
    }
    function reqNiveau(){
      return $this->niveau;
    }
    function reqAcces(){
		return $this->acces;
	}


}

