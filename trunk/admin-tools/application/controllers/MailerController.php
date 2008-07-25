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
    Zend_Loader::loadClass('MailTemplate');
    Zend_Loader::loadClass('Language');
    Zend_Loader::loadClass('Zend_Filter');
  }

  /**
   * Add a new mail template to the database
   *
   */
  function addAction() {
    if ($this->_request->isPost()) {
      $data = array(
            'language_id' => $this->_request->getPost('language'),
            'creation_date' => new Zend_Db_Expr('CURDATE()'),
            'subject' => utf8_decode($this->_request->getPost('subject')),
            'body' => utf8_decode($this->_request->getPost('body')));
      $mailTemplate = new MailTemplate();
      $mailTemplate->insert($data);
      
      $this->_redirect('/mailer/');
      
    } else {
      $language = new Language();
      $this->view->action = "add";
      $this->view->languages = $language->fetchAll();
    }
  }
  
  function editAction() {
    if ($this->_request->isPost()) {
      $data = array(
            'language_id' => $this->_request->getPost('language'),
            'subject' => utf8_decode($this->_request->getPost('subject')),
            'body' => utf8_decode($this->_request->getPost('body')));
      $mailTemplate = new MailTemplate();
      $id = Zend_Filter::get($this->_request->getPost('id'), 'Digits');
      $where = $mailTemplate->getAdapter()->quoteInto('mail_template_id = ?', $id);
      $mailTemplate->update($data, $where);
      
      $this->_redirect('/mailer/');
    } else {
      
      $language = new Language();
      $this->view->languages = $language->fetchAll();
      $this->view->action = "edit";
      
      $id = Zend_Filter::get($this->_request->getParam('id'), 'Digits');

      if ($id > 0) {
        $mailTemplate = new MailTemplate();
        $row = $mailTemplate->fetchAll("mail_template_id=" . $id)->current();
        if ($row != null) {
          $this->view->id = $id;
          $this->view->subject = utf8_encode($row->subject);
          $this->view->body = utf8_encode($row->body);
          $this->view->language_id = $row->language_id;
        } else {
          $this->_redirect('/mailer/');
        }
      } else {
        $this->_redirect('/mailer/');
      }

    }
  }
  
  function deleteAction() {
    if (!$this->_request->isPost()) {
      $id = Zend_Filter::get($this->_request->getParam('id'), 'Digits');
      if ($id > 0) {
        $mailTemplate = new MailTemplate();
        $where = $mailTemplate->getAdapter()->quoteInto('mail_template_id = ?', $id);
        $mailTemplate->delete($where);
        $this->_redirect('/mailer/');
      }
    }
  }
  
  function indexAction() {
    $this->view->title = "Mailer index";
    $mailTemplate = new MailTemplate();
    $mails = $mailTemplate->fetchAll();
    $this->view->mails = $mails;
  }
  
  function sendAction() {
    if ($this->_request->isPost()) {
      $registry = Zend_Registry::getInstance();
      $config = $registry->get('config');
      
      //get login info for the smtp server
      $smtp = array('username' => $config->mail->smtp->username,
                  'password' => $config->mail->smtp->password);
      
      $transport = new Zend_Mail_Transport_Smtp($config->mail->smtp->server, $smtp);
      
      //get the mail data
      $id = Zend_Filter::get($this->_request->getParam('id'), 'Digits');
      $mailTemplate = new MailTemplate();
      $row = $mailTemplate->fetchAll("mail_template_id=" . $id)->current();
      if($row == null) {
        $this->_redirect('/mailer/');
      }
      $subject = $row->subject;
      $body = $row->body;
      
      
      //build the message
      $mail = new Zend_Mail();
      $mail->setSubject(subject);
      $mail->setBodyHtml($body);
      $mail->setFrom($config->mail->from, $config->mail->fromname);
      
        
      $message = array();
      // parse the textarea data to get email
      // format is "Full name" <mail@adress.com>,
      $lines = explode(",", $this->_request->getPost('to'));
      foreach($lines as $line) {
        if(preg_match("/\"(.*)\" <(.*)>/", $line, $matches) != 0) {
          $copy = clone $mail; 
          $copy->addTo($matches[2], utf8_decode($matches[1])); 
          try {
            $copy->send($transport);
            $message[$matches[1] . $matches[2]] = array('recipient' => Zend_Filter::get(utf8_decode($line), 'HtmlEntities'),
                                    'success' => 1);
          } catch (Zend_Mail_Protocol_Exception $e) {

            $message[$line] = array('recipient' => Zend_Filter::get(utf8_decode($line), 'HtmlEntities'),
                                    'success' => 0,
                                    'message' => $e->getMessage());
                                                  
          }
          unset($copy);
        }
      }
      $this->view->title = "Send a mail.";
      $mail = new MailTemplate();
      $row = $mail->fetchAll("mail_template_id=" . $id)->current();
      if ($row != null) {
        $this->view->mail = $row;
      }
      $this->view->message = $message;

    } else {
      
      $id = Zend_Filter::get($this->_request->getParam('id'), 'Digits');
      if ($id > 0) {
        $this->view->title = "Send a mail.";
        $mail = new MailTemplate();
        $row = $mail->fetchAll("mail_template_id=" . $id)->current();
        if ($row != null) {
          $this->view->mail = $row;
        } else {
          $this->_redirect('/mailer/');
        }
      } else {
        $this->_redirect('/mailer/');
      }

    }

  }
  
}
