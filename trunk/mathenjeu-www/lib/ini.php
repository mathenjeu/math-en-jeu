<?php
/*******************************************************************************
Fichier : ini.php
Auteur : Maxime Bégin
Description : regroupe toutes les fonctions d'initialisation

********************************************************************************
10-11-2006 Maxime Bégin - Ajout de constante pour la connexion à un serveur SMTP
	pour l'envoie de courriel
26-07-2006 Maxime Bégin - modification de DOC_ROOT en constante et non plus en
	variable globale.
04-07-2006 Maxime Bégin - Ajout d'un fichier xml pour les configurations
21-06-2006 Maxime Bégin - Ajout de commentaires.
30-05-2006 Maxime Bégin - Version initiale
*******************************************************************************/

ini_set('display_errors','1');


define("SQL_DEBUG",1);      //mettre en commentaire pour passer au mode release
define("CONTRAT_DEBUG",1);  //mettre en commentaire pour passer au mode release

//on cherche le dossier racine 
$chemin = strtr(__FILE__,'\\',"/");
define('LIB_DIR',substr($chemin,0,strrpos($chemin,'/')));
define('DOC_ROOT',(substr(LIB_DIR,0,strrpos(LIB_DIR,'/')+1)));		//dossier racine du site web ( ex : /var/www/html/mathenjeu )
define('LOG_DIR',DOC_ROOT . "log/");

//inclus les fichier requis pour faire fonctionner le site web
require_once(LIB_DIR . "/Smarty/Smarty.class.php");
require_once(LIB_DIR . "/phpmailer/class.phpmailer.php");
require_once(LIB_DIR . "/util.php");
require_once(LIB_DIR . "/exception.class.php");
require_once(LIB_DIR . "/mon_mysqli.class.php");
require_once(LIB_DIR . "/utilisateur.class.php");
require_once(LIB_DIR . "/joueur.class.php");
require_once(LIB_DIR . "/admin.class.php");
require_once(LIB_DIR . "/nouvelles.class.php");
require_once(LIB_DIR . "/sondage.class.php");
require_once(LIB_DIR . "/groupe.class.php");
require_once(LIB_DIR . "/superadmin.class.php");
require_once(LIB_DIR . "/faq.class.php");
require_once(LIB_DIR . "/clog.class.php");


define("LANGAGE_DIR",DOC_ROOT . "langage/");
define("CONFIG_FILE", DOC_ROOT . "/config/configuration.xml");
$config = simplexml_load_file(CONFIG_FILE);

require_once(LANGAGE_DIR . $config->langue . "/lang_main.php");

define("MAX_NB_NOUVELLES",(int)$config->nbNouvelles);       	//nombre maximal de nouvelles à afficher
define("MAX_NB_JOUEURS_PALMARES",(int)$config->nbJoueurs);   	//nombre de joueurs pour les palmarès
define("MIN_NB_PARTIE_PALMARES",(int)$config->minParties);     	//nombre de partie minimal pour les palmarès
define("NB_JOUR_PALMARES",(int)$config->nbJours);          		//nombre de jour dans le calcul des palmarès
define("ADRESSE_SITE_WEB",(string)$config->adresseWeb);			//"http://www.smac.ulaval.ca/mathenjeu/jeu_version2/");
define('TEMPLATES_DIR',DOC_ROOT . "templates/");				//dossier des templates
define('TEMPLATE','templates/' . $config->template . "/");  	//dossier du template utilisé
define('TEMPLATE_DIR',DOC_ROOT . "/" . TEMPLATE);				//chemin complet du template utilisé
define('CSS_FILE',(string)$config->css);						//fichier css utilisé

define("NOM_COURRIEL",(string)$config->nomCourriel);			//le nom utilisé lorsqu'on envoie des courriels
define("ADRESSE_COURRIEL",(string)$config->courriel);			//l'adresse de courriel utilisé
define("PORT_SMTP",(string)$config->portSMTP);					//le port du serveur SMTP
define("SERVEUR_SMTP",(string)$config->serveurSMTP);			//l'adresse du serveur SMTP
define("USER_SMTP",(string)$config->utilisateurSMTP);			//le nom d'utilisateur du serveur SMTP
define("PASS_SMTP",(string)$config->motDePasseSMTP);			//le mot de passe du serveur SMTP

define('SUJET_COURRIEL_INSCRIPTION',utf8_decode((string)$config->sujet_courriel_inscription));	//sujet du courriel de l'inscription
define('COURRIEL_INSCRIPTION',utf8_decode((string)$config->courriel_inscription));				//courriel envoyé lors de l'inscription
define('SUJET_COURRIEL_PASS_PERDU',utf8_decode((string)$config->sujet_courriel_pass_perdu));	//sujet du courriel de récupération de mot de passe
define('COURRIEL_PASS_PERDU',utf8_decode((string)$config->courriel_pass_perdu));				//courriel envoyé pour les nom d'usager ou les mot de passe perdu

//constante utile pour la création des questions
define("TEMP_DIR",(string)$config->tempDir);					//temporary dir for the questions creation
define("LATEX",(string)$config->latexApp);						//latex application command
define("FICHIER_XSL",(string)$config->xslFile);					//xsl fil to be used with xalan
define("XALAN_JAR",(string)$config->xalanApp);					//xalan jar file
define("HEADER_LATEX",(string)$config->latexHeader);			//latex header
define("FOOTER_LATEX",(string) $config->latexFooter);			//latex footer
define("QUESTION_EPS_DIR",(string)$config->epsDir);				//eps/ps dir
define("QUESTION_FLASH_DIR",(string)$config->flashDir);			//flash dir
define("QUESTION_EPS_WEB_DIR",(string)$config->epsWebDir);		//eps/ps web dir
define("QUESTION_FLASH_WEB_DIR",(string)$config->flashWebDir);	//flash web dir

define("IMAGE_DIR",DOC_ROOT . "img/sujet");						//dossier des images pour les nouvelles
define("LOG_FILE",DOC_ROOT . "/log/log.txt");					//fichier pour les logs


//définir les configurations de session
//ini_set('session.gc_maxlifetime',1800);
ini_set('session.cookie_lifetime',1800);
ini_set('session.use_trans_sid',1);
//ini_set('session.use_only_cookies',1);

//si la session n'existe pas on la débute
if(session_id()=="")
{
  //débute la session
  session_start();
}


//extension de la classe Smarty pour automatiquement
//inclure les dossier de compilation et de templates
class MonSmarty extends Smarty
{
     function MonSmarty()
     {
        // Constructeur de la classe.
        // Appelé automatiquement à l'instanciation de la classe.
        $this->Smarty();
        $this->caching = 0;		//controle de la cache pour chaque fichier
        $this->template_dir = TEMPLATE_DIR;
        $this->compile_dir = LIB_DIR . '/Smarty/templates_c';
        $this->config_dir = LIB_DIR . '/Smarty/configs';
        $this->cache_dir = LIB_DIR . '/Smarty/cache';
            
        global $lang;
        $this->assign('lang', $lang);
        $this->assign('template',TEMPLATE);
        $this->assign("sid",strip_tags(SID));
                
        if(isset($_SESSION['css']) && file_exists(TEMPLATE . $_SESSION['css']))
        {
        	$this->assign('fichier_css',TEMPLATE . $_SESSION['css']);
        }
        else
        {
          $_SESSION['css'] = CSS_FILE;
          $this->assign('fichier_css',TEMPLATE . "/" . CSS_FILE);
        }
        	
        $this->assign('loc_template',TEMPLATE);
     }
}

//si l'objet mon_mysqli n'existe pas on le crée et l'ajoute
//à la super-globale $_SESSION
if(!isset($_SESSION["mysqli"]))
{
    $mysqli=new mon_mysqli((string)$config->dbHote,
                (string)$config->dbUtilisateur,
                (string)$config->dbMotDePasse,
                (string)$config->dbSchema);
    
    $_SESSION["mysqli"]=$mysqli;
}

//session_destroy();




