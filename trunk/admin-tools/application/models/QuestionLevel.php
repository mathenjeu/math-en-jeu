<?php

class QuestionLevel extends Zend_Db_Table_Abstract {
  
  protected $_name = 'question_level';
  protected $_primary = array('question_id','level_id');

  protected $_referenceMap    = array(
          'Level' => array(
                  'columns'                     => 'level_id',
                  'refTableClass'               => 'Level',
                  'refColumns'                  => 'level_id'
                  ),
          'Question' => array(
                  'columns'                     => 'question_id',
                  'refTableClass'               => 'Question',
                  'refColumns'                  => 'question_id'
                  )
          );
}
