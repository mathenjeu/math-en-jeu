<?php
/*******************************************************************************
Fichier : mon_mysqli.class.php
Auteur : Maxime Bégin
Description :
    extension de la classe mysqli pour automatiquement se connecté
    à la base de données
********************************************************************************
05-06-2006 Maxime Bégin - Version initiale
21-06-2006 Maxime Bégin - ajout de quelques commentaires
*******************************************************************************/

require_once("exception.class.php");

class mon_mysqli extends mysqli
{
    private $dbHote;
    private $dbUtilisateur;
    private $dbMotDePasse;
    private $dbSchema;

    //**************************************************************************
    // Sommaire:        Constructeur avec connexion à la BD
    // Entrée:          $hote : le serveur hote
    //                  $utilisateur : le nom d'utilisateur
    //                  $motDePasse : le mote de passe
    //                  $schema : le nom de la base de données
    // Sortie:
    // Note:
    //**************************************************************************
    function mon_mysqli($hote,$utilisateur,$motDePasse,$schema)
    {
      global $lang;
      $this->dbHote=$hote;
      $this->dbUtilisateur=$utilisateur;
      $this->dbMotDePasse=$motDePasse;
      $this->dbSchema=$schema;
      parent::__construct($hote,$utilisateur,$motDePasse,$schema);
    }

    //**************************************************************************
    // Sommaire:        redéfinition de la fonction query pour générer un exception
    //                  lorsque que la requête est invalide.
    // Entrée:          $requete : la requete sql à exécuter
    //
    // Sortie:
    // Note:            génère une exception de type sql si une erreur survient
    //**************************************************************************
    function query($requete)
    {
        $result = parent::query($requete);
        if(mysqli_error($this))
        {
         	$log = new clog(LOG_FILE);
         	$log->ecrire("Erreur SQL : " . $requete);

         	if(defined('SQL_DEBUG'))
         	{
            	throw new SQLException(mysqli_error($this), mysqli_errno($this), $requete);
            }
        }

        return $result;
    }

    //**************************************************************************
    // Sommaire:        retourne la liste des nom d'object qui doivent être conservé
    // Entrée:          
    // Sortie:
    // Note:
    //**************************************************************************
    function __sleep()
    {
        return array_keys(get_class_vars(get_class($this)));
    }

    //**************************************************************************
    // Sommaire:        s'assure de recréer la connexion à la base de donnée
    // Entrée:
    // Sortie:
    // Note:
    //**************************************************************************
    function __wakeup()
    {
      if($this->dbHote!="")
        $this->mon_mysqli($this->dbHote,
                        $this->dbUtilisateur,
                        $this->dbMotDePasse,
                        $this->dbSchema);
    }
    

}


