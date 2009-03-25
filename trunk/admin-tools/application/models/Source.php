<?php

class Source extends Zend_Db_Table_Abstract
{
  protected $_name = 'source';

  protected $_dependentTables = array('Question');
}
