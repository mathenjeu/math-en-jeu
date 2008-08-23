<?php

class AnswerTypeInfo extends Zend_Db_Table_Abstract {
  
  protected $_name = 'answer_type_info';

    protected $_referenceMap    = array(
        'AnswerType' => array(
            'columns'						=> 'answer_type_id',
            'refTableClass'			=> 'AnswerType',
            'refColumns'				=> array('answer_type_id')
        ));
}