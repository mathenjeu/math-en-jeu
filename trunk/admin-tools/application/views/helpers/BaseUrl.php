<?php
class Zend_View_Helper_BaseUrl
{
  function baseUrl()
  {
    $fcontroller = Zend_Controller_Front::getInstance();
    return $fcontroller->getBaseUrl();
  }
}
