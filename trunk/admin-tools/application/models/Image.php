<?php
class Image extends Zend_Db_Table_Abstract
{
  protected $_name = 'image';
  protected $_dependentTables = array('User');
  
  
  protected $_referenceMap    = array(
        'Creator' => array(
            'columns'           => 'user_id',
            'refTableClass'     => 'User',
            'refColumns'        => 'user_id'
        ));


}