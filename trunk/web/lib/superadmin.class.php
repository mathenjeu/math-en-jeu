<?php
/*******************************************************************************
Fichier : superadmin.class.php
Auteur : Maxime Bégin
Description :
    classes servant à la gestion des super-admin.
    aucune fonction n'on été définie pour pouvoir ajouter des super-admin
    l'ajout se fait directement dans la base de données pour des raisons
    de sécurité.
********************************************************************************
15-08-2006 Maxime Bégin - Ajout du cryptage du mot de passe. Utilisation de la 
	fonction password() de MySQL.
13-07-2006 Maxime Bégin - ajout de quelques fonctions utile 
	pour le script d'installation
21-06-2006 Maxime Bégin - ajout de quelques commentaires
25-05-2006 Maxime Bégin - Version initiale
*******************************************************************************/

class SuperAdmin
{
  private $cle;
  private $nom;
  private $prenom;
  private $courriel;
  private $motDePasse;
  
  private $mysqli;
  
  function SuperAdmin($mysqli)
  {
    PRECONDITION(get_class($mysqli)=="mon_mysqli" || get_class($mysqli)=="mysqli" );
    $this->cle=0;
    $this->nom="";
    $this->prenom="";
    $this->courriel="";
    $this->motDePasse="";
    $this->mysqli=$mysqli;
  }
  
  function INVARIANTS()
  {
    INVARIANT($this->nom!="");
    INVARIANT($this->prenom!="");
    INVARIANT(Courriel::validerCourriel($this->courriel));
    INVARIANT(strlen($this->motDePasse)>5);
  }
  
  function asgSuperAdmin($nom,$prenom,$courriel,$motDePasse)
  {
    $this->asgNom($nom);
    $this->asgPrenom($prenom);
    $this->asgCourriel($courriel);
    $this->asgMotDePasse($motDePasse);
  }
  
  function asgNom($nom)
  {
    PRECONDITION($nom!="");
    $this->nom=$nom;
    POSTCONDITION($this->reqNom()==$nom);
  }
  function asgPrenom($prenom)
  {
    PRECONDITION($prenom!="");
    $this->prenom=$prenom;
    POSTCONDITION($this->reqPrenom()==$prenom);
  }
  function asgCourriel($courriel)
  {
    PRECONDITION(Courriel::validerCourriel($courriel));
    $this->courriel=$courriel;
    POSTCONDITION($this->reqCourriel()==$courriel);
  }
  function asgMotDePasse($pass)
  {
    //PRECONDITION(strlen($pass)>5);
    $this->motDePasse=$pass;
    POSTCONDITION($this->reqMotDePasse()==$pass);
  }
  function asgCle($cle)
  {
    PRECONDITION($cle>=0);
    $this->cle=$cle;
    POSTCONDITION($this->reqCle()==$cle);
  }
  
  function adminExiste($courriel)
  {
    $sql = "select courriel from superadmin where courriel='$courriel'";
    $result = $this->mysqli->query($sql);
    if($result->num_rows>0)
        return true;
    else
    	return false;
  }
  
  function insertionMySQL()
  {
    $this->INVARIANTS();
    $sql = "INSERT INTO superadmin(nom,prenom,courriel,motDePasse) 
	  		values('" . $this->nom . "','" . $this->prenom . "','" . 
		  	$this->courriel . "',password('" . $this->motDePasse . "'))";
	 $this->mysqli->query($sql);
    $this->asgCle($this->mysqli->insert_id);
  }

  function chargerMySQL($courriel,$motDePasse)
  {
    $sql="select * from superadmin where courriel='$courriel' and motDePasse=password('$motDePasse')";
    $result = $this->mysqli->query($sql);
    if($result->num_rows==0)
        return false;

    $row=$result->fetch_object();
    $this->asgCle($row->cleSuperadmin);
    $this->asgNom($row->nom);
    $this->asgPrenom($row->prenom);
    $this->asgCourriel($row->courriel);
    $this->asgMotDePasse($row->motDePasse);
    return true;
  }
  

  //*******************
  //fonctions de retour
  //*******************
  
  function reqCle()
  {
    return $this->cle;
  }
  function reqPrenom()
  {
    return $this->prenom;
  }
  function reqNom()
  {
    return $this->nom;
  }
  function reqCourriel()
  {
    return $this->courriel;
  }
  function reqMotDePasse()
  {
    return $this->motDePasse;
  }
}


