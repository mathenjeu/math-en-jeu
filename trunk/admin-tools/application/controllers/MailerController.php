<?php

class MailerController extends Zend_Controller_Action {
  
  function preDispatch() {
    $auth = Zend_Auth::getInstance();
    if (!$auth->hasIdentity()) {
      $this->_redirect('auth/login');
    }
  }

  function init() {
    $this->view->baseUrl = $this->_request->getBaseUrl();
    $this->view->user = Zend_Auth::getInstance()->getIdentity();
    Zend_Loader::loadClass('Zend_Mail');
    Zend_Loader::loadClass('Zend_Mail_Transport_Smtp');
  }

  function indexAction() {
    $this->view->title = "Mailer";
    
  }
  
  function sendAction() {
    if ($this->_request->isPost()) {
      $registry = Zend_Registry::getInstance();
      $config = $registry->get('config');
      
      //get login info for the smtp server
      $smtp = array('username' => $config->mail->smtp->username,
                  'password' => $config->mail->smtp->password);
      
      $transport = new Zend_Mail_Transport_Smtp($config->mail->smtp->server, $smtp);
      
      //build the message
      $mail = new Zend_Mail();
      $mail->setSubject($this->_request->getPost('subject'));
      $mail->setBodyHtml($this->_request->getPost('body'));
      $mail->setFrom($config->mail->from, $config->mail->fromname);
      
        
      // parse the textarea data to get email
      // format is "Full name" <mail@adress.com>,
      $lines = explode(",", $this->_request->getPost('to'));
      foreach($lines as $line) {
        preg_match("/\"(.*)\" <(.*)>/", $line, $matches);
        $copy = clone $mail; 
        $copy->addTo($matches[2], $matches[1]); 
        $copy->send($transport); 
        unset($copy);       
      }
            
    }
    
    
  }
  
}
