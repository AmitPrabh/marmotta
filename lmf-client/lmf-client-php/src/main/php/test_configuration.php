<?php
/*
 * Copyright (C) 2013 Salzburg Research.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * Created by IntelliJ IDEA.
 * User: sschaffe
 * Date: 25.01.12
 * Time: 15:49
 * To change this template use File | Settings | File Templates.
 */
require_once 'autoload.php';

use LMFClient\ClientConfiguration;
use LMFClient\Clients\ConfigurationClient;

$config = new ClientConfiguration("http://localhost:8080/LMF");

$client = new ConfigurationClient($config);

//var_dump($client->listConfigurationKeys());

//var_dump($client->listConfigurations("solr"));

$client->setConfiguration("lmfclient.php.test1","1234");
$client->setConfiguration("lmfclient.php.test2",array("1234","abc"));

var_dump($client->getConfiguration("lmfclient.php.test1"));
var_dump($client->getConfiguration("lmfclient.php.test2"));

$client->deleteConfiguration("lmfclient.php.test1");
$client->deleteConfiguration("lmfclient.php.test2");


var_dump($client->getConfiguration("lmfclient.php.test1"));
var_dump($client->getConfiguration("lmfclient.php.test2"));

?>