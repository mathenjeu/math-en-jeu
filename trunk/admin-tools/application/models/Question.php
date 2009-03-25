<?php

class Question extends Zend_Db_Table_Abstract {
  
  protected $_name = 'question';
  
  protected $_dependentTables = array('QuestionInfo','QuestionLevel','Answer');
  
  
  protected $_referenceMap    = array(
          'Category' => array(
                  'columns'                     => 'category_id',
                  'refTableClass'               => 'Category',
                  'refColumns'                  => 'category_id'
                  ),
          'AnswerType' => array(
                  'columns'                     => 'answer_type_id',
                  'refTableClass'               => 'AnswerType',
                  'refColumns'                  => 'answer_type_id'
                  ),
          'Source' => array(
                  'columns'                     => 'source_id',
                  'refTableClass'               => 'Source',
                  'refColumns'                  => 'source_id'
                  ),
          'Title' => array(
                  'columns'                     => 'title_id',
                  'refTableClass'               => 'Title',
                  'refColumns'                  => 'title_id'
                  )
          );
          
}
