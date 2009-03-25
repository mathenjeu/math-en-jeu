<?php

class QuestionInfo extends Zend_Db_Table_Abstract {
  
  protected $_name = 'question_info';
  protected $_primary = array('question_id','language_id');

  protected $_dependentTables = array('Comment');
  
  protected $_referenceMap    = array(
          'Language' => array(
                  'columns'                     => 'language_id',
                  'refTableClass'               => 'Language',
                  'refColumns'                  => 'language_id'
                  ),
          'Creator' => array(
                  'columns'                     => 'user_id',
                  'refTableClass'               => 'User',
                  'refColumns'                  => 'user_id'
                  ),
          'Question' => array(
                  'columns'                     => 'question_id',
                  'refTableClass'               => 'Question',
                  'refColumns'                  => 'question_id'
                  )
          );
}
