<?php
/*******************************************************************************
Fichier : index.php
Auteur : Maxime Bégin
Description : affiche l'index
********************************************************************************
10-06-2006 Maxime Bégin - Version initiale
*******************************************************************************/

require_once("lib/ini.php");

main();

function main()
{
  try
  {
	$smarty = new MonSmarty;
	global $lang;
	
	if(isset($_SESSION["joueur"]))
	{
	 	//vérifie que l'utilisateur peut être ici
	 	if($_SESSION["joueur"]->reqCategorie()<1)
	 	{
			redirection('index.php',0);
			return;
		}
		$smarty->assign('connecter',1); 
		$smarty->assign('alias',$_SESSION["joueur"]->reqAlias());
		$smarty->assign('motDePasse',$_SESSION["joueur"]->reqMotDePasse());
		$smarty->assign('acces',$_SESSION["joueur"]->reqCategorie());
	}
	else
	{
		redirection('index.php',0);
		return;
	}

	$smarty->assign('titre',$lang['titre_ajout_question']);
	$smarty->cache_lifetime = 0;
	$smarty->display('header.tpl');
	if(isset($_SESSION['css']))
	{
		$smarty->assign('css',$_SESSION['css']);
	}
	$smarty->cache_lifetime = 0;
	$smarty->display('menu.tpl');
	
	$smarty->cache_lifetime = 0;
	
	//vérifier l'action à effectué
	if(isset($_GET['action']))
	{
	    $action=$_GET['action'];
	    //on veut modifier une question
		if($action=="modification")
		{
		 	$sql = "select * from question where cleQuestion=" . $_GET['cleQuestion'];
		 	$result = $_SESSION['mysqli']->query($sql);
		 	$row=$result->fetch_object();
		 	if(($row->cleJoueur==$_SESSION['joueur']->reqCle()) || ($_SESSION['joueur']->reqCategorie()>=2))
		 	{
				modificationQuestion($smarty,$_GET['cleQuestion']);
			}
		}
		//on veut soumettre les modifications sur les questions
		elseif($action=="mod_soumettre")
		{
		 	if(!isset($_POST['rebuild']))
		 	{
				modificationCreationQuestion("mod",$_POST['cleQuestion']);
			}
			else
			{
				$sql = "update question set " .
				" cleSujet=" . $_POST['categorie'] . 
				",generaleAcademique=" . $_POST['generaleAcademique'] .
				",bonneReponse='" . $_POST['bonneReponse'] . "'" .
				",valeurGroupeAge1=" . $_POST['niveau1'] .
				",valeurGroupeAge2=" . $_POST['niveau2'] .
				",valeurGroupeAge3=" . $_POST['niveau3'] .
				",valeurGroupeAge4=" . $_POST['niveau4'] .
				",valeurGroupeAge5=" . $_POST['niveau5'] .
				",valeurGroupeAge6=" . $_POST['niveau6'] .
				",valeurGroupeAge7=" . $_POST['niveau7'] .
				",valeurGroupeAge8=" . $_POST['niveau8'] .
				",valeurGroupeAge9=" . $_POST['niveau9'] .
				",valeurGroupeAge10=" . $_POST['niveau10'] .
				",valeurGroupeAge11=" . $_POST['niveau11'] .
				",valeurGroupeAge12=" . $_POST['niveau12'] .
				",valeurGroupeAge13=" . $_POST['niveau13'] .
				",valeurGroupeAge14=" . $_POST['niveau14'];
				$_SESSION['mysqli']->query($sql);
			}
		}
		//on veut soumettre l'ajout d'une question
		elseif($action=="ajout_soumettre")
		{
			modificationCreationQuestion("ajout",0);
		}
		//afficher les questions de l'utilisateur courant
		elseif($action=="liste_courant")
		{
			$param['cleJoueur'] = $_SESSION["joueur"]->reqCle();
			$param['texteQ']="";
			$param['texteR']="";
			$param['categorie'] = -1;
			$param['nonvalide'] = "off";
			$param['cleQuestion'] = "";
			
			if(isset($_GET['from']))
			{
				$param['from'] = $_GET['from'];
			}
			else
			{
				$param['from'] = 0;
			}
			$param['limit'] = 30;
			chercherQuestion($smarty,$param);
		}
		//afficher les chois pour chercher des questions
		elseif($action=="find")
		{
			$smarty->display('find_question.tpl');
		}
		//lancer la recherche
		elseif($action=="chercher")
		{
		 	$param['cleJoueur'] = 0;
		 	if(isset($_GET['nonvalide']))
		 	{
		 		$param['nonvalide'] = $_GET['nonvalide'];
		 	}
		 	else
		 	{
				$param['nonvalide'] = "off";
			}
			
			$param['categorie'] = $_GET['categorie'];		 	
		 	$param['cleQuestion'] = $_GET['cleQuestion'];
		 	$param['texteQ'] = $_GET['texteQ'];
		 	$param['texteR'] = $_GET['texteR'];
		 	$param['from'] = $_GET['from'];
		 	$param['limit'] = $_GET['limit'];
			chercherQuestion($smarty,$param);
			
		}
		//rendre une question valide
		elseif($action=="valide")
		{
		 	//vérifier que la personne a le droit de modifier cette question
		 	if($_SESSION['joueur']->reqCategorie()>=2)
		 	{
				valideQuestion($_GET['cleQuestionV']);
			}
		 			 	
		 	$param['cleJoueur'] = $_GET['cleJoueur'];
		 	if(isset($_GET['nonvalide']))
		 	{
		 		$param['nonvalide'] = $_GET['nonvalide'];
		 	}
		 	else
		 	{
				$param['nonvalide'] = "off";
			}
			$param['categorie'] = $_GET['categorie'];		 	
		 	$param['cleQuestion'] = $_GET['cleQuestion']; 
		 	$param['texteQ'] = $_GET['texteQ'];
		 	$param['texteR'] = $_GET['texteR'];
		 	$param['from'] = $_GET['from'];
		 	$param['limit'] = $_GET['limit'];
		 	
			chercherQuestion($smarty,$param);
			
		}
		//rendre une question non valide
		elseif($action=="non_valide")
		{
		 	//vérifier que la personne a le droit de modifier cette question
		 	if($_SESSION['joueur']->reqCategorie()>=2)
		 	{
		 		invalideQuestion($_GET['cleQuestionV']);
		 	}
		 	
		 	$param['cleJoueur'] = $_GET['cleJoueur'];
		 	if(isset($_GET['nonvalide']))
		 	{
		 		$param['nonvalide'] = $_GET['nonvalide'];
		 	}
		 	else
		 	{
				$param['nonvalide'] = "off";
			}
			
			$param['categorie'] = $_GET['categorie'];		 	
		 	$param['cleQuestion'] = $_GET['cleQuestion']; 
		 	$param['texteQ'] = $_GET['texteQ'];
		 	$param['texteR'] = $_GET['texteR'];
		 	$param['from'] = $_GET['from'];
		 	$param['limit'] = $_GET['limit'];
			chercherQuestion($smarty,$param);
		}
		//afficher le formulaire pour l'upload de fichier flash
		elseif($action=="upload")
		{
			$sql = "select * from question where cleQuestion=" . $_GET['cleQuestion'];
		 	$result = $_SESSION['mysqli']->query($sql);
		 	$row=$result->fetch_object();
		 	if(($row->cleJoueur==$_SESSION['joueur']->reqCle()) || ($_SESSION['joueur']->reqCategorie()>=2))
		 	{
				$smarty->assign('cleQuestion',$_GET['cleQuestion']);
 				$smarty->display('upload_question.tpl');
			}
		}
	}
	else
	{
	 	if(isset($_POST['action']))
	 	{
			$action=$_POST['action'];
			$cle= $_POST['cleQuestion'];
			//upload swf file
			if($action=="doUpload")
			{
				//on décplace les fichiers reçus
				move_uploaded_file($_FILES['question']['tmp_name'],QUESTION_FLASH_DIR . "MP-$cle-Q.swf");
				move_uploaded_file($_FILES['retroaction']['tmp_name'],QUESTION_FLASH_DIR . "MP-$cle-R.swf");
				
				//on met à jour les informations dans la base de données
				$sql = "update question set FichierFlashQuestion='MP-$cle-Q.swf',
					FichierFlashReponse='MP-$cle-R.swf' where cleQuestion=$cle";
				$result = $_SESSION['mysqli']->query($sql);
			}
		}
		else
		//on affiche le formulaire pour l'ajout de question
		{
			$smarty->assign('mode_question','ajout');
			$smarty->assign('valeurGroupeAge', array(0,1,2,3,4,5,6));
			$smarty->assign('choixReponse', array("a","b","c","d"));
			$smarty->display('ajout_mod_question.tpl');
		}

	}
	
    $smarty->cache_lifetime = -1;
	$smarty->display('footer.tpl');

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


//afficher le formulaire pour la modification de question
function modificationQuestion(&$smarty,$cleQuestion)
{
 	$sql = "select * from question where cleQuestion=$cleQuestion";
 	$result = $_SESSION['mysqli']->query($sql);
 	$row = $result->fetch_object();
 	 	
	$smarty->assign('mode_question','mod');
	
	$smarty->assign('cleQuestion',$cleQuestion);
	$smarty->assign('categorie' . $row->cleSujet,"selected");
	$smarty->assign('generaleAcademique' . $row->generaleAcademique,"selected");
	if($row->texteASCII != "")
	{
		$smarty->assign('questionAscii',$row->texteASCII);
		$smarty->assign('retroactionAscii',$row->retroactionASCII);
		$smarty->assign('reponseAASCII',$row->reponseAASCII);
		$smarty->assign('reponseBASCII',$row->reponseBASCII);
		$smarty->assign('reponseCASCII',$row->reponseCASCII);
		$smarty->assign('reponseDASCII',$row->reponseDASCII);
	}
	else
	{
		$smarty->assign('questionAscii',strtr($row->texteLaTeX,"\\","\r\n\r\n"));
		$smarty->assign('retroactionAscii',strtr($row->retroactionLaTex,"\\","\r\n\r\n"));
		$smarty->assign('reponseAASCII',strtr($row->reponseALaTeX,"\\","\r\n\r\n"));
		$smarty->assign('reponseBASCII',strtr($row->reponseBLaTeX,"\\","\r\n\r\n"));
		$smarty->assign('reponseCASCII',strtr($row->reponseCLaTeX,"\\","\r\n\r\n"));
		$smarty->assign('reponseDASCII',strtr($row->reponseDLaTeX,"\\","\r\n\r\n"));
	}

	
	
	$smarty->assign('choixReponse', array("a","b","c","d"));
	$smarty->assign('bonneReponse',$row->bonneReponse);
	
	$smarty->assign('valeurGroupeAge', array(0,1,2,3,4,5,6));
	$smarty->assign('valeurGroupeAge1',$row->valeurGroupeAge1);
	$smarty->assign('valeurGroupeAge2',$row->valeurGroupeAge2);
	$smarty->assign('valeurGroupeAge3',$row->valeurGroupeAge3);
	$smarty->assign('valeurGroupeAge4',$row->valeurGroupeAge4);
	$smarty->assign('valeurGroupeAge5',$row->valeurGroupeAge5);
	$smarty->assign('valeurGroupeAge6',$row->valeurGroupeAge6);
	$smarty->assign('valeurGroupeAge7',$row->valeurGroupeAge7);
	$smarty->assign('valeurGroupeAge8',$row->valeurGroupeAge8);
	$smarty->assign('valeurGroupeAge9',$row->valeurGroupeAge9);
	$smarty->assign('valeurGroupeAge10',$row->valeurGroupeAge10);
	$smarty->assign('valeurGroupeAge11',$row->valeurGroupeAge11);
	$smarty->assign('valeurGroupeAge12',$row->valeurGroupeAge12);
	$smarty->assign('valeurGroupeAge13',$row->valeurGroupeAge13);
	$smarty->assign('valeurGroupeAge14',$row->valeurGroupeAge14);

	
	$smarty->display('ajout_mod_question.tpl');
	
	
}

//rendre invalide une question
function invalideQuestion($cleQuestion)
{
	$sql = "update question set valide=0 where cleQuestion=$cleQuestion";
	
	$_SESSION['mysqli']->query($sql);
}

//rendre une question valide
function valideQuestion($cleQuestion)
{
	$sql = "update question set valide=1 where cleQuestion=$cleQuestion";
	
	$_SESSION['mysqli']->query($sql);
	
}

//chercher parmis les questions à partir des paramêtres
function chercherQuestion(&$smarty,$param)
{
 
 	if($param['from']<0)
    {
		$param['from']=0;
	}
 	
 	$champs = "question.*";
 	$from = "question";
 	$where = "";
 	$limit = " limit " . $param['from'] . "," . $param['limit'];
 	$order_by = " cleQuestion";
 	$and = false;
 	
 	//on cherche une question pour un joueur en particulier
 	if($param['cleJoueur'] != 0)
 	{
 	 	$where .= " question.cleJoueur=" . $param['cleJoueur'];
 	 	$and=true;
	}
	
	//on cherche UNE question
	if($param['cleQuestion'] != "")
	{
	 	if($and == true)
		{
			$where .= " and ";
		}
		
		$where .= " question.cleQuestion=" . $param['cleQuestion'];
		$and=true;
	}
	
	//on cherche dans une catégorie de questions
	if($param['categorie'] != -1)
	{
		if($and == true)
		{
			$where .= " and ";
		}
		$where .= " question.cleSujet=" . $param['categorie'];
		$and=true;
	}
	
	//on cherche seulement les question nonvalide?
	if($param['nonvalide'] == "on")
	{
	 	if($and == true)
		{
			$where .= " and ";
		}
		$where .= " question.valide=false";
	}
	
	//on cherche un texte dans une question
	if($param['texteQ'] != "")
	{
	 	$texteQ = $param['texteQ'];
		if($and == true)
		{
			$where .= " and ";
		}
		$where .= "(texteASCII like '%$texteQ%' or texteLaTeX like '%$texteQ%')";
	}

	//on cherche un texte dans une réponse
	if($param['texteR'] != "")
	{
	 	$texteR = $param['texteR'];
		if($and == true)
		{
			$where .= " and ";
		}
		$where .= "(texteASCII like '%$texteR%' or texteLaTeX like '%$texteR%')";
	}

	
	//construction de la requête SQL
	$sql = "select $champs from $from";
	if($where != "")
	{
		$sql .= " where $where";
	}
	$sql .= " order by $order_by";
	$sql.= $limit;
	
		
	$result = $_SESSION["mysqli"]->query($sql);
    $nb = $result->num_rows;
    
    //boucle pour chaque enregistrement
    for($i=0;$i<$nb;$i++)
    {
        $row=$result->fetch_object();
        $arr[$i]['cle'] = $row->cleQuestion;
        $arr[$i]['qFlash'] = $row->FichierFlashQuestion;
        $arr[$i]['rFlash'] = $row->FichierFlashReponse;
        $arr[$i]['qEps'] = $row->FichierEpsQuestion;
        $arr[$i]['rEps'] = $row->FichierEpsReponse;
        $arr[$i]['qPs'] = $row->FichierPsQuestion;
        $arr[$i]['rPs'] = $row->FichierPsReponse;
        $arr[$i]['typeReponse'] = $row->typeReponse;
        if($row->cleJoueur != 0)
        {
         	$sql = "select alias from joueur where cleJoueur=" . $row->cleJoueur;
         	$resultJ = $_SESSION["mysqli"]->query($sql);
        	$arr[$i]['alias'] = $resultJ->fetch_object()->alias;
        }
        $arr[$i]['valide'] = $row->valide;
    }
    
    //trouver le nombre maximal d'enregistrement pour cette requêtes
    $sql = "select count(cleQuestion) as nb from $from";
	if($where != "")
	{
		$sql .= " where $where";
	}
	$result = $_SESSION["mysqli"]->query($sql);
	$smarty->assign('max_enr',$result->fetch_object()->nb);
	
    //assigne l'enregistrement courant et le nombre d'enregistrement retourné
    $smarty->assign('from',$param['from']);
    $smarty->assign('nb_enr',$nb);
    
    //assignation des param à smarty
    $smarty->assign('cleJoueur',$param['cleJoueur']);
	$smarty->assign('limit',$param['limit']);
	$smarty->assign('texteQ',$param['texteQ']);
	$smarty->assign('texteR',$param['texteR']);
	$smarty->assign('categorie',$param['categorie']);
	$smarty->assign('cleQuestion',$param['cleQuestion']);
	$smarty->assign('nonvalide',$param['nonvalide']);
    
    if($nb>0)
	{
    	$smarty->assign('questions',$arr);
    	$smarty->assign('dossier_flash',QUESTION_FLASH_WEB_DIR);
    	$smarty->assign('dossier_eps',QUESTION_EPS_WEB_DIR);
    }
    
    $smarty->display('liste_question.tpl');

	
}


//lance le processus de création de la question
function modificationCreationQuestion($action,$cleQuestion)
{
	
	$questionLatex="";
	$retroactionLatex = "";
	
	
	//création des fichiers mathml temporaire sur le disque
 	$fichierQML = tempnam(TEMP_DIR,"mml_Q");
 	$fichierRML = tempnam(TEMP_DIR,"mml_R");
 	
	//création des fichier ml temporaire pour la question et la rétroaction 	
	$fh = fopen($fichierQML,"w");
	fwrite($fh,$_POST["outputMLQuestion"]);
	fclose($fh);

	$fh = fopen($fichierRML,"w");
	fwrite($fh,$_POST["outputMLRetroaction"]);
	fclose($fh);
	
	//écriture des données Mathml dans des fichiers temporaire
	$fichiersRep = array(tempnam(TEMP_DIR,"mml_A"),tempnam(TEMP_DIR,"mml_B"),tempnam(TEMP_DIR,"mml_C"),tempnam(TEMP_DIR,"mml_D"));
	for($i=0;$i<=3;$i++)
	{
		$fh = fopen($fichiersRep[$i],"w");
		if($i==0)
			fwrite($fh,$_POST["outputMLReponse1"]);
		elseif($i==1)
			fwrite($fh,$_POST["outputMLReponse2"]);
		elseif($i==2)
			fwrite($fh,$_POST["outputMLReponse3"]);
		else
			fwrite($fh,$_POST["outputMLReponse4"]);
		fclose($fh);
	}
	
	
	//obtient 2 nom temporaire pour le fichier question et le fichier réponse
	$fichierQTex = tempnam(TEMP_DIR,"tex_Q");
	$fichierRTex = tempnam(TEMP_DIR,"tex_R");
	
	//création des fichiers tex de la question et de la réponse
	exec("java -jar " . XALAN_JAR . " -IN $fichierQML -XSL " . FICHIER_XSL . " -OUT $fichierQTex");
	exec("java -jar " . XALAN_JAR . " -IN $fichierRML -XSL " . FICHIER_XSL . " -OUT $fichierRTex");
	
	//efface les fichiers ml temporaire de question et rétroaction
	unlink($fichierQML);
	unlink($fichierRML);
	
	//onverture et lecture du fichier tex de question
	$fh = fopen($fichierQTex,"r+");
	$questionLatex = HEADER_LATEX . "\r\n";
	$tmp="";
	while(!feof($fh)) 
	{
  		$tmp .= utf8_decode(fgets($fh, 4096));
	}
	$questionLatexSql = trim($tmp);
	$questionLatex.= trim($tmp) . "\r\n\r\n";
	fclose($fh);
	
	//onverture et lecture du fichier tex de rétroaction
	$fh = fopen($fichierRTex,"r+");
	$retroactionLatex = HEADER_LATEX . "\r\n";
	$tmp="";
	while(!feof($fh)) 
	{
  		$tmp .= utf8_decode(fgets($fh, 4096));
	}
	$retroactionLatexSql = trim($tmp);
	$retroactionLatex .= trim($tmp) . "\r\n" . FOOTER_LATEX;
	fclose($fh);
				

	//création et récupération du latex des choix de réponse
	$choix = array('a)','b)','c)','d)');
	$reponseLatex =  array('','','','');
	$fichierRepTex = tempnam(TEMP_DIR,"tmp");
	$questionLatex .= "\begin{enumerate} \r\n";
	for($i=0;$i<=3;$i++)
	{
	 	//création du fichier tex pour chaque choix de réponse
	 	exec("java -jar " . XALAN_JAR . " -IN " . $fichiersRep[$i] . " -XSL " . FICHIER_XSL . " -OUT $fichierRepTex");
	 	
	 	//suppression des fichier ml temporaire
	 	unlink($fichiersRep[$i]);
	 	
	 	//insertion de \item
	 	$questionLatex .= "\item ";
	 	
	 	//lecture du fichier tex et ajout à la question
	 	$fh = fopen($fichierRepTex,"r+");
	 	$tmp="";
		while(!feof($fh)) 
		{
  			$tmp .= utf8_decode(fgets($fh, 4096));
		}
		fclose($fh);
		$reponseLatex[$i] = $tmp;
		$questionLatex .= trim($tmp) . "\r\n";
	}
	$questionLatex .= "\end{enumerate} \r\n";
	$questionLatex .= FOOTER_LATEX;
	unlink($fichierRepTex);
	
	
	//écriture des nouveau fichier question et rétroaction latex
	$fh = fopen($fichierQTex,"w");
	fwrite($fh,$questionLatex);
	fclose($fh);
	
	$fh = fopen($fichierRTex,"w");
	fwrite($fh,$retroactionLatex);
	fclose($fh);
	
	//conversion vers le eps et ps du fichier question
	exec("cd " . TEMP_DIR . " ; " . LATEX . " -interaction=nonstopmode " . substr($fichierQTex,strlen(TEMP_DIR)+1));
	exec("dvips -q -E $fichierQTex.dvi -o $fichierQTex.eps");
	exec("dvips -q -t legal $fichierQTex.dvi -o $fichierQTex.ps");
	
	//conversion vers le eps du fichier réponse
	exec("cd " . TEMP_DIR . "; " . LATEX . " -interaction=nonstopmode " . substr($fichierRTex,strlen(TEMP_DIR)+1));
	exec("dvips -q -E $fichierRTex.dvi -o $fichierRTex.eps");
	exec("dvips -q -t legal $fichierRTex.dvi -o $fichierRTex.ps");
	
	
	//suppresion des fichiers tex,dvi,aux de question et rétroaction
	
	unlink($fichierQTex);
	unlink($fichierRTex);
	unlink("$fichierQTex.dvi");
	unlink("$fichierQTex.aux");
	unlink("$fichierQTex.log");
	unlink("$fichierRTex.dvi");
	unlink("$fichierRTex.aux");
	unlink("$fichierRTex.log");
	
	
	//vérifier le niveau d'accès de l'utilisateur
	if($_SESSION['joueur']->reqCategorie()>=2)
	{
		$valide=0;
	}
	else
	{
		$valide=0;
	}
	
	//vérifier dans quel mode on est ( ajout ou modification)
	if($action=="mod")
	{
		$sql = "update question set ";
	}
	else
	{
		$sql = "insert into question set ";
	}
	//construction de la requête sql
	$sql .= " cleSujet=" . $_POST['categorie'] . 
		",generaleAcademique=" . $_POST['generaleAcademique'] .
		",texteASCII='" . addslashes($_POST['question']) . "'" .
		",texteLaTeX='" . addslashes($questionLatexSql) . "'" .
		",texteMathML='" . addslashes($_POST["outputMLQuestion"]) . "'" .
		",retroactionASCII='" . addslashes($_POST['retroaction']) . "'" .
		",retroactionLaTeX='" . addslashes($retroactionLatexSql) . "'" .
		",retroactionMathML='" . addslashes($_POST["outputMLRetroaction"]) . "'" .
		",reponseAASCII='" . addslashes($_POST['reponse1']) . "'" .
		",reponseALaTeX='" . addslashes($reponseLatex[0]) . "'" .
		",reponseAMathML='" . addslashes($_POST["outputMLReponse1"]) . "'" .
		",reponseBASCII='" . addslashes($_POST['reponse2']) . "'" .
		",reponseBLaTeX='" . addslashes($reponseLatex[1]) . "'" .
		",reponseBMathML='" . addslashes($_POST["outputMLReponse2"]) . "'" .
		",reponseCASCII='" . addslashes($_POST['reponse3']) . "'" .
		",reponseCLaTeX='" . addslashes($reponseLatex[2]) . "'" .
		",reponseCMathML='" . addslashes($_POST["outputMLReponse3"]) . "'" .
		",reponseDASCII='" . addslashes($_POST['reponse4']) . "'" .
		",reponseDLaTeX='" . addslashes($reponseLatex[3]) . "'" .
		",reponseDMathML='" . addslashes($_POST["outputMLReponse4"]) . "'" .
		",typeReponse=0" .
		",bonneReponse='" . $_POST['bonneReponse'] . "'" .
		",valeurGroupeAge1=" . $_POST['niveau1'] .
		",valeurGroupeAge2=" . $_POST['niveau2'] .
		",valeurGroupeAge3=" . $_POST['niveau3'] .
		",valeurGroupeAge4=" . $_POST['niveau4'] .
		",valeurGroupeAge5=" . $_POST['niveau5'] .
		",valeurGroupeAge6=" . $_POST['niveau6'] .
		",valeurGroupeAge7=" . $_POST['niveau7'] .
		",valeurGroupeAge8=" . $_POST['niveau8'] .
		",valeurGroupeAge9=" . $_POST['niveau9'] .
		",valeurGroupeAge10=" . $_POST['niveau10'] .
		",valeurGroupeAge11=" . $_POST['niveau11'] .
		",valeurGroupeAge12=" . $_POST['niveau12'] .
		",valeurGroupeAge13=" . $_POST['niveau13'] .
		",valeurGroupeAge14=" . $_POST['niveau14'] .
		",valide=" . $valide; 
	if($action!="mod")
	{
		$sql .= ",cleJoueur=" . $_SESSION["joueur"]->reqCle();
	}
	
	//si on est en mode modification on ajoute la clause where cleQuestion
	if($action=="mod")
	{
		$sql .= " where cleQuestion=$cleQuestion";
		$_SESSION['mysqli']->query($sql);
		$insert_id = $cleQuestion;
	}
	else
	{
		$_SESSION['mysqli']->query($sql);
		$insert_id = $_SESSION['mysqli']->insert_id;
	}
		
	//déplace les fichier eps et ps vers le bon répertoire
	rename("$fichierRTex.eps",QUESTION_EPS_DIR . "MP-$insert_id-R.eps");
	rename("$fichierRTex.ps",QUESTION_EPS_DIR . "MP-$insert_id-R.ps");
	rename("$fichierQTex.eps",QUESTION_EPS_DIR . "MP-$insert_id-Q.eps");
	rename("$fichierQTex.ps",QUESTION_EPS_DIR . "MP-$insert_id-Q.ps");
		
	//mise à jour de la question
	$sql = "update question set FichierEpsQuestion = 'MP-$insert_id-Q.eps'" . 
			",FichierEpsReponse='MP-$insert_id-R.eps'" . 
			",FichierPsQuestion='MP-$insert_id-Q.ps'" . 
			",FichierPsReponse='MP-$insert_id-R.ps'" .
			" where cleQuestion=$insert_id";
	$_SESSION['mysqli']->query($sql);
		
	redirection("question.php?action=chercher&amp;categorie=-1&amp;cleQuestion=$insert_id&amp;from=0&amp;limit=20&amp;texteQ=&amp;texteR=",0);
}

