<?php
/*******************************************************************************
Fichier : install.php
Auteur : Maxime Bégin
Description : Script d'installation pour le site web.
********************************************************************************
25-07-2006 Maxime Bégin - Modifcation pour l'ajout de fichier sql contenant des données
20-07-2006 Maxime Bégin	- Modification pour vérifier que le fichier de configuration
	est disponible en écriture.
12-07-2006 Maxime Bégin - Version initiale
*******************************************************************************/
if(version_compare(phpversion(),"5.0.0","<"))
{
	echo "Impossible d'installer : ce site requiert la version 5+ de php.";
	exit;
}

error_reporting(E_ALL ^ E_WARNING);
require_once("../lib/ini.php");

//définir les temps de session maximal
ini_set('session.gc_maxlifetime',0);
ini_set('session.cookie_lifetime',0);

//temps maximal d'exécution du script, 0 pour infini
set_time_limit(0);

main();

function main()
{

	global $lang;
	
	$smarty = new Smarty;
	$smarty->template_dir = DOC_ROOT . "/install/template/";
	$smarty->compile_dir = DOC_ROOT . 'lib/Smarty/templates_c';
	$smarty->clear_compiled_tpl();
	$smarty->clear_cache();
	$smarty->assign('lang',$lang);
	
	//lorsque le script d'installation est lancé pour la première fois
	if(!isset($_GET['etape'])) 
	{
		langage_dir($smarty,LANGAGE_DIR);
		$smarty->assign('etape',"0");
	}
	else //véfication de l'étape de l'installation
	{
	  $etape=$_GET['etape'];
	  
	  //première étape : 
	  if($etape==0)
	  {	
	     $configs = simplexml_load_file(CONFIG_FILE);
		 $configs->langue = $_POST['langue'];
		
		 //trouver l'adresse de base du site web
	     $chemin = "http://" . $_SERVER["HTTP_HOST"] . $_SERVER["REQUEST_URI"];
		 $chemin = substr($chemin,0,strrpos($chemin,"/"));
		 $chemin = substr($chemin,0,strrpos($chemin,"/")+1);
		 $configs->adresseWeb = $chemin;
		
		 ecrireXml($configs);
	     redirection('install.php?etape=1',0);
	     return;
	  }
	  elseif($etape==1)
	  {
	  	 if(!fopen(CONFIG_FILE, 'a'))
	  	 {
	  	 	$smarty->assign('erreur',$lang['install_erreur_ecriture_config']);
	  	 }
	     $smarty->assign('etape',2);
	  }
	  elseif($etape==3)
	  {
	     $erreur=0;
	     //essaie la connexion au serveur
		 $mysqli = new mon_mysqli($_POST['dbHote'],$_POST['dbUtilisateur'],$_POST['dbMotDePasse'],$_POST['dbSchema']);
	     if(mysqli_connect_errno())
	     {
		    $arr['db_connect'] = 0;
		    $erreur=1;
		  }
		  else //aucune erreur on peut donc procédé à la création des tables et à l'insertion des données
		  {
		     $arr['db_connect'] = 1;
		     $_SESSION['mysqli'] = $mysqli;
			 $ret = creationTable(DOC_ROOT . '/install/sql/table.sql',$arr,$mysqli,$smarty);
			 if($ret == true)
			 {	  	 
		  	 	if(!insertionDonnee(DOC_ROOT . '/install/sql/data/',$mysqli))
		  	 	{
		  	 		$erreur = true;
		  	 		$arr['insertion_donnee'] = 0;
		  	 	}
		  	 	else
		  	 	{
		  	 		$arr['insertion_donnee'] = 1;
		  	 	}
		  	 }
		  	 else
		  	 {
		  	 	$erreur = true;
		  	 }
	     }
	
	     //si aucune erreur importante, on inscrit les valeurs dans le fichier de configuration
		  if($erreur==0)
		  {
		     $configs = simplexml_load_file(CONFIG_FILE);
		     $configs->dbHote = $_POST['dbHote'];
			 $configs->dbUtilisateur = $_POST['dbUtilisateur'];
			 $configs->dbMotDePasse = $_POST['dbMotDePasse'];
			 $configs->dbSchema = $_POST['dbSchema'];
			 ecrireXml($configs);
		  }
	
		  $smarty->assign('erreur',$erreur);
		  $smarty->assign('info',$arr);
		  $smarty->assign('etape',3);
	    
	  }
	  elseif($etape==4)
	  {
	    $smarty->assign('etape',4);
	  }
	  elseif($etape==5)
	  {
	  	 if($_POST['admin_prenom']=="")
	    {
	    	$smarty->assign('etape',4);
	    	$smarty->assign('erreur',$lang['install_erreur_prenom']);
	    }
	    elseif($_POST['admin_nom']=="")
	    {
	    	$smarty->assign('etape',4);
	    	$smarty->assign('erreur',$lang['install_erreur_nom']);
	    }
	    elseif(!Courriel::validerCourriel($_POST['admin_courriel']))
	    {
	      $smarty->assign('etape',4);
	    	$smarty->assign('erreur',$lang['install_erreur_courriel']);
	    }
	    elseif(strlen($_POST['admin_password'])<5)
	    {
	      $smarty->assign('etape',4);
	    	$smarty->assign('erreur',$lang['install_erreur_password']);
	    }
	    else
	    {
	      $sa = new SuperAdmin($_SESSION['mysqli']);
	      if($sa->adminExiste($_POST['admin_courriel'])==false)
	      {
	      	$sa->asgSuperAdmin($_POST['admin_nom'],$_POST['admin_prenom'],$_POST['admin_courriel'],$_POST['admin_password']);
	      	$sa->insertionMySQL();
	      	$smarty->assign('etape',6);
		   }
		   else
		   {
		      $smarty->assign('erreur',$lang['install_admin_exist']);
		      $smarty->assign('etape',6);
		   }
		 }
	  }
	  elseif($etape==6)
	  {
	    $smarty->clear_compiled_tpl();
	    $smarty->clear_cache();
	    redirection('super-admin.php',0);
	  }
	  
	}
	
	$smarty->display('install.tpl');
	
}


/*******************************************************************************
Fonction : insertionDonnee($dir,&$mysqli)
Paramètre :
    - $dir : le dossier qui contient les fichiers
    - &$mysqli : adresse de l'object mysqli
Description : insertion des données dans la table
*******************************************************************************/
function insertionDonnee($dir,&$mysqli)
{
	//on charge les fichiers sql qui contiennent les données
 	if (is_dir($dir))
  	{
  		if ($dh = opendir($dir))
  		{
  		  //lit chaque fichier/dossier
 		  while (($file = readdir($dh)) !== false)
 		  {
 		    //on vérifie si c'est un fichier .sql
   			if(substr($file, count($file)-5,4)==".sql")
   			{
   				$sql = split(';',file_get_contents($dir . $file));
   				for($i=0;$i<count($sql);$i++)
   				{
   					try
   					{
   						//s'assurer que des lignes vide ne soit pas exécuté
   						if(strlen($sql[$i]) > 10)
   						{
   							$mysqli->query($sql[$i]);
   						}
   					}
   					catch(SQLException $e)
   					{
   						return false;
   					}
   				}
   			}
 		  }
  		}
	}
	return true;

}

/*******************************************************************************
Fonction : creationTable($fichier,&$arr,&$mysqli,&$smarty)
Paramètre :
    - $fichier : le fichier sql à exécuter.
    - &$arr : adresse du tableau utilisé pour contenir les informations 
    	sur les erreurs
    - &$mysqli : adresse de l'object mysqli
    - &$smarty : adresse de l'objet smarty
Description : on crée les tables
*******************************************************************************/
function creationTable($fichier,&$arr,&$mysqli,&$smarty)
{
	 //lecture du fichier .sql
  	 $sql = file_get_contents($fichier);
  	 if(!$sql)
  	 {
  		$arr['lecture_fichier'] = 0;
  		return false;
  	 }
  	 else
  	 {
  		$arr['lecture_fichier'] = 1;
  	 }
  	 
  	 //on sépare chaque création de table
  	 $sql = split(';',$sql);
  	 $arr['nb_table'] = count($sql);

	 //pour chaque table on effectue la requête
	 for($i=0;$i<count($sql)-1;$i++)
	 {
	 	 //on cherche le nom de la table
		 $tmp = spliti("`",$sql[$i]);
	    $arr['table_name'][$i] = $tmp[1];
	    
	    try
	    {
		   //on vérifie si la table existe déjà
		   $result = $mysqli->query("show tables like '".$tmp[1]."'");
		   if($result->num_rows>0)
			{
				$smarty->assign('erreur_table_exist',1);
			  	$arr["table_exist"][$i] = 1;
			  	$arr["table_check"][$i] = 0;
			}
			else // si la table n'existe pas on essaie de la créer
			{
			   $arr["table_exist"][$i] = 0;
				$mysqli->query($sql[$i]);
				$arr["table_check"][$i] = 1;
			}
	    }
	 	catch(SQLException $e)
	 	{
			return false;
	 	}
    }

	 return true;
}

/*******************************************************************************
Fonction : ecrireXml($object)
Paramètre :
    - $object : un object xml
Description : écrire les données dans le fichier xml
*******************************************************************************/
function ecrireXml($object)
{
  $fichier = fopen(CONFIG_FILE,'w');
  $contenu = "<?xml version ='1.0' encoding='UTF-8' ?>
<config>
";

  foreach($object as $cle => $valeur) 
  {
    $contenu .= "<$cle><![CDATA[$valeur]]></$cle>\r\n";
  }
  $contenu .= "</config>";
  fwrite($fichier,$contenu);  
  fclose($fichier);
}