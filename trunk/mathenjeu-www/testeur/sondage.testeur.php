<?

//require_once("../lib/sondage.class.php");
require_once("../lib/ini.php");

main();

function main()
{
  try
  {
    test_ReponseSondage_Constructeur();
    test_ReponseSondage_asgReponse();
    test_ReponseSondage_asgCleReponse();
    test_ReponseSondage_asgCompteur();
    test_ReponseSondage_chargerReponseMySQL();
    test_ReponseSondage_insertionReponseMySQL();
    test_ReponseSondage_miseAJourReponseMySQL();
    test_ReponseSondage_deleteReponseMySQL();
    test_ReponseSondage_ajoutReponseUtilisateurMySQL();
    test_ReponseSondage_afficherResultatReponse();

    echo "La Classe ReponseSondage fonctionne bien.<br>";

    test_Sondage_Constructeur();
    test_Sondage_asgCleSondage();
    test_Sondage_asgTitre();
    test_Sondage_asgDate();
    test_Sondage_ajoutReponse();
    test_Sondage_reqReponse();
    test_Sondage_chargerSondageMySQL();
    test_Sondage_chargerPlusRecentSondageMySQL();
    test_Sondage_ajoutChoixJoueur();
    test_Sondage_joueurDejaRepondu();
    test_Sondage_afficherResultatSondage();
    test_Sondage_afficherSondage();

    echo "La Classe Sondage fonctionne bien.";
  }
  catch(ContratException $e)
  {
    echo $e->exception_dump();
  }

}


function test_ReponseSondage_Constructeur()
{
  {
    $reponse = new ReponseSondage($_SESSION["mysqli"]);
    ASSERTION($reponse->reqCleReponse()==0);
    ASSERTION($reponse->reqReponse() == "");
    ASSERTION($reponse->reqCompteur()==0);
  }
}

function test_ReponseSondage_asgReponse()
{
  //valide
  {
    $reponse = new ReponseSondage($_SESSION["mysqli"]);
    $reponse->asgReponse("test");
    ASSERTION($reponse->reqReponse()=="test");
  }
  //invalide
  {
    $casLimite=false;
    try
    {
        $reponse = new ReponseSondage($_SESSION["mysqli"]);
        $reponse->asgReponse("");
    }
    catch(Exception $e)
    {
      $casLimite=true;
    }
    ASSERTION($casLimite);
  }
}

function test_ReponseSondage_asgCleReponse()
{
  //valide
  {
    $reponse = new ReponseSondage($_SESSION["mysqli"]);
    $reponse->asgCleReponse(2);
    ASSERTION($reponse->reqCleReponse()==2);
  }
  //invalide
  {
    $casLimite=false;
    try
    {
        $reponse = new ReponseSondage($_SESSION["mysqli"]);
        $reponse->asgCleReponse(-2);
    }
    catch(Exception $e)
    {
      $casLimite=true;
    }
    ASSERTION($casLimite);
  }
}

function test_ReponseSondage_asgCompteur()
{
  //valide
  {
    $reponse = new ReponseSondage($_SESSION["mysqli"]);
    $reponse->asgCompteur(4);
    ASSERTION($reponse->reqCompteur()==4);
  }
  //invalide
  {
    $casLimite=false;
    try
    {
        $reponse = new ReponseSondage($_SESSION["mysqli"]);
        $reponse->asgCompteur(-2);
    }
    catch(Exception $e)
    {
      $casLimite=true;
    }
    ASSERTION($casLimite);
  }
}

function test_ReponseSondage_chargerReponseMySQL()
{
  //valide : charger une réponse qui existe
  {
    $reponse = new ReponseSondage($_SESSION["mysqli"]);
    $reponse->chargerMySQL(1);
    ASSERTION($reponse->reqCleReponse()==1);
  }
  
  //invalide : charger une réponse qui existe pas
  {
    $casLimite=false;
    try
    {
        $reponse = new ReponseSondage($_SESSION["mysqli"]);
        $reponse->chargerMySQL(-2);
    }
    catch(Exception $e)
    {
      $casLimite=true;
    }
    ASSERTION($casLimite);
  }
  
}

function test_ReponseSondage_insertionReponseMySQL()
{
  //valide
  {
    $reponse = new ReponseSondage($_SESSION["mysqli"]);
    $reponse->asgReponse("testeur");
    $reponse->asgCompteur(999);
    $reponse->insertionMySQL(100000);
    
    $reponse->asgReponse("test");

    $reponse->chargerMySQL($reponse->reqCleReponse());
    ASSERTION($reponse->reqReponse()=="testeur");
    $reponse->deleteMySQL();
  }
  
  //invalide
  {
    $casLimite=false;
    try
    {
        $reponse = new ReponseSondage($_SESSION["mysqli"]);
        $reponse->insertionMySQL(10);
    }
    catch(Exception $e)
    {
      $casLimite=true;
    }
    ASSERTION($casLimite);
  }
}

function test_ReponseSondage_miseAJourReponseMySQL()
{
  //valide
  {
    $reponse = new ReponseSondage($_SESSION["mysqli"]);
    $reponse->asgReponse("testeur");
    $reponse->asgCompteur(999);
    $reponse->insertionMySQL(100000);
    
    $reponse->chargerMySQL($reponse->reqCleReponse());
    $reponse->asgReponse("test");
    $reponse->miseAJourMySQL();
    $reponse->chargerMySQL($reponse->reqCleReponse());
    
    ASSERTION($reponse->reqReponse()=="test");

    $reponse->deleteMySQL();
  }
  
  //invalide
  {
    $casLimite=false;
    try
    {
        $reponse = new ReponseSondage($_SESSION["mysqli"]);
        $reponse->miseAJourMySQL();
    }
    catch(Exception $e)
    {
      $casLimite=true;
    }
    ASSERTION($casLimite);
  }
}

function test_ReponseSondage_deleteReponseMySQL()
{
  {
    $reponse = new ReponseSondage($_SESSION["mysqli"]);
    $reponse->asgReponse("testeur");
    $reponse->asgCompteur(999);
    $reponse->insertionMySQL(100000);
    $cle = $reponse->reqCleReponse();
    $reponse->deleteMySQL();
    ASSERTION($reponse->chargerMySQL($cle)==false);

  }
}

function test_ReponseSondage_ajoutReponseUtilisateurMySQL(){}
function test_ReponseSondage_afficherResultatReponse(){}


//function de test pour la classe Sondage
function test_Sondage_Constructeur()
{
  {
    $sondage = new Sondage($_SESSION["mysqli"]);
    ASSERTION($sondage->reqDate()==date("Y-m-d"));
    ASSERTION($sondage->reqTitre()=="");
    ASSERTION($sondage->reqCleSondage()==0);
    ASSERTION($sondage->reqNbReponse()==0);
    ASSERTION($sondage->reqTotal()==0);
  }
}
function test_Sondage_asgCleSondage()
{
  //valide
  {
    $sondage = new Sondage($_SESSION["mysqli"]);
    $sondage->asgCleSondage(10);
    ASSERTION($sondage->reqCleSondage()==10);
  }
  
  //invalide
  {
    $casLimite=false;
    try
    {
        $sondage = new Sondage($_SESSION["mysqli"]);
        $sondage->asgCleSondage(0);
    }
    catch(Exception $e)
    {
      $casLimite=true;
    }
    ASSERTION($casLimite);
  }
}

function test_Sondage_asgTitre()
{
  //valide
  {
    $sondage = new Sondage($_SESSION["mysqli"]);
    $sondage->asgTitre("testeur");
    ASSERTION($sondage->reqTitre()=="testeur");
  }

  //invalide
  {
    $casLimite=false;
    try
    {
        $sondage = new Sondage($_SESSION["mysqli"]);
        $sondage->asgTitre("");
    }
    catch(Exception $e)
    {
      $casLimite=true;
    }
    ASSERTION($casLimite);
  }
}

function test_Sondage_asgDate()
{
  //valide
  {
    $sondage = new Sondage($_SESSION["mysqli"]);
    $sondage->asgDate(date("Y-m-d"));
    ASSERTION($sondage->reqDate()==date("Y-m-d"));
  }

  //invalide
  {
    $casLimite=false;
    try
    {
        $sondage = new Sondage($_SESSION["mysqli"]);
        $sondage->asgDate("2005-32-32");
    }
    catch(Exception $e)
    {
      $casLimite=true;
    }
    ASSERTION($casLimite);
  }
}

function test_Sondage_ajoutReponse()
{
    //valide
    {
      $reponse=new ReponseSondage($_SESSION["mysqli"]);
      $sondage = new Sondage($_SESSION["mysqli"]);
      $sondage->ajoutReponse($reponse);
      ASSERTION($sondage->reqNbReponse()==1);
    }
}

function test_Sondage_reqReponse()
{
  //valide
  {
      $reponse=new ReponseSondage($_SESSION["mysqli"]);
      $sondage = new Sondage($_SESSION["mysqli"]);
      $sondage->ajoutReponse($reponse);
      ASSERTION($sondage->reqReponse(1)==$reponse);
  }


  //invalide
  {
    $casLimite=false;
    try
    {
        $sondage = new Sondage($_SESSION["mysqli"]);
        $sondage->reqReponse(1);
    }
    catch(Exception $e)
    {
      $casLimite=true;
    }
    ASSERTION($casLimite);
  }
}

function test_Sondage_chargerSondageMySQL()
{
  //valide : charger un sondage qui existe
  {
      $sondage = new Sondage($_SESSION["mysqli"]);
      $sondage->chargerSondageMySQL(1);
      ASSERTION($sondage->reqCleSondage()==1);
  }
  //invalide
  {
    $casLimite=false;
    try
    {
        $sondage = new Sondage($_SESSION["mysqli"]);
        $sondage->chargerSondageMySQL(0);
    }
    catch(Exception $e)
    {
      $casLimite=true;
    }
    ASSERTION($casLimite);
  }
}

function test_Sondage_chargerPlusRecentSondageMySQL()
{
  //valide
  {
      $sondage = new Sondage($_SESSION["mysqli"]);
      $sondage->chargerPlusRecentSondageMySQL(array(0));
      
      $sql="select * from sondage order by dateSondage desc limit 1";
      $result = $_SESSION["mysqli"]->query($sql);
      $row=$result->fetch_object();
      
      ASSERTION($sondage->reqCleSondage()==$row->cleSondage);
  }
}
function test_Sondage_ajoutChoixJoueur()
{

    {
        $sondage = new Sondage($_SESSION["mysqli"]);
        $sondage->chargerPlusRecentSondageMySQL(array(0));
        $sondage->ajoutChoixJoueur(1000,1);
        
        $sql="select * from choixjoueursondage where cleUtilisateur=1000 and
            cleSondage=" . $sondage->reqCleSondage();
        $result = $_SESSION["mysqli"]->query($sql);

        ASSERTION($result->num_rows!=0);
            
        $sql="delete from choixjoueursondage where cleUtilisateur=1000 and
            cleSondage=" . $sondage->reqCleSondage();
        $_SESSION["mysqli"]->query($sql);
    }

}

function test_Sondage_joueurDejaRepondu()
{
  //valide
  {
    $sondage = new Sondage($_SESSION["mysqli"]);
    $sondage->chargerPlusRecentSondageMySQL(array(0));
    $sondage->ajoutChoixJoueur(1000,1);
    ASSERTION($sondage->joueurDejaRepondu(1000)==true);

    $sql="delete from choixjoueursondage where cleUtilisateur=1000 and
        cleSondage=" . $sondage->reqCleSondage();
    $_SESSION["mysqli"]->query($sql);
  }

  {
    $sondage = new Sondage($_SESSION["mysqli"]);
    $sondage->chargerPlusRecentSondageMySQL(array(0));
    ASSERTION($sondage->joueurDejaRepondu(0)==false);
  }
}
function test_Sondage_afficherResultatSondage(){}
function test_Sondage_afficherSondage(){}

?>
