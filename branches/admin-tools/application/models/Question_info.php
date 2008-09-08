<?php

class QuestionInfo extends Zend_Db_Table_Abstract
{
  protected $_name = 'question_info';
  protected $_dependentTables = array('User', 'Comment', 'Language');

  
  protected $_referenceMap    = array(
        'Creator' => array(
            'columns'           => 'user_id',
            'refTableClass'     => 'User',
            'refColumns'        => 'user_id'
        ));

}
