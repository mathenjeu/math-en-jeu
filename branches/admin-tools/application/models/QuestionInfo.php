<?php

class QuestionInfo extends Zend_Db_Table_Abstract
{
  protected $_name = 'question_info';
  
  protected $_dependentTables = array('Comment', 'QuestionGroupQuestion');
  
  protected $_referenceMap    = array(
        'Question' => array(
            'columns'						=> 'question_id',
            'refTableClass'			=> 'Question'
        ),
        'Creator' => array(
            'columns'           => 'user_id',
            'refTableClass'     => 'User',
            'refColumns'        => 'user_id'
        ),
        'Language' => array(
            'columns'           => 'language_id',
            'refTableClass'     => 'Language',
            'refColumns'        => 'language_id'
        ));
        

}
