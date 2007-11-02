<?php

class QuestionController extends Zend_Controller_Action {
	
	
  function preDispatch() {
    $auth = Zend_Auth::getInstance();
    if (!$auth->hasIdentity()) {
      $this->_redirect('auth/login');
    }
  }

  function init() {
    $this->view->baseUrl = $this->_request->getBaseUrl();
    $this->view->user = Zend_Auth::getInstance()->getIdentity();
  }
  
  function indexAction() {
    $this->view->title = "Image index";
    
    $image = new Image();
    $this->view->images = $image->fetchAll();
  }
  
	
}
