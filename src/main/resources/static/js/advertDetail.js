;(function () {

    'use strict';

    $(document).ready(function () {
        $('#gal2').lightGallery({
            download: false,
            autoplayControls: false,
            actualSize: false,
            selector: '.gallery-item'
        });


        var showUser = $('#show-user');
        showUser.on('click', function () {
            var advertId = showUser.attr('advertId');
            $.post("/user", {"advertId": advertId}, function (data, status) {
                showUser.replaceWith(data);
            }).fail(function (xhr, ajaxOptions, thrownError) {
                if (xhr.status == 401) {
                    $('#login-modal').modal('show');
                }
            });
        });

    });
}());