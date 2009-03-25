<?php

class IndexController extends Zend_Controller_Action {
  function preDispatch() {
    $auth = Zend_Auth::getInstance();
    if (!$auth->hasIdentity()) {
      $this->_redirect('auth/login');
    }
  }

  function init() {
    $this->view->user = Zend_Auth::getInstance()->getIdentity();
  }

  function indexAction() {
    $this->view->title = "Index";   
  }
  
}
