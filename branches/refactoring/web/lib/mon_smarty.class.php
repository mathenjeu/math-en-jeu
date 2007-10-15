<?php

require_once(LIB_DIR . "/Smarty/Smarty.class.php");

//extension de la classe Smarty pour automatiquement
//inclure les dossier de compilation et de templates
class MonSmarty extends Smarty
{
     function MonSmarty($langage)
     {
        // Constructeur de la classe.
        // Appel� automatiquement � l'instanciation de la classe.
        $this->Smarty();
        $this->caching = 0;		//controle de la cache pour chaque fichier
        $this->template_dir = TEMPLATE_DIR;
        $this->compile_dir = LIB_DIR . '/Smarty/templates_c';
        $this->config_dir = LIB_DIR . '/Smarty/configs';
        $this->cache_dir = LIB_DIR . '/Smarty/cache';
            
        global $lang;
        $this->assign('lang', $lang);
        $this->assign('language',$langage);
        $this->assign('template',TEMPLATE);
        $this->assign("sid",strip_tags(SID));
        
        /* 
        if(isset($_SESSION['css']) && file_exists(TEMPLATE . $_SESSION['css']))
        {
        	$this->assign('fichier_css',TEMPLATE . $_SESSION['css']);
        }
        else
        {
          //$_SESSION['css'] = CSS_FILE;
          $this->assign('fichier_css',TEMPLATE . "/" . CSS_FILE);
        }
        */
        $this->assign('fichier_css',TEMPLATE . "/" . CSS_FILE);
        	
        $this->assign('loc_template',TEMPLATE);
     }
}
?>