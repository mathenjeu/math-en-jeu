<?php

class Level extends Zend_Db_Table_Abstract {
  
  protected $_name = 'level';
  
  protected $_dependentTables = array('QuestionLevel');
  
  
}