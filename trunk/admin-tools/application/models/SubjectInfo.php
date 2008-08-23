<?php

class SubjectInfo extends Zend_Db_Table_Abstract
{
  protected $_name = 'subject_info';
  
  
    protected $_referenceMap    = array(
        'Subject' => array(
            'columns'						=> 'subject_id',
            'refTableClass'			=> 'Subject',
            'refColumns'				=> array('subject_id', 'name')
        ));
  
}