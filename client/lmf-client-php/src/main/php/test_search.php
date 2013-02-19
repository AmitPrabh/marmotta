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
 * Date: 27.01.12
 * Time: 15:55
 * To change this template use File | Settings | File Templates.
 */

require_once 'autoload.php';

use LMFClient\ClientConfiguration;
use LMFClient\Clients\SearchClient;

$config = new ClientConfiguration("http://localhost:8080/LMF");

$client = new SearchClient($config);

foreach($client->simpleSearch("dc","summary:Sepp") as $result) {
    echo "Result: " . $result->uri . "\n";
}

?>