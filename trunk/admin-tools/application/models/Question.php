<?php

class Question extends Zend_Db_Table_Abstract
{
  protected $_name = 'question';
  
  protected $_dependentTables = array('QuestionInfo');
  
  
  


}
