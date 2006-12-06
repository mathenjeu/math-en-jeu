<?
//testeur de la classe groupe

//require_once("../lib/groupe.class.php");
//require_once("../lib/joueur.class.php");
require_once("../lib/ini.php");


main();

function main()
{
  try
  {
    test_unGroupe_Constructeur();
    test_unGroupe_asgCle();
    test_unGroupe_asgDureePartie();
    test_unGroupe_asgClavardage();
    test_unGroupe_asgNom();
    
    test_unGroupe_asgGroupe();
    test_unGroupe_asgBanqueQuestions();
    
    test_unGroupe_ajouterJoueur();
    test_unGroupe_enleverJoueur();
    test_unGroupe_chargerMySQL();
    test_unGroupe_insertionMySQL();
    test_unGroupe_miseAJourMySQL();
    test_unGroupe_deleteMySQL();
    
    echo "La Classe UnGroupe fonctionne bien.<br>";
    
    test_groupes_constructeur();
    test_groupes_ajoutGroupes();
    test_groupes_enleverGroupe();
    test_groupes_asgCleAdministrateur();
    test_groupes_chargerMySQL();
    test_groupes_reqGroupe();
    echo "La Classe Groupes fonctionne bien.";
  }
  catch(ContratException $e)
  {
    echo $e->exception_dump();
  }

}
function test_unGroupe_asgBanqueQuestions(){}
function test_unGroupe_asgGroupe(){}

function test_unGroupe_Constructeur()
{
  $unGroupe = new UnGroupe($_SESSION["mysqli"]);
  ASSERTION($unGroupe->reqCle()==0);
}

function test_unGroupe_asgCle()
{
  //valide
  {
    $unGroupe = new UnGroupe($_SESSION["mysqli"]);
    $unGroupe->asgCle(10);
    ASSERTION($unGroupe->reqCle()==10);
  }

  //invalide
  {
    $casLimite=false;
    try
    {
        $unGroupe = new UnGroupe($_SESSION["mysqli"]);
        $unGroupe->asgCle(-1);
    }
    catch(Exception $e)
    {
      $casLimite=true;
    }
    ASSERTION($casLimite);
  }

}
function test_unGroupe_asgDureePartie()
{
  //valide
  {
    $unGroupe = new UnGroupe($_SESSION["mysqli"]);
    $unGroupe->asgDureePartie(10);
    ASSERTION($unGroupe->reqDureePartie()==10);
  }
  //invalide
  {
    $casLimite=false;
    try
    {
        $unGroupe = new UnGroupe($_SESSION["mysqli"]);
        $unGroupe->asgDureePartie(-5);
    }
    catch(Exception $e)
    {
      $casLimite=true;
    }
    ASSERTION($casLimite);
  }
}
function test_unGroupe_asgClavardage()
{
  //valide
  {
    $unGroupe = new UnGroupe($_SESSION["mysqli"]);
    $unGroupe->asgClavardage(true);
    ASSERTION($unGroupe->reqClavardage()==true);
  }
  //invalide
  {
    $casLimite=false;
    try
    {
        $unGroupe = new UnGroupe($_SESSION["mysqli"]);
        $unGroupe->asgClavardage(4);
    }
    catch(Exception $e)
    {
      $casLimite=true;
    }
    ASSERTION($casLimite);
  }
}
function test_unGroupe_asgNom()
{
  //valide
  {
    $unGroupe = new UnGroupe($_SESSION["mysqli"]);
    $unGroupe->asgNom("testeur");
    ASSERTION($unGroupe->reqNom()=="testeur");
  }
  //invalide
  {
    $casLimite=false;
    try
    {
        $unGroupe = new UnGroupe($_SESSION["mysqli"]);
        $unGroupe->asgNom("");
    }
    catch(Exception $e)
    {
      $casLimite=true;
    }
    ASSERTION($casLimite);
  }
}

function test_unGroupe_ajouterJoueur()
{
    //valide
    {
      $unGroupe = new UnGroupe($_SESSION["mysqli"]);
      $joueur = new Joueur($_SESSION["mysqli"]);
      $unGroupe->ajouterJoueur($joueur);
      ASSERTION($unGroupe->reqJoueur(1)->reqCle() == $joueur->reqCle());
      ASSERTION($unGroupe->reqNbJoueur()==1);
    }
    //invalide
    {
      $casLimite=false;
      try
      {
        $unGroupe = new UnGroupe($_SESSION["mysqli"]);
        $unGroupe->ajouterJoueur(false);
      }
      catch(Exception $e)
      {
        $casLimite=true;
      }
      ASSERTION($casLimite);
    }
}
function test_unGroupe_enleverJoueur()
{
    //valide
    {
        $unGroupe = new UnGroupe($_SESSION["mysqli"]);
        $joueur = new Joueur($_SESSION["mysqli"]);
        $unGroupe->ajouterJoueur($joueur);
        $unGroupe->enleverJoueur(1);
        ASSERTION($unGroupe->reqNbJoueur()==0);
    }
    //invalide
    {
        $casLimite=false;
        try
        {
            $unGroupe = new UnGroupe($_SESSION["mysqli"]);
            $joueur = new Joueur($_SESSION["mysqli"]);
            $unGroupe->ajouterJoueur($joueur);
            $unGroupe->enleverJoueur(2);
        }
        catch(Exception $e)
        {
          $casLimite=true;
        }
        ASSERTION($casLimite);
    }
}
function test_unGroupe_chargerMySQL()
{
    //valide
    {
        $unGroupe = new UnGroupe($_SESSION["mysqli"]);
        $unGroupe->asgGroupe("testeur",true,30,0,0,1);
        $unGroupe->insertionMySQL();
        $cle=$unGroupe->reqCle();
        
        $unGroupe = new UnGroupe($_SESSION["mysqli"]);
        $unGroupe->chargerMySQL($cle);
        ASSERTION($unGroupe->reqCle()==$cle);
        $unGroupe->deleteMySQL();
    }
    //invalide
    {
        $casLimite=false;
        try
        {
            $unGroupe = new UnGroupe($_SESSION["mysqli"]);
            $unGroupe->chargerMySQL(-2);
        }
        catch(Exception $e)
        {
          $casLimite=true;
        }
        ASSERTION($casLimite);
    }
}
function test_unGroupe_insertionMySQL()
{
    //valide
    {
        $unGroupe = new UnGroupe($_SESSION["mysqli"]);
        $unGroupe->asgGroupe("testeur",true,30,0,0,1);
        $unGroupe->insertionMySQL();
        
        $cle = $unGroupe->reqCle();
        $unGroupe = new UnGroupe($_SESSION["mysqli"]);
        $unGroupe->chargerMySQL($cle);
        
        ASSERTION($unGroupe->reqNom()=="testeur");
        $unGroupe->deleteMySQL();
    }
    //invalide
    {
        $casLimite=false;
        $unGroupe = new UnGroupe($_SESSION["mysqli"]);
        try
        {
            $unGroupe->asgGroupe("testeur",true,30,0,0,1);
            $unGroupe->insertionMySQL();
            $unGroupe->insertionMySQL();
        }
        catch(Exception $e)
        {
          $unGroupe->deleteMySQL();
          $casLimite=true;
        }
        ASSERTION($casLimite);
    }
}

function test_unGroupe_miseAJourMySQL()
{
    //valide
    {
        $unGroupe = new UnGroupe($_SESSION["mysqli"]);
        $unGroupe->asgGroupe("testeur",true,30,0,0,1);
        $unGroupe->insertionMySQL(10);
        $unGroupe->asgNom("testeur2");
        $unGroupe->miseAJourMySQL();
        
        $cle = $unGroupe->reqCle();
        $unGroupe = new UnGroupe($_SESSION["mysqli"]);
        $unGroupe->chargerMySQL($cle);
        ASSERTION($unGroupe->reqNom()=="testeur2");
        $unGroupe->deleteMySQL();
    }
    //invalide
    {
        $casLimite=false;
        try
        {
            $unGroupe = new UnGroupe($_SESSION["mysqli"]);
            $unGroupe->miseAJourMySQL();
        }
        catch(Exception $e)
        {
          $casLimite=true;
        }
        ASSERTION($casLimite);
    }
}

function test_unGroupe_deleteMySQL()
{
    //valide
    {
        $unGroupe = new UnGroupe($_SESSION["mysqli"]);
        $unGroupe->asgGroupe("testeur",true,30,0,0,1);
        $unGroupe->insertionMySQL(10);
        $cle = $unGroupe->reqCle();
        $unGroupe->deleteMySQL();
        ASSERTION($unGroupe->chargerMySQL($cle)==false);


    }
    //invalide
    {
        $casLimite=false;
        try
        {
            $unGroupe = new UnGroupe($_SESSION["mysqli"]);
            $unGroupe->deleteMySQL();
        }
        catch(Exception $e)
        {
          $casLimite=true;
        }
        ASSERTION($casLimite);
    }
}




function test_groupes_constructeur()
{
  //valide
  {
    $groupes = new Groupes($_SESSION["mysqli"]);
    ASSERTION($groupes->reqNbGroupe()==0);
  }
}

function test_groupes_ajoutGroupes()
{
  //valide
  {
    $groupes = new Groupes($_SESSION["mysqli"]);
    $unGroupe = new UnGroupe($_SESSION["mysqli"]);
    $unGroupe->asgGroupe("testeur",true,30,0,0,1);
    $groupes->ajoutGroupe($unGroupe);
    ASSERTION($groupes->reqGroupe(1)->reqNom()=="testeur");
    ASSERTION($groupes->reqNbGroupe()==1);
  }
  //invalide
  {
    $casLimite=false;
    try
    {
        $groupes = new Groupes($_SESSION["mysqli"]);
        $groupes->ajoutGroupe(1);
    }
    catch(Exception $e)
    {
        $casLimite=true;
    }
    ASSERTION($casLimite);
  }
}
function test_groupes_enleverGroupe()
{
  //valide
  {
    $groupes = new Groupes($_SESSION["mysqli"]);
    $unGroupe = new UnGroupe($_SESSION["mysqli"]);
    $unGroupe->asgGroupe("testeur",true,30,0,0,1);
    $groupes->ajoutGroupe($unGroupe);
    $groupes->enleverGroupe(1);
    ASSERTION($groupes->reqNbGroupe()==0);
  }
  //invalide
  {
    $casLimite=false;
    try
    {
        $groupes = new Groupes($_SESSION["mysqli"]);
        $groupes->enleverGroupe(1);
    }
    catch(Exception $e)
    {
        $casLimite=true;
    }
    ASSERTION($casLimite);
  }
}
function test_groupes_asgCleAdministrateur()
{
  //valide
  {
    $groupes = new Groupes($_SESSION["mysqli"]);
    $groupes->asgCleAdministrateur(10);
    ASSERTION($groupes->reqCleAdministrateur()==10);
  }
  //invalide
  {
    $casLimite=false;
    try
    {
        $groupes = new Groupes($_SESSION["mysqli"]);
        $groupes->asgCleAdministrateur(-10);
    }
    catch(Exception $e)
    {
        $casLimite=true;
    }
    ASSERTION($casLimite);
  }
}
function test_groupes_chargerMySQL()
{
  //valide
  {
    $groupes = new Groupes($_SESSION["mysqli"]);
    $unGroupe = new UnGroupe($_SESSION["mysqli"]);
    $unGroupe->asgGroupe("testeur",true,30,0,0,1);
    $unGroupe->insertionMySQL();
    $groupes->asgCleAdministrateur(1);
    $groupes->chargerMySQL();
    ASSERTION($groupes->reqNbGroupe()>=1);
    $unGroupe->deleteMySQL();
  }
}
function test_groupes_reqGroupe()
{
  //valide
  {
    $groupes = new Groupes($_SESSION["mysqli"]);
    $unGroupe = new UnGroupe($_SESSION["mysqli"]);
    $unGroupe->asgGroupe("testeur",true,30,0,0,1);
    $groupes->ajoutGroupe($unGroupe);
    ASSERTION($groupes->reqGroupe(1)->reqNom()=="testeur");
    
  }
  //invalide
  {
    $casLimite=false;
    try
    {
        $groupes = new Groupes($_SESSION["mysqli"]);
        $groupes->reqGroupe(1);
    }
    catch(Exception $e)
    {
        $casLimite=true;
    }
    ASSERTION($casLimite);
  }
}
?>

