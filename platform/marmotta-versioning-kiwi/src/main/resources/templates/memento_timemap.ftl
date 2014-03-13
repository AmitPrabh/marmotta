<#--

    Licensed to the Apache Software Foundation (ASF) under one
    or more contributor license agreements. See the NOTICE file
    distributed with this work for additional information
    regarding copyright ownership. The ASF licenses this file
    to you under the Apache License, Version 2.0 (the
    "License"); you may not use this file except in compliance
    with the License.  You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

-->
<!DOCTYPE html>
<html lang="en">

<head>
    <title>Timemap in HTML</title>
    <meta http-equiv="Content-Type" content="text/html;charset=utf-8"/>
    <script type="text/javascript" src="${baseUri}webjars/jquery/1.8.2/jquery.min.js"></script>
    <link href="${SERVER_URL}${DEFAULT_STYLE}style.css" rel="stylesheet" type="text/css" />
    <link href="${SERVER_URL}${DEFAULT_STYLE}rdfhtml.css" rel="stylesheet" type="text/css" />

    <script type="text/javascript" src="http://graves.cl/timeknots/src/d3.v2.min.js"></script>
    <script type="text/javascript" src="http://graves.cl/timeknots/src/timeknots-min.js"></script>
</head>

<body>

<div id="wrapper">
    <div id="header">
        <a id="logo" href="${SERVER_URL}">
            <img src="${SERVER_URL}${LOGO}">
        </a>
        <h1 style="left:200px">Memento Timemap</h1>
        <div class="clean"></div>
    </div>
    <div id="center" style="width: 100%">
        <div id="content">
            <div id="timeknots" style="width:100%"></div>
<script type="text/javascript">
var v = [
<#list versions as version>
{'name':"${version.date}", 'date':new Date("${version.tstamp}")},
</#list>
{'name':"now", 'date':new Date()}
];
TimeKnots.draw("#timeknots", v, {dateFormat: "%Y", color: "#696", showLabels: true, labelFormat: "%Y-%m-%d %H:%M:%S"});
</script>
            <table class="simple_table">
                <tr>
                    <th>Verions</th>
                </tr>
                <#list versions as version>
                <tr>
                    <td><a target="_blank" href="${version.uri}" class="ldcache">${version.date}</a></td>
                </tr>
            </#list>
            </table>
        </div>
    </div>

    <div class="clear"></div>
    <div id="footer">
        <div id="footer_line">
            <span>
            ${FOOTER}<br>
                The version access is following the <a href="http://www.mementoweb.org/">Memento</a> principles.
            </span>
        </div>
    </div>
</div>
</body>

</html>
