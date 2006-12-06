<?
//require_once("../lib/nouvelles.class.php");
require_once("../lib/ini.php");

main();

function main()
{
  try
  {
    test_uneNouvelle_Constructeur();
    test_uneNouvelle_asgUneNouvelle();
    test_uneNouvelle_asgNouvelle();
    test_uneNouvelle_asgTitre();
    test_uneNouvelle_asgDate();
    test_uneNouvelle_asgCleNouvelle();

    test_uneNouvelle_chargerMySQL();
    test_uneNouvelle_insertionMySQL();
    test_uneNouvelle_miseAJourMySQL();
    test_uneNouvelle_deleteMySQL();
    echo "La Classe uneNouvelle fonctionne bien.<br>";
    
    test_Nouvelles_Constructeur();
    test_Nouvelles_ajoutNouvelle();
    test_Nouvelles_reqNouvelle();
    test_Nouvelles_chargerNouvellesMySQL();
    echo "La Classe Nouvelles fonctionne bien.";
  }
  catch(ContratException $e)
  {
    echo $e->exception_dump();
  }

}

function test_uneNouvelle_Constructeur()
{
  $uneNouvelle = new UneNouvelle($_SESSION["mysqli"]);
  ASSERTION($uneNouvelle->reqCle()==0);
}

function test_uneNouvelle_asgUneNouvelle()
{
  {
    $uneNouvelle = new UneNouvelle($_SESSION["mysqli"]);
    $uneNouvelle->asgUneNouvelle("test",date("Y-m-d"),"test",0,"test.png");
    ASSERTION($uneNouvelle->reqNouvelle()=="test");
    ASSERTION($uneNouvelle->reqDate()==date("Y-m-d"));
    ASSERTION($uneNouvelle->reqTitre() == "test");
  }

}
        
function test_uneNouvelle_asgNouvelle()
{
  //valide
  {
    $uneNouvelle = new UneNouvelle($_SESSION["mysqli"]);
    $uneNouvelle->asgNouvelle("test");
    ASSERTION($uneNouvelle->reqNouvelle()=="test");
  }
  
  //invalide
  {
    $casLimite=false;
    try
    {
        $uneNouvelle = new UneNouvelle($_SESSION["mysqli"]);
        $uneNouvelle->asgNouvelle("");
    }
    catch(Exception $e)
    {
      $casLimite=true;
    }
    ASSERTION($casLimite);
  }

}
function test_uneNouvelle_asgTitre()
{
  //valide
  {
    $uneNouvelle = new UneNouvelle($_SESSION["mysqli"]);
    $uneNouvelle->asgTitre("test");
    ASSERTION($uneNouvelle->reqTitre()=="test");
  }

  //invalide
  {
    $casLimite=false;
    try
    {
        $uneNouvelle = new UneNouvelle($_SESSION["mysqli"]);
        $uneNouvelle->asgTitre("");
    }
    catch(Exception $e)
    {
      $casLimite=true;
    }
    ASSERTION($casLimite);
  }
}
function test_uneNouvelle_asgDate()
{
  //valide
  {
    $uneNouvelle = new UneNouvelle($_SESSION["mysqli"]);
    $uneNouvelle->asgDate(date("Y-m-d"));
    ASSERTION($uneNouvelle->reqDate()==date("Y-m-d"));
  }

  //invalide
  {
    $casLimite=false;
    try
    {
        $uneNouvelle = new UneNouvelle($_SESSION["mysqli"]);
        $uneNouvelle->asgDate("2004-32-32");
    }
    catch(Exception $e)
    {
      $casLimite=true;
    }
    ASSERTION($casLimite);
  }
}
function test_uneNouvelle_asgCleNouvelle()
{
  //valide
  {
    $uneNouvelle = new UneNouvelle($_SESSION["mysqli"]);
    $uneNouvelle->asgCleNouvelle(200);
    ASSERTION($uneNouvelle->reqCle()==200);
  }

  //invalide
  {
    $casLimite=false;
    try
    {
        $uneNouvelle = new UneNouvelle($_SESSION["mysqli"]);
        $uneNouvelle->asgCleNouvelle(-5);
    }
    catch(Exception $e)
    {
      $casLimite=true;
    }
    ASSERTION($casLimite);
  }
}

function test_uneNouvelle_chargerMySQL()
{
  //valide : on charge une nouvelle qui existe
  {
    $uneNouvelle = new UneNouvelle($_SESSION["mysqli"]);
    $uneNouvelle->asgUneNouvelle("test",date("Y-m-d"),"test",0,"test.png");
    $uneNouvelle->insertionMySQL();
    $cle=$uneNouvelle->reqCle();
    
    $uneNouvelle = new UneNouvelle($_SESSION["mysqli"]);
    ASSERTION($uneNouvelle->chargerMySQL($cle));
    $uneNouvelle->deleteMySQL();
  }

  //invalide : une nouvelle qui n'existe pas
  {
    $uneNouvelle = new UneNouvelle($_SESSION["mysqli"]);
    ASSERTION(!$uneNouvelle->chargerMySQL(-1));
  }
}

function test_uneNouvelle_insertionMySQL()
{
  //valide
  {
    $uneNouvelle = new UneNouvelle($_SESSION["mysqli"]);
    $uneNouvelle->asgUneNouvelle("test",date("Y-m-d"),"test",0,"test.png");
    $uneNouvelle->insertionMySQL();
    ASSERTION($uneNouvelle->chargerMySQL($uneNouvelle->reqCle()));
    ASSERTION($uneNouvelle->reqTitre()=="test");
    $uneNouvelle->deleteMySQL();
  }
  
  //invalide
  {
    $casLimite=false;
    try
    {
        $uneNouvelle = new UneNouvelle($_SESSION["mysqli"]);
        $uneNouvelle->insertionMySQL();
    }
    catch(Exception $e)
    {
      $casLimite=true;
    }
    ASSERTION($casLimite);
  }
}

function test_uneNouvelle_miseAJourMySQL()
{
  //valide
  {
    $uneNouvelle = new UneNouvelle($_SESSION["mysqli"]);
    
    $uneNouvelle->asgUneNouvelle("test",date("Y-m-d"),"test",0,"test.png");
    $uneNouvelle->insertionMySQL();
    $uneNouvelle->asgTitre("Test2");
    $uneNouvelle->miseAJourMySQL();
    $uneNouvelle->chargerMySQL($uneNouvelle->reqCle());
    ASSERTION($uneNouvelle->reqTitre()=="Test2");
    $uneNouvelle->deleteMySQL();
  }
  
  //invalide
  {
    $casLimite=false;
    try
    {
        $uneNouvelle = new UneNouvelle($_SESSION["mysqli"]);
        $uneNouvelle->miseAJourMySQL();
    }
    catch(Exception $e)
    {
      $casLimite=true;
    }
    ASSERTION($casLimite);
  }
}


function test_uneNouvelle_deleteMySQL()
{
  //valide
  {
    $uneNouvelle = new UneNouvelle($_SESSION["mysqli"]);

    $uneNouvelle->asgUneNouvelle("test",date("Y-m-d"),"test",0,"test.png");
    $uneNouvelle->insertionMySQL();
    $uneNouvelle->deleteMySQL();
    ASSERTION(!$uneNouvelle->chargerMySQL($uneNouvelle->reqCle()));
  }
  
  //invalide
  {
    $uneNouvelle = new UneNouvelle($_SESSION["mysqli"]);
    ASSERTION(!$uneNouvelle->deleteMySQL());
  }

}



//fonction de test pour la classe Nouvelles
function test_Nouvelles_Constructeur()
{
  {
    $news = new Nouvelles($_SESSION["mysqli"]);
    ASSERTION($news->reqNbNouvelle()==0);
  }
}

function test_Nouvelles_ajoutNouvelle()
{
  {
    $uneNouvelle = new UneNouvelle($_SESSION["mysqli"]);
    $uneNouvelle->asgUneNouvelle("test",date("Y-m-d"),"test",0,"test.png");

    $news = new Nouvelles($_SESSION["mysqli"]);
    $news->ajoutNouvelle($uneNouvelle);
    ASSERTION($news->reqNbNouvelle()==1);
  }
}

function test_Nouvelles_reqNouvelle()
{
  //valide
  {
    $uneNouvelle = new UneNouvelle($_SESSION["mysqli"]);
    $uneNouvelle->asgUneNouvelle("test",date("Y-m-d"),"test",0,"test.png");

    $news = new Nouvelles($_SESSION["mysqli"]);
    $news->ajoutNouvelle($uneNouvelle);

    $uneNouvelle = $news->reqNouvelle(1);
    ASSERTION($uneNouvelle->reqNouvelle()=="test");
    ASSERTION($uneNouvelle->reqTitre()=="test");
    ASSERTION($uneNouvelle->reqDate()==date("Y-m-d"));
  }
  
  //invalide
  {
    $casLimite=false;
    try
    {
      $news = new Nouvelles($_SESSION["mysqli"]);
      $news->reqNouvelle(1);
    }
    catch(Exception $e)
    {
      $casLimite=true;
    }
    ASSERTION($casLimite);
  }
}

function test_Nouvelles_chargerNouvellesMySQL()
{
  //valide : il doit y avoir au moin une nouvelle dans la table
  {
    $news = new Nouvelles($_SESSION["mysqli"]);
    $news->chargerMySQL(1,array(0));
    ASSERTION($news->reqNbNouvelle()==1);
  }

}

?>
