<?php

class Question extends Zend_Db_Table_Abstract {
  
  protected $_name = 'question';
  
  protected $_dependentTables = array('QuestionInfo');
  
  
  protected $_referenceMap    = array(
        'Category' => array(
            'columns'						=> 'category_id',
            'refTableClass'			=> 'Category',
            'refColumns'				=> 'category_id'
        ),
        'AnswerType' => array(
            'columns'						=> 'answer_type_id',
            'refTableClass'			=> 'AnswerType',
            'refColumns'				=> 'answer_type_id'
        ));
        

}
