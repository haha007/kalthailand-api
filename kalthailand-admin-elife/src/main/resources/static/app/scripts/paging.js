/*
 http://jasonwatmore.com/post/2016/01/31/angularjs-pagination-example-with-logic-like-google
 */
function DataTable(data, pageSize) {
    this.data = undefined;
    this.dataInPage = undefined;
    this.pager = undefined;

    this.construct(data, pageSize);
}
DataTable.prototype.construct = function (data, pageSize) {
    if (!hasValue(data)) {
        data = [];
    }
    this.data = data;
    this.pager = new Pager(data.length, pageSize);
    this.pager.calculatePages(0);
    this.dataInPage = this.calculateDataInPage(this.data, this.pager);
};
DataTable.prototype.calculateDataInPage = function (data, pager) {
    var dataInPage = [];
    var startIndex = pager.startIndex;
    var endIndex = pager.endIndex;
    if (startIndex >= 0 && endIndex >= 0) {
        for (i = startIndex; i <= endIndex; i++) {
            var row = data[i];
            row.$$index = i;
            dataInPage.push(row);
        }
    }
    return dataInPage;
};
DataTable.prototype.setPage = function (currentPage) {
    this.pager.calculatePages(currentPage);
    this.dataInPage = this.calculateDataInPage(this.data, this.pager);
};
function Pager(totalItems, pageSize) {
    this.MAX_PAGES_TO_SHOW = 10;
    this.MAX_PAGES_BEFORE_CURRENT = 4;//Before current page will have maximum 4 pages.
    this.MAX_PAGES_AFTER_CURRENT = 4;//After current page will have maximum 4 pages.

    this.totalItems = totalItems;
    this.pageSize = pageSize;

    this.currentPage = undefined;
    this.startPage = undefined;
    this.endPage = undefined;
    this.totalPages = undefined;
    this.startPage = undefined;
    this.endPage = undefined;
    this.startIndex = undefined;
    this.endIndex = undefined;
    this.pages = undefined;

    this.calculatePages(0);
}

// service implementation
Pager.prototype.calculatePages = function (currentPage) {
    var maxPagesToShow = this.MAX_PAGES_TO_SHOW;
    var maxPagesFromBeginning = this.MAX_PAGES_BEFORE_CURRENT;//Before current page will have maximum 4 pages.
    var maxPagesToEnding = this.MAX_PAGES_AFTER_CURRENT;//After current page will have maximum 4 pages.

    var totalItems = this.totalItems;
    var pageSize = this.pageSize;

    // default to first page
    currentPage = currentPage || 0;

    // calculate total pages
    var totalPages = Math.ceil(totalItems / pageSize);

    var startPage = undefined;
    var endPage = undefined;

    if (totalPages <= maxPagesToShow) {
        // less than 10 total pages so show all
        startPage = 0;
        endPage = totalPages - 1;
    } else {
        // more than 10 total pages so calculate start and end pages
        if (currentPage <= (maxPagesToShow - maxPagesFromBeginning)) {
            startPage = 0;
            endPage = maxPagesToShow - 1;
        } else if (currentPage + maxPagesToEnding >= totalPages) {
            startPage = totalPages - (maxPagesToShow - 1) - 1;
            endPage = totalPages - 1;
        } else {
            startPage = currentPage - (maxPagesFromBeginning + 1);
            endPage = currentPage + maxPagesToEnding;
        }
    }

    // calculate start and end item indexes
    var startIndex = (currentPage) * pageSize;
    var endIndex = Math.min(startIndex + pageSize - 1, totalItems - 1);

    // create an array of pages to ng-repeat in the pager control
    //var pages = _.range(startPage, endPage + 1);
    var pages = Array.prototype.newArray(startPage, endPage + 1);

    // return object with all pager properties required by the view
    this.currentPage = currentPage;
    this.startPage = startPage;
    this.endPage = endPage;
    this.totalPages = totalPages;
    this.startPage = startPage;
    this.endPage = endPage;
    this.startIndex = startIndex;
    this.endIndex = endIndex;
    this.pages = pages;
};