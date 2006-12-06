<?
/***********************************
utilisateur.testeur.php
Test de la classe utilisateur.
***********************************/

//require_once("../lib/joueur.class.php");
require_once("../lib/ini.php");

main();

function main()
{
  try
  {
    test_asgAlias();
    test_asgMotDePasse();
    test_genererChaineAleatoire();
    test_asgNom();
    test_asgPrenom();
    test_asgEstConfirmer();
    test_asgCourriel();
    test_asgEtablissement();
    test_asgNiveau();
    //test_asgCle();

    echo "La classe utilisateur fonctionne correctement.";
  }
  catch(ContratException $e)
  {
    echo $e->exception_dump();
  }

}

function test_asgAlias()
{
  //valide
  {
    $joueur = new Joueur($_SESSION["mysqli"]);
    $joueur->asgAlias("test1");
    ASSERTION($joueur->reqAlias()=="test1");
  }
  
  //invalide
  {
    $casLimite=false;
    try
    {
      $joueur = new Joueur($_SESSION["mysqli"]);
      $joueur->asgAlias("");
    }
    catch(Exception $e)
    {
      $casLimite=true;
    }
    ASSERTION($casLimite);
  }

  {
    $casLimite=false;
    try
    {
      $joueur = new Joueur($_SESSION["mysqli"]);
      $joueur->asgAlias("123");
    }
    catch(Exception $e)
    {
      $casLimite=true;
    }
    ASSERTION($casLimite);
  }
  
  {
    $casLimite=false;
    try
    {
      $joueur = new Joueur($_SESSION["mysqli"]);
      $joueur->asgAlias("*&?/23");
    }
    catch(Exception $e)
    {
      $casLimite=true;
    }
    ASSERTION($casLimite);
  }
  
}


function test_asgMotDePasse()
{
  //valide
  {
    $joueur = new Joueur($_SESSION["mysqli"]);
    $joueur->asgMotDePasse("test1");
    ASSERTION($joueur->reqMotDePasse()=="test1");
  }
  {
    $joueur = new Joueur($_SESSION["mysqli"]);
    $joueur->asgMotDePasse("?%/$*");
    ASSERTION($joueur->reqMotDePasse()=="?%/$*");
  }
  
  //invalide
  {
    $casLimite=false;
    try
    {
      $joueur = new Joueur($_SESSION["mysqli"]);
      $joueur->asgMotDePasse("");
    }
    catch(Exception $e)
    {
      $casLimite=true;
    }
    ASSERTION($casLimite);
  }
  
  {
    $casLimite=false;
    try
    {
      $joueur = new Joueur($_SESSION["mysqli"]);
      $joueur->asgMotDePasse("1234");
    }
    catch(Exception $e)
    {
      $casLimite=true;
    }
    ASSERTION($casLimite);
  }


}

function test_genererChaineAleatoire()
{
    $joueur = new Joueur($_SESSION["mysqli"]);
    $cle = $joueur->genererChaineAleatoire(30);
    ASSERTION(strlen($cle)==30);
}


function test_asgNom()
{
  //valide
  {
    $joueur = new Joueur($_SESSION["mysqli"]);
    $joueur->asgNom("salut");
    ASSERTION($joueur->reqNom()=="salut");
  }
  
  //invalide
  {
    $casLimite=false;
    try
    {
      $joueur = new Joueur($_SESSION["mysqli"]);
      $joueur->asgNom("");
    }
    catch(Exception $e)
    {
      $casLimite=true;
    }
    ASSERTION($casLimite);
  }
}


function test_asgPrenom()
{
  //valide
  {
    $joueur = new Joueur($_SESSION["mysqli"]);
    $joueur->asgPrenom("salut");
    ASSERTION($joueur->reqPrenom()=="salut");
  }

  //invalide
  {
    $casLimite=false;
    try
    {
      $joueur = new Joueur($_SESSION["mysqli"]);
      $joueur->asgPrenom("");
    }
    catch(Exception $e)
    {
      $casLimite=true;
    }
    ASSERTION($casLimite);
  }
}


function test_asgEstConfirmer()
{
  //valide
  {
    $joueur = new Joueur($_SESSION["mysqli"]);
    $joueur->asgEstConfirmer(0);
    ASSERTION($joueur->reqEstConfirmer()==0);
  }
  {
    $joueur = new Joueur($_SESSION["mysqli"]);
    $joueur->asgEstConfirmer(1);
    ASSERTION($joueur->reqEstConfirmer()==1);
  }

  //invalide
  {
    $casLimite=false;
    try
    {
      $joueur = new Joueur($_SESSION["mysqli"]);
      $joueur->asgEstConfirmer(2);
    }
    catch(Exception $e)
    {
      $casLimite=true;
    }
    ASSERTION($casLimite);
  }
}

function test_asgCourriel()
{
  {
    $joueur = new Joueur($_SESSION["mysqli"]);
    $joueur->asgCourriel("test@test.com");
    ASSERTION($joueur->reqCourriel()=="test@test.com");
  }


}

function test_asgEtablissement()
{
  {
    $joueur = new Joueur($_SESSION["mysqli"]);
    $joueur->asgEtablissement(0);
    ASSERTION($joueur->reqEtablissement()==0);
  }
  
  //invalide
  {
    $casLimite=false;
    try
    {
      $joueur = new Joueur($_SESSION["mysqli"]);
      $joueur->asgEtablissement(-2);
    }
    catch(Exception $e)
    {
      $casLimite=true;
    }
    ASSERTION($casLimite);
  }
}
function test_asgNiveau()
{
  {
    $joueur = new Joueur($_SESSION["mysqli"]);
    $joueur->asgNiveau(4);
    ASSERTION($joueur->reqNiveau()==4);
  }

  //invalide
  {
    $casLimite=false;
    try
    {
      $joueur = new Joueur($_SESSION["mysqli"]);
      $joueur->asgNiveau(-2);
    }
    catch(Exception $e)
    {
      $casLimite=true;
    }
    ASSERTION($casLimite);
  }
  {
    $casLimite=false;
    try
    {
      $joueur = new Joueur($_SESSION["mysqli"]);
      $joueur->asgNiveau(20);
    }
    catch(Exception $e)
    {
      $casLimite=true;
    }
    ASSERTION($casLimite);
  }

}

/*
function test_asgCle()
{
  //valide
  {
    $joueur = new Joueur;
    $joueur->asgCle(10);
    if($joueur->reqEstConfimer()!=10)
        throw new Exception("test_asgCle");
  }

  //invalide
  {
    $casLimite=false;
    try
    {
      $joueur = new Joueur;
      $joueur->asgCle(-4);
    }
    catch(Exception $e)
    {
      $casLimite=true;
    }
    if($casLimite==false) throw new Exception("test_asgCle");
  }
}

*/



?>
