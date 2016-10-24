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
            $.get("/user/" + advertId, function (html) {
                showUser.replaceWith(html);
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