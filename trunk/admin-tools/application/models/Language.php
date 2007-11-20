<?php
class Language extends Zend_Db_Table_Abstract
{
  protected $_name = 'language';
  
  protected $_dependentTables = 
    array('User', 
          'QuestionInfo', 
          'Comment', 
          'Subject', 
          'CategoryInfo',
          'MailTemplate');

}