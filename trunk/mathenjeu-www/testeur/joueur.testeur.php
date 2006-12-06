<?php

//require_once("../lib/joueur.class.php");
//require_once("../lib/courriel.class.php");
require_once("../lib/ini.php");

main();

function main()
{
  try
  {
    test_Constructeur();
    test_asgVille();
    test_asgPays();
    test_asgProvince();
    test_asgDateInscription();
    test_asgCleGroupe();
    
    test_chargerUtilisateurMySQL();
    test_validerAliasUnique();
    test_validerCourrielUnique();
    
    test_insertionMySQL();
    test_miseAJourMySQL();
    
    test_calculNbPartieTempsJouee();
    
    
    echo "La classe joueur fonctionne correctement.";
  }
  catch(ContratException $e)
  {
    echo $e->exception_dump();
  }

}

function test_Constructeur()
{
  $joueur = new Joueur($_SESSION["mysqli"]);
  ASSERTION($joueur->reqCle()==0);
  ASSERTION($joueur->reqCleAdministrateur()==0);
  ASSERTION($joueur->reqCleGroupe()==0);
}

function test_asgVille()
{
  //valide
  {
    $joueur = new Joueur($_SESSION["mysqli"]);
    $joueur->asgVille("test");
    ASSERTION($joueur->reqVille()=="test");
  }
  
  //invalide
  {
    $casLimite=false;
    try
    {
      $joueur = new Joueur($_SESSION["mysqli"]);
      $joueur->asgVille("");
    }
    catch(Exception $e)
    {
      $casLimite=true;
    }
    ASSERTION($casLimite);
  }
        
}

function test_asgPays()
{
  //valide
  {
    $joueur = new Joueur($_SESSION["mysqli"]);
    $joueur->asgPays("test");
    ASSERTION($joueur->reqPays()=="test");
  }
  
  //invalide
  {
    $casLimite=false;
    try
    {
      $joueur = new Joueur($_SESSION["mysqli"]);
      $joueur->asgPays("");
    }
    catch(Exception $e)
    {
      $casLimite=true;
    }
    ASSERTION($casLimite);
  }
}

function test_asgProvince()
{

  //valide
  {
    $joueur = new Joueur($_SESSION["mysqli"]);
    $joueur->asgProvince("test");
    ASSERTION($joueur->reqProvince()=="test");
  }

  //invalide
  {
    $casLimite=false;
    try
    {
      $joueur = new Joueur($_SESSION["mysqli"]);
      $joueur->asgProvince("");
    }
    catch(Exception $e)
    {
      $casLimite=true;
    }
    ASSERTION($casLimite);
  }

}

function test_asgDateInscription()
{
  //valide
  {
    $casLimite=true;
    try
    {
        $joueur = new Joueur($_SESSION["mysqli"]);
        $joueur->asgDateInscription(date("Y-m-d"));
    }
    catch(Exception $e)
    {
      $casLimite=false;
    }
    ASSERTION($casLimite);
  }
  
  //invalide
  {
    $casLimite=false;
    try
    {
      $joueur = new Joueur($_SESSION["mysqli"]);
      $joueur->asgDateInscription(date("2000-32-32"));
    }
    catch(Exception $e)
    {
      $casLimite=true;
    }
    ASSERTION($casLimite);
  }
}

function test_asgCleGroupe()
{
  //valide
  {
    $casLimite=true;
    try
    {
        $joueur = new Joueur($_SESSION["mysqli"]);
        $joueur->asgCleGroupe(10);
    }
    catch(Exception $e)
    {
      $casLimite=false;
    }
    ASSERTION($casLimite);
  }

  //invalide
  {
    $casLimite=false;
    try
    {
      $joueur = new Joueur($_SESSION["mysqli"]);
      $joueur->asgCleGroupe(-2);
    }
    catch(Exception $e)
    {
      $casLimite=true;
    }
    ASSERTION($casLimite);
  }
}



function test_chargerUtilisateurMySQL()
{
  //valide : charger un utilisateur qui est dans la base de données
  {
    $joueur = new Joueur($_SESSION["mysqli"]);
    $joueur->asgJoueur("test","test","testeur","testeur","111111@q11111.com",
        0,0,2,"test","test", "test",date("Y-m-d"),1,1,1,1);
    ASSERTION($joueur->insertionMySQL());
    ASSERTION($joueur->chargerMySQL("testeur","testeur"));
    $joueur->deleteMySQL();
  }

  //invalide : charger un utilisateur qui n'est pas dans la base de donées
  {
    $joueur = new Joueur($_SESSION["mysqli"]);
    ASSERTION(!$joueur->chargerMySQL("m","m"));
  }
}

function test_validerAliasUnique()
{
  //valide
  {
    $joueur = new Joueur($_SESSION["mysqli"]);
    ASSERTION($joueur->validerAliasUnique("m"));
  }
  
  //invalide : choisir un alias déjà dans la table joueur
  {
    $joueur = new Joueur($_SESSION["mysqli"]);
    $joueur->asgJoueur("test","test","test122","test122","beginmaxime@hotmail.com",
        0,0,2,"test","test", "test",date("Y-m-d"),1,1,1,1);
    $joueur->insertionMySQL();
    ASSERTION(!$joueur->validerAliasUnique("test122"));
    $joueur->deleteMySQL();
  }
}

function test_validerCourrielUnique()
{
  //valide : un courriel qui n'est pas dans la table joueur
  {
    $joueur = new Joueur($_SESSION["mysqli"]);
    ASSERTION($joueur->validerCourrielUnique("-@-.-"));
  }

  //invalide : choisir un courriel déjà dans la table joueur
  {
    $joueur = new Joueur($_SESSION["mysqli"]);
    $joueur->asgJoueur("test","test","test122","test122","beginmaxime@hotmail.com",
        0,0,2,"test","test", "test",date("Y-m-d"),1,1,1,1);
    $joueur->insertionMySQL();
    ASSERTION(!$joueur->validerCourrielUnique("beginmaxime@hotmail.com"));
    $joueur->deleteMySQL();
    
  }
}


function test_insertionMySQL()
{
  //valide
  {
    $joueur = new Joueur($_SESSION["mysqli"]);
    $joueur->asgJoueur("test","test","test122","test122","beginmaxime@hotmail.com",
        0,0,2,"test","test", "test",date("Y-m-d"),1,1,1,1);
    $joueur->insertionMySQL();
    $joueur->chargerMySQL("test122","test122");
    ASSERTION($joueur->reqNom()=="test");
    $joueur->deleteMySQL();
  }

}

function test_miseAJourMySQL()
{
  //valide
  {
    $joueur = new Joueur($_SESSION["mysqli"]);
    $joueur->asgJoueur("test","test","test122","test122","beginmaxime@hotmail.com",
        0,0,2,"test","test", "test",date("Y-m-d"),1,1,1,1);
    $joueur->insertionMySQL();
    $joueur->chargerMySQL("test122","test122");
    $joueur->asgNom("bonjour");
    $joueur->miseAJourMySQL();
    ASSERTION($joueur->reqNom()=="bonjour");
    $joueur->deleteMySQL();
  }

}

function test_envoyerCourrielConfirmation(){}

function test_calculNbPartieTempsJouee()
{
  //valide
  {
    $joueur = new Joueur($_SESSION["mysqli"]);
    $joueur->asgJoueur("test","test","test122","test122","beginmaxime@hotmail.com",
        0,0,2,"test","test", "test",date("Y-m-d"),1,1,1,1);
    $joueur->insertionMySQL();
    $joueur->chargerMySQL("test122","test122");
    $joueur->calculNbPartieTempsJouee();
    ASSERTION($joueur->reqNbVictoire()==0);
    ASSERTION($joueur->reqTempsPartie()==0);
    ASSERTION($joueur->reqPartiesCompletes()==0);
  }

}

?>


