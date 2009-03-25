<?php

class Title extends Zend_Db_Table_Abstract
{
  protected $_name = 'title';

  protected $_dependentTables = array('Question');
}
