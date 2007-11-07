<?php

class Comment extends Zend_Db_Table_Abstract {
  
  protected $_name = 'comment';
  
  protected $_referenceMap    = array(
        'Comment' => array(
            'columns'           => array('question_id','language_id'),
            'refTableClass'     => 'QuestionInfo',
            'refColumns'        => array('question_id','language_id'))
        );
  
  
}