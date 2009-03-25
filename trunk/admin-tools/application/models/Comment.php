<?php

class Comment extends Zend_Db_Table_Abstract {
  
  protected $_name = 'comment';

  protected $_referenceMap    = array(
          'Language' => array(
                  'columns'                     => 'language_id',
                  'refTableClass'               => 'Language',
                  'refColumns'                  => 'language_id'
                  ),
          'User' => array(
                  'columns'                     => 'user_id',
                  'refTableClass'               => 'User',
                  'refColumns'                  => 'user_id'
                  ),
          'QuestionInfo' => array(
                  'columns'                     => array('question_id','language_id'),
                  'refTableClass'               => 'QuestionInfo',
                  'refColumns'                  => array('question_id','language_id')
                  )
          );
}
