<?php
class Zend_View_Helper_EchoIfNotEmpty
{
        function echoIfNotEmpty($string1, $string2, $prefix)
        {
                if (!empty($string1)) return $string1;
                return $prefix . "" . $string2;
        }
}
