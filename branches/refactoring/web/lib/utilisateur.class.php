<?php
/*******************************************************************************
Fichier : utilisateur.class.php
Auteur : Maxime B�gin
Description :
    classe abstraire qui ne peut pas �tre utilis� directement
    elle contient les fonctions relative aux utilisateurs du site web de math enjeu
    c'est la classe de base pour les joueurs et les administrateurs
********************************************************************************
14-08-2006 Maxime B�gin - ajout du chiffrement de mot de passe
21-06-2006 Maxime B�gin - ajout de quelques commentaires
25-05-2006 Maxime B�gin - Version initiale
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
    //private $niveau;
    private $categorie;
    protected $cleConfirmation;
    protected $mysqli;


    //**************************************************************************
    //Somaire : met la cle � -1 ce qui signifie que le joueur n'est pas encore
    //          valide
    //Entr�e:
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
    // Sommaire:    V�rifier les invariants de la classe
    // Entr�e:
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
      //INVARIANT(($this->niveau > 0) && ($this->niveau < 15));
      INVARIANT(Courriel::validerCourriel($this->courriel));
      INVARIANT(eregi("^[a-zA-Z0-9_-]{4,8}$",$this->alias));

    }
    
    //**************************************************************************
    //Somaire : Valider un alias
    //Entr�e: $alias : l'alias � valider
    //Sortie: retourne vrai si l'alias est valide faux sinon
    //Note:
    //**************************************************************************
    function validerAlias($alias)
    {
      return (eregi("^[a-zA-Z0-9_-]{4,8}$",stripcslashes($alias)));
    }

    //**************************************************************************
    //Somaire : Valider un mot de passe
    //Entr�e: $alias : l'alias � valider
    //Sortie: retourne vrai si le mot de passe est valide faux sinon
    //Note: le mot de passe doit avoir entre 5 et caract�res
    //**************************************************************************
    static function validerMotDePasse($pass)
    {
      return (strlen($pass)>=5 && strlen($pass)<=20);
      //return true;
    }
    
    //**************************************************************************
    // Sommaire:        Assigner l'alias � l'utilisateur
    // Entr�e:          $alias
    // Sortie:          
    // Note:            une exception est lanc� si l'alias est invalide
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
    // Entr�e:          $courriel
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
    //Sommaire : Assigne les valeurs � l'utilisateur courant
    //Entr�e:
    //    $nom
    //    $prenom
    //    $alias
    //    $motDePasse
    //    $courriel
    //    $estConfirmer       : si le courriel est confirm� ou non
    //    $etablissement      : cl� de l'�tablissement
    //    $niveau             : le niveau scolaire
    //Sortie:
    //Note : on appel les fonctions d'assignations
    //**************************************************************************
    function asgUtilisateur($nom,$prenom,$alias,$motDePasse,$courriel,
        $estConfirmer,$etablissement)
    {
      $this->asgNom($nom);
      $this->asgPrenom($prenom);
      $this->asgAlias($alias);
      $this->asgMotDePasse($motDePasse);
      $this->asgCourriel($courriel);
      $this->asgEstConfirmer($estConfirmer);

      $this->asgEtablissement($etablissement);
      
      
      $this->INVARIANTS();
    }
    

    //**************************************************************************
    // Sommaire:        Assigner le mot de passe � l'utilisateur
    // Entr�e:          $motDePasse
    // Sortie:
    // Note:            une exception est lanc� si le mot de passe est invalide
    //**************************************************************************
    function asgMotDePasse($motDePasse)
    {
      $this->motDePasse=$motDePasse;
      POSTCONDITION($this->reqMotDePasse()==$motDePasse);
    }
    //**************************************************************************
    // Sommaire:        Assigner la cl� unique � l'utilisateur
    // Entr�e:          $cle
    // Sortie:
    // Note:            la cl� doit �tre > 0
    //**************************************************************************
    protected function asgCle($cle)
    {
      PRECONDITION($cle >= 0);
      $this->cle=$cle;
      POSTCONDITION($this->reqCle()==$cle);
    }

    //**************************************************************************
    // Sommaire:        Assigne le nom � l'utilisateur
    // Entr�e:          $nom
    // Sortie:
    // Note:            la longueur du nom ne doit pas �tre nulle
    //**************************************************************************
    function asgNom($nom)
    {
      PRECONDITION(strlen($nom)>0);
      $this->nom = $nom;
      POSTCONDITION($this->reqNom()==$nom);
    }

    //**************************************************************************
    // Sommaire:        Assigne le pr�nom � l'utilisateur
    // Entr�e:          $prenom
    // Sortie:
    // Note:            la longueur du pr�nom ne doit pas �tre nulle
    //**************************************************************************
    function asgPrenom($prenom)
    {
      PRECONDITION(strlen($prenom)>0);
      $this->prenom=$prenom;
      POSTCONDITION($this->reqPrenom()==$prenom);
    }

    //**************************************************************************
    // Sommaire:        asigne � l'utilisateur le status de confirmer ou non
    // Entr�e:          $estConfirmer : 0 pour non confirmer, 1 pour confirmer
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
    // Sommaire:        asigne � l'utilisateur le num�ro d'�tablissement
    // Entr�e:          $etablissement : le num�ro d'�tablissement
    // Sortie:
    // Note:            le # d'�tablissement doit �tre positif
    //**************************************************************************
    function asgEtablissement($etablissement)
    {
      PRECONDITION($etablissement >= 0);
      $this->etablissement=$etablissement;
      POSTCONDITION($this->reqEtablissement()==$etablissement);
    }

    
    
    //**************************************************************************
    // Sommaire:        asigne � l'utilisateur le niveau d'acc�s
	//				(cat�gorie de l'utilisateur)
    // Entr�e:          $acces
    // Sortie:
    // Note:            le niveau d'acc�s doit �tre entre 0 et 5 inclusivement
    //**************************************************************************    
    function asgCategorie($categorie)
    {
     	PRECONDITION($categorie>=0 && $categorie<=5);
		$this->categorie = $categorie;
		POSTCONDITION($this->reqCategorie() == $categorie);
	}
    
    //**************************************************************************
    // Sommaire:        g�n�rer une cha�ne al�atoire
    // Entr�e:          $longueur : la longueur de la cha�ne
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
    // Entr�e:          
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
    // Sommaire:        v�rifier si le mot de passe est valide
    // Entr�e:          $passChiffrer = le mot de passe chiffre
	//					$pass : le mot de passe non-chiffrer qu'on doit v�rifier
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


    //les fonctions � d�veloper dans les classes filles

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
    function reqCategorie(){
		return $this->categorie;
	}


}

