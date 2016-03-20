<!DOCTYPE html>

<html lang="en">
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <!-- The above 3 meta tags *must* come first in the head; any other head content must come *after* these tags -->
    <meta name="description" content="">
    <meta name="author" content="">

    <title>Elife Dashboard</title>

    <!-- Bootstrap core CSS -->
    <link rel="stylesheet" href="../bower_components/bootstrap-css-only/css/bootstrap.min.css" />

    <!-- Custom styles for this template -->
    <link href="../elife.css" rel="stylesheet">
</head>

<body ng-app="myApp">

<nav class="navbar navbar-inverse navbar-fixed-top">
    <div class="container-fluid">
        <div class="navbar-header">
            <button type="button" class="navbar-toggle collapsed" data-toggle="collapse" data-target="#navbar" aria-expanded="false" aria-controls="navbar">
                <span class="sr-only">Toggle navigation</span>
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
            </button>
            <a class="navbar-brand" href="#">eLife Dashboard</a>
        </div>
    </div>
</nav>

<div class="container-fluid" id="dashboardContent">
    <div class="row">
        <div class="col-sm-3 col-md-2 sidebar">
            <ul class="nav nav-sidebar">
                <li class="active"><a href="admin">Overview<span class="sr-only">(current)</span></a></li>
                <li><a href="admin/collectionFile">Collection file upload</a></li>
            </ul>
        </div>

        <!-- BEGIN Page specific stuff -->
        <div class="col-sm-9 col-sm-offset-3 col-md-10 col-md-offset-2 main">
            <h1 class="page-header">Dashboard</h1>

            <div class="table-responsive">
                <table class="table table-striped">
                    <thead>
                    <tr>
                        <th>#</th>
                        <th>Header</th>
                        <th>Header</th>
                        <th>Header</th>
                        <th>Header</th>
                    </tr>
                    </thead>
                    <tbody>
                    <tr>
                        <td>1,001</td>
                        <td>Lorem</td>
                        <td>ipsum</td>
                        <td>dolor</td>
                        <td>sit</td>
                    </tr>
                    </tbody>
                </table>
            </div>
        </div>
        <!-- END Page specific stuff -->

    </div>
</div>
<script src="../bower_components/angular/angular.min.js"></script>
<script src="../bower_components/angular-resource/angular-resource.min.js"></script>
<script src="../bower_components/angular-file-model/angular-file-model.js"></script>
<script src="../app/app.js"></script>
<script src="../app/controllers.js"></script>
<script src="../app/services.js"></script>
<script src="../bower_components/jquery/dist/jquery.min.js"></script>

</body>
</html>
