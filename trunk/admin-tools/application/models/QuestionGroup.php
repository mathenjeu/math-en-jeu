<?php

class QuestionGroup extends Zend_Db_Table_Abstract
{
  protected $_name = 'question_group';

  protected $_dependentTables = array('QuestionGroupQuestion');
  
  
}