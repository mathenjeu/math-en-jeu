<?php

error_reporting(E_ALL|E_STRICT);

date_default_timezone_set('America/Montreal');
set_include_path('.' . PATH_SEPARATOR . './library' 
  . PATH_SEPARATOR . './application/models/'
  . PATH_SEPARATOR . './application/views/scripts/question/'
  . PATH_SEPARATOR . get_include_path());

include 'Zend/Loader/Autoloader.php';
$autoloader = Zend_Loader_Autoloader::getInstance();
$autoloader->setFallbackAutoloader(true);

// load configuration
$config = new Zend_Config_Ini('./application/config.ini', 'general');
$registry = Zend_Registry::getInstance();
$registry->set('config', $config);


// setup database
$db = Zend_Db::factory($config->db->adapter, $config->db->config->toArray());
$db->query("SET NAMES 'utf8' COLLATE 'utf8_unicode_ci'");
Zend_Db_Table::setDefaultAdapter($db);
Zend_Registry::set('db', $db);

require_once 'Zend/Controller/Front.php';	
$frontController = Zend_Controller_Front::getInstance();	
$frontController->throwExceptions(true);

try {
	$frontController->setControllerDirectory('application/controllers');
        Zend_Layout::startMvc(array('layoutPath'=>'application/views/layouts'));
        $frontController->dispatch();
}
catch (Exception $e) {
        echo $e->getMessage();
        // Consider using Zend_Log to log the error
}

?>
