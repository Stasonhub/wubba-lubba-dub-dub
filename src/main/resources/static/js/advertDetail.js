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
                if (xhr.status == 403) {
                    window.location.href = "/login";
                }
            });
        });

    });

    ymaps.ready(function () {
        var map = new ymaps.Map("adv-detail-map", {
            center: [55.76, 37.64],
            controls: ["zoomControl"],
            zoom: 13
        });
        map.behaviors.disable('scrollZoom');
    });
}());