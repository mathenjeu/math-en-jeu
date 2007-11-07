<?php

class ImageController extends Zend_Controller_Action {
  
  function preDispatch() {
    $auth = Zend_Auth::getInstance();
    if (!$auth->hasIdentity()) {
      $this->_redirect('auth/login');
    }
  }
  
  function init() {
    $this->view->baseUrl = $this->_request->getBaseUrl();
    Zend_Loader::loadClass('Image');
    //Zend_Loader::loadClass('Zend_Registry');
    $this->view->user = Zend_Auth::getInstance()->getIdentity();
  }

  function indexAction() {
    $this->view->title = "Image index";
    
    $image = new Image();
    $this->view->images = $image->fetchAll();
  }
  
  function uploadAction() {

    if (is_uploaded_file($_FILES["file"]["tmp_name"])) {
        
        //load the registry to get the destination directory for the images
        $registry = Zend_Registry::getInstance();
        $config = $registry->get('config');
        $newname = tempnam($config->file->image->dir, "image");
        
        //convert the filename.tmp to filename.eps
        $path_info = pathinfo($newname);
        $newname = $path_info['dirname'] . DIRECTORY_SEPARATOR . current(explode(".", $path_info['basename'])) . ".eps";
        
        //do the conversion using image magick
        exec("convert " . $_FILES["file"]["tmp_name"] . " " . $newname, $result, $result_code);

        //if nothing is returned as a result that mean everything went fine
        if ($result_code == 0) {
          $this->view->message = $_FILES["file"]['name'] . " uploaded.";
          
          //add the image info to the database
          Zend_Loader::loadClass('Zend_Filter_StripTags');
          $filter = new Zend_Filter_StripTags();
          
          $logical_name = trim($filter->filter($_FILES["file"]['name']));
          $description = trim($this->_request->getPost('description'));
          $physical_name = current(explode(".", $newname));
          
          $data = array(
						'logical_name' => $logical_name,
						'description' => $description,
            'physical_name' => $physical_name,
            'user_id' => Zend_Auth::getInstance()->getIdentity()->user_id
          );
          
          $image = new Image();
          $image->insert($data);
          
        } else {
          $this->view->message = "Could not upload the file, check that the file is a valid image.";
        }
    }
    
    //$this->view->message = $_FILES["file"]["type"];
  }
  
}
