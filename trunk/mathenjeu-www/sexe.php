<?php

require_once("lib/ini.php");

main();

function main()
{
  $smarty = new MonSmarty();

  try
  {
    if(!isset($_SESSION["joueur"]))
    {
        redirection("nouvelles.php",0);
    }
    else
    {
      	$smarty->display('header.tpl');
      	if(isset($_POST['sexe']))
      	{
		  	$_SESSION['joueur']->asgSexe($_POST['sexe']);
			$_SESSION['joueur']->miseAJourMySQL();
			redirection("index.php",0);
	  	}
	  	else
		{
			$smarty->display('sexe.tpl');
		}
	  	$smarty->display('footer.tpl');
    }
  }
  catch(SQLException $e)
  {
    echo $e->exception_dump();
  }
  catch(MyException $e)
  {
    echo $e->exception_dump();
  }

}
?>