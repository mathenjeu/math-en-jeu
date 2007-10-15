<?php
/*******************************************************************************
Fichier : confirmation-courriel.php
Description : afficher la page pour la confirmation de l'adresse courriel
    le joueur re�oit dans un courriel � l'inscription  un lien vers cette page
*******************************************************************************/

require_once("lib/ini.php");

main();

function main()
{
  $smarty = new MonSmarty($_SESSION['langage']);
  global $lang;
  
  try
  {
    //on v�rifier s'il y a un num�ro de confirmation dans l'url
    if(!isset($_GET["ID"]))
    {
      //aucun num�ro de confirmation on retourne � la page principale
      redirection("index.php",0);
    }
    else
    {
      $smarty->assign('titre',$lang['titre_confirm_courriel']);
      $smarty->cache_lifetime = 0;
      $smarty->display("header.tpl");
      
      $joueur=new Joueur($_SESSION["mysqli"]);
      if(!$joueur->validerConfirmation($_GET["ID"]))
      {
        echo "<p class=erreur>" . $lang['cle_confirmation_invalide'] . "</p>";
        $smarty->cache_lifetime = -1;
        $smarty->display('footer.tpl');
      }
      else
      {
        $smarty->cache_lifetime = 0;
        $smarty->display('confirmation_courriel.tpl');
        
        $smarty->cache_lifetime = -1;
        $smarty->display('footer.tpl');
        redirection("login-joueur.php",10);
      }
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

