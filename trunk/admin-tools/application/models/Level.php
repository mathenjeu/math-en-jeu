<?php
class Level extends Zend_Db_Table_Abstract
{
  protected $_name = 'level';
  protected $_primary = array('level_id','language_id');

  protected $_dependentTables = array('QuestionLevel','AnswerFrequency');
}
