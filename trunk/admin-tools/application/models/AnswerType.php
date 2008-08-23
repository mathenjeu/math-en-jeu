<?php

class AnswerType extends Zend_Db_Table_Abstract {
  
  protected $_name = 'answer_type';
  
  protected $_dependentTables = array('Question', 'AnswerTypeInfo');
  
}