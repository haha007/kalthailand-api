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
                <li><a href="/admin">Overview</a></li>
                <li class="active"><a href="/admin/collectionFile">Collection file upload<span class="sr-only">(current)</span></a></li>
            </ul>
        </div>

        <!-- BEGIN Page specific stuff -->
        <style>
            .form-upload {
                border: 1px solid #eee;
                margin-bottom: 20px;
                padding: 5px;
            }
        </style>
        <div class="col-sm-9 col-sm-offset-3 col-md-10 col-md-offset-2 main" ng-controller="AppController">
            <div class="page-header">
                <h1>List of all Collection file</h1>
            </div>
            <form name="formUpload" ng-submit="upload($event)" class="form-inline form-upload">
                <div class="form-group">
                    <label for="fileToUpload">Choose a collection file to upload</label>
                    <input type="file" file-model="file" id="fileToUpload">
                </div>
                <button type="submit" class="btn btn-primary btn-danger">Submit</button>
            </form>
            <div class="alert alert-info" role="alert" ng-hide="collectionFiles && collectionFiles.length > 0">
                There are no collection file yet.
            </div>
            <div class="alert alert-danger" role="alert" ng-show="errorMessage">
                <span class="glyphicon glyphicon-exclamation-sign" aria-hidden="true"></span>
                {{ errorMessage }}
            </div>
            <table class="table table-bordered table-striped table-hover table-condensed">
                <tr>
                    <td>Received Date</td>
                    <td>Job Starting Date</td>
                    <td>Job Ending Date</td>
                    <td>Number of lines</td>
                    <td>Generated lines</td>
                </tr>
                <tr ng-repeat="collectionFile in collectionFiles"
                    ng-class="{
                        'success': collectionFile.deductionFile.lines.length && collectionFile.lines.length == collectionFile.deductionFile.lines.length,
                        'danger': collectionFile.deductionFile.lines.length && collectionFile.lines.length != collectionFile.deductionFile.lines.length
                    }">
                    <td>{{ collectionFile.receivedDate | date:'yyyy-MM-dd HH:mm:ss Z'}}</td>
                    <td>{{ collectionFile.jobStartedDate | date:'yyyy-MM-dd HH:mm:ss Z'}}</td>
                    <td>{{ collectionFile.jobEndedDate | date:'yyyy-MM-dd HH:mm:ss Z'}}</td>
                    <td>{{ collectionFile.lines.length }}</td>
                    <td>{{ collectionFile.deductionFile.lines.length }}</td>
                </tr>
            </table>
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
