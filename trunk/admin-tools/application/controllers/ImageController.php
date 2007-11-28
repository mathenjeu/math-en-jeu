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
    Zend_Loader::loadClass('Zend_Filter');
    //Zend_Loader::loadClass('Zend_Registry');
    $this->view->user = Zend_Auth::getInstance()->getIdentity();
    
    $registry = Zend_Registry::getInstance();
    $config = $registry->get('config');
    
    $this->view->image_url = $config->file->image->url;
  }

  function indexAction() {
    $this->view->title = "Image index";
    
    $image = new Image();
    $this->view->images = $image->fetchAll();
  }
  
  function deleteAction() {
    if (!$this->_request->isPost()) {
      $id = Zend_Filter::get($this->_request->getParam('id'), 'Digits');
      if ($id > 0) {
        $image = new Image();
        $where = $image->getAdapter()->quoteInto('image_id = ?', $id);
        
        
        //delete the file
        $registry = Zend_Registry::getInstance();
        $config = $registry->get('config');
        if (!@unlink($config->file->image->dir . DIRECTORY_SEPARATOR . $image->physical_name)) {
          $this->view->message = "Unable to physicaly remove the file : " . $config->file->image->dir . DIRECTORY_SEPARATOR . $image->physical_name;
        }
        @unlink($config->file->image->html->dir . DIRECTORY_SEPARATOR . str_replace(".eps",".jpeg",$image->physical_name));
        
        $image->delete($where);
        //$this->_redirect('/image/');
      }
    }
  }
  
  function uploadAction() {
    $this->view->title = "Add an image.";
    if ($this->_request->isPost()) {
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

          //if there is already another image with this name, 
          //get a temp one
          $image = new Image();
          $row = $image->fetchAll("physical_name='" . current(explode(".",$_FILES["file"]["name"])) . ".eps'")->current();
          if ($row != null) {
            $physical_name = current(explode(".", $path_info['basename'])) . ".eps";
          } else {
            $physical_name = current(explode(".",$_FILES["file"]["name"])) . ".eps";
          }
          
          //try to move the file
          if (rename($newname, $config->file->image->dir . DIRECTORY_SEPARATOR . $physical_name)) {
            
            //convert the file back to png
            exec("convert " . $config->file->image->dir . DIRECTORY_SEPARATOR . $physical_name 
              . " " . $config->file->image->html->dir . DIRECTORY_SEPARATOR . current(explode(".",$physical_name)) . ".jpeg", 
              $result, 
              $result_code);
            
            //add the image info to the database
            Zend_Loader::loadClass('Zend_Filter_StripTags');
            $filter = new Zend_Filter_StripTags();
            
            $logical_name = trim($filter->filter($_FILES["file"]['name']));
            $description = trim($filter->filter($this->_request->getPost('description')));
            //$physical_name = $path_info['basename'] . ".eps"; //current(explode(".", $newname));
            
            $data = array(
              'logical_name' => $logical_name,
              'description' => $description,
              'physical_name' => $physical_name,
              'user_id' => Zend_Auth::getInstance()->getIdentity()->user_id
            );
            
            $image = new Image();
            $image->insert($data);
            
            $this->view->message = "Image " . $_FILES["file"]['name'] . " uploaded.";
          } else {
            $this->view->message = "Unable to move the file, make sure the destination image dir exist.";
          }

        } else {
          $this->view->message = "Could not upload the file, check that the file is a valid image.";
        }
      }
    }

  }
  
}
