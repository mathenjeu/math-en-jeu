<?php

class ImageController extends Zend_Controller_Action {
  
  function init() {
    $this->view->baseUrl = $this->_request->getBaseUrl();
    Zend_Loader::loadClass('Image');
  }

  function indexAction() {
    $this->view->title = "Image index";
    
    $image = new Image();
    $this->view->images = $image->fetchAll();
  }
  
}
