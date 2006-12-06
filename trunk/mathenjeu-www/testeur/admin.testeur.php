<?php
//admin.testeur.php
//test de la classe admin.class
//require_once("../lib/admin.class.php");
require_once("../lib/ini.php");

main();

function main()
{
    try
    {
        test_constructeur();
        test_chargerMySQL();
        test_genererCleConfirmation();
        test_validerAliasUnique();
        test_validerCourrielUnique();
        test_insertionMySQL();
        test_miseAJourMySQL();
        test_deleteMySQL();
        test_envoyerCourrielConfirmation();
        test_envoyerCourrielInfoPerdu();
        
        echo "La classe admin est valide.";
    }
    catch(ContratException $e)
    {
        echo $e->exception_dump();
    }
}

function test_envoyerCourrielConfirmation(){}
function test_envoyerCourrielInfoPerdu(){}


function test_constructeur()
{
  //valide
  {
    $admin=new Admin($_SESSION["mysqli"]);
    ASSERTION($admin->reqCle()==0);
  }
}

function test_chargerMySQL()
{
  //valide : charger un utilisateur qui est dans la base de données
  {
    $admin = new Admin($_SESSION["mysqli"]);
    ASSERTION($admin->chargerMySQL("maxime","maxime"));
  }

  //invalide : charger un utilisateur qui n'est pas dans la base de donées
  {
    $casLimite=false;
    try
    {
        $admin = new Admin($_SESSION["mysqli"]);
        ASSERTION($admin->chargerMySQL("m","m"));
    }
    catch(ContratException $e)
    {
      $casLimite=true;
    }
    ASSERTION($casLimite);

  }
}

function test_genererCleConfirmation()
{
  {
    $admin = new Admin($_SESSION["mysqli"]);
    $admin->genererCleConfirmation();
    $sql = "select * from administrateur where cleConfirmation='" .
        $admin->reqCleConfirmation() . "'";
    $result = $_SESSION["mysqli"]->query($sql);
    ASSERTION($result->num_rows==0);
        
  }
}

function test_validerAliasUnique()
{
  //valide
  {
    $admin = new Admin($_SESSION["mysqli"]);
    ASSERTION($admin->validerAliasUnique("m"));
  }

  //invalide : choisir un alias déjà dans la table joueur
  {
    $casLimite=false;
    try
    {
        $admin = new Admin($_SESSION["mysqli"]);
        ASSERTION($admin->validerAliasUnique("maxime"));
    }
    catch(ContratException $e)
    {
      $casLimite=true;
    }
    ASSERTION($casLimite);
  }
}

function test_validerCourrielUnique()
{
  //valide : un courriel qui n'est pas dans la table joueur
  {
    $admin = new Admin($_SESSION["mysqli"]);
    ASSERTION($admin->validerCourrielUnique("-@-.-"));
  }

  //invalide : choisir un courriel déjà dans la table joueur
  {
    $casLimite=false;
    try
    {
        $admin = new Admin($_SESSION["mysqli"]);
        ASSERTION($admin->validerCourrielUnique("kimola4@gmail.com"));
    }
    catch(ContratException $e)
    {
      $casLimite=true;
    }
    ASSERTION($casLimite);
  }
}

function test_insertionMySQL()
{
  {
    $admin = new Admin($_SESSION["mysqli"]);
    $admin->asgAdmin("test","test","testeur","testeur","test123@test123.com",0,0,10);
    $admin->insertionMySQL();
    
    $admin2 = new Admin($_SESSION["mysqli"]);
    $admin2->chargerMySQL($admin->reqAlias(),$admin->reqMotDePasse());
    ASSERTION($admin2->reqCourriel()=="test123@test123.com");
    $admin->deleteMySQL();
  }

}


function test_miseAJourMySQL()
{
  //valide
  {
    $admin = new Admin($_SESSION["mysqli"]);
    $admin->asgAdmin("test","test","testeur","testeur","test123@test123.com",0,0,10);
    $admin->insertionMySQL();
    $admin->asgNom("test2");
    $admin->miseAJourMySQL();
    
    $admin->chargerMySQL($admin->reqAlias(),$admin->reqMotDePasse());
    ASSERTION($admin->reqNom()=="test2");
    $admin->deleteMySQL();
  }

}

function test_deleteMySQL()
{
  //valide
  {
    $admin = new Admin($_SESSION["mysqli"]);
    $admin->asgAdmin("test","test","testeur","testeur","test123@test123.com",0,0,10);
    $admin->insertionMySQL();
    $admin->deleteMySQL();
    ASSERTION(!$admin->chargerMySQL("testeur","testeur"));

  }
}

?>
