<?php

require_once("../lib/courriel.class.php");

main();

function main()
{
  try
  {
    test_constructeur();
    test_asgSujet();
    test_asgMessage();
    test_asgHeader();
    test_asgAdresseDestination();
    tets_asgAdresseSource();
    test_envoyerCourriel();
    test_validerCourriel();

    echo "La classe courriel fonctionne correctement.";
  }
  catch(ContratException $e)
  {
    echo $e->exception_dump();
  }

}

function test_envoyerCourriel(){}

function test_constructeur()
{
  $courriel = new Courriel("test","test","maxime.begin@gmail.com","smac@smac.ulaval.ca");
  ASSERTION($courriel->reqSujet()=="test");
  ASSERTION($courriel->reqMessage()=="test");
  ASSERTION($courriel->reqAdresseSource()=="smac@smac.ulaval.ca");
  ASSERTION($courriel->reqAdresseDestination()=="maxime.begin@gmail.com");
}

function test_asgSujet()
{
  {
    $courriel = new Courriel("test","test","maxime.begin@gmail.com","smac@smac.ulaval.ca");
    $courriel->asgSujet("test2");
    ASSERTION($courriel->reqSujet()=="test2");
  }
  //invalide
  {
    $casLimite=false;
    try
    {
        $courriel = new Courriel("test","test","maxime.begin@gmail.com","smac@smac.ulaval.ca");
        $courriel->asgSujet("");
    }
    catch(Exception $e)
    {
      $casLimite=true;
    }
    ASSERTION($casLimite);
  }
}
function test_asgMessage()
{
  {
    $courriel = new Courriel("test","test","maxime.begin@gmail.com","smac@smac.ulaval.ca");
    $courriel->asgMessage("test2");
    ASSERTION($courriel->reqMessage()=="test2");
  }
  //invalide
  {
    $casLimite=false;
    try
    {
        $courriel = new Courriel("test","test","maxime.begin@gmail.com","smac@smac.ulaval.ca");
        $courriel->asgMessage("");
    }
    catch(Exception $e)
    {
      $casLimite=true;
    }
    ASSERTION($casLimite);
  }
}
function test_asgHeader()
{
  {
    $courriel = new Courriel("test","test","maxime.begin@gmail.com","smac@smac.ulaval.ca");
    $headers  = "From: bonjour\r\n";
    $headers .= "Content-type: text/html\r\n";
    $courriel->asgHeader($headers);
    ASSERTION($courriel->reqHeader()==$headers);
  }
  //invalide
  {
    $casLimite=false;
    try
    {
        $courriel = new Courriel("test","test","maxime.begin@gmail.com","smac@smac.ulaval.ca");
        $courriel->asgHeader("");
    }
    catch(Exception $e)
    {
      $casLimite=true;
    }
    ASSERTION($casLimite);
  }
}
function test_asgAdresseDestination()
{
  {
    $courriel = new Courriel("test","test","maxime.begin@gmail.com","smac@smac.ulaval.ca");
    $courriel->asgAdresseDestination("test2344@test456.com");
    ASSERTION($courriel->reqAdresseDestination()=="test2344@test456.com");
  }
  //invalide
  {
    $casLimite=false;
    try
    {
        $courriel = new Courriel("test","test","maxime.begin@gmail.com","smac@smac.ulaval.ca");
        $courriel->asgAdresseDestination("");
    }
    catch(Exception $e)
    {
      $casLimite=true;
    }
    ASSERTION($casLimite);
  }

}
function tets_asgAdresseSource()
{
  {
    $courriel = new Courriel("test","test","maxime.begin@gmail.com","smac@smac.ulaval.ca");
    $courriel->asgAdresseSource("test2344@test456.com");
    ASSERTION($courriel->reqAdresseSource()=="test2344@test456.com");
  }
  //invalide
  {
    $casLimite=false;
    try
    {
        $courriel = new Courriel("test","test","maxime.begin@gmail.com","smac@smac.ulaval.ca");
        $courriel->asgAdresseSource("");
    }
    catch(Exception $e)
    {
      $casLimite=true;
    }
    ASSERTION($casLimite);
  }
}

function test_validerCourriel()
{
  //valide
  {
    ASSERTION(Courriel::validerCourriel("test@test.com"));
  }

  {
    ASSERTION(Courriel::validerCourriel("test.test@test.test.com"));
  }


  {
    ASSERTION(Courriel::validerCourriel("_test_test@test.com"));
  }

  {
    ASSERTION(Courriel::validerCourriel("a@a9.aa"));
  }


  //invalide
  {
      ASSERTION(!Courriel::validerCourriel("test@-test.com"));
  }

  {
      ASSERTION(!Courriel::validerCourriel("@test.com"));
  }

  {
      ASSERTION(!Courriel::validerCourriel("test.@test.com"));
  }

}


?>
