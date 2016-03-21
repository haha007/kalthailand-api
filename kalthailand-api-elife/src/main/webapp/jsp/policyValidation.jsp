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
                <li><a href="/admin/collectionFile">Collection file upload</a></li>
                <li class="active"><a href="/admin/policyValidation">Policy Validation<span class="sr-only">(current)</span></a></li>
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
        <div class="col-sm-9 col-sm-offset-3 col-md-10 col-md-offset-2 main" ng-controller="ValidationController">
            <div class="page-header">
                <h1>Search for Policy to validate</h1>
            </div>
            <form class="form-horizontal" ng-submit="search($event)">
                <div class="form-group">
                    <label for="policyID" class="col-sm-4 control-label">Policy number</label>
                    <div class="col-sm-4">
                        <input type="text" class="form-control" id="policyID" ng-model="policyID">
                    </div>
                </div>
                <div class="form-group">
                    <div class="col-sm-offset-4 col-sm-8">
                        <button type="submit" class="btn btn-default">Search</button>
                    </div>
                </div>
            </form>
            <div class="alert alert-danger" role="alert" ng-show="errorMessage">
                <span class="glyphicon glyphicon-exclamation-sign" aria-hidden="true"></span>
                Unable to validate this policy. {{ errorMessage }}
            </div>
            <form class="form-horizontal">
                <div class="row">
                    <div class="col-sm-6">
                        <div class="form-group">
                            <label class="col-sm-4 control-label">Policy ID</label>
                            <div class="col-sm-8">
                                <p class="form-control-static">{{ policyDetail.policyId }}</p>
                            </div>
                        </div>
                        <div class="form-group">
                            <label class="col-sm-4 control-label">First Name</label>
                            <div class="col-sm-8">
                                <p class="form-control-static">{{ policyDetail.insureds[0].person.givenName }}</p>
                            </div>
                        </div>
                        <div class="form-group">
                            <label class="col-sm-4 control-label">Date of birth</label>
                            <div class="col-sm-8">
                                <p class="form-control-static">{{ policyDetail.insureds[0].person.birthDate }}</p>
                            </div>
                        </div>
                        <div class="form-group">
                            <label class="col-sm-4 control-label">Premium amount</label>
                            <div class="col-sm-8">
                                <p class="form-control-static">{{ policyDetail.premiumsData.financialScheduler.modalAmount.value }}</p>
                            </div>
                        </div>
                        <div class="form-group">
                            <label class="col-sm-4 control-label">Premium periodicity</label>
                            <div class="col-sm-8">
                                <p class="form-control-static">{{ policyDetail.premiumsData.financialScheduler.modalAmount.periodicity.code }}</p>
                            </div>
                        </div>
                    </div>
                    <div class="col-sm-6">
                        <div class="form-group">
                            <label class="col-sm-4 control-label">Status</label>
                            <div class="col-sm-8">
                                <p class="form-control-static">{{ policyDetail.status }}</p>
                            </div>
                        </div>
                        <div class="form-group">
                            <label class="col-sm-4 control-label">Last Name</label>
                            <div class="col-sm-8">
                                <p class="form-control-static">{{ policyDetail.insureds[0].person.surName }}</p>
                            </div>
                        </div>
                        <div class="form-group">
                            <label class="col-sm-4 control-label">Thai ID</label>
                            <div class="col-sm-8">
                                <p class="form-control-static">{{ policyDetail.insureds[0].person.registrations[0].id }}</p>
                            </div>
                        </div>
                        <div class="form-group">
                            <label class="col-sm-4 control-label">Premium currency</label>
                            <div class="col-sm-8">
                                <p class="form-control-static">{{ policyDetail.premiumsData.financialScheduler.modalAmount.currencyCode }}</p>
                            </div>
                        </div>
                        <div class="form-group">
                            <label class="col-sm-4 control-label">Premium end date</label>
                            <div class="col-sm-8">
                                <p class="form-control-static">{{ policyDetail.premiumsData.financialScheduler.modalAmount.endDate }}</p>
                            </div>
                        </div>
                    </div>
                </div>
            </form>
            <div class="row">
                <div class="col-md-12">
                    <form class="form">
                        <div class="form-group">
                            <div class="col-sm-offset-4 col-sm-8">
                                <button type="submit" class="btn btn-default btn-success">Validate</button>
                            </div>
                        </div>
                    </form>
                </div>
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
