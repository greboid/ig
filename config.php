<?php
    $usernames = array();
    $safeusernames = array();

    if (file_exists(__DIR__ . '/config.local.php')) {
        include(__DIR__ . '/config.local.php');
    }
