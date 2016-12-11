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

    ymaps.ready(function () {
        var latitude = $('#geo-data').attr('latitude').text();
        var longitude = $('#geo-data').attr('longitude').text()

        var map = new ymaps.Map("adv-detail-map", {
            center: [latitude, longitude],
            controls: ["zoomControl"],
            zoom: 11
        });
        map.behaviors.disable('scrollZoom');

        var myPlacemark = new ymaps.Placemark([latitude, longitude], {}, {
            preset: 'islands#redIcon'
        });
        map.geoObjects.add(myPlacemark);
    });
}());