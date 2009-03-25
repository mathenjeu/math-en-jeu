<?php
class Language extends Zend_Db_Table_Abstract
{
  protected $_name = 'language';
  
  protected $_dependentTables = array('Level','CategoryInfo','SubjectInfo','AnswerTypeInfo','QuestionInfo','Comment','AnswerInfo','User','MailTemplate');

}