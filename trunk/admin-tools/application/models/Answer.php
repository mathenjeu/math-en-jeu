<?php

class Answer extends Zend_Db_Table_Abstract
{
  protected $_name = 'answer';

  protected $_dependentTables = array('AnswerInfo','AnswerFrequency');
  
  protected $_referenceMap    = array(
          'Question' => array(
                  'columns'                     => 'question_id',
                  'refTableClass'               => 'Question',
                  'refColumns'                  => 'question_id'
                  )
          );
}
